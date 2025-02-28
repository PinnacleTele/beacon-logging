package com.itextos.beacon.smpp.objects.counters;

public class ServerRequestCounter
{

    private static class SingletonHolder
    {

        public static final ServerRequestCounter INSTANCE = new ServerRequestCounter();

    }

    public static ServerRequestCounter getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private long mDeliverSm;
    private long mDeliverSmResp;
    private long mEnquireLink;
    private long mSubmitSm;
    private long mSubmitSmResp;

    private ServerRequestCounter()
    {}

    public void reset()
    {
        mSubmitSm      = 0;
        mSubmitSmResp  = 0;
        mEnquireLink   = 0;
        mDeliverSm     = 0;
        mDeliverSmResp = 0;
    }

    public long getDeliverSm()
    {
        return mDeliverSm;
    }

    public void setDeliverSm(
            long aDeliverSm)
    {
        mDeliverSm = aDeliverSm;
    }

    public long getDeliverSmResp()
    {
        return mDeliverSmResp;
    }

    public void setDeliverSmResp(
            long aDeliverSmResp)
    {
        mDeliverSmResp = aDeliverSmResp;
    }

    public long getEnquireLink()
    {
        return mEnquireLink;
    }

    public void setEnquireLink(
            long aEnquireLink)
    {
        mEnquireLink = aEnquireLink;
    }

    public long getSubmitSm()
    {
        return mSubmitSm;
    }

    public void setSubmitSm(
            long aSubmitSm)
    {
        mSubmitSm = aSubmitSm;
    }

    public long getSubmitSmResp()
    {
        return mSubmitSmResp;
    }

    public void setSubmitSmResp(
            long aSubmitSmResp)
    {
        mSubmitSmResp = aSubmitSmResp;
    }

    @Override
    public String toString()
    {
        return "ServerRequestCounter [mDeliverSm=" + mDeliverSm + ", mDeliverSmResp=" + mDeliverSmResp + ", mEnquireLink=" + mEnquireLink + ", mSubmitSm=" + mSubmitSm + ", mSubmitSmResp="
                + mSubmitSmResp + "]";
    }

}