package com.winnovature.fileuploads.utils;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.utils.utils.UploadedFilesTrackingUtility;

public class Utility {
	static Log log = LogFactory.getLog(Constants.FileUploadLogger);

	public static String humanReadableNumberFormat(long count) {
		String resp = null;
		DecimalFormat format = new DecimalFormat("0.#");
		try {
			resp = format.format(count);
			if (count < 1000)
				return "" + count;
			int exp = (int) (Math.log(count) / Math.log(1000));
			String value = format.format(count / Math.pow(1000, exp));
			resp = String.format("%s%c", value, "kMBTPE".charAt(exp - 1));
		} catch (Exception e) {
			log.error("[FileReadService] [humanReadableNumberFormat] Exception", e);
		}
		return resp;
	}
	
	public static long getTimeDifference(Instant start) {
		Instant finish = Instant.now();
		return Duration.between(start, finish).toMillis(); 
	}
	
	// push filenames to redis for cleaning purpose
	public static boolean sendFilesToTrackingRedis(String requestFrom, String username, List<String> filesList) {
		if (requestFrom.equalsIgnoreCase(Constants.CAMPAIGN)) {
			UploadedFilesTrackingUtility.setUploadedFilesInfo(Constants.CAMPAIGN, username, filesList);
		} else if (requestFrom.equalsIgnoreCase(Constants.GROUP)) {
			UploadedFilesTrackingUtility.setUploadedFilesInfo(Constants.GROUP, username, filesList);
		} else if (requestFrom.equalsIgnoreCase(Constants.TEMPLATE)) {
			UploadedFilesTrackingUtility.setUploadedFilesInfo(Constants.TEMPLATE, username, filesList);
		}
		return true;
	}

}
