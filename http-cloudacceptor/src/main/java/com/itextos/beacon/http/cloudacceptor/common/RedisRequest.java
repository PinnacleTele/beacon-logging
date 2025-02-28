package com.itextos.beacon.http.cloudacceptor.common;

import java.util.StringJoiner;

import com.itextos.beacon.http.clouddatautil.common.Constants;

public class RedisRequest
{

    private String fileId;
    private String clientId;
    private String clientIp;
    private String receivedTime;
    private String reqType;
    private String actualReq;

    public String getFileId()
    {
        return fileId;
    }

    public void setFileId(
            String aFileId)
    {
        fileId = aFileId;
    }

    public String getClientId()
    {
        return clientId;
    }

    public void setClientId(
            String aClientId)
    {
        clientId = aClientId;
    }

    public String getClientIp()
    {
        return clientIp;
    }

    public void setClientIp(
            String aClientIp)
    {
        clientIp = aClientIp;
    }

    public String getReceivedTime()
    {
        return receivedTime;
    }

    public void setReceivedTime(
            String aReceivedTime)
    {
        receivedTime = aReceivedTime;
    }

    public String getReqType()
    {
        return reqType;
    }

    public void setReqType(
            String aReqType)
    {
        reqType = aReqType;
    }

    public String getActualReq()
    {
        return actualReq;
    }

    public void setActualReq(
            String aActualReq)
    {
        actualReq = aActualReq;
    }

    public String getRedisRequest()
    {
        return new StringJoiner(Constants.CONCATE_STRING).add(fileId).add(clientId).add(clientIp).add(receivedTime).add(reqType).add(actualReq).toString();
    }

    @Override
    public String toString()
    {
        return "RedisRequest [fileId=" + fileId + ", clientId=" + clientId + ", clientIp=" + clientIp + ", receivedTime=" + receivedTime + ", reqType=" + reqType + ", actualReq=" + actualReq + "]";
    }

}
