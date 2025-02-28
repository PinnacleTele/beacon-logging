package com.itextos.beacon.platform.topic2table.dbinfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfo;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.topic2table.utils.T2TUtility;

public class ColumnInfoHandler
{

    private static final Log log = LogFactory.getLog(ColumnInfoHandler.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final ColumnInfoHandler INSTANCE = new ColumnInfoHandler();

    }

    public static ColumnInfoHandler getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    // Schema -> TableName -> ColumnIndex -> ColumnDetails
    private final Map<String, Map<String, Map<Integer, ColumnInfo>>> colInfoHolder = new ConcurrentHashMap<>();

    private ColumnInfoHandler()
    {}

    public Map<Integer, ColumnInfo> getColumnInfo(
            String aSchemaName,
            String aTableName)
    {
        final String schemaName = CommonUtility.nullCheck(aSchemaName, true);
        final String tableName  = CommonUtility.nullCheck(aTableName, true);

        if ("".equals(schemaName) || "".equals(tableName))
        {
            final String s = "Invalid Schema / Table specified. Schema : '" + aSchemaName + "' TableName : '" + aTableName + "'";
            log.error(s);
         //   throw new ItextosRuntimeException(s);
        }

        addTableCollectionInSchemaCollection(schemaName);

        final Map<Integer, ColumnInfo> columnInfo = getTableColumnInfoForTable(schemaName, tableName);

        if (columnInfo.isEmpty()) // (columnInfo == null) Not Required here.
        {
            final String s = "There is some problem in getting the column details from the database. Schema : '" + aSchemaName + "', Tablename : '" + aTableName + "'";
        //    throw new ItextosRuntimeException(s);
        }
        return columnInfo;
    }

    private Map<Integer, ColumnInfo> getTableColumnInfoForTable(
            String aSchemaName,
            String aTableName)
    {
        final Map<String, Map<Integer, ColumnInfo>> tableColumnInfo = colInfoHolder.get(aSchemaName); // This return value should not be null.
        Map<Integer, ColumnInfo>                    columnInfo      = tableColumnInfo.get(aTableName);

        if (columnInfo == null)
        {
            if (log.isDebugEnabled())
                log.debug("Column information is null for the Schema '" + aSchemaName + "' and table '" + aTableName + "'. Trying to get in a Synchronized block.");

            synchronized (tableColumnInfo)
            {
                columnInfo = tableColumnInfo.get(aTableName);

                if (log.isDebugEnabled())
                    log.debug("Within synchronized BLOCK Column information is " + columnInfo + " for the Schema '" + aSchemaName + "' and table '" + aTableName + "'. Trying to get it from DB.");

                if (columnInfo == null)
                {
                    columnInfo = getColumnInfoFromDB(aSchemaName, aTableName);
                    tableColumnInfo.put(aTableName, columnInfo);
                }
            }
        }
        return columnInfo;
    }

    private Map<String, Map<Integer, ColumnInfo>> addTableCollectionInSchemaCollection(
            String aSchemaName)
    {
        Map<String, Map<Integer, ColumnInfo>> tableColumnInfo = colInfoHolder.get(aSchemaName);

        if (tableColumnInfo == null)
        {
            if (log.isDebugEnabled())
                log.debug("Adding table name in the schema collection. Schema '" + aSchemaName);

            synchronized (colInfoHolder)
            {
                tableColumnInfo = colInfoHolder.get(aSchemaName);

                if (tableColumnInfo == null)
                {
                    tableColumnInfo = new HashMap<>();
                    colInfoHolder.put(aSchemaName, tableColumnInfo);
                }
            }
        }
        return tableColumnInfo;
    }

    private static Map<Integer, ColumnInfo> getColumnInfoFromDB(
            String aSchemaName,
            String aTableName)
    {
        final Map<Integer, ColumnInfo> allColumns    = new ConcurrentHashMap<>();
        ResultSet                      rsColumnNames = null;

        Connection con = null;
         PreparedStatement pstmt = null;
        try
        {
        	  con = DBDataSourceFactory.getConnectionFromThin(JndiInfo.CONFIGURARION_DB);
              pstmt = con.prepareStatement(T2TUtility.ALL_COLUMNS_QUERY);
            pstmt.setString(1, aTableName);
            pstmt.setString(2, aSchemaName);
            rsColumnNames = pstmt.executeQuery();

            while (rsColumnNames.next())
            {
                final ColumnInfo info = new ColumnInfo(rsColumnNames.getInt(1), T2TUtility.getColumnDataType(rsColumnNames.getString(3)), rsColumnNames.getString(2), rsColumnNames.getInt(4));
                allColumns.put(info.getColumnIndex(), info);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the column information from the Database for Schema : '" + aSchemaName + "', Tablename : '" + aTableName + "'", e);
            allColumns.clear();
        }
        finally
        {
            CommonUtility.closeResultSet(rsColumnNames);
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
   
        }
        return allColumns;
    }

}