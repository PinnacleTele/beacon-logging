package com.winnovature.dltfileprocessor.singletons;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.dltfileprocessor.utils.Constants;
import com.winnovature.utils.singletons.GlobalPropertiesTon;

public class DltFileProcessorPropertiesTon {

	static Log log = LogFactory.getLog(Constants.FileUploadLogger);

	private static DltFileProcessorPropertiesTon singleton = new DltFileProcessorPropertiesTon();
	private PropertiesConfiguration propConf;
	static final String propertyFileLookupKey = "dlt.file.processor.properties.loc";
	private String className = "DltFileProcessorPropertiesTon";

	private DltFileProcessorPropertiesTon() {
	}

	public static DltFileProcessorPropertiesTon getInstance() {
		return singleton;
	}

	public PropertiesConfiguration getPropertiesConfiguration() throws Exception {
		if (propConf == null)
			loadProperties();

		return propConf;
	}

	private void loadProperties() throws Exception {
		String methodName = " [loadProperties] ";
		if (log.isDebugEnabled())
			log.debug(className + methodName + " LOADING PROPERTIES....");

		PropertiesConfiguration globalProp;
		/** Get the Path for the Properties from the Global Properties * */
		try {
			globalProp = GlobalPropertiesTon.getInstance().getGlobalConfigObj();
		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			throw new Exception("***** ERROR: GETTING GLOBAL PROPERTIES CONFIGURATION *****");
		}

		try {
			String propertyFilePath = globalProp.getString(propertyFileLookupKey);

			if (StringUtils.isNotBlank(propertyFilePath)) {
				if (log.isDebugEnabled())
					log.debug(className + methodName + " Properties file - " + propertyFilePath);
				propConf = new PropertiesConfiguration(propertyFilePath);
				propConf.setReloadingStrategy(new FileChangedReloadingStrategy());
			} else {
				throw new Exception(
						"****** ERROR: EITHER IT COULD NOT FIND " + propertyFilePath + " PROPERTY OR HAVE NO VALUES ****");
			}
		} catch (Exception e) {
			log.error(className + methodName + "Exception: ", e);
			throw e;
		}
	}

}
