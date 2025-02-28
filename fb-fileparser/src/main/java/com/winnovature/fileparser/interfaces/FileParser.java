package com.winnovature.fileparser.interfaces;

import java.util.List;

public interface FileParser {

	public void parse() throws Exception;

	public void registerEventListener(ParseFileHandler listener);

	public final String DELIMITER = "!@#~";

	public void setDelimiter(String delimiter);

	public void setLimit(int limit);

	public void setColumnsLimit(int columnsLimit);

	public void setMsgTypeCheck(boolean chkmsgType);

	public void setTemplateHeaders(List<String> headers);

	public void setMobileColumnName(String name);

	public void deleteFile(String filename);

	public void setNumericValuePattern(String numericValuePattern);

	void setIndexBasedTemplateFlag(boolean isIndexBasedTemplate);

}
