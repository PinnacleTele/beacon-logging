package com.itextos.beacon.commonlib.redisstatistics.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.pwdencryption.Encryptor;
import com.itextos.beacon.commonlib.utility.CommonUtility;

class RedisConfig
{

    private static final Log log                            = LogFactory.getLog(RedisConfig.class);

    private static final int DEFAULT_READ_TIMEOUT_SEC       = 3;
    private static final int DEFAULT_CONNECTION_TIMEOUT_SEC = 5;
    private static final int DEFAULT_MAX_WAIT_TIMEOUT_SEC   = 10;
    private static final int DEFAULT_MAX_POOL_SIZE          = 25;
    private static final int DEFAULT_MIN_IDLE               = 1;
    private static final int DEFAULT_MAX_IDLE               = 10;

    private final Component  mComponentType;
    private final int        mRedisMapId;
    private final int        mRedisPoolIndex;
    private final String     mRedisId;
    private final String     mIP;
    private final int        mPort;
    private final String     mPassword;
    private final int        mDatabase;
    private final int        mReadTimeoutInsec;
    private final int        mConnectionTimeoutInsec;
    private final int        mMaxWaitTimeInsec;
    private final int        mMaxPoolSize;
    private final boolean    mDebugEnabled;
    private final int        mMaxIdle;
    private final int        mMinIdle;

    RedisConfig(
            Component aRedisType,
            int aRedisMapId,
            int aRedisPoolIndex,
            String aRedisId,
            String aIP,
            int aPort,
            String aPassword,
            int aDatabase,
            int aReadTimeoutInsec,
            int aConnectionTimeoutInsec,
            int aMaxWaitTimeInsec,
            int aMaxPoolSize,
            int aMaxIdle,
            int aMinIdle,
            boolean aDebugEnabled)
    {
        super();
        mComponentType          = aRedisType;
        mRedisMapId             = aRedisMapId;
        mRedisPoolIndex         = aRedisPoolIndex;
        mRedisId                = aRedisId;
        mIP                     = aIP;
        mPort                   = aPort;
        mPassword               = getDecryptedPassword(mIP, mPort, aPassword);
        mDatabase               = aDatabase;
        mReadTimeoutInsec       = aReadTimeoutInsec < 0 ? DEFAULT_READ_TIMEOUT_SEC : aReadTimeoutInsec;
        mConnectionTimeoutInsec = aConnectionTimeoutInsec < 0 ? DEFAULT_CONNECTION_TIMEOUT_SEC : aConnectionTimeoutInsec;
        mMaxWaitTimeInsec       = aMaxWaitTimeInsec < 0 ? DEFAULT_MAX_WAIT_TIMEOUT_SEC : aMaxWaitTimeInsec;
        mMaxPoolSize            = aMaxPoolSize < 0 ? DEFAULT_MAX_POOL_SIZE : aMaxPoolSize;
        mMinIdle                = getMinIdleValue(aMinIdle, aMaxPoolSize);
        mMaxIdle                = getMaxIdleValue(aMaxIdle, aMaxPoolSize);
        mDebugEnabled           = aDebugEnabled;
    }

    Component getComponent()
    {
        return mComponentType;
    }

    int getRedisPoolIndex()
    {
        return mRedisPoolIndex;
    }

    String getRedisId()
    {
        return mRedisId;
    }

    String getIP()
    {
        return mIP;
    }

    int getPort()
    {
        return mPort;
    }

    String getPassword()
    {
        return mPassword;
    }

    int getDatabase()
    {
        return mDatabase;
    }

    int getRedisMapId()
    {
        return mRedisMapId;
    }

    int getReadTimeoutInsec()
    {
        return mReadTimeoutInsec;
    }

    int getConnectionTimeoutInsec()
    {
        return mConnectionTimeoutInsec;
    }

    int getMaxWaitTimeInsec()
    {
        return mMaxWaitTimeInsec;
    }

    int getMaxPoolSize()
    {
        return mMaxPoolSize;
    }

    int getMaxIdle()
    {
        return mMaxIdle;
    }

    int getMinIdle()
    {
        return mMinIdle;
    }

    boolean isDebugEnabled()
    {
        return mDebugEnabled;
    }

