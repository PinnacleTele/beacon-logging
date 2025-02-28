package com.itextos.beacon.queryprocessor.databaseconnector;

public class FMSGInfoRecord
{

    public String baseMsgId;
    public int    subSuccess;
    public int    dnSuccess;
    public int    subFailed;
    public int    dnFailed;
    public int    totalMsgParts;
    public int    msgPartCount;
    public double smsRate;
    public double dltRate;

    public FMSGInfoRecord(
            String pBaseMsgId,
            int pTotalParts,
            int pSubSuccess,
            int pDnSuccess,
            int pSubFailed,
            int pDnFailed,
            double pSmsRate,
            double pDltRate)
    {
        baseMsgId     = pBaseMsgId;
        totalMsgParts = pTotalParts;
        subSuccess    = pSubSuccess;
        dnSuccess     = pDnSuccess;
        subFailed     = pSubFailed;
        dnFailed      = pDnFailed;
        smsRate       = pSmsRate;
        dltRate       = pDltRate;

        msgPartCount  = 1;
    }

}