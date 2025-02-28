package com.winnovature.utils.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.winnovature.utils.singletons.ConfigParamsTon;

public class Utility {
	static Log log = LogFactory.getLog(Constants.UtilsLogger);
	private static final String className = "[Utility]";

	public static int getThreadSleepTime() {
		String logName = className + " [getThreadSleepTime]";
		int threadsleeptime = 1000;
		try {
			Map<String, String> configMap = ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
			String strThreadTime = configMap.get(Constants.THREAD_SLEEP_TIME_IN_MILLISECONDS);
			if (strThreadTime != null) {
				threadsleeptime = Integer.parseInt(strThreadTime);
			}
		} catch (Exception e) {
			log.error(logName, e);
		}
		return threadsleeptime;
	}

	public static long getConsumersSleepTime() {
		String logName = className + " [ getConsumersSleepTime ]";
		long sleeptime = 1000;
		try {
			Map<String, String> configMap = ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
			String timeInMillis = configMap.get(Constants.CONSUMERS_SLEEP_TIME_IN_MILLI_SECONDS);
			if (timeInMillis != null) {
				sleeptime = Long.parseLong(timeInMillis);
			}
		} catch (Exception e) {
			log.error(logName, e);
			sleeptime = 1000;
		}
		return sleeptime;
	}

	public static String getCustomDateAsString(String format) {
		String methodName = " [getCustomDateAsString(format)] ";
		DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date date = Calendar.getInstance().getTime();
		String timeStampAsString = null;
		try {
			if (format != null) {
				formatter = new SimpleDateFormat(format);
			}
			timeStampAsString = formatter.format(date);
		} catch (Exception e) {
			log.error(className + methodName + " Exception when formating date. Hence setting default format :"
					+ Constants.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS + " \n", e);
			formatter = new SimpleDateFormat(Constants.DATE_FORMAT_DD_MM_YYYY_HH_MM_SS);
			timeStampAsString = formatter.format(date);
		}
		return timeStampAsString;
	}

	// Method to get timestamp for posting in REDIS to check thread HeartBeat
	public static String getTimestampAsString() {
		Date date = Calendar.getInstance().getTime();
		DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String timeStampAsString = formatter.format(date);
		return timeStampAsString;
	}

	public static long getIdleThreadSleepTime() {
		String logName = className + " [getIdleThreadSleepTime]";
		long threadsleeptime = 1000;
		try {
			Map<String, String> configMap = ConfigParamsTon.getInstance().getConfigurationFromconfigParams();
			String strThreadTime = configMap.get(Constants.IDLE_THREAD_SLEEP_TIME_IN_MILLISECONDS);
			if (strThreadTime != null) {
				threadsleeptime = Integer.parseInt(strThreadTime);
			}
		} catch (Exception e) {
			log.error(logName, e);
		}
		return threadsleeptime;
	}

	public static String getPlaceholdersFromTemplateMessage(String templateMessage, int msgtype, boolean isColumnBased) {
		String placeholdersCols = null;
		try {
			if (msgtype == 1) {
				templateMessage = UnicodeUtil.INSTANCE.convertHexStringToString(templateMessage);
			}

			List<String> arrayList = new ArrayList<String>();
			// find placeholders
			while (templateMessage.length() > 0) {
				// int openIndex = templateMessage.indexOf("[");
				// int closeIndex = templateMessage.indexOf("]");
				int openIndex = templateMessage.indexOf("{#");
				if(openIndex >=0) {
					openIndex++;
				}
				int closeIndex = templateMessage.indexOf("#}");

				if (openIndex < 0 || closeIndex < 0) {
					break;
				}

				String placeholder = templateMessage.substring(openIndex + 1, closeIndex);
				if(StringUtils.isNotBlank(placeholder)) {
					if(isColumnBased) {
						arrayList.add(placeholder.trim());
					}else {
						try {
							Integer.parseInt(placeholder.trim());
							arrayList.add(placeholder.trim());
						}catch(Exception e) {
							// looks some text present in index based template. 
							// ignore exception and treat it as normal text
						}
					}
				}
				templateMessage = templateMessage.substring(closeIndex + 2);
			}

			if (arrayList.size() > 0) {
				placeholdersCols = StringUtils.join(arrayList, ",");
			}
		} catch (Exception e) {
			log.error(className + "[getPlaceholdersFromTemplateMessage] Exception ", e);
		}
		return placeholdersCols;
	}
	
	// BOMInputStream is used to remove ByteOrderMark from its first bytes.
	// The BOMInputStream class detects these bytes and, if required, can
	// automatically skip them and return the subsequent byte as the first byte in
	// the stream.
	public static void storeCSVFile(String sourceFile, String targetFile) throws Exception {
		BOMInputStream bomIn = new BOMInputStream(new FileInputStream(sourceFile));
		File file = new File(targetFile);
		try (OutputStream outputStream = new FileOutputStream(file)) {
			IOUtils.copy(bomIn, outputStream);
		} catch (Exception e) {
			throw e;
		}
	}

	public static String convertDateFormat(String srcFormat, String trgFormat, String date) throws Exception {
		try {
			DateFormat originalFormat = new SimpleDateFormat(srcFormat);
			DateFormat targetFormat = new SimpleDateFormat(trgFormat);
			Date dt = originalFormat.parse(date);
			return targetFormat.format(dt);
		} catch (Exception e) {
			log.error(className+" [convertDateFormat] Exception "+ e.getMessage());
			return null;
		}
	}

    
	public static String getDefaultCountryCode() {
		String lCountryCode = CommonUtility
				.nullCheck(getConfigParamsValueAsString(ConfigParamConstants.DEFAULT_COUNTRY_CODE), true);
		if (lCountryCode.isBlank())
			lCountryCode = "IND";

		return lCountryCode;
	}

	public static String getConfigParamsValueAsString(ConfigParamConstants aKey) {
		final ApplicationConfiguration lAppConfigValues = (ApplicationConfiguration) InmemoryLoaderCollection
				.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
		return lAppConfigValues.getConfigValue(aKey.getKey());
	}
	
	
	public static String getDefaultCountryCode1() {
		String methodName = " [getDefaultCountryCode()] ";
		String countryCode = "";
		try {
			countryCode = getDefaultCountryCode();
		} catch (Exception e) {
			log.error(className + methodName + " Exception ::: ", e);
			throw e;
		} catch (Throwable t) {
			log.error(className + methodName + " Throwable ::: ", t);
			throw t;
		}
		return countryCode;
	}
	
	
    
}
