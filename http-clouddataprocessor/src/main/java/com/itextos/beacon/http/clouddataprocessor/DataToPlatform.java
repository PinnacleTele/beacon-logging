package com.itextos.beacon.http.clouddataprocessor;

import com.google.gson.JsonObject;
import com.itextos.beacon.http.clouddatautil.common.RequestType;

class DataToPlatform
{

    private final String                 mFileId;
    private final String                 mClientId;
    private final String                 mClientIp;
    private final String                 mReceivedTime;
    private final RequestType            mRequestType;
    private final IDataToPlatformRequest mDataToPlatform;

    public DataToPlatform(
            String aFileId,
            String aClientId,
            String aClientIp,
            String aReceivedTime,
            RequestType aRequestType,
            IDataToPlatformRequest aDataToPlatform)
    {
        super();
        mFileId         = aFileId;
        mClientId       = aClientId;
        mClientIp       = aClientIp;
        mReceivedTime   = aReceivedTime;
        mRequestType    = aRequestType;
        mDataToPlatform = aDataToPlatform;
    }

    public JsonObject getJsonObject()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("file_id", mFileId);
        jsonObject.addProperty("cliend_ip", mClientIp);
        jsonObject.addProperty("received_time", mReceivedTime);
        jsonObject.addProperty("incoming_request_type", mRequestType.getKey());
        jsonObject.addProperty("incoming_request", mDataToPlatform.getJsonString());
        return jsonObject;
    }

    @Override
    public String toString()
    {
        return "DataToPlatform [mFileId=" + mFileId + ", mClientId=" + mClientId + ", mClientIp=" + mClientIp + ", mReceivedTime=" + mReceivedTime + ", mRequestType=" + mRequestType
                + ", mDataToPlatform=" + mDataToPlatform + "]";
    }

}