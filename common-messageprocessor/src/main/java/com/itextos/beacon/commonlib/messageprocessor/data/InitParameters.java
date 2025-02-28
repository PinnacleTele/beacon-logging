package com.itextos.beacon.commonlib.messageprocessor.data;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class InitParameters
{

    private final ClusterType     mPlatformCluster;
    private final InterfaceGroup  mInterfaceGroup;
    private final MessageType     mMessageType;
    private final MessagePriority mMessagePriority;
    private final boolean         mIntlFlag;
    private final String          mClientId;

    private final boolean         isClientSpecific;

    public InitParameters(
            ClusterType aPlatformCluster,
            InterfaceGroup aInterfaceGroup,
            MessageType aMessageType,
            MessagePriority aMessagePriority,
            boolean aIntlFlag,
            String aClientId)
    {
        super();
        mPlatformCluster = aPlatformCluster;
        mInterfaceGroup  = aInterfaceGroup;
        mMessageType     = aMessageType;
        mMessagePriority = aMessagePriority;
        mIntlFlag        = aIntlFlag;
        mClientId        = CommonUtility.nullCheck(aClientId, true);

        isClientSpecific = (!mClientId.isEmpty());
    }

    public ClusterType getPlatformCluster()
    {
        return mPlatformCluster;
    }

    public InterfaceGroup getInterfaceGroup()
    {
        return mInterfaceGroup;
    }

    public MessageType getMessageType()
    {
        return mMessageType;
    }

    public MessagePriority getMessagePriority()
    {
        return mMessagePriority;
    }

    public boolean isIntlFlag()
    {
        return mIntlFlag;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public boolean isClientSpecific()
    {
        return isClientSpecific;
    }

    @Override
    public String toString()
    {
        return "InitParameters [mPlatformCluster=" + mPlatformCluster + ", mInterfaceGroup=" + mInterfaceGroup + ", mMessageType=" + mMessageType + ", mMessagePriority=" + mMessagePriority
                + ", mIntlFlag=" + mIntlFlag + ", mClientId=" + mClientId + ", isClientSpecific=" + isClientSpecific + "]";
    }

}