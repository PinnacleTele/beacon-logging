package com.itextos.beacon.r3r.process;

import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.r3r.data.R3CUserInfo;
import com.itextos.beacon.r3r.data.R3rRequestData;
import com.itextos.beacon.r3r.dbo.DBHandler;
import com.itextos.beacon.r3r.inmemory.RequestDataInmemoryQueue;
import com.itextos.beacon.r3r.utils.R3RConstants;

public class UrlRedirectProcessor
{

    private static final Log log = LogFactory.getLog(UrlRedirectProcessor.class);

    private UrlRedirectProcessor()
    {}

    public static String processRequestInfo(
            R3CUserInfo aR3cUserInfo)
    {
        String lRedirectUrl = null;

        try
        {
            if (log.isDebugEnabled())
                log.debug("Request info is : - '" + aR3cUserInfo + "'");

            final String[] lsplittedData = aR3cUserInfo.getRequestUrl().split("/");

            if (lsplittedData.length >= 1)
            {
                final String lShortCode = CommonUtility.nullCheck(lsplittedData[1], true);

                if (log.isDebugEnabled())
                    log.debug("Request Shortcode is : - '" + lShortCode + "'");

                if (lShortCode.equalsIgnoreCase("favicon.ico"))
                {
                    log.error("Request comes with favicon : '" + lsplittedData + "'");
                    addToInmemory("Received favicon.ico", aR3cUserInfo, null, "Invalid Request");
                    lRedirectUrl = R3RConstants.SHORTCODE_NOT_PROVIDED;
                }
                else
                    return processShortCode(lShortCode, aR3cUserInfo);
            }
            else
            {
                log.error("ShortCode not available in request : '" + lsplittedData + "'");
                addToInmemory("No ShortCode", aR3cUserInfo, null, "ShortCode Not Available in Request");
                lRedirectUrl = R3RConstants.REQUEST_SHORTCODE_NOT_AVAILABLE;
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while process the URL Redirect Request ", e);
            return R3RConstants.REDIRECT_URL_EXCEPTION;
        }
        return lRedirectUrl;
    }

    private static String processShortCode(
            String aShortCode,
            R3CUserInfo aR3cUserInfo)
    {
        String                    lRedirectUrl      = null;
        String                    lExpiryDate       = null;
        final Map<String, String> lShortCodeDataMap = DBHandler.getShortCodeData(aShortCode);

        if (log.isDebugEnabled())
            log.debug("Shortcode Data from DataBase is : - " + lShortCodeDataMap);

        if (!lShortCodeDataMap.isEmpty())
        {
            lExpiryDate = lShortCodeDataMap.get("expiry_date");

            final Date lExpiryDateToCheck = DateTimeUtility.getDateFromString(lExpiryDate, DateTimeFormat.DEFAULT_DATE_ONLY);
            final Date lcurrentDate       = DateTimeUtility.getCurrentDateWithoutTime();
            final int  lExpiryDuration    = lcurrentDate.compareTo(lExpiryDateToCheck);

            if (lExpiryDuration > 0)
            {
                // Date Expired
                log.error("Redirect Link has been Expired for the ShortCode " + aShortCode);
                addToInmemory(aShortCode, aR3cUserInfo, lShortCodeDataMap, "Redirect Url Duration Expired");
            }
            else
            {
                addToInmemory(aShortCode, aR3cUserInfo, lShortCodeDataMap, "Success");

                lRedirectUrl = CommonUtility.nullCheck(lShortCodeDataMap.get("url"), true);

                if (log.isDebugEnabled())
                    log.debug("Url going to Redirect : '" + lRedirectUrl + "'");

                if (!lRedirectUrl.isBlank())
                {

                    if (!lRedirectUrl.toLowerCase().startsWith("http"))
                    {
                        log.error("In customer url not starts with http -- So http is prepend to the URL. '" + lRedirectUrl + "'");
                        lRedirectUrl = "http://" + lRedirectUrl;
                    }
                }
                else
                {
                    log.error("Redirect URL is blank in databse for the requested shortCode : '" + aShortCode + "'");
                    lRedirectUrl = R3RConstants.REDIRECT_URL_NOT_VALID;
                }
            }
        }
        else
        {
            log.error("No records found in databse for the requested shortCode : '" + aShortCode + "'");
            addToInmemory(aShortCode, aR3cUserInfo, lShortCodeDataMap, "Request shortcode is invalid");
            lRedirectUrl = R3RConstants.REDIRECT_URL_NOT_AVAILABLE;
        }

        return lRedirectUrl;
    }

    private static void addToInmemory(
            String lshortCode,
            R3CUserInfo aR3cUserInfo,
            Map<String, String> aShortCodeDataMap,
            String aRequestStatus)
    {
        final R3rRequestData lR3rRequestData = new R3rRequestData();

        lR3rRequestData.setShortCode(lshortCode);
        lR3rRequestData.setUserAgent(aR3cUserInfo.getUserAgent());
        lR3rRequestData.setRequestIpAddress(aR3cUserInfo.getUserIp());
        lR3rRequestData.setShortCodeDataMap(aShortCodeDataMap);
        lR3rRequestData.setRequestTime(aR3cUserInfo.getRequestedTime());
        lR3rRequestData.setRequestStatus(aRequestStatus);

        RequestDataInmemoryQueue.getInstance().add(lR3rRequestData);
        if (log.isDebugEnabled())
            log.debug("Request Data added to Inmemory : " + lR3rRequestData);
    }

    public static String getDefaultRedirectUrl()
    {
        final ApplicationConfiguration applicationConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return CommonUtility.nullCheck(applicationConfiguration.getConfigValue(ConfigParamConstants.R3C_DEFAULT_DOMAIN_URL), true);
    }

}
