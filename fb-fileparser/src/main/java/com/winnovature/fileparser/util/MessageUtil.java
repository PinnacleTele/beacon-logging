package com.winnovature.fileparser.util;

import java.io.UnsupportedEncodingException;

public class MessageUtil {
	public static String stringToHexString(String msg) throws Exception {

		byte[] byteArr = null;
		try {
			byteArr = msg.getBytes("UTF-16");
		} catch (UnsupportedEncodingException e) {
			throw e;
		}

		StringBuffer sb = new StringBuffer(byteArr.length * 2);
		for (int i = 0; i < byteArr.length; i++) {
			int v = byteArr[i] & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}

		// Removing header from hex string
		String hexStringafterheader = "";

		hexStringafterheader = sb.toString();
		// to remove header

		if (hexStringafterheader.indexOf("FEFF") != -1) {
			hexStringafterheader = hexStringafterheader
					.substring(hexStringafterheader.lastIndexOf("FEFF") + 4);

		}

		if (hexStringafterheader.indexOf("feff") != -1) {
			hexStringafterheader = hexStringafterheader
					.substring(hexStringafterheader.lastIndexOf("feff") + 4);

		}

		// System.out.println(sb.toString().toUpperCase());
		return hexStringafterheader;

		// System.out.println(sb.toString().toUpperCase());
		// return sb.toString().toUpperCase();
	}
	
	public static boolean isHeader(String dest) {
		boolean isHeader = true;
		try {
			Long.parseLong(dest);
			isHeader = false;
		}catch(NumberFormatException nfe) {
			isHeader = dest.trim().toLowerCase().startsWith("mobile");
		}catch(Exception e) {
			isHeader = false;
		}
		return isHeader;
	}
}
