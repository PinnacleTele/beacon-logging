package com.itextos.beacon.commonlib.shortcodeprovider.operation;

import java.io.File;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.ShortcodeLength;

public class ShortCodeProperties
{

    private static final String BASE_PATH         = "shortcode.base.path";
    private static final String REDIS_MIN_COUNT   = "shortcode.redis.min.count";
    private static final String LEN_5_EXPIRY_DAYS = "expiry.days.len.5";
    private static final String LEN_6_EXPIRY_DAYS = "expiry.days.len.6";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final ShortCodeProperties INSTANCE = new ShortCodeProperties();

    }

    public static ShortCodeProperties getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final PropertiesConfiguration mPropConf;

    private ShortCodeProperties()
    {
        mPropConf = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.SHORTCODE_PROVIDER_PATH, true);
    }

    public String getShortCodeFilePath(
            ShortcodeLength aShortCodeLen)
    {
        final String s       = mPropConf.getString(BASE_PATH);
        final String moredir = aShortCodeLen == ShortcodeLength.LENGTH_5 ? "strlen_5" : "strlen_6";
        return s + File.separator + moredir + File.separator;
    }

    public int getMinimumRedisCount()
    {
        return mPropConf.getInt(REDIS_MIN_COUNT, 50000);
    }

    public int getExpiryDays(
            ShortcodeLength aShortCodeLen)
    {

        switch (aShortCodeLen)
        {
            case LENGTH_5:
                return mPropConf.getInt(LEN_5_EXPIRY_DAYS, 30);

            case LENGTH_6:
                return mPropConf.getInt(LEN_6_EXPIRY_DAYS, 30);

            default:
                break;
        }
        return 100;
    }

}