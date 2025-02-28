package com.itextos.beacon.platform.topic2table.impl.common;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.platform.topic2table.impl.AbstractTableInserterWrapper;

public class DNPostLogTableInserter
        extends
        AbstractTableInserterWrapper
{

    public DNPostLogTableInserter(
            String aThreadName,
            Component aComponent,
            ClusterType aPlatformCluster,
            String aTopicName,
            ConsumerInMemCollection aConsumerInMemCollection,
            int aSleepInMillis)
    {
        super(aThreadName, aComponent, aPlatformCluster, aTopicName, aConsumerInMemCollection, aSleepInMillis, Table2DBInserterId.SMPP_POST_LOG);
    }

}
