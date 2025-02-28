package com.winnovature.dltfileprocessor.fileparser;

import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public class FileParserFactory {

	private FileParserFactory() {
	}

	public static FileParser get(String fileName,
			boolean isFileUpload, int startingRowIndex, String telco, String username) throws Exception,
			IOException, InvalidFormatException {
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
		FileParser parser = null;
		File file = new File(fileName);

		if ("csv".equalsIgnoreCase(extension))
			parser = new CsvReaderFileParser(file, isFileUpload, startingRowIndex, telco, username);
		else if ("xls".equalsIgnoreCase(extension))
			parser = new XlsFileParser(file, isFileUpload, startingRowIndex, telco, username);
		else if ("xlsx".equalsIgnoreCase(extension))
			parser = new XlsxFileParser(file, isFileUpload, startingRowIndex, telco, username);

		if (parser == null) {
			throw new RuntimeException("no parser found for the file type: "
					+ extension);
		}
		return parser;
	}


}
