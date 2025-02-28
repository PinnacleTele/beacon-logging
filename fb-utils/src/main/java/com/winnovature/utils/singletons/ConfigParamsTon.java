package com.winnovature.utils.singletons;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.utils.daos.GenericDao;
import com.winnovature.utils.utils.Constants;

public class ConfigParamsTon {

	private static String className = "[ConfigParamsTon]";
	static Log log = LogFactory.getLog(Constants.UtilsLogger);

	private static ConfigParamsTon singleton;
	private static Map<String, String> configDetailsMap = null;

	private ConfigParamsTon() {

	}

	public static ConfigParamsTon getInstance() {

		if (singleton == null) {
			singleton = new ConfigParamsTon();
		}

		return singleton;
	}

	public Map<String, String> getConfigurationFromconfigParams() throws Exception {

		if (configDetailsMap == null || configDetailsMap.size() == 0)
			load();

		return configDetailsMap;
	}

	public void reload() throws Exception {
		// configDetailsMap = null;
		load();
	}

	public void load() throws Exception {
		String methodName = " [load] ";
		try {

			Map<String, String> map = new GenericDao().getConfigurations();

			if (map != null && map.size() > 0)
				configDetailsMap = map;
			else {
				configDetailsMap = new HashMap<String, String>();
				log.error(className + methodName + " ERROR : ****** COULD NOT FIND DATA IN CONFIG_PARAMS ******");
			}

		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			throw e;
		}
	}
}
