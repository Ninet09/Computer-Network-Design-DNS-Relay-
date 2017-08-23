package com.ninet.dnsrelay;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.ninet.util.DomainName;
import com.ninet.util.LocalIPMapTableUtil;
import com.ninet.util.SocketByteUtil;

public class DNSRelay {

	static byte[] data = null;
	static Map<String, String> ipMapTables = null;
	static Map<Integer, DatagramID> datagramIDMapTable = new HashMap<Integer,DatagramID>();
	static DatagramPacket receivePacket = null;
	static DatagramSocket socket = null;
	static InetAddress inetAddress = null;
	static private InetAddress resolverAddress;
	static private int resolverPort;
	static private boolean isIPv6 = false;

	private static void init() {
		try {
			inetAddress = InetAddress.getByAddress(new byte[] { 10, 3, 9, 6 });
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		LocalIPMapTableUtil tableUtil = new LocalIPMapTableUtil();
		ipMapTables = tableUtil.getIPMapTables();
		try {
			socket = new DatagramSocket(53);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		byte[] buf = new byte[4096];
		receivePacket = new DatagramPacket(buf, buf.length);
		while (true) {
			try {
				socket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			data = receivePacket.getData();
			if ((data[2] & 0x80) == 0x00) {
				query();
			} else {
				response();
			}
		}
	}

	private static void query() {
		resolverAddress = receivePacket.getAddress();
		resolverPort = receivePacket.getPort();
		DomainName domainName = SocketByteUtil.getDomainName(data);
		String domainNameStr = domainName.getDomainNameStr();
		isIPv6 = domainName.IsIPv6();
		if (ipMapTables.containsKey(domainNameStr)) {
			localQuery(domainNameStr);
		} else {
			forwardQuery(domainNameStr);
		}
	}

	private static void localQuery(String domainNameStr) {
		if ("0.0.0.0".equals(ipMapTables.get(domainNameStr))) {
			zeroIPLocalQuery(domainNameStr);
		} else {
			if(isIPv6) {
				ipv6Query(domainNameStr);
			}else {
				ipv4LocalQuery(domainNameStr);
			}
		}
	}

	private static void zeroIPLocalQuery(String domainNameStr) {
		// Build a DNS response(flag = 0x8183) and using sendto().
		data[2] = (byte) (data[2] | 0x81);
		data[3] = (byte) (data[3] | 0x83);
		DatagramPacket forwardPacket = new DatagramPacket(data, data.length, receivePacket.getAddress(),
				receivePacket.getPort());
		try {
			socket.send(forwardPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		isIPv6 = false;
		System.out.println("[Local Zero Query]\tDomain Name: " + domainNameStr + "\tNo such name (reply code =0011)");
	}
	
	private static void ipv6Query(String domainNameStr) {
		System.out.print("[IPV6]");
		forwardQuery(domainNameStr);
	}
	
	private static void ipv4LocalQuery(String domainNameStr) {
		// Build a DNS response(flag = 0x8180) and using sendto().
		data[2] = (byte) (data[2] | 0x81);
		data[3] = (byte) (data[3] | 0x80);
		data[6] = (byte) (data[6] | 0x00);
		data[7] = (byte) (data[7] | 0x01); // answer: 1
		byte[] answerHead = new byte[] { (byte) 0xc0, 0x0c, 0x00, 0x01, 0x00, 0x01, 0x00, 0x01, 0x51, (byte) 0x80, 0x00,
				0x04 };
		byte[] ipArray = SocketByteUtil.ipAddressToByteArray(ipMapTables.get(domainNameStr));
		byte[] answer = SocketByteUtil.byteArrayAppend(answerHead, ipArray);
		byte[] forwardData = data;
		System.arraycopy(answer, 0, forwardData, domainNameStr.length() + 2 + 16, answer.length);
		DatagramPacket forwardPacket = new DatagramPacket(forwardData, forwardData.length, receivePacket.getAddress(),
				receivePacket.getPort());
		try {
			socket.send(forwardPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("[IPV4 Query]\tDomain Name: " + domainNameStr);
	}

	private static void forwardQuery(String domainNameStr) {
		DatagramPacket forwardPacket = null;
		try {
			forwardPacket = new DatagramPacket(data, data.length, inetAddress, 53);
			socket.send(forwardPacket);
			isIPv6 = false;
			DatagramID datagramID = new DatagramID(data, resolverAddress, resolverPort);
			datagramIDMapTable.put(datagramID.getId(), datagramID);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("[Forward Query]\tDomain Name: " + domainNameStr);
	}

	private static void response() {
		DatagramPacket forwardPacket = null;
		try {
			int id = data[0];
			if (datagramIDMapTable.containsKey(id)) {
				DatagramID datagramID = datagramIDMapTable.get(id);
				forwardPacket = new DatagramPacket(data, data.length, datagramID.getAddress(), datagramID.getPort());
				socket.send(forwardPacket);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		init();
	}

}
