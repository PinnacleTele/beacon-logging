package com.itextos.beacon.queryprocessor.databaseconnector;

import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import com.itextos.beacon.queryprocessor.commonutils.CommonVariables;
import com.itextos.beacon.queryprocessor.commonutils.Utility;

public class SqlLiteConnectionProvider
{

    private static final Log        log                     = LogFactory.getLog(SqlLiteConnectionProvider.class);

    private ConnectionPoolSingleton ConnPoolObj             = null;
    private Connection              conn                    = null;
    final String                    sort_field;
    final String                    sort_order;
    public final String             dbFilePath;
    final boolean                   full_message;
    boolean                         isFMSG1PartTableCreated = false;

    public SqlLiteConnectionProvider(
            ConnectionPoolSingleton connPool,
            String dbFile,
            String sortField,
            String sortOrder,
            boolean fullMessage)
    {
        this.ConnPoolObj  = connPool;
        this.dbFilePath   = dbFile;
        this.sort_field   = sortField;
        this.sort_order   = sortOrder;
        this.full_message = fullMessage;
    }

    public void closeConnection()
            throws Exception
    {

        if ((conn != null) && !conn.isClosed())
        {
            conn.close();
            conn = null;
        }
    }

    public void GetDBConnection()
            throws Exception
    {
        // create new database for the queue
        final String dbURL = String.format("jdbc:sqlite:%s", dbFilePath);

        if ((conn != null) && !conn.isClosed())
            conn.close();

        conn = createSQLTDatabase(dbURL);

        conn = DriverManager.getConnection(dbURL);

        if (sort_field != null)
            createCSVTable();
    }

    public ResultSet getFMSGRecords()
            throws Exception
    {
        final String    ssql = String.format("SELECT * FROM %s", CommonVariables.SQLT_FMSG_DATA_TABLE_NAME);
        final Statement stmt = conn.createStatement();
        final ResultSet rs   = stmt.executeQuery(ssql);
        return rs;
    }

    private int fmsg2PartSetData(
            PreparedStatement fmdata_pstmt,
            HashMap<String, FMSGInfoRecord> hmFMSGInfo,
            StringBuilder sbBaseMsgID)
            throws SQLException
    {
        int                     dataCount              = 0;
        final Statement         fm1p_stmt              = conn.createStatement();

        final String            sqlt_1p_fmsg_sql_tmplt = "select * from " + CommonVariables.SQLT_FMSG_1PART_TABLE_NAME
                + " where base_msg_id in (%s)";
        final String            sql                    = String.format(sqlt_1p_fmsg_sql_tmplt, sbBaseMsgID.toString());

        final ResultSet         fm1p_rs                = fm1p_stmt.executeQuery(sql);
        final ResultSetMetaData rsmd                   = fm1p_rs.getMetaData();

        while (fm1p_rs.next())
        {
            final String         bMsgId     = fm1p_rs.getString(1);
            final FMSGInfoRecord fmir1p     = hmFMSGInfo.get(bMsgId);
            int                  fm_col_idx = 1;
            int                  rs_col_idx = 2;

            final int            is_hex_msg = fm1p_rs.getInt(rs_col_idx);
            fmdata_pstmt.setInt(fm_col_idx, is_hex_msg);
            fm_col_idx++;
            rs_col_idx++;

            final String msg_id = fm1p_rs.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, msg_id);
            fm_col_idx++;
            rs_col_idx++;

            final String cli_id = fm1p_rs.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, cli_id);
            fm_col_idx++;
            rs_col_idx++;

