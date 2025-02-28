package com.itextos.beacon.platform.topic2table.es;

import java.io.FileReader;
import java.util.Properties;

import com.itextos.beacon.errorlog.K2ESLog;

public class AppConfiguration
{

    private static final K2ESLog                              log                     = K2ESLog.getInstance();
    private  Properties prpConfig = new Properties();

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
/*
    private static Properties readProperties() throws ItextosRuntimeException
    {

        try
        {
            final PropertiesConfiguration pc = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.COMMON_K2ES_PROPERTIES, true);

            if (pc != null)
            {
                final Properties       props   = new Properties();
                final Iterator<String> keys    = pc.getKeys();
                String                 currKey = null;

                while (keys.hasNext())
                {
                    currKey = keys.next();
                    props.setProperty(currKey, pc.getString(currKey));
                }

                return props;
            }
            throw new ItextosRuntimeException("Unable to load the common db properties");
        }
        catch (final Exception exp)
        {
            log.error("Problem loading property file...", exp);
            throw new ItextosRuntimeException("Unable to load the common db properties");
        }
    }
  */  
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
