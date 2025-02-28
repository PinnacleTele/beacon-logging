package com.itextos.beacon.platform.rc.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.RouteConstants;
import com.itextos.beacon.commonlib.constants.RouteLogic;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.clientallowedheaders.ClientAllowedHeaders;
import com.itextos.beacon.inmemory.commonheader.CommonHeaders;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.customfeatures.InmemCustomFeatures;
import com.itextos.beacon.inmemory.governmentheaders.GovtHeaderBlockCheck;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.msgutil.cache.CarrierCircle;
import com.itextos.beacon.inmemory.msgutil.util.IndiaNPFinder;
import com.itextos.beacon.inmemory.msgvalidity.ClientMsgValidity;
import com.itextos.beacon.inmemory.msgvalidity.CommonMsgValidity;
import com.itextos.beacon.inmemory.routeinfo.util.RouteUtil;
import com.itextos.beacon.inmemory.userheader.IntlUserHeaderInfo;
import com.itextos.beacon.inmemory.whitelistnumbers.MobileWhitelistNumbers;
import com.itextos.beacon.platform.faillistutil.FaillistFinder;

public class RCUtil
{

    private static final Log    log                    = LogFactory.getLog(RCUtil.class);

    public static final int     INTL_HEADER_MIN_LENGTH = CommonUtility.getInteger(RouteUtil.getAppConfigValueAsString(ConfigParamConstants.INTL_HEADER_MIN_LEN), 6);
    public static final int     INTL_HEADER_MAX_LENGTH = CommonUtility.getInteger(RouteUtil.getAppConfigValueAsString(ConfigParamConstants.INTL_HEADER_MAX_LEN), 15);

    private static final String ALL_CLIENTS            = "*";

    private RCUtil()
    {}

    public static void setMessageValidityPeriod(
            MessageRequest aMessageRequest)
    {
        final int lMessageExpiry = aMessageRequest.getMaxValidityInSec();

        if (log.isDebugEnabled())
            log.debug("Client Request Message Validity :'" + lMessageExpiry + "'");

        final boolean isGlobalMsgValidity = lMessageExpiry <= 0;

        if (isGlobalMsgValidity)
        {
            final String          lClientId          = aMessageRequest.getClientId();
            final MessageType     lMsgType           = aMessageRequest.getMessageType();
            final MessagePriority lMsgPriority       = aMessageRequest.getMessagePriority();

            final int             lClientMsgValidity = getClientMessageValidity(lClientId, lMsgType.getKey());

            if (log.isDebugEnabled())
                log.debug("Client Configured Message Validity :'" + lClientMsgValidity + "'");

            if (lClientMsgValidity > 0)
                aMessageRequest.setMaxValidityInSec(lClientMsgValidity);
            else
            {
                final int lMsgValidity = getMessageValidity(lMsgType.getKey(), String.valueOf(lMsgPriority.getPriority()));

                if (log.isDebugEnabled())
                    log.debug("Priority based Message Validity :'" + lClientMsgValidity + "'");

                aMessageRequest.setMaxValidityInSec(lMsgValidity);
            }
        }
    }

    public static String checkDomesticFailList(
            MessageRequest aMessageRequest)
    {
        String lRouteId = null;

        try
        {
            final String lClientId               = aMessageRequest.getClientId();
            final String lBaseMessageId          = aMessageRequest.getBaseMessageId();
            final String lMobileNumber           = aMessageRequest.getMobileNumber();

            boolean      lDomesticClientFailList = CommonUtility.isEnabled(getCutomFeatureValue(lClientId, CustomFeatures.DOMESTIC_CLIENT_FAILLIST_CHK));

            if (lDomesticClientFailList)
            {
                final boolean lDomesticFaillistMobileNumber = FaillistFinder.isDomesticClientBlocklistNumber(lClientId, lMobileNumber);

                if (lDomesticFaillistMobileNumber)
                {
                    lRouteId = getAppConfigValueAsString(ConfigParamConstants.ACC_FAILLIST_DOMESTIC_ROUTE_ID);
                    if (log.isDebugEnabled())
                        log.debug("checkDomesticFailList() number registered under ACC_FAILLIST_DOMESTIC_ROUTE_ID mid:" + lBaseMessageId + " dummyroute:" + lRouteId + " Mobile Number:" + lMobileNumber
                                + " Client Id :" + lClientId);

                    if (lRouteId != null)
                    {
                        aMessageRequest.setRouteId(lRouteId);
                        aMessageRequest.setRouteLogicId(CommonUtility.getInteger(RouteLogic.ACC_FAILLIST_DOMESTIC_LOGICID.getKey()));
                    }
                    return lRouteId;
                }
                else
                    if (log.isDebugEnabled())
                        log.debug("checkDomesticFailList() number not registered under ACC_FAILLIST_DOMESTIC_ROUTE_ID mid:" + lBaseMessageId + " dummyroute:" + lRouteId + " Mobile Number:"
                                + lMobileNumber + " Client Id :" + lClientId);
            }

            // checking for faillist domestic global check
            lDomesticClientFailList = CommonUtility.isEnabled(getCutomFeatureValue(lClientId, CustomFeatures.DOMESTIC_GLOBAL_FAILLIST_CHK));

            if (!lDomesticClientFailList)

                lDomesticClientFailList = CommonUtility.isEnabled(getAppConfigValueAsString(ConfigParamConstants.DOMESTIC_GLOBAL_FAILLIST_CHK));

            if (!lDomesticClientFailList)
                return null;

            final boolean lDomesticFaillistMobileNumber = FaillistFinder.isDomesticGlobalBlocklistNumber(lMobileNumber);

            if (lDomesticFaillistMobileNumber)
            {
                lRouteId = getAppConfigValueAsString(ConfigParamConstants.GLOBAL_FAILLIST_DOMESTIC_ROUTE_ID);

                if (log.isDebugEnabled())
                    log.debug("Number registered under DOMESTIC_GLOBAL_FAILLIST_CHK mid:" + lBaseMessageId + " dummyroute:" + lRouteId + " Mobile Number:" + lMobileNumber);

                if (lRouteId != null)
                {
                    aMessageRequest.setRouteId(lRouteId);
                    aMessageRequest.setRouteLogicId(CommonUtility.getInteger(RouteLogic.GLOBAL_FAILLIST_DOMESTIC_LOGICID.getKey()));
                }
                return lRouteId;
            }
            else
                if (log.isDebugEnabled())
                    log.debug("Number not registered under DOMESTIC_GLOBAL_FAILLIST_CHK mid:" + lBaseMessageId + " dummyroute:" + lRouteId + " Mobile Number:" + lMobileNumber);
        }
        catch (final Exception e)
        {
            log.error("Exception occer while faillist check..", e);
        }
        return null;
    }

