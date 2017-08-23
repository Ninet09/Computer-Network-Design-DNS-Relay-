package com.ninet.util;

public class DomainName {
	String domainName;
	boolean isIPv6;

	public DomainName(String domainName, boolean isIPv6) {
		this.domainName = domainName;
		this.isIPv6 = isIPv6;
	}
	
	public String getDomainNameStr() {
		return domainName;
	}
	public boolean IsIPv6() {
		return isIPv6;
	}
}
