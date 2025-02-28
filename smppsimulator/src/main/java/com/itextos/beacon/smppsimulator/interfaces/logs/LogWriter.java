package com.itextos.beacon.smppsimulator.interfaces.logs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Date;
import java.util.Map;

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
	
		
		   try{
			   Date date=new Date();
			      FileWriter file = new FileWriter("/logs/"+filename+"_"+date.getHours()+".log",true);

			      // Creates a BufferedWriter
			      BufferedWriter output = new BufferedWriter(file);

			      // Writes the string to the file
			      output.write(date+"\t"+ data.toString());
			      output.newLine();
			      output.newLine();
			      output.newLine();
			      output.newLine();

			      // Closes the writer
			      output.close();
			   }catch(Exception e){
				   e.printStackTrace();
			   }
	}
}