    // public static boolean isExpiredRoute(
    // BaseMessage aNunMessage)
    // {
    // return
    // RConstants.EXPIRED.equalsIgnoreCase(CommonUtility.nullCheck(aNunMessage.getValue(MiddlewareConstant.MW_ROUTE_ID),
    // true));
    // }

    public static boolean isExpiredRoute(
            String aRouteId)
    {
        return RouteConstants.EXPIRED.equalsIgnoreCase(aRouteId);
    }

    public static boolean isAbsoluteRoute(
            String aRouteId)
    {
        if (log.isDebugEnabled())
            log.debug("isAbsoluteRoute()");

        return RouteUtil.isRouteAvailable(aRouteId);
    }

    public static boolean isGlobalHeaderBlocklist(
            String aMessageId,
            String aHeader)
    {
        if (log.isDebugEnabled())
            log.debug("isGlobalHeaderBlocklist() start Message Id : " + aMessageId);

        final CommonHeaders lCommonHeader = (CommonHeaders) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.COMMON_HEADERS);

        return (lCommonHeader == null) ? false : lCommonHeader.isCommonHeader(aHeader);
    }

    public static boolean isClientAllowedHeader(
            String aMessageId,
            String aHeader,
            String aClientId)
    {
        if (log.isDebugEnabled())
            log.debug("isClientAllowedHeader() start Message Id : " + aMessageId);

        final ClientAllowedHeaders lClientAllowedHeaders = (ClientAllowedHeaders) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CLIENT_ALLOW_HEADERS);

        return (lClientAllowedHeaders == null) ? false : lClientAllowedHeaders.isHeaderAllowed(aClientId, aHeader);
    }

    public static boolean checkIntlFaillistNumber(
            MessageRequest aMessageRequest)
    {

        try
        {
            return getIntlFailList(aMessageRequest);
        }
        catch (final Exception e)
        {
            log.error("Exception while checking the fail list number.", e);
        }
        return false;
    }

    private static boolean getIntlFailList(
            MessageRequest aMessageRequest)
    {

        try
        {
            final String lClientId       = aMessageRequest.getClientId();
            final String lBaseMessageId  = aMessageRequest.getBaseMessageId();
            final String lMobileNumber   = aMessageRequest.getMobileNumber();
            boolean      isClientLevel   = false;
            boolean      lValueOfFeature = CommonUtility.isEnabled(getCutomFeatureValue(lClientId, CustomFeatures.INTL_CLIENT_FAILLIST_CHK));

            if (lValueOfFeature)
                isClientLevel = true;
            else
            {
                lValueOfFeature = CommonUtility.isEnabled(getCutomFeatureValue(lClientId, CustomFeatures.INTL_GLOBAL_FAILLIST_CHK));
                if (!lValueOfFeature)
                    lValueOfFeature = CommonUtility.isEnabled(getCutomFeatureValue(ALL_CLIENTS, CustomFeatures.INTL_GLOBAL_FAILLIST_CHK));
                if (!lValueOfFeature)
                    return false;
            }

            if (isClientLevel)
            {
                final boolean isIntlFaillistNumber = FaillistFinder.isInternationalClientBlocklistNumber(lClientId, lMobileNumber);

                if (isIntlFaillistNumber)
                {
                    if (log.isDebugEnabled())
                        log.debug("Number registered under INTL_CLIENT_FAILLIST_CHK mid:" + lBaseMessageId + " Mobile Number:" + lMobileNumber + " Client ID:" + lClientId);
                    aMessageRequest.setIntlClientFaillistCheck(CustomFeatures.INTL_CLIENT_FAILLIST_CHK.getKey());
                    return true;
                }
            }
            else
            {
                final boolean isIntlFaillistNumber = FaillistFinder.isInternationalGlobalBlocklistNumber(lMobileNumber);

                if (isIntlFaillistNumber)
                {
                    if (log.isDebugEnabled())
                        log.debug("getIntlFailList() number registered under INTL_GLOBAL_FAILLIST_CHK mid:" + lBaseMessageId + " Mobile Number:" + lMobileNumber);
                    aMessageRequest.setIntlGlobalFaillistCheck(CustomFeatures.INTL_GLOBAL_FAILLIST_CHK.getKey());
                    return true;
                }
            }

            if (log.isDebugEnabled())
                log.debug("getIntlFailList() number not registered under MW_INTL_GLOBAL_FAILLIST_CHK mid:" + lBaseMessageId + " Mobile Number:" + lMobileNumber);
            return false;
        }
        catch (final Exception e)
        {
            log.error("Exception Occer Global/Client Faillist Check..", e);
        }
        return false;
    }

    public static String getCutomFeatureValue(
            String aClientId,
            CustomFeatures aCustomFeature)
    {
        final InmemCustomFeatures lCustomFeatures = (InmemCustomFeatures) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_FEATURES);
        return lCustomFeatures.getValueOfCustomFeature(aClientId, aCustomFeature.getKey());
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    public static boolean isValidUserHeader(
            String aClientId,
            String aHeader)
    {
        final IntlUserHeaderInfo lIntlUserHeaderInfo = (IntlUserHeaderInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.INTL_USER_HEADERS);
        return (lIntlUserHeaderInfo == null) ? false : lIntlUserHeaderInfo.isHeaderMatches(aClientId, aHeader.toLowerCase());
    }

    public static int getMessageValidity(
            String aMsgType,
            String aMsgPriority)
    {
        final CommonMsgValidity lMsgValidity = (CommonMsgValidity) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.COMMON_MSG_VALIDITY);
        return lMsgValidity.getMessageValidity(aMsgType, aMsgPriority);
    }

    public static String getGovtRoute(
            String aHeader)
    {
        final GovtHeaderBlockCheck lGovtHeaderBlock = (GovtHeaderBlockCheck) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.GOVT_HEADER_BLOCK);
        return lGovtHeaderBlock.getGovernmentRoute(aHeader);
    }

    public static boolean checkNumberWhiteListed(
            String aMobileNumber)
    {
        final MobileWhitelistNumbers lWhiteListNumber = (MobileWhitelistNumbers) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.MOBILE_WHITELIST);
        return (lWhiteListNumber == null) ? false : lWhiteListNumber.isNumberWhitelisted(aMobileNumber);
    }

    public static void findAndSetCarrierCircle(
            MessageRequest aMessageRequest)
    {
        CarrierCircle lCarrierCircle = null;

        final String  lMNumber       = CommonUtility.nullCheck(aMessageRequest.getMobileNumber(), true);

        try
        {
            String lMscCodeSplitVal = CommonUtility.nullCheck(getAppConfigValueAsString(ConfigParamConstants.MSC_CODE_SPLIT_CHK), true);

            if (lMscCodeSplitVal.isEmpty())
                lMscCodeSplitVal = "7~6";

            final String[] lSplitArr = lMscCodeSplitVal.split("~");

            for (final String splitVal : lSplitArr)
            {
                lCarrierCircle = IndiaNPFinder.getCarrierCircle(lMNumber.substring(0, Integer.parseInt(splitVal)));

                if (lCarrierCircle != null)
                {
                    if (log.isDebugEnabled())
                        log.debug("Carrier & Circle taken for " + lMNumber + " from mcc_mnc code " + lCarrierCircle);

                    break;
                }
            }
        }
        catch (final Exception e)
        {
            log.error("Unable to finding carrier/circle from msc_error codes for " + lMNumber, e);
        }

        if (lCarrierCircle == null)
        {
            if (log.isDebugEnabled())
                log.debug("Carrier & Circle taken as default for " + lMNumber);

            lCarrierCircle = CarrierCircle.DEFAULT_CARRIER_CIRCLE;
        }

        aMessageRequest.setCarrier(lCarrierCircle.getCarrier());
        aMessageRequest.setCircle(lCarrierCircle.getCircle());
    }

    public static int getClientMessageValidity(
            String aClientId,
            String aMsgType)
    {
        final ClientMsgValidity lClientMsgValidity = (ClientMsgValidity) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CLIENT_MSG_VALIDITY);
        return lClientMsgValidity.getClientMessageValidity(aClientId, aMsgType);
    }

}
