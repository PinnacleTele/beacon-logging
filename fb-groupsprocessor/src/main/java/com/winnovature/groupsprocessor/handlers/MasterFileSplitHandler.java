package com.winnovature.groupsprocessor.handlers;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.fileparser.factory.FileParserFactory;
import com.winnovature.fileparser.handler.FileChopHandler;
import com.winnovature.fileparser.interfaces.FileParser;
import com.winnovature.groupsprocessor.daos.GroupsMasterDAO;
import com.winnovature.groupsprocessor.singletons.GroupsProcessorPropertiesTon;
import com.winnovature.groupsprocessor.singletons.RedisConnectionTon;
import com.winnovature.groupsprocessor.singletons.RedisQueueSender;
import com.winnovature.groupsprocessor.utils.Constants;
import com.winnovature.utils.dtos.FileDataBean;
import com.winnovature.utils.dtos.SendSMSTypes;
import com.winnovature.utils.dtos.SplitFileData;
import com.winnovature.utils.dtos.TagwiseSplitFiles;
import com.winnovature.utils.singletons.ConfigParamsTon;
import com.winnovature.utils.utils.JsonUtility;

import redis.clients.jedis.Jedis;

public class MasterFileSplitHandler {

	private static final String className = "[MasterFileSplitHandler]";
	static Log log = LogFactory.getLog(Constants.GroupsProcessorLogger);
	private Map<String, String> requestMap;
	private String fileSplitQueuename;
	private Map<String, String> configMap = null;
	PropertiesConfiguration props = null;

	public MasterFileSplitHandler(Map<String, String> requestMap, String queueName) {
		this.requestMap = requestMap;
		this.fileSplitQueuename = queueName;
		try {
			props = GroupsProcessorPropertiesTon.getInstance().getPropertiesConfiguration();
			this.configMap = (HashMap<String, String>) ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
		} catch (Exception e) {
		}
	}

