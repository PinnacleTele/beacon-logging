package com.itextos.beacon.platform.dnpayloadutil.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.dnpayloadutil.PayloadKey;
import com.itextos.beacon.platform.dnpayloadutil.PayloadProcessor;
import com.itextos.beacon.platform.dnpayloadutil.PayloadUpdateTask;

public class PayloadInsertInDB
{

    private static final Log    log                          = LogFactory.getLog(PayloadInsertInDB.class);

    private static final String SQL_SELECT_PAYLOAD           = "select payload from dn_payload_map where message_id=? and retry_attempt=? and processed=0";
    private static final String SQL_INSERT_PAYLOAD           = "insert into dn_payload_map (message_id,retry_attempt,stime,payload,cluster,payload_expiry) values(?,?,?,?,?,?)";
    private static final String SQL_UPDATE_PAYLOAD           = "update dn_payload_map set processed=1 where message_id=? and retry_attempt=?";
    private static final String SQL_DELETE_PAYLOAD           = "delete from dn_payload_map where processed=1";
    private static final String SQL_DELETE_PAYLOAD_SPECIFIC  = "delete from dn_payload_map where message_id=? and retry_attempt=?";
    private static final String SQL_CUSTOMER_EXPIRE_DURATION = "select * from acc_ignore_from_dn_generation where is_active=1";

    private PayloadInsertInDB()
    {}

    public static String storePayload(
            String aMessageId,
            int aRetryAttempt,
            long aSTime,
            String aCluster,
            String aPayload,
            long aExpiryTime)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("query::" + SQL_INSERT_PAYLOAD);

        Connection        lConn  = null;
        PreparedStatement lPstmt = null;

