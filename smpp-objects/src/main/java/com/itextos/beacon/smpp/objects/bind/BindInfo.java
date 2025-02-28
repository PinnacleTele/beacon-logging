package com.itextos.beacon.smpp.objects.bind;

import com.cloudhopper.smpp.SmppBindType;
import com.itextos.beacon.platform.smpputil.AbstractSmppInfo;
import com.itextos.beacon.smpp.objects.SmppRequestType;

class BindInfo
        extends
        AbstractSmppInfo
{

    private final String          instanceId;
    private final String          clientId;
    private final SmppRequestType requestType;
    private final SmppBindType    bindType;
    private final String          bindId;
    private final int             serverPort;
    private final String          systemId;
    private final String          sourceIp;
    private final String          threadName;
    private int                   errorcode;
    private String                bindTime;
    private String                serverIp;
    private String                reason;
    private String                bindDate;

    BindInfo(
            String aInstanceId,
            String aClientId,
            SmppRequestType aRequestType,
            SmppBindType aBindType,
            String aBindId,
            String aServerIp,
            int aServerPort,
            String aSystemId,
            String aSourceIp,
            String aThreadName)
    {
        super();
        instanceId  = aInstanceId;
        clientId    = aClientId;
        requestType = aRequestType;
        bindType    = aBindType;
        bindId      = aBindId;
        serverIp    = aServerIp;
        serverPort  = aServerPort;
        systemId    = aSystemId;
        sourceIp    = aSourceIp;
        threadName  = aThreadName;
    }

    public int getErrorcode()
    {
        return errorcode;
    }

    public void setErrorcode(
            int aErrorcode)
    {
        errorcode = aErrorcode;
    }

    public String getInstanceId()
    {
        return instanceId;
    }

    public String getClientId()
    {
        return clientId;
    }

    public SmppRequestType getRequestType()
    {
        return requestType;
    }

    public SmppBindType getBindType()
    {
        return bindType;
    }

    public String getBindId()
    {
        return bindId;
    }

    public int getServerPort()
    {
        return serverPort;
    }

    public String getSystemId()
    {
        return systemId;
    }

    public String getSourceIp()
    {
        return sourceIp;
    }

    public String getThreadName()
    {
        return threadName;
    }

    public String getBindTime()
    {
        return bindTime;
    }

    public String getServerIp()
    {
        return serverIp;
    }

    public void setServerIp(
            String aServerIp)
    {
        serverIp = aServerIp;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(
            String aReason)
    {
        reason = aReason;
    }

    public String getBindDate()
    {
        return bindDate;
    }

    public void setBindDate(
            String aBindDate)
    {
        bindDate = aBindDate;
    }

    public void setBindTime(
            String aBindTime)
    {
        bindTime = aBindTime;
    }

    @Override
    public String toString()
    {
        return "BindInfo [instanceId=" + instanceId + ", clientId=" + clientId + ", requestType=" + requestType + ", bindType=" + bindType + ", bindId=" + bindId + ", serverPort=" + serverPort
                + ", systemId=" + systemId + ", sourceIp=" + sourceIp + ", threadName=" + threadName + ", errorcode=" + errorcode + ", bindTime=" + bindTime + ", serverIp=" + serverIp + ", reason="
                + reason + ", bindDate=" + bindDate + "]";
    }

}