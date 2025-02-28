package com.itextos.beacon.inmemory.routeinfo.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.message.MessagePart;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.routeinfo.cache.RouteConfigInfo;

public class HeaderValidation
{

    private static final Log log = LogFactory.getLog(HeaderValidation.class);

    private HeaderValidation()
    {}

    public static boolean isValidHeader(
            String aHeader,
            String aRouteId)
    {
        boolean       isOpenHeader        = true;
        boolean       isHeaderWhiteListed = false;
        final String  key                 = CommonUtility.combine(aHeader.toUpperCase(), aRouteId);

        final Integer lStatus             = RouteUtil.getHeaderRouteStatus(key);
        final int     lHeaderStatus       = lStatus == null ? -1 : lStatus;

        switch (lHeaderStatus)
        {
            case 0:
                isOpenHeader = false;
                break;

            case 1:
                isHeaderWhiteListed = true;
                break;

            default:
        }

        final RouteConfigInfo lRConfig = RouteUtil.getRouteConfiguration(aRouteId);

        if (log.isDebugEnabled())
            log.debug("Route Configuration for Route Id : " + aRouteId);

        if (CommonUtility.isEnabled(lRConfig.getHeaderWhitelisted()))
            return isOpenHeader;

        return isHeaderWhiteListed;
    }

    public static boolean isInvalidHeader(
            MessageRequest aMessageRequest)
    {
        if (log.isDebugEnabled())
            log.debug("isInValidHeader");
        final String      lRouteId       = aMessageRequest.getRouteId();
        final String      lHeader        = MessageUtil.getHeaderId(aMessageRequest);
        final int         lTotalMsgParts = aMessageRequest.getMessageTotalParts();

        final MessageType lMsgType       = aMessageRequest.getMessageType();

        if (log.isDebugEnabled())
            log.debug("isInValidHeader route :" + lRouteId + "Header :" + lHeader + "msgtype :" + lMsgType + "totalmsgcount:" + lTotalMsgParts);

        if ((lMsgType == MessageType.PROMOTIONAL) && !RouteUtil.isCustomPromoHeaderPool(lRouteId, lHeader))
        {
            if (log.isDebugEnabled())
                log.debug("check for Alternate Header");

            // fixed header from route_config
            final String lDefaultRouteHeader = CommonUtility.nullCheck(RouteUtil.getDefaultHeaderRoute(lRouteId), true);
            if (!lDefaultRouteHeader.isEmpty())
                MessageUtil.setHeaderId(aMessageRequest, lDefaultRouteHeader);
            else
                if (lTotalMsgParts == 1)
                {
                    final List<MessagePart> lMessages = aMessageRequest.getMessageParts();

                    if (lMessages.isEmpty())
                    {
                        log.error("Check why the messages are coming as empty.", new Exception());
                        return false;
                    }
                    final String lUdh = lMessages.get(0).getUdh();

                    if (lUdh.isEmpty() && setRandomHeader(aMessageRequest, lRouteId))
                    {
                        if (log.isDebugEnabled())
                            log.debug("Random Header is not set for route id : " + lRouteId);

                        if (setPromoHeader(aMessageRequest, lRouteId))
                            return true;
                    }
                }
                else
                    if (setPromoHeader(aMessageRequest, lRouteId))
                        return true;
        }
        return false;
    }

    private static boolean setPromoHeader(
            MessageRequest aMessageRequest,
            String aRouteId)
    {
        final String lPromoHeader = CommonUtility.nullCheck(RouteUtil.getDefaultPromoHeader(aRouteId), true);

        if (lPromoHeader.isEmpty())
            return true;

        MessageUtil.setHeaderId(aMessageRequest, lPromoHeader);

        return false;
    }

    private static boolean setRandomHeader(
            MessageRequest aMessageRequest,
            String aRouteId)
    {
        final String lRandomHeader = CommonUtility.nullCheck(RouteUtil.getRandomHeader(aRouteId), true);
        if (lRandomHeader.isEmpty())
            return true;

        MessageUtil.setHeaderId(aMessageRequest, lRandomHeader);

        return false;
    }

    public static boolean isInValidHeaderForAlterOrRetry(
            MessageRequest aMessageRequest,
            String aRouteId)
    {
        if (log.isDebugEnabled())
            log.debug("isInValidHeaderForAlterOrRetry");

        final MessageType lMsgType       = aMessageRequest.getMessageType();
        final String      lHeader        = MessageUtil.getHeaderId(aMessageRequest);
        final int         lTotalMsgParts = aMessageRequest.getMessageTotalParts();

        if (log.isDebugEnabled())
            log.debug("isInValidHeaderForAlterOrRetry route :" + aRouteId + "Header :" + lHeader + "msgtype :" + lMsgType + "totalmsgcount:" + lTotalMsgParts);

        if ((lMsgType == MessageType.PROMOTIONAL) && !RouteUtil.isCustomPromoHeaderPool(aRouteId, lHeader))
        {
            if (log.isDebugEnabled())
                log.debug("check for Alternate Header");

            final String lDefaultHeaderRoute = CommonUtility.nullCheck(RouteUtil.getDefaultHeaderRoute(aRouteId), true);
            if (!lDefaultHeaderRoute.isEmpty())
                MessageUtil.setHeaderId(aMessageRequest, lDefaultHeaderRoute);
            else
                if (lTotalMsgParts == 1)
                {
                    if (setRandomHeader(aMessageRequest, aRouteId))
                        return true;
                }
                else
                    return true;
        }
        else
            MessageUtil.setHeaderId(aMessageRequest, lHeader);

        return false;
    }

    public static void prefixDND(
            MessageRequest aMessageRequest)
    {
        final String lDndEnable      = CommonUtility.nullCheck(aMessageRequest.getDndPreferences(), true);
        String       lHeader         = MessageUtil.getHeaderId(aMessageRequest);
        final String lIsHeaderMasked = CommonUtility.nullCheck(aMessageRequest.getIsHeaderMasked(), true);

        if (!lDndEnable.isEmpty() && lIsHeaderMasked.isEmpty() && StringUtils.isNumeric(lHeader))
        {
            aMessageRequest.setMaskedHeader(lHeader);

            lHeader = lHeader.substring(1, lHeader.length());
            MessageUtil.setHeaderId(aMessageRequest, lDndEnable + lHeader);

            aMessageRequest.setClientHeader(MessageUtil.getHeaderId(aMessageRequest));
            aMessageRequest.setIsHeaderMasked("2");
        }
    }

}
