package com.itextos.beacon.platform.ch;

import com.itextos.beacon.platform.ch.processor.CarrierHandoverProcess;

public class Test {

	public static void main(String[] args) {

		String s=CarrierHandoverProcess.getHashValue("234,567");
		
		System.out.println(s);
	}

}
