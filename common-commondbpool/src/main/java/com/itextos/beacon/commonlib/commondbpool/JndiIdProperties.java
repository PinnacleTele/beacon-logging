package com.itextos.beacon.commonlib.commondbpool;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;

public class JndiIdProperties
{

    private static final Log log = LogFactory.getLog(JndiIdProperties.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final JndiIdProperties INSTANCE = new JndiIdProperties();

    }

    public static JndiIdProperties getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final PropertiesConfiguration propConf;

    private JndiIdProperties()
    {
        propConf = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.JNDI_PROPERTIES, true);
    }

    public int getJndiProperty(
            String aJndiIdKey)
    {
        final int returnValue = propConf.getInt(aJndiIdKey, -1);

        if (returnValue == -1)
            log.fatal("Unable to find the JNDI Id for the jndi name '" + aJndiIdKey + "' in '" + PropertyLoader.getInstance().getPropertiesFilePath(PropertiesPath.JNDI_PROPERTIES.getKey()));

        return returnValue;
    }

}