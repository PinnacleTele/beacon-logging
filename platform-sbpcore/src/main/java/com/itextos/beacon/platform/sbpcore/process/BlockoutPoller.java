package com.itextos.beacon.platform.sbpcore.process;

import com.itextos.beacon.platform.sbpcore.dao.DBPoller;

public class BlockoutPoller
        extends
        AbstractDataPoller
{

    public BlockoutPoller(
            int aAppInstanceId)
    {
        super(aAppInstanceId, DBPoller.TABLE_NAME_BLOCKOUT);
    }

}