	public void handleMasterRecords() throws Exception {

		String methodName = " [handleMasterRecords] ";
		// this is the starting point, here id is ssr id.
		//methodName += com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + requestMap.get("gm_id") + " ";

		// change made for template when mobile not found in file
		boolean deadLockFound = false;

		if (log.isDebugEnabled())
			log.debug(className + methodName + "Begin.");

		String queueName = props.getString(Constants.GROUP_SPLIT_FILES_QUEUE);
		
		GroupsMasterDAO groupsMasterDAO = new GroupsMasterDAO();

		String maxRetryCount = configMap.get(com.winnovature.utils.utils.Constants.MAX_RETRY_COUNT);
		int total = requestMap.get("total") == null ? 0 : Integer.parseInt(requestMap.get("total"));

		List<SplitFileData> lstSplitFiles = new ArrayList<SplitFileData>();
		try {
			try {
				TagwiseSplitFiles activity = new TagwiseSplitFiles();
				try {
					// Check if file already splitted, if yes, then hand over to queue
					lstSplitFiles = groupsMasterDAO.getSplitFiles(requestMap.get("gf_id"), maxRetryCount);

					if (log.isDebugEnabled()) {
						log.debug(className + methodName + " lstSplitFiles from DB: " + lstSplitFiles);
					}
					
					if (lstSplitFiles.size() == 0) {
						FileDataBean splitData = null;
						try {
							splitData = reWriteAndSplitUploadedFiles();
							lstSplitFiles = splitData.getSplitFiles();
							total = splitData.getTotalNumbers();
						} catch (Exception e) {
							throw e;
						}

						if (log.isDebugEnabled()) {
							log.debug(className + methodName + " total in file:" + total);
							log.debug(className + methodName + " lstSplitFiles from file" + lstSplitFiles);
						}	
						
						// insert split files into group_file_splits and update master record as
						// inprocess.
						groupsMasterDAO.insertSplitDetailsInGroupFileSplits(lstSplitFiles, requestMap);
					}
					requestMap.put("delimiter", configMap.get(com.winnovature.utils.utils.Constants.SPLIT_FILE_DELIMITER));
					activity.setChildFiles(lstSplitFiles);
					activity.setTotal_count(total);
					activity.setCampaignId(requestMap.get("gm_id"));
					activity.setCampaignName(requestMap.get("g_name"));
					activity.setRequestMap(requestMap);

					requestMap.put("total", String.valueOf(total));
				} catch (FileNotFoundException fnfe) {
					log.error(className + methodName + "FileNotFoundException: ", fnfe);
					try {
						groupsMasterDAO.updateCampaignStatus(requestMap.get("gm_id"), requestMap.get("gf_id"),
								Constants.PROCESS_STATUS_FAILED, "FileNotFoundException in GroupsProcessor", maxRetryCount);
					} catch (Exception e2) {
						// if update status failed in DB , log in a separate logger
						log.error(className + methodName
								+ "FileNotFoundException in GroupsProcessor and Db back update status [id:"
								+ requestMap.get("gm_id") + " total:" + requestMap.get("total") + " ] Exception:",
								fnfe);
					}
					return;
				} catch (Exception e) {
					log.error(className + methodName
							+ " unable to split files OR convert to txt. So HO back to GroupQ gm_id: "
							+ requestMap.get("gm_id"), e);
					// push back to GroupQ
					if (e.getMessage().toLowerCase().contains(Constants.DEADLOCK_EXCEPTION_DEFAULT.toLowerCase())) {
						deadLockFound = true;
					}
					pushBacktoGroupsQ(deadLockFound);
					return;
				} // end of catch block file write

				if (log.isDebugEnabled()) {
					log.debug(className + methodName + " HO to GroupsSplitFilesQ :" + queueName);
				}

				try {
					// Insert into redis
					if (activity.getChildFiles().size() == 0) {
						// no records - change the status to invalidfile in SSR
						String reason = "File contains 0 records";
						updateStatusFailed(reason);
					} else {
						boolean status = false;
						status = RedisQueueSender.getInstance().sendToRedis(activity, queueName);
						if (!status) {
							if (log.isDebugEnabled())
								log.debug(className + methodName + " Failed HO to DQ");
							pushBacktoGroupsQ(deadLockFound);
						}
					} // end of child list size > 0
				} catch (Exception e) {
					log.error(className + methodName + "GroupsSplitFilesQ HO failed :gm_id[" + requestMap.get("gm_id") + "]", e);
					// push back to TASKQ
					pushBacktoGroupsQ(deadLockFound);

				} // end of catch block Q HO

			} catch (Exception e) {
				log.error(className + methodName + "GroupsSplitFilesQ HO failed :gm_id[" + requestMap.get("gm_id") + "]", e);
				// push back to TASKQ
				pushBacktoGroupsQ(deadLockFound);
			} // end of catch block Q HO

		} catch (Exception e) {
			log.error(className + methodName + " Exception:", e);
			throw e;
		}
	}

	/**
	 * If file as 0 records update the status as Invalid file
	 **/
	private void updateStatusFailed(String reason) {

		String methodName = "[updateStatusFailed]";
		methodName += com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + requestMap.get("gm_id") + " ";

		GroupsMasterDAO dao = new GroupsMasterDAO();
		try {
			dao.updateCampaignFilesStatusInvalidFile(requestMap.get("gm_id"), reason, requestMap.get("total"));
		} catch (Exception e) {
			// if DB update failed , log in separate logger
			log.error(className + methodName + reason + ", Exception in DB update - status :invalidfile - failed"
					+ requestMap.get("gm_id") + " total:" + requestMap.get("total") + " ] Exception:", e);
		}
	}

