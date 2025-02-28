package com.itextos.beacon.errorlog;

import java.io.File;

public class FolderCreation {

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
}