        try
        {
            lConn = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.PAYLOAD.getKey()));
            lConn.setAutoCommit(false);

            lPstmt = lConn.prepareStatement(SQL_INSERT_PAYLOAD);
            lPstmt.setString(1, aMessageId);
            lPstmt.setString(2, String.valueOf(aRetryAttempt));
            lPstmt.setTimestamp(3, new Timestamp(aSTime));
            lPstmt.setString(4, aPayload);
            lPstmt.setString(5, aCluster.trim().toLowerCase());
            lPstmt.setTimestamp(6, new Timestamp(aExpiryTime));

            lPstmt.execute();
            lConn.commit();

            return PayloadProcessor.REDIS_INDEX_REFER_DB;
        }
        catch (final Exception exp)
        {
            log.error("Problem inserting payload to mysql...", exp);
            CommonUtility.rollbackConnection(lConn);

            throw exp;
        }
        finally
        {
            CommonUtility.closeStatement(lPstmt);
            CommonUtility.closeConnection(lConn);
        }
    }

    public static void updatePayload(
            List<PayloadKey> aPayloadProcessedList)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("update sql::" + SQL_UPDATE_PAYLOAD);

        Connection        lCon   = null;
        PreparedStatement lPstmt = null;

        try
        {
            lCon = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.PAYLOAD.getKey()));
            lCon.setAutoCommit(false);

            lPstmt = lCon.prepareStatement(SQL_UPDATE_PAYLOAD);

            for (final PayloadKey aKey : aPayloadProcessedList)
            {
                if (log.isDebugEnabled())
                    log.debug("update " + aKey);

                lPstmt.setString(1, aKey.getMid());
                lPstmt.setInt(2, aKey.getRetryAttempt());
                lPstmt.addBatch();
            }

            lPstmt.executeBatch();
            lCon.commit();
        }
        catch (final Exception exp)
        {
            log.error("problem selecting payload from mysql...", exp);
            CommonUtility.rollbackConnection(lCon);

            throw exp;
        }
        finally
        {
            CommonUtility.closeStatement(lPstmt);
            CommonUtility.closeConnection(lCon);
        }
    }

    public static String retrivePayload(
            String aMessageId,
            int aRetryAttempt)
            throws Exception
    {

        if (log.isDebugEnabled())
        {
            log.debug("select sql::" + SQL_SELECT_PAYLOAD);
            log.debug("MessageId:" + aMessageId + " rp:" + aRetryAttempt);
        }
    	Connection lCon =null;
    	PreparedStatement lPstmt = null;
    	ResultSet lResultSet = null;
 

        try
        {
        	  lCon = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.PAYLOAD.getKey()));
              lPstmt = lCon.prepareStatement(SQL_SELECT_PAYLOAD);
             
            lPstmt.setString(1, String.valueOf(aMessageId));
            lPstmt.setString(2, String.valueOf(aRetryAttempt));

            lResultSet = lPstmt.executeQuery();

            String sPayload = null;
            if (lResultSet.next())
                sPayload = lResultSet.getString("payload");

            PayloadUpdateTask.getInstance().addKey(new PayloadKey(aMessageId, aRetryAttempt));

            if (log.isDebugEnabled())
                log.debug("selected payload=" + sPayload);

            return sPayload;
        }
        catch (final Exception exp)
        {
            log.error("Problem selecting payload from mysql...", exp);
            throw exp;
        }
        finally
        {
            CommonUtility.closeResultSet(lResultSet);
            CommonUtility.closeStatement(lPstmt);
            CommonUtility.closeConnection(lCon);
        }
    }

    public static int deletePayload()
    {
        int lDeletedCount = 0;

     	Connection lCon =null;
    	PreparedStatement lPstmt = null;
     
        try
        {
        	 lCon = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.PAYLOAD.getKey()));
             lPstmt = lCon.prepareStatement(SQL_DELETE_PAYLOAD);
            
            lDeletedCount = lPstmt.executeUpdate();

            if (log.isDebugEnabled())
                log.debug("Removed records count=" + lDeletedCount);
        }
        catch (final Exception exp)
        {
            log.error("Problem selecting payload from mysql...", exp);
        }finally {
            CommonUtility.closeStatement(lPstmt);
            CommonUtility.closeConnection(lCon);
   
        }

        return lDeletedCount;
    }

    public static int deletePayload(
            String aMessageId,
            int aRetryAttempt)
    {
        int lDeletedCount = 0;
      	Connection lSqlCon =null;
    	PreparedStatement lPstmt = null;
   
        try
        {
        	lSqlCon = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.PAYLOAD.getKey()));
            lPstmt = lSqlCon.prepareStatement(SQL_DELETE_PAYLOAD_SPECIFIC);
            lPstmt.setString(1, aMessageId);
            lPstmt.setInt(2, aRetryAttempt);

            lDeletedCount = lPstmt.executeUpdate();

            if (log.isDebugEnabled())
                log.debug("Removed records count=" + lDeletedCount);
        }
        catch (final Exception exp)
        {
            log.error("Problem selecting payload from mysql...", exp);
        }finally {
            CommonUtility.closeStatement(lPstmt);
            CommonUtility.closeConnection(lSqlCon);
   
        }

        return lDeletedCount;
    }

    public static Map<String, String> getPayloadUserConfig()
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("select sql::" + SQL_CUSTOMER_EXPIRE_DURATION);

        final Map<String, String> lResult = new ConcurrentHashMap<>();

      	Connection lSqlCon =null;
    	PreparedStatement lPstmt = null;
    	ResultSet lResultSet = null;
   
        try 
        {

             lSqlCon = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.PAYLOAD.getKey()));
             lPstmt = lSqlCon.prepareStatement(SQL_CUSTOMER_EXPIRE_DURATION);
             lResultSet = lPstmt.executeQuery();
            while (lResultSet.next())
                lResult.put(lResultSet.getString("esmeaddr"), lResultSet.getString("payload_expiry_in_hr"));
        }
        catch (final Exception exp)
        {
            log.error("problem selecting payload from mysql...", exp);
            throw exp;
        }finally {
        	  CommonUtility.closeResultSet(lResultSet);
        	  CommonUtility.closeStatement(lPstmt);
              CommonUtility.closeConnection(lSqlCon);
        }

        if (log.isDebugEnabled())
            log.debug("selected payload expiry successfuly ==>" + lResult);
        return lResult;
    }

}
