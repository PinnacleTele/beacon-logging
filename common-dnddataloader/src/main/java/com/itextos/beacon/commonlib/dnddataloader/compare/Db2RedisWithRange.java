package com.itextos.beacon.commonlib.dnddataloader.compare;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.dnddataloader.common.CountHolder;
import com.itextos.beacon.commonlib.dnddataloader.db.Db2RedisThreadBased;
import com.itextos.beacon.commonlib.dnddataloader.redis.RedisOperations;
import com.itextos.beacon.commonlib.dnddataloader.util.DndPropertyProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;

class Db2RedisWithRange
        implements
        Runnable
{

    private static final Log      log                    = LogFactory.getLog(Db2RedisWithRange.class);

    protected static final String SQL_TO_SELECT          = "SELECT dest, preferences FROM " + DndPropertyProvider.getDnDDataTableName()
            + " WHERE (dest BETWEEN ? AND ?) and convert(SUBSTR(dest, 3, LENGTH(dest)), int) MOD " + RedisOperations.getDndMasterRedisCount() + " = ? ORDER BY 1";

    private final long            startNumber;
    private final long            endNumber;

    private boolean               hasAllThreadsCompleted = false;

    Db2RedisWithRange(
            long aStartNumber,
            long aEndNumber)
    {
        startNumber = aStartNumber;
        endNumber   = aEndNumber;
    }

    @Override
    public void run()
    {
        if (log.isDebugEnabled())
            log.debug("Started processing records for " + startNumber + " and " + endNumber);

        try (
                final Connection con = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfo(Db2RedisThreadBased.DND_JNDI_INFO));
                final PreparedStatement pstmtSelect = con.prepareStatement(SQL_TO_SELECT, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);)
        {
            pstmtSelect.setFetchSize(5000);

            for (int redisIndex = 0; redisIndex < RedisOperations.getDndMasterRedisCount(); redisIndex++)
            {
                ResultSet resultSet = null;

                try
                {
                    if (log.isDebugEnabled())
                        log.debug("SQL Parameters : ' - Start Number : '" + startNumber + "', End Number : '" + endNumber + "' Mod : '" + redisIndex + "'");

                    pstmtSelect.setLong(1, startNumber);
                    pstmtSelect.setLong(2, endNumber);
                    pstmtSelect.setInt(3, redisIndex);

                    resultSet = pstmtSelect.executeQuery();

                    final Map<String, List<String>> dbRecords = new HashMap<>();
                    final String                    range     = "[" + startNumber + "-" + endNumber + "]";

                    while (resultSet.next())
                    {
                        final String dest        = resultSet.getString(1);
                        final String preferences = resultSet.getString(2);

                        if (!validate(dest))
                        {
                            CountHolder.getInstance().incrementInvalidMobileNumberRequest();
                            continue;
                        }

                        final String       outerKey = dest.substring(2, 7);
                        final String       innerKey = dest.substring(7);
                        final List<String> list     = dbRecords.computeIfAbsent(outerKey, k -> new ArrayList<>());
                        list.add(innerKey + "~" + preferences);
                    }

                    final DndCompareObject lDndCompareObject = new DndCompareObject(redisIndex, range, dbRecords);
                    lDndCompareObject.process();
                }
                finally
                {
                    if (resultSet != null)
                        resultSet.close();
                }
            }

            hasAllThreadsCompleted = true;
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            log.error("Exception while getting records from DB", e);
        }
    }

    private static boolean validate(
            String aDest)
    {

        try
        {
            final String temp = CommonUtility.nullCheck(aDest, true);
            final int    len  = temp.length();

            switch (len)
            {
                case 10:
                    return true;

                case 12:
                    if (temp.startsWith("91"))
                        return true;
                    return false;

                default:
                    return false;
            }
        }
        catch (final Exception e)
        {
            return false;
        }
    }

    boolean isCompleted()
    {
        return hasAllThreadsCompleted;
    }

    @Override
    public String toString()
    {
        return "[" + startNumber + "-" + endNumber + "]";
    }

}