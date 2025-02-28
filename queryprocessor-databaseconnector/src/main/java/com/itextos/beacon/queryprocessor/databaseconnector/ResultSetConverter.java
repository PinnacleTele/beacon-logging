package com.itextos.beacon.queryprocessor.databaseconnector;

import java.io.BufferedWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.MessageConvertionUtility;
import com.itextos.beacon.queryprocessor.commonutils.CommonVariables;
import com.itextos.beacon.queryprocessor.commonutils.Utility;

public class ResultSetConverter
{

    private static final Log       log      = LogFactory.getLog(SqlLiteConnectionProvider.class);
    static ConnectionPoolSingleton connPool = null;

    @SuppressWarnings("unchecked")
    public static JSONArray ConvertRsToJSONArray(
            ResultSet rs,
            List<String> lstJSONCols,
            String timeZone)
            throws Exception
    {
        if (rs.isClosed())
            return null;

        final JSONArray ja       = new JSONArray();
        int             rowCount = 0;

        while (true)
        {
            final JSONObject obj = ConvertRsToJSONObject(rs, lstJSONCols, timeZone);
            if (obj == null)
                break;
            ja.add(obj);
            rowCount++;
        }

        if (rowCount == 0)
            return null;

        return ja;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject ConvertRsToJSONObject(
            ResultSet rs,
            List<String> lstJSONCols,
            String timeZone)
            throws Exception
    {
        if (rs.isClosed())
            return null;

        if (!rs.next())
            return null;

        final ResultSetMetaData rsmd        = rs.getMetaData();
        int                     colIdxStart = 1;
        int                     isHexMsg    = -1;
        int                     JSONColIdx  = 0;
        String                  column_name = null;
        final int               numColumns  = rsmd.getColumnCount();

        final JSONObject        obj         = new JSONObject();

        if (lstJSONCols != null)
        {
            column_name = rsmd.getColumnName(1);

            if (column_name.equals(CommonVariables.MDB_IS_HEX_MSG)
                    || column_name.equals(CommonVariables.PGDB_IS_HEX_MSG))
            {
                isHexMsg    = rs.getInt(1);

                column_name = rsmd.getColumnName(2);

                if (column_name.equals(CommonVariables.MSG_ID_COL_NAME))
                {
                    final String msgId = Utility.nullCheck(rs.getString(2), true);
                    obj.put(CommonVariables.MSG_ID_COL_NAME, msgId);
                    colIdxStart = 3;
                }
                else
                    colIdxStart = 2;
            }
        }

        for (int colIdx = colIdxStart; colIdx <= numColumns; colIdx++)
        {
            column_name = rsmd.getColumnName(colIdx);

            if (lstJSONCols == null)
                obj.put(column_name, Utility.nullCheck(rs.getString(colIdx), true));
            else
            {
                final String JSONColName = lstJSONCols.get(JSONColIdx);
                JSONColIdx++;
                final String pg_column_name = JSONColName;
                String       column_type    = "text";

                column_type = connPool.hmMapCol_DataType.get(pg_column_name);

                if (pg_column_name.equals(CommonVariables.PGDB_MSG_COL_NAME))
                {
                    String column_value = Utility.nullCheck(rs.getString(colIdx), true);

                    if (!"".equals(column_value))
                        if (isHexMsg == 1)
                            try
                            {
                                final String utf_value = MessageConvertionUtility.convertHex2String(column_value);
                                column_value = utf_value;
                            }
                            catch (final Exception ex)
                            {
                                log.error("Hex Char Conversion error", ex);
                            }
                    obj.put(JSONColName, column_value);
                }
                else
                    if (column_type.equals(CommonVariables.COL_DATETIME_TYPE))
                    {
                        String                   strValue = "";
                        final java.sql.Timestamp tsValue  = rs.getTimestamp(colIdx);

                        if (!rs.wasNull() && (tsValue != null))
                        {
                            final Date dtValue = new java.util.Date(tsValue.getTime());
                            if ("".equals(timeZone))
                                strValue = DateTimeUtility.getFormattedDateTime(dtValue, DateTimeFormat.DEFAULT);
                            else
                                strValue = DateTimeUtility.getFormattedDateTime(dtValue, DateTimeFormat.DEFAULT,
                                        TimeZone.getTimeZone(timeZone));
                        }
                        obj.put(JSONColName, strValue);
                    }
                    else
                        obj.put(JSONColName, Utility.nullCheck(rs.getString(colIdx), true));
            }
        }

        return obj;
    }

    @SuppressWarnings("unchecked")
    public static String ConvertRsToLogCSVRecord(
            ResultSet rs,
            String timeZoneName,
            HashSet<String> hsMsgId)
            throws Exception
    {
        if (!rs.next())
            return null;

        final String num_format = "%." + CommonVariables.MDB_SMS_RATE_DECIMALS + "f";

        final String msgId      = rs.getString(2);
        if (hsMsgId.contains(msgId))
            return CommonVariables.DUP_MSGID_FOUND;
        hsMsgId.add(msgId);

        final ResultSetMetaData rsmd      = rs.getMetaData();
        final int               col_count = rsmd.getColumnCount();
        final StringBuilder     sbRecord  = new StringBuilder();
        final int               isHexMsg  = rs.getInt(1);

        final String            cliId     = Utility.nullCheck(rs.getString(3), true);
        final String            user      = Utility.nullCheck(connPool.hmCliUserMap.get(cliId), true);
        String                  bill_curr = null;
        sbRecord.append(user);

        for (int ci = 4; ci <= col_count; ci++)
        {
            String colValue = Utility.nullCheck(rs.getString(ci), true);

            if (!"".equals(colValue))
            {
                final String colName = rsmd.getColumnName(ci);
                final String colType = connPool.hmLog_DL_Col_DataType.get(colName);

                if (colName.equals(CommonVariables.MDB_MSG_COL_NAME))
                {
                    if (isHexMsg == 1)
                        try
                        {
                            final String utf_value = MessageConvertionUtility.convertHex2String(colValue);
                            colValue = utf_value;
                        }
                        catch (final Exception ex)
                        {
                            log.error("Hex Char Conversion error", ex);
                        }
                    // colValue = colValue.replaceAll("\"", "\"\"").replace("\\",
                    // "\\\\").replace("“", "\\“");
                    colValue = colValue.replaceAll("\"", "\"\"");
                    colValue = "\"" + colValue + "\"";
                }
                else
                    if (colName.equals(CommonVariables.MDB_BILL_CURR_COL_NAME))
                    {
                        bill_curr = colValue;
                        continue;
                    }
                    else
                        if (colName.equals(CommonVariables.MDB_SMS_RATE_COL_NAME)
                                || colName.equals(CommonVariables.MDB_DLT_RATE_COL_NAME))
                        {
                            final String strValue = String.format(num_format, Double.parseDouble(colValue));
                            colValue = strValue + " " + bill_curr;
                        }
                        else
                            if (colType.equals(CommonVariables.COL_DATETIME_TYPE))
                                if (!"".equals(timeZoneName))
                                {
                                    final Date dtValue = DateTimeUtility.getDateFromString(colValue,
                                            DateTimeFormat.DEFAULT, TimeZone.getTimeZone(timeZoneName));

                                    colValue = DateTimeUtility.getFormattedDateTime(dtValue, DateTimeFormat.DEFAULT);
                                }
            }
            sbRecord.append(",");
            sbRecord.append(colValue);
        }
        return sbRecord.toString();
    }

    @SuppressWarnings("unchecked")
    public static int writeRStoLogCSVFile(
            ResultSet rs,
            BufferedWriter bwCSV,
            String timeZoneName)
            throws Exception
    {
        if (rs.isClosed())
            return -1;

        int                   row_count = 0;
        final HashSet<String> hsMsgId   = new HashSet<>();

        while (true)
        {
            final String csvRecord = ConvertRsToLogCSVRecord(rs, timeZoneName, hsMsgId);
            if (csvRecord == null)
                break;
            else
                if (csvRecord.equals(CommonVariables.DUP_MSGID_FOUND))
                    continue;
            bwCSV.write(csvRecord);
            bwCSV.newLine();
            row_count++;
        }
        return row_count;
    }

}
