package com.itextos.beacon.platform.msgtool.servlet;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class App {

    private static final Log                log                               = LogFactory.getLog(App.class);

    private static boolean IS_START_PROMETHEUS=false;
    
    public static void foldercreaton(String folderPath) {
        

        // Create a File object representing the directory
        File folder = new File(folderPath);

        // Check if the directory exists
        if (!folder.exists()) {
            // Attempt to create the directory
            if (folder.mkdirs()) {
                System.out.println("Directory created successfully: " + folderPath);
            } else {
                System.out.println("Failed to create directory: " + folderPath);
            }
        } else {
            System.out.println("Directory already exists: " + folderPath);
        }
    }
    
    public static void createfolder() {

    	System.setProperty("common.property.file.location", "/global.properties");
    	System.setProperty("log4j.configurationFile", "file:/log4j2-common.xml");
    	System.setProperty("prometheus.jetty.port", "1075");

    	foldercreaton("/opt/jboss/wildfly/logs/dnp");
    	foldercreaton("/opt/jboss/wildfly/logs/http");

    	foldercreaton("/opt/jboss/wildfly/logs/k2e");

    	foldercreaton("/opt/jboss/wildfly/logs/topic");
		foldercreaton("/opt/jboss/wildfly/logs/table2db");
		foldercreaton("/opt/jboss/wildfly/logs/consumer");
		foldercreaton("/opt/jboss/wildfly/logs/producer");
		foldercreaton("/opt/jboss/wildfly/logs/kafkasender");
		foldercreaton("/opt/jboss/wildfly/logs/executorlog1");
		foldercreaton("/opt/jboss/wildfly/logs/executorlog2");
		foldercreaton("/opt/jboss/wildfly/logs/application");
		foldercreaton("/opt/jboss/wildfly/logs/kafkareceiver");
		foldercreaton("/opt/jboss/wildfly/logs/timetaken");
		foldercreaton("/opt/jboss/wildfly/logs/aux");

		
		/*
		try {
			AppendToHosts.appendCustomHostsToSystemHosts();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
    }
}
