package com.itextos.beacon.platform.faillistutil.util;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;

/**
 * A class to read and load the properties file.
 */
public class FaillistPropertyLoader
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final FaillistPropertyLoader INSTANCE = new FaillistPropertyLoader();

    }

    public static FaillistPropertyLoader getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private static final Log log = LogFactory.getLog(FaillistPropertyLoader.class);

    private FaillistConfig   mInternationalConfig;
    private FaillistConfig   mDomesticConfig;
    private int              mBatchSize;

    private FaillistPropertyLoader()
    {

        try
        {
            final PropertiesConfiguration lPropertyConfig     = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.FAILLIST_PROPERTTIES, true);
            final String                  lIntlFilePath       = lPropertyConfig.getString(FaillistConstants.INTL_FILE_PATH);
            final int                     lIntlNumSplitLen    = lPropertyConfig.getInt(FaillistConstants.INTL_NUMBER_SPLIT_LENGTH);
            final String                  lIntlRedisPoolKey   = lPropertyConfig.getString(FaillistConstants.INTL_REDIS_POOL_KEY);
            final String                  lIntlRedisPrefixKey = lPropertyConfig.getString(FaillistConstants.INTL_REDIS_PREFIX_KEY);

            mInternationalConfig = new FaillistConfig(lIntlFilePath, lIntlNumSplitLen, lIntlRedisPoolKey, lIntlRedisPrefixKey, true);

            if (log.isDebugEnabled())
                log.debug("Internation Config : '" + mInternationalConfig + "'");

            mInternationalConfig.validate();

            final String domFilePath         = lPropertyConfig.getString(FaillistConstants.DOMESTIC_FILE_PATH);
            final int    domesNumSplitLen    = Integer.parseInt(lPropertyConfig.getString(FaillistConstants.DOMESTIC_NUMBER_SPLIT_LENGTH));
            final String domesRedisPoolKey   = lPropertyConfig.getString(FaillistConstants.DOMESTIC_REDIS_POOL_KEY);
            final String domesRedisPrefixKey = lPropertyConfig.getString(FaillistConstants.DOMESTIC_REDIS_PREFIX_KEY);

            mDomesticConfig = new FaillistConfig(domFilePath, domesNumSplitLen, domesRedisPoolKey, domesRedisPrefixKey, false);

            if (log.isDebugEnabled())
                log.debug("Domestic Config    : '" + mDomesticConfig + "'");

            mDomesticConfig.validate();

            try
            {
                mBatchSize = lPropertyConfig.getInt(FaillistConstants.BATCH_PROCESS_SIZE);
            }
            catch (final Exception e)
            {
                mBatchSize = 1000;
            }

            if (log.isDebugEnabled())
                log.debug("Batch Size : '" + mBatchSize + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while reading properties", e);
            System.exit(-1);
        }
    }

    public FaillistConfig getInternationConfig()
    {
        return mInternationalConfig;
    }

    public FaillistConfig getDomesticConfig()
    {
        return mDomesticConfig;
    }

    public int getBatchSize()
    {
        return mBatchSize;
    }

}
