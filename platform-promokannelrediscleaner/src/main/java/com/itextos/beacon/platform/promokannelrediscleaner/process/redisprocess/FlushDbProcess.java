package com.itextos.beacon.platform.promokannelrediscleaner.process.redisprocess;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;

public class FlushDbProcess
        extends
        AbstractRedisProcess
{

    private static final Log log = LogFactory.getLog(FlushDbProcess.class);

    public FlushDbProcess(
            ClusterType aClusterType,
            int aRedisIndex)
    {
        super(aClusterType, aRedisIndex);
    }

    @Override
    public boolean process()
    {
        final List<String> keys = getRedisKeys();

        if (keys.isEmpty())
            return false;

        deleteKeys(keys);

        return false;
    }

}