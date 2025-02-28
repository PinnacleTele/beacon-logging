package com.winnovature.utils.dtos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TagwiseSplitFiles {

	private int total_count = 0;
	private List<SplitFileData> childFiles = new ArrayList<SplitFileData>();
	private Map<String, String> requestMap = new LinkedHashMap<String, String>();
	private String campaignId = "";
	private String campaignName = "";
	private String delimiter = "";

	public String getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}

	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	/**
	 * @return the childFiles
	 */
	public List<SplitFileData> getChildFiles() {
		return childFiles;
	}

	/**
	 * @param childFiles
	 *            the childFiles to set
	 */
	public void setChildFiles(List<SplitFileData> childFiles) {
		this.childFiles = childFiles;
	}

	/**
	 * @return the total_count
	 */
	public int getTotal_count() {
		return total_count;
	}

	/**
	 * @param total_count
	 *            the total_count to set
	 */
	public void setTotal_count(int total_count) {
		this.total_count = total_count;
	}

	public Map<String, String> getRequestMap() {
		return requestMap;
	}

	public void setRequestMap(Map<String, String> requestMap) {
		this.requestMap = requestMap;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	@Override
	public String toString() {
		return "campaignId:" + campaignId + "\t" + "campaignName:" + campaignName;
	}
}
