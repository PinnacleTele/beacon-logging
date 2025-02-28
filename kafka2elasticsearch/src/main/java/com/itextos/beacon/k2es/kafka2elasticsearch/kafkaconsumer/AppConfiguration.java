package com.itextos.beacon.k2es.kafka2elasticsearch.kafkaconsumer;

import java.io.FileReader;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AppConfiguration
{

    private static final Log log       = LogFactory.getLog(AppConfiguration.class);
    private final Properties prpConfig = new Properties();

    public AppConfiguration(
            String fileName)
            throws Exception
    {
        loadFile(fileName);
    }

    private void loadFile(
            String fileName)
            throws Exception
    {
        final FileReader cfgReader = new FileReader(fileName);
        prpConfig.load(cfgReader);
        cfgReader.close();
    }

    public String getString(
            String key)
    {
        return prpConfig.getProperty(key).trim();
    }

    public int getInt(
            String key)
    {
        int          retVal = -99999;
        final String keyVal = prpConfig.getProperty(key).trim();

        try
        {
            retVal = Integer.parseInt(keyVal);
        }
        catch (final Exception ex)
        {
            log.error("Unable to Convert the Key: " + key + ", Value: " + keyVal + " to Integer");
            log.error(ex.getMessage());
        }
        return retVal;
    }

}
