package com.ninet.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LocalIPMapTableUtil {
	
	public Map<String, String> ipMapTables = null;
	
	public LocalIPMapTableUtil() {
		this.ipMapTables = new HashMap<String, String>();
	}
	
	public Map<String, String> getIPMapTables() {
		BufferedReader bReader = loadIPMapFile("./src/dnsrelay.txt");
		if(bReader != null) {
			String line = new String();
			try {
				while((line = bReader.readLine()) != null) {
					createIPMap(line);
				}
				System.out.println("Load successfully.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			System.out.println("Load fail. It is null.");
		}
		try {
			bReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this.ipMapTables;
	}
	
	private void createIPMap(String line) {
		int splitPos = line.indexOf(' ');
		if(splitPos > 0) {
			(this.ipMapTables).put(line.substring(splitPos + 1).trim(), line.substring(0, splitPos).trim());
		}
	}
	
	private BufferedReader loadIPMapFile(String filePath) {
		BufferedReader bReader = null;
		FileReader fReader = null;
		try {
			fReader = new FileReader(new File(filePath));
			bReader = new BufferedReader(fReader);
			System.out.println("Loading 'dnsrelay.txt'...");
			//fReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return bReader;
	}
	
}
