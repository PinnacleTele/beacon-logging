package com.itextos.beacon.inmemory.optin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.RedisKeys;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemdata.redis.RedisHandler;

public class OptInChecker
{

    private static final Log log           = LogFactory.getLog(OptInChecker.class);

    private int              mRetryAttempt = 0;

    private static class SingletonHolder
    {

        static final OptInChecker INSTANCE = new OptInChecker();

    }

    public static OptInChecker getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    public String doOptInCheck(
            long aOptInId,
            String aMobileNumber)
    {
        while (mRetryAttempt < 5)
            try
            {
                final boolean isOptIn = RedisHandler.checkKeyValueExists(Component.OPT_IN_CHK, RedisKeys.OPT_IN + ":" + aOptInId, aMobileNumber);

                if (log.isDebugEnabled())
                    log.debug("Redis Optin status for optin id: '" + aOptInId + "' Mobile : '" + aMobileNumber + "' is : " + isOptIn);

                return isOptIn ? Constants.TRUE : Constants.FALSE;
            }
            catch (final Exception e)
            {
                log.error("Exception occer while doing Optin check. Going for DB Call.", e);

                try
                {
                    final boolean isOptIn = checkOptInExistFromDB(aOptInId, aMobileNumber);

                    if (log.isDebugEnabled())
                        log.debug("DB Optin status for optin id: '" + aOptInId + "' Mobile : '" + aMobileNumber + "' is : " + isOptIn);

                    return isOptIn ? Constants.TRUE : Constants.FALSE;
                }
                catch (final Exception e1)
                {
                    mRetryAttempt++;

                    log.error("DB Call also failed, Hence goiung to verify in Redis.", e);
                    doOptInCheck(aOptInId, aMobileNumber);
                }
            }

        return Constants.FAIL_STATUS;
    }

    public static boolean checkOptInExistFromDB(
            long aOptInId,
            String aMobileNumber)
            throws Exception
    {
        ResultSet    lResultSet = null;
        boolean      lDestFound = false;
        final String lSql       = "select * from optin_list where optin_id=? and dest=?";

        try (
                Connection lConnection = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.LISTING.getKey()));
                PreparedStatement lStatement = lConnection.prepareStatement(lSql);)
        {
            lStatement.setLong(1, aOptInId);
            lStatement.setString(2, aMobileNumber);

            lResultSet = lStatement.executeQuery();

            if (lResultSet.next())
                lDestFound = true;

            if (log.isDebugEnabled())
                log.debug("Optin status for optin id: '" + aOptInId + "' Mobile : '" + aMobileNumber + "' is : " + lDestFound);
        }
        catch (final Exception e)
        {
            log.error("checkOptInExistFromDB(); Not able to get optin", e);
            throw e;
        }
        finally
        {
            CommonUtility.closeResultSet(lResultSet);
        }
        return lDestFound;
    }

}