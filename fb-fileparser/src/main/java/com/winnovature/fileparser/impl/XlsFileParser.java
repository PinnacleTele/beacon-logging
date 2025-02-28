package com.winnovature.fileparser.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;
//import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.winnovature.fileparser.interfaces.FileParser;
import com.winnovature.fileparser.interfaces.ParseFileHandler;
import com.winnovature.fileparser.util.Constants;
import com.winnovature.fileparser.util.MessageUtil;
import com.winnovature.utils.dtos.SendSMSTypes;

public class XlsFileParser implements FileParser {

	private static Log log = LogFactory.getLog(Constants.loggerName);
	private static Log loggerCheck = LogFactory.getLog(Constants.loggerNameCheck);
	private ParseFileHandler listener;
	private File file;
	private InputStream stream;
	private HSSFWorkbook workbook;
	private HSSFSheet sheet = null;
	private List<String> lineData = new ArrayList<String>();
	private int limit = -1;
	private int columnsLimit = -1;
	private int count;
	private boolean chkMsgType = true;
	private long sourceFileCount = 0;
	private List<String> headers;
	private List<String> requiredColumns = new ArrayList<String>();
	private Map<Integer, String> indexedColumns = new HashMap<Integer, String>();
	private Map<String, String> requiredColumnsData = new HashMap<String, String>();
	private boolean isHeaderRow = false;
	private String mobileColumnName;
	private Map<String, String> headersFound = new HashMap<String, String>();
	private String numericValuePattern;
	private boolean isIndexBasedTemplate = false;

	public XlsFileParser(File file, ParseFileHandler listener) throws Exception {
		this.file = file;
		this.listener = listener;
		this.count = 0;
	}

	public XlsFileParser(InputStream stream, ParseFileHandler listener) throws Exception {
		this.stream = stream;
		this.listener = listener;
	}

