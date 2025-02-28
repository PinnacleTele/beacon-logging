package com.winnovature.utils.dtos;


public class SplitFileData {

	private String fileName;
	private long total;
	private String delimiter;
	private int splitSeq;
	private String id;
	private String scheduleTime;
	private String scheduleTimeAsLong;
	
	public String getScheduleTimeAsLong() {
		return scheduleTimeAsLong;
	}

	public void setScheduleTimeAsLong(String scheduleTimeAsLong) {
		this.scheduleTimeAsLong = scheduleTimeAsLong;
	}

	public String getScheduleTime() {
		return scheduleTime;
	}

	public void setScheduleTime(String scheduleTime) {
		this.scheduleTime = scheduleTime;
	}

	//AIR-225
	private String domainUrls;
	private String broadCastDetails;

	public String getDomainUrls() {
		return domainUrls;
	}

	public void setDomainUrls(String domainUrls) {
		this.domainUrls = domainUrls;
	}

	public String getBroadCastDetails() {
		return broadCastDetails;
	}

	public void setBroadCastDetails(String broadCastDetails) {
		this.broadCastDetails = broadCastDetails;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public int getSplitSeq() {
		return splitSeq;
	}

	public void setSplitSeq(int splitSeq) {
		this.splitSeq = splitSeq;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "SplitFileData [fileName=" + fileName + ", total=" + total + ", delimiter=" + delimiter + ", splitSeq="
				+ splitSeq + ", id=" + id + ", domainUrls=" + domainUrls + ", broadCastDetails=" + broadCastDetails
				+ "]";
	}
	
	
}
