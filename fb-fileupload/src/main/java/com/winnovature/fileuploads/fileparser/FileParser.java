package com.winnovature.fileuploads.fileparser;

import java.util.List;

public interface FileParser {

	public static final int previewCount = 6;
	public static final String delimiter = ",";
	public long parse() throws Exception;

	List<List<String>> getIndexBasedPreviewList();

	List<List<String>> getColumnBasedPreviewList();

}
