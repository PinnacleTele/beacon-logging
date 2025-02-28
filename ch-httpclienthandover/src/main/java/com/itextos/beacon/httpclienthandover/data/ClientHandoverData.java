package com.itextos.beacon.httpclienthandover.data;

import java.util.ArrayList;
import java.util.List;

public class ClientHandoverData
{

    private final long                       handoverId;
    private final long                       clientId;
    private final int                        batchSize;
    private final int                        retryExpiryLogic;
    private final int                        expiryTimeSeconds;
    private final int                        maxRetryCount;
    private final int                        retrySleepTimeMills;
    private final boolean                    logRetryAttempt;
    private final int                        threadCount;
    private final List<ClientHandoverMaster> clientHandoverMaster = new ArrayList<>();

    public ClientHandoverData(
            long aHandoverId,
            long aClientId,
            int aBatchSize,
            int aRetryExpiryLogic,
            int aExpiryTimeSeconds,
            int aMaxRetryCount,
            int aRetrySleepTimeMills,
            boolean aLogRetryAttempt,
            int aThreadCount)
    {
        super();
        handoverId          = aHandoverId;
        clientId            = aClientId;
        batchSize           = aBatchSize;
        retryExpiryLogic    = aRetryExpiryLogic;
        expiryTimeSeconds   = aExpiryTimeSeconds;
        maxRetryCount       = aMaxRetryCount;
        retrySleepTimeMills = aRetrySleepTimeMills;
        logRetryAttempt     = aLogRetryAttempt;
        threadCount         = aThreadCount;
    }

    public int getThreadCount()
    {
        return threadCount;
    }

    public long getHandoverId()
    {
        return handoverId;
    }

    public long getClientId()
    {
        return clientId;
    }

    public int getBatchSize()
    {
        return batchSize;
    }

    public int getRetryExpiryLogic()
    {
        return retryExpiryLogic;
    }

    public int getExpiryTimeSeconds()
    {
        return expiryTimeSeconds;
    }

    public int getMaxRetryCount()
    {
        return maxRetryCount;
    }

    public int getRetrySleepTimeMills()
    {
        return retrySleepTimeMills;
    }

    public void addClientHandoverMaster(
            ClientHandoverMaster aClientHandoverMasters)
    {
        clientHandoverMaster.add(aClientHandoverMasters);
    }

    public List<ClientHandoverMaster> getClientHandoverMaster()
    {
        return clientHandoverMaster;
    }

    public boolean isLogRetryAttempt()
    {
        return logRetryAttempt;
    }

    @Override
    public String toString()
    {
        return "ClientHandoverData [handoverId=" + handoverId + ", clientId=" + clientId + ", batchSize=" + batchSize + ", retryExpiryLogic=" + retryExpiryLogic + ", expiryTimeSeconds="
                + expiryTimeSeconds + ", maxRetryCount=" + maxRetryCount + ", retrySleepTimeMills=" + retrySleepTimeMills + ", logRetryAttempt=" + logRetryAttempt + ", clientHandoverMaster="
                + clientHandoverMaster + "]";
    }

}