package com.itextos.beacon.http.clouddataprocessor;

import com.google.gson.JsonObject;

class QsDataToPlatformRequest
        implements
        IDataToPlatformRequest
{

    private String mAppendCountry;
    private String mAppendCountryCode;
    private String mCustomerReference;
    private String mDcs;
    private String mDestination;
    private String mPort;
    private String mDlrRequired;
    private String mDltEntityId;
    private String mDltTemplateId;
    private String mMaxSplit;
    private String mMessage;
    private String mExpiry;
    private String mMessageTag;
    private String mMessageType;
    private String mParam1;
    private String mParam2;
    private String mParam3;
    private String mParam4;
    private String mParam5;
    private String mParam6;
    private String mParam7;
    private String mParam8;
    private String mParam9;
    private String mParam10;
    private String mScheduleTime;
    private String mHeader;
    private String mTemplateId;
    private String mTemplateValues;
    private String mUdh;
    private String mUdhi;
    private String mUrltrack;

    protected void setAppendCountry(
            String aAppendCountry)
    {
        mAppendCountry = aAppendCountry;
    }

    protected void setAppendCountryCode(
            String aAppendCountryCode)
    {
        mAppendCountryCode = aAppendCountryCode;
    }

    protected void setCustomerReference(
            String aCustomerReference)
    {
        mCustomerReference = aCustomerReference;
    }

    protected void setDcs(
            String aDcs)
    {
        mDcs = aDcs;
    }

    protected void setDestination(
            String aDestination)
    {
        mDestination = aDestination;
    }

    protected void setPort(
            String aPort)
    {
        mPort = aPort;
    }

    protected void setDlrRequired(
            String aDlrRequired)
    {
        mDlrRequired = aDlrRequired;
    }

    protected void setDltEntityId(
            String aDltEntityId)
    {
        mDltEntityId = aDltEntityId;
    }

    protected void setDltTemplateId(
            String aDltTemplateId)
    {
        mDltTemplateId = aDltTemplateId;
    }

    protected void setMaxSplit(
            String aMaxSplit)
    {
        mMaxSplit = aMaxSplit;
    }

    protected void setMessage(
            String aMessage)
    {
        mMessage = aMessage;
    }

    protected void setExpiry(
            String aExpiry)
    {
        mExpiry = aExpiry;
    }

    protected void setMessageTag(
            String aMessageTag)
    {
        mMessageTag = aMessageTag;
    }

    protected void setMessageType(
            String aMessageType)
    {
        mMessageType = aMessageType;
    }

    protected void setParam1(
            String aParam1)
    {
        mParam1 = aParam1;
    }

    protected void setParam2(
            String aParam2)
    {
        mParam2 = aParam2;
    }

    protected void setParam3(
            String aParam3)
    {
        mParam3 = aParam3;
    }

    protected void setParam4(
            String aParam4)
    {
        mParam4 = aParam4;
    }

    protected void setParam5(
            String aParam5)
    {
        mParam5 = aParam5;
    }

    protected void setParam6(
            String aParam6)
    {
        mParam6 = aParam6;
    }

    protected void setParam7(
            String aParam7)
    {
        mParam7 = aParam7;
    }

    protected void setParam8(
            String aParam8)
    {
        mParam8 = aParam8;
    }

    protected void setParam9(
            String aParam9)
    {
        mParam9 = aParam9;
    }

    protected void setParam10(
            String aParam10)
    {
        mParam10 = aParam10;
    }

    protected void setScheduleTime(
            String aScheduleTime)
    {
        mScheduleTime = aScheduleTime;
    }

    protected void setHeader(
            String aHeader)
    {
        mHeader = aHeader;
    }

    protected void setTemplateId(
            String aTemplateId)
    {
        mTemplateId = aTemplateId;
    }

    protected void setTemplateValues(
            String aTemplateValues)
    {
        mTemplateValues = aTemplateValues;
    }

    protected void setUdh(
            String aUdh)
    {
        mUdh = aUdh;
    }

    protected void setUdhi(
            String aUdhi)
    {
        mUdhi = aUdhi;
    }

    protected void setUrltrack(
            String aUrltrack)
    {
        mUrltrack = aUrltrack;
    }

    @Override
    public String toString()
    {
        return "QsDataToPlatformRequest [mAppendCountry=" + mAppendCountry + ", mAppendCountryCode=" + mAppendCountryCode + ", mCustomerReference=" + mCustomerReference + ", mDcs=" + mDcs
                + ", mDestination=" + mDestination + ", mPort=" + mPort + ", mDlrRequired=" + mDlrRequired + ", mDltEntityId=" + mDltEntityId + ", mDltTemplateId=" + mDltTemplateId + ", mMaxSplit="
                + mMaxSplit + ", mMessage=" + mMessage + ", mExpiry=" + mExpiry + ", mMessageTag=" + mMessageTag + ", mMessageType=" + mMessageType + ", mParam1=" + mParam1 + ", mParam2=" + mParam2
                + ", mParam3=" + mParam3 + ", mParam4=" + mParam4 + ", mParam5=" + mParam5 + ", mParam6=" + mParam6 + ", mParam7=" + mParam7 + ", mParam8=" + mParam8 + ", mParam9=" + mParam9
                + ", mParam10=" + mParam10 + ", mScheduleTime=" + mScheduleTime + ", mHeader=" + mHeader + ", mTemplateId=" + mTemplateId + ", mTemplateValues=" + mTemplateValues + ", mUdh=" + mUdh
                + ", mUdhi=" + mUdhi + ", mUrltrack=" + mUrltrack + "]";
    }

    @Override
    public String getJsonString()
    {
        final JsonObject jsonObj = new JsonObject();

        addJsonObject(jsonObj, MessageParserConstants.APP_COUNTRY, mAppendCountry);
        addJsonObject(jsonObj, MessageParserConstants.COUNTRY_CD, mAppendCountryCode);
        addJsonObject(jsonObj, MessageParserConstants.CUST_REF, mCustomerReference);
        addJsonObject(jsonObj, MessageParserConstants.DCS, mDcs);
        addJsonObject(jsonObj, MessageParserConstants.DEST, mDestination);
        addJsonObject(jsonObj, MessageParserConstants.PORT, mPort);
        addJsonObject(jsonObj, MessageParserConstants.DLR_REQ, mDlrRequired);
        addJsonObject(jsonObj, MessageParserConstants.DLT_ENTITY_ID, mDltEntityId);
        addJsonObject(jsonObj, MessageParserConstants.DLT_TEMPLATE_ID, mDltTemplateId);
        addJsonObject(jsonObj, MessageParserConstants.MAX_SPLIT, mMaxSplit);
        addJsonObject(jsonObj, MessageParserConstants.MSG, mMessage);
        addJsonObject(jsonObj, MessageParserConstants.EXPIRY, mExpiry);
        addJsonObject(jsonObj, MessageParserConstants.MSG_TAG, mMessageTag);
        addJsonObject(jsonObj, MessageParserConstants.TYPE, mMessageType);
        addJsonObject(jsonObj, MessageParserConstants.PARAM1, mParam1);
        addJsonObject(jsonObj, MessageParserConstants.PARAM2, mParam2);
        addJsonObject(jsonObj, MessageParserConstants.PARAM3, mParam3);
        addJsonObject(jsonObj, MessageParserConstants.PARAM4, mParam4);
        addJsonObject(jsonObj, MessageParserConstants.PARAM5, mParam5);
        addJsonObject(jsonObj, MessageParserConstants.PARAM6, mParam6);
        addJsonObject(jsonObj, MessageParserConstants.PARAM7, mParam7);
        addJsonObject(jsonObj, MessageParserConstants.PARAM8, mParam8);
        addJsonObject(jsonObj, MessageParserConstants.PARAM9, mParam9);
        addJsonObject(jsonObj, MessageParserConstants.PARAM10, mParam10);
        addJsonObject(jsonObj, MessageParserConstants.SCHEDULE_TIME, mScheduleTime);
        addJsonObject(jsonObj, MessageParserConstants.TEMPLATE_ID, mTemplateId);
        addJsonObject(jsonObj, MessageParserConstants.TEMPLATE_VALUES, mTemplateValues);
        addJsonObject(jsonObj, MessageParserConstants.UDH, mUdh);
        addJsonObject(jsonObj, MessageParserConstants.UDHI, mUdhi);
        addJsonObject(jsonObj, MessageParserConstants.URLTRACK, mUrltrack);
        return jsonObj.toString();
    }

    private static void addJsonObject(
            JsonObject aJsonObject,
            String aKey,
            String aValue)
    {
        if (aValue != null)
            aJsonObject.addProperty(aKey, aValue);
    }

}