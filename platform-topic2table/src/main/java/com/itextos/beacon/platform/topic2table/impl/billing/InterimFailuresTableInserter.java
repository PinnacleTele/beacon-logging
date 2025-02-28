package com.itextos.beacon.platform.topic2table.impl.billing;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.platform.topic2table.impl.AbstractTableInserterWrapper;

public class InterimFailuresTableInserter
        extends
        AbstractTableInserterWrapper
{

    public InterimFailuresTableInserter(
            String aThreadName,
            Component aComponent,
            ClusterType aPlatformCluster,
            String aTopicName,
            ConsumerInMemCollection aConsumerInMemCollection,
            int aSleepInMillis)
    {
        super(aThreadName, aComponent, aPlatformCluster, aTopicName, aConsumerInMemCollection, aSleepInMillis, Table2DBInserterId.INTERIM_FAILUERS);
    }

}
