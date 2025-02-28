package com.winnovature.splitstage.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.inmemdata.account.UserInfo;
import com.winnovature.fileparser.factory.FileParserFactory;
import com.winnovature.fileparser.handler.FileChopHandler;
import com.winnovature.fileparser.interfaces.FileParser;
import com.winnovature.splitstage.daos.CampaignFileSplitsDAO;
import com.winnovature.splitstage.daos.GenericDAO;
import com.winnovature.splitstage.singletons.RedisConnectionTon;
import com.winnovature.splitstage.singletons.RedisQueueSender;
import com.winnovature.splitstage.singletons.SplitStagePropertiesTon;
import com.winnovature.splitstage.utils.Constants;
import com.winnovature.utils.dtos.FileDataBean;
import com.winnovature.utils.dtos.QueueType;
import com.winnovature.utils.dtos.SendSMSTypes;
import com.winnovature.utils.dtos.SplitFileData;
import com.winnovature.utils.dtos.TagwiseSplitFiles;
import com.winnovature.utils.dtos.Templates;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.singletons.DeliveryEngineQueueTon;
import com.winnovature.utils.utils.JsonUtility;
import com.winnovature.utils.utils.UnicodeUtil;
import com.winnovature.utils.utils.UserDetails;

import redis.clients.jedis.Jedis;

public class MasterFileSplitHandler {

	private static final String className = "[MasterFileSplitHandler]";
	static Log log = LogFactory.getLog(Constants.SplitStageLogger);
	private Map<String, String> requestMap;
	private String fileSplitQueuename;
	private Map<String, String> configMap = null;
	UserInfo userDetailsByClientId = null;
	private Map<String, String> userInfo = null;

	public MasterFileSplitHandler(Map<String, String> requestMap, String queueName) {
		this.requestMap = requestMap;
		this.fileSplitQueuename = queueName;
		try {
			this.configMap = (HashMap<String, String>) ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
		} catch (Exception e) {
		}
	}

	public void handleMasterRecords() throws Exception {

		String methodName = " [handleMasterRecords] ";
		// this is the starting point, here id is ssr id.
		methodName += com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + requestMap.get("cf_id") + " ";

		// change made for template when mobile not found in file
		boolean mobileColumnFoundInFile = true;

		boolean deadLockFound = false;

		if (log.isDebugEnabled())
			log.debug(className + methodName + "Begin.");

		String queueName = "";

		String maxRetryCount = configMap.get(com.winnovature.utils.utils.Constants.MAX_RETRY_COUNT);
		int total = requestMap.get("total") == null ? 0 : Integer.parseInt(requestMap.get("total"));

		List<SplitFileData> lstSplitFiles = new ArrayList<SplitFileData>();
		CampaignFileSplitsDAO deliveryDAO = new CampaignFileSplitsDAO();
		
		try {
			userDetailsByClientId = UserDetails.getUserInfo(requestMap.get("cli_id"));
			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " AccountDetails - "+ userDetailsByClientId.getAccountDetails());
			}
			userInfo = new JsonUtility().convertJsonStringToMap(userDetailsByClientId.getAccountDetails());
			// used for unicode conversion
			requestMap.put("uc_iden_char_len", userInfo.get("uc_iden_char_len"));
			requestMap.put("uc_iden_occur", userInfo.get("uc_iden_occur"));
			requestMap.put("is_remove_uc_chars", userInfo.get("is_remove_uc_chars"));
			
			if (requestMap.get("c_lang_type").equalsIgnoreCase("unicode")
					&& StringUtils.isNotBlank(requestMap.get("msg_original"))) {
				//String hex = UnicodeUtil.INSTANCE.toHex(requestMap.get("msg_original"), null, null, requestMap);
				// requestMap.put("msg", hex);
				String resp = UnicodeUtil.INSTANCE.toHexString(requestMap.get("msg_original"), null, null, requestMap);
				String[] arr = resp.split("~", 2);
				requestMap.put("msg", arr[1]);
			}
			
