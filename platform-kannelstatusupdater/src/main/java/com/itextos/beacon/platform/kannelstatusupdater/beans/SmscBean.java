package com.itextos.beacon.platform.kannelstatusupdater.beans;

public class SmscBean
{

    private String id;
    private String ip;
    private String alive;
    private String bindtype;
    private long   sms;
    private String smshuman;
    private long   dlr;
    private String dlrhuman;
    private long   queued;
    private String queuedhuman;
    private long   failed;
    private String failedhuman;
    private int    portRX;
    private int    portTX;
    private String status;
    private String systemType;
    private String username;

    public String getId()
    {
        return id;
    }

    public void setId(
            String aId)
    {
        id = aId;
    }

    public String getIp()
    {
        return ip;
    }

    public void setIp(
            String aIp)
    {
        ip = aIp;
    }

    public String getAlive()
    {
        return alive;
    }

    public void setAlive(
            String aAlive)
    {
        alive = aAlive;
    }

    public String getBindtype()
    {
        return bindtype;
    }

    public void setBindtype(
            String aBindtype)
    {
        bindtype = aBindtype;
    }

    public long getSms()
    {
        return sms;
    }

    public void setSms(
            long aSms)
    {
        sms = aSms;
    }

    public String getSmshuman()
    {
        return smshuman;
    }

    public void setSmshuman(
            String aSmshuman)
    {
        smshuman = aSmshuman;
    }

    public long getDlr()
    {
        return dlr;
    }

    public void setDlr(
            long aDlr)
    {
        dlr = aDlr;
    }

    public String getDlrhuman()
    {
        return dlrhuman;
    }

    public void setDlrhuman(
            String aDlrhuman)
    {
        dlrhuman = aDlrhuman;
    }

    public long getQueued()
    {
        return queued;
    }

    public void setQueued(
            long aQueued)
    {
        queued = aQueued;
    }

    public String getQueuedhuman()
    {
        return queuedhuman;
    }

    public void setQueuedhuman(
            String aQueuedhuman)
    {
        queuedhuman = aQueuedhuman;
    }

    public long getFailed()
    {
        return failed;
    }

    public void setFailed(
            long aFailed)
    {
        failed = aFailed;
    }

    public String getFailedhuman()
    {
        return failedhuman;
    }

    public void setFailedhuman(
            String aFailedhuman)
    {
        failedhuman = aFailedhuman;
    }

    public int getPortRX()
    {
        return portRX;
    }

    public void setPortRX(
            int aPortRX)
    {
        portRX = aPortRX;
    }

    public int getPortTX()
    {
        return portTX;
    }

    public void setPortTX(
            int aPortTX)
    {
        portTX = aPortTX;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(
            String aStatus)
    {
        status = aStatus;
    }

    public String getSystemType()
    {
        return systemType;
    }

    public void setSystemType(
            String aSystemtype)
    {
        systemType = aSystemtype;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(
            String aUsername)
    {
        username = aUsername;
    }

    public String getKey()
    {
        return ip + portTX + portRX + id;
    }

    @Override
    public String toString()
    {
        return "Smsc [id=" + id + ", ip=" + ip + ", alive=" + alive + ", bindtype=" + bindtype + ", sms=" + sms + ", smshuman=" + smshuman + ", dlr=" + dlr + ", dlrhuman=" + dlrhuman + ", queued="
                + queued + ", queuedhuman=" + queuedhuman + ", failed=" + failed + ", failedhuman=" + failedhuman + ", portRX=" + portRX + ", portTX=" + portTX + ", status=" + status + ", systemtype="
                + systemType + ", username=" + username + "]";
    }

}