package com.itextos.beacon.smslog;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogCustomFormatter extends Formatter {

	 @Override
	    public String format(LogRecord record) {
	        // Format the log message without class name and method name
	        return new Date() + " : " + record.getMessage() + "\n";
	    }
}
