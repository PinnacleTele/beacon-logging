package com.itextos.beacon.interfaces.generichttpapi.processor.response;

import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.InterfaceStatusCode;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceRequestStatus;
import com.itextos.beacon.http.generichttpapi.common.data.response.ResponseObject;
import com.itextos.beacon.http.generichttpapi.common.interfaces.IResponseProcessor;
import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.http.generichttpapi.common.utils.Utility;
import com.itextos.beacon.inmemory.interfaces.bean.InterfaceResponse;
import com.itextos.beacon.inmemory.interfaces.bean.InterfaceResponseCodeMapping;
import com.itextos.beacon.inmemory.interfaces.cache.GenericResponse;
import com.itextos.beacon.inmemory.interfaces.util.IInterfaceUtil;

abstract class GenerateAbstractResponse
        implements
        IResponseProcessor
{

    private static final Log         log             = LogFactory.getLog(GenerateAbstractResponse.class);

    protected InterfaceRequestStatus mRequestStatus;
    protected String                 mCustIp         = "";
    protected String                 mUserName       = "";
    protected String                 mServletContext = "";
    protected String                 mClientId       = "";
    protected String                 mReqType;
    protected String                 mTimeZone       = "";
    protected int                    mHttpStatus     = HttpServletResponse.SC_OK;

    protected GenerateAbstractResponse(
            String aIP)
    {
        this.mCustIp = aIP;
    }

    @Override
    public void setStatusObject(
            InterfaceRequestStatus aRequestStatus,
            String aClientId,
            String aReqType,
            String aTimeZone)
    {
        mRequestStatus = aRequestStatus;
        mClientId      = aClientId;
        mReqType       = aReqType;
        mTimeZone      = aTimeZone;
    }

    @Override
    public void setUname(
            String aUname)
    {
        mUserName = aUname;
    }

    @Override
    public void setServletContext(
            String aServletContext)
    {
        mServletContext = aServletContext;
    }

    @Override
    public String getServletContext()
    {
        return mServletContext;
    }

    @Override
    public String getRequestType()
    {
        return mReqType;
    }

    public String getResponseDateTimeString()
    {
        return Utility.getFormattedDateTime(new Date(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS, mTimeZone);
    }

    public String getResponseDateTimeString(
            String aDateTimeFormat)
    {
        return Utility.getFormattedDateTime(new Date(), aDateTimeFormat, mTimeZone);
    }

    @Override
    public String generateResponse()
    {
        String returnValue = null;

        try
        {
            final ResponseObject    ro              = getResponseObject();
            final GenericResponse   lGenericResp    = IInterfaceUtil.getGenericResponse();
            final InterfaceResponse dynamicResponse = lGenericResp.getInterfaceResponse(mClientId, mReqType);

            if (dynamicResponse == null)
            {
                ro.setReqTime(getResponseDateTimeString());
                returnValue = getGeneralReqTypeSepecificResponse(ro);
            }
            else
            {
                ro.setReqTime(getResponseDateTimeString(dynamicResponse.getDateTimeFormat()));
                final InterfaceResponseCodeMapping lResponseCodeMapping = dynamicResponse.getResponseCodeMapping(ro.getStatusCode().getStatusCode());
                returnValue = Utility.replaceParamIfAvailable(dynamicResponse, ro, lResponseCodeMapping);

                if (log.isDebugEnabled())
                {
                    log.debug("Response Value : " + returnValue);
                    log.debug("Response Code Mapping :" + lResponseCodeMapping);
                }

                if (lResponseCodeMapping != null)
                    mHttpStatus = CommonUtility.getInteger(lResponseCodeMapping.getHttpStatus(), HttpServletResponse.SC_OK);
                else
                    mHttpStatus = HttpServletResponse.SC_OK;
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while generating the response for the request: '" + mRequestStatus + "'", e);
            returnValue = getErrorString();
        }

        return returnValue;
    }

    @Override
    public int getHttpStatus()
    {
        return mHttpStatus;
    }

    private ResponseObject getResponseObject()
    {
        ResponseObject ro = new ResponseObject();

        try
        {
            final InterfaceStatusCode lStatusCode = mRequestStatus.getStatusCode();
            String                    lReason     = lStatusCode.getStatusDesc();
            final String              lStatusInfo = (lStatusCode != InterfaceStatusCode.SUCCESS) ? APIConstants.STATUS_INFO_REJECT : APIConstants.STATUS_INFO_ACCEPT;
            final String              lUserName   = CommonUtility.nullCheck(mUserName, true).isBlank() ? "" : mUserName;

            PrometheusMetrics.apiIncrementStatusCount(InterfaceType.HTTP_JAPI, mReqType, APIConstants.CLUSTER_INSTANCE, mCustIp, lStatusCode.getStatusCode(), lUserName);

            if (!CommonUtility.nullCheck(mRequestStatus.getStatusDesc(), true).isBlank())
                lReason = lReason + " ( " + mRequestStatus.getStatusDesc() + " )";

            ro.setStatusCode(lStatusCode);
            ro.setReason(lReason);
            ro.setStatusInfo(lStatusInfo);
            ro.setMessageId(mRequestStatus.getMessageId());
            ro.setReqTime(getResponseDateTimeString());
        }
        catch (final Exception e)
        {
            log.error("Exception while generating the response for the request: '" + mRequestStatus + "'", e);
            ro = ResponseObject.getErrorResponse();
        }
        return ro;
    }

    protected abstract String getGeneralReqTypeSepecificResponse(
            ResponseObject aRo)
            throws Exception;

    protected abstract String getErrorString();

}