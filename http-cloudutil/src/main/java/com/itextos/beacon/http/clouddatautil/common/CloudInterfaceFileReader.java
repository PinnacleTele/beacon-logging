package com.itextos.beacon.http.clouddatautil.common;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class CloudInterfaceFileReader
{

    private final static Log log = LogFactory.getLog(CloudInterfaceFileReader.class);

    private String           platformURL;
    private String           platformBulkURL;

    PropertiesConfiguration  propConf;

    private static class SingletonHolder
    {

        static final CloudInterfaceFileReader INSTANCE = new CloudInterfaceFileReader();

    }

    public static CloudInterfaceFileReader getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    public CloudInterfaceFileReader()
    {

        try
        {
            propConf = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.CLOUD_INTERFACE_PROPERTIES, true);
            readProperties();
        }
        catch (final Exception e)
        {
            log.error("Problem loading the properties file. Exiting the application.", e);
            System.exit(-1);
        }
    }

    private void readProperties()
    {
        platformURL     = CommonUtility.nullCheck(propConf.getString(Constants.PROPERTY_PLATFORM_URL), true);
        platformBulkURL = CommonUtility.nullCheck(propConf.getString(Constants.PROPERTY_PLATFORM_URL_BULK), true);

        if (!platformURL.contains("?"))
            platformURL = platformURL + "?";

        log.warn(" Platform URL to Hit: '" + platformURL + "'");
        log.warn(" Platform URL to Hit BULK: '" + platformBulkURL + "'");
    }

    public String getPlatformURL()
    {
        return platformURL;
    }

    public void setPlatformURL(
            String aPlatformURL)
    {
        platformURL = aPlatformURL;
    }

    public String getPlatformBulkURL()
    {
        return platformBulkURL;
    }

    public void setPlatformBulkURL(
            String aPlatformBulkURL)
    {
        platformBulkURL = aPlatformBulkURL;
    }

    @Override
    public String toString()
    {
        return "CloudInterfaceFileReader [platformURL=" + platformURL + ", platformBulkURL=" + platformBulkURL + ", propConf=" + propConf + "]";
    }

}