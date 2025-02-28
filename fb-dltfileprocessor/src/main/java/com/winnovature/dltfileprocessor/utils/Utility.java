package com.winnovature.dltfileprocessor.utils;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
			log.error("[Utility] [humanReadableNumberFormat] Exception", e);
		}
		return resp;
	}

	public static long getTimeDifference(Instant start) {
		Instant finish = Instant.now();
		return Duration.between(start, finish).toMillis();
	}

}
