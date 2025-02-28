package com.itextos.beacon.commonlib.messageidentifier;

import java.util.Objects;

class MessageIdUsedInfo
        implements
        Comparable<MessageIdUsedInfo>
{

    private final String appInstanceID;
    private final String interfaceType;
    private final String usedIP;
    private final String allocatedTime;
    private final String lastUsedTime;
    private final long   lastUsedTimeinLong;

    MessageIdUsedInfo(
            final String aAppInstanceID,
            final String aInterfaceType,
            final String aUsedIP,
            final String aAllocatedTime,
            final String aLastUsedTime,
            final long aLastUsedTimeinLong)
    {
        super();
        interfaceType      = aInterfaceType;
        appInstanceID      = aAppInstanceID;
        usedIP             = aUsedIP;
        allocatedTime      = aAllocatedTime;
        lastUsedTime       = aLastUsedTime;
        lastUsedTimeinLong = aLastUsedTimeinLong;
    }

    @Override
    public int compareTo(
            final MessageIdUsedInfo aO)
    {
        if (aO == null)
            return -1;

        if (aO == this)
            return 0;

        if (aO.lastUsedTimeinLong < lastUsedTimeinLong)
            return 1;
        return -1;
    }

    String getAppInstanceID()
    {
        return appInstanceID;
    }

    String getInterfaceType()
    {
        return interfaceType;
    }

    String getUsedIP()
    {
        return usedIP;
    }

    String getAllocatedTime()
    {
        return allocatedTime;
    }

    String getLastUsedTime()
    {
        return lastUsedTime;
    }

    long getLastUsedTimeinLong()
    {
        return lastUsedTimeinLong;
    }

    @Override
    public int hashCode()
    {
        final int prime  = 31;
        int       result = 1;
        result = (prime * result) + ((allocatedTime == null) ? 0 : allocatedTime.hashCode());
        result = (prime * result) + ((appInstanceID == null) ? 0 : appInstanceID.hashCode());
        result = (prime * result) + ((interfaceType == null) ? 0 : interfaceType.hashCode());
        result = (prime * result) + ((lastUsedTime == null) ? 0 : lastUsedTime.hashCode());
        result = (prime * result) + (int) (lastUsedTimeinLong ^ (lastUsedTimeinLong >>> 32));
        result = (prime * result) + ((usedIP == null) ? 0 : usedIP.hashCode());
        return result;
    }

    @Override
    public boolean equals(
            final Object obj)
    {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;
        final MessageIdUsedInfo other = (MessageIdUsedInfo) obj;

        if (!Objects.equals(allocatedTime, other.allocatedTime))
            return false;

        if (!Objects.equals(appInstanceID, other.appInstanceID))
            return false;

        if (!Objects.equals(interfaceType, other.interfaceType))
            return false;

        if (!Objects.equals(lastUsedTime, other.lastUsedTime))
            return false;

        if (lastUsedTimeinLong != other.lastUsedTimeinLong)
            return false;

        if (!Objects.equals(usedIP, other.usedIP))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "MessageIdUsedInfo [appInstanceID=" + appInstanceID + ", interfaceType=" + interfaceType + ", usedIP=" + usedIP + ", allocatedTime=" + allocatedTime + ", lastUsedTime=" + lastUsedTime
                + ", lastUsedTimeinLong=" + lastUsedTimeinLong + "]";
    }

}
