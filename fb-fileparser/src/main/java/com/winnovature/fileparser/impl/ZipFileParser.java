package com.winnovature.fileparser.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.winnovature.fileparser.factory.FileParserFactory;
import com.winnovature.fileparser.interfaces.FileParser;
import com.winnovature.fileparser.interfaces.ParseFileHandler;

public class ZipFileParser implements FileParser {

	private ParseFileHandler listener;
	private FileParser parser;
	private File file;
	private InputStream stream;
	private int limit = -1;
	private int columnsLimit = -1;
	private String delimiter;
	private boolean chkMsgType = true;
	private List<String> headers;
	private boolean isIndexBasedTemplate = false;
	
	public ZipFileParser(File file, ParseFileHandler listener)
			throws InvalidFormatException, FileNotFoundException, IOException,
			Exception {
		this.listener = listener;
		this.file = file;
	}

	public ZipFileParser(InputStream stream, ParseFileHandler listener)
			throws InvalidFormatException, IOException, Exception {
		this.listener = listener;
		this.stream = stream;
	}

	@Override
	// HELP-508 - added return type
	public void parse() throws Exception {
		ZipEntry fileEntry = null;
		ZipInputStream zipInputStream;

		if (file != null) {
			zipInputStream = new ZipInputStream(new FileInputStream(file));
		} else if (stream != null) {
			zipInputStream = new ZipInputStream(stream);
		} else {
			throw new Exception("Source File not provided");
		}

		String ZipDatafileName = null;
		String fileType = null;

		while ((fileEntry = zipInputStream.getNextEntry()) != null) {

			if (fileEntry.isDirectory()) {
				continue;
			} else {

				ZipDatafileName = fileEntry.getName();
				fileType = ZipDatafileName.substring(ZipDatafileName
						.lastIndexOf(".") + 1);
				break;
			}
		}

		parser = FileParserFactory.get(zipInputStream, fileType, listener);
		parser.setLimit(limit);
		parser.setColumnsLimit(columnsLimit);
		// TODO change done
		parser.setDelimiter(delimiter);

		parser.parse();
		// since while loop breaks whenever it finds a file
	}

	@Override
	public void registerEventListener(ParseFileHandler listener) {

	}

	// TODO change done
	@Override
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	@Override
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	@Override
	public void setColumnsLimit(int columnsLimit) {
		this.columnsLimit = columnsLimit;

	}
	
	@Override
	public void setMsgTypeCheck(boolean chkmsgType){
		this.chkMsgType = chkmsgType;
	}

	@Override
	public void setTemplateHeaders(List<String> headers) {
		this.headers = headers;		
	}

	@Override
	public void setMobileColumnName(String name) {
	}

	@Override
	public void deleteFile(String filename) {
	}
	
	@Override
	public void setNumericValuePattern(String numericValuePattern) {
	}

	@Override
	public void setIndexBasedTemplateFlag(boolean isIndexBasedTemplate) {
	}

}