            final String recv_time = fm1p_rs.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, recv_time);
            fm_col_idx++;
            rs_col_idx++;

            final String carrier_sub_time = fm1p_rs.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, carrier_sub_time);
            fm_col_idx++;
            rs_col_idx++;

            String dly_time = "";
            if (fmir1p.subFailed == 0)
                dly_time = fm1p_rs.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, dly_time);
            fm_col_idx++;
            rs_col_idx++;

            final String cli_hdr = fm1p_rs.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, cli_hdr);
            fm_col_idx++;
            rs_col_idx++;

            final String dest = fm1p_rs.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, dest);
            fm_col_idx++;
            rs_col_idx++;

            final String msg_part_no = fm1p_rs.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, msg_part_no);
            fm_col_idx++;
            rs_col_idx++;

            final String msg = fm1p_rs.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, msg);
            fm_col_idx++;
            rs_col_idx++;

            if ((fmir1p.dnFailed == 1) || (fmir1p.subFailed == 1))
            {
                fmdata_pstmt.setString(fm_col_idx, CommonVariables.FMSG_STATUS_FAILED);
                fmdata_pstmt.setString(fm_col_idx + 1, CommonVariables.FMSG_REASON_FAILED);
            }
            else
                if ((fmir1p.dnSuccess == 1) && (fmir1p.dnFailed == 0))
                {
                    fmdata_pstmt.setString(fm_col_idx, CommonVariables.FMSG_DEL_STATUS_SUCCESS);
                    fmdata_pstmt.setString(fm_col_idx + 1, CommonVariables.FMSG_DEL_REASON_SUCCESS);
                }
                else
                    if ((fmir1p.subSuccess == 1) && (fmir1p.subFailed == 0))
                    {
                        fmdata_pstmt.setString(fm_col_idx, CommonVariables.FMSG_SUB_STATUS_SUCCESS);
                        fmdata_pstmt.setString(fm_col_idx + 1, CommonVariables.FMSG_SUB_REASON_SUCCESS);
                    }
            fm_col_idx += 2;

            final String campaign_name = fm1p_rs.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, campaign_name);
            fm_col_idx++;
            rs_col_idx++;

            final String dlt_tmpl_id = fm1p_rs.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, dlt_tmpl_id);
            fm_col_idx++;
            rs_col_idx++;

            final String dlt_entity_id = fm1p_rs.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, dlt_entity_id);
            fm_col_idx++;
            rs_col_idx++;

            final String billing_currency = fm1p_rs.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, billing_currency);
            fm_col_idx++;
            rs_col_idx++;

            fmdata_pstmt.setString(fm_col_idx, "" + fmir1p.smsRate);
            fmdata_pstmt.setString(fm_col_idx + 1, "" + fmir1p.dltRate);
            fm_col_idx += 2;

            final String file_id = fm1p_rs.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, file_id);
            fm_col_idx++;
            rs_col_idx++;

            final String intf_grp_type = fm1p_rs.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, intf_grp_type);

            fmdata_pstmt.addBatch();
            hmFMSGInfo.remove(fmir1p.baseMsgId);
            dataCount++;
        }

        if (dataCount > 0)
        {
            fmdata_pstmt.executeBatch();
            conn.commit();
            fmdata_pstmt.clearBatch();
        }
        return dataCount;
    }

    @SuppressWarnings("resource")
    public int insertMDBFMSGData2Part(
            PreparedStatement fmdata_pstmt,
            Connection clientConnection,
            List<String> client_ids,
            LocalDateTime ldt_startDate,
            LocalDateTime ldt_endDate,
            JSONObject paramFilters)
            throws Exception
    {
        log.info("Inserting Full Message Single Part from Maria DB");

        final ResultSet     rsFMSG1Part      = SQLStatementExecutor.getFMSGLogRecords1Part(clientConnection, client_ids,
                ldt_startDate, ldt_endDate, paramFilters);
        ResultSetMetaData   rsmd             = rsFMSG1Part.getMetaData();
        int                 col_count        = rsmd.getColumnCount();

        final StringBuilder sb_fmg1part_cols = new StringBuilder("base_msg_id TEXT, is_hex_msg INTEGER");
        final StringBuilder sb_ins_paramas   = new StringBuilder("?, ?");

        for (int cidx = 3; cidx <= col_count; cidx++)
        {
            final String col_name = rsmd.getColumnLabel(cidx);
            sb_fmg1part_cols.append(", ").append(col_name).append(" TEXT");
            sb_ins_paramas.append(", ?");
        }

        if (!isFMSG1PartTableCreated)
        {
            createFMSG1PartTable(sb_fmg1part_cols.toString());
            isFMSG1PartTableCreated = true;
        }

        final String            ssql_fm1part  = String.format("INSERT INTO %s VALUES(%s)",
                CommonVariables.SQLT_FMSG_1PART_TABLE_NAME, sb_ins_paramas.toString());
        final PreparedStatement fm1part_pstmt = conn.prepareStatement(ssql_fm1part);

        int                     record_count  = 0;
        int                     batch_count   = 0;

        while (rsFMSG1Part.next())
        {
            final String base_msg_id = rsFMSG1Part.getString(1);
            fm1part_pstmt.setString(1, base_msg_id);
            final int is_hex_msg = rsFMSG1Part.getInt(2);
            fm1part_pstmt.setInt(2, is_hex_msg);
            for (int cidx = 3; cidx <= col_count; cidx++)
                fm1part_pstmt.setString(cidx, rsFMSG1Part.getString(cidx));
            fm1part_pstmt.addBatch();
            record_count++;
            batch_count++;

            if (batch_count == CommonVariables.SQLT_FMSG_1PART_BATCH_COUNT)
            {
                fm1part_pstmt.executeBatch();
                conn.commit();
                fm1part_pstmt.clearBatch();
                batch_count = 0;
            }
        }

        if (batch_count > 0)
        {
            fm1part_pstmt.executeBatch();
            conn.commit();
            fm1part_pstmt.clearBatch();
        }

        fm1part_pstmt.close();
        Statement stmt = rsFMSG1Part.getStatement();
        rsFMSG1Part.close();
        stmt.close();

        log.info("Full Message 1 Part MariaDB, Count: " + record_count);

        log.info("Retrieving FMSG Info from Maria DB");
        final ResultSet rsFMSGInfo = SQLStatementExecutor.getFMSGInfo(clientConnection, client_ids, ldt_startDate,
                ldt_endDate, paramFilters);

        rsmd        = rsFMSGInfo.getMetaData();
        col_count   = rsmd.getColumnCount();
        batch_count = 0;
        final HashMap<String, FMSGInfoRecord> hmFMSGInfo  = new HashMap<>();
        StringBuilder                         sbBaseMsgID = new StringBuilder();

        while (rsFMSGInfo.next())
        {
            int          rs_col_idx  = 1;
            final String base_msg_id = rsFMSGInfo.getString(rs_col_idx);
            rs_col_idx++;
            final int total_parts = rsFMSGInfo.getInt(rs_col_idx);
            rs_col_idx++;
            final String sub_sts_code = Utility.nullCheck(rsFMSGInfo.getString(rs_col_idx), true);
            rs_col_idx++;
            final String dn_sts_code = Utility.nullCheck(rsFMSGInfo.getString(rs_col_idx), true);
            rs_col_idx++;
            final double sms_rate = rsFMSGInfo.getDouble(rs_col_idx);
            rs_col_idx++;
            final double dlt_rate   = rsFMSGInfo.getDouble(rs_col_idx);
            int          subSuccess = 0;
            int          subFailed  = 0;
            int          dnSuccess  = 0;
            int          dnFailed   = 0;

            if (sub_sts_code.equals(CommonVariables.SUB_STS_CODE_SUCCESS))
                subSuccess = 1;
            else
                subFailed = 1;

            if (!"".equals(dn_sts_code))
                if (dn_sts_code.equals(CommonVariables.DEL_STS_CODE_SUCCESS))
                    dnSuccess = 1;
                else
                    dnFailed = 1;

            if (hmFMSGInfo.containsKey(base_msg_id))
            {
                final FMSGInfoRecord fmir = hmFMSGInfo.get(base_msg_id);
                if (subSuccess == 1)
                    fmir.subSuccess = 1;

                if (subFailed == 1)
                    fmir.subFailed = 1;

                if (dnSuccess == 1)
                    fmir.dnSuccess = 1;

                if (dnFailed == 1)
                    fmir.dnFailed = 1;

                fmir.smsRate += sms_rate;
                fmir.dltRate += dlt_rate;

                fmir.msgPartCount++;

                if (fmir.msgPartCount == fmir.totalMsgParts)
                {
                    batch_count++;
                    if (batch_count > 1)
                        sbBaseMsgID.append(", ");

                    sbBaseMsgID.append("'").append(base_msg_id).append("'");

                    if (batch_count == CommonVariables.SQLT_FMSG_INFO_BATCH_COUNT)
                    {
                        fmsg2PartSetData(fmdata_pstmt, hmFMSGInfo, sbBaseMsgID);
                        sbBaseMsgID = new StringBuilder();
                        batch_count = 0;
                    }
                }
            }
            else
            {
                final FMSGInfoRecord fmir = new FMSGInfoRecord(base_msg_id, total_parts, subSuccess, dnSuccess,
                        subFailed, dnFailed, sms_rate, dlt_rate);
                hmFMSGInfo.put(base_msg_id, fmir);
            }
        }

        stmt = rsFMSGInfo.getStatement();
        rsFMSGInfo.close();
        stmt.close();

        if (hmFMSGInfo.size() > 0)
            for (final Entry<String, FMSGInfoRecord> fmiEntry : hmFMSGInfo.entrySet())
            {
                final String         base_msg_id = fmiEntry.getKey();
                final FMSGInfoRecord fmir        = fmiEntry.getValue();

                if (fmir.msgPartCount != fmir.totalMsgParts)
                {
                    batch_count++;
                    if (batch_count > 1)
                        sbBaseMsgID.append(",");

                    sbBaseMsgID.append("'" + base_msg_id + "'");
                }
            }

        if (batch_count > 0)
            fmsg2PartSetData(fmdata_pstmt, hmFMSGInfo, sbBaseMsgID);

        return record_count;
    }

    @SuppressWarnings("resource")
    public int insertPGFMSGData2Part(
            PreparedStatement fmdata_pstmt,
            Connection clientConnection,
            List<String> client_ids,
            LocalDateTime ldt_startDate,
            LocalDateTime ldt_endDate,
            JSONObject paramFilters)
            throws Exception
    {
        log.info("Inserting Full Message data from Postgres DB");
        final ResultSet         rsFMSG2Part  = SQLStatementExecutor.getPGFMSGLogRecords2Part(clientConnection,
                client_ids, ldt_startDate, ldt_endDate, paramFilters);
        final ResultSetMetaData rsmd         = rsFMSG2Part.getMetaData();
        final int               col_count    = rsmd.getColumnCount();

        int                     record_count = 0;
        int                     batch_count  = 0;

        while (rsFMSG2Part.next())
        {
            int       rs_col_idx = 1;
            int       fm_col_idx = 1;

            final int is_hex_msg = rsFMSG2Part.getInt(rs_col_idx);
            fmdata_pstmt.setInt(fm_col_idx, is_hex_msg);
            rs_col_idx++;
            fm_col_idx++;

            final String msg_id = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, msg_id);
            rs_col_idx++;
            fm_col_idx++;

            final String cli_id = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, cli_id);
            rs_col_idx++;
            fm_col_idx++;

            final String recv_time = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, recv_time);
            rs_col_idx++;
            fm_col_idx++;

            final String carrier_sub_time = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, carrier_sub_time);
            rs_col_idx++;
            fm_col_idx++;

            final String dly_time = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, dly_time);
            rs_col_idx++;
            fm_col_idx++;

            final String cli_hdr = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, cli_hdr);
            rs_col_idx++;
            fm_col_idx++;

            final String dest = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, dest);
            rs_col_idx++;
            fm_col_idx++;

            final String msg_part_no = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, msg_part_no);
            rs_col_idx++;
            fm_col_idx++;

            final String msg = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, msg);
            rs_col_idx++;
            fm_col_idx++;

            final int subSuccess = rsFMSG2Part.getInt(rs_col_idx);
            rs_col_idx++;

            final int subFailed = rsFMSG2Part.getInt(rs_col_idx);
            rs_col_idx++;

            final int dnSuccess = rsFMSG2Part.getInt(rs_col_idx);
            rs_col_idx++;

            final int dnFailed = rsFMSG2Part.getInt(rs_col_idx);
            rs_col_idx++;

            if ((dnFailed == 1) || (subFailed == 1))
            {
                fmdata_pstmt.setString(fm_col_idx, CommonVariables.FMSG_STATUS_FAILED);
                fmdata_pstmt.setString(fm_col_idx + 1, CommonVariables.FMSG_REASON_FAILED);
            }
            else
                if (dnSuccess == 1)
                {
                    fmdata_pstmt.setString(fm_col_idx, CommonVariables.FMSG_DEL_STATUS_SUCCESS);
                    fmdata_pstmt.setString(fm_col_idx + 1, CommonVariables.FMSG_DEL_REASON_SUCCESS);
                }
                else
                    if (subSuccess == 1)
                    {
                        fmdata_pstmt.setString(fm_col_idx, CommonVariables.FMSG_SUB_STATUS_SUCCESS);
                        fmdata_pstmt.setString(fm_col_idx + 1, CommonVariables.FMSG_SUB_REASON_SUCCESS);
                    }

            fm_col_idx += 2;

            final String campaign_name = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, campaign_name);
            rs_col_idx++;
            fm_col_idx++;

            final String dlt_tmpl_id = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, dlt_tmpl_id);
            rs_col_idx++;
            fm_col_idx++;

            final String dlt_entity_id = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, dlt_entity_id);
            rs_col_idx++;
            fm_col_idx++;

            final String billing_currency = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, billing_currency);
            rs_col_idx++;
            fm_col_idx++;

            final String sms_rate = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, sms_rate);
            rs_col_idx++;
            fm_col_idx++;

            final String dlt_rate = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, dlt_rate);
            rs_col_idx++;
            fm_col_idx++;

            final String file_id = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, file_id);
            rs_col_idx++;
            fm_col_idx++;

            final String intf_grp_type = rsFMSG2Part.getString(rs_col_idx);
            fmdata_pstmt.setString(fm_col_idx, intf_grp_type);

            fmdata_pstmt.addBatch();
            record_count++;
            batch_count++;

            if (batch_count == CommonVariables.SQLT_FMSG_1PART_BATCH_COUNT)
            {
                fmdata_pstmt.executeBatch();
                conn.commit();
                fmdata_pstmt.clearBatch();
                batch_count = 0;
            }
        }

        if (batch_count > 0)
        {
            fmdata_pstmt.executeBatch();
            conn.commit();
            fmdata_pstmt.clearBatch();
        }
        log.info("Full Message data from Postgres DB, Count: " + record_count);
        return record_count;
    }

    @SuppressWarnings("resource")
    public int insertFMSGData(
            String dbType,
            Connection clientConnection,
            List<String> client_ids,
            LocalDateTime ldt_startDate,
            LocalDateTime ldt_endDate,
            JSONObject paramFilters)
            throws Exception
    {
        isFMSG1PartTableCreated = false;

        conn.setAutoCommit(false);

        ResultSet rsFMSGData = null;

        if (dbType.equals(CommonVariables.MARIA_DB))
            rsFMSGData = SQLStatementExecutor.getLogRecordsWithFlag(clientConnection, client_ids, ldt_startDate,
                    ldt_endDate, paramFilters, true, false, 1);
        else
            if (dbType.equals(CommonVariables.PG_DB))
                rsFMSGData = SQLStatementExecutor.getPGLogRecordsWithFlag(clientConnection, client_ids, ldt_startDate,
                        ldt_endDate, paramFilters, true, false, 1);

        final ResultSetMetaData rsmd           = rsFMSGData.getMetaData();
        final int               col_count      = rsmd.getColumnCount();

        final StringBuilder     sb_fmg_cols    = new StringBuilder("is_hex_msg INTEGER");
        final StringBuilder     sb_ins_paramas = new StringBuilder("?");

        for (int cidx = 2; cidx <= col_count; cidx++)
        {
            final String col_name = rsmd.getColumnLabel(cidx);
            sb_fmg_cols.append(", ").append(col_name).append(" TEXT");
            sb_ins_paramas.append(", ?");
        }

        createFMSGDataTable(sb_fmg_cols.toString());

        final String            ssql_fmdata  = String.format("INSERT INTO %s VALUES(%s)",
                CommonVariables.SQLT_FMSG_DATA_TABLE_NAME, sb_ins_paramas.toString());
        final PreparedStatement fmdata_pstmt = conn.prepareStatement(ssql_fmdata);

        int                     record_count = 0;
        int                     batch_count  = 0;

        while (rsFMSGData.next())
        {
            final int is_hex_msg = rsFMSGData.getInt(1);
            fmdata_pstmt.setInt(1, is_hex_msg);
            for (int cidx = 2; cidx <= col_count; cidx++)
                fmdata_pstmt.setString(cidx, rsFMSGData.getString(cidx));
            fmdata_pstmt.addBatch();
            record_count++;
            batch_count++;

            if (batch_count == 5000)
            {
                fmdata_pstmt.executeBatch();
                conn.commit();
                fmdata_pstmt.clearBatch();
                batch_count = 0;
            }
        }

        if (batch_count > 0)
        {
            fmdata_pstmt.executeBatch();
            conn.commit();
            fmdata_pstmt.clearBatch();
        }

        final Statement stmt = rsFMSGData.getStatement();
        rsFMSGData.close();
        stmt.close();

        log.info("Full Message Single Part Count: " + record_count);

        int fmsg2PartCount = 0;
        if (dbType.equals(CommonVariables.MARIA_DB))
            fmsg2PartCount = insertMDBFMSGData2Part(fmdata_pstmt, clientConnection, client_ids, ldt_startDate,
                    ldt_endDate, paramFilters);
        else
            if (dbType.equals(CommonVariables.PG_DB))
                fmsg2PartCount = insertPGFMSGData2Part(fmdata_pstmt, clientConnection, client_ids, ldt_startDate,
                        ldt_endDate, paramFilters);

        fmdata_pstmt.close();
        log.info("Full Message Multi Part Count: " + fmsg2PartCount);
        record_count += fmsg2PartCount;
        return record_count;
    }

    public int insertCSVRecords(
            ResultSet rs,
            String timeZoneName)
            throws Exception
    {
        if (rs.isClosed())
            return -1;

        final String            ssql       = String.format("INSERT INTO %s(%s, %s) VALUES(?, ?)",
                CommonVariables.SQLT_TABLE_NAME, sort_field, CommonVariables.SQLT_CSV_COLUMN_NAME);
        final PreparedStatement sqlt_pstmt = conn.prepareStatement(ssql);
        conn.setAutoCommit(false);
        int                   record_count = 0;
        int                   batch_count  = 0;
        final HashSet<String> hsMsgId      = new HashSet<>();

        while (true)
        {
            final String csvRecord = ResultSetConverter.ConvertRsToLogCSVRecord(rs, timeZoneName, hsMsgId);
            if (csvRecord == null)
                break;
            else
                if (csvRecord.equals(CommonVariables.DUP_MSGID_FOUND))
                    continue;

            record_count++;
            final String sortColValue = Utility.nullCheck(rs.getString(sort_field), true);
            sqlt_pstmt.setString(1, sortColValue);
            sqlt_pstmt.setString(2, csvRecord);
            sqlt_pstmt.addBatch();
            batch_count++;

            if (batch_count == 5000)
            {
                sqlt_pstmt.executeBatch();
                conn.commit();
                sqlt_pstmt.clearBatch();
                batch_count = 0;
            }
        }

        if (batch_count > 0)
        {
            sqlt_pstmt.executeBatch();
            conn.commit();
            sqlt_pstmt.clearBatch();
        }
        sqlt_pstmt.close();
        return record_count;
    }

    public int writeSortLogCSVFile(
            BufferedWriter bwCSV)
            throws Exception
    {
        int             row_count = 0;
        final String    ssql      = String.format("SELECT %s FROM %s ORDER BY %s %s",
                CommonVariables.SQLT_CSV_COLUMN_NAME, CommonVariables.SQLT_TABLE_NAME, sort_field, sort_order);

        final Statement stmt      = conn.createStatement();

        // create a new table
        final ResultSet rs        = stmt.executeQuery(ssql);

        while (rs.next())
        {
            bwCSV.write(rs.getString(CommonVariables.SQLT_CSV_COLUMN_NAME));
            bwCSV.newLine();
            row_count++;
        }
        rs.close();
        stmt.close();
        return row_count;
    }

    void createCSVTable()
            throws Exception
    {
        final Statement stmt = conn.createStatement();

        log.info("Creating CSV Record Table in SQLite");
        final String ssql = String.format("CREATE TABLE %s (%s text, %s text)", CommonVariables.SQLT_TABLE_NAME,
                sort_field, CommonVariables.SQLT_CSV_COLUMN_NAME);
        // create a new table
        stmt.executeUpdate(ssql);
        stmt.close();
    }

    void createFMSGDataTable(
            String table_cols)
            throws Exception
    {
        final Statement stmt  = conn.createStatement();

        final String    ssql1 = String.format("DROP TABLE IF EXISTS %s", CommonVariables.SQLT_FMSG_DATA_TABLE_NAME);
        stmt.executeUpdate(ssql1);
        log.info("Creating FMSG Data Table in SQLite");
        final String ssql2 = String.format("CREATE TABLE %s (%s)", CommonVariables.SQLT_FMSG_DATA_TABLE_NAME,
                table_cols);
        // create a new table
        stmt.executeUpdate(ssql2);

        stmt.close();
    }

    void createFMSG1PartTable(
            String table_cols)
            throws Exception
    {
        final Statement stmt  = conn.createStatement();
        final String    ssql1 = String.format("DROP TABLE IF EXISTS %s", CommonVariables.SQLT_FMSG_1PART_TABLE_NAME);
        stmt.executeUpdate(ssql1);
        log.info("Creating FMSG First Part Data Table in SQLite");
        final String ssql2 = String.format("CREATE TABLE %s (%s)", CommonVariables.SQLT_FMSG_1PART_TABLE_NAME,
                table_cols);
        // create a new table
        stmt.executeUpdate(ssql2);

        stmt.close();
    }

    static Connection createSQLTDatabase(
            String url)
            throws Exception
    {
        final Connection conn = DriverManager.getConnection(url);

        if (conn != null)
            conn.getMetaData();

        return conn;
    }

}
