package com.itextos.beacon.commonlib.prometheusmetricsutil.smpp;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class SmppPrometheusInfo
{

    private final ClusterType mClusterType;
    private final String      mInstanceId;
    private final String      mSystemId;
    private final String      mClientIp;
    private final String      mBindType;

    public SmppPrometheusInfo(
            ClusterType aClusterType,
            String aInstanceId,
            String aSystemId,
            String aBindType)
    {
        this(aClusterType, aInstanceId, aSystemId, null, aBindType);
    }

    public SmppPrometheusInfo(
            ClusterType aClusterType,
            String aInstanceId,
            String aSystemId,
            String aClientIp,
            String aBindType)
    {
        super();
        mClusterType = aClusterType;
        mInstanceId  = CommonUtility.nullCheck(aInstanceId, true);
        mSystemId    = CommonUtility.nullCheck(aSystemId, true);
        mClientIp    = CommonUtility.nullCheck(aClientIp, true);
        mBindType    = CommonUtility.nullCheck(aBindType, true);
    }

    public ClusterType getClusterType()
    {
        return mClusterType;
    }

    public String getInstanceId()
    {
        return mInstanceId;
    }

    public String getSystemId()
    {
        return mSystemId;
    }

    public String getClientIp()
    {
        return mClientIp;
    }

    public String getBindType()
    {
        return mBindType;
    }

    @Override
    public String toString()
    {
        return "SmppPrometheusInfo [mClusterType=" + mClusterType + ", mInstanceId=" + mInstanceId + ", mSystemId=" + mSystemId + ", mClientIp=" + mClientIp + ", mBindType=" + mBindType + "]";
    }

}