package com.winnovature.dltfileprocessor.fileparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.NumberToTextConverter;

import com.winnovature.dltfileprocessor.daos.DltTemplateMasterDAO;
import com.winnovature.dltfileprocessor.daos.DltTemplateRequestDAO;
import com.winnovature.dltfileprocessor.singletons.DltFileProcessorPropertiesTon;
import com.winnovature.dltfileprocessor.utils.Constants;
import com.winnovature.dltfileprocessor.utils.Utility;
import com.winnovature.utils.utils.UploadedFilesTrackingUtility;

import au.com.bytecode.opencsv.CSVWriter;

public class XlsFileParser implements FileParser {

	private static Log log = LogFactory.getLog(Constants.FileUploadLogger);
	private File file;
	private InputStream stream;
	private HSSFWorkbook workbook;
	private HSSFSheet sheet = null;
	private List<String> fileHeaders = new ArrayList<String>();
	private List<String> requiredColumns = new ArrayList<String>();
	private Map<Integer, String> indexedColumns = new HashMap<Integer, String>();
	private Map<String, String> requiredColumnsData = new HashMap<String, String>();
	private boolean isFileUpload;
	private int startIndex = 0;
	Map<String, String> columnMapping = null;
	Map<String, String> requestMap = null;
	private boolean isHeaderRow = false;
	String[] headerRow = null;
	private List<Integer> headerIndexes = new ArrayList<Integer>();
	List<String> clientIds = null;
	String telco = null;
	String username = null;

	public XlsFileParser(File file, boolean isFileUpload, int startIndex, String telco, String username) throws Exception {
		this.file = file;
		this.isFileUpload = isFileUpload;
		this.startIndex = startIndex;
		this.telco = telco;
		this.username = username;
	}

	@Override
	public long parse() throws Exception {
		String loggerName = "[XlsFileParser] [parse] ";
		if (log.isDebugEnabled()) {
			log.debug(loggerName + "  Begin.");
		}
		Instant startTime = Instant.now();
		FileInputStream fis = null;

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
			UUID uuid = UUID.randomUUID();
			char delim = FileParser.delimiter.charAt(0);
			String failedDataFile = file.getParent() + "/" + uuid.toString() + ".csv";
			csvWriter = new CSVWriter(new FileWriter(failedDataFile, true), delim, '"');
			filesList.add(failedDataFile);
			DltTemplateMasterDAO dltTemplateMasterDAO = new DltTemplateMasterDAO();

			fis = new FileInputStream(file);
			if (file != null) {
				workbook = new HSSFWorkbook(fis);
			} else if (stream != null) {
				workbook = new HSSFWorkbook(stream);
			} else {
				if (fis != null) {
					fis.close();
				}
				throw new Exception("No source file provided");
			}

			if (fileHeaders != null && fileHeaders.size() > 0) {
				requiredColumns = fileHeaders;
			}

			Map<Integer, String> lineDataMap = new HashMap<Integer, String>();

			sheet = workbook.getSheetAt(0);
			// Iterate through each rows from first sheet
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Iterator<Cell> cellIterator = row.cellIterator();

				lineDataMap.clear();
				requiredColumnsData.clear();

				while (cellIterator.hasNext()) {
					Cell cell = (Cell) cellIterator.next();
					String strData = "";
					switch (cell.getCellType()) {
					case STRING:
						strData = cell.getStringCellValue().trim();
						break;
					case NUMERIC:
						// strData = new HSSFDataFormatter().formatCellValue(cell);
						strData = NumberToTextConverter.toText(cell.getNumericCellValue());
						break;
					case FORMULA:
						strData = cell.getStringCellValue().trim();
						break;
					} // end of switch
					if (StringUtils.isNotBlank(strData)) {
						lineDataMap.put(cell.getColumnIndex(), strData);
					}
				} // end of while cell Iterator

				if (lineDataMap.size() > 0) {
					fileRowsCount++;
				}

				if (!isFileUpload) {
					for (Integer ind : lineDataMap.keySet()) {
						String data = lineDataMap.get(ind);
						if(data!=null) {
							data = data.trim();
						}
						if (requiredColumns.contains(data.toUpperCase()) && fileRowsCount == startIndex) {
							indexedColumns.put(ind, data.toUpperCase());
							requiredColumnsData.put(data.toUpperCase(), data);
							isHeaderRow = true;
							headerIndexes.add(ind);
						} else if (indexedColumns.containsKey(ind)) {
							requiredColumnsData.put(indexedColumns.get(ind), data);
							isHeaderRow = false;
						} else if (requiredColumns.size() > 0 && fileRowsCount == startIndex) {
							isHeaderRow = true;
						}
					}
				}

				if (isHeaderRow) {
					int index = 0;
					headerRow = new String[headerIndexes.size()];
					for (int ind : headerIndexes) {
						headerRow[index++] = indexedColumns.get(ind);
					}
				}

				if (isFileUpload) {
					if (fileRowsCount == startIndex) {
						for (int index : lineDataMap.keySet()) {
							fileHeaders.add(lineDataMap.get(index).trim().toLowerCase());
						}
					}
				} else {
					if (!isHeaderRow && fileRowsCount > startIndex && requiredColumns.size() > 0) {
						if(!telco.equalsIgnoreCase("custom")) {
							requiredColumnsData.put("entity_id", requestMap.get("entity_id"));
						}
						requiredColumnsData.put("telco", telco);
						requiredColumnsData.put("template_group_id", requestMap.get("template_group_id"));
						requiredColumnsData.put("fileloc", requestMap.get("fileloc"));
						requiredColumnsData.put("cli_id", requestMap.get("cli_id"));
						requiredColumnsData.put("dtf_id", requestMap.get("dtf_id"));
						try {
							int invalid = dltTemplateMasterDAO
									.insertIntoDltTemplateGroupHeaderEntityMapAndDltTemplateInfo(requiredColumnsData,
											columnMapping);
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
							int index = 0;
							String[] nextLine = new String[headerIndexes.size()];
							for (int ind : headerIndexes) {
								nextLine[index++] = requiredColumnsData.get(indexedColumns.get(ind));
							}
							csvWriter.writeNext(nextLine);
						}
					}
				}
			} // end of while row iterator

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
				request.put("failed_fileloc", failedDataFile);
				request.put("failed", failedRowsCount + "");
				request.put("duplicate", duplicateCount + "");
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

		} catch (Exception e) {
			log.error(loggerName + " Exception", e);
			throw e;
		} finally {
			try {
				workbook.close();
				fis.close();
				if (csvWriter != null) {
					csvWriter.close();
				}
			} catch (Exception e) {
			}
		}

		if (log.isDebugEnabled()) {
			log.debug(loggerName + " end. Time taken to read " + file.getName() + " is "
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

	@Override
	public void setRequestObject(Map<String, String> requestMap) {
		this.requestMap = requestMap;
	}

	@Override
	public void setSharedClientIds(List<String> clientIds) {
		this.clientIds = clientIds;
	}
}
