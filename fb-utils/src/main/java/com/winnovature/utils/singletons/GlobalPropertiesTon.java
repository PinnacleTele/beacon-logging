package com.winnovature.utils.singletons;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.winnovature.utils.utils.Constants;

public class GlobalPropertiesTon {
	static Log log = LogFactory.getLog(Constants.UtilsLogger);

	private static GlobalPropertiesTon globalProp = new GlobalPropertiesTon();
	private PropertiesConfiguration globalConfiguration;
	private Configuration systemConfiguration;

	final static String GLOBAL_PROPERTY = "global.properties.loc";

	public static GlobalPropertiesTon getInstance() {
		return globalProp;
	}

	private void loadGlobalProperties() throws Exception {
		if (log.isDebugEnabled())
			log.debug("LOADING GLOBAL PROPERTIES....");

		systemConfiguration = new SystemConfiguration();
		String globalPropertiesLocation = (String) systemConfiguration.getProperty(GLOBAL_PROPERTY);
		globalPropertiesLocation="/fileprocessor/global.properties_"+System.getenv("propertyip");
		if (StringUtils.isNotEmpty(globalPropertiesLocation)) {
			if (log.isDebugEnabled())
				log.debug("Global Property File Location - " + globalPropertiesLocation);

			/** Load the properties * */
			globalConfiguration = new PropertiesConfiguration(globalPropertiesLocation);
			globalConfiguration.setReloadingStrategy(new FileChangedReloadingStrategy());
			if (log.isDebugEnabled())
				log.debug("GLOBAL PROPERTIES LOADED....");
		} else {
			throw new Exception("[GlobalPropertiesTon] COULD NOT LOCATE -Dglobal.properties.loc PARAMETER");
		}
	}

	public PropertiesConfiguration getGlobalConfigObj() throws Exception {
		if (globalConfiguration == null)
			loadGlobalProperties();

		return globalConfiguration;
	}
}
