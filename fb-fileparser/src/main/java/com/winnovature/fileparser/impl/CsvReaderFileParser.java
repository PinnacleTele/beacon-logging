package com.winnovature.fileparser.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.fileparser.interfaces.FileParser;
import com.winnovature.fileparser.interfaces.ParseFileHandler;
import com.winnovature.fileparser.util.Constants;
import com.winnovature.fileparser.util.MessageUtil;
import com.winnovature.utils.dtos.SendSMSTypes;

import au.com.bytecode.opencsv.CSVReader;

public class CsvReaderFileParser implements FileParser {

	private static Log log = LogFactory.getLog(Constants.loggerName);
	private static Log loggerCheck = LogFactory.getLog(Constants.loggerNameCheck);
	private ParseFileHandler listener;
	private File file;
	private String delimiter;
	private int limit = -1;
	private int count;
	private boolean chkMsgType = true;
	private List<String> headers;
	private List<String> requiredColumns = new ArrayList<String>();
	private Map<Integer,String> indexedColumns = new HashMap<Integer,String>();
	private Map<String,String> requiredColumnsData = new HashMap<String,String>();
	private boolean isHeaderRow = false;
	private String mobileColumnName;
	private Map<String,String> headersFound = new HashMap<String,String>();
	private String numericValuePattern;
	private boolean isIndexBasedTemplate = false;

	public CsvReaderFileParser(File file, ParseFileHandler listener)
			throws Exception {
		this.file = file;
		this.listener = listener;
		this.count = 0;
	}

