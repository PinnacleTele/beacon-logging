package com.itextos.beacon.platform.dlrpayloadgen.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

public class UpdateGeneratedDlrCount
{

    private static final Log log          = LogFactory.getLog(UpdateGeneratedDlrCount.class);

    private static String    UPDATE_QUERY = "update dlr_sp_log set count=count+? where route_id=? and record_date=?";
    private static String    INSERT_QUERY = "insert into dlr_sp_log (route_id,record_date,count) values (?,?,?)";

    private UpdateGeneratedDlrCount()
    {}

    public static synchronized void updateGeneratedDlrCount(
            Map<String, Integer> lDlrMap,
            String aRedisKey)
    {
        if (log.isDebugEnabled())
            log.debug("DlrMap:" + lDlrMap + " redisKey:" + aRedisKey);

        Connection        lSqlConn      = null;
        PreparedStatement lUpdatePstmt  = null;
        PreparedStatement lInsertPstmt  = null;
        int               lUpdateResult = 0;

        try
        {
            final Date             lTempDate = DateTimeUtility.getDateFromString(aRedisKey.split(":")[1], DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH);
            final java.sql.Date    sqlDate   = new java.sql.Date(lTempDate.getTime());

            final Iterator<String> iterator  = lDlrMap.keySet().iterator();

            while (iterator.hasNext())
            {
                final String  lRouteId = iterator.next();
                final Integer lCount   = lDlrMap.get(lRouteId);
                if (log.isDebugEnabled())
                    log.debug("Route Id:" + lRouteId + " count:" + lCount + " redisKey:" + aRedisKey);

                boolean toStop = true;
                while (toStop)
                    try
                    {
                        lSqlConn     = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.LOGGING.getKey()));

                        lUpdatePstmt = lSqlConn.prepareStatement(UPDATE_QUERY);
                        lUpdatePstmt.setLong(1, lCount);
                        lUpdatePstmt.setString(2, lRouteId.toUpperCase());
                        lUpdatePstmt.setDate(3, sqlDate);

                        lUpdateResult = lUpdatePstmt.executeUpdate();
                        if (log.isDebugEnabled())
                            log.debug("updateGeneratedDNCount() route:" + lRouteId + " count:" + lCount + " redisKey:" + aRedisKey + " updateResult:" + lUpdateResult);

                        if (lUpdateResult == 0)
                        {
                            lInsertPstmt = lSqlConn.prepareStatement(INSERT_QUERY);
                            lInsertPstmt.setString(1, lRouteId.toUpperCase());
                            lInsertPstmt.setDate(2, sqlDate);
                            lInsertPstmt.setLong(3, lCount);
                            lUpdateResult = lInsertPstmt.executeUpdate();
                            if (log.isDebugEnabled())
                                log.debug("updateGeneratedDNCount() route:" + lRouteId + " count:" + lCount + " redisKey:" + aRedisKey + " insertResult:" + lUpdateResult);
                        }
                        toStop = false;
                    }
                    catch (final SQLException sqlEx)
                    {
                        log.error("Exception", sqlEx);
                        Thread.sleep(5000);
                    }
                    catch (final Exception e)
                    {
                        log.error("Exception", e);
                        toStop = false;
                    }
                    finally
                    {
                        CommonUtility.closeStatement(lUpdatePstmt);
                        CommonUtility.closeStatement(lInsertPstmt);
                        CommonUtility.closeConnection(lSqlConn);
                    }
            }
        }
        catch (final Exception e)
        {
            log.error("Exception", e);
        }
    }

}
