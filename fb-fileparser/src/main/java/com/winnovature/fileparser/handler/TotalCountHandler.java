package com.winnovature.fileparser.handler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.fileparser.interfaces.ParseFileHandler;
import com.winnovature.fileparser.util.Constants;
import com.winnovature.fileparser.util.MessageUtil;
import com.winnovature.utils.dtos.FileDataBean;
import com.winnovature.utils.dtos.SendSMSTypes;
import com.winnovature.utils.dtos.SplitFileData;

public class TotalCountHandler implements ParseFileHandler {
	private static Log log = LogFactory.getLog(Constants.loggerName);
	private int total = 0;
	private static final String className = "[TotalCountHandler]";

	private FileDataBean fileDto;
	private int totalNumbers = 0;
	private int splitFileTotal = 0;
	private List<SplitFileData> splitFileNames;
	private SplitFileData splitData = null;
	private String splitFileName = null;
	private String newLineChar = "";

	private FileWriter fw;
	private int chopSize;

	public TotalCountHandler() {
	}

	@Override
	public String start() {
		return null;
	}

	@Override
	public void newLine(String line[]) {
		if (line != null && line.length > 0) {
			total++;
		}
	}

	@Override
	public void end() {
	}

	public int getTotal() {
		return total;
	}

	@Override
	public FileDataBean getFileDetails() {
		FileDataBean fileData = new FileDataBean();
		fileData.setTotalNumbers(total);
		return fileData;
	}


	@Override
	public void newLineUC(String[] line) throws Exception {
		String methodName = " [newLineUC] ";
		String logName = className+" [newLineUC] ";

		String mobile = "";
		String message = "";
		String strMessage = "";
		String hexMessage = "";
		//UPL-2244
		String dlttemplateid ="";
		String dltentityid ="";
		try {
			if (splitFileTotal >= chopSize) {
				if(log.isDebugEnabled())
					log.debug(logName+" Splitfilename:"+splitFileName+" inside new line");

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
					mobile = line[0].trim();
					message = "";
					int j=1;
					
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

			mobile = StringUtils.deleteWhitespace(mobile);
			// strMessage is message with out delimeters
			strMessage = StringUtils.replace(message, fileDto.getDelimiter(),"");

			// if both are null ignore that row
			if ((mobile == null || mobile.trim().length() == 0)
					&& (strMessage.trim().length() == 0)) {
				return;
			}
			message = message.replaceAll(com.winnovature.utils.utils.Constants.OS_SPECIFIC_LINE_BREAK, newLineChar);
			hexMessage = MessageUtil.stringToHexString(message);

			totalNumbers++;
			splitFileTotal++;

			if (splitFileTotal > chopSize) {
				splitFileTotal = 1;
				// HELP-508 - closing old file and opening new file
				closeFile();
				String fileName = createNewFile(fileDto.getFileName());
				setFileWriter(fileName);
			}
			if(log.isDebugEnabled()){
				log.debug(className+methodName+ "  writing data in file" );
			}
			if (fileDto.getSendSMSTypes().equals(SendSMSTypes.OTM)) {
				fw.write(mobile + "\n");
			} else {
				fw.write(mobile + fileDto.getDelimiter() + hexMessage + "\n");
			}


		} catch (Exception e) {
			log.error(className + methodName+ " newLine error while handling totalNumbers "
					+ totalNumbers + " line in " + fileDto.getFileName(), e);
			throw e;
		}
	}


	@Override
	public void setFileWriter(String fileName) throws IOException {
		if(log.isDebugEnabled()){
			log.debug(className+ " [setFileWriter] begin");
			log.debug(className+ " [setFileWriter]: "+fileName+" is Opened");
		}
		File file = new File(fileName);

		try {

			// set the file to FileWriter
			this.fw = new FileWriter(file);

		} catch (IOException e) {
			log.error(className + " [setFileWriter] IOException ", e);
			throw e;
		}
	}


	@Override
	public boolean deleteFile(String fileName) throws Exception {
		String methodName = " [deleteFile] ";
		if(log.isDebugEnabled()){
			log.debug(className+ methodName+" begin");
			log.debug(className+ methodName+ fileName);
		}
		boolean fileDeletion ;
		try {
			closeFile();
			File f = new File(fileName);
			fileDeletion = f.delete();

		} catch (Exception e) {
			log.error(className + methodName+" Unable to delete file "+fileName);
			log.error(className + methodName+" Exception ", e);
			throw e;
		}
		return fileDeletion;
	}

	private String createNewFile(String fileName) throws Exception {

		String randomId = UUID.randomUUID().toString();
		splitData = new SplitFileData();

		// Append randomId to the end of file
		splitFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "_"
				+ randomId + ".txt";
		File file = new File(splitFileName);

		try {

			// Close current file
			closeFile();

			// create new file
			this.fw = new FileWriter(file);

		} catch (IOException e) {
			log.error(className + " createNewFile", e);
			throw e;
		}
		return splitFileName;
	}

	private void closeFile() throws Exception {
		if (fw != null) {
			try {
				fw.close();
			} catch (IOException e) {
				log.error(className + " closeFile ", e);
				throw e;
			}
		}
	}

	@Override
	public void setHeaders(List<String> headers) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeHeaders() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
