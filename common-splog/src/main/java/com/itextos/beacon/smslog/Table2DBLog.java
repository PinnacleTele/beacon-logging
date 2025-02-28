package com.itextos.beacon.smslog;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Table2DBLog {

    private final Logger logger = Logger.getLogger(Table2DBLog.class.getName());
    private static LinkedHashMap<String, Table2DBLog> objmap = new LinkedHashMap<>();

    private String label; // Label for the log entries

    // Public method to get the singleton instance for each topic
    public static synchronized Table2DBLog getInstance(String topicname) {
        synchronized (objmap) {
        	Table2DBLog obj = objmap.get(topicname);
            if (obj == null) {
                obj = new Table2DBLog(topicname);
                objmap.put(topicname, obj);
            }
            return obj;
        }
    }

    // Private constructor to prevent instantiation without a label
    private Table2DBLog() {
    }

    // Constructor with topic name (label)
    private Table2DBLog(String topicname) {
        this.label = topicname; // Set the label for the logger

        int limit = 1024 * 5; // 1 MB file size limit
        int count = 1; // Number of log files

        String logFileNamePattern = "/opt/jboss/wildfly/logs/topic/topiclog_" + topicname + ".%g.log";
        Level loglevel = Level.INFO;

        String loglevelFromEnv = System.getenv("loglevel");
        if (loglevelFromEnv != null) {
            if (loglevelFromEnv.equalsIgnoreCase("all")) {
                loglevel = Level.ALL;
            } else if (loglevelFromEnv.equalsIgnoreCase("off")) {
                loglevel = Level.OFF;
            }
        }

        // Create a FileHandler with the specified log file name
        FileHandler fileHandler = null;
        try {
            fileHandler = new FileHandler(logFileNamePattern, limit, count, true);

            // Set the logging level for the handler
            fileHandler.setLevel(loglevel);

            // Set a formatter for the handler (optional)
            fileHandler.setFormatter(new SimpleFormatter());

            // Add the handler to the logger
            logger.addHandler(fileHandler);
            logger.setLevel(loglevel);

        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    // Method to log a message with category and label
    public void log(String category, String message) {
        String logMessage = String.format("[%s] [%s]: %s", category, label, message);
        logger.info(logMessage);
    }

    // Overloaded method to log without specifying a category (default to "GENERAL")
    public void log(String message) {
        log("GENERAL", message);
    }
}
