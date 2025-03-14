package com.itextos.beacon.smppsimulator.interfaces.monitor;

import com.itextos.beacon.smppsimulator.interfaces.logs.LogWriter;
import com.itextos.beacon.smppsimulator.interfaces.sessionhandlers.ItextosSessionManager;

public class Monitor extends Thread {

	
	public void run() {
		
		while(true) {
			
			System.out.println("Monitoring Started");
			gotosleep();
			System.out.println("Monitoring Awake");

			LogWriter.getInstance().logs("livebind", ItextosSessionManager.getInstance().getBindDetails());
		}
	}

	private void gotosleep() {
		
		
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