    void isValidConfiig() throws ItextosRuntimeException
    {

        if (mIP.equals(""))
        {
            final String error = "Invalid IP address specified. For Redis Type : '" + mComponentType + "' Redis Index :'" + mRedisId + "'";
            log.error(error);
            throw new ItextosRuntimeException(error);
        }

        try
        {

            if ((mPort < 0) || (mPort > 65535))
            {
                final String error = "Invalid mPort specified. Port : '" + mPort + "' For Redis Type : '" + mComponentType + "' Redis Index :'" + mRedisId + "'";
                log.error(error);
                throw new ItextosRuntimeException(error);
            }

            if (mPort > 49151)
            {
                final String error = "**** WARNING **** : THE SPECIFIED PORT SHOULD NOT USED FOR CONNECTIVITY. THEY ARE *ephemeral* PORTS. Port Specified : '" + mPort + "' For Redis Type : '"
                        + mComponentType + "' Redis Index :'" + mRedisId + "'";
                log.warn(error, new ItextosRuntimeException(error));
            }
        }
        catch (final Exception e)
        {
            final String error = "Invalid mPort specified. Port : '" + mPort + "' For Redis Type : '" + mComponentType + "' Redis Index :'" + mRedisId + "'";
            log.error(error);
            throw new ItextosRuntimeException(error);
        }

        if (mPassword.trim().equals(""))
        {
            final String error = "**** WARNING **** : Empty mPassword was used, which is less security. For Redis Type : '" + mComponentType + "' Redis Index :'" + mRedisId + "'";
            log.warn(error);
        }

        try
        {
            if (mDatabase < 0)
                throw new ItextosRuntimeException("Invalid Database");
        }
        catch (final Exception e)
        {
            final String error = "Invalid mDatabase specified. DB :'" + mDatabase + "' For Redis Type : '" + mComponentType + "' Redis Index :'" + mRedisId + "'";
            log.error(error);
            throw new ItextosRuntimeException(error);
        }
    }

    private static int getMaxIdleValue(
            int aMaxIdle,
            int aMaxPoolSize)
    {
        int lMaxIdle;

        if (aMaxIdle > aMaxPoolSize)
            lMaxIdle = aMaxPoolSize / 2;
        else
            if (aMaxIdle < DEFAULT_MAX_IDLE)
                lMaxIdle = DEFAULT_MAX_IDLE;
            else
                lMaxIdle = aMaxIdle;
        return lMaxIdle;
    }

    private static int getMinIdleValue(
            int aMinIdle,
            int aMaxPoolSize)
    {
        int lMinIdle;

        if (aMinIdle < DEFAULT_MIN_IDLE)
            lMinIdle = DEFAULT_MIN_IDLE;
        else
            if (aMinIdle > aMaxPoolSize)
            {
                if ((aMaxPoolSize < 10))
                    lMinIdle = DEFAULT_MIN_IDLE;
                else
                    lMinIdle = 5;
            }
            else
                if ((aMaxPoolSize < 10))
                    lMinIdle = DEFAULT_MIN_IDLE;
                else
                    lMinIdle = aMinIdle;
        return lMinIdle;
    }

    private static String getDecryptedPassword(
            String aIp,
            int aPort,
            String aEncryptedPassword)
    {
        if (CommonUtility.nullCheck(aEncryptedPassword, true).equals(""))
            return aEncryptedPassword;

        try
        {
            return Encryptor.getDecryptedDbPassword(aEncryptedPassword);
        }
        catch (final Exception e)
        {
            final String s = ">>>>>>>>>>>>>>>>> Invalid password for IP '" + aIp + "' and Port '" + aPort + "'";
            System.err.println(s);
            e.printStackTrace();
            log.error(s, e);
        }

        return null;
    }

    @Override
    public String toString()
    {
        return "RedisConfig [mComponentType=" + mComponentType + ", mRedisMapId=" + mRedisMapId + ", mRedisPoolIndex=" + mRedisPoolIndex + ", mRedisId=" + mRedisId + ", mIP=" + mIP + ", mPort="
                + mPort + ", mDatabase=" + mDatabase + ", mReadTimeoutInsec=" + mReadTimeoutInsec + ", mConnectionTimeoutInsec=" + mConnectionTimeoutInsec + ", mMaxWaitTimeInsec=" + mMaxWaitTimeInsec
                + ", mMaxPoolSize=" + mMaxPoolSize + ", mDebugEnabled=" + mDebugEnabled + ", mMaxIdle=" + mMaxIdle + ", mMinIdle=" + mMinIdle + "]";
    }

}