			TagwiseSplitFiles activity = new TagwiseSplitFiles();
			try {
				// Check if file already splitted, if yes, then hand over to queue
				lstSplitFiles = deliveryDAO.getSplitFiles(requestMap.get("cf_id"), maxRetryCount);

				if (log.isDebugEnabled()) {
					log.debug(className + methodName + " lstSplitFiles from DB: " + lstSplitFiles);
				}

				if (lstSplitFiles.size() == 0) {
					// Write the records to file. If greater than splitCount,
					// multiple child records will be added to list.
					// Default is txt
					FileDataBean splitData = null;
					try {
						splitData = reWriteAndSplitUploadedFiles();
						lstSplitFiles = splitData.getSplitFiles();
						total = splitData.getTotalNumbers();
					} catch (Exception e) {
						if (com.winnovature.fileparser.util.Constants.MOBILE_NOTFOUND_IN_FILE
								.equalsIgnoreCase(e.getMessage())) {
							mobileColumnFoundInFile = false;
						} else {
							throw e;
						}
					}

					if (log.isDebugEnabled()) {
						log.debug(className + methodName + " total in file:" + total);
						log.debug(className + methodName + " list of SplitFiles from file" + lstSplitFiles);
					}

					if (mobileColumnFoundInFile) {
						// updating splitfile delimiter
						if (requestMap.get("c_type").equalsIgnoreCase(com.winnovature.utils.utils.Constants.MTM_CAMP) 
								|| requestMap.get("c_type").equalsIgnoreCase(com.winnovature.utils.utils.Constants.TEM_CAMP)) {
							requestMap.put("delimiter",
									configMap.get(com.winnovature.utils.utils.Constants.SPLIT_FILE_DELIMITER));
						}

						// insert split files into campaign_file_splits and update master record as
						// INPROCESS.
						new CampaignFileSplitsDAO().insertSplitDetailsInCampaignFileSplits(lstSplitFiles, requestMap);
					}

				} // end of if lstSplitFiles.size() ==0

				activity.setChildFiles(lstSplitFiles);
				activity.setTotal_count(total);
				activity.setCampaignId(requestMap.get("cm_id"));
				activity.setCampaignName(requestMap.get("c_name"));
				activity.setRequestMap(requestMap);

				requestMap.put("total", String.valueOf(total));
			} catch (FileNotFoundException fnfe) {
				log.error(className + methodName + "FileNotFoundException: ", fnfe);
				try {
					new GenericDAO().updateCampaignStatus(requestMap.get("cm_id"), requestMap.get("cf_id"),
							Constants.PROCESS_STATUS_FAILED, "FileNotFoundException in SplitStage");
				} catch (Exception e2) {
					// if update status failed in DB , log in a separate logger
					log.error(className + methodName
							+ "FileNotFoundException in SplitStage and Db back update status [cf_id:"
							+ requestMap.get("cf_id") + " total:" + requestMap.get("total") + " ] Exception:",
							fnfe);
				}
				return;
			} catch (Exception e) {
				log.error(className + methodName
						+ " unable to split files OR convert to txt OR insert to campaign_file_splits. So HO back to FileSplitQ cf_id: "
						+ requestMap.get("cf_id"), e);
				// push back to TASKQ
				if (e.getMessage().toLowerCase().contains(Constants.DEADLOCK_EXCEPTION_DEFAULT.toLowerCase())) {
					deadLockFound = true;
				}
				pushBacktoFileSplitQ(deadLockFound);
				return;
			} // end of catch block file write

			// Added for identifying low count
			QueueType queueNameType = DeliveryEngineQueueTon.getInstance()
					.getQueueNameBasedOnCount(activity.getTotal_count());
			queueName = queueNameType.getQueueName();
			
			// PRIORITY (0,1 – High ; 2,3,4,5 – Low)
			// Low volume -- set high(0,1) priority
			// Medium volume -- set low(2,3) priority
			// High volume -- set low(4,5) priority
			if(queueName.startsWith("Low")) {
				requestMap.put("PRIORITY", "1");
			}else if(queueName.startsWith("Medium")) {
				requestMap.put("PRIORITY", "2");
			} else {
				requestMap.put("PRIORITY", "4");
			}
			activity.setRequestMap(requestMap);

