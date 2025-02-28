package com.itextos.beacon.http.interfacefallback.dao;

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
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class FallBackDaoRCS
{

    private static final Log    log        = LogFactory.getLog(FallBackDaoRCS.class);

    private static final String INSERT_SQL = "insert into interface_fallback_rcs (messageid, priority, input_req) values (?,?,?)";
    private static final String SELECT_SQL = "select messageid, priority, input_req from interface_fallback_rcs limit 500";
    private static final String DELETE_SQL = "delete from interface_fallback_rcs where messageid=?";

    private FallBackDaoRCS()
    {}

    public static void storeFalbackData(
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

            final String s = "Excception while inserting the data into 'interface_fallback'";
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
            final MessageRequest message = (MessageRequest) lMessage;
            aPstmt.setString(1, CommonUtility.nullCheck(message.getBaseMessageId(), true));
            aPstmt.setString(2, CommonUtility.nullCheck(message.getMessagePriority().getKey(), true));
            aPstmt.setString(3, message.getJsonString());
            aPstmt.addBatch();
        }

        aPstmt.executeBatch();
    }

    public static Map<String, MessageRequest> getFallbackData()
    {
        final Map<String, MessageRequest> lMessageMap = new HashMap<>();

        try (
                Connection lSqlConn = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));
                PreparedStatement lPrepStatment = lSqlConn.prepareStatement(SELECT_SQL);
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
                    final MessageRequest lMsg = new MessageRequest(lMessageString);

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
                log.error("Exception while deleting the records from table 'interface_fallback'", e);
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
