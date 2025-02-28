package com.itextos.beacon.queryprocessor.databaseconnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.itextos.beacon.queryprocessor.commonutils.CommonVariables;
import com.itextos.beacon.queryprocessor.commonutils.Utility;

public class SQLStatementExecutor
{

    private static final Log              log         = LogFactory.getLog(SQLStatementExecutor.class);
    public static ConnectionPoolSingleton ConnPoolObj = null;

    public static ResultSet getQueryRecordsForUpdate(
            Connection conn,
            String ssql)
    {
        Statement stmt;

        try
        {
            stmt = conn.createStatement();

            // read client table to get connection strings
            final ResultSet rsResults = stmt.executeQuery(ssql);

            return rsResults;
        }
        catch (final SQLException e)
        {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    public static ResultSet getQueryRecords(
            Connection conn,
            String ssql)
    {
        Statement stmt;

        try
        {
            stmt = conn.createStatement();

            // read client table to get connection strings
            final ResultSet rsResults = stmt.executeQuery(ssql);

            return rsResults;
        }
        catch (final SQLException e)
        {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    public static boolean executeDMLQuery(
            Connection conn,
            String ssql)
    {
        Statement stmt;

        try
        {
            stmt = conn.createStatement();

            // read client table to get connection strings
            return stmt.execute(ssql);
        }
        catch (final SQLException e)
        {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    private static String getPGColumns(
            String paramColumns)
            throws Exception
    {
        final ConnectionPoolSingleton conSingleton = ConnectionPoolSingleton.getInstance();
        final String[]                colListArr   = paramColumns.split(",");
        final StringBuilder           sbColumns    = new StringBuilder();

        int                           colIdx       = 0;

        for (final String lString : colListArr)
        {
            final String paramColName = lString.trim();
            String       colName      = null;
            if (paramColName.startsWith("s."))
                colName = conSingleton.hmMapSubPG_MYSQL.get(paramColName.replace("s.", ""));
            else
                if (paramColName.startsWith("d."))
                    colName = conSingleton.hmMapDelPG_MYSQL.get(paramColName.replace("d.", ""));

            if ((colName == null) || "".equals(colName))
                colName = lString.replace("s.", "").replace("d.", "");

            if (colIdx > 0)
                sbColumns.append(",");

            sbColumns.append(colName);
            colIdx++;
        }
        return sbColumns.toString();
    }

    public static ResultSet getMariaDBDataForAPI(
            Connection clientConnection,
            List<String> client_ids,
            LocalDateTime ldt_startDate,
            LocalDateTime ldt_endDate,
            JSONObject paramJson,
            int maxlimit)
            throws Exception
    {
        final String condition    = generateFilterCondition((JSONObject) paramJson.get("filters"),
                CommonVariables.MARIA_DB);

        final String startDate    = Utility.formatDateTime(ldt_startDate);
        final String endDate      = Utility.formatDateTime(ldt_endDate);
        final String cli_id_list  = String.join(",", client_ids);

        String       dbName       = CommonVariables.MARIA_DB_PREFIX;
        String       subTableName = CommonVariables.MARIA_DB_SUB_TABLE_PREFIX;
        String       delTableName = CommonVariables.MARIA_DB_DEL_TABLE_PREFIX;

        if (ConnPoolObj.isDBMonthSuffix)
        {
            final String dbSuffix    = "_" + Utility.formatDateTime(ldt_startDate, Utility.DATE_YYYYMM);
            final String tableSuffix = "_" + Utility.formatDateTime(ldt_startDate, Utility.DATE_YYYYMMDD);
            dbName       += dbSuffix;
            subTableName += tableSuffix;
            delTableName += tableSuffix;
        }

        final String ssql = String.format(
                "SELECT s.is_hex_msg, s.msg_id," + paramJson.get("columns") + " FROM " + dbName + "." + subTableName
                        + " s LEFT JOIN " + dbName + "." + delTableName + " d  ON s.msg_id = d.msg_id "
                        + "WHERE s.recv_time >= '%s' AND s.recv_time < '%s' AND s.cli_id in (%s) %s LIMIT %s",
                startDate, endDate, cli_id_list, condition, maxlimit);

        log.info(ssql);

        return getQueryRecords(clientConnection, ssql);
    }

    public static ResultSet getPGDataForAPI(
            Connection clientConnection,
            List<String> client_ids,
            LocalDateTime ldt_startDate,
            LocalDateTime ldt_endDate,
            JSONObject paramJson,
            int maxlimit)
            throws Exception
    {
        final String condition   = generateFilterCondition((JSONObject) paramJson.get("filters"),
                CommonVariables.PG_DB);
        final String columnList  = getPGColumns(paramJson.get("columns").toString());

        final String tableSuffix = Utility.formatDateTime(ldt_startDate, Utility.DATE_YYYYMMDD);
        final String startDate   = Utility.formatDateTime(ldt_startDate);
        final String endDate     = Utility.formatDateTime(ldt_endDate);
        final String cli_id_list = String.join(",", client_ids);

        final String ssql        = String.format(
                "SELECT s.sub_is_hex_msg, s.msg_id, " + columnList + " FROM smslog.sub_del_log_%s s "
                        + "WHERE s.recv_time >= '%s' AND s.recv_time < '%s' AND s.cli_id in (%s) %s LIMIT %s",
                tableSuffix, startDate, endDate, cli_id_list, condition, maxlimit);

        log.info(ssql);

        return getQueryRecords(clientConnection, ssql);
    }

    public static ResultSet getLogRecords(
            Connection clientConnection,
            List<String> client_ids,
            LocalDateTime ldt_startDate,
            LocalDateTime ldt_endDate,
            JSONObject paramFilters,
            boolean isFullMessage)
            throws Exception
    {
        return getLogRecordsWithFlag(clientConnection, client_ids, ldt_startDate, ldt_endDate, paramFilters,
                isFullMessage, false, 0);
    }

    public static ResultSet getLogRecordsWithFlag(
            Connection clientConnection,
            List<String> client_ids,
            LocalDateTime ldt_startDate,
            LocalDateTime ldt_endDate,
            JSONObject paramFilters,
            boolean isFullMessage,
            boolean isFirstePart,
            int multiPartFlag)
            throws Exception
    {
        final String condition    = generateFilterCondition(paramFilters, CommonVariables.MARIA_DB);
        final String startDate    = Utility.formatDateTime(ldt_startDate);
        final String endDate      = Utility.formatDateTime(ldt_endDate);
        final String cli_id_list  = String.join(",", client_ids);

        String       dbName       = CommonVariables.MARIA_DB_PREFIX;
        String       subTableName = CommonVariables.MARIA_DB_SUB_TABLE_PREFIX;
        String       delTableName = CommonVariables.MARIA_DB_DEL_TABLE_PREFIX;

        if (ConnPoolObj.isDBMonthSuffix)
        {
            final String dbSuffix    = "_" + Utility.formatDateTime(ldt_startDate, Utility.DATE_YYYYMM);
            final String tableSuffix = "_" + Utility.formatDateTime(ldt_startDate, Utility.DATE_YYYYMMDD);
            dbName       += dbSuffix;
            subTableName += tableSuffix;
            delTableName += tableSuffix;
        }

        String sql = "SELECT s.is_hex_msg, s.msg_id, s.cli_id, "
                + "DATE_FORMAT(s.recv_time,'%%Y-%%m-%%d %%H:%%i:%%s') as recv_time, "
                + "DATE_FORMAT(s.carrier_sub_time, '%%Y-%%m-%%d %%H:%%i:%%s') as carrier_sub_time, "
                + "(case when d.dly_time is null or s.sub_ori_sts_code != '400' then '' "
                + "    else DATE_FORMAT(d.dly_time, '%%Y-%%m-%%d %%H:%%i:%%s') end) as dly_time, "
                + "s.cli_hdr, s.dest, ";

        if (isFullMessage)
            sql += "s.total_msg_parts as msg_part_no, ";
        else
            sql += "s.msg_part_no, ";

        sql += "s.msg, coalesce(d.delivery_status, s.sub_status) as status, "
                + "coalesce(d.dn_ori_sts_desc, s.sub_ori_sts_desc) as reason, "
                + "s.campaign_name, s.dlt_tmpl_id, s.dlt_entity_id, s.billing_currency, "
                + "(s.billing_sms_rate + coalesce(d.billing_sms_rate,0)) as sms_rate, "
                + "(s.billing_add_fixed_rate + coalesce(d.billing_add_fixed_rate,0)) as dlt_rate, "
                + "(case when s.intf_grp_type = 'api' then s.file_id else '' end) as file_id, "
                + "s.intf_grp_type FROM " + dbName + "." + subTableName + " s LEFT JOIN " + dbName + "." + delTableName
                + " d ON s.msg_id = d.msg_id "
                + "WHERE s.recv_time >= '%s' AND s.recv_time < '%s'  AND s.cli_id in (%s) ";

        if (isFirstePart)
            sql += " AND s.msg_part_no in (0,1) ";

        if (multiPartFlag == 1)
            sql += " AND s.total_msg_parts = 1 ";
        else
            if (multiPartFlag == 2)
                sql += " AND s.total_msg_parts > 1 ";

        sql += "  %s";
        final String ssql = String.format(sql, startDate, endDate, cli_id_list, condition);

        log.info(ssql);

        return getQueryRecords(clientConnection, ssql);
    }

    public static ResultSet getFMSGLogRecords1Part(
            Connection clientConnection,
            List<String> client_ids,
            LocalDateTime ldt_startDate,
            LocalDateTime ldt_endDate,
            JSONObject paramFilters)
            throws Exception
    {
        final String condition    = generateFilterCondition(paramFilters, CommonVariables.MARIA_DB);
        final String startDate    = Utility.formatDateTime(ldt_startDate);
        final String endDate      = Utility.formatDateTime(ldt_endDate);
        final String cli_id_list  = String.join(",", client_ids);

        String       dbName       = CommonVariables.MARIA_DB_PREFIX;
        String       subTableName = CommonVariables.MARIA_DB_SUB_TABLE_PREFIX;
        String       delTableName = CommonVariables.MARIA_DB_DEL_TABLE_PREFIX;
        String       fmTableName  = CommonVariables.MARIA_DB_FMSG_TABLE_PREFIX;

        if (ConnPoolObj.isDBMonthSuffix)
        {
            final String dbSuffix    = "_" + Utility.formatDateTime(ldt_startDate, Utility.DATE_YYYYMM);
            final String tableSuffix = "_" + Utility.formatDateTime(ldt_startDate, Utility.DATE_YYYYMMDD);
            dbName       += dbSuffix;
            subTableName += tableSuffix;
            delTableName += tableSuffix;
            fmTableName  += tableSuffix;
        }

        final String sql  = "SELECT s.base_msg_id, s.is_hex_msg,  s.msg_id, s.cli_id, "
                + "DATE_FORMAT(s.recv_time, '%%Y-%%m-%%d %%H:%%i:%%s') as recv_time, "
                + "DATE_FORMAT(s.carrier_sub_time, '%%Y-%%m-%%d %%H:%%i:%%s') as carrier_sub_time, "
                + "(case when d.dly_time is null then '' else DATE_FORMAT(d.dly_time, '%%Y-%%m-%%d %%H:%%i:%%s') end) as dly_time, "
                + "s.cli_hdr, s.dest, s.total_msg_parts as msg_part_no, "
                + "fm.long_msg as msg, s.campaign_name, s.dlt_tmpl_id, s.dlt_entity_id, s.billing_currency, "
                // + "(s.billing_sms_rate + coalesce(d.billing_sms_rate,0)) as sms_rate, "
                // + "(s.billing_add_fixed_rate + coalesce(d.billing_add_fixed_rate,0)) as
                // dlt_rate, "
                + "(case when s.intf_grp_type = 'api' then s.file_id else '' end) as file_id, s.intf_grp_type "
                + "FROM " + dbName + "." + subTableName + " s INNER JOIN " + dbName + "." + fmTableName
                + " fm ON s.base_msg_id = fm.base_msg_id LEFT JOIN " + dbName + "." + delTableName
                + " d ON s.msg_id = d.msg_id "
                + "WHERE s.recv_time >= '%s' AND s.recv_time < '%s'  AND s.cli_id in (%s) %s"
                + " AND s.msg_part_no = 1 AND s.total_msg_parts > 1";

        final String ssql = String.format(sql, startDate, endDate, cli_id_list, condition);

        log.info(ssql);

        return getQueryRecords(clientConnection, ssql);
    }

    public static ResultSet getFMSGInfo(
            Connection clientConnection,
            List<String> client_ids,
            LocalDateTime ldt_startDate,
            LocalDateTime ldt_endDate,
            JSONObject paramFilters)
            throws Exception
    {
        final String condition    = generateFilterCondition(paramFilters, CommonVariables.MARIA_DB);
        final String startDate    = Utility.formatDateTime(ldt_startDate);
        final String endDate      = Utility.formatDateTime(ldt_endDate);
        final String cli_id_list  = String.join(",", client_ids);

        String       dbName       = CommonVariables.MARIA_DB_PREFIX;
        String       subTableName = CommonVariables.MARIA_DB_SUB_TABLE_PREFIX;
        String       delTableName = CommonVariables.MARIA_DB_DEL_TABLE_PREFIX;
        String       fmTableName  = CommonVariables.MARIA_DB_FMSG_TABLE_PREFIX;

        if (ConnPoolObj.isDBMonthSuffix)
        {
            final String dbSuffix    = "_" + Utility.formatDateTime(ldt_startDate, Utility.DATE_YYYYMM);
            final String tableSuffix = "_" + Utility.formatDateTime(ldt_startDate, Utility.DATE_YYYYMMDD);
            dbName       += dbSuffix;
            subTableName += tableSuffix;
            delTableName += tableSuffix;
            fmTableName  += tableSuffix;
        }

        String sql = "SELECT s.base_msg_id, s.total_msg_parts, s.sub_ori_sts_code, "
                + "coalesce(d.dn_ori_sts_code, '') as dn_ori_sts_code, "
                + "(s.billing_sms_rate + coalesce(d.billing_sms_rate, 0)) as sms_rate, "
                + "(s.billing_add_fixed_rate + coalesce(d.billing_add_fixed_rate, 0)) as dlt_rate  FROM " + dbName + "."
                + subTableName + " s LEFT JOIN " + dbName + "." + delTableName + " d ON s.msg_id = d.msg_id "
                + "WHERE s.recv_time >= '%s' AND s.recv_time < '%s' AND s.cli_id in (%s) "
                + " AND s.total_msg_parts > 1 ";
        sql += "  %s";
        final String ssql = String.format(sql, startDate, endDate, cli_id_list, condition);

        log.info(ssql);

        return getQueryRecords(clientConnection, ssql);
    }

    public static ResultSet getPGLogRecords(
            Connection clientConnection,
            List<String> client_ids,
            LocalDateTime ldt_startDate,
            LocalDateTime ldt_endDate,
            JSONObject paramFilters,
            boolean isFullMessage)
            throws Exception
    {
        return getPGLogRecordsWithFlag(clientConnection, client_ids, ldt_startDate, ldt_endDate, paramFilters,
                isFullMessage, false, 0);
    }

    public static ResultSet getPGLogRecordsWithFlag(
            Connection clientConnection,
            List<String> client_ids,
            LocalDateTime ldt_startDate,
            LocalDateTime ldt_endDate,
            JSONObject paramFilters,
            boolean isFullMessage,
            boolean isFirstePart,
            int multiPartFlag)
            throws Exception
    {
        final String condition     = generateFilterCondition(paramFilters, CommonVariables.PG_DB);

        final String startDate     = Utility.formatDateTime(ldt_startDate.toLocalDate());
        final String startDateTime = Utility.formatDateTime(ldt_startDate);
        final String endDateTime   = Utility.formatDateTime(ldt_endDate);
        final String cli_id_list   = String.join(",", client_ids);

        final String tableSuffix   = Utility.formatDateTime(ldt_startDate, Utility.DATE_YYYYMMDD);
        final String sub_del_tbl   = "smslog." + CommonVariables.PG_DB_SUB_DEL_TABLE_PREFIX + "_" + tableSuffix;

        String       sql           = "SELECT s.sub_is_hex_msg as is_hex_msg,  s.msg_id, s.cli_id, "
                + "to_char(s.recv_time,'YYYY-MM-DD HH24:MI:SS') as recv_time, "
                + "to_char(s.sub_carrier_sub_time,'YYYY-MM-DD HH24:MI:SS') as carrier_sub_time, "
                + "(case when s.del_dly_time is null or s.sub_sub_ori_sts_code != '400' then '' "
                + "       else to_char(s.del_dly_time,'YYYY-MM-DD HH24:MI:SS') end) as dly_time, "
                + "s.sub_cli_hdr as cli_hdr, s.dest, ";

        if (isFullMessage)
            sql += "s.sub_total_msg_parts as msg_part_no,";
        else
            sql += "s.sub_msg_part_no as msg_part_no,";

        sql += "s.sub_msg as msg, coalesce(s.del_delivery_status, s.sub_sub_status) as status, "
                + "coalesce(s.del_dn_ori_sts_desc, s.sub_sub_ori_sts_desc) as reason, "
                + "s.sub_campaign_name as campaign_name, s.sub_dlt_tmpl_id as dlt_tmpl_id, s.sub_dlt_entity_id as dlt_entity_id, "
                + "s.sub_billing_currency as billing_currency, "
                + "(s.sub_billing_sms_rate + coalesce(s.del_billing_sms_rate, 0)) as sms_rate, "
                + "(s.sub_billing_add_fixed_rate + coalesce(s.del_billing_add_fixed_rate, 0)) as dlt_rate, "
                + "(case when s.sub_intf_grp_type = 'api' then s.sub_file_id else '' end) as file_id, "
                + "s.sub_intf_grp_type as intf_grp_type FROM " + sub_del_tbl
                + " s WHERE s.recv_date = '%s' AND s.recv_time >= '%s' AND s.recv_time < '%s' AND s.cli_id in (%s) ";

        if (isFirstePart)
            sql += " AND s.sub_msg_part_no in (0,1) ";

        if (multiPartFlag == 1)
            sql += " AND s.sub_total_msg_parts = 1 ";
        else
            if (multiPartFlag == 2)
                sql += " AND s.sub_total_msg_parts > 1 ";

        sql += "  %s";

        final String ssql = String.format(sql, startDate, startDateTime, endDateTime, cli_id_list, condition);

        log.info(ssql);

        return getQueryRecords(clientConnection, ssql);
    }

    public static ResultSet getPGFMSGLogRecords2Part(
            Connection clientConnection,
            List<String> client_ids,
            LocalDateTime ldt_startDate,
            LocalDateTime ldt_endDate,
            JSONObject paramFilters)
            throws Exception
    {
        final String condition     = generateFilterCondition(paramFilters, CommonVariables.PG_DB);

        final String startDate     = Utility.formatDateTime(ldt_startDate.toLocalDate());
        final String startDateTime = Utility.formatDateTime(ldt_startDate);
        final String endDateTime   = Utility.formatDateTime(ldt_endDate);
        final String cli_id_list   = String.join(",", client_ids);

        final String tableSuffix   = Utility.formatDateTime(ldt_startDate, Utility.DATE_YYYYMMDD);
        final String sub_del_tbl   = "smslog." + CommonVariables.PG_DB_SUB_DEL_TABLE_PREFIX + "_" + tableSuffix;
        final String fm_tbl        = "smslog." + CommonVariables.PG_DB_FMSG_TABLE_PREFIX + "_" + tableSuffix;
        final String fmi_tbl       = "smslog." + CommonVariables.PG_DB_FMSG_INFO_TABLE_PREFIX + "_" + tableSuffix;

        final String sql           = "SELECT s.sub_is_hex_msg as is_hex_msg,  s.msg_id, s.cli_id, "
                + "to_char(s.recv_time,'YYYY-MM-DD HH24:MI:SS') as recv_time, "
                + "to_char(s.sub_carrier_sub_time,'YYYY-MM-DD HH24:MI:SS') as carrier_sub_time, "
                + "(case when del_dly_time is null or sub_sub_ori_sts_code != '400' then '' "
                + "       else to_char(del_dly_time,'YYYY-MM-DD HH24:MI:SS') end) as dly_time, "
                + "s.sub_cli_hdr as cli_hdr, s.dest, s.sub_total_msg_parts as msg_part_no, fm.long_msg as msg, "
                + "fmi.sub_success, fmi.sub_failed, fmi.dn_success, fmi.dn_failed, "
                + "s.sub_campaign_name as campaign_name, s.sub_dlt_tmpl_id as dlt_tmpl_id, s.sub_dlt_entity_id as dlt_entity_id, "
                + "fmi.billing_currency, fmi.billing_sms_rate as sms_rate, fmi.billing_add_fixed_rate as dlt_rate, "
                + "(case when s.sub_intf_grp_type = 'api' then s.sub_file_id else '' end) as file_id, "
                + "s.sub_intf_grp_type as intf_grp_type " + "FROM " + sub_del_tbl + " s INNER JOIN " + fmi_tbl
                + " fmi ON s.recv_date = fmi.recv_date " + " AND s.base_msg_id = fmi.base_msg_id INNER JOIN " + fm_tbl
                + " fm ON s.recv_date = fm.recv_date " + " AND  s.base_msg_id = fm.base_msg_id "
                + "WHERE s.recv_date = '%s' AND s.recv_time >= '%s' AND s.recv_time < '%s' AND s.cli_id in (%s) %s "
                + " AND s.sub_msg_part_no = 1 AND s.sub_total_msg_parts > 1";

        final String ssql          = String.format(sql, startDate, startDateTime, endDateTime, cli_id_list, condition);

        log.info(ssql);

        return getQueryRecords(clientConnection, ssql);
    }

    public static ResultSet getPGFMSGInfo(
            Connection clientConnection,
            List<String> client_ids,
            LocalDateTime ldt_startDate,
            LocalDateTime ldt_endDate,
            JSONObject paramFilters)
            throws Exception
    {
        final String condition   = generateFilterCondition(paramFilters, CommonVariables.MARIA_DB);
        final String startDate   = Utility.formatDateTime(ldt_startDate);
        final String endDate     = Utility.formatDateTime(ldt_endDate);
        final String cli_id_list = String.join(",", client_ids);

        final String tableSuffix = Utility.formatDateTime(ldt_startDate, Utility.DATE_YYYYMMDD);
        final String sub_del_tbl = "smslog." + CommonVariables.PG_DB_SUB_DEL_TABLE_PREFIX + "_" + tableSuffix;
        final String fm_tbl      = "smslog." + CommonVariables.PG_DB_FMSG_TABLE_PREFIX + "_" + tableSuffix;

        final String sql         = "select sub_base_msg_id, sub_total_msg_parts, sub_msg_part_no, sub_sub_ori_sts_code, "
                + "(case when del_dn_ori_sts_code is null then '' else del_dn_ori_sts_code end) as del_dn_ori_sts_code "
                + " FROM " + sub_del_tbl + " WHERE recv_time >= '%s' AND recv_time < '%s' "
                + " AND cli_id in (%s) AND sub_total_msg_parts > 1  %s";
        final String ssql        = String.format(sql, startDate, endDate, cli_id_list, condition);

        log.info(ssql);

        return getQueryRecords(clientConnection, ssql);
    }

    @SuppressWarnings("unchecked")
    private static String generateFilterCondition(
            JSONObject paramFilters,
            String dbType)
            throws Exception
    {
        final StringBuilder           str          = new StringBuilder("");
        final ConnectionPoolSingleton conSingleton = ConnectionPoolSingleton.getInstance();

        if (paramFilters != null)
            paramFilters.keySet().forEach(keyStr -> {
                str.append(" AND ").append(" (");

                final JSONArray keyconditions  = (JSONArray) paramFilters.get(keyStr);
                final int       filterKeyCount = keyconditions.size();
                int             filterKeyIdx   = 0;

                for (final Object kCondition : keyconditions)
                {
                    filterKeyIdx++;
                    final JSONObject jsonFilter = (JSONObject) kCondition;
                    final String     fieldName  = jsonFilter.get("field").toString().trim();
                    String           operator   = jsonFilter.get("op").toString().toUpperCase();
                    String           value      = null;

                    // processing for not in and in operator
                    if (operator.trim().equals("IN") || operator.trim().equals("NOT IN")
                            || operator.trim().equals("NOT_IN"))
                    {
                        final JSONArray     jaInValue = (JSONArray) jsonFilter.get("val");
                        final StringBuilder inVal     = new StringBuilder("");
                        // final int inSize = jaInValue.size();
                        int                 inIdx     = 0;

                        for (final Object objVal : jaInValue)
                        {
                            inIdx++;
                            if (inIdx > 1)
                                inVal.append(", ");

                            if (objVal instanceof Number)
                                inVal.append(objVal.toString());
                            else
                                inVal.append("'").append(objVal.toString()).append("'");
                        }
                        value = "(" + inVal.toString() + ")";

                        if (operator.trim().equals("NOT_IN"))
                            operator = "NOT IN";
                    }
                    else
                    {
                        final Object objVal = jsonFilter.get("val");
                        if (objVal instanceof Number)
                            value = jsonFilter.get("val").toString();
                        else
                            value = "'" + jsonFilter.get("val").toString() + "'";
                    }

                    if (dbType.equals(CommonVariables.MARIA_DB))
                        str.append(String.format("%s %s %s", fieldName, operator, value));
                    else
                    {
                        final String paramColName = fieldName;
                        String       strpgColName = null;
                        if (paramColName.startsWith("s."))
                            strpgColName = conSingleton.hmMapSubPG_MYSQL.get(paramColName.replace("s.", ""));
                        else
                            if (paramColName.startsWith("d."))
                                strpgColName = conSingleton.hmMapDelPG_MYSQL.get(paramColName.replace("d.", ""));

                        if ((strpgColName == null) || "".equals(strpgColName))
                            strpgColName = paramColName.replace("s.", "").replace("d.", "");
                        // strpgColName = paramColName.replace("d.", "s.");

                        strpgColName = "s." + strpgColName;

                        str.append(String.format("%s %s %s", strpgColName, operator, value));
                    }

                    if (filterKeyIdx < filterKeyCount)
                        str.append(" ").append(keyStr).append(" ");
                }

                str.append(")");
            });

        return str.toString();
    }

    public static ResultSet getQueryRequestInfo(
            Connection clientConnection,
            String queue_id)
    {
        log.info(String.format("Fetching request parameter info - QUEUE ID : %s", queue_id));

        final String ssql = String.format("SELECT * FROM query_async_queue_req_info WHERE queue_id = '%s' ", queue_id);

        return getQueryRecords(clientConnection, ssql);
    }

    @SuppressWarnings("resource")
    public static ResultSet getQueueStatusInfo(
            Connection clientConnection,
            String queue_id)
            throws Exception
    {
        log.info(String.format("Fetching Queue Status info - QUEUE ID: %s", queue_id));

        final String            ssql  = "SELECT current_status as status, "
                + "DATE_FORMAT(requested_ts, '%Y-%m-%d %H:%i:%s') as requested_ts, "
                + "DATE_FORMAT(started_ts, '%Y-%m-%d %H:%i:%s') as started_ts, "
                + "DATE_FORMAT(completed_ts, '%Y-%m-%d %H:%i:%s') as completed_ts, "
                + "DATE_FORMAT(created_ts, '%Y-%m-%d %H:%i:%s') as created_ts, "
                + "query_type,record_count FROM query_async_queue WHERE queue_id = ?";
        final PreparedStatement pstmt = clientConnection.prepareStatement(ssql);
        pstmt.setString(1, queue_id);
        final ResultSet rs = pstmt.executeQuery();
        return rs;
    }

    public static ResultSet getQueueStatusList(
            Connection clientConnection,
            int pageNo)
    {
        final String ssql = "select * from " + "( SELECT queue_id, current_status as status, "
                + "DATE_FORMAT(requested_ts, '%Y-%m-%d %H:%i:%s') as requested_ts, "
                + "DATE_FORMAT(started_ts, '%Y-%m-%d %H:%i:%s') as started_ts, "
                + "DATE_FORMAT(completed_ts, '%Y-%m-%d %H:%i:%s') as completed_ts, "
                + "DATE_FORMAT(created_ts, '%Y-%m-%d %H:%i:%s') as created_ts, "
                + "query_type,record_count FROM query_async_queue) as qt limit 20 Offset " + ((pageNo - 1) * 20);

        return getQueryRecords(clientConnection, ssql);
    }

    public static boolean logQueueProcessingInfo(
            Connection clientConnection,
            String queue_id,
            String statusMessage,
            String status_type)
    {

        try
        {
            final PreparedStatement pstmt = clientConnection.prepareStatement(
                    "INSERT INTO `query_async_queue_exec_log`(`queue_id`,`log_ts`,`log_message`,`log_type`)"
                            + " VALUES(?, now(), ?, ?)");

            pstmt.setString(1, queue_id);
            pstmt.setString(2, statusMessage);
            pstmt.setString(3, status_type);
            pstmt.executeUpdate();
            return true;
        }
        catch (final Exception ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();
            return false;
        }
    }

    public static ResultSet getPendingQueue(
            Connection clientConnection)
    {
        // open queue table with lock to read and update data
        final String ssql = "SELECT queue_id FROM query_async_queue WHERE current_status = 'QUEUED' "
                + "ORDER BY requested_ts  LIMIT 1 OFFSET 0 FOR UPDATE ";

        return getQueryRecordsForUpdate(clientConnection, ssql);
    }

    public static void updateQueueStatus(
            Connection clientConnection,
            String queue_id,
            String status,
            int recordCount,
            String completionStatus)
            throws Exception
    {
        final String            updSQL = "update query_async_queue "
                + " set record_count=?, current_status=?, completed_status=?, completed_ts=now(), modified_ts=now() "
                + " where queue_id = ?";
        final PreparedStatement pstmt  = clientConnection.prepareStatement(updSQL);
        pstmt.setInt(1, recordCount);
        pstmt.setString(2, status);
        pstmt.setString(3, completionStatus);
        pstmt.setString(4, queue_id);
        pstmt.executeUpdate();
        pstmt.close();
        clientConnection.commit();
    }

    public static ResultSet getJndiInfo(
            Connection clientConnection,
            List<Long> client_ids)
    {
        final String ssql = String.format(
                "SELECT distinct blm.jndi_id, ji.* FROM bill_log_map blm "
                        + "INNER JOIN jndi_info ji on ji.id = blm.jndi_id " + "WHERE blm.id in(%s) ",
                Utility.getCSV(client_ids));

        return getQueryRecords(clientConnection, ssql);
    }

    public String CreateQueue(
            Connection masterDBConn,
            String r_params,
            String r_app,
            String r_app_version,
            String r_username,
            String r_host,
            String r_queue_status)
    {

        try
        {
            final String            queue_id  = UUID.randomUUID().toString();
            // create main queue log
            final PreparedStatement que_pstmt = masterDBConn
                    .prepareStatement("INSERT INTO query_async_queue (queue_id, query_type, requested_ts, "
                            + "current_status, created_ts, modified_ts, record_count) "
                            + "VALUES (?, ?, NOW(), ?, NOW() , NOW(), 0 )", Statement.RETURN_GENERATED_KEYS);

            que_pstmt.setString(1, queue_id);
            que_pstmt.setString(2, CommonVariables.QUERY_TYPE);
            que_pstmt.setString(3, r_queue_status);
            que_pstmt.executeUpdate();

            // get last generated key
            final ResultSet rs = que_pstmt.getGeneratedKeys();

            if (rs.next())
            {
                final PreparedStatement req_pstmt = masterDBConn.prepareStatement(
                        "INSERT INTO query_async_queue_req_info(`queue_id`,`query_parameters`,`requested_app`,"
                                + "`requested_app_version`,`requested_username`,`requested_host`,`created_ts`,`modified_ts`) "
                                + "VALUES (?, ?, ?, ?, ?, ?, NOW() , NOW() )");

                req_pstmt.setString(1, queue_id);
                req_pstmt.setString(2, r_params);
                req_pstmt.setString(3, r_app);
                req_pstmt.setString(4, r_app_version);
                req_pstmt.setString(5, r_username);
                req_pstmt.setString(6, r_host);

                req_pstmt.executeUpdate();
                req_pstmt.close();

                logQueueProcessingInfo(masterDBConn, queue_id, "Queue Created", CommonVariables.INFO);
            }

            rs.close();
            que_pstmt.close();
            masterDBConn.commit();

            return queue_id;
        }
        catch (final SQLException e)
        {
            //
            log.error(e.getMessage(), e);
            return null;
        }
    }

}
