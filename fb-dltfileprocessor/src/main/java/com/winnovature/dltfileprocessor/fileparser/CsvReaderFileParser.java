package com.winnovature.dltfileprocessor.fileparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.dltfileprocessor.daos.DltTemplateMasterDAO;
import com.winnovature.dltfileprocessor.daos.DltTemplateRequestDAO;
import com.winnovature.dltfileprocessor.singletons.DltFileProcessorPropertiesTon;
import com.winnovature.dltfileprocessor.utils.Constants;
import com.winnovature.dltfileprocessor.utils.Utility;
import com.winnovature.utils.utils.UploadedFilesTrackingUtility;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class CsvReaderFileParser implements FileParser {

	private static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	private File file;
	private String delimiter;
	private int count;
	List<String> columnDataIndex = new ArrayList<String>();
	private List<String> fileHeaders = new ArrayList<String>();
	private List<String> requiredColumns = new ArrayList<String>();
	private Map<Integer, String> indexedColumns = new HashMap<Integer, String>();
	private Map<String, String> requiredColumnsData = new HashMap<String, String>();
	private boolean isFileUpload;
	private int startingRowIndex = 0;
	private boolean isHeaderRow = false;
	Map<String, String> requestMap = null;
	Map<String, String> columnMapping = null;
	List<String> clientIds = null;
	String telco = null;
	String username = null;

	public CsvReaderFileParser(File file, boolean isFileUpload, int startingRowIndex, String telco, String username) throws Exception {
		this.file = file;
		this.count = 0;
		this.isFileUpload = isFileUpload;
		this.startingRowIndex = startingRowIndex;
		this.telco = telco;
		this.username = username;
	}

	@Override
	public long parse() throws Exception {
		String loggerName = " [CsvReaderFileParser] [parse] ";
		if (log.isDebugEnabled()) {
			log.debug(loggerName + " begin.");
		}
		Instant startTime = Instant.now();
		CSVReader csvReader = null;
		CSVWriter csvWriter = null;
		long fileRowsCount = 0;
		long failedRowsCount = 0;
		long successRowsCount = 0;
		long invalidCount = 0;
		long duplicateCount = 0;
		List<String> filesList = new ArrayList<String>();
		String requestFrom = Constants.DLT;

		try {
			PropertiesConfiguration props = DltFileProcessorPropertiesTon.getInstance().getPropertiesConfiguration();
			delimiter = FileParser.delimiter;
			UUID uuid = UUID.randomUUID();

			char delim = delimiter.charAt(0);
			String failedDataFile = file.getParent() + "/" + uuid.toString() + ".csv";
			csvWriter = new CSVWriter(new FileWriter(failedDataFile, true), delim, '"');
			filesList.add(failedDataFile);

			if (file != null) {
				csvReader = new CSVReader(new FileReader(file), delim, '"', 0);
			} else {
				throw new FileNotFoundException("No source file provided");
			}

			if (fileHeaders != null && fileHeaders.size() > 0) {
				requiredColumns = fileHeaders;
			}

			String[] headerRow = null;

			DltTemplateMasterDAO dltTemplateMasterDAO = new DltTemplateMasterDAO();
			String[] nextLine = null;

			while ((nextLine = csvReader.readNext()) != null) {
				requiredColumnsData.clear();
				if (isEmptyRow(nextLine)) {
					// empty row
					continue;
				} else if (nextLine != null && nextLine.length > 0) {
					fileRowsCount++;
					String[] splits = nextLine;
					if (isFileUpload) {
						if (fileRowsCount == startingRowIndex) {
							for (String split : splits) {
								fileHeaders.add(split.trim().toLowerCase());
							}
						}
					} else {
						int columnIndex = 1;
						for (String split : splits) {
							if (requiredColumns.contains(split.trim().toUpperCase()) && count == 0
									&& fileRowsCount == startingRowIndex) {
								indexedColumns.put(columnIndex, split.trim().toUpperCase());
								isHeaderRow = true;
								headerRow = nextLine;
							} else if (indexedColumns.containsKey(columnIndex)) {
								requiredColumnsData.put(indexedColumns.get(columnIndex), split.trim());
								isHeaderRow = false;
							} else if (requiredColumns.size() > 0 && fileRowsCount == startingRowIndex) {
								isHeaderRow = true;
							}
							columnIndex++;
						}

						if (!isHeaderRow && fileRowsCount > startingRowIndex && requiredColumns.size() > 0) {
							if(!telco.equalsIgnoreCase("custom")) {
								requiredColumnsData.put("entity_id", requestMap.get("entity_id"));
							}
							requiredColumnsData.put("telco", telco);
							requiredColumnsData.put("template_group_id", requestMap.get("template_group_id"));
							requiredColumnsData.put("fileloc", requestMap.get("fileloc"));
							requiredColumnsData.put("cli_id", requestMap.get("cli_id"));
							requiredColumnsData.put("dtf_id", requestMap.get("dtf_id"));
							
							String tempId = requiredColumnsData.get(columnMapping.get("template_id"));
							if (StringUtils.isNotBlank(tempId)) {
								tempId = tempId.trim();
								tempId = tempId.replace("'", "");
								tempId = tempId.replaceAll("'", "");
							}
							tempId = new BigDecimal(tempId.trim()).toPlainString();
							requiredColumnsData.put("template_id", tempId);
							try {
								int invalid = dltTemplateMasterDAO
										.insertIntoDltTemplateGroupHeaderEntityMapAndDltTemplateInfo(
												requiredColumnsData, columnMapping);
								if (invalid > 0) {
									invalidCount++;
								} else {
									if (requiredColumnsData.get("is_duplicate") != null) {
										duplicateCount++;
									} else {
										successRowsCount++;
									}
								}
							} catch (Exception e) {
								if (failedRowsCount == 0) {
									csvWriter.writeNext(headerRow);
								}
								failedRowsCount++;
								csvWriter.writeNext(nextLine);
							}
						}
					} // file processing
				} // non-empty line
			} // while
			
			Map<String, String> requestSummary = null;

			if (!isFileUpload && failedRowsCount > 0) {
				// total rows include header also, remove it from total
				fileRowsCount--;

				Map<String, String> request = new HashMap<String, String>();
				request.put("dtr_id", requestMap.get("dtr_id"));
				request.put("dtf_id", requestMap.get("dtf_id"));
				request.put("total", fileRowsCount + "");
				request.put("valid", successRowsCount + "");
				request.put("invalid", invalidCount + "");
				request.put("duplicate", duplicateCount + "");
				request.put("failed_fileloc", failedDataFile);
				request.put("failed", failedRowsCount + "");
				new DltTemplateRequestDAO().reprocessFailedRows(request);
				requestSummary = request;
			} else if (!isFileUpload) {
				// total rows include header also, remove it from total
				fileRowsCount--;

				Map<String, String> campaignFilesData = new HashMap<String, String>();
				campaignFilesData.put("dtf_id", requestMap.get("dtf_id"));
				campaignFilesData.put("status", props.getString("status.completed"));
				campaignFilesData.put("instance_id", props.getString("instance.monitoring.id"));
				campaignFilesData.put("total", fileRowsCount + "");
				campaignFilesData.put("valid", successRowsCount + "");
				campaignFilesData.put("invalid", invalidCount + "");
				campaignFilesData.put("failed", failedRowsCount + "");
				campaignFilesData.put("duplicate", duplicateCount + "");
				new DltTemplateRequestDAO().updateDltFilesStatus(campaignFilesData);
				requestSummary = campaignFilesData;
			}
			
			UploadedFilesTrackingUtility.setUploadedFilesInfo(requestFrom, username, filesList);

			if (log.isDebugEnabled())
				log.debug(loggerName + " RequestSummary:" + requestSummary);

		} catch (FileNotFoundException fnfe) {
			log.error(loggerName + " FileNotFoundException occured. Check " + file + " is there in disc or not.");
			throw fnfe;
		} catch (Exception e) {
			log.error(loggerName + " Exception occured", e);
			log.error("requiredColumnsData --- "+requiredColumnsData);
			throw e;
		} finally {
			if (csvReader != null) {
				csvReader.close();
			}
			if (csvWriter != null) {
				csvWriter.close();
			}
		}

		if (log.isDebugEnabled()) {
			log.debug(loggerName + " end. Time taken to read file " + file.getName() + " is "
					+ Utility.getTimeDifference(startTime) + " milliseconds.");
		}

		return fileRowsCount;
	}

	@Override
	public List<String> getHeadersList() {
		return fileHeaders;
	}

	@Override
	public void setTemplateHeaders(List<String> headers) {
		this.fileHeaders = headers;
	}

	@Override
	public void setColumnMapping(Map<String, String> columnMapping) {
		this.columnMapping = columnMapping;
	}

	public boolean isEmptyRow(String[] nextLine) {
		String data = null;
		try {
			List<String> list = Arrays.asList(nextLine);
			data = list.toString().replace("[", "").replaceAll("]", "");
			data = data.replaceAll(",", "").trim();
		} catch (Exception e) {
			log.error("[CsvReaderFileParser] isEmptyRow Exception", e);
		}
		return StringUtils.isBlank(data);
	}

	@Override
	public void setRequestObject(Map<String, String> requestMap) {
		this.requestMap = requestMap;
	}

	@Override
	public void setSharedClientIds(List<String> clientIds) {
		this.clientIds = clientIds;
	}

}
