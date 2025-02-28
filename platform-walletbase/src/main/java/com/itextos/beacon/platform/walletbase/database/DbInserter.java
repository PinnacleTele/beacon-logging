package com.itextos.beacon.platform.walletbase.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.walletbase.data.WalletInput;

public class DbInserter
{

    private static final Log    log        = LogFactory.getLog(DbInserter.class);

    private static final String INSERT_SQL = "INSERT INTO " //
            + " prepaid_history" //
            + " (cli_id, file_id, base_msg_id, " //
            + " msg_id, process_type, no_of_parts, sms_rate, " //
            + " dlt_rate, reason, request_time)" //
            + " VALUES ( ?,?,?, ?,?,?,?, ?,?,?)";

    private DbInserter()
    {}

    public static void insertIntoDb(
            WalletInput aWalletMessage)
            throws Exception
    {
        if (aWalletMessage != null)
            try (
                    Connection con = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.BILLING.getKey()));
                    PreparedStatement pstmt = con.prepareStatement(INSERT_SQL))
            {
                setValues(pstmt, aWalletMessage);

                pstmt.execute();

                if (log.isDebugEnabled())
                    log.debug("Record succesfully inserted into billing.prepaid_history table");
            }
            catch (final Exception e)
            {
                log.error("Exception while inserting into billing.prepaid_history table", e);
                throw e;
            }
    }

    private static void setValues(
            PreparedStatement aPstmt,
            WalletInput aWalletMessage)
            throws SQLException
    {
        aPstmt.setString(1, aWalletMessage.getClientId());
        aPstmt.setString(2, aWalletMessage.getFileId());
        aPstmt.setString(3, aWalletMessage.getBaseMessageId());
        aPstmt.setString(4, aWalletMessage.getMessageId());
        aPstmt.setString(5, aWalletMessage.getProcessType());
        aPstmt.setInt(6, aWalletMessage.getNoOfParts());
        aPstmt.setDouble(7, aWalletMessage.getSmsRate());
        aPstmt.setDouble(8, aWalletMessage.getDltRate());
        aPstmt.setString(9, aWalletMessage.getReason());
        aPstmt.setString(10, DateTimeUtility.getFormattedDateTime(aWalletMessage.getRequestedTime(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
    }

    public static void insertIntoDb(
            List<WalletInput> aWalletMessageList)
            throws Exception
    {

        if (aWalletMessageList != null)
        {
            final Connection con = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.BILLING.getKey()));

            try (

                    PreparedStatement pstmt = con.prepareStatement(INSERT_SQL))
            {
                con.setAutoCommit(false);

                for (final WalletInput walletMessage : aWalletMessageList)
                {
                    setValues(pstmt, walletMessage);

                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                con.commit();

                if (log.isDebugEnabled())
                    log.debug(aWalletMessageList.size() + " Records succesfully inserted into billing.prepaid_history table");
            }
            catch (final Exception e)
            {
                CommonUtility.rollbackConnection(con);
                log.error("Exception while inserting into billing.prepaid_history table. Rollbacking the transaction.", e);
                throw e;
            }
            finally
            {
                CommonUtility.closeConnection(con);
            }
        }
    }

}