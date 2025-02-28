package com.winnovature.fileparser.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.winnovature.fileparser.interfaces.FileDepositer;

public class UploadStreamDepositer implements FileDepositer {

	private InputStream stream;
	private String outputFilePath;
	private String outputFileName;
	private String randomNumber;
	private String type;
	private int buffersize;

	private FileOutputStream fos = null;
	private BufferedInputStream fis = null;

	public UploadStreamDepositer(InputStream stream, String outputFilePath,
			String outputFileName, int buffersize,String randomNumber,String type) {
		this.stream = stream;
		this.outputFileName = outputFileName;
		this.outputFilePath = outputFilePath;
		this.buffersize = buffersize;
	}

	@Override
	public String start() throws Exception {
		outputFileName = outputFilePath
				+ outputFileName.substring(0,
						outputFileName.lastIndexOf(".")) + "_"
				+ randomNumber + "." + type;
		
		return outputFileName;
	}

	@Override
	public void write() throws Exception {

		File createFile = new File(outputFilePath + outputFileName);

		fos = new FileOutputStream(createFile);
		fis = new BufferedInputStream(stream, buffersize);

		int c;
		byte bufferReadersize[] = new byte[5000];

		while ((c = fis.read(bufferReadersize)) != -1) {
			fos.write(bufferReadersize, 0, c);
		}

	}

	@Override
	public void end() throws Exception {
		if (fos != null) {
			fos.close();
		}
		if (fis != null) {
			fis.close();
		}
	}

}
