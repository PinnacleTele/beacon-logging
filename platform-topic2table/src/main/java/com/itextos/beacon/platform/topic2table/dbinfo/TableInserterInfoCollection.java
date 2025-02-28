package com.itextos.beacon.platform.topic2table.dbinfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class TableInserterInfoCollection
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                           log                              = LogFactory.getLog(TableInserterInfoCollection.class);
    private static final int                           COL_INDEX_TABLE_INSERTER_ID      = 1;
    private static final int                           COL_INDEX_JNDIINFO_NAME          = 2;
    private static final int                           COL_INDEX_DATABASE_NAME          = 3;
    private static final int                           COL_INDEX_TABLE_NAME             = 4;
    private static final int                           COL_INDEX_TABLE_NAME_FINDER      = 5;
    private static final int                           COL_INDEX_TABLE_NAME_FINDER_KEYS = 6;
    private static final int                           COL_INDEX_REPLACE_IGNORE_COLUMNS = 7;
    private static final int                           COL_INDEX_SLEEP_SECS             = 8;
    private static final int                           COL_INDEX_BATCH_SIZE             = 9;

    private Map<Table2DBInserterId, TableInserterInfo> tableInserterMap                 = new EnumMap<>(Table2DBInserterId.class);

    public TableInserterInfoCollection(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public TableInserterInfo getTableInserterInfo(
            Table2DBInserterId aTableInserterId)
    {
        return tableInserterMap.get(aTableInserterId);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<Table2DBInserterId, TableInserterInfo> tempTableInserterMap = new HashMap<>();

        while (aResultSet.next())
        {
            final Table2DBInserterId lTableInserterId = Table2DBInserterId.getTableInserterId(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_TABLE_INSERTER_ID), true));

            if (lTableInserterId == null)
            {
                log.error("Unable to find the table inserter id for '" + aResultSet.getString(COL_INDEX_TABLE_INSERTER_ID) + "'");
                continue;
            }
            final TableInserterInfo tii = new TableInserterInfo(lTableInserterId, //
                    aResultSet.getInt(COL_INDEX_JNDIINFO_NAME), //
                    CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_DATABASE_NAME), true), //
                    CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_TABLE_NAME), true), //
                    CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_TABLE_NAME_FINDER), true), //
                    CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_TABLE_NAME_FINDER_KEYS), true), //
                    CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_REPLACE_IGNORE_COLUMNS), true), //
                    aResultSet.getInt(COL_INDEX_SLEEP_SECS), //
                    aResultSet.getInt(COL_INDEX_BATCH_SIZE));
            tempTableInserterMap.put(tii.getTableInserterId(), tii);
        }
        if (tempTableInserterMap.size() > 0)
            tableInserterMap = tempTableInserterMap;
    }

}