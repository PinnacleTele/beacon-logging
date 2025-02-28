package com.itextos.beacon.platform.dnpcore.poller;

import com.itextos.beacon.commonlib.constants.ClusterType;

public class NoPayloadRetryPoller
        extends
        AbstractDataPoller
{

    public NoPayloadRetryPoller(
            ClusterType aCluster)
    {
        super(aCluster);
    }

}
