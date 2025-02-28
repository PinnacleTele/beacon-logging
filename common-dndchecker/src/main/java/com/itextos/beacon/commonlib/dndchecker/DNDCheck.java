package com.itextos.beacon.commonlib.dndchecker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;

import redis.clients.jedis.Jedis;

public abstract class DNDCheck
{

    private static final Log    log        = LogFactory.getLog(DNDCheck.class);
    private static final String DND_PREFIX = "dnd:";

    private DNDCheck()
    {}

    public static String getDNDInfo(
            String aMobileNumber)
    {
        String lResult      = null;

        String lTempMNumber = aMobileNumber;
        if (lTempMNumber.length() == 12)
            lTempMNumber = lTempMNumber.substring(2);

        final long lMNumber = CommonUtility.getLong(lTempMNumber, -1);

        if (-1 == lMNumber)
        {
            log.error("Invalid mobile number specified. '" + aMobileNumber + "'");
            return lResult;
        }

        final String lHashKey  = DND_PREFIX + lTempMNumber.substring(0, 5);
        final String lRedisKey = lTempMNumber.substring(5, lTempMNumber.length());

        if (log.isDebugEnabled())
            log.debug("Checking DND value for mobile number : '" + aMobileNumber + "'");

        boolean isDone = false;

        while (!isDone)
            try
            {
                lResult = getDndValue(lHashKey, lRedisKey, lMNumber);
                isDone  = true;
            }
            catch (final Exception exp)
            {
                log.error("Problem checking dnd. Will retry after 1 second", exp);
                CommonUtility.sleepForAWhile(1000);
            }
        return lResult;
    }

    private static String getDndValue(
            String aHashKey,
            String aKey,
            long aMobileNumber)
            throws Exception
    {
        String    lDndVal       = null;

        final int lRedisPoolCnt = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.PRI_DND_CHK);

        final int lRedisIndex   = (int) (aMobileNumber % lRedisPoolCnt);

        if (log.isDebugEnabled())
            log.debug("Looking for hashkey=" + aHashKey + " key=" + aKey + " Mobile Number=" + aMobileNumber);

        try
        {
            lDndVal = checkInRedis(Component.PRI_DND_CHK, lRedisIndex, aHashKey, aKey);
        }
        catch (final Exception e)
        {

            try
            {
                lDndVal = checkInRedis(Component.SEC_DND_CHK, lRedisIndex, aHashKey, aKey);
            }
            catch (final Exception e1)
            {
                lDndVal = getInfoFromDB(aMobileNumber);
            }
        }

        if (log.isDebugEnabled())
            log.debug("Preferences for dest " + aMobileNumber + " is :" + lDndVal);

        return lDndVal;
    }

    private static String checkInRedis(
            Component aRedisType,
            int aRedisIndex,
            String aHashKey,
            String aKey)
    {

        try (
                Jedis lJedis = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, aRedisType, (aRedisIndex + 1));)
        {
            return lJedis.hget(aHashKey, aKey);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the value from Redis. Redis Type " + aRedisType, e);
            throw e;
        }
    }

    private static String getInfoFromDB(
            long aMobileNumber)
            throws Exception
    {
        String       returnValue = null;
        ResultSet    lResultSet  = null;
        final String lSql        = "select preferences from dnd_data where dest = ?";

        try (
                Connection lConnection = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.LISTING.getKey()));
                PreparedStatement lPrepareStatement = lConnection.prepareStatement(lSql);)
        {
            log.error("Getting preferences from database for " + aMobileNumber);

            lPrepareStatement.setString(1, Long.toString(aMobileNumber));
            lResultSet = lPrepareStatement.executeQuery();

            if (lResultSet.next())
                returnValue = lResultSet.getString("preferences");
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the DND value from database for " + aMobileNumber, e);
            throw e;
        }
        finally
        {
            CommonUtility.closeResultSet(lResultSet);
        }

        return returnValue;
    }

}