package com.itextos.beacon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AppendToHosts {

    
    public static void appendCustomHostsToSystemHosts() throws IOException {
    	 String customHostsFilePath = "/custom_hosts_production.properties"; // Path to your custom hosts file
         String hostsFilePath = "/etc/hosts"; // Path to the system hosts file

    	try (BufferedReader br = new BufferedReader(new FileReader(customHostsFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(hostsFilePath, true))) { // 'true' for append mode

            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine(); // Write a newline after each entry
            }
        }catch(Exception e) {
        	
        	e.printStackTrace();
        }
    }
}

