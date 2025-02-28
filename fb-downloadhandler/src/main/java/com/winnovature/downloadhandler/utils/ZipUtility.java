package com.winnovature.downloadhandler.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ZipUtility {

	static Log log = LogFactory.getLog(Constants.DownloadHandlerLogger);
	private static final String className = "ZipUtility";

	public static void archive(List<String> srcFiles, String targetFileName) throws Exception {
		String logname = className + " [archive] ";
		if (log.isDebugEnabled()) {
			log.debug(logname + " Begin srcFiles=" + srcFiles);
		}
		FileOutputStream fos = null;
		ZipOutputStream zipOut = null;

		try {
			fos = new FileOutputStream(targetFileName);
			zipOut = new ZipOutputStream(fos);

			for (String srcFile : srcFiles) {
				File fileToZip = new File(srcFile);
				FileInputStream fis = new FileInputStream(fileToZip);
				ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
				zipOut.putNextEntry(zipEntry);

				byte[] bytes = new byte[1024];
				int length;
				while ((length = fis.read(bytes)) >= 0) {
					zipOut.write(bytes, 0, length);
				}
				fis.close();
			}
			if (log.isDebugEnabled()) {
				log.debug(
						logname + " Archive(" + srcFiles.size() + " files) success, targetFileName " + targetFileName);
			}
		} catch (Exception e) {
			log.error(logname + " Exception archiving files", e);
			throw e;
		} finally {
			try {
				zipOut.close();
				fos.close();
			} catch (Exception e) {
				log.error(logname + " Exception closing streams ", e);
			}
		}

	}

}
