package com.winnovature.dltfileprocessor.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ZipHandler {
	static Log log = LogFactory.getLog(Constants.FileUploadLogger);

	public List<Map<String, Object>> extractZipFileContent(String inputZipFile, String pathToExtract) throws Exception {
		List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();
		try (java.util.zip.ZipFile zipFile = new ZipFile(inputZipFile)) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				UUID uuid = UUID.randomUUID();
				String extension = "." + FilenameUtils.getExtension(entry.getName());
				
				String storedFileName = StringUtils.replace(entry.getName(), extension, "")
						.concat("_" + uuid.toString()).concat(extension);
				
				String tempFileName = StringUtils.replace(entry.getName(), extension, "")
						.concat("_" + uuid.toString())
						.concat("_"
								+ com.winnovature.utils.utils.Utility.getCustomDateAsString("yyyy-MM-dd_HHmmssSSS"))
						.concat(extension);
				
				File entryDestination = null;
				if (extension.equalsIgnoreCase(".csv")) {
					entryDestination = new File(pathToExtract, tempFileName);
				} else {
					entryDestination = new File(pathToExtract, storedFileName);
				}
				if (entry.isDirectory()) {
					// sub directories/folders are not allowed as of now
					//entryDestination.mkdirs();
				} else {
					entryDestination.getParentFile().mkdirs();
					try (InputStream in = zipFile.getInputStream(entry);
							OutputStream out = new FileOutputStream(entryDestination)) {
						
						IOUtils.copy(in, out);
						if (extension.equalsIgnoreCase(".csv")) {
							com.winnovature.utils.utils.Utility.storeCSVFile(pathToExtract + tempFileName, pathToExtract + storedFileName);
						}
						Map<String, Object> data = new HashMap<String, Object>();
						data.put("filename", entry.getName());
						data.put("r_filename", storedFileName);
						response.add(data);
					}
				}
			}
		} catch (Exception e) {
			log.error("[ZipHandler] [extractZipFileContent] Exception ", e);
			throw e;
		}
		return response;
	}
}
