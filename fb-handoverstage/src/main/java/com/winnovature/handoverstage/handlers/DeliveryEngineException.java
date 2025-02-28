package com.winnovature.handoverstage.handlers;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.handoverstage.utils.Constants;

public class DeliveryEngineException {
	static Log log = LogFactory.getLog(Constants.HandoverStageLogger);
	private static final String className = "[DeliveryEngineException]";
	String methodName = null;

	private Map<String, String> mapObject;

	public DeliveryEngineException(Map<String, String> mapObject) {
		this.mapObject = mapObject;
	}

	public void handleException(Exception ex) throws Exception {

		methodName = "handleException";
		String fileName = mapObject.get("fileloc");
		String id = mapObject.get("c_f_s_id");
		//String dupChk = mapObject.get("remove_dupe_yn");
		//String userid = mapObject.get("cli_id");
		//String tagid = mapObject.get("cm_id");

		//int intDupChk = Integer.parseInt(dupChk);
		//long longUserid = Long.parseLong(userid);
		//long longTagid = Long.parseLong(tagid);
		//String username = mapObject.get("USERNAME").toString();
		//String usernameInLower = username.toLowerCase();

		log.error(className + methodName + "Exception " + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id
				+ " file Name:" + fileName, ex);

	}
}
