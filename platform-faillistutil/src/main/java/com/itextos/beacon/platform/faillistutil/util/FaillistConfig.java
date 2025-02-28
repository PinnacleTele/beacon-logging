package com.itextos.beacon.platform.faillistutil.util;

import com.itextos.beacon.commonlib.utility.CommonUtility;

/**
 * A class to retain the configuration information to be read from the
 * properties file.
 */
public class FaillistConfig
{

    private final String  mFilePath;
    private final int     mNumberSplitLength;
    private final String  mRedisPoolKey;
    private final String  mRedisPrefixKey;
    private final boolean mIsInternational;

    /**
     * To read the Block List specific configurations from the properties file and
     * keep into the memory.
     *
     * @param aFilePath
     *                           - A <code>String</code> representing the folder
     *                           path for the CSV files to be read specific to
     *                           International / Domestic.
     * @param aNumberSplitLength
     *                           - An <code>int</code> representing the length of
     *                           the number to be split into two for the outer key
     *                           and inner list.
     * @param aRedisPoolKey
     *                           - A <code>String</code> representing the Redis Pool
     *                           Key to load or read the data from the Redis.
     * @param aRedisPrefixKey
     *                           - A <code>String</code> representing the Key Prefix
     *                           to be used in the key generation in Redis.
     * @param aIsInternational
     *                           - A <code>boolean</code> representing whether the
     *                           configuration is related specific to International
     *                           / Domestic.
     */
    public FaillistConfig(
            String aFilePath,
            int aNumberSplitLength,
            String aRedisPoolKey,
            String aRedisPrefixKey,
            boolean aIsInternational)
    {
        super();
        mFilePath          = aFilePath;
        mNumberSplitLength = aNumberSplitLength;
        mRedisPoolKey      = aRedisPoolKey;
        mRedisPrefixKey    = aRedisPrefixKey;
        mIsInternational   = aIsInternational;
    }

    public String getFilePath()
    {
        return mFilePath;
    }

    public int getNumberSplitLength()
    {
        return mNumberSplitLength;
    }

    public String getRedisPoolKey()
    {
        return mRedisPoolKey;
    }

    public String getRedisPrefixKey()
    {
        return mRedisPrefixKey;
    }

    public boolean isInternational()
    {
        return mIsInternational;
    }

    @Override
    public String toString()
    {
        return "FailListConfig [mFilePath=" + mFilePath + ", mNumberSplitLength=" + mNumberSplitLength + ", mRedisPoolKey=" + mRedisPoolKey + ", mRedisPrefixKey=" + mRedisPrefixKey
                + ", mIsInternational=" + mIsInternational + "]";
    }

    public void validate()
    {
        if (mNumberSplitLength <= 0)
            throw new RuntimeException(getIntlString() + "Invalid Number Split Length specified. Number Split Length : '" + mNumberSplitLength + "'");

        if ("".equals(CommonUtility.nullCheck(mRedisPoolKey, true)))
            throw new RuntimeException(getIntlString() + "Invalid Redis Pool Key specified.");

        if ("".equals(CommonUtility.nullCheck(mRedisPrefixKey, true)))
            throw new RuntimeException(getIntlString() + "Invalid Redis Prefix Key specified.");
    }

    private String getIntlString()
    {
        return (mIsInternational ? "Internation :: " : "Domestic :: ");
    }

}