	@Override
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	@Override
	public void parse() throws Exception {
		String loggerName = " [CsvReaderFileParser] [parse] ";
		if (log.isDebugEnabled()) {
			log.debug(loggerName + " begin.");
		}
		CSVReader csvReader = null;
		String splitFileName = "";
		// Changes made for FTM only.
		// mobile column is mandatory. If not found reject the request.
		boolean isMobileColumnFound = false;
		int mobileColumnIndex = -1;

		try {
			delimiter = (delimiter == null ? FileParser.DELIMITER : delimiter);
			
			char delim = delimiter.charAt(0);
			
			if (file != null) {
				csvReader = new CSVReader(new FileReader(file), delim, '"', 0);
			} else {
				throw new FileNotFoundException("No source file provided");
			}
			
			splitFileName = listener.start();
			listener.setFileWriter(splitFileName);

			if (headers != null && headers.size() > 0) {
				requiredColumns = headers;
				if(isIndexBasedTemplate) {
					isMobileColumnFound = true;
					mobileColumnIndex = Integer.parseInt(mobileColumnName);
					for(String col : requiredColumns) {
						if(!col.equalsIgnoreCase(com.winnovature.utils.utils.Constants.UNSUPPORTED_ROW_HEADER)) {
							int ind = Integer.parseInt(col);
							indexedColumns.put(ind-1, String.valueOf(ind));
							headersFound.put(String.valueOf(ind), String.valueOf(ind));
						}
					}
					listener.setHeaders(requiredColumns);
					listener.writeHeaders();
				}
			}
			
			long fileRowsCount = 0;

			String[] nextLine = null;
			List<String> lineData = new ArrayList<String>();
			Map<Integer, String> lineDataMap = new HashMap<Integer, String>();
			while ((nextLine = csvReader.readNext()) != null) {
				boolean notEmpty = false;

				if (limit != -1 && count > limit) {
					break;
				}
				
				if(nextLine != null && nextLine.length == 1 && nextLine[0].trim().length() == 0) {
					// empty row
					continue;
				}else if (nextLine != null && nextLine.length > 0) {
					fileRowsCount++;
					String[] splits = nextLine;
					int columnIndex = 1;
					for (String split : splits) {
						if(isIndexBasedTemplate) {
							// pls note that file index(columnIndex) starts from zero where as in template placeholders(requiredColumns) starts from one
							if(indexedColumns.containsKey(columnIndex-1)) {
								requiredColumnsData.put(indexedColumns.get(columnIndex-1), split.trim());
								isHeaderRow = false;
							}
						}else {
							if (requiredColumns.contains(split.trim().toUpperCase()) && count == 0 && fileRowsCount == 1) {
								indexedColumns.put(columnIndex, split.trim().toUpperCase());
								requiredColumnsData.put(split.trim().toUpperCase(), split);
								isHeaderRow = true;
								if(mobileColumnName!=null && mobileColumnName.equalsIgnoreCase(split.trim().toUpperCase())) {
									isMobileColumnFound = true;
								}
								headersFound.put(split.trim().toUpperCase(), split);
							} else if (indexedColumns.containsKey(columnIndex)) {
								requiredColumnsData.put(indexedColumns.get(columnIndex), split);
								isHeaderRow = false;
							}else if(requiredColumns.size() > 0 && fileRowsCount == 1) {
								isHeaderRow = true;
							}
						}
						lineData.add(split);
						lineDataMap.put(columnIndex-1, split);
						columnIndex++;
					}
				}else {
					// empty row
					continue;
				}
				
				if(isHeaderRow) {
					if(!isMobileColumnFound) {
						throw new Exception(Constants.MOBILE_NOTFOUND_IN_FILE);
					}else {
						// remove unmatched (not available in file) headers
						if(requiredColumns.size() > requiredColumnsData.keySet().size()) {
							requiredColumns.retainAll(requiredColumnsData.keySet());
							requiredColumns.add(0,com.winnovature.utils.utils.Constants.UNSUPPORTED_ROW_HEADER);
						}
					}
					listener.setHeaders(requiredColumns);
					listener.writeHeaders();
					// always clear line
					nextLine = new String[0];
					requiredColumnsData.clear();
				}else {
					if (requiredColumns.size() > 0) {
						lineData = new ArrayList<String>();
						for (String col : requiredColumns) {
							// do not include data for row status column as it should be decided by
							// ChopHandler by verifying content
							if (!col.equalsIgnoreCase(
									com.winnovature.utils.utils.Constants.UNSUPPORTED_ROW_HEADER)) {
								String data = requiredColumnsData.get(col);
								if (data != null) {
									lineData.add(requiredColumnsData.get(col));
								}else if(col.equalsIgnoreCase(mobileColumnName)) {
									lineData.add("");
								}else {
									// Write empty column
									lineData.add("");
								}
							}
						}
						
						requiredColumnsData.clear();
					}
					//count++;
				}
				
				// For MTM - 1st column = mobile, 2nd column = message
				// For OTM - 1st column = mobile
				SendSMSTypes smsType = listener.getFileDetails().getSendSMSTypes();
				if (fileRowsCount == 0 && (smsType == SendSMSTypes.MTM || smsType == SendSMSTypes.OTM)) {
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
				
				if (fileRowsCount == 0 && (smsType == SendSMSTypes.MTM || smsType == SendSMSTypes.OTM)) {
					if (lineData.size() > 0 && MessageUtil.isHeader(lineData.get(0))) {
						// don't write header to split file. ignore the row and continue.
						continue;
					}
				}
				
				
				/*
				 * ::: File Processing Note ::: If row is not empty then write as it is. No need
				 * to verify/validate/modify data because HandoverStage needs these records as it is.
				 */
				//String strData = "";
				String mobile = "";
				String specialChar = "";
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
					count++;

					if (notEmpty) {
						for (int n = 0; n < strMobileNumbers.size(); n++) {
							String mobileString = strMobileNumbers.get(n);
							lineData.set(0, mobileString.trim());
							if (chkMsgType) { // normal
								listener.newLine(lineData.toArray(new String[lineData.size()]));
							} else {
								listener.newLineUC(lineData.toArray(new String[lineData.size()]));
							}
						}
					} // end of kalam

				} // end line data not null

			}

			if (log.isDebugEnabled()) {
				log.debug(loggerName + " lines count:" + count);
			}

			if (loggerCheck.isDebugEnabled())
				loggerCheck.debug(loggerName + " filename:" + file + " Count:" + count);

		} catch (FileNotFoundException fnfe) {
			log.error(loggerName + " FileNotFoundException occured. Check " + file + " is there in disc or not.");
			log.error(loggerName + " Deleting file " + splitFileName);
			deleteFile(splitFileName);
			throw fnfe;
		} catch (Exception e) {
			log.error(loggerName + " Exception occured", e);
			log.error(loggerName + " Deleting file " + splitFileName);
			deleteFile(splitFileName);
			throw e;
		} finally {
			if (csvReader != null) {
				csvReader.close();
			}
			listener.end();
		}

		if (log.isDebugEnabled()) {
			log.debug(loggerName + " end.");
		}

	}
	
	@Override
	public void deleteFile(String filename) {
		String loggerName = " [CsvReaderFileParser] [deleteFile] ";
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
	public void registerEventListener(ParseFileHandler listener) {
		this.listener = listener;
	}

	@Override
	public void setLimit(int limit) {
		this.limit = limit;
	}

	@Override
	public void setColumnsLimit(int columnsLimit) {
		// Do not restrict column since message will have delimiter content
		// this.columnsLimit = columnsLimit;

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
	public void setIndexBasedTemplateFlag(boolean isIndexBasedTemplate) {
		this.isIndexBasedTemplate = isIndexBasedTemplate;
	}
	
}
