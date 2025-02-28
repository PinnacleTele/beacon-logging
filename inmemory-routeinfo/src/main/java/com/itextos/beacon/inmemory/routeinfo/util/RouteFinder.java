package com.itextos.beacon.inmemory.routeinfo.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class RouteFinder
{

    private static final Log log = LogFactory.getLog(RouteFinder.class);

    private RouteFinder()
    {}

    public static boolean setRouteTryWithHeaderFailedRoute(
            MessageRequest aMessageRequest)
    {
        return setRouteTryWithHeaderFailedRoute((BaseMessage) aMessageRequest);
    }

    public static boolean setRouteTryWithHeaderFailedRoute(
            DeliveryObject aDeliveryObject)
    {
        return setRouteTryWithHeaderFailedRoute((BaseMessage) aDeliveryObject);
    }

    private static boolean setRouteTryWithHeaderFailedRoute(
            BaseMessage aBaseMessage)
    {

        try
        {

            for (int lLogicId = 18; lLogicId < 21; lLogicId++)
            {
                final String mapkey = RouteUtil.createMapKey(aBaseMessage, lLogicId, "", false);
                if (log.isDebugEnabled())
                    log.debug("logicid:" + lLogicId + " mapkey:" + mapkey);

                String lRouteId = CommonUtility.nullCheck(RouteUtil.getThirdAttemptHeaderAltRoutes().get(mapkey), true);

                if (!lRouteId.isEmpty())
                {
                    if (log.isDebugEnabled())
                        log.debug("logicid:" + lLogicId + " mapkey:" + mapkey + " found in cache");

                    final MessageType lMsgType = aBaseMessage.getMessageType();

                    // Header_aleternative_routes
                    if ((lLogicId == 19) && StringUtils.isNumeric(lRouteId))
                    {
                        final String       lRouteGroup    = lRouteId + lMsgType.getKey();

                        final List<String> lRouteInfoList = RouteUtil.getGroupRouteList(lRouteGroup);

                        if ((lRouteInfoList != null) && !lRouteInfoList.isEmpty())
                        {
                            final int lIndex = RouteUtil.getRRPointer(lRouteGroup, lRouteInfoList.size());
                            lRouteId = lRouteInfoList.get(lIndex - 1);
                        }
                    }

                    if (((lMsgType == MessageType.PROMOTIONAL) && RouteUtil.isPromoRoute(lRouteId)) || ((lMsgType == MessageType.TRANSACTIONAL) && RouteUtil.isTXNRoute(lRouteId)))
                    {

                        if (lLogicId == 19)
                        {
                            final String  lHeader       = (aBaseMessage instanceof MessageRequest) ? MessageUtil.getHeaderId((MessageRequest) aBaseMessage)
                                    : MessageUtil.getHeaderId((DeliveryObject) aBaseMessage);

                            final boolean isValidHeader = HeaderValidation.isValidHeader(lHeader, lRouteId);

                            if (!isValidHeader)
                            {
                                // if header is neither whitelisted nor open route - continuing next logicid
                                // checks
                                if (log.isDebugEnabled())
                                    log.debug("logicid :" + lLogicId + " Header is not whiltelisted continuing next logicids****");
                                continue;
                            }
                        }

                        // mMessageRequest.setRouteId(lRouteId);
                        aBaseMessage.putValue(MiddlewareConstant.MW_ROUTE_ID, lRouteId);
                        // mMessageRequest.setRouteLogicId(lLogicId);
                        aBaseMessage.putValue(MiddlewareConstant.MW_ROUTE_LOGIC_ID, Integer.toString(lLogicId));
                        return true;
                    }
                }
            }
        }
        catch (final Exception e)
        {
            log.error("setRouteTryWithHeaderFailedRoute() exception ", e);
        }
        return false;
    }

    public static boolean setRouteTryWithDefaultRoute(
            MessageRequest aMessageRequest)
    {
        return setRouteTryWithDefaultRoute((BaseMessage) aMessageRequest);
    }

    public static boolean setRouteTryWithDefaultRoute(
            DeliveryObject aDeliveryObject)
    {
        return setRouteTryWithDefaultRoute((BaseMessage) aDeliveryObject);
    }

    private static boolean setRouteTryWithDefaultRoute(
            BaseMessage aBaseMessage)
    {

        try
        {
            final String lMapKey  = RouteUtil.createMapKey(aBaseMessage, 17, "", false);

            String       lRouteId = CommonUtility.nullCheck(RouteUtil.getSecondAttemptCustomRoute(lMapKey), true);

            if (log.isDebugEnabled())
                log.debug("SecondAttemptCustomRoute - Route Id: " + lRouteId);

            if (!lRouteId.isBlank())
            {

                if (StringUtils.isNumeric(lRouteId))
                {
                    final MessageType  lMsgType       = aBaseMessage.getMessageType();
                    final String       lRouteGroup    = lRouteId + lMsgType.getKey();

                    final List<String> lRouteInfoList = RouteUtil.getGroupRouteList(lRouteGroup);

                    if ((lRouteInfoList != null) && !lRouteInfoList.isEmpty())
                    {
                        final int lIndex = RouteUtil.getRRPointer(lMapKey, lRouteInfoList.size());
                        lRouteId = lRouteInfoList.get(lIndex - 1);
                    }
                }

                aBaseMessage.putValue(MiddlewareConstant.MW_ROUTE_ID, lRouteId);
                aBaseMessage.putValue(MiddlewareConstant.MW_ROUTE_LOGIC_ID, Integer.toString(17));

                return true;
            }
        }
        catch (final Exception e)
        {
            log.error("setRouteTryWithDefaultRoute() exception ", e);
        }

        return false;
    }

}
