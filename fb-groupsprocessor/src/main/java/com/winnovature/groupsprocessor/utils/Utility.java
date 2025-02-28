package com.winnovature.groupsprocessor.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Utility {

	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);
	private static final String className = "[Utility]";

	public static Map<String, String> constructGroupDetails(String[] columns) throws Exception {
		String methodName = " [constructGroupDetails] ";
		Map<String, String> contactDetails = new HashMap<String, String>();
		try {
			// mobile is needed while storing contacts
			contactDetails.put("mobile", columns[1]);

			if (StringUtils.isNotBlank(columns[2])) {
				contactDetails.put("name", columns[2].trim());
			}

			if (StringUtils.isNotBlank(columns[3])) {
				contactDetails.put("email", columns[3].trim());
			}

		} catch (Exception e) {
			log.error(className + methodName + " Exception : " + columns + " " + e.getMessage());
		}
		return contactDetails;
	}
	
	public static long getTimeDifference(Instant start) {
		Instant finish = Instant.now();
		return Duration.between(start, finish).toMillis(); 
	}
	
	public static String frameFileName(String campaignId, String groupId) {
		String filename = "";
		String uuid = UUID.randomUUID().toString();
		try {
			filename = campaignId+"_"+groupId+"_"+uuid+".csv";
		}catch(Exception e) {
			filename = uuid+".csv";
		}
		return filename;
	}

}