	private void pushBacktoGroupsQ(boolean deadLockFound) {
		String methodName = " [pushBacktoGroupsQ] ";
		methodName += com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + requestMap.get("gm_id") + " ";

		if (log.isDebugEnabled())
			log.debug(className + methodName + " Begin: Queue name:" + fileSplitQueuename);

		Jedis con = null;
		GroupsMasterDAO dao = new GroupsMasterDAO();
		String maxRetryCount = "";

		try {
			// Incrementing retry count since failed
			String retryCount = requestMap.get("retry_count");
			maxRetryCount = configMap.get(com.winnovature.utils.utils.Constants.MAX_RETRY_COUNT);

			if (StringUtils.isBlank(retryCount)) {
				retryCount = "0";
			}

			if (log.isDebugEnabled()) {
				log.debug(className + methodName + " retryCount" + retryCount);
			}

			// if max retry crossed stop adding to GroupQ and update directly in DB
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
				dao.updateCampaignStatus(requestMap.get("gm_id"),requestMap.get("gf_id"),Constants.PROCESS_STATUS_FAILED, "GroupQ HO failed in GroupsProcessor", maxRetryCount);
			}
		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			// if push to GroupQ failed, update status as failed in groups table
			try {
				dao.updateCampaignStatus(requestMap.get("gm_id"),requestMap.get("gf_id"),Constants.PROCESS_STATUS_FAILED, "GroupQ HO failed in GroupsProcessor", maxRetryCount);
			} catch (Exception e2) {
				// if update status failed in DB , log in a separate logger
				log.error(className + methodName + "GroupQ HO failed in GroupsProcessor and Db back update status [id:"
						+ requestMap.get("gm_id") + " total:" + requestMap.get("total") + " ] Exception:", e2);
			}
		} finally {
			if (con != null)
				con.close();
		}
	}

	private FileDataBean reWriteAndSplitUploadedFiles() throws Exception {

		String methodName = " [reWriteAndSplitUploadedFiles] ";
		methodName += com.winnovature.utils.utils.Constants.FILEID_FOR_LOGGER + requestMap.get("gm_id")
				+ " ";

		if (log.isDebugEnabled())
			log.debug(className + methodName + " Begin.");

		int splitLimit = props.getInt(Constants.FILE_SPLIT_LIMIT);

		// For csv files delimiter will not be available in request.
		// Always take comma as delimiter.
		boolean isCSV = requestMap.get("fileloc").trim().toLowerCase().endsWith(".csv");
		if (isCSV) {
			requestMap.put("delimiter", ",");
		}

		String xlsDelimiter = "";
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
		// treating contacts file as index based template
		SendSMSTypes smsType = SendSMSTypes.TEMPLATE;
		fileDto.setDelimiter(xlsDelimiter);

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " splitLimit:" + splitLimit);
			log.debug(className + methodName + " xlsDelimiter:" + xlsDelimiter);
			log.debug(className + methodName + " smsType:" + smsType);
		}

		fileDto.setSendSMSTypes(smsType);
		
		String fileStoreLocation = FilenameUtils.getFullPath(requestMap.get("fileloc"));
		fileDto.setFileStoreDirectory(fileStoreLocation);

		// columns to lookup in template file for placeholders
		List<String> tmpFields = new ArrayList<String>();
		tmpFields.add(com.winnovature.utils.utils.Constants.UNSUPPORTED_ROW_HEADER);
		tmpFields.add("1");
		tmpFields.add("2");
		tmpFields.add("3");
		
		// CU-319
		requestMap.put("is_contact_processing","true");
		
		fileDto.setCMMetaData(requestMap);
		fileDto.setMessage(message);
		fileDto.setHeaders(tmpFields);

		FileParser fileParser = null;
		FileDataBean splitData = null;
		FileChopHandler splitHandler = new FileChopHandler(fileDto);

		fileParser = FileParserFactory.get(requestMap.get("fileloc"), splitHandler);

		fileParser.setDelimiter(requestMap.get("delimiter"));
		fileParser.setTemplateHeaders(tmpFields);
		fileParser.setIndexBasedTemplateFlag(true);
		// mobile must be in 1st column
		fileParser.setMobileColumnName("1");
		// true means, file will have only English chars
		fileParser.setMsgTypeCheck(false);

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
