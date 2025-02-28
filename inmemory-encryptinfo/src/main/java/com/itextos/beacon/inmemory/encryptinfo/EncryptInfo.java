package com.itextos.beacon.inmemory.encryptinfo;

public class EncryptInfo
{

    private final String mClientId;
    private final int    mIncomingCryptoType;
    private final String mIncomingCryptoAlogrithm;
    private final int    mBillingCryptoType;
    private final String mBillingCryptoAlgorithm;
    private final int    mBillingCryptoColumns;
    private final int    mHandoverCryptoType;
    private final String mHandoverCryptoAlgorithm;
    private String       mIncomingCryptoParam1;
    private String       mIncomingCryptoParam2;
    private String       mIncomingCryptoParam3;
    private String       mIncomingCryptoParam4;
    private String       mIncomingCryptoParam5;
    private String       mBillingCryptoParam1;
    private String       mBillingCryptoParam2;
    private String       mBillingCryptoParam3;
    private String       mBillingCryptoParam4;
    private String       mBillingCryptoParam5;
    private String       mHandoverCryptoParam1;
    private String       mHandoverCryptoParam2;
    private String       mHandoverCryptoParam3;
    private String       mHandoverCryptoParam4;
    private String       mHandoverCryptoParam5;

    public EncryptInfo(
            String aClientId,
            int aIncomingCryptoType,
            String aIncomingCryptoAlogrithm,
            int aBillingCryptoType,
            String aBillingCryptoAlgorithm,
            int aBillingCryptoColumns,
            int aHandoverCryptoType,
            String aHandoverCryptoAlgorithm)
    {
        super();
        mClientId                = aClientId;
        mIncomingCryptoType      = aIncomingCryptoType;
        mIncomingCryptoAlogrithm = aIncomingCryptoAlogrithm;
        mBillingCryptoType       = aBillingCryptoType;
        mBillingCryptoAlgorithm  = aBillingCryptoAlgorithm;
        mBillingCryptoColumns    = aBillingCryptoColumns;
        mHandoverCryptoType      = aHandoverCryptoType;
        mHandoverCryptoAlgorithm = aHandoverCryptoAlgorithm;
    }

    public String getIncomingCryptoParam1()
    {
        return mIncomingCryptoParam1;
    }

    public void setIncomingCryptoParam1(
            String aIncomingCryptoParam1)
    {
        mIncomingCryptoParam1 = aIncomingCryptoParam1;
    }

    public String getIncomingCryptoParam2()
    {
        return mIncomingCryptoParam2;
    }

    public void setIncomingCryptoParam2(
            String aIncomingCryptoParam2)
    {
        mIncomingCryptoParam2 = aIncomingCryptoParam2;
    }

    public String getIncomingCryptoParam3()
    {
        return mIncomingCryptoParam3;
    }

    public void setIncomingCryptoParam3(
            String aIncomingCryptoParam3)
    {
        mIncomingCryptoParam3 = aIncomingCryptoParam3;
    }

    public String getIncomingCryptoParam4()
    {
        return mIncomingCryptoParam4;
    }

    public void setIncomingCryptoParam4(
            String aIncomingCryptoParam4)
    {
        mIncomingCryptoParam4 = aIncomingCryptoParam4;
    }

    public String getIncomingCryptoParam5()
    {
        return mIncomingCryptoParam5;
    }

    public void setIncomingCryptoParam5(
            String aIncomingCryptoParam5)
    {
        mIncomingCryptoParam5 = aIncomingCryptoParam5;
    }

    public String getBillingCryptoParam1()
    {
        return mBillingCryptoParam1;
    }

    public void setBillingCryptoParam1(
            String aBillingCryptoParam1)
    {
        mBillingCryptoParam1 = aBillingCryptoParam1;
    }

    public String getBillingCryptoParam2()
    {
        return mBillingCryptoParam2;
    }

    public void setBillingCryptoParam2(
            String aBillingCryptoParam2)
    {
        mBillingCryptoParam2 = aBillingCryptoParam2;
    }

    public String getBillingCryptoParam3()
    {
        return mBillingCryptoParam3;
    }

    public void setBillingCryptoParam3(
            String aBillingCryptoParam3)
    {
        mBillingCryptoParam3 = aBillingCryptoParam3;
    }

