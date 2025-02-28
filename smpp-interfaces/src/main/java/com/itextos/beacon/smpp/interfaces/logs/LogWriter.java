package com.itextos.beacon.smpp.interfaces.logs;

import java.util.Map;

import com.itextos.beacon.smslog.SmppLog;

public class LogWriter {

	private static LogWriter obj= new LogWriter();
	
	private LogWriter() {
		
	}
	
	public static LogWriter getInstance() {
		
		if(obj==null) {
			
			obj = new LogWriter();
		} 
		
		return obj;
	}
	
	public void logs(String filename,Map<String ,String> data) {
	
		
		SmppLog.log(data.toString()+"\n");
	}
}
