package com.winnovature.handoverstage.processors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.http.interfaceutil.uiftp.InterfaceConstant;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.winnovature.handoverstage.daos.GenericDAO;
import com.winnovature.handoverstage.handlers.KafkaHandover;
import com.winnovature.handoverstage.handlers.SuccessfullHandover;
import com.winnovature.handoverstage.utils.Constants;
import com.winnovature.handoverstage.utils.Utility;
import com.winnovature.utils.dtos.UnprocessRow;
import com.winnovature.utils.utils.JsonUtility;
import com.winnovature.utils.utils.UserDetails;

public class ProcessOTM {

	static Log log = LogFactory.getLog(Constants.HandoverStageLogger);
	static Log trackingLog = LogFactory.getLog(Constants.TrackingLogger);

	private static final String className = "[ProcessOTM]";

	public void processData(Map<String, String> mapObject) throws Exception {

		String methodName = " [processData] ";

		if (log.isDebugEnabled())
			log.debug(className + methodName + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER
					+ mapObject.get("c_f_s_id"));

		String id = mapObject.get("c_f_s_id");
		String fileName = mapObject.get("fileloc");
		String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
		String campId = mapObject.get("cm_id");
		String removeDuplicate = mapObject.get("remove_dupe_yn");
		String fileId = mapObject.get("fileid");

		UserInfo userDetailsByClientId = null;

		Instant startTime = Instant.now();
		int handoverCount = 0;
		EnumMap<InterfaceConstant, String> aRequest = null;
		Map<String, String> userMap = null;

		try {
			new GenericDAO().updateFileStartTime(id);
		} catch (Exception e) {
			log.error(className + methodName + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id
					+ " Error while updating started_ts in campaign_file_splits ", e);
		}

		BufferedReader reader = null;
		int intDupChk = 0;
		try {

			String clientId = mapObject.get("cli_id");
			intDupChk = Integer.parseInt(removeDuplicate);

			userDetailsByClientId = UserDetails.getUserInfo(clientId);
			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " AccountDetails - " + userDetailsByClientId.getAccountDetails());
			}

			if (userDetailsByClientId != null && userDetailsByClientId.getAccountDetails() != null) {
				userMap = new JsonUtility()
						.convertJsonStringToMap(userDetailsByClientId.getAccountDetails());
				if (userMap != null && userMap.get("platform_cluster") != null
						&& "otp".equalsIgnoreCase(userMap.get("platform_cluster"))) {
					throw new ItextosException();
				}
			}

			File file = new File(fileName);
			int sucessCount = 0;
			int failureCount = 0;

			int total = 0;
			List<UnprocessRow> inValidList = new ArrayList<UnprocessRow>();
			// UnprocessRow unProcessedRow;

			Utility util = new Utility();

			String msg = mapObject.get("msg");
			String msgType = mapObject.get("c_lang_type");

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " File reading start time=" + new Date().toString()
						+ com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id + " file Name:" + fileName);
			}

			if (fileType.equalsIgnoreCase("txt")) {

				reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

				String line;

				while ((line = reader.readLine()) != null) {

					if (line != null)
						line = line.trim();

					if (line.trim().length() == 0)
						continue;

					total++;

					String mobileNo = line.trim();

					String mobileStatus = null;
					mobileStatus = util.validateMobile(mobileNo, total);
					mobileNo = util.removeHypenSpacePlusChars(mobileNo);

					// if (mobileStatus.equals(Constants.INVALID_NUMBER)) {
					// prepare list - HO Invalid numbers to Q
					// unProcessedRow = new UnprocessRow();
					// unProcessedRow.setEsmeaddr(clientId);
					// unProcessedRow.setReason(Constants.NUMBER_FORMAT_EXCEPTION_FAILURE_REASON);
					// unProcessedRow.setFileId(fileId);
					// unProcessedRow.setMobile(mobileNo);
					// inValidList.add(unProcessedRow);
					// failureCount++;
					// } else
					if (mobileStatus.equals(Constants.IS_HEADER)) {
						total = 0;
					} else {
						sucessCount++;
						Map<String, String> data = new HashMap<String, String>();
						data.put("clientId", clientId);
						data.put("mobile", mobileNo);
						data.put("message", msg);
						if (msgType.equalsIgnoreCase(Constants.UNICODE)) {
							data.put("is_hex_msg", "true");
							data.put("message_class", "UC");
						} else {
							data.put("is_hex_msg", "false");
							data.put("message_class", "PM");
						}
						// message_tag is not coming from ui yet.
						// data.put("message_tag", mapObject.get("c_name"));
						long datetime = util.extractDateAndTime(mapObject.get("created_ts"));
						data.put("received_date", datetime + "");
						data.put("received_time", datetime + "");
						data.put("header", mapObject.get("header"));
						data.put("intl_header", mapObject.get("intl_header"));
						data.put("file_id", fileId);
						data.put("user_ip", mapObject.get("ipaddr"));
						data.put("dlt_entity_id", mapObject.get("dlt_entity_id"));
						data.put("dlt_template_id", mapObject.get("dlt_template_id"));
						data.put("c_f_s_id", id);
						data.put("APP_INSTANCE_ID", mapObject.get("APP_INSTANCE_ID"));
						if (intDupChk == 1) {
							data.put("dupe_yn", "1");
						} else {
							data.put("dupe_yn", "0");
						}
						data.put("camp_id", campId);
						data.put("camp_name", mapObject.get("c_name"));
						data.put("shorten_url", mapObject.get("shorten_url"));
						data.put("tag1", null);
						data.put("tag2", null);
						data.put("tag3", null);
						data.put("tag4", null);
						data.put("tag5", null);
						data.put("PRIORITY", mapObject.get("PRIORITY"));

						aRequest = KafkaHandover.prepareDataAndSendToKafka(data, userDetailsByClientId, sucessCount);
						handoverCount++;
					} // end of processing success

				} // end of file type txt

				String time = Utility.getTimeDifference(startTime) + " milli seconds.";

				if (log.isDebugEnabled()) {
					log.debug(className + methodName + " File reading end time= " + new Date().toString()
							+ com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + id + " file Name:" + fileName
							+ " file count: " + total + " no.of destinations HO:" + sucessCount
							+ " :: Time taken for MW HO is " + time);
				}
				
				if (trackingLog.isDebugEnabled()) {
					trackingLog.debug(className + methodName + " campaign name : " + mapObject.get("c_name")
							+ " spilt file id " + id + "  filename:" + fileName + " file count: " + total
							+ " no.of destinations HO:" + sucessCount + " :: Time taken to handover file data to MW is "
							+ time + " \n last request sent is \n aRequest=" + aRequest + " \n aAccJson=" + userMap);
				}

				/**
				 * Handle successful completion updates
				 */
				new SuccessfullHandover(mapObject).handleSuccess(total, sucessCount, failureCount, inValidList, null);

			} // end of campaign status not eq paused and normal flow

		} // end of try block
		catch (ItextosException e) {
			trackingLog.error(methodName + " ItextosException occured, split file campaign_file_splits.id ["
					+ mapObject.get("c_f_s_id") + "] will not be processed ", e);
			new SuccessfullHandover(mapObject).dropRequest();
		} catch (Exception e) {
			log.error(className + methodName + "userInfo : " + userDetailsByClientId);
			log.error(className + methodName + "Exception " + com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER
					+ id + " file Name:" + fileName, e);
			// new DeliveryEngineException(mapObject).handleException(e);
			if (handoverCount == 0) {
				throw e;
			}
		} finally {
			if (reader != null)
				reader.close();
		}
	}
}
