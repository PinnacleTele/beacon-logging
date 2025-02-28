package com.winnovature.fileparser.impl;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.winnovature.fileparser.interfaces.FileDepositer;

public class ZipUploadStreamDepositer implements FileDepositer {

	private ZipInputStream stream;
	private String outputFilePath;
	private String outputFileName;
	private String randomNumber;
	private String type;

	public ZipUploadStreamDepositer(InputStream stream, String outputFilePath,
			String outputFileName, int buffersize,String randomNumber,String type) {
		this.stream = new ZipInputStream(stream);
		this.outputFileName = outputFileName;
		this.outputFilePath = outputFilePath;
		this.randomNumber = randomNumber;
		this.type = type;
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
		@SuppressWarnings("unused")
		ZipEntry entry;
		byte[] buffer = new byte[2048];
		while ((entry = stream.getNextEntry()) != null) {
			FileOutputStream output = null;
			try {
				output = new FileOutputStream(outputFileName);
				int len = 0;
				while ((len = stream.read(buffer)) > 0) {
					output.write(buffer, 0, len);
				}
			} finally {
				if (output != null) {
					output.close();
				}
			}
		}
	}

	@Override
	public void end() throws Exception {
		if(stream!=null){
			stream.close();
		}
	}

}
