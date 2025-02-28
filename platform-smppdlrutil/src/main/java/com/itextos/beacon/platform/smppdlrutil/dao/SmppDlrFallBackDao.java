package com.itextos.beacon.platform.smppdlrutil.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class SmppDlrFallBackDao
{

    private static final Log    log        = LogFactory.getLog(SmppDlrFallBackDao.class);

    private static final String MOD_VALUE  = System.getProperty("modvalue"); // valid values are 0,1,2,3
    private static final String SQL_INSERT = "insert into smpp_dlr_fallback (cli_id, msg_id, cluster, paylod_dlr) values (?,?,?,?)";
    private static final String SQL_SELECT = "select * from smpp_dlr_fallback where mod(sno,4) in (" + MOD_VALUE + ") limit 500";
    private static final String SQL_DELETE = "delete from smpp_dlr_fallback where  sno=?";

    private SmppDlrFallBackDao()
    {
        if ((MOD_VALUE == null) || MOD_VALUE.isBlank()) {
            //throw new ItextosRuntimeException("Invalid Modvalue set in Runtime...'" + MOD_VALUE + "'");
        	log.error("Invalid Modvalue set in Runtime...' System going to down" + MOD_VALUE + "'");
        	
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
                 //   throw new ItextosRuntimeException("Invalid Modvalue set in Runtime...'" + MOD_VALUE + "'");
                	log.error("Invalid Modvalue set in Runtime...' System going to down" + MOD_VALUE + "'");
                	
                	System.exit(-1);
                }
            }
        }
    }

    public static void insertRecords(
            List<DeliveryObject> aDeliveryObjectList)
            throws ItextosException
    {
        Connection        lSqlCon = null;
        PreparedStatement lPstmt  = null;

        if (log.isDebugEnabled())
            log.debug("insertRecords() - Record inserting table name : 'smpp_dlr_fallback'");

        try
        {
            lSqlCon = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));
            lPstmt  = lSqlCon.prepareStatement(SQL_INSERT);

            lSqlCon.setAutoCommit(false);
            insertRecords(lPstmt, aDeliveryObjectList);
            lSqlCon.commit();
        }
        catch (final Exception e)
        {

            try
            {
                if (lSqlCon != null)
                    lSqlCon.rollback();
            }
            catch (final SQLException e1)
            {
                // Ignore
            }
            final String s = "Excception while inserting the data into 'smpp_dlr_fallback'";
            log.error(s, e);
            throw new ItextosException(s, e);
        }
        finally
        {
            CommonUtility.closeStatement(lPstmt);
            CommonUtility.closeConnection(lSqlCon);
        }
    }

    private static void insertRecords(
            PreparedStatement aPstmt,
            List<DeliveryObject> aDeliveryObjectList)
            throws SQLException
    {

        for (final DeliveryObject lDeMessageRequest : aDeliveryObjectList)
        {
            aPstmt.setString(1, lDeMessageRequest.getClientId());
            aPstmt.setString(2, lDeMessageRequest.getMessageId());
            aPstmt.setString(3, lDeMessageRequest.getClusterType().getKey());
            aPstmt.setString(4, lDeMessageRequest.getJsonString());
            aPstmt.addBatch();
        }

        aPstmt.executeBatch();
    }

    public static Map<Long, DeliveryObject> getRecordsFromTable()
    {
        final Map<Long, DeliveryObject> returnValue = new HashMap<>();
        ResultSet                       rs          = null;

        try (
                final Connection con = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));
                final PreparedStatement pstmt = con.prepareStatement(SQL_SELECT);)
        {
            rs = pstmt.executeQuery();

            while (rs.next())
            {
                final long   seqNo      = rs.getLong("sno");
                final String jsOnString = rs.getString("paylod_dlr");
                returnValue.put(seqNo, new DeliveryObject(jsOnString));
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while reading data from  table 'smpp_dlr_fallback'", e);
        }
        finally
        {
            CommonUtility.closeResultSet(rs);
        }
        return returnValue;
    }

    public static void deleteRecordsFromTable(
            List<Long> aSeqNoList)
    {
        Connection        con        = null;
        PreparedStatement pstmt      = null;
        boolean           isDone     = false;
        int               retryCount = 0;

        while (!isDone)
            try
            {
                con   = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));
                pstmt = con.prepareStatement(SQL_DELETE);
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
                log.error("Exception while deleting the records from table 'smpp_dlr_fallback'", e);
                log.error("Retry count " + retryCount + ". Will try after a second.");
                CommonUtility.sleepForAWhile(100);
            }
            finally
            {
                CommonUtility.closeStatement(pstmt);
                CommonUtility.closeConnection(con);
            }
    }

}
