package com.winnovature.initialstate.utils;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Utility {
	static Log log = LogFactory.getLog(Constants.InitialStageLogger);
	
	// TODO - Method to get data from memdata.jar or user_acctconf_details_view
	public static void getUserAccountDetails(String esmeaddr, Map<String, String> sendSmsReqMap) {
		//FetchUserAccDetails fetchUserAccDetails = new FetchUserAccDetails();
		//UserAccDetails userAccDetails = fetchUserAccDetails.fetchUserDetailsFromDB(esmeaddr);
		// All the below values taken from userAccDetails
		sendSmsReqMap.put("USERNAME", "TestUser");
		sendSmsReqMap.put("ACODE", "TestAcode");
		sendSmsReqMap.put("MSGCLASS","1");
		sendSmsReqMap.put("CLUSTER", "1");
		/*
		if (userAccDetails.getAdditionalFieldsMap() != null) {
			sendSmsReqMap.put("segment", userAccDetails
					.getAdditionalFieldsMap().get("segment"));
		}
		*/
		sendSmsReqMap.put("segment", String.valueOf(1));
		sendSmsReqMap.put("WID", String.valueOf(1));
		sendSmsReqMap.put("IS_DLT", String.valueOf(1));
		//sendSmsReqMap.put("msg_type", userAccDetails.getMsgType());
		sendSmsReqMap.put("account_msg_type", "1");
	}
	
	// TODO - Method to get data from memdata.jar and txn_msg_patterns
	public static boolean allowAllMsg(String esmeAddr) {
		/*
		FetchUserAccDetails fetch = new FetchUserAccDetails();
		try {
			UserAccDetails userAccDet = fetch.fetchUserDetails(esmeAddr);

			if (userAccDet != null && userAccDet.getSpamFilter() == 0
					&& new GenericDao().checkMessagePattern(esmeAddr))
				return true;

		} catch (Exception nfe) {
			nfe.printStackTrace();
			return false;
		}
		return false;
		*/
		return true;
	}
	
	// TODO - Method to implement intl redis check
	public static boolean checkIntlService(String esmeAddr, int retry)
			throws Exception{
		return true;
	}
	
	
}
