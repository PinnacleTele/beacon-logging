package com.winnovature.cronjobs.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Utility {
	static Log log = LogFactory.getLog(Constants.CronJobLogger);
	private static final String className = "[Utility]";

	public List<String> deleteFiles(List<String> files) {
		String logname = className + " [deleteFiles] ";
		List<String> notDeleted = null;
		try {
			File file = null;
			for (String filepath : files) {
				file = new File(filepath);
				if (!file.delete()) {
					if (notDeleted == null) {
						notDeleted = new ArrayList<String>();
					}
					notDeleted.add(filepath);
				}
			}
		} catch (Exception e) {
			log.error(logname + " Exception: ", e);
		}
		return notDeleted;
	}

}
