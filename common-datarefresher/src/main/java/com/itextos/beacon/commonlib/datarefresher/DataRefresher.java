package com.itextos.beacon.commonlib.datarefresher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfo;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.datarefresher.dataobjects.DataOperation;
import com.itextos.beacon.commonlib.datarefresher.dataobjects.DataRefresherMasterData;
import com.itextos.beacon.commonlib.datarefresher.dataobjects.IDataRefresher;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;

public class DataRefresher
        implements
        ITimedProcess
{

    private static final Log    log                            = LogFactory.getLog(DataRefresher.class);

    private static final String ALL_TABLES                     = "alltables";
    private static final int    COL_INDEX_SEQ_NO               = 1;
    private static final int    COL_INDEX_TABLE_NAME           = 2;
    private static final int    COL_INDEX_PRIMARY_COLUMN_VALUE = 3;
    private static final int    COL_INDEX_RECORD_STATUS        = 4;

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final DataRefresher INSTANCE = new DataRefresher();

    }

    public static DataRefresher getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final TimedProcessor                 mTimedProcessor;
    private Map<String, DataRefresherMasterData> mDataRefresherCollection = new HashMap<>();
    private boolean                              mCanContinue             = true;
    private int                                  iterateIndex             = 0;

    private DataRefresher()
    {
    	
    	
        mTimedProcessor = new TimedProcessor("DataRefresherMasterData", this, TimerIntervalConstant.DATA_REFRESHER_RELOAD_INTERVAL);
   
    	ExecutorSheduler.getInstance().addTask(mTimedProcessor, ALL_TABLES);
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {
        readData();
        return false;
    }

    private void readData()
    {
        if ((iterateIndex % 100) == 0)
            loadMasterData();

        iterateIndex++;

        final Map<String, Map<DataOperation, Integer>> lCheckForDataChange = checkForDataChange();

        if (!lCheckForDataChange.isEmpty() && log.isDebugEnabled())
        {
            StringJoiner sj = new StringJoiner("\t");
            sj.add("Table Name");
            sj.add("Inserted");
            sj.add("Updated");
            sj.add("Deleted");
            log.debug(sj.toString());

            for (final Entry<String, Map<DataOperation, Integer>> entry : lCheckForDataChange.entrySet())
            {
                sj = new StringJoiner("\t");

                final String tableName = entry.getKey();
                sj.add(tableName);
                final Map<DataOperation, Integer> counts = entry.getValue();

                Integer                           value  = counts.get(DataOperation.INSERT);
                sj.add(value == null ? "-" : (value < 0 ? "Failed" : value.intValue() + ""));

                value = counts.get(DataOperation.UPDATE);
                sj.add(value == null ? "-" : (value < 0 ? "Failed" : value.intValue() + ""));

                value = counts.get(DataOperation.DELETE);
                sj.add(value == null ? "-" : (value < 0 ? "Failed" : value.intValue() + ""));
                log.debug(sj.toString());
            }
        }
    }

    private Map<String, Map<DataOperation, Integer>> checkForDataChange()
    {
        return checkForDataChange(null);
    }

    public Map<String, Map<DataOperation, Integer>> checkForDataChange(
            String aTableName)
    {
        Map<String, Map<DataOperation, Integer>> returnValue     = new HashMap<>();

        final String                             tableNameFilter = ((aTableName == null) || ALL_TABLES.equalsIgnoreCase(aTableName)) ? "" : (" table_name ='" + aTableName + "' and ");
        final String                             SQL             = "select seq_no, table_name, primary_column_value, record_status from data_event_log where " + tableNameFilter
                + " is_processed = 0 order by created_ts desc";

        try (
                final Connection con = DBDataSourceFactory.getConnectionFromThin(JndiInfo.CONFIGURARION_DB);
                final PreparedStatement pstmt = con.prepareStatement(SQL);
                final ResultSet rs = pstmt.executeQuery();)
        {
            final Map<String, Map<DataOperation, List<String>>> updatedDataSet = new HashMap<>();
            final Map<String, List<String>>                     uniqueData     = new HashMap<>();
            final List<Long>                                    toUpdate       = new ArrayList<>();
            final Map<String, DataOperation>                    operationMap   = new HashMap<>();

            while (rs.next())
            {
                final long   seqNo     = rs.getLong(COL_INDEX_SEQ_NO);
                final String tableName = CommonUtility.nullCheck(rs.getString(COL_INDEX_TABLE_NAME), true);
                final String data      = CommonUtility.nullCheck(rs.getString(COL_INDEX_PRIMARY_COLUMN_VALUE), true);

                toUpdate.add(seqNo);

                final List<String> uniqueDataForTable = uniqueData.computeIfAbsent(tableName, k -> new ArrayList<>());

                if (!uniqueDataForTable.contains(data))
                {
                    uniqueDataForTable.add(data);

                    final Map<DataOperation, List<String>> operationData = updatedDataSet.computeIfAbsent(tableName, k -> new EnumMap<>(DataOperation.class));
                    final DataOperation                    operation     = DataOperation.getDataOperation(rs.getInt(COL_INDEX_RECORD_STATUS));
                    final List<String>                     values        = operationData.computeIfAbsent(operation, k -> new ArrayList<>());

                    operationMap.put(data, operation);

                    values.add(data);
                }
                else
                    log.error("Table : '" + tableName + "' already has the data '" + data + "' with operation " + operationMap.get(data));
            }

            returnValue = processData(updatedDataSet);

            updatProcessedData(toUpdate);
        }
        catch (final Exception e)
        {
            log.error("Exception while loading data refresh master data", e);
        }
        return returnValue;
    }

    private static void updatProcessedData(
            List<Long> aToUpdate)
    {
        final String SQL = "update data_event_log set is_processed = 1 where seq_no = ?";

        try (
                final Connection con = DBDataSourceFactory.getConnectionFromThin(JndiInfo.CONFIGURARION_DB);
                final PreparedStatement pstmt = con.prepareStatement(SQL);)
        {

            for (final Long seqNo : aToUpdate)
            {
                pstmt.setLong(1, seqNo);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
        catch (final Exception e)
        {
            log.error("Exception while updating processed data", e);
        }
    }

    private Map<String, Map<DataOperation, Integer>> processData(
            Map<String, Map<DataOperation, List<String>>> aUpdatedDataSet)
    {
        final Map<String, Map<DataOperation, Integer>> processResultForAll = new HashMap<>();
        for (final Entry<String, Map<DataOperation, List<String>>> entry : aUpdatedDataSet.entrySet())
            try
            {
                final String                  tableName                = entry.getKey();
                final DataRefresherMasterData lDataRefresherMasterData = mDataRefresherCollection.get(tableName);
                final IDataRefresher          idr                      = getDataRefresher(lDataRefresherMasterData);
                idr.setData(entry.getValue());
                final Map<DataOperation, Integer> processResult = idr.process();
                processResultForAll.put(tableName, processResult);
            }
            catch (final Exception e)
            {
                log.error("Exception while loading the data into Redis", e);
            }
        return processResultForAll;
    }

    private static IDataRefresher getDataRefresher(
            DataRefresherMasterData aDataRefresherMasterData)
            throws Exception
    {
        final Class<?> classObj = Class.forName(aDataRefresherMasterData.getProcessClassName());
        return (IDataRefresher) classObj.getDeclaredConstructor().newInstance();
    }

    private void loadMasterData()
    {
        final String SQL = "select * from data_refresher";

        try (
                Connection con = DBDataSourceFactory.getConnectionFromThin(JndiInfo.CONFIGURARION_DB);
                PreparedStatement pstmt = con.prepareStatement(SQL);
                ResultSet rs = pstmt.executeQuery();)
        {
            final Map<String, DataRefresherMasterData> tempDataRefresherCollection = new HashMap<>();

            while (rs.next())
            {
                final DataRefresherMasterData dataRefresher = new DataRefresherMasterData(CommonUtility.nullCheck(rs.getString(1), true), CommonUtility.nullCheck(rs.getString(2), true),
                        CommonUtility.nullCheck(rs.getString(3), true));
                tempDataRefresherCollection.put(dataRefresher.getTableName(), dataRefresher);
            }
            if (tempDataRefresherCollection.size() > 0)
                mDataRefresherCollection = tempDataRefresherCollection;
        }
        catch (final Exception e)
        {
            log.error("Exception while loading data refresh master data", e);
        }
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
    }

}