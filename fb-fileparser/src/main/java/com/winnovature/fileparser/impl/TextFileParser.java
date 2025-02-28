package com.winnovature.fileparser.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.fileparser.interfaces.FileParser;
import com.winnovature.fileparser.interfaces.ParseFileHandler;
import com.winnovature.fileparser.interfaces.UnicodeReader;
import com.winnovature.fileparser.util.Constants;
import com.winnovature.fileparser.util.MessageUtil;

public class TextFileParser implements FileParser {

	private static Log log = LogFactory.getLog(Constants.loggerName);
	private static Log loggerCheck = LogFactory.getLog(Constants.loggerNameCheck);
	private ParseFileHandler listener;
	private File file;
	private InputStream stream;
	private String delimiter;
	private int limit = -1;
	private int columnsLimit = -1;
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
	
	public TextFileParser(File file, ParseFileHandler listener)
			throws Exception {
		this.file = file;
		this.listener = listener;
		this.count = 0;
	}

	public TextFileParser(InputStream stream, ParseFileHandler listener)
			throws Exception {
		this.stream = stream;
		this.listener = listener;
	}

	@Override
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	@Override
	public void parse() throws Exception {
		String loggerName = " [TextFileParser] [parse] ";
		if(log.isDebugEnabled()){
			log.debug(loggerName +" begin.");
		}
		BufferedReader reader = null;
		UnicodeReader unocodeReader;
		
		String fileName = "";
		
		
		// Changes made for FTM only.
		// mobile column is mandatory. If not found reject the request.
		boolean isMobileColumnFound = false;
		

	try{
		if (file != null) {
			unocodeReader = new UnicodeReader(new FileInputStream(file), null);
		} else if (stream != null) {
			unocodeReader = new UnicodeReader(stream, null);
		} else {
			throw new Exception("No source file provided");
		}

		unocodeReader.getEncoding();
		unocodeReader.getDefaultEncoding();
		
		fileName = listener.start();
		listener.setFileWriter(fileName);

		reader = new BufferedReader(unocodeReader);

		String line = null;
		int mobileColumnIndex = -1;
		if(headers!=null) {
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
		
		delimiter = (delimiter == null ? FileParser.DELIMITER : delimiter);

		while ((line = reader.readLine()) != null) {

			if (limit != -1 && count > limit) {
				if(log.isDebugEnabled())
					log.debug(loggerName+" inside limit check");
				break;
			}
			
			if (line != null && line.trim().length() > 0) {
				fileRowsCount++;
				String[] splits =  StringUtils.splitByWholeSeparatorPreserveAllTokens(line.trim(),delimiter, columnsLimit);
				int columnIndex = 1;
				for (String split : splits) {
					if(isIndexBasedTemplate) {
						// pls note that file index(columnIndex) starts from zero where as in template placeholders(requiredColumns) starts from one
						if(indexedColumns.containsKey(columnIndex-1)) {
							requiredColumnsData.put(indexedColumns.get(columnIndex-1), split.trim());
							isHeaderRow = false;
						}
					}else {
						if(requiredColumns.contains(split.trim().toUpperCase()) && count == 0 && fileRowsCount == 1) {
							// index of columns found in file
							indexedColumns.put(columnIndex, split.trim().toUpperCase());
							// actual data so dont trim
							requiredColumnsData.put(split.trim().toUpperCase(), split);
							isHeaderRow = true;
							if(mobileColumnName!=null && mobileColumnName.equalsIgnoreCase(split.trim().toUpperCase())) {
								isMobileColumnFound = true;
							}
							headersFound.put(split.trim().toUpperCase(), split);
						}else if(indexedColumns.containsKey(columnIndex)) {
							// actual data so dont trim
							requiredColumnsData.put(indexedColumns.get(columnIndex), split);
							isHeaderRow = false;
						}else if(requiredColumns.size() > 0 && fileRowsCount == 1) {
							isHeaderRow = true;
						}
					}
					columnIndex++;
				}
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
				// always clear line for FTM
				line = "";
				requiredColumnsData.clear();
			}else {
				if(requiredColumns.size() > 0) {
					count++;
					writeToSplitFile();
					// always clear line for FTM
					line = "";
				}
			}
			

			if (line != null && line.trim().length() > 0) {
				
				if(count == 0) {
					String[] lineArray = StringUtils.splitByWholeSeparatorPreserveAllTokens(line.trim(),delimiter, columnsLimit);
					
					if(lineArray!=null && lineArray.length > 0) {
						if(MessageUtil.isHeader(lineArray[0])) {
							// don't write header to split file. ignore the row and continue.
							continue;
						}
						// String does not start with text mobile. Could be some invalid data, write to file.
					}	
				}
				
				count++;

				if (chkMsgType) { 
					listener.newLine(StringUtils
							.splitByWholeSeparatorPreserveAllTokens(line.trim(),
									delimiter, columnsLimit));
				} else {
					listener.newLineUC(StringUtils
							.splitByWholeSeparatorPreserveAllTokens(line.trim(),
									delimiter, columnsLimit));
				}

			}
			
		}
		
		if(log.isDebugEnabled()){
			log.debug(loggerName +" lines count:"+count);
		}
		
		if(loggerCheck.isDebugEnabled())
			loggerCheck.debug(loggerName+" filename:"+file+" Count:"+count);

		
	}catch(FileNotFoundException fnfe){
		log.error(loggerName+" FileNotFoundException"
				+ " occured. Check "+file+" is there in disc or not.");
		log.error(loggerName+" Deleting file "+fileName);
		deleteFile(fileName);
		throw fnfe;
	}catch(Exception e) {
		log.error(loggerName+" Exception ", e);
		deleteFile(fileName);
		throw e;
	}finally {
		listener.end();
		if(reader!=null)
			reader.close();
	}
	
		if(log.isDebugEnabled()){
			log.debug(loggerName +" end.");
		}
	}
	
	private void writeToSplitFile() throws Exception {
		String loggerName = " [TextFileParser] [writeToSplitFile] ";
		try {
			List<String> lineData = new ArrayList<String>();
			for(String col : requiredColumns) {
				// do not include data for row status column as it should be decided by ChopHandler by verifying content
				if(!col.equalsIgnoreCase(com.winnovature.utils.utils.Constants.UNSUPPORTED_ROW_HEADER)) {
					String data = requiredColumnsData.get(col);
					if(data!=null && data.trim().length() > 0) {
						lineData.add(requiredColumnsData.get(col));
					}else if(col.equalsIgnoreCase(mobileColumnName)) {
						lineData.add("");
					}else {
						// UPL-1855 : Write empty column
						//lineData.add("["+headersFound.get(col)+"]");
						lineData.add("");
					}
				}
			}
			listener.newLineUC(lineData.toArray(new String[lineData.size()]));
			requiredColumnsData.clear();
		}catch(Exception e) {
			log.error(loggerName+" Exception ", e);
			throw e;
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
	public void deleteFile(String filename) {
		String loggerName = " [TextFileParser] [deleteFile] ";
		boolean fileDeletion = false;
		try {
			if(filename!=null && filename.trim().length() > 0) {
				fileDeletion = listener.deleteFile(filename);
				if (fileDeletion) {
					log.error(loggerName + " " + filename + " Deletion success");
				} else {
					log.error(loggerName + " Unable to delete file " + filename);
				}
			}
		} catch (Exception e1) {
			log.error(loggerName + " Unable to delete file " + filename);
		}
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
