package com.itextos.beacon.commonlib.messageprocessor.request;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;

public class ProducerKafkaRequest
{

    private final Component       mFromComponent;
    private final Component       mNextComponent;
    private final ClusterType     mPlatformCluster;
    private final InterfaceGroup  mInterfaceGroup;
    private final MessageType     mMessageType;
    private final MessagePriority mMessagePriority;
    private final boolean         mIntlFlag;
    private final String          mClientId;
    private final boolean         isClientSpecific;

    public ProducerKafkaRequest(
            Component aFromComponent,
            Component aNextComponent,
            ClusterType aPlatformCluster,
            InterfaceGroup aInterfaceGroup,
            MessageType aMessageType,
            MessagePriority aMessagePriority,
            boolean aIntlFlag,
            String aClientId)
    {
        mFromComponent = aFromComponent;
        mNextComponent    = aNextComponent;
        mPlatformCluster  = aPlatformCluster;
        mInterfaceGroup   = aInterfaceGroup;
        mMessageType      = aMessageType;
        mMessagePriority  = aMessagePriority;
        mIntlFlag         = aIntlFlag;
        mClientId         = aClientId;

        isClientSpecific  = (aClientId != null) && (!mClientId.isBlank());
    }

    public Component getFromComponent()
    {
        return mFromComponent;
    }

    public Component getNextComponent()
    {
        return mNextComponent;
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

    public boolean isPriorityTopic()
    {
        return (mPlatformCluster != null) || (mInterfaceGroup != null) || (mMessageType != null) || (mMessagePriority != null);
    }

    @Override
    public String toString()
    {
        return "ProducerKafkaRequest [mFromComponent=" + mFromComponent + ", mNextComponent=" + mNextComponent + ", mPlatformCluster=" + mPlatformCluster + ", mInterfaceGroup=" + mInterfaceGroup
                + ", mMessageType=" + mMessageType + ", mMessagePriority=" + mMessagePriority + ", mIntlFlag=" + mIntlFlag + ", mClientId=" + mClientId + ", isClientSpecific=" + isClientSpecific
                + "]";
    }

}