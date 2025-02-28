package com.itextos.beacon.platform.customkafkaprocessor.process;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfo;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

public class FullMessageTableInserter
        extends
        AbstractCustomProcess
{

    private static final Log    log             = LogFactory.getLog(FullMessageTableInserter.class);

    private static final String SQL             = "insert into test.temp_full_message_202203_2 values (?, ?, ?, ?, now())";
    private static final long   START_DATE_TIME = getStartTime();
    private static final long   END_DATE_TIME   = getEndTime();

    public FullMessageTableInserter(
            int aProcessSize)
    {
        super(aProcessSize);
    }

    private static long getStartTime()
    {
        final Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.set(Calendar.YEAR, 2022);
        c.set(Calendar.MONTH, 02); // March
        c.set(Calendar.DATE, 01);
        c.set(Calendar.HOUR_OF_DAY, 00);
        c.set(Calendar.MINUTE, 00);
        c.set(Calendar.SECOND, 00);

        c.add(Calendar.HOUR_OF_DAY, -1); // make it Feb 28 night 11:00:00

        log.fatal("Start Time " + c.getTime());
        System.out.println("Start Time " + c.getTime());

        return c.getTimeInMillis();
    }

    private static long getEndTime()
    {
        final Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.set(Calendar.YEAR, 2022);
        c.set(Calendar.MONTH, 02); // March
        c.set(Calendar.DATE, 02);
        c.set(Calendar.HOUR_OF_DAY, 14);
        c.set(Calendar.MINUTE, 00);
        c.set(Calendar.SECOND, 00);

        log.fatal("End Time " + c.getTime());
        System.out.println("End Time " + c.getTime());

        return c.getTimeInMillis();
    }

    @Override
    protected void process(
            List<IMessage> aMessages)
    {
        Connection        con   = null;
        PreparedStatement pstmt = null;

        if (log.isDebugEnabled())
            log.debug("Calling process for the records count of " + aMessages.size());

        log.debug("Total records '" + aMessages.size() + "'");

        try
        {
            con = getConnection();
            con.setAutoCommit(false);

            pstmt = con.prepareStatement(SQL);
            // boolean isRecordAdded = false;

            for (final IMessage message : aMessages)
            {
                final SubmissionObject msgRequest    = (SubmissionObject) message;

                // if (!canContinue(msgRequest))
                // continue;

                // isRecordAdded = true;
                final String           cliId         = msgRequest.getClientId();
                final String           baseMessageId = msgRequest.getBaseMessageId();
                final String           fullMessage   = msgRequest.getLongMessage();

                pstmt.setString(1, cliId);
                pstmt.setString(2, DateTimeUtility.getFormattedDateTime(msgRequest.getMessageReceivedDate(), DateTimeFormat.DEFAULT_DATE_ONLY));
                pstmt.setString(3, baseMessageId);
                pstmt.setString(4, fullMessage);

                pstmt.addBatch();
            }

            // if (isRecordAdded)
            // {
            final int[] lExecuteBatch = pstmt.executeBatch();
            log.fatal("Total records '" + aMessages.size() + "' Inserted records " + lExecuteBatch.length);

            con.commit();
            // }

            pstmt.clearBatch();
            pstmt.clearParameters();
        }
        catch (final Exception e)
        {
            log.fatal("Exception while inserting batch", e);
            CommonUtility.rollbackConnection(con);
            processIndependeRecords(aMessages);
        }
        finally
        {
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
        }
    }

    // private static boolean canContinue(
    // SubmissionObject aMsgRequest)
    // {
    // final long lFirstReceivedTime =
    // aMsgRequest.getMessageReceivedTime().getTime();
    // final boolean canContinue = (lFirstReceivedTime >= START_DATE_TIME) &&
    // (lFirstReceivedTime < END_DATE_TIME);
    // // final String baseMessageId = aMsgRequest.getBaseMessageId();
    // // log.fatal("Base id :'" + baseMessageId + "' Received Time '" +
    // // aMsgRequest.getMessageReceivedDate() + "' canContinue " + canContinue);
    //
    // return canContinue;
    // }

    private static void processIndependeRecords(
            List<IMessage> aMessages)
    {
        Connection        con   = null;
        PreparedStatement pstmt = null;

        try
        {
            con   = getConnection();
            pstmt = con.prepareStatement(SQL);

            for (final IMessage message : aMessages)
                processRecord(con, pstmt, message);
        }
        catch (final Exception e)
        {
            log.error("Exception while inserting individually", e);
        }
        finally
        {
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
        }
    }

    private static void processRecord(
            Connection aCon,
            PreparedStatement aPstmt,
            IMessage aMessage)
    {

        try
        {
            aCon.setAutoCommit(false);

            final SubmissionObject msgRequest    = (SubmissionObject) aMessage;

            // if (!canContinue(msgRequest))
            // return;

            final String           cliId         = msgRequest.getClientId();
            final String           baseMessageId = msgRequest.getBaseMessageId();
            final String           fullMessage   = msgRequest.getLongMessage();

            aPstmt.setString(1, cliId);
            aPstmt.setString(2, DateTimeUtility.getFormattedDateTime(msgRequest.getMessageReceivedDate(), DateTimeFormat.DEFAULT_DATE_ONLY));
            aPstmt.setString(3, baseMessageId);
            aPstmt.setString(4, fullMessage);

            aPstmt.execute();

            aCon.commit();
        }
        catch (final Exception e)
        {
            CommonUtility.rollbackConnection(aCon);
            log.error("Failed while inserting record", e);
        }
    }

    private static Connection getConnection()
            throws Exception
    {
        return DBDataSourceFactory.getConnectionFromThin(JndiInfo.CONFIGURARION_DB);
    }

}