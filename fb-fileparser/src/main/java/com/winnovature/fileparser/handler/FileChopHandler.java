package com.winnovature.fileparser.handler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.fileparser.interfaces.ParseFileHandler;
import com.winnovature.fileparser.util.Constants;
import com.winnovature.utils.dtos.FileDataBean;
import com.winnovature.utils.dtos.SendSMSTypes;
import com.winnovature.utils.dtos.SplitFileData;
import com.winnovature.utils.utils.UnicodeUtil;

public class FileChopHandler implements ParseFileHandler {

	private static Log log = LogFactory.getLog(Constants.loggerName);
	private static final String className = " [FileChopHandler] ";
	private static String methodName = "";

	private FileDataBean fileDto;
	private int totalNumbers = 0;
	private int splitFileTotal = 0;
	private List<SplitFileData> splitFileNames;
	private SplitFileData splitData = null;
	private String splitFileName = null;
	private String newLineChar = "";
	Set<String> smartLinkIdsInFile = null;
	private List<String> headersList = null;

	private FileWriter fw;
	private int chopSize;
	
	private Map<String, String> campMetaData = null;

	public FileChopHandler(FileDataBean fileDto) {
		this.fileDto = fileDto;
		this.totalNumbers = 0;
		this.chopSize = fileDto.getChopFileCount();
		this.splitFileNames = new ArrayList<SplitFileData>();
		this.campMetaData = fileDto.getCMMetaData();
		this.smartLinkIdsInFile = new HashSet<String>();
	}

	@Override
	public String start() throws Exception {
		String fileName = createNewFile(fileDto.getFileName());
		newLineChar = fileDto.getNewLineChar() == null ? "" : fileDto
				.getNewLineChar();
		return fileName;
	}
	

	@Override
	public void newLine(String[] line) throws Exception {
		methodName = " [newLine] ";
		String logName = className + methodName;

		if(log.isDebugEnabled()) {
			log.debug(logName + " Begin");
		}

		String mobile = "";
		String message = "";
		String strMessage = "";
		try {
			if (splitFileTotal >= chopSize) {
				splitData.setTotal(splitFileTotal);
				splitData.setFileName(splitFileName);
				splitData.setSplitSeq(splitFileNames.size() + 1);
				splitData.setDelimiter(fileDto.getDelimiter());
				splitFileNames.add(splitData);
			}

			if (fileDto.getSendSMSTypes().equals(SendSMSTypes.OTM)) {
				mobile = line[0];
				message = fileDto.getMessage();
			} else {
				if (line != null && line.length >= 1) {
                   int j=1;
					mobile = line[0].trim();
					message = "";

					for (int i = j; i < line.length; i++) {
						String data = line[i];
						if (i == j) {
							message = message + data.trim();
						} else {
							message = message + fileDto.getDelimiter()
									+ data.trim();
						}
					}
				}
			}

			/*  ::: File Processing Note ::: 
				If row is not empty then write as it is. 
				No need to verify/validate/modify data because DE needs these records as it is.
			 */
			
			// strMessage is message with out delimiters
			strMessage = StringUtils.replace(message, fileDto.getDelimiter(),"");

			// if both are null ignore that row
			if ((mobile == null || mobile.trim().length() == 0)
					&& (strMessage.trim().length() == 0)) {
				return;
			}
			
			if (message != null && message.trim().length() > 0)
				message = message.replaceAll(com.winnovature.utils.utils.Constants.OS_SPECIFIC_LINE_BREAK, newLineChar);

			totalNumbers++;
			splitFileTotal++;

			if (splitFileTotal > chopSize) {
				// decrement splitFileTotal by 1 to get actual count written in split file. 
				// Then reset splitFileTotal to 1 after closeFile() so that next split file count starts from 1.
				splitFileTotal--;
				closeFile();
				String fileName = createNewFile(fileDto.getFileName());
				setFileWriter(fileName);
				writeHeaders();
				splitFileTotal = 1;
			}
			if (log.isDebugEnabled()) {
				log.debug(className + methodName + "  writing data in file");
			}
			
			mobile = handleExponentialNumber(mobile);
			
			if (fileDto.getSendSMSTypes().equals(SendSMSTypes.OTM)) {
				fw.write(mobile + "\n");
			} else {
				fw.write(mobile + fileDto.getSplitFileDelimiter() + message + "\n");
			}

		} catch (Exception e) {
			log.error(className + methodName
					+ " newLine error while handling totalNumbers "
					+ totalNumbers + " line in " + fileDto.getFileName(), e);
			throw e;
		}
	}

	@Override
	public void newLineUC(String[] line) throws Exception {
		String methodName = " [newLineUC] ";
		String logName = className + methodName;
		if(log.isDebugEnabled()) {
			log.debug(logName + " Begin  Line "+line);
		}
		String mobile = "";
		String message = "";
		String strMessage = "";
		String hexMessage = "";
		String messageType = com.winnovature.utils.utils.Constants.MSG_TYPE_TEXT;
		List<String> lineData = null;

		try {
			if (splitFileTotal >= chopSize) {
				splitData.setTotal(splitFileTotal);
				splitData.setFileName(splitFileName);
				splitData.setSplitSeq(splitFileNames.size() + 1);
				splitData.setDelimiter(fileDto.getDelimiter());
				splitFileNames.add(splitData);
			}
			if (fileDto.getSendSMSTypes().equals(SendSMSTypes.OTM)) {
				mobile = line[0];
				message = fileDto.getMessage();
			} else if(fileDto.getSendSMSTypes().equals(SendSMSTypes.TEMPLATE)){
				lineData = new ArrayList<String>();
				if (line != null && line.length >= 1) {
					mobile = line[0]!=null?line[0].trim():"";
					message = "";
					for (int i = 1; i < line.length; i++) {
						String data = line[i]!=null?line[i].trim():"";
						if (i == 1) {
							message = message + data.trim();
						} else {
							message = message + fileDto.getSplitFileDelimiter() + data.trim();
						}
						data = data.replaceAll(com.winnovature.utils.utils.Constants.OS_SPECIFIC_LINE_BREAK, newLineChar);
						lineData.add(data.trim());
					}
				}
			}else {
				if (line != null && line.length >= 1) {
					mobile = line[0]!=null?line[0].trim():"";
					message = "";
					int j=1;
					for (int i = j; i < line.length; i++) {
						String data = line[j]!=null?line[i].trim():"";
						if (i == j) {
							message = message + data.trim();
						} else {
							message = message + fileDto.getDelimiter()
									+ data.trim();
						}
					}
				}
			}
			
			/*  ::: File Processing Note ::: 
				If row is not empty then write as it is. 
				No need to verify/validate/modify data because DE needs these records as it is.
			*/

			// strMessage is message with out delimeters
			strMessage = StringUtils.replace(message, fileDto.getDelimiter(),"");

			// if both are null ignore that row
			if ((mobile == null || mobile.trim().length() == 0)
					&& (strMessage.trim().length() == 0)) {
				return;
			}
			
			message = message.replaceAll(com.winnovature.utils.utils.Constants.OS_SPECIFIC_LINE_BREAK, newLineChar);
			
			String resp = "";
			// CU-319
			if(campMetaData.get("is_contact_processing") == null) {
				if(fileDto.getSendSMSTypes().equals(SendSMSTypes.TEMPLATE)) {
					resp = UnicodeUtil.INSTANCE.toHexString(null,lineData,fileDto.getSplitFileDelimiter(),campMetaData);
				}else if(fileDto.getSendSMSTypes().equals(SendSMSTypes.MTM)) {
					resp = UnicodeUtil.INSTANCE.toHexString(message, null, null, campMetaData);
				}
				
				String[] arr = resp.split("~", 2);
				if(arr[0].equalsIgnoreCase("true")) {
					hexMessage = arr[1];
					messageType = com.winnovature.utils.utils.Constants.MSG_TYPE_MULTI_LANG;
				}else {
					hexMessage = arr[1];
				}
			}else {
				hexMessage = StringUtils.join(lineData, fileDto.getSplitFileDelimiter());
			}
			
			totalNumbers++;
			splitFileTotal++;

			if (splitFileTotal > chopSize) {
				//decrement splitFileTotal by 1 to get actual count written in split file. 
				// Then reset splitFileTotal to 1 after closeFile() so that next split file count starts from 1.
				splitFileTotal--;
				closeFile();
				String fileName = createNewFile(fileDto.getFileName());
				setFileWriter(fileName);
				writeHeaders();
				splitFileTotal = 1;
			}
			if (log.isDebugEnabled()) {
				log.debug(className + methodName + "  writing data in file");
			}
			
			//mobile = handleExponentialNumber(mobile);
			
			if (fileDto.getSendSMSTypes().equals(SendSMSTypes.OTM)) {
				fw.write(mobile + "\n");
			} else if(fileDto.getSendSMSTypes().equals(SendSMSTypes.TEMPLATE)){
				if(hexMessage.trim().length() > 0) {
					fw.write(messageType + fileDto.getSplitFileDelimiter() + mobile + fileDto.getSplitFileDelimiter() + hexMessage + "\n");
				}else {
					fw.write(messageType + fileDto.getSplitFileDelimiter() + mobile + "\n");
				}
			} else {
				fw.write(messageType + fileDto.getSplitFileDelimiter() + mobile + fileDto.getSplitFileDelimiter() + hexMessage + "\n");
			}
		} catch (Exception e) {
			log.error(className + methodName
					+ " newLine error while handling totalNumbers "
					+ totalNumbers + " line in " + fileDto.getFileName(), e);
			throw e;
		}
	}

	private String createNewFile(String fileName) throws Exception {
		methodName = " [createNewFile] ";

		String logName = className + methodName;
		//String esme = campMetaData.get("cli_id");
		String ts = com.winnovature.utils.utils.Utility
				.getCustomDateAsString("yyyyMMddHHmmssSSS");
		
		if (log.isDebugEnabled()) {
			log.debug(logName + " Filename : " + fileName);
		}
		
		//String randomId = UUID.randomUUID().toString();
		splitData = new SplitFileData();
		// Throttle changes - setting file store path for throttle files
		if (fileDto.getFileStoreDirectory() != null
				&& !fileDto.getFileStoreDirectory().equalsIgnoreCase("")) {
			splitFileName = fileName.substring(0, fileName.lastIndexOf("."))
					+ "_" + ts + ".txt";
			File f = new File(splitFileName);
			String fName = f.getName();
			//splitFileName = fileDto.getFileStoreDirectory() + esme+"_"+ts+"_"+fName;
			splitFileName = fileDto.getFileStoreDirectory() + fName;

			if (log.isDebugEnabled()) {
				log.debug(logName + " Split Filename : " + splitFileName);
			}
		} else {
			// Non-throttle file. store to regular file upload path
			splitFileName = fileName.substring(0, fileName.lastIndexOf("."))
					+ "_" + ts + ".txt";
			File f = new File(splitFileName);
			String fName = f.getName();
			//splitFileName = f.getParent() + File.separator + esme+"_"+ts+"_"+fName;
			splitFileName = f.getParent() + File.separator + fName;

			if (log.isDebugEnabled()) {
				log.debug(logName + " Split Filename : "
						+ splitFileName);
			}
		}
		
		//AIR-239 - adding split file start details to campMetaData and passing it to fileStatsCollector
		//campMetaData.put(MonitoringStatusConstants.STATSTYPE, MonitoringStatusConstants.FILE_SPLIT_START);
		//campMetaData.put(MonitoringConstants.FILE_SPLIT_NAME, splitFileName);
		//campMetaData.put(MonitoringConstants.FILE_SPLIT_SEQUENCE, (splitFileNames.size() + 1)+"");
		//campMetaData.put(MonitoringConstants.FILE_SPLIT_START_TIME, com.karix.upfileprocessor.utils.utilClasses.Util.getCustomDateAsString());
		//if(fileDto.isThrottle()){
			//campMetaData.put(MonitoringConstants.IS_THROTTLE_FILE, "YES");
		//}else{
			//campMetaData.put(MonitoringConstants.IS_THROTTLE_FILE, "NO");
		//}
		//new FileProcessStatsCollector().fileStatsCollector(campMetaData);
		
		
		File file = new File(splitFileName);
		if (log.isDebugEnabled()) {
			log.debug(logName + " split Filename : " + splitFileName
					+ "  is created ");
		}
		try {
			// create new file
			this.fw = new FileWriter(file);
			closeFile();
		} catch (IOException e) {
			log.error(className + methodName + " createNewFile", e);
			throw e;
		}

		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " End.");
		}

		return splitFileName;

	}
	
	@Override
	public void writeHeaders() throws Exception {
		String methodName = " [writeHeaders] ";
		try {
			if(this.headersList!=null && this.headersList.size() > 0) {
				fw.write(StringUtils.join(this.headersList, fileDto.getSplitFileDelimiter()) + "\n");
			}
		} catch (Exception e) {
			log.error(className + methodName + " Exception", e);
			throw e;
		}
	}
		

	@Override
	public void setFileWriter(String fileName) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug(className + " [setFileWriter] begin");
			log.debug(className + " [setFileWriter]: " + fileName
					+ " is Opened");
		}
		try {
			File file = new File(fileName);
			// set the file to FileWriter
			this.fw = new FileWriter(file,true);

		} catch (Exception e) {
			log.error(className + " [setFileWriter] Exception ", e);
			throw e;
		}
	}

	@Override
	public void end() throws Exception {

		if (splitFileTotal > 0) {
			splitData.setTotal(splitFileTotal);
			splitData.setFileName(splitFileName);
			splitData.setSplitSeq(splitFileNames.size() + 1);
			splitData.setDelimiter(fileDto.getDelimiter());
			splitFileNames.add(splitData);

		}
		closeFile();
	}

	private void closeFile() throws Exception {
		methodName = " [closeFile] ";
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + splitFileName
					+ "  file is closed.");
		}
		if (fw != null) {

			try {
				fw.close();
				//AIR-239 - adding split file end details to campMetaData and passing it to fileStatsCollector
				//campMetaData.put(MonitoringStatusConstants.STATSTYPE, MonitoringStatusConstants.FILE_SPLIT_END);
				//campMetaData.put(MonitoringConstants.FILE_SPLIT_COUNT, splitFileTotal+"");
				//campMetaData.put(MonitoringConstants.FILE_SPLIT_END_TIME, com.karix.upfileprocessor.utils.utilClasses.Util.getCustomDateAsString());
				//new FileProcessStatsCollector().fileStatsCollector(campMetaData);
			} catch (IOException e) {
				log.error(className + methodName + " closeFile ", e);
				throw e;
			}
		}
	}

	public FileDataBean getFileDetails() {
		fileDto.setTotalNumbers(totalNumbers);
		fileDto.setSplitFiles(splitFileNames);
		return this.fileDto;
	}

	@Override
	public boolean deleteFile(String fileName) throws Exception {
		methodName = " [deleteFile] ";
		if (log.isDebugEnabled()) {
			log.debug(className + methodName + " begin");
			log.debug(className + methodName + fileName);
		}
		boolean fileDeletion;
		try {
			closeFile();
			File f = new File(fileName);
			fileDeletion = f.delete();

		} catch (Exception e) {
			log.error(className + methodName + " Unable to delete file "
					+ fileName);
			log.error(className + methodName + " Exception ", e);
			throw e;
		}
		return fileDeletion;
	}
	
	public List<String> getSmartLinkIdsInFile() {
		return new ArrayList<String>(this.smartLinkIdsInFile);
	}

	@Override
	public void setHeaders(List<String> headers) throws Exception {
		this.headersList = headers;
	}
	
	public String handleExponentialNumber(String mobile) {
		try {
			if (mobile.toUpperCase().contains("E+") || mobile.toUpperCase().contains("E-")) {
				// could be exponential value. check if it is parsable double value.
				Double.parseDouble(mobile);
				mobile = new BigDecimal(mobile.trim()).toPlainString();
			}
		} catch (Exception e) {
		}
		return mobile;
	}
}
