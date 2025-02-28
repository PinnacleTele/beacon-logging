package com.itextos.beacon.platform.sbpcore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public abstract class DBPoller
{

    private static final Log    log                 = LogFactory.getLog(DBPoller.class);

    public static final String  TABLE_NAME_SCHEDULE = "schedule_data";
    public static final String  TABLE_NAME_BLOCKOUT = "blockout_data";

    private static final String MOD_VALUE           = System.getProperty("modvalue")==null?System.getenv("modvalue"):System.getProperty("modvalue"); // valid values are 0,1,2,3

    private static final String SQL_SELECT          = "select * from {0} where  date_time_to_process < now() and instance_id = ? and mod(sno,4) in (" + MOD_VALUE + ") limit 50";
    private static final String SQL_DELETE          = "delete from {0} where  sno=?";
    private static final String SQL_SELECT_INSTANCE = "select  DISTINCT 'schedule' as type, instance_id from messaging.schedule_data sd where date_time_to_process < now() union all select  DISTINCT 'blockout' as type, instance_id from messaging.blockout_data bd where date_time_to_process < now()";

    private DBPoller()
    {
        if ((MOD_VALUE == null) || MOD_VALUE.isBlank()) {
          //  throw new ItextosRuntimeException("Invalid Modvalue set in Runtime...'" + MOD_VALUE + "'");
        
        	log.error("Invalid Modvalue set in Runtime... System going to down'" + MOD_VALUE + "'");
        	
        	System.exit(-1);
        }

        {
            if (log.isDebugEnabled())
                log.debug("Modvalues passed '" + MOD_VALUE + "'");

            final String[] mods = MOD_VALUE.split(",");

            for (final String s : mods)
            {
                final int mod = CommonUtility.getInteger(s, -999);
                if ((mod == -999) || (mod >= 4)) {
                    //throw new ItextosRuntimeException("Invalid Modvalue set in Runtime...'" + MOD_VALUE + "'");
                    log.error("Invalid Modvalue set in Runtime... system going down'" + MOD_VALUE + "'");
                    System.exit(-1);
                }
            }
        }
    }

    public static Map<Long, MessageRequest> getRecordsFromTable(
            int aAppInstanceId,
            String aTableName)
    {
    	
    	Connection        con        = null;
        PreparedStatement pstmt      = null;
        final Map<Long, MessageRequest> returnValue = new HashMap<>();
        final String                    sql         = MessageFormat.format(SQL_SELECT, aTableName);
        ResultSet                       rs          = null;

        JndiInfoHolder.getInstance();

        try 
        {
        	con = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));
            pstmt = con.prepareStatement(sql);
        	log.debug("sql : "+sql);
            pstmt.setInt(1, aAppInstanceId);
            rs = pstmt.executeQuery();

            while (rs.next())
            {
                final long   seqNo      = rs.getLong("sno");
                final String jsOnString = rs.getString("message_payload");
                returnValue.put(seqNo, new MessageRequest(jsOnString));
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while reading data for instance id '" + aAppInstanceId + "' in  table '" + aTableName + "'", e);
        }
        finally
        {
            CommonUtility.closeResultSet(rs);
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
      
        }
        return returnValue;
    }

    public static void deleteRecordsFromTable(
            String aTableName,
            List<Long> aSeqNoList)
    {
        Connection        con        = null;
        PreparedStatement pstmt      = null;
        boolean           isDone     = false;
        int               retryCount = 0;

        while (!isDone)
            try
            {
                final String sql = MessageFormat.format(SQL_DELETE, aTableName);
                JndiInfoHolder.getInstance();
                con   = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));
                pstmt = con.prepareStatement(sql);
                con.setAutoCommit(false);

                for (final Long l : aSeqNoList)
                {
                    pstmt.setLong(1, l);
                    pstmt.addBatch();
                }

                pstmt.executeBatch();
                con.commit();
                isDone = true;
            }
            catch (final Exception e)
            {
                retryCount++;

                try
                {
                    if (con != null)
                        con.rollback();
                }
                catch (final SQLException e1)
                {
                    //
                }
                log.error("Exception while deleting the records from table '" + aTableName + "'", e);
                log.error("Retry count " + retryCount + ". Will try after a second.");
                CommonUtility.sleepForAWhile(1000);
            }
            finally
            {
                CommonUtility.closeStatement(pstmt);
                CommonUtility.closeConnection(con);
            }
    }

    
    public static void deleteRecordsFromTable(
            String aTableName,
            Long aSeqNo)
    {
        Connection        con        = null;
        PreparedStatement pstmt      = null;
        boolean           isDone     = false;
        int               retryCount = 0;

        while (!isDone)
            try
            {
                final String sql = MessageFormat.format(SQL_DELETE, aTableName);
                JndiInfoHolder.getInstance();
                con   = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));
                pstmt = con.prepareStatement(sql);
                pstmt.setLong(1, aSeqNo);
                pstmt.execute();
                isDone = true;
            }
            catch (final Exception e)
            {
                retryCount++;

                try
                {
                    if (con != null)
                        con.rollback();
                }
                catch (final SQLException e1)
                {
                    //
                }
                log.error("Exception while deleting the records from table '" + aTableName + "'", e);
                log.error("Retry count " + retryCount + ". Will try after a second.");
                CommonUtility.sleepForAWhile(1000);
            }
            finally
            {
                CommonUtility.closeStatement(pstmt);
                CommonUtility.closeConnection(con);
            }
    }

    public static Map<String, List<String>> getInstanceIds()
    {
        ResultSet                       rs              = null;

        final Map<String, List<String>> lSBInstanceInfo = new HashMap<>();

        JndiInfoHolder.getInstance();

        try (
                final Connection con = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));
                final PreparedStatement pstmt = con.prepareStatement(SQL_SELECT_INSTANCE);)
        {
            rs = pstmt.executeQuery();

            while (rs.next())
            {
                final String       lType        = rs.getString("type");
                final String       lInstanceId  = rs.getString("instance_id");

                final List<String> lInstanceLst = lSBInstanceInfo.computeIfAbsent(lType, k -> new ArrayList<>());
                lInstanceLst.add(lInstanceId);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while reading data for instance ids", e);
        }
        finally
        {
            CommonUtility.closeResultSet(rs);
        }
        return lSBInstanceInfo;
    }

    public static Map<String, List<String>> getAllInstances()
    {
        final String lInstanceIds = CommonUtility.nullCheck(System.getProperty("instance.ids"), true);

        if (!lInstanceIds.isEmpty())
            return (Map<String, List<String>>) new HashMap<>().put("all", Arrays.asList(lInstanceIds.split(",")));

        return null;
    }

}