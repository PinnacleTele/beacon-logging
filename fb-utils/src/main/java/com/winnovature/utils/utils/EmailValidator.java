package com.winnovature.utils.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EmailValidator {
	static Log log = LogFactory.getLog(Constants.UtilsLogger);
	private static final String className = "[EmailValidator]";

	private static final String regex1 = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

	public static boolean validate(String email, String regex) {
		boolean result = false;
		try {
			if (StringUtils.isBlank(email)) {
				return false;
			} else if (StringUtils.isBlank(regex)) {
				regex = regex1;
			}
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(email);
			result = matcher.matches();
		} catch (Exception e) {
			log.error(className + " [validate] Exception ::: ", e);
		}
		return result;
	}

}
