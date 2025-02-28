package com.itextos.beacon.commonlib.kafkaservice.consumer.partitionlogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;

public class PartitionInfoCollection
        implements
        ITimedProcess
{

    private static final Log    log = LogFactory.getLog(PartitionInfoCollection.class);
    private static final String SQL = "insert into kafka_partition_log (" //
            + " process_id, component, event_type, prometheus_port," //
            + " is_on_startup, topic_name, partition_no," //
            + " kafka_offset, redis_offset, event_time) values (" //
            + " ?,?,?," //
            + " ?,?,?," //
            + " ?,?,?,?)";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final PartitionInfoCollection INSTANCE = new PartitionInfoCollection();

    }

    public static PartitionInfoCollection getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private boolean                                  canContinue    = true;
    private final TimedProcessor                     mTimedProcessor;

    private final BlockingQueue<KafkaParititionInfo> mPartitionInfo = new LinkedBlockingQueue<>(5000);

    private PartitionInfoCollection()
    {
    	
        mTimedProcessor = new TimedProcessor("PartitionInfoCollection", this, TimerIntervalConstant.KAFKA_PARTITION_INFO_INSERT);
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "PartitionInfoCollection");
    }

    public void addKafkaPartition(
            KafkaParititionInfo aPartitionInfo)
    {

        try
        {
            final boolean lOffer = mPartitionInfo.offer(aPartitionInfo, 100, TimeUnit.MILLISECONDS);
            if (!lOffer)
                log.error("Unable to add the information to the inmemory queue due to timeout. " + aPartitionInfo);
        }
        catch (final InterruptedException e)
        {
            log.error("Unable to add the information to the inmemory queue. " + aPartitionInfo, e);
        }
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        insertIntoDb();
        return false;
    }

    private void insertIntoDb()
    {
        if (mPartitionInfo.isEmpty())
            return;

        try (
                Connection con = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.LOGGING.getKey()));
                PreparedStatement pstmt = con.prepareStatement(SQL);)
        {

            while (!mPartitionInfo.isEmpty())
            {
                int listSize = mPartitionInfo.size();

                if (listSize > 1000)
                    listSize = 1000;

                final List<KafkaParititionInfo> toInsert = new ArrayList<>(listSize);
                mPartitionInfo.drainTo(toInsert, listSize);

                for (final KafkaParititionInfo kpi : toInsert)
                {
                    int i = 1;
                    pstmt.setString(i++, kpi.getProcessId());
                    pstmt.setString(i++, kpi.getComponent().toString());
                    pstmt.setString(i++, kpi.getParitionType().toString());
                    pstmt.setInt(i++, kpi.getPrometheusServerPort());
                    pstmt.setInt(i++, kpi.isOnStartup() ? 1 : 0);
                    pstmt.setString(i++, kpi.getTopic());
                    pstmt.setInt(i++, kpi.getPartition());
                    pstmt.setLong(i++, kpi.getKafkaOffset());
                    pstmt.setLong(i++, kpi.getRedisOffset());
                    pstmt.setString(i++, DateTimeUtility.getFormattedDateTime(kpi.getCreatedTime(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                pstmt.clearBatch();
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while inserting Partition info data into table.", e);
        }
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}