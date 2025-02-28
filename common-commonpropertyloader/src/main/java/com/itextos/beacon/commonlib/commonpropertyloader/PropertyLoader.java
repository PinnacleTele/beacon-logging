package com.itextos.beacon.commonlib.commonpropertyloader;

import java.io.FileReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyLoader
{

    private static final Log    log                          = LogFactory.getLog(PropertyLoader.class);
    private static final String COMMON_PROPERY_FILE_LOCATION = "common.property.file.location";

    private static final Set<String> profileFile=new HashSet<String>();
    static {
    	
    	profileFile.add("/common-db.properties");
    	profileFile.add("/kafka-consumer.properties");
    	profileFile.add("/kafka-producer.properties");
    	profileFile.add("/elasticsearch.properties");
    	profileFile.add("/kafka2elasticsearch.properties");

    }
    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final PropertyLoader INSTANCE = new PropertyLoader();

    }

    public static PropertyLoader getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private PropertiesConfiguration                            mCommonConfiguration;
    private final Map<PropertiesPath, PropertiesConfiguration> mPropertiesConfigMap = new EnumMap<>(PropertiesPath.class);
    private final Map<PropertiesPath, Properties>              mPropertiesMap       = new EnumMap<>(PropertiesPath.class);
    private final Map<String, Properties>                      mPropertiesFileMap   = new HashMap<>();

    private PropertyLoader()
    {
        loadCommonProperties();
    }

    private void loadCommonProperties()
    {
         String commonPropertiesLocation = System.getProperty(COMMON_PROPERY_FILE_LOCATION);

        commonPropertiesLocation="/global.properties";
        
        if (log.isDebugEnabled())
            log.debug("Common Properties Key : '" + COMMON_PROPERY_FILE_LOCATION + "'. Common Properties Path : '" + commonPropertiesLocation + "'");

        try
        {
            mCommonConfiguration = new PropertiesConfiguration(commonPropertiesLocation);
            mCommonConfiguration.setReloadingStrategy(new FileChangedReloadingStrategy());

            if (log.isDebugEnabled())
                log.debug("Loading Global Properties completed.");
        }
        catch (final Throwable e)
        {
            final String s = "Problem while loading the global properties file. Refer exception below. ********* Exiting the application. ********* ";
            log.error(s, e);
            System.err.println(s);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public PropertiesConfiguration getPropertiesConfiguration(
            final PropertiesPath aPropertiesKey,
            final boolean aStopIfNotAvailable)
    {
    	
    
    	
    		final PropertiesConfiguration obj = mPropertiesConfigMap.get(aPropertiesKey);

            if (obj == null)
                return loadPropertiesConfiguration(aPropertiesKey, aStopIfNotAvailable);
            return obj;
    }

    public Properties getProperties(
            final PropertiesPath aKey,
            final boolean aStopIfNotAvailable)
    {
        final Properties properties = mPropertiesMap.get(aKey);

        if (properties == null)
            return loadProperties(aKey, aStopIfNotAvailable);
        return properties;
    }

    public Properties getPropertiesByFileName(
            String aPropertiesFileName)
    {

        try
        {
            Properties props = mPropertiesFileMap.get(aPropertiesFileName);

            if (props == null)
            {
                props = loadPropertiesByFileName(aPropertiesFileName);

                mPropertiesFileMap.put(aPropertiesFileName, props);
                if (log.isDebugEnabled())
                    log.debug("Loading Properties from '" + aPropertiesFileName + "' is completed.");
            }
            return props;
        }
        catch (final Throwable e)
        {
            final String s = "Problem while loading the properties file from file : '" + aPropertiesFileName + "'. Refer the exception below.";
            log.error(s);
            System.err.println(s);
            e.printStackTrace();

            final String msg = "Due to the properties loading issue, ********* Exiting the application *********";
            log.error(msg, e);
            System.err.println(msg);
            System.exit(-1);
        }
        return null;
    }

    private PropertiesConfiguration loadPropertiesConfiguration(
            final PropertiesPath aPropertiesKey,
            final boolean aStopAppIfNotLoaded)
    {

        try
        {
        	 String                  propertiesPath="";
            if(PropertiesPath.DN_PAYLOAD_PARAMS_PROPERTIES==aPropertiesKey) {
            	
            	propertiesPath ="/payload-params.properties";
            	
            }else if(PropertiesPath.MESSAGE_KEY_REMOVE_PROPERTIES==aPropertiesKey) {
            	
            	propertiesPath ="/messageremove.properties";

            }else {
            	
            	propertiesPath   = mCommonConfiguration.getString(aPropertiesKey.getKey());
            	
            	propertiesPath=propertiesPath.substring(propertiesPath.lastIndexOf('/'));

            }

            if(profileFile.contains(propertiesPath)) {
            	
            	propertiesPath+="_"+System.getenv("profile");
            }
            
            final PropertiesConfiguration propConfiguration = new PropertiesConfiguration(propertiesPath);
            propConfiguration.setReloadingStrategy(new FileChangedReloadingStrategy());
            mPropertiesConfigMap.put(aPropertiesKey, propConfiguration);

            if (log.isDebugEnabled())
                log.debug("Loading PropertiesConfiguration for key : '" + aPropertiesKey + "' from '" + propertiesPath + "' is completed.");

            return propConfiguration;
          
        }
        catch (final Throwable e)
        {
            log.error("Problem while loading the properties file for the key : '" + aPropertiesKey + "'. Refer the exception below.");

            if (aStopAppIfNotLoaded)
            {
                log.error("Due to the properties loading issue, ********* Exiting the application *********", e);
                System.exit(-1);
            }
            else
            {
                final String msg = "********* WARNING ********** Although properties loading is having issue for the key : '" + aPropertiesKey + "', application is still continuing....";
                log.fatal(msg, e);
                System.err.println(msg);
                e.printStackTrace();
                throw new RuntimeException(msg, e);
            }
        }
        return null;
    }

    private Properties loadProperties(
            final PropertiesPath aPropertiesKey,
            final boolean aStopAppIfNotLoaded)
    {

        try
        {
            final String     propertyPath = mCommonConfiguration.getString(aPropertiesKey.getKey());
            final Properties props        = loadPropertiesByFileName(propertyPath);
            mPropertiesMap.put(aPropertiesKey, props);

            if (log.isDebugEnabled())
                log.debug("Loading Properties for key : '" + aPropertiesKey + "' from '" + propertyPath + "' is completed.");

            return props;
        }
        catch (final Throwable e)
        {
            final String s = "Problem while loading the properties file for the key : '" + aPropertiesKey + "'. Refer the exception below.";
            log.error(s);
            System.err.println(s);
            e.printStackTrace();

            if (aStopAppIfNotLoaded)
            {
                final String msg = "Due to the properties loading issue, ********* Exiting the application *********";
                log.error(msg, e);
                System.err.println(msg);
                System.exit(-1);
            }
            else
            {
                final String msg = "********* WARNING ********** Although properties loading is having issue for the key : '" + aPropertiesKey + "', application is still continuing....";
                log.fatal(msg, e);
                System.err.println(msg);
                throw new RuntimeException(msg, e);
            }
        }
        return null;
    }

    private static Properties loadPropertiesByFileName(
            final String aPropertiesFilePath)
            throws IOException
    {
        Properties props = null;

        try (
                FileReader fr = new FileReader(aPropertiesFilePath);)
        {
            props = new Properties();
            props.load(fr);
        }
        return props;
    }

    public String getPropertiesFilePath(
            final String aPropertiesKey)
    {
        return mCommonConfiguration.getString(aPropertiesKey);
    }

}