	@Override
	public void parse() throws Exception {
		String loggerName = "XlsFileParser [parse] ";
		if (log.isDebugEnabled()) {
			log.debug(" [XlsFileParser] [parse] Begin.");
		}
		String specialChar = "";
		String fileName = listener.start();
		listener.setFileWriter(fileName);
		// Changes made for FTM only.
		// mobile column is mandatory. If not found reject the request.
		boolean isMobileColumnFound = false;

		try {
			if (headers != null) {
				requiredColumns = headers;
			}
			FileInputStream fis = new FileInputStream(file);
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
			int mobileColumnIndex = -1;
			if (isIndexBasedTemplate) {
				isMobileColumnFound = true;
				mobileColumnIndex = Integer.parseInt(mobileColumnName);
				for (String col : requiredColumns) {
					if (!col.equalsIgnoreCase(com.winnovature.utils.utils.Constants.UNSUPPORTED_ROW_HEADER)) {
						int ind = Integer.parseInt(col);
						indexedColumns.put(ind - 1, String.valueOf(ind));
						headersFound.put(String.valueOf(ind), String.valueOf(ind));
					}
				}
				listener.setHeaders(requiredColumns);
				listener.writeHeaders();
			}

			Map<Integer, String> lineDataMap = new HashMap<Integer, String>();
			long fileRowsCount = 0;
			// int mobileColumnIndex = -1;

			sheet = workbook.getSheetAt(0);
			HSSFSheet sheet = workbook.getSheetAt(0);
			// Iterate through each rows from first sheet
			Iterator<Row> rowIterator = sheet.iterator();
			List<String> lineData = new ArrayList<String>();
			while (rowIterator.hasNext()) {
				lineData.clear();
				boolean notEmpty = false;
				Row row = rowIterator.next();

				// block to handle null rows
				for (int count1 = 0; count1 < row.getLastCellNum(); count1++) {

					Cell cell1 = row.getCell(count1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

					if (cell1 == null)
						lineData.add(null);
				} // end of check blank
					// end of block to handle null rows

				lineData.clear();
				lineDataMap.clear();

				Iterator<Cell> cellIterator = row.cellIterator();
				String strData = "";
				String mobile = "";
				fileRowsCount++;

				while (cellIterator.hasNext()) {
					Cell cell = (Cell) cellIterator.next();
					strData = "";
					int columnIndex = cell.getColumnIndex();
					switch (cell.getCellType()) {
					case STRING:
						// dont do any formating(to handle exponential numbers). Let them go to platform as it is.
						/*
						if (mobileColumnIndex >= 0 && mobileColumnIndex == columnIndex) {
							try {
								// UPL-1958
								strData = new DecimalFormat(numericValuePattern).format(cell.getNumericCellValue());
							} catch (Exception e) {
								log.error(loggerName + " Unable to parse cell value " + cell.getStringCellValue()
										+ "  Exception:" + e.getMessage());
								strData = cell.getStringCellValue().trim();
							}
						} else {
							strData = cell.getStringCellValue().trim();
						}
						*/
						strData = cell.getStringCellValue().trim();
						lineData.add(strData);
						lineDataMap.put(columnIndex, strData);
						break;
					case NUMERIC:
						// dont do any formating(to handle exponential numbers). Let them go to platform as it is.
						
						if (mobileColumnIndex >= 0 && mobileColumnIndex == columnIndex) {
							try {
								strData = new DecimalFormat(numericValuePattern).format(cell.getNumericCellValue());
							} catch (Exception e) {
								log.error(loggerName + " Unable to parse cell value " + cell.getNumericCellValue()
										+ "  Exception:" + e.getMessage());
								strData = new HSSFDataFormatter().formatCellValue(cell);
							}
						} else {
							strData = new HSSFDataFormatter().formatCellValue(cell);
						}
						
						strData = new HSSFDataFormatter().formatCellValue(cell);
						lineData.add(strData);
						lineDataMap.put(columnIndex, strData);
						break;
					case FORMULA:
						// dont do any formating(to handle exponential numbers). Let them go to platform as it is.
						
						if (mobileColumnIndex >= 0 && mobileColumnIndex == columnIndex) {
							try {
								strData = new DecimalFormat(numericValuePattern).format(cell.getNumericCellValue());
							} catch (Exception e) {
								log.error(loggerName + " Unable to parse cell value " + cell.getStringCellValue()
										+ "  Exception:" + e.getMessage());
								strData = cell.getStringCellValue().trim();
							}
						} else {
							strData = cell.getStringCellValue().trim();
						}
						
						strData = cell.getStringCellValue().trim();
						lineData.add(strData);
						lineDataMap.put(columnIndex, strData);
						break;
					} // end of switch

					if (isIndexBasedTemplate) {
						// pls note that file index(columnIndex) starts from zero where as in template
						// placeholders(requiredColumns) starts from one
						if (indexedColumns.containsKey(columnIndex)) {
							requiredColumnsData.put(indexedColumns.get(columnIndex), strData);
							isHeaderRow = false;
						}
					} else {
						if (requiredColumns.contains(strData.toUpperCase()) && sourceFileCount == 0) {
							indexedColumns.put(columnIndex, strData.toUpperCase());
							requiredColumnsData.put(strData.toUpperCase(), strData);
							isHeaderRow = true;
							if (mobileColumnName != null
									&& mobileColumnName.equalsIgnoreCase(strData.trim().toUpperCase())) {
								isMobileColumnFound = true;
								mobileColumnIndex = columnIndex;
							}
							headersFound.put(strData.toUpperCase(), strData);
						} else if (indexedColumns.containsKey(columnIndex)) {
							requiredColumnsData.put(indexedColumns.get(columnIndex), strData);
							isHeaderRow = false;
						} else if (requiredColumns.size() > 0 && fileRowsCount == 1) {
							isHeaderRow = true;
						}
					}

					// handled for FMM - read only 1st 2 columns
					if (lineData.size() == columnsLimit)
						break;

					count++;

				} // end of while cell Iterator

				// For FMM - 1st column = mobile, 2nd column = message
				// For FOM - 1st column = mobile
				SendSMSTypes smsType = listener.getFileDetails().getSendSMSTypes();
				if (sourceFileCount == 0 && (smsType == SendSMSTypes.MTM || smsType == SendSMSTypes.OTM)) {
					mobileColumnIndex = 0;
				}

				if (smsType == SendSMSTypes.MTM) {
					lineData.clear();
					if (lineDataMap.containsKey(0)) {
						lineData.add(lineDataMap.get(0));
					} else {
						lineData.add("");
					}

					if (lineDataMap.containsKey(1)) {
						lineData.add(lineDataMap.get(1));
					} else {
						lineData.add("");
					}
				} else if (smsType == SendSMSTypes.OTM) {
					lineData.clear();
					if (lineDataMap.containsKey(0)) {
						lineData.add(lineDataMap.get(0));
					}
				}

				if (sourceFileCount == 0 && (smsType == SendSMSTypes.MTM || smsType == SendSMSTypes.OTM)) {
					if (lineData.size() > 0 && MessageUtil.isHeader(lineData.get(0))) {
						// don't write header to split file. ignore the row and continue.
						continue;
					}
				}

				if (isHeaderRow) {
					if (!isMobileColumnFound) {
						throw new Exception(Constants.MOBILE_NOTFOUND_IN_FILE);
					} else {
						// remove unmatched (not available in file) headers
						if (requiredColumns.size() > requiredColumnsData.keySet().size()) {
							requiredColumns.retainAll(requiredColumnsData.keySet());
							requiredColumns.add(0, com.winnovature.utils.utils.Constants.UNSUPPORTED_ROW_HEADER);
						}
					}
					listener.setHeaders(requiredColumns);
					listener.writeHeaders();
					// always clear line for FTM
					lineData.clear();
					requiredColumnsData.clear();
				} else {
					if (requiredColumns.size() > 0) {
						lineData = new ArrayList<String>();
						for (String col : requiredColumns) {
							// do not include data for row status column as it should be decided by
							// ChopHandler by verifying content
							if (!col.equalsIgnoreCase(com.winnovature.utils.utils.Constants.UNSUPPORTED_ROW_HEADER)) {
								String data = requiredColumnsData.get(col);
								if (data != null) {
									lineData.add(requiredColumnsData.get(col));
								} else if (col.equalsIgnoreCase(mobileColumnName)) {
									lineData.add("");
								} else {
									lineData.add("");
								}
							}
						}
						requiredColumnsData.clear();
					}
				}

				/*
				 * ::: File Processing Note ::: If row is not empty then write as it is. No need
				 * to verify/validate/modify data because DE needs these records as it is.
				 */
				if (lineData != null && lineData.size() > 0) {
					mobile = lineData.get(0);
					// no need to lookup for spl chars in mobile
					/*
					if (!mobile.matches("[0-9]+"))// if mobile contains special characters
					{
						char allCharacters[] = removeDuplicates(mobile.replaceAll("[a-zA-Z0-9]", "")).toCharArray();// get
																													// characters
						int maxOcurrence = 0;

						for (char temp : allCharacters) {
							if (StringUtils.countMatches(mobile, String.valueOf(temp)) > maxOcurrence) {
								specialChar = "\\" + String.valueOf(temp);
								maxOcurrence = StringUtils.countMatches(mobile, String.valueOf(temp));
							}
						}
					}
					*/

					List<String> strMobileNumbers = new ArrayList<String>();
					if (!specialChar.isEmpty()) {
						strMobileNumbers = Arrays.asList(mobile.split(specialChar));
					} else {
						strMobileNumbers.add(mobile);
					}

					notEmpty = true;
					sourceFileCount++;

					if (notEmpty) {
						// Feature added
						for (int n = 0; n < strMobileNumbers.size(); n++) {
							String mobileString = strMobileNumbers.get(n);
							lineData.set(0, mobileString.trim());
							if (chkMsgType) { // normal
								listener.newLine(lineData.toArray(new String[lineData.size()]));
							} else {
								listener.newLineUC(lineData.toArray(new String[lineData.size()]));
							}
						}
					} // end

				} // end line data not null

			} // end of while row iterator

			if (loggerCheck.isDebugEnabled())
				loggerCheck.debug(loggerName + " filename:" + file + " Count:" + count);

			listener.end();

			if (fis != null) {
				fis.close();
			}

		} catch (FileNotFoundException fnfe) {

			log.error(loggerName + " FileNotFoundException" + " occured. Check " + file + " is there in disc or not.");
			log.error(loggerName + " Deleting file " + fileName);
			try {

				boolean fileDeletion = listener.deleteFile(fileName);
				if (fileDeletion) {
					log.error(loggerName + " " + fileName + " Deletion sucess");
				} else {
					log.error(loggerName + " Unable to delete file " + fileName);
				}
			} catch (Exception e) {
				log.error(loggerName + " Unable to delete file " + fileName);
			}
			throw fnfe;

		}

		if (log.isDebugEnabled()) {
			log.debug(" [XlsFileParser] [parse] rows count:" + count);
			log.debug(" [XlsFileParser] [parse] End.");
		}
	}

	@Override
	public void registerEventListener(ParseFileHandler listener) {
		this.listener = listener;
	}

	@Override
	public void setDelimiter(String delimiter) {

	}

	@Override
	public void setLimit(int limit) {
		this.limit = limit;
	}

	private String removeDuplicates(String s) {
		StringBuilder noDupes = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			String si = s.substring(i, i + 1);
			if (noDupes.indexOf(si) == -1) {
				noDupes.append(si);
			}
		}
		return noDupes.toString();
	}

	@Override
	public void setColumnsLimit(int columnsLimit) {
		this.columnsLimit = columnsLimit;
	}

	@Override
	public void setMsgTypeCheck(boolean chkmsgType) {
		this.chkMsgType = chkmsgType;
	}

	@Override
	public void setTemplateHeaders(List<String> headers) {
		this.headers = headers;
	}

	@Override
	public void setMobileColumnName(String name) {
		this.mobileColumnName = name;
	}

	@Override
	public void setNumericValuePattern(String numericValuePattern) {
		this.numericValuePattern = numericValuePattern;
	}

	@Override
	public void deleteFile(String filename) {
		String loggerName = " [TextFileParser] [deleteFile] ";
		try {
			boolean fileDeletion = listener.deleteFile(filename);
			if (fileDeletion) {
				log.error(loggerName + " " + filename + " Deletion success");
			} else {
				log.error(loggerName + " Unable to delete file " + filename);
			}
		} catch (Exception e1) {
			log.error(loggerName + " Unable to delete file " + filename);
		}
	}

	@Override
	public void setIndexBasedTemplateFlag(boolean isIndexBasedTemplate) {
		this.isIndexBasedTemplate = isIndexBasedTemplate;
	}

}
