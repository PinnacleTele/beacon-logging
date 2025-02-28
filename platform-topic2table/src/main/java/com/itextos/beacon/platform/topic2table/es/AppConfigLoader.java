package com.itextos.beacon.platform.topic2table.es;

import com.itextos.beacon.errorlog.K2ESLog;

public class AppConfigLoader
{

    private static final K2ESLog                              log                     = K2ESLog.getInstance();
    private static final String APP_CONFIG_FILE_LOCATION = "kafka.2.elasticsearch.config.file";
    private AppConfiguration    APP_CONFG;

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final AppConfigLoader INSTANCE = new AppConfigLoader();

    }

    private AppConfigLoader()
    {
        loadCommonProperties();
    }

    public static AppConfigLoader getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    public AppConfiguration getAppConfiguration()
    {
        return APP_CONFG;
    }

    private void loadCommonProperties()
    {
        final String commonPropertiesLocation = System.getProperty(APP_CONFIG_FILE_LOCATION);

        if (log.isDebugEnabled())
            log.debug("Common Properties Key : '" + APP_CONFIG_FILE_LOCATION + "'. Common Properties Path : '" + commonPropertiesLocation + "'");

        try
        {
            APP_CONFG = new AppConfiguration(commonPropertiesLocation);
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

}
