package com.itextos.beacon.platform.sbc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.sbc.util.ConsumerId;

public abstract class DBHandler
{

    private DBHandler()
    {}

    private static final Log    log                 = LogFactory.getLog(DBHandler.class);
    private static final String INSERT_SQL          = "insert into {0} (instance_id, cli_id, date_time_to_process, message_payload) values (?,?,?,?)";

    public static final String  TABLE_NAME_SCHEDULE = "schedule_data";
    public static final String  TABLE_NAME_BLOCKOUT = "blockout_data";

    public static void insertRecords(
            List<MessageRequest> aMessageRequestList,
            String aTableName)
            throws ItextosException
    {
        Connection        lSqlCon = null;
        PreparedStatement lPstmt  = null;

        if (log.isDebugEnabled())
            log.debug("insertRecords() - Record inserting table name : " + aTableName);

        try
        {
            final String lSql = MessageFormat.format(INSERT_SQL, aTableName);
            lSqlCon = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));
            lPstmt  = lSqlCon.prepareStatement(lSql);

            lSqlCon.setAutoCommit(false);
            insertRecords(lPstmt, aMessageRequestList);
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
            final String s = "Excception while inserting the data into '" + aTableName + "'";
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
            List<MessageRequest> aMessageRequestList)
            throws SQLException
    {

        for (final MessageRequest lMessageRequest : aMessageRequestList)
        {
        //    aPstmt.setString(1, CommonUtility.nullCheck(lMessageRequest.getAppInstanceId(), true));
            aPstmt.setString(1, ConsumerId.getInstance().getConsumerId());
            aPstmt.setString(2, lMessageRequest.getClientId());

            final Date processTime = getProcessTime(lMessageRequest);

            if (processTime == null)
            {
                log.error("Cannot insert record as the process time is null. Message " + lMessageRequest);
                continue;
            }

            aPstmt.setTimestamp(3, new Timestamp(processTime.getTime()));
            aPstmt.setString(4, lMessageRequest.getJsonString());
            aPstmt.addBatch();
        }

        aPstmt.executeBatch();
    }

    private static Date getProcessTime(
            MessageRequest aMessageRequest)
    {
        Date processTime = getBlockoutTime(aMessageRequest);
        if (processTime == null)
            processTime = getScheduleTime(aMessageRequest);
        return processTime;
    }

    private static Date getScheduleTime(
            MessageRequest aMessageRequest)
    {
        return getDateTimeFromMessage(aMessageRequest.getScheduleDateTime());
    }

    private static Date getBlockoutTime(
            MessageRequest aMessageRequest)
    {
        return getDateTimeFromMessage(aMessageRequest.getProcessBlockoutTime());
    }

    private static Date getDateTimeFromMessage(
            Date aProcessedTime)
    {
        if (aProcessedTime != null)
            return aProcessedTime;
        return null;
    }

}