package com.winnovature.dltfileprocessor.fileparser;

import java.util.List;
import java.util.Map;

public interface FileParser {

	public static final int previewCount = 6;
	public static final String delimiter = ",";
	public long parse() throws Exception;

	List<String> getHeadersList();

	void setTemplateHeaders(List<String> headers);

	void setColumnMapping(Map<String, String> columnMapping);
	
	void setRequestObject(Map<String, String> requestMap);
	
	void setSharedClientIds(List<String> clientIds);

}
