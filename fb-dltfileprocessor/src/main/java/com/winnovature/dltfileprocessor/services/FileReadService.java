package com.winnovature.dltfileprocessor.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.dltfileprocessor.daos.GenericDAO;
import com.winnovature.dltfileprocessor.fileparser.CsvReaderFileParser;
import com.winnovature.dltfileprocessor.fileparser.XlsFileParser;
import com.winnovature.dltfileprocessor.fileparser.XlsxFileParser;
import com.winnovature.dltfileprocessor.singletons.DltFileProcessorPropertiesTon;
import com.winnovature.dltfileprocessor.utils.Constants;
import com.winnovature.dltfileprocessor.utils.Utility;

public class FileReadService implements Callable<Map<String, Object>> {
	static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	private String fileToBeRead = null;
	Map<String, Object> data = null;
	private String telco = null;

	public FileReadService(Map<String, Object> data, String path, String telco) {
		this.data = data;
		this.fileToBeRead = path + data.get("r_filename");
		this.telco = telco;
	}

	@Override
	public Map<String, Object> call() throws Exception {
		long count = 0;
		List<String> fileHeaders = null;
		try {
			PropertiesConfiguration dltProperties = DltFileProcessorPropertiesTon.getInstance().getPropertiesConfiguration();
			String extension = FilenameUtils.getExtension(fileToBeRead);
			int startIndex = 1;
			String airtelIbmTelcoName = dltProperties.getString("airtel.ibm.telco");
			if(telco.toLowerCase().equalsIgnoreCase(airtelIbmTelcoName)) {
				startIndex = dltProperties.getInt("airtel.ibm.file.starting.row");
			}
			
			String username = null;
			if(data.get("username")!=null) {
				username = data.get("username").toString();
			}
			
			switch (extension) {
			case "csv":
				CsvReaderFileParser txtParser = new CsvReaderFileParser(new File(fileToBeRead), true, startIndex, telco, username);
				count = txtParser.parse();
				fileHeaders = txtParser.getHeadersList();
				break;
			case "xls":
				XlsFileParser xlsParser = new XlsFileParser(new File(fileToBeRead), true, startIndex, telco, username);
				count = xlsParser.parse();
				fileHeaders = xlsParser.getHeadersList();
				break;
			case "xlsx":
				XlsxFileParser xlsxParser = new XlsxFileParser(new File(fileToBeRead), true, startIndex, telco, username);
				count = xlsxParser.parse();
				fileHeaders = xlsxParser.getHeadersList();
				break;
			default:
				throw new Exception(Constants.UNSUPPORTED_FILE_TYPE+"~"+data.get("filename"));
			}
			
			List<String> missingColumns = new ArrayList<String>();
			if(validateUserFileHeaders(fileHeaders, missingColumns)) {
				data.put("isValidFile", "true");
			}else {
				data.put("isValidFile", "false");
			}
			// exclude header count
			if(count > 0) {
				count = count -1;
				// ibm will have 1 extra row in file
				if(telco.toLowerCase().equalsIgnoreCase(airtelIbmTelcoName) && count > 0) {
					count = count -1;
				}
			}
			data.put("missing", missingColumns);
			data.put("count", count);
			data.put("count_human", Utility.humanReadableNumberFormat(count));
			
		} catch (Exception e) {
			log.error("[FileReadService] [call] Exception", e);
			throw e;
		}
		return data;
	}
	
	
	private boolean validateUserFileHeaders(List<String> fileHeaders, List<String> missingColumns) throws Exception {
		Map<String, String> columnMapping = new GenericDAO().getDltTemplate(telco);
		if(log.isDebugEnabled()) {
			log.debug("[FileReadService] [validateUserFileHeaders] required columns list for telco :::"+telco+" is "+columnMapping);
		}
		boolean isHeaderExist = fileHeaders.contains(columnMapping.get("header").trim().toLowerCase());
		boolean isTempIdExist = fileHeaders.contains(columnMapping.get("template_id").trim().toLowerCase());
		boolean isTempContentExist = fileHeaders.contains(columnMapping.get("template_content").trim().toLowerCase());
		//boolean isAprvlStatusExist = fileHeaders.contains(columnMapping.get("approval_status").trim().toLowerCase());
		
		if(!isHeaderExist) {
			missingColumns.add(columnMapping.remove("header").trim());
		}
		if(!isTempIdExist) {
			missingColumns.add(columnMapping.remove("template_id").trim());
		}
		if(!isTempContentExist) {
			missingColumns.add(columnMapping.remove("template_content").trim());
		}
		
		if(!telco.equalsIgnoreCase("custom")) {
			boolean isAprvlStatusExist = fileHeaders.contains(columnMapping.get("approval_status").trim().toLowerCase());
			if(!isAprvlStatusExist) {
				missingColumns.add(columnMapping.remove("approval_status").trim());
			}
		}
		
		boolean isValidFile = missingColumns.size() > 0 ? false : true;
		
		// datetime_format holds the format of dates, its not any column name
		columnMapping.remove("datetime_format");
		
		String delim = DltFileProcessorPropertiesTon.getInstance().getPropertiesConfiguration().getString("column.name.delimiter");
		
		for (String key : columnMapping.keySet()) {
			String value = columnMapping.get(key);
			if (StringUtils.isNotBlank(value)) {
				String[] columns = value.trim().split(delim);
				for (String column : columns) {
					if(!fileHeaders.contains(column.trim().toLowerCase())) {
						missingColumns.add(column.trim());
					}
				}
			}
		}
		
		if(log.isDebugEnabled()) {
			log.debug("[FileReadService] [validateUserFileHeaders] missing columns list for telco :::"+telco+" is "+missingColumns);
		}
		
		return isValidFile;
	}

}