			String excludeGrpIds = requestMap.get("exclude_group_ids") == null ? "" : requestMap.get("exclude_group_ids");

			if (excludeGrpIds.length() > 0)
				queueName = queueName + com.winnovature.utils.utils.Constants.EXCLUDE;

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " HO to DeliveryQ or ExcludeQ :" + queueName);
			}

			try {
				// Insert into redis
				if (activity.getChildFiles().size() == 0) {
					// no records - change the status to invalidfile in SSR
					String reason = "File contains 0 records";
					if (!mobileColumnFoundInFile) {
						// send failure notification
						reason = com.winnovature.fileparser.util.Constants.MOBILE_NOTFOUND_IN_FILE;
					}
					updateStatusFailed(reason, maxRetryCount);
				} else {
					boolean status = false;
					status = RedisQueueSender.getInstance().sendToRedis(activity, queueName, excludeGrpIds);
					if (!status) {
						if (log.isDebugEnabled())
							log.debug(className + methodName + " Failed HO to DQ");
						pushBacktoFileSplitQ(deadLockFound);
					}
				} // end of child list size > 0
			} catch (Exception e) {
				log.error(className + methodName + "DeliveryQ HO failed :cf_id[" + requestMap.get("cf_id") + "]", e);
				// push back to TASKQ
				pushBacktoFileSplitQ(deadLockFound);

			} // end of catch block Q HO

		} catch (Exception e) {
			log.error(className + methodName + "DeliveryQ HO failed :cf_id[" + requestMap.get("cf_id") + "]  userDetails :"+userDetailsByClientId+"  cli_id:"+requestMap.get("cli_id"), e);
			// push back to TASKQ
			pushBacktoFileSplitQ(deadLockFound);
		} // end of catch block Q HO
	}

	/**
	 * If file as 0 records update the status as Invalid file
	 **/
	private void updateStatusFailed(String reason, String maxRetryCount) {

		String methodName = "[updateStatusFailed]";
		methodName += com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + requestMap.get("cf_id") + " ";

		GenericDAO dao = new GenericDAO();
		try {
			dao.updateCampaignFilesStatusInvalidFile(requestMap.get("cf_id"), reason, requestMap.get("total"), maxRetryCount);
		} catch (Exception e) {
			// if DB update failed , log in separate logger
			log.error(className + methodName + reason + ", Exception in DB update - status :failed"
					+ requestMap.get("cf_id") + " total:" + requestMap.get("total") + " ] Exception:", e);
		}
	}

	private void pushBacktoFileSplitQ(boolean deadLockFound) {
		String methodName = " [pushBacktoFileSplitQ] ";
		methodName += com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + requestMap.get("cf_id") + " ";

		if (log.isDebugEnabled())
			log.debug(className + methodName + " Begin: Queue name:" + fileSplitQueuename);

		Jedis con = null;
		GenericDAO dao = new GenericDAO();
		try {
			// Incrementing retry count since failed
			String retryCount = requestMap.get("retry_count");
			String maxRetryCount = configMap.get(com.winnovature.utils.utils.Constants.MAX_RETRY_COUNT);

			if (StringUtils.isBlank(retryCount)) {
				retryCount = "0";
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " retryCount" + retryCount);
			}

			// if max retry crossed stop adding to TaskQ and update directly in DB
			if (Integer.parseInt(retryCount) <= Integer.parseInt(maxRetryCount)) {
				int retryTime = 0;

				if (retryCount != null) {
					retryTime = Integer.parseInt(retryCount);
				}

				if (!deadLockFound) {
					retryTime = retryTime + 1;
				}
				requestMap.put("retry_count", String.valueOf(retryTime));

				con = RedisConnectionTon.getInstance().getJedisConnectionAsRoundRobin();

				String json = new JsonUtility().convertMapToJSON(requestMap);
				con.lpush(fileSplitQueuename, json);

				if (log.isDebugEnabled())
					log.debug(className + methodName + " FileSender-request sent-" + json);
			} else {
				dao.updateCampaignStatus(requestMap.get("cm_id"),requestMap.get("cf_id"),Constants.PROCESS_STATUS_FAILED, "FileSplitQ HO failed in SplitStage");
			}
		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			// if push to FileSplitQ failed, update status as failed in CM table
			try {
				dao.updateCampaignStatus(requestMap.get("cm_id"),requestMap.get("cf_id"),Constants.PROCESS_STATUS_FAILED, "FileSplitQ HO failed in SplitStage");
			} catch (Exception e2) {
				// if update status failed in DB , log in a separate logger
				log.error(className + methodName + "FileSplitQ HO failed in SplitStage and Db back update status [cf_id:"
						+ requestMap.get("cf_id") + " total:" + requestMap.get("total") + " ] Exception:", e2);
			}
		} finally {
			if (con != null)
				con.close();
		}
	}

	private FileDataBean reWriteAndSplitUploadedFiles() throws Exception {

		String methodName = " [reWriteAndSplitUploadedFiles] ";
		methodName += com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + requestMap.get("cf_id")
				+ " ";

		if (log.isDebugEnabled())
			log.debug(className + methodName + " Begin.");

		PropertiesConfiguration props = SplitStagePropertiesTon.getInstance().getPropertiesConfiguration();

		int splitLimit = Integer.parseInt(configMap.get(Constants.SMS_SPLIT_LIMIT));

		// For csv files delimiter will not be available in request.
		// Always take comma as delimiter.
		boolean isCSV = requestMap.get("fileloc").trim().toLowerCase().endsWith(".csv");
		if (isCSV) {
			requestMap.put("delimiter", ",");
		}

		String xlsDelimiter = null;
		String splitFileDelimiter = configMap
				.get(com.winnovature.utils.utils.Constants.SPLIT_FILE_DELIMITER);

		// Added to handle message containing file delimiter
		String delimiter = requestMap.get("delimiter");

		// chars to be replaced for linebreaks(\n) in message
		String newLineChar = configMap.get(com.winnovature.utils.utils.Constants.LINE_BREAK_REPLACER);

		if (StringUtils.isNotBlank(delimiter)) {
			xlsDelimiter = delimiter;
		}

		FileDataBean fileDto = new FileDataBean();
		fileDto.setChopFileCount(splitLimit);
		fileDto.setFileName(requestMap.get("fileloc"));
		fileDto.setNewLineChar(newLineChar);
		fileDto.setSplitFileDelimiter(splitFileDelimiter);

		// message will have linebreaks replaced with newLineChar
		String message = requestMap.get("msg");

		SendSMSTypes smsType = SendSMSTypes.MTM;

		if (requestMap.get("c_type").equalsIgnoreCase(com.winnovature.utils.utils.Constants.MTM_CAMP)) {
			smsType = SendSMSTypes.MTM;
			fileDto.setDelimiter(xlsDelimiter);
		} else if (requestMap.get("c_type").equalsIgnoreCase(com.winnovature.utils.utils.Constants.QUICK_CAMP)
				|| requestMap.get("c_type").equalsIgnoreCase(com.winnovature.utils.utils.Constants.GROUP_CAMP)
				|| requestMap.get("c_type").equalsIgnoreCase(com.winnovature.utils.utils.Constants.OTM_CAMP)) {
			smsType = SendSMSTypes.OTM;
		} else if (requestMap.get("c_type").equalsIgnoreCase(com.winnovature.utils.utils.Constants.TEM_CAMP)) {
			smsType = SendSMSTypes.TEMPLATE;
			fileDto.setDelimiter(xlsDelimiter);
		} else if (requestMap.get("c_type").equals("SUB")) {
			smsType = SendSMSTypes.MTM;
			fileDto.setDelimiter(xlsDelimiter);
		}

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " splitLimit:" + splitLimit);
			log.debug(className + methodName + " xlsDelimiter:" + xlsDelimiter);
			log.debug(className + methodName + " message:" + message);
			log.debug(className + methodName + " smsType:" + smsType);
		}

		fileDto.setSendSMSTypes(smsType);
		String fileStoreLocation = configMap.get(com.winnovature.utils.utils.Constants.CAMPAIGNS_FILE_STORE_PATH);
		fileStoreLocation = fileStoreLocation + requestMap.get("username").toLowerCase() + File.separator;
		fileDto.setFileStoreDirectory(fileStoreLocation);

		// No need to replace for line breaks in template because DE will read template
		// directly
		// columns to lookup in FTM file for placeholders
		List<String> tmpFields = new ArrayList<String>();
		boolean isIndexBasedTemplate = false;
		String templateId = requestMap.get("template_id");
		String mobileColumnName = null;
		if (smsType == SendSMSTypes.TEMPLATE) {
			//Templates template = new GenericDAO().selectTemplate(templateId);
			Templates template = new Templates();
			template.setTmplid(templateId);
			template.setTemplateType(requestMap.get("template_type"));
			template.setPhoneNumberField(requestMap.get("template_mobile_column"));
			template.setMsg_text(requestMap.get("msg"));
			template.setUnicode(requestMap.get("c_lang_type").equalsIgnoreCase("unicode")?1:0);
			
			if (template != null) {
				tmpFields.add(com.winnovature.utils.utils.Constants.UNSUPPORTED_ROW_HEADER);
				tmpFields.add(template.getPhoneNumberField().trim().toUpperCase());
				mobileColumnName = template.getPhoneNumberField().trim().toUpperCase();
				isIndexBasedTemplate = template.getTemplateType().equalsIgnoreCase("INDEX");
				String fields = com.winnovature.utils.utils.Utility
						.getPlaceholdersFromTemplateMessage(template.getMsg_text(), template.getUnicode(), !isIndexBasedTemplate);

				if (fields != null && fields.trim().length() > 0) {
					List<String> list = Arrays.asList(fields.split(","));
					for (String field : list) {
						if (!tmpFields.contains(field.trim().toUpperCase())) {
							tmpFields.add(field.trim().toUpperCase());
						}
					}
					template.setTmpl_flds(fields);
				}

				if (template.getUnicode() == 1) {
					requestMap.put("c_lang_type", com.winnovature.utils.utils.Constants.MSG_TYPE_MULTI_LANG);
				} else {
					requestMap.put("c_lang_type", com.winnovature.utils.utils.Constants.MSG_TYPE_TEXT);
				}
				message = template.getMsg_text();
				Map<Object, Object> templateAsMap = new JsonUtility().beanToMap(template);
				String tmpAsJson = new JsonUtility().convertMapToJson(templateAsMap);
				requestMap.put("TEMPLATE_BEAN", tmpAsJson);
				requestMap.put("msg", message);
			}
		}

		fileDto.setCMMetaData(requestMap);
		fileDto.setMessage(message);
		fileDto.setHeaders(tmpFields);

		FileParser fileParser = null;
		FileDataBean splitData = null;
		FileChopHandler splitHandler = new FileChopHandler(fileDto);

		fileParser = FileParserFactory.get(requestMap.get("fileloc"), splitHandler);

		fileParser.setDelimiter(requestMap.get("delimiter"));
		fileParser.setTemplateHeaders(tmpFields);
		// set only for non-throttle requests, because throttle files will have headers
		// when reached here
		if (requestMap.get("DDR_MASTER_ID") == null) {
			fileParser.setIndexBasedTemplateFlag(isIndexBasedTemplate);
		}

		fileParser.setMobileColumnName(mobileColumnName);
		
		// Added to limit columns for xls and xlsx fmm
		if (smsType == SendSMSTypes.MTM) {
			fileParser.setColumnsLimit(2);
		}

		if (smsType == SendSMSTypes.MTM || smsType == SendSMSTypes.TEMPLATE) {
			fileParser.setMsgTypeCheck(false);
		} else {
			fileParser.setMsgTypeCheck(true);
		}

		String numericvaluepattern = props.getString(com.winnovature.utils.utils.Constants.PATTERN_DECIMALFORMATTER_FOR_DIGITS);
		fileParser.setNumericValuePattern(numericvaluepattern);
		// master file reading starts here
		fileParser.parse();

		splitData = splitHandler.getFileDetails();

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " splitData Completed..");
		}

		return splitData;
	}

}
