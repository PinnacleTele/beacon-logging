package com.itextos.beacon.platform.dnrfallback.dao;

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
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class DlrFallBackDao
{

    private static final Log    log        = LogFactory.getLog(DlrFallBackDao.class);

    private static final String INSERT_SQL = "insert into dlr_receive_fallback (messageid, priority, cluster, input_req) values (?,?,?,?)";
    private static final String SELECT_SQL = "select messageid, priority, cluster, input_req from dlr_receive_fallback where cluster={0} limit 500";
    private static final String DELETE_SQL = "delete from dlr_receive_fallback where messageid=?";

    private DlrFallBackDao()
    {}

    public static void storeFalbackData(
            List<IMessage> aRecords)
            throws ItextosException
    {
        Connection        lSqlConn      = null;
        PreparedStatement lPreepareStmt = null;

        try
        {
            lSqlConn      = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.PAYLOAD.getKey()));
            lPreepareStmt = lSqlConn.prepareStatement(INSERT_SQL);

            lSqlConn.setAutoCommit(false);
            insertRecords(lPreepareStmt, aRecords);
            lSqlConn.commit();
        }
        catch (final Exception e)
        {
            CommonUtility.rollbackConnection(lSqlConn);

            final String s = "Excception while inserting the data into 'dlr_receive_fallback'";
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
            aPstmt.setString(1, CommonUtility.nullCheck(message.getMessageId(), true));
            aPstmt.setString(2, CommonUtility.nullCheck(message.getMessagePriority().getKey(), true));
            aPstmt.setString(3, CommonUtility.nullCheck(message.getClusterType().getKey(), true));
            aPstmt.setString(4, message.getJsonString());
            aPstmt.addBatch();
        }

        aPstmt.executeBatch();
    }

    public static Map<String, DeliveryObject> getFallbackData(
            String aCluster)
    {
        final Map<String, DeliveryObject> lMessageMap = new HashMap<>();

        try (
                Connection lSqlConn = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.PAYLOAD.getKey()));
                PreparedStatement lPrepStatment = lSqlConn.prepareStatement(MessageFormat.format(SELECT_SQL, "'" + aCluster + "'"));
                ResultSet lResultSet = lPrepStatment.executeQuery();)
        {

            while (lResultSet.next())
            {
                String lMessageString = null;
                String lMessageId     = null;

                try
                {
                    lMessageString = lResultSet.getString("input_req");
                    lMessageId     = lResultSet.getString("messageid");
                    final DeliveryObject lMsg = new DeliveryObject(lMessageString);

                    lMessageMap.put(lMessageId, lMsg);
                }
                catch (final Exception e)
                {
                    log.error("Error while converting message request Msgid : '" + lMessageId + "' , Json String [" + lMessageString + "]");
                }
            }
        }
        catch (final Exception e)
        {
            log.error("getFallbackData() - err -", e);
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
                lSqlCon = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.PAYLOAD.getKey()));
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
                log.error("Exception while deleting the records from table 'dlr_receive_fallback'", e);
                log.error("Retry count " + lRetryCount + ". Will try after a second.");
                CommonUtility.sleepForAWhile(1000);
            }
            finally
            {
                CommonUtility.closeStatement(lPStmt);
                CommonUtility.closeConnection(lSqlCon);
            }
    }

}
