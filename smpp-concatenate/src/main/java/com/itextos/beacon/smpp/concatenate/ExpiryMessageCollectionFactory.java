package com.itextos.beacon.smpp.concatenate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class ExpiryMessageCollectionFactory
{

    private static final Log log = LogFactory.getLog(ExpiryMessageCollectionFactory.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final ExpiryMessageCollectionFactory INSTANCE = new ExpiryMessageCollectionFactory();

    }

    public static ExpiryMessageCollectionFactory getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<String, ExpiryMessageProcessor> expiryProcessorMap = new ConcurrentHashMap<>();

    private ExpiryMessageCollectionFactory()
    {}

    public void addExpiryProcessor(
            ClusterType aClusterType,
            int aRedisPoolIndex)
    {
        final String key = CommonUtility.combine(aClusterType.getKey(), Integer.toString(aRedisPoolIndex));
        expiryProcessorMap.put(key, new ExpiryMessageProcessor(aClusterType, aRedisPoolIndex));
    }

    void addMessage(
            ClusterType aClusterType,
            int aRedisPoolIndex,
            String aRefNumber)
    {
        final String                 key             = CommonUtility.combine(aClusterType.getKey(), Integer.toString(aRedisPoolIndex));
        final ExpiryMessageProcessor expiryProcessor = expiryProcessorMap.computeIfAbsent(key, k -> new ExpiryMessageProcessor(aClusterType, aRedisPoolIndex));
        expiryProcessor.addRefNumber(aRefNumber);
    }

}
