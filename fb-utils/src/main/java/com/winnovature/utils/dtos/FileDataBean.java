package com.winnovature.utils.dtos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileDataBean {

	private String fileName = null;

	private int validNumbers = 0;

	private int invalidNumbers = 0;

	private int totalNumbers = 0;

	private int msgDropCount = 0;

	private List<String> invalidNumList = null;

	private int validBatchCount = 0;

	private int invalidBatchCount = 0;

	private int validStoreFrontCount = 0;

	private int duplicateBatchCount = 0;

	private int duplicateCount = 0;

	private String message = "";

	private String delivery = "";

	private String delimiter = "";

	private String newLineChar = "";

	private String contentType = "";

	private int maxMsgLength;
	
	private int chopFileCount;

	private List<SplitFileData> splitFiles = new ArrayList<SplitFileData>();
	
	private List invalidAndDupLs = new ArrayList();
	
	// added for vlink function - start
	private int visualizeLinkFlag ;
	
	private String schedule;
	
	private String msgType;
	
	// AIR-193
	private String domainURLs;
	
	// AIR-225
	private Map<String,String> vlinkWiseDomainUrls = new HashMap<String, String>();
	
	// AIR-225
	private List<String> smartLinkIdsInFile = new ArrayList<String>();
	
	// AIR-239
	private Map<String,String> cmMetaData = new HashMap<String, String>();
	
	private boolean isThrottle = false;
	
	private boolean isfmmDlt = false;
	
	private String splitFileDelimiter = "";
		
	public boolean isIsfmmDlt() {
		return isfmmDlt;
	}

	public void setIsfmmDlt(boolean isfmmDlt) {
		this.isfmmDlt = isfmmDlt;
	}

	public String getSplitFileDelimiter() {
		return splitFileDelimiter;
	}

	public void setSplitFileDelimiter(String splitFileDelimiter) {
		this.splitFileDelimiter = splitFileDelimiter;
	}

	private List<String> headers = new ArrayList<String>();

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	public boolean isThrottle() {
		return isThrottle;
	}

	public void setThrottle(boolean isThrottle) {
		this.isThrottle = isThrottle;
	}

	public Map<String, String> getCMMetaData() {
		return cmMetaData;
	}

	public void setCMMetaData(Map<String, String> metaData) {
		cmMetaData = metaData;
	}

	public List<String> getSmartLinkIdsInFile() {
		return smartLinkIdsInFile;
	}

	public void setSmartLinkIdsInFile(List<String> smartLinkIdsInFile) {
		this.smartLinkIdsInFile = smartLinkIdsInFile;
	}

	public Map<String, String> getVlinkWiseDomainUrls() {
		return vlinkWiseDomainUrls;
	}

	public void setVlinkWiseDomainUrls(Map<String, String> vlinkWiseDomainUrls) {
		this.vlinkWiseDomainUrls = vlinkWiseDomainUrls;
	}

	public String getDomainURLs() {
		return domainURLs;
	}

	public void setDomainURLs(String domainURLs) {
		this.domainURLs = domainURLs;
	}

	// HELP-508 - user uploaded file count
	private long sourceFileCount = 0;
	
	public long getSourceFileCount() {
		return sourceFileCount;
	}

	public void setSourceFileCount(long sourceFileCount) {
		this.sourceFileCount = sourceFileCount;
	}

	// Throttle changes
	private String fileStoreDirectory="";
	
	public String getFileStoreDirectory() {
		return fileStoreDirectory;
	}

	public void setFileStoreDirectory(String fileStoreDirectory) {
		this.fileStoreDirectory = fileStoreDirectory;
	}

	public int getVisualizeLinkFlag() {
		return visualizeLinkFlag;
	}

	public void setVisualizeLinkFlag(int visualizeLinkFlag) {
		this.visualizeLinkFlag = visualizeLinkFlag;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public int getPreviewLimit() {
		return previewLimit;
	}

	public void setPreviewLimit(int previewLimit) {
		this.previewLimit = previewLimit;
	}

	private int previewLimit;

	public int getMaxMsgLength() {
		return maxMsgLength;
	}

	public void setMaxMsgLength(int maxMsgLength) {
		this.maxMsgLength = maxMsgLength;
	}

	public int getMaxUnicodeLength() {
		return maxUnicodeLength;
	}

	public void setMaxUnicodeLength(int maxUnicodeLength) {
		this.maxUnicodeLength = maxUnicodeLength;
	}

	private int maxUnicodeLength;

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getNewLineChar() {
		return newLineChar;
	}

	public void setNewLineChar(String newLineChar) {
		this.newLineChar = newLineChar;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getDelivery() {
		return delivery;
	}

	public void setDelivery(String delivery) {
		this.delivery = delivery;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	// calculating estimated credits
	double creditsConsumption = 0;

	private SendSMSTypes sendSMSTypes;

	public SendSMSTypes getSendSMSTypes() {
		return sendSMSTypes;
	}

	public void setSendSMSTypes(SendSMSTypes sendSMSTypes) {
		this.sendSMSTypes = sendSMSTypes;
	}

	public double getCreditsConsumption() {
		return creditsConsumption;
	}

	public void setCreditsConsumption(double creditsConsumption) {
		this.creditsConsumption = creditsConsumption;
	}

	//private List<PreviewBean> previewLs = null;

	private int numberOfColumns = 1;

	public int getDuplicateCount() {
		return duplicateCount;
	}

	public void setDuplicateCount(int duplicateCount) {
		this.duplicateCount = duplicateCount;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getValidNumbers() {
		return validNumbers;
	}

	public void setValidNumbers(int validNumbers) {
		this.validNumbers = validNumbers;
	}

	public int getInvalidNumbers() {
		return invalidNumbers;
	}

	public void setInvalidNumbers(int invalidNumbers) {
		this.invalidNumbers = invalidNumbers;
	}

	public List<String> getInvalidNumList() {
		return invalidNumList;
	}

	public void setInvalidNumList(List<String> invalidNumList) {
		this.invalidNumList = invalidNumList;
	}

	public int getTotalNumbers() {
		return totalNumbers;
	}

	public void setTotalNumbers(int totalNumbers) {
		this.totalNumbers = totalNumbers;
	}

	public int getMsgDropCount() {
		return msgDropCount;
	}

	public void setMsgDropCount(int msgDropCount) {
		this.msgDropCount = msgDropCount;
	}

	public int getValidBatchCount() {
		return validBatchCount;
	}

	public void setValidBatchCount(int validBatchCount) {
		this.validBatchCount = validBatchCount;
	}

	public int getInvalidBatchCount() {
		return invalidBatchCount;
	}

	public void setInvalidBatchCount(int invalidBatchCount) {
		this.invalidBatchCount = invalidBatchCount;
	}

	public int getValidStoreFrontCount() {
		return validStoreFrontCount;
	}

	public void setValidStoreFrontCount(int validStoreFrontCount) {
		this.validStoreFrontCount = validStoreFrontCount;
	}

	public int getDuplicateBatchCount() {
		return duplicateBatchCount;
	}

	public void setDuplicateBatchCount(int duplicateBatchCount) {
		this.duplicateBatchCount = duplicateBatchCount;
	}

	/*
	 * public List<PreviewBean> getPreviewLs() { return previewLs; }
	 * 
	 * public void setPreviewLs(List<PreviewBean> previewLs) { this.previewLs =
	 * previewLs; }
	 */

	public int getNumberOfColumns() {
		return numberOfColumns;
	}

	public void setNumberOfColumns(int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
	}

	public List<SplitFileData> getSplitFiles() {
		return splitFiles;
	}

	public void setSplitFiles(List<SplitFileData> splitFiles) {
		this.splitFiles = splitFiles;
	}

	public int getChopFileCount() {
		return chopFileCount;
	}

	public void setChopFileCount(int chopFileCount) {
		this.chopFileCount = chopFileCount;
	}

	public List getInvalidAndDupLs() {
		return invalidAndDupLs;
	}

	public void setInvalidAndDupLs(List invalidAndDupLs) {
		this.invalidAndDupLs = invalidAndDupLs;
	}

}
