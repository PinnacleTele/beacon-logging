package com.winnovature.utils.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.inmemdata.account.ClientAccountDetails;
import com.itextos.beacon.inmemdata.account.UserInfo;

public class UserDetails {

	static Log log = LogFactory.getLog(Constants.UtilsLogger);
	private static final String className = "[UserDetails]";

	public static UserInfo getUserInfo(String clientId) throws Exception {
		String methodName = " [getUserInfo] ";
		UserInfo userInfo = null;
		try {
			userInfo = ClientAccountDetails.getUserDetailsByClientId(clientId);
		} catch (Exception e) {
			log.error(className + methodName + " Exception ::: ", e);
			throw e;
		} catch (Throwable t) {
			log.error(className + methodName + " Throwable ::: ", t);
			throw new Exception(t.getMessage());
		}
		return userInfo;
	}

}