    public String getBillingCryptoParam4()
    {
        return mBillingCryptoParam4;
    }

    public void setBillingCryptoParam4(
            String aBillingCryptoParam4)
    {
        mBillingCryptoParam4 = aBillingCryptoParam4;
    }

    public String getBillingCryptoParam5()
    {
        return mBillingCryptoParam5;
    }

    public void setBillingCryptoParam5(
            String aBillingCryptoParam5)
    {
        mBillingCryptoParam5 = aBillingCryptoParam5;
    }

    public String getHandoverCryptoParam1()
    {
        return mHandoverCryptoParam1;
    }

    public void setHandoverCryptoParam1(
            String aHandoverCryptoParam1)
    {
        mHandoverCryptoParam1 = aHandoverCryptoParam1;
    }

    public String getHandoverCryptoParam2()
    {
        return mHandoverCryptoParam2;
    }

    public void setHandoverCryptoParam2(
            String aHandoverCryptoParam2)
    {
        mHandoverCryptoParam2 = aHandoverCryptoParam2;
    }

    public String getHandoverCryptoParam3()
    {
        return mHandoverCryptoParam3;
    }

    public void setHandoverCryptoParam3(
            String aHandoverCryptoParam3)
    {
        mHandoverCryptoParam3 = aHandoverCryptoParam3;
    }

    public String getHandoverCryptoParam4()
    {
        return mHandoverCryptoParam4;
    }

    public void setHandoverCryptoParam4(
            String aHandoverCryptoParam4)
    {
        mHandoverCryptoParam4 = aHandoverCryptoParam4;
    }

    public String getHandoverCryptoParam5()
    {
        return mHandoverCryptoParam5;
    }

    public void setHandoverCryptoParam5(
            String aHandoverCryptoParam5)
    {
        mHandoverCryptoParam5 = aHandoverCryptoParam5;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public int getIncomingCryptoType()
    {
        return mIncomingCryptoType;
    }

    public String getIncomingCryptoAlogrithm()
    {
        return mIncomingCryptoAlogrithm;
    }

    public int getBillingCryptoType()
    {
        return mBillingCryptoType;
    }

    public String getBillingCryptoAlgorithm()
    {
        return mBillingCryptoAlgorithm;
    }

    public int getBillingCryptoColumns()
    {
        return mBillingCryptoColumns;
    }

    public int getHandoverCryptoType()
    {
        return mHandoverCryptoType;
    }

    public String getHandoverCryptoAlgorithm()
    {
        return mHandoverCryptoAlgorithm;
    }

    @Override
    public String toString()
    {
        return "EncryptInfo [mClientId=" + mClientId + ", mIncomingCryptoType=" + mIncomingCryptoType + ", mIncomingCryptoAlogrithm=" + mIncomingCryptoAlogrithm + ", mBillingCryptoType="
                + mBillingCryptoType + ", mBillingCryptoAlgorithm=" + mBillingCryptoAlgorithm + ", mBillingCryptoColumns=" + mBillingCryptoColumns + ", mHandoverCryptoType=" + mHandoverCryptoType
                + ", mHandoverCryptoAlgorithm=" + mHandoverCryptoAlgorithm + ", mIncomingCryptoParam1=" + mIncomingCryptoParam1 + ", mIncomingCryptoParam2=" + mIncomingCryptoParam2
                + ", mIncomingCryptoParam3=" + mIncomingCryptoParam3 + ", mIncomingCryptoParam4=" + mIncomingCryptoParam4 + ", mIncomingCryptoParam5=" + mIncomingCryptoParam5
                + ", mBillingCryptoParam1=" + mBillingCryptoParam1 + ", mBillingCryptoParam2=" + mBillingCryptoParam2 + ", mBillingCryptoParam3=" + mBillingCryptoParam3 + ", mBillingCryptoParam4="
                + mBillingCryptoParam4 + ", mBillingCryptoParam5=" + mBillingCryptoParam5 + ", mHandoverCryptoParam1=" + mHandoverCryptoParam1 + ", mHandoverCryptoParam2=" + mHandoverCryptoParam2
                + ", mHandoverCryptoParam3=" + mHandoverCryptoParam3 + ", mHandoverCryptoParam4=" + mHandoverCryptoParam4 + ", mHandoverCryptoParam5=" + mHandoverCryptoParam5 + "]";
    }

}