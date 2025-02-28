package com.winnovature.dltfileprocessor.services;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.dltfileprocessor.daos.DltTemplateRequestDAO;
import com.winnovature.dltfileprocessor.daos.GenericDAO;
import com.winnovature.dltfileprocessor.fileparser.FileParser;
import com.winnovature.dltfileprocessor.fileparser.FileParserFactory;
import com.winnovature.dltfileprocessor.singletons.DltFileProcessorPropertiesTon;
import com.winnovature.dltfileprocessor.utils.Constants;

public class MasterFileSplitHandler {

	private static final String className = "[MasterFileSplitHandler]";
	static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	private Map<String, String> requestMap;

	public MasterFileSplitHandler(Map<String, String> requestMap, String queueName) {
		this.requestMap = requestMap;
	}

	public void handleMasterRecords() throws Exception {

		String methodName = " [handleMasterRecords] ";
		// this is the starting point, here id is ssr id.
		methodName += " dlt_template_files id: " + requestMap.get("dtf_id") + " ";

		PropertiesConfiguration props = DltFileProcessorPropertiesTon.getInstance().getPropertiesConfiguration();

		if (log.isDebugEnabled())
			log.debug(className + methodName + "Begin.");

		try {
			processUploadedFiles();
		} catch (FileNotFoundException fnfe) {
			log.error(className + methodName + "FileNotFoundException: ", fnfe);
			try {
				Map<String, String> campaignFilesData = new HashMap<String, String>();
				campaignFilesData.put("dtf_id", requestMap.get("dtf_id"));
				campaignFilesData.put("status", props.getString("status.failed"));
				campaignFilesData.put("reason", "FileNotFoundException in MasterFileSplitHandler");
				campaignFilesData.put("instance_id", props.getString("instance.monitoring.id"));
				new DltTemplateRequestDAO().updateDltFilesStatus(campaignFilesData);
			} catch (Exception e2) {
				// if update status failed in DB , log in a separate logger
				log.error(
						className + methodName
								+ "FileNotFoundException in MasterFileSplitHandler and Db back update status [dtf_id:"
								+ requestMap.get("dtf_id") + " total:" + requestMap.get("total") + " ] Exception:",
						fnfe);
			}
			return;
		} catch (Exception e) {
			log.error(className + methodName
					+ " unable to split files OR convert to txt OR insert to campaign_file_splits. So HO back to FileSplitQ cf_id: "
					+ requestMap.get("cf_id"), e);
			Map<String, String> campaignFilesData = new HashMap<String, String>();
			campaignFilesData.put("dtf_id", requestMap.get("dtf_id"));
			campaignFilesData.put("status", props.getString("status.failed"));
			campaignFilesData.put("reason", e.getMessage());
			campaignFilesData.put("instance_id", props.getString("instance.monitoring.id"));
			new DltTemplateRequestDAO().updateDltFilesStatus(campaignFilesData);
		} // end of catch block file write
	}

	private void processUploadedFiles() throws Exception {

		String methodName = " [processUploadedFiles] ";
		methodName += " dlt_template_files id: " + requestMap.get("dtf_id") + " ";

		if (log.isDebugEnabled())
			log.debug(className + methodName + " Begin.");

		PropertiesConfiguration props = DltFileProcessorPropertiesTon.getInstance().getPropertiesConfiguration();

		String username = requestMap.get("username");
		List<String> tmpFields = new ArrayList<String>();
		String telco = requestMap.get("telco");
		Map<String, String> columnMapping = new GenericDAO().getDltTemplate(telco);
		// datetime_format holds the format of dates, its not any column name to lookup
		// for
		String dateFormat = columnMapping.remove("datetime_format");
		// approval_text holds the texts to identify if template is approved, its not
		// any column name to lookup for
		String approvalText = columnMapping.remove("approval_text");
		
		// for telco = custom entity id will be part of file data
		if(telco.equalsIgnoreCase("custom")) {
			columnMapping.put("entity_id", "ENTITY_ID");
		}

		String delim = props.getString("column.name.delimiter");

		for (String key : columnMapping.keySet()) {
			String value = columnMapping.get(key);
			if (StringUtils.isNotBlank(value)) {
				String[] columns = value.trim().split(delim);
				for (String column : columns) {
					if (!tmpFields.contains(column.trim().toUpperCase())) {
						tmpFields.add(column.trim().toUpperCase());
					}
				}
				columnMapping.put(key, value.trim().toUpperCase());
			} else {
				columnMapping.put(key, value);
			}
		}

		if(StringUtils.isNotBlank(dateFormat)) {
			columnMapping.put("datetime_format", dateFormat);
		}
		
		if(StringUtils.isNotBlank(approvalText)) {
			columnMapping.put("approval_text", approvalText);
		}
		
		int startIndex = 1;
		String airtelIbmTelcoName = props.getString("airtel.ibm.telco");
		if (telco.toLowerCase().equalsIgnoreCase(airtelIbmTelcoName)) {
			startIndex = props.getInt("airtel.ibm.file.starting.row");
		}

		// Finding clientIds who are all sharing the same template_group_id to add same
		// headers to all in user_headers table
		// List<String> clientIds = new
		// GenericDAO().getUsersWithDltTemplateGroupId(requestMap.get("template_group_id"));

		FileParser fileParser = null;
		fileParser = FileParserFactory.get(requestMap.get("fileloc"), false, startIndex, telco, username);
		fileParser.setTemplateHeaders(tmpFields);
		fileParser.setColumnMapping(columnMapping);
		fileParser.setRequestObject(requestMap);
		// fileParser.setSharedClientIds(clientIds);

		fileParser.parse();

	}

}
