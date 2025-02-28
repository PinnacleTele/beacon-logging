package com.winnovature.fileparser.factory;

import java.io.InputStream;

import com.winnovature.fileparser.impl.UploadStreamDepositer;
import com.winnovature.fileparser.impl.ZipUploadStreamDepositer;
import com.winnovature.fileparser.interfaces.FileDepositer;

public class FileDepositerFatory {
	public static FileDepositer get(InputStream stream, String outputFilePath,
			String outputFileName, int buffersize, String randomNumber,
			String type) {
		
		if ("zip".equalsIgnoreCase(type)) {
			return new ZipUploadStreamDepositer(stream, outputFilePath,
					outputFileName, buffersize, randomNumber, type);
		} else {
			return new UploadStreamDepositer(stream, outputFilePath,
					outputFileName, buffersize, randomNumber, type);
		}
	}
}
