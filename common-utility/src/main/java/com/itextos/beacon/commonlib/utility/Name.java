package com.itextos.beacon.commonlib.utility;

public class Name {
	
    public static String getCurrentMethodName() {
        // Get the stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // The first two elements are Thread.getStackTrace and getCurrentMethodName
        // The third element is the method calling getCurrentMethodName
        // So, the method name is in the third element of the stack trace
        String methodName = stackTrace[2].getMethodName();

        return methodName;
    }
    
    public static String getClassName() {
    	// Get the stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        String className = stackTrace[2].getClassName();

        return className;

	}
    
    
    public static int getLineNumber() {
    	// Get the stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        StackTraceElement callingMethod = stackTrace[2];

        return callingMethod.getLineNumber();

	}
    
   
}
