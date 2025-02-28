package com.winnovature.fileparser.interfaces;

import java.util.List;

import com.winnovature.utils.dtos.FileDataBean;


public interface ParseFileHandler {
	public String start() throws Exception;

	public void end() throws Exception;

	public void newLine(String[] line) throws Exception;

	public FileDataBean getFileDetails();

	public void newLineUC(String[] line) throws Exception;

	public void setFileWriter(String fileName) throws Exception;

	public boolean deleteFile(String fileName) throws Exception;

	public void setHeaders(List<String> headers) throws Exception;

	public void writeHeaders() throws Exception;

}
