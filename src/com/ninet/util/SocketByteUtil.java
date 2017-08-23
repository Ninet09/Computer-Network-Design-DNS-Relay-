package com.ninet.util;

import java.util.ArrayList;

public class SocketByteUtil {
	
	public static byte[] ipAddressToByteArray(String ipAddress) {	
		byte[] byteArray = new byte[4];
		int point1Pos = ipAddress.indexOf(".");
		int point2Pos = ipAddress.indexOf(".", point1Pos + 1);
		int point3Pos = ipAddress.indexOf(".", point2Pos + 1);
		byteArray[0] = (byte) Integer.parseInt(ipAddress.substring(0,point1Pos));
		byteArray[1] = (byte) Integer.parseInt(ipAddress.substring(point1Pos + 1, point2Pos));
		byteArray[2] = (byte) Integer.parseInt(ipAddress.substring(point2Pos + 1, point3Pos));
		byteArray[3] = (byte) Integer.parseInt(ipAddress.substring(point3Pos + 1));
		return byteArray;
	}
	
	public static byte[] byteArrayAppend(byte[] array1, byte[] array2) {
		byte[] newArray = new byte[array1.length + array2.length];
		System.arraycopy(array1,0,newArray,0,array1.length);
		System.arraycopy(array2,0,newArray,array1.length,array2.length);
		return newArray;
	}
	
	public static DomainName getDomainName(byte[] data) {
		boolean IPv6Flag = false;
		ArrayList<Integer> segmentLengths = new ArrayList<Integer>(); 
		int length = 0;
		int cursor = 12;
		while(data[cursor] != 0x00) {		
			segmentLengths.add((int)data[cursor]);
			cursor += data[cursor];
			cursor ++;
		}
		if (data[cursor + 1] == 0x00 && data[cursor + 2] == 0x1c) {
			IPv6Flag = true; 
		}
		for (int i = 0; i < segmentLengths.size(); ++i) {
			length += (int) segmentLengths.get(i);
		}
		String dataStr = new String(data, 0, data.length);
		String questionStr = dataStr.substring(13, 12 + length + segmentLengths.size());
		StringBuffer sBuffer = new StringBuffer(questionStr);
		int segmentCursor = 0;
		for(int i = 0; i < segmentLengths.size() - 1; i++) {
			segmentCursor += segmentLengths.get(i);
			sBuffer.setCharAt(segmentCursor, '.');
			segmentCursor += 1;
		}
		questionStr = sBuffer.toString();
		return new DomainName(questionStr, IPv6Flag);
	}
	
}
