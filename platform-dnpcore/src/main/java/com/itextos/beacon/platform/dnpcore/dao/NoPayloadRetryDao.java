package com.itextos.beacon.platform.dnpcore.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class NoPayloadRetryDao
{

    private static final Log    log        = LogFactory.getLog(NoPayloadRetryDao.class);

    private static final String MOD_VALUE  = System.getProperty("modvalue"); // valid values are 0,1,2,3

    private static final String INSERT_SQL = "insert into no_payload_dn_retry (payload_rid, expiry_count, cluster, dn_payload) values (?,?,?,?)";
    private static final String SELECT_SQL = "select sno, payload_rid, expiry_count, cluster, dn_payload from no_payload_dn_retry where cluster={0} and mod(sno,4) in (" + MOD_VALUE
            + ") ORDER by expiry_count DESC limit 500";
    private static final String UPDATE_SQL = "update no_payload_dn_retry set expiry_count=? where sno=?";
    private static final String DELETE_SQL = "delete from no_payload_dn_retry where sno=?";

    private NoPayloadRetryDao()
    {
        if ((MOD_VALUE == null) || MOD_VALUE.isBlank()) {
        	
            //throw new ItextosRuntimeException("Invalid Modvalue set in Runtime...'" + MOD_VALUE + "'");
        	log.error("Invalid Modvalue set in Runtime...'" + MOD_VALUE + "'");
        }

        {
            if (log.isDebugEnabled())
                log.debug("Modvalues passed '" + MOD_VALUE + "'");

            final String[] mods = MOD_VALUE.split(",");

            for (final String s : mods)
            {
                final int mod = CommonUtility.getInteger(s, -999);
                if ((mod == -999) || (mod >= 4)) {
                  //  throw new ItextosRuntimeException("Invalid Modvalue set in Runtime...'" + MOD_VALUE + "'");
                    log.error("Invalid Modvalue set in Runtime...'" + MOD_VALUE + "'");
                }
            }
        }
    }

    public static void storeNoPayloadRetryData(
            List<IMessage> aRecords)
            throws ItextosException
    {
        Connection        lSqlConn      = null;
        PreparedStatement lPreepareStmt = null;

        try
        {
            lSqlConn      = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));
            lPreepareStmt = lSqlConn.prepareStatement(INSERT_SQL);

            lSqlConn.setAutoCommit(false);
            insertRecords(lPreepareStmt, aRecords);
            lSqlConn.commit();
        }
        catch (final Exception e)
        {
            CommonUtility.rollbackConnection(lSqlConn);

            final String s = "Excception while inserting the data into 'no_payload_dn_retry'";
            log.error(s, e);
            throw new ItextosException(s, e);
        }
        finally
        {
            CommonUtility.closeStatement(lPreepareStmt);
            CommonUtility.closeConnection(lSqlConn);
        }
    }

    private static void insertRecords(
            PreparedStatement aPstmt,
            List<IMessage> aIMessageList)
            throws SQLException
    {

        for (final IMessage lMessage : aIMessageList)
        {
            final DeliveryObject message = (DeliveryObject) lMessage;
            aPstmt.setString(1, CommonUtility.nullCheck(message.getPayloadRedisId(), true));
            aPstmt.setInt(2, CommonUtility.getInteger(CommonUtility.nullCheck(message.getValue(MiddlewareConstant.MW_NO_PAYLOD_RETRY_EXPIRY_COUNT), true)));
            aPstmt.setString(3, CommonUtility.nullCheck(message.getClusterType().getKey(), true));
            aPstmt.setString(4, message.getJsonString());
            aPstmt.addBatch();
        }

        aPstmt.executeBatch();
    }

    public static void updateNoPayloadRetryData(
            List<IMessage> aRecords)
            throws ItextosException
    {
        Connection        lSqlConn      = null;
        PreparedStatement lPreepareStmt = null;

        try
        {
            lSqlConn      = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));
            lPreepareStmt = lSqlConn.prepareStatement(UPDATE_SQL);

            lSqlConn.setAutoCommit(false);
            updateRecords(lPreepareStmt, aRecords);
            lSqlConn.commit();
        }
        catch (final Exception e)
        {
            CommonUtility.rollbackConnection(lSqlConn);

            final String s = "Excception while inserting the data into 'no_payload_dn_retry'";
            log.error(s, e);
            throw new ItextosException(s, e);
        }
        finally
        {
            CommonUtility.closeStatement(lPreepareStmt);
            CommonUtility.closeConnection(lSqlConn);
        }
    }

    private static void updateRecords(
            PreparedStatement aPstmt,
            List<IMessage> aIMessageList)
            throws SQLException
    {

        for (final IMessage lMessage : aIMessageList)
        {
            final DeliveryObject message          = (DeliveryObject) lMessage;

            final int            lPayloadExpCount = CommonUtility.getInteger(message.getValue(MiddlewareConstant.MW_NO_PAYLOD_RETRY_EXPIRY_COUNT));
            if (log.isDebugEnabled())
                log.debug("Payload Expiry Count : " + lPayloadExpCount + ", Sno:'" + CommonUtility.nullCheck(message.getValue(MiddlewareConstant.MW_SNO), true) + "'");

            aPstmt.setInt(1, lPayloadExpCount);
            aPstmt.setInt(2, CommonUtility.getInteger(CommonUtility.nullCheck(message.getValue(MiddlewareConstant.MW_SNO), true)));
            aPstmt.addBatch();
        }

        aPstmt.executeBatch();
    }

    public static Map<String, DeliveryObject> getNoPayloadData(
            String aCluster)
    {
        final Map<String, DeliveryObject> lMessageMap = new HashMap<>();

        try (
                Connection lSqlConn = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));
                PreparedStatement lPrepStatment = lSqlConn.prepareStatement(MessageFormat.format(SELECT_SQL, "'" + aCluster + "'"));
                ResultSet lResultSet = lPrepStatment.executeQuery();)
        {

            while (lResultSet.next())
            {
                String lMessageString = null;
                String lSno           = null;

                try
                {
                    lMessageString = lResultSet.getString("dn_payload");
                    lSno           = lResultSet.getString("sno");
                    final int            lMaxExpiryCount = lResultSet.getInt("expiry_count");
                    final DeliveryObject lMsg            = new DeliveryObject(lMessageString);

                    lMsg.putValue(MiddlewareConstant.MW_NO_PAYLOD_RETRY_EXPIRY_COUNT, String.valueOf(lMaxExpiryCount));
                    lMsg.putValue(MiddlewareConstant.MW_SNO, lSno);

                    lMessageMap.put(lSno, lMsg);
                }
                catch (final Exception e)
                {
                    log.error("Error while converting message request Sno : '" + lSno + "' , Json String [" + lMessageString + "]");
                }
            }
        }
        catch (final Exception e)
        {
            log.error("getNoPayloadData() - err -", e);
        }
        return lMessageMap;
    }

    public static void deleteRecords(
            List<String> aMessageIdList)
    {
        Connection        lSqlCon     = null;
        PreparedStatement lPStmt      = null;
        boolean           isDone      = false;
        int               lRetryCount = 0;

        while (!isDone)
            try
            {
                lSqlCon = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));
                lPStmt  = lSqlCon.prepareStatement(DELETE_SQL);
                lSqlCon.setAutoCommit(false);

                for (final String messageId : aMessageIdList)
                {
                    lPStmt.setString(1, messageId);
                    lPStmt.addBatch();
                }

                lPStmt.executeBatch();
                lSqlCon.commit();
                isDone = true;
            }
            catch (final Exception e)
            {
                lRetryCount++;

                try
                {
                    if (lSqlCon != null)
                        lSqlCon.rollback();
                }
                catch (final SQLException e1)
                {
                    //
                }
                log.error("Exception while deleting the records from table 'no_payload_dn_retry'", e);
                log.error("Retry count " + lRetryCount + ". Will try after a second.");
                CommonUtility.sleepForAWhile(100);
            }
            finally
            {
                CommonUtility.closeStatement(lPStmt);
                CommonUtility.closeConnection(lSqlCon);
            }
    }

}
