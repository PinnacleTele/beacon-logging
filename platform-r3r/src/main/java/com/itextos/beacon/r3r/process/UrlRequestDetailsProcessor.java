package com.itextos.beacon.r3r.process;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.httpclient.HTTPRequestUtility;
import com.itextos.beacon.commonlib.httpclient.HttpHeader;
import com.itextos.beacon.commonlib.httpclient.HttpResult;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemdata.account.ClientAccountDetails;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.r3r.data.R3RObject;
import com.itextos.beacon.r3r.data.R3rRequestData;
import com.itextos.beacon.r3r.inmemory.RequestDataInmemoryQueue;
import com.itextos.beacon.r3r.inmemory.RequestDetailsInmemoryQueue;
import com.itextos.beacon.r3r.utils.R3RConstants;

import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;

public class UrlRequestDetailsProcessor
        extends
        Thread
{

    private static final Log log          = LogFactory.getLog(UrlRequestDetailsProcessor.class);
    private boolean          mCanContinue = true;

    @Override
    public void run()
    {
        while (mCanContinue)
            processRequestDetails();
    }

    private static void processRequestDetails()
    {

        try
        {
            final R3rRequestData lr3rRequestData = RequestDataInmemoryQueue.getInstance().getData();

            if (lr3rRequestData != null)
            {
                if (log.isDebugEnabled())
                    log.debug("Data consumed from the RequestDataInmemoryQueue is : " + lr3rRequestData.toString());
                final Map<String, String> shortCodeDataMap = lr3rRequestData.getShortCodeDataMap();
                final R3RObject           lR3rObject       = new R3RObject();

                if ((shortCodeDataMap != null) && !shortCodeDataMap.isEmpty())
                {
                    if (log.isDebugEnabled())
                        log.debug("shortCodeDataMap is : " + shortCodeDataMap);
                    final String  lClientId  = shortCodeDataMap.get("client_id");
                    final boolean isUrlTrack = getUrlFlag(lClientId);

                    if (log.isDebugEnabled())
                        log.debug("is_url_track is : - " + isUrlTrack);

                    if (isUrlTrack)
                    {
                        lR3rObject.setRequestTime(lr3rRequestData.getRequestTime());
                        lR3rObject.setClientId(lClientId);
                        lR3rObject.setDest(shortCodeDataMap.get("dest"));
                        lR3rObject.setMsgId(shortCodeDataMap.get("msg_id"));
                        lR3rObject.setCustMsgId(shortCodeDataMap.get("cli_msg_id"));
                        lR3rObject.setCamapignId(shortCodeDataMap.get("campaign_id"));
                        lR3rObject.setCampaignName(shortCodeDataMap.get("campaign_name"));
                        lR3rObject.setMsgRecvTime(Long.parseLong(shortCodeDataMap.get("recv_time")));
                        lR3rObject.setShortCode(lr3rRequestData.getShortCode());
                        lR3rObject.setShortenUrl(shortCodeDataMap.get("shortner_url"));
                        lR3rObject.setSmartLinkId(shortCodeDataMap.get("smartlink_id"));
                        lR3rObject.setRedirectUrl(shortCodeDataMap.get("url"));
                        lR3rObject.setReason(lr3rRequestData.getRequestStatus());

                        getLocationInfo(lR3rObject, lr3rRequestData.getRequestIpAddress());
                        getDeviceDetails(lR3rObject, lr3rRequestData.getUserAgent());

                        if (log.isDebugEnabled())
                            log.debug("R3RObject is going to Store the Data : - " + lR3rObject);

                        RequestDetailsInmemoryQueue.getInstance().add(lR3rObject);
                    }
                    else
                    {
                        if (log.isDebugEnabled())
                            log.debug(" ----------- Url Track is 0 --------------------- ");
                        RequestDetailsInmemoryQueue.getInstance().add(lR3rObject);
                    }
                }
                else
                {
                    lR3rObject.setShortCode(lr3rRequestData.getShortCode());
                    lR3rObject.setRequestTime(lr3rRequestData.getRequestTime());
                    lR3rObject.setReason(lr3rRequestData.getRequestStatus());
                    final String lIPAddress = lr3rRequestData.getRequestIpAddress();

                    if (lIPAddress != null)
                    {
                        lR3rObject.setIpAddress(lr3rRequestData.getRequestIpAddress());
                        getLocationInfo(lR3rObject, lr3rRequestData.getRequestIpAddress());
                    }
                    getDeviceDetails(lR3rObject, lr3rRequestData.getUserAgent());

                    if (log.isDebugEnabled())
                        log.debug("R3RObject is going to Store the Data : - " + lR3rObject);
                    RequestDetailsInmemoryQueue.getInstance().add(lR3rObject);
                }
            }
        }
        catch (final Exception e)
        {
            log.error("Error while process the Request Data ", e);
        }
    }

    private static boolean getUrlFlag(
            String aClientId)
    {
        boolean  returnValue = false;
        UserInfo lUserInfo   = null;

        try
        {
            lUserInfo = ClientAccountDetails.getUserDetailsByClientId(aClientId);

            if (log.isDebugEnabled())
                log.debug("User Configuration : - " + lUserInfo.toString());
            final JsonObject lAccountDetailsObject = JsonParser.parseString(lUserInfo.getAccountDetails()).getAsJsonObject();
            returnValue = CommonUtility.isEnabled(lAccountDetailsObject.get(MiddlewareConstant.MW_URL_TRACKING_ENABLE.getName()).getAsString());
        }
        catch (final Exception e)
        {
            log.error("Error while process the JSon data for the client '" + aClientId + "' R3CUserInfo " + lUserInfo, e);
        }

        return returnValue;
    }

    public static void getLocationInfo(
            R3RObject aR3rObject,
            String aIpAddress)
    {

        try
        {
            final HttpResult lhttpResult = getLongituteLatituteLinfo(aIpAddress);
            final int        statusCode  = lhttpResult.getStatusCode();

            if (statusCode == HttpStatus.SC_OK)
            {
                final String lresponse = lhttpResult.getResponseString();
                if (log.isDebugEnabled())
                    log.debug("Response from IP Location finder URL is ::" + lresponse);

                if (lresponse != null)
                    setLocationInfo(aR3rObject, aIpAddress, lresponse);
            }
            else
                if (log.isDebugEnabled())
                    log.debug("Can't able to find the location details for requested Ip :" + aIpAddress + " Http Response Code is : " + statusCode);
        }
        catch (final Exception e)
        {
            log.error("Exception while processing the URL Request to get Location Info: -  ", e);
        }
    }

    private static void setLocationInfo(
            R3RObject aR3rObject,
            String aIpAddress,
            String aResponse)
    {
        final String[] respStr = aResponse.split(";");

        if (respStr.length > R3RConstants.LOCATION_INFO_RESPONSE_LENGTH)
        {
            aR3rObject.setCountryCode(respStr[R3RConstants.COUNTRY_CODE_INDEX]);
            aR3rObject.setCountryName(respStr[R3RConstants.COUNTRY_NAME_INDEX]);
            aR3rObject.setRegion(respStr[R3RConstants.REGION_INDEX]);
            aR3rObject.setCity(respStr[R3RConstants.CITY_INDEX]);
            aR3rObject.setLongitude(respStr[R3RConstants.LONGITUDE_INDEX]);
            aR3rObject.setLatitude(respStr[R3RConstants.LATITUDE_INDEX]);
        }
        else
            log.error("Location Response does not have enough information. Response '" + aResponse + "'");
    }

    private static HttpResult getLongituteLatituteLinfo(
            String aIpAddress)
    {
        final ApplicationConfiguration applicationConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        final String                   laccessKey               = CommonUtility.nullCheck(applicationConfiguration.getConfigValue(R3RConstants.ACCESSKEY), true);
        final String                   laccessUrl               = CommonUtility.nullCheck(applicationConfiguration.getConfigValue(R3RConstants.ACCESSURL), true);

        final StringBuilder            lqueryString             = new StringBuilder();
        lqueryString.append("?" + R3RConstants.REQUEST_KEY + "=" + laccessKey);
        lqueryString.append("&" + R3RConstants.IPADDRESSKEY + "=" + aIpAddress);

        final HttpHeader<String, String> lheaderMap = new HttpHeader<>();
        lheaderMap.put(R3RConstants.REQUEST_HEADER_CONTENT_TYPE_KEY, R3RConstants.REQUEST_HEADER_CONTENT_TYPE_VALUE);

        final String lcompleteUrl = laccessUrl + lqueryString.toString();
        if (log.isDebugEnabled())
            log.debug("Url going to hit to get the Location Details : -" + lcompleteUrl);

        return HTTPRequestUtility.processGetRequest(null, lcompleteUrl, lheaderMap);
    }

    public static void getDeviceDetails(
            R3RObject aR3rObject,
            String aUserAgent)
    {

        try
        {
            final UserAgent       lUserAgent       = UserAgent.parseUserAgentString(aUserAgent);
            final OperatingSystem loperatingSystem = lUserAgent.getOperatingSystem();
            aR3rObject.setBrowserName(CommonUtility.nullCheck(lUserAgent.getBrowser().getName(), true));
            aR3rObject.setOsName(CommonUtility.nullCheck(lUserAgent.getOperatingSystem(), true));
            aR3rObject.setOsGroup(CommonUtility.nullCheck(loperatingSystem.getGroup(), true));
            aR3rObject.setDeviceName(loperatingSystem.getDeviceType() == null ? "" : loperatingSystem.getDeviceType().getName());
            aR3rObject.setBrowserVersion(CommonUtility.nullCheck(lUserAgent.getBrowserVersion(), true));
            loperatingSystem.getName();
        }
        catch (final Exception e)
        {
            log.error("Exception while processing the URL Request to get Device Info: -  ", e);
        }
    }

    public void stopMe()
    {
        processRequestDetails();
        mCanContinue = false;
        log.fatal("Stopping the URL Request Detaisl Process Thread");
    }

}