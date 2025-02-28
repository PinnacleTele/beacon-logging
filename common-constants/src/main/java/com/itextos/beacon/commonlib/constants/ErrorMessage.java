package com.itextos.beacon.commonlib.constants;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorMessage {

	 public static String getStackTraceAsString(Exception e) {
		 
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw);
	        e.printStackTrace(pw);
	        return sw.toString();
	    }
	 
	 public static String getStackTraceAsString(Throwable e) {
		 
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw);
	        e.printStackTrace(pw);
	        return sw.toString();
	    }
}
