package com.itextos.beacon.httpclienthandover.retry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.ClientHandoverObject;

public class RetryDBHelper
{

    private static final String INSERT_QUERY            = "INSERT INTO client_handover_retry_meta_data_failure (msg_id, cli_hover_message,is_customer_specific) VALUES(?,?,?)";
    private static final String SELECT_QUERY            = "select * from client_handover_retry_meta_data_failure order by created_time limit 100";
    private static final String RETRY_DATA_DELETE_QUERY = "delete from client_handover_retry_meta_data_failure  where msg_id IN ";

    private static final String QUERY                   = "Select * from client_handover_retry_master  where msg_id IN ";
    private static final String DELETE_QUERY            = "delete from client_handover_retry_master  where msg_id IN ";

    private static final Log    log                     = LogFactory.getLog(RetryDBHelper.class);

    public RetryDBHelper()
    {}

    public static List<BaseMessage> getBaseMessagesForMIDs(
            List<String> aMIDList)
    {
        final String            queryMidList = buildMidForQuery(aMIDList);
        final String            actualQuery  = QUERY + queryMidList;

        final List<BaseMessage> messageList  = new ArrayList<>();

        try (
                final Connection con = getClientHandoverConnection();
                final PreparedStatement pstmt = con.prepareStatement(actualQuery, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                final ResultSet resultSet = pstmt.executeQuery();)
        {
            while (resultSet.next())
                messageList.add(buildBaseMessage(resultSet));
        }
        catch (final Exception e)
        {
            log.error("Exception while fetching the data from retry_data DB ", e);
        }

        return messageList;
    }

    public static void insertRetryMetaDataFailure(
            List<BaseMessage> baseMessage,
            boolean isCustomerSpecific)
    {

        try (
                final Connection con = getClientHandoverConnection();
                final PreparedStatement pstmt = con.prepareStatement(INSERT_QUERY, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);)
        {

            for (final BaseMessage message : baseMessage)
            {
                pstmt.setString(1, message.getValue(MiddlewareConstant.MW_MESSAGE_ID));
                pstmt.setString(2, message.getJsonString());
                pstmt.setBoolean(3, isCustomerSpecific);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
        catch (final Exception e)
        {
            log.error("Exception while inserting retry data failure ", e);
        }
    }

    public static void getRetryMetaDataFailure()
    {
        final List<String> midList = new ArrayList<>();

        try (
                final Connection con = getClientHandoverConnection();
                final PreparedStatement pstmt = con.prepareStatement(SELECT_QUERY, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                final ResultSet resultSet = pstmt.executeQuery();)
        {

            while (resultSet.next())
            {
                final BaseMessage baseMessage = buildBaseMessage(resultSet);

                if (resultSet.getBoolean("is_customer_specific"))
                    RedisPusher.getInstance().addCustomerQueue(baseMessage);
                else
                    RedisPusher.getInstance().add(baseMessage);

                midList.add(resultSet.getString("mid"));
            }

            if (!midList.isEmpty())
                deleteRetryMetaDataForMid(midList);
        }
        catch (final Exception e)
        {
            log.error("Exception while inserting retry data failure ", e);
        }
    }

    private static void deleteRetryMetaDataForMid(
            List<String> aMidList)
    {
        final String queryMID    = buildMidForQuery(aMidList);
        final String actualQuery = RETRY_DATA_DELETE_QUERY + queryMID;

        try (
                final Connection con = getClientHandoverConnection();
                final PreparedStatement pstmt = con.prepareStatement(actualQuery, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);)
        {
            pstmt.executeUpdate();
        }
        catch (final Exception e)
        {
            log.error("Exception while fetching the data from retry_data DB ", e);
        }
    }

    public static void deleteForMids(
            List<String> aMidList)
    {
        final String queryMidList = buildMidForQuery(aMidList);
        final String actualQuery  = DELETE_QUERY + queryMidList;

        try (
                final Connection con = getClientHandoverConnection();
                final PreparedStatement pstmt = con.prepareStatement(actualQuery, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);)
        {
            pstmt.executeUpdate();
        }
        catch (final Exception e)
        {
            log.error("Exception while fetching the data from retry_data DB ", e);
        }
    }

    private static BaseMessage buildBaseMessage(
            ResultSet aResultSet)
            throws Exception
    {
        return new ClientHandoverObject(aResultSet.getString(MiddlewareConstant.MW_CLIENT_HANDOVER_RETRY_DATA_MESSAGE.getName()));
    }

    private static String buildMidForQuery(
            List<String> aMIDList)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("(");

        final int size = aMIDList.size();

        for (int i = 0; i < size; i++)
        {
            sb.append("'");
            sb.append(getMid(aMIDList.get(i)));
            sb.append("'");

            if (((i + 1) != size))
                sb.append(",");
        }
        sb.append(")");

        return sb.toString();
    }

    private static String getMid(
            String aRedisArgs)
    {
        final String[] splitValues = aRedisArgs.split("~");
        return splitValues[1];
    }

    private static Connection getClientHandoverConnection()
            throws Exception
    {
        return DBDataSourceFactory.getConnectionFromPool(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.CLIENT_HANDOVER.getKey()));
    }

}
