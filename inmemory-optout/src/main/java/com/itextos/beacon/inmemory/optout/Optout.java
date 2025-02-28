package com.itextos.beacon.inmemory.optout;

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

public class Optout
{

    public static final Log log = LogFactory.getLog(Optout.class);

    private Optout()
    {}

    public static boolean isOptOut(
            String aOptoutId,
            String aMobileNumber)
    {
        boolean    returnValue = false;
        final long lMNumber    = CommonUtility.getLong(aMobileNumber, -1);

        if (lMNumber == -1)
        {
            log.error("Invalid mobile number specified. Optout id '" + aOptoutId + "' mobilenumber '" + aMobileNumber + "'");
            return returnValue;
        }

        final int lRedisIndex = (int) (lMNumber % RedisConnectionProvider.getInstance().getRedisPoolCount(Component.PRI_OPT_OUT_CHK));

        boolean   isDone      = false;

        while (!isDone)
            try
            {
                returnValue = isOptoutNumber(lRedisIndex, aOptoutId, aMobileNumber);
                isDone      = true;
            }
            catch (final Exception exp)
            {
                log.error("Problem getting Optout. Will retry after 1 second", exp);
                CommonUtility.sleepForAWhile(1000);
            }
        return returnValue;
    }

    public static boolean isOptoutNumber(
            int aRedisIndex,
            String aOptoutId,
            String aMobileNumber)
            throws Exception
    {
        boolean returnValue = false;

        try
        {
            returnValue = isOptoutExistsInRedis(Component.PRI_OPT_OUT_CHK, aRedisIndex, aOptoutId, aMobileNumber);
        }
        catch (final Exception e)
        {

            try
            {
                returnValue = isOptoutExistsInRedis(Component.SEC_OPT_OUT_CHK, aRedisIndex, aOptoutId, aMobileNumber);
            }
            catch (final Exception e1)
            {
                returnValue = isOptoutInfoExistsInDB(aOptoutId, aMobileNumber);
            }
        }
        return returnValue;
    }

    private static boolean isOptoutExistsInRedis(
            Component aRedisType,
            int aRedisIndex,
            String aOptoutId,
            String aMobileNumber)
    {

        try (
                Jedis lJedisConn = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, aRedisType, aRedisIndex);)
        {
            return lJedisConn.hexists(aOptoutId, aMobileNumber);
        }
        catch (final Exception e)
        {
            log.error("Excception while getting optin information from redis for the Optout id '" + aOptoutId + "' mobilenumber '" + aMobileNumber + "' in Redis Type '" + aRedisType + "'", e);
            throw e;
        }
    }

    public static boolean isOptoutInfoExistsInDB(
            String aOptoutId,
            String aMobileNumber)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("isOptoutInfoExistsInDB() started");

        ResultSet    lResultSet = null;
        final String lSql       = "SELECT * FROM optout_list where optout_id=? and mnumber=?";

        try (
                Connection lConnection = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.LISTING.getKey()));
                PreparedStatement lStatement = lConnection.prepareStatement(lSql, ResultSet.CONCUR_READ_ONLY, ResultSet.FETCH_FORWARD);)
        {
            lStatement.setString(1, aOptoutId);
            lStatement.setString(2, aMobileNumber);

            lResultSet = lStatement.executeQuery();

            if (lResultSet.next())
            {
                if (log.isDebugEnabled())
                    log.debug("optout_id:" + aOptoutId + " mnumber:" + aMobileNumber + " found in DB");
                return true;
            }
        }
        catch (final Exception e)
        {
            log.error("Not able to load Optout...", e);
            throw e;
        }
        finally
        {
            CommonUtility.closeResultSet(lResultSet);
        }
        return false;
    }

}