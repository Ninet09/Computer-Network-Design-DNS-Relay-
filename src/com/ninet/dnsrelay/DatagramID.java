package com.ninet.dnsrelay;

import java.net.InetAddress;

public class DatagramID {
	
	private Integer id;
	private InetAddress address;
	private int port;
	
	public DatagramID(byte[] data, InetAddress address, int port) {
		this.id = (int) data[0];
		this.address = address;
		this.port = port;
	}
	
	public Integer getId() {
		return id;
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}
	
}
