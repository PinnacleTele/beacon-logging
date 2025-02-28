package com.itextos.beacon.platform.dnpayloadutil.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.inmemory.dnpayload.cache.pojo.DlrClientPercentageInfo;
import com.itextos.beacon.inmemory.dnpayload.cache.pojo.DlrPercentageInfo;
import com.itextos.beacon.inmemory.dnpayload.util.DNPUtil;

public class MaskingErrorCodeUtil
{

    private static final Log    log                   = LogFactory.getLog(MaskingErrorCodeUtil.class);

    private static final String ERROR_2_SUCCESS_COUNT = "ERROR_2_SUCCESS_COUNT";
    private static final String TOTAL_ERROR_COUNT     = "TOTAL_ERROR_COUNT";
    private static final String TEMPORARY_VARIABLE    = "tempVar";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final MaskingErrorCodeUtil INSTANCE = new MaskingErrorCodeUtil();

    }

    public static MaskingErrorCodeUtil getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<String, Map<String, Integer>> mClientMessageCountMap = new HashMap<>();
    private final Map<String, Map<String, Integer>> mMessageCountMap       = new HashMap<>();

    public synchronized String getMaskedRouteId(
            String aClientId,
            String aMsgType,
            String aPriority,
            String aRouteId,
            String aErrorCode,
            String aMNumber,
            String aCircle)
    {
        if (log.isDebugEnabled())
            log.debug("Client Id:" + aClientId + "msg_type :" + aMsgType + "aPriority:" + aPriority + ", RouteID :" + aRouteId + ", ErrorCode :" + aErrorCode + " Mobile Number:" + aMNumber
                    + " Circle:" + aCircle);

        final boolean isNmberWhitelisted = PayloadUtil.checkNumberWhiteListed(aMNumber);
        if (log.isDebugEnabled())
            log.debug("Is the Number Whitelisted:" + isNmberWhitelisted);

        // Mobile Number should not be listed in whitelist_mobile and circle
        // should not be listed in configuration.circle_exlcude_config
        if (isNmberWhitelisted)
            return null;

        final boolean circleInExcludeList = PayloadUtil.isCircleInExcludeList(aClientId, aCircle);
        if (log.isDebugEnabled())
            log.debug("Is Circle excluded from configuration.circle_exlcude_config " + circleInExcludeList);

        if (circleInExcludeList)
            return null;

        // if the client is not listed in dn_percentage_exempt then
        // check in dn_percentage_mapping
        final boolean clientExempt = DNPUtil.isClientDlrExclude(aClientId);
        if (log.isDebugEnabled())
            log.debug("Client is exempted from dn_gen_percentage_exempt " + clientExempt);

        if (clientExempt)
            return null;

        // First check in acc_dn_percentage_mapping
        String maskedRouteIDOnFailuerCodeToSuccessCode = getClientWiseMaskedRouteIdOnFailuerToSuccessCode(aClientId, aRouteId, aErrorCode);
        if (log.isDebugEnabled())
            log.debug("client wise pecentage masked value:" + maskedRouteIDOnFailuerCodeToSuccessCode);

        if ((maskedRouteIDOnFailuerCodeToSuccessCode != null))
        {
            if (log.isDebugEnabled())
                log.debug("We got a Non Null maskedRouteIDOnFailuerCodeToSuccessCode '" + maskedRouteIDOnFailuerCodeToSuccessCode + "'");

            if (!TEMPORARY_VARIABLE.equalsIgnoreCase(maskedRouteIDOnFailuerCodeToSuccessCode))
                return maskedRouteIDOnFailuerCodeToSuccessCode;

            // if maskedRouteIDOnFailuerCodeToSuccessCode equals TEMPORARY_VARIABLE -
            // clientPercentageMappingHandler found key for client, aRouteID, aErrorCode
            // combination and we should not check dn_percentage_mapping

            if (log.isDebugEnabled())
                log.debug("*************client_dn_percentage_mapping found entry for Client, aRouteID, aErrorCode - should not check dn_gen_percentage_map");

            return null;
        }

        maskedRouteIDOnFailuerCodeToSuccessCode = getDlrPercentageMaskedRouteId(aMsgType, aPriority, aRouteId, aErrorCode);
        if (log.isDebugEnabled())
            log.debug("Dn pecentage masked value:" + maskedRouteIDOnFailuerCodeToSuccessCode);

        return maskedRouteIDOnFailuerCodeToSuccessCode;
    }

    private synchronized String getClientWiseMaskedRouteIdOnFailuerToSuccessCode(
            String aClientId,
            String aRouteId,
            String aErrorCode)
    {
        String                        returnValue              = null;
        final DlrClientPercentageInfo lDlrClientPercentageinfo = DNPUtil.getDlrClientPercentageInfo(aClientId, aRouteId, aErrorCode);

        if (log.isDebugEnabled())
            log.debug("Percentage mapping for Client Id :" + aClientId + ", RouteID :" + aRouteId + ", ErrorCode :" + aErrorCode + " is " + lDlrClientPercentageinfo);

        if (lDlrClientPercentageinfo != null)
        {
            returnValue = TEMPORARY_VARIABLE;

            final String               lCurrentKey         = lDlrClientPercentageinfo.getCurrentKey();
            final Map<String, Integer> lCountsMap          = mClientMessageCountMap.computeIfAbsent(lCurrentKey, k -> new HashMap<>());
            int                        lTotalErrorCount    = lCountsMap.computeIfAbsent(TOTAL_ERROR_COUNT, k -> 0);
            int                        lError2SuccessCount = lCountsMap.computeIfAbsent(ERROR_2_SUCCESS_COUNT, k -> 0);

            // however increase the total error count here.
            lTotalErrorCount++;

            // calculate the convertible ErrorCount
            final int tobeConvertableErrorCount = (int) Math.floor(lTotalErrorCount * lDlrClientPercentageinfo.getPercentage());

            if (log.isDebugEnabled())
                log.debug("Map Key :" + lCurrentKey //
                        + ", Previous Total Error Count :" + lTotalErrorCount //
                        + ", Previous Error to Success Count : " + lError2SuccessCount //
                        + ", TobeConvertableErrorCount : " + tobeConvertableErrorCount);

            final boolean lCanConvert = tobeConvertableErrorCount > lError2SuccessCount;

            if (lCanConvert)
            {
                lError2SuccessCount++;
                returnValue = lDlrClientPercentageinfo.getMaskedRouteId();
            }

            if (log.isDebugEnabled())
                log.debug("canConvert : '" + lCanConvert + "' --> Masked Id : '" + returnValue + "'");

            lCountsMap.put(TOTAL_ERROR_COUNT, lTotalErrorCount);
            lCountsMap.put(ERROR_2_SUCCESS_COUNT, lError2SuccessCount);
        }
        return returnValue;
    }

    public synchronized String getDlrPercentageMaskedRouteId(
            String aMsgType,
            String aPriority,
            String aRouteID,
            String aErrorCode)
    {
        String                  returnValue        = null;
        final DlrPercentageInfo lDlrPercentageInfo = DNPUtil.getDlrPercentageInfo(aMsgType, aPriority, aRouteID, aErrorCode);

        if (log.isDebugEnabled())
            log.debug("Percentage mapping for msg_type :" + aMsgType + "aPriority:" + aPriority + ", RouteID :" + aRouteID + ", ErrorCode :" + aErrorCode + " is " + lDlrPercentageInfo);

        if (lDlrPercentageInfo != null)
        {
            final String               lCurrentKey         = lDlrPercentageInfo.getCurrentKey();
            final Map<String, Integer> lMsgCountsMap       = mMessageCountMap.computeIfAbsent(lCurrentKey, k -> new HashMap<>());
            int                        lTotalErrorCount    = lMsgCountsMap.computeIfAbsent(TOTAL_ERROR_COUNT, k -> 0);
            int                        lError2SuccessCount = lMsgCountsMap.computeIfAbsent(ERROR_2_SUCCESS_COUNT, k -> 0);

            // however increase the total error count here.
            lTotalErrorCount++;

            // calculate the convertible ErrorCount
            final int tobeConvertableErrorCount = (int) Math.floor(lTotalErrorCount * lDlrPercentageInfo.getPercentage());

            if (log.isDebugEnabled())
                log.debug("Map Key :" + lCurrentKey //
                        + ", Previous Total Error Count :" + lTotalErrorCount //
                        + ", Previous Error to Success Count : " + lError2SuccessCount //
                        + ", TobeConvertableErrorCount : " + tobeConvertableErrorCount);

            final boolean lCanConvert = tobeConvertableErrorCount > lError2SuccessCount;

            if (lCanConvert)
            {
                lError2SuccessCount++;
                returnValue = lDlrPercentageInfo.getMaskedRouteId();
            }

            if (log.isDebugEnabled())
                log.debug("canConvert : '" + lCanConvert + "' --> Masked Id : '" + returnValue + "'");

            lMsgCountsMap.put(TOTAL_ERROR_COUNT, lTotalErrorCount);
            lMsgCountsMap.put(ERROR_2_SUCCESS_COUNT, lError2SuccessCount);
        }
        return returnValue;
    }

}
