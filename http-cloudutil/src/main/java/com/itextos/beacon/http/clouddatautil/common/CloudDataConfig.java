package com.itextos.beacon.http.clouddatautil.common;

public class CloudDataConfig
{

    private final boolean isWriteResponseFirst;
    private final String  authenticationKey;
    private final String  clientId;
    private final String  customerIPParameter;

    private final int     totalThreadsToHit;
    private final int     redisBatchSize;
    private final int     processWaitSecs;

    private final String  swapFrom;
    private final String  swapTo;
    private final String  clientIP;

    public CloudDataConfig(
            boolean aIsWriteResponseFirst,
            String aAuthenticationKey,
            String aCustomerIPParameter,
            int aTotalThreadsToHit,
            int aRedisBatchSize,
            int aProcessWaitSecs,

            String aSwapFrom,
            String aSwapTo,
            String aClientIp,
            String aClientId)
    {
        super();
        isWriteResponseFirst = aIsWriteResponseFirst;
        authenticationKey    = aAuthenticationKey;
        customerIPParameter  = aCustomerIPParameter;

        totalThreadsToHit    = aTotalThreadsToHit;
        redisBatchSize       = aRedisBatchSize;
        processWaitSecs      = aProcessWaitSecs;

        swapFrom             = aSwapFrom;
        swapTo               = aSwapTo;
        clientIP             = aClientIp;
        clientId             = aClientId;
    }

    public String getClientIP()
    {
        return clientIP;
    }

    public boolean isWriteResponseFirst()
    {
        return isWriteResponseFirst;
    }

    public String getAuthenticationKey()
    {
        return authenticationKey;
    }

    public String getCustomerIPParameter()
    {
        return customerIPParameter;
    }

    public int getTotalThreadsToHit()
    {
        return totalThreadsToHit;
    }

    public int getRedisBatchSize()
    {
        return redisBatchSize;
    }

    public int getProcessWaitSecs()
    {
        return processWaitSecs;
    }

    public String getSwapFrom()
    {
        return swapFrom;
    }

    public String getSwapTo()
    {
        return swapTo;
    }

    public String getClientId()
    {
        return clientId;
    }

    @Override
    public String toString()
    {
        return "CloudDataConfig [isWriteResponseFirst=" + isWriteResponseFirst + ", authenticationKey=" + authenticationKey + ", clientId=" + clientId + ", customerIPParameter=" + customerIPParameter
                + ", totalThreadsToHit=" + totalThreadsToHit + ", redisBatchSize=" + redisBatchSize + ", processWaitSecs=" + processWaitSecs + ", swapFrom=" + swapFrom + ", swapTo=" + swapTo
                + ", clientIP=" + clientIP + "]";
    }

}
