package com.itextos.beacon.smpp.concatenate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.smpp.objects.request.SmppMessageRequest;
import com.itextos.beacon.smpp.utils.SmppApplicationParams;

public class DbInmemoryCollectionFactory
{

    private static final Log log = LogFactory.getLog(DbInmemoryCollectionFactory.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final DbInmemoryCollectionFactory INSTANCE = new DbInmemoryCollectionFactory();

    }

    public static DbInmemoryCollectionFactory getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<ClusterType, DbOperationInMemory> dbOperationMap = new ConcurrentHashMap<>();

    private DbInmemoryCollectionFactory()
    {
        startPollers(SmppApplicationParams.getInstance().getClusters());
    }

    private void startPollers(
            List<ClusterType> aClusterList)
    {
        if (aClusterList != null)
            for (final ClusterType lClusterType : aClusterList)
                dbOperationMap.put(lClusterType, new DbOperationInMemory(lClusterType));
    }

    void addMessage(
            ClusterType aClusterType,
            SmppMessageRequest aSmppMessageRequest)
            throws Exception
    {
        final DbOperationInMemory dbOperationInMem = dbOperationMap.computeIfAbsent(aClusterType, k -> new DbOperationInMemory(aClusterType));
        dbOperationInMem.addMessage(aSmppMessageRequest);
    }

}