package com.itextos.beacon.inmemory.routeinfo.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteConstants;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.commonlib.utility.RoundRobin;
import com.itextos.beacon.inmemory.commonlib.promoheaderpool.RandomHeaderPool;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.promoheaderpool.CustomPromoHeaderPool;
import com.itextos.beacon.inmemory.routeinfo.cache.AlternateHeaderRoute;
import com.itextos.beacon.inmemory.routeinfo.cache.CustomRouteTemplates;
import com.itextos.beacon.inmemory.routeinfo.cache.FixedHeaderRoute;
import com.itextos.beacon.inmemory.routeinfo.cache.GroupRoutesInfo;
import com.itextos.beacon.inmemory.routeinfo.cache.LoadCustomerRouteInfo;
import com.itextos.beacon.inmemory.routeinfo.cache.LoadDefaultRoute;
import com.itextos.beacon.inmemory.routeinfo.cache.LoadFirstAttemptMobileRoute;
import com.itextos.beacon.inmemory.routeinfo.cache.LoadHeeaderStatus;
import com.itextos.beacon.inmemory.routeinfo.cache.LoadMaskHeaderPool;
import com.itextos.beacon.inmemory.routeinfo.cache.LoadOnnetTableInfo;
import com.itextos.beacon.inmemory.routeinfo.cache.PriorityHeaderOpenRoute;
import com.itextos.beacon.inmemory.routeinfo.cache.RouteConfigInfo;
import com.itextos.beacon.inmemory.routeinfo.cache.RouteConfiguration;

public class RouteUtil
{

    private static final Log log = LogFactory.getLog(RouteUtil.class);

    private RouteUtil()
    {}

    public static Map<String, String> getThirdAttemptHeaderAltRoutes()
    {
        final Map<String, String>     mThirdAttemptHeaderAltRoutes = new HashMap<>();
        final AlternateHeaderRoute    lAlternateHeaderRoute        = (AlternateHeaderRoute) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ALTERNATE_HEADER_ROUTE);
        final Map<String, String>     lAlternateHeaderRoutesMap    = lAlternateHeaderRoute.getAlternateHeaderRoutes();

        final FixedHeaderRoute        lFixedHeaderRoute            = (FixedHeaderRoute) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.FIXED_HEADER_ROUTE);
        final Map<String, String>     lFixedHeaderRoutesMap        = lFixedHeaderRoute.getFixedHeaderRoutes();

        final PriorityHeaderOpenRoute lPriorityHeaderOpenRoute     = (PriorityHeaderOpenRoute) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.PRIORITY_HEADER_ROUTE);
        final Map<String, String>     lPriorityHeaderOpenRoutesMap = lPriorityHeaderOpenRoute.getPriorityHeaderOpenRoute();

        if ((!lAlternateHeaderRoutesMap.isEmpty()) || (!lPriorityHeaderOpenRoutesMap.isEmpty()) || (!lFixedHeaderRoutesMap.isEmpty()))
        {
            mThirdAttemptHeaderAltRoutes.putAll(lAlternateHeaderRoutesMap);
            mThirdAttemptHeaderAltRoutes.putAll(lPriorityHeaderOpenRoutesMap);
            mThirdAttemptHeaderAltRoutes.putAll(lFixedHeaderRoutesMap);
        }

        return mThirdAttemptHeaderAltRoutes;
    }

    public static RouteConfigInfo getRouteConfiguration(
            String aRouteId)
    {
        final RouteConfiguration lRouteCconfig = (RouteConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ROUTE_CONFIGURATION);
        return lRouteCconfig.getRouteConfig(aRouteId);
    }

    public static boolean isRouteAvailable(
            String aRouteId)
    {
        final RouteConfiguration lRouteCconfig = (RouteConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ROUTE_CONFIGURATION);
        return lRouteCconfig.isRoutePresentInRouteConfig(aRouteId);
    }

    public static String getDefaultHeaderRoute(
            String aRouteId)
    {
        final RouteConfiguration lRouteCconfig = (RouteConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ROUTE_CONFIGURATION);
        return lRouteCconfig.getDefaultHeaderMap(aRouteId);
    }

    public static boolean isRouteGroupAvailable(
            String aRouteGroup)
    {
        final GroupRoutesInfo lGrpoupRouteInfo = (GroupRoutesInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ROUTE_GROUPS);
        return lGrpoupRouteInfo.isRouteGroupAvailable(aRouteGroup);
    }

    public static List<String> getGroupRouteList(
            String aRouteGroup)
    {
        final GroupRoutesInfo lGrpoupRouteInfo = (GroupRoutesInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ROUTE_GROUPS);
        return lGrpoupRouteInfo.getRouteListFromGroup(aRouteGroup);
    }

    public static boolean isTXNRoute(
            String aRouteId)
    {
        if (aRouteId.equals(RouteConstants.EXPIRED))
            return true;

        final RouteConfigInfo routeinfo = getRouteConfiguration(aRouteId);

        return (routeinfo == null) ? false : routeinfo.isTxnRoute();
    }

    public static boolean isPromoRoute(
            String aRouteId)
    {
        if (RouteConstants.EXPIRED.equals(aRouteId))
            return true;

        final RouteConfigInfo routeinfo = getRouteConfiguration(aRouteId);
        return (routeinfo == null) ? false : routeinfo.isPromoRoute();
    }

    public static boolean isINTLRoute(
            String aRouteId)
    {
        if (RouteConstants.EXPIRED.equals(aRouteId))
            return true;

        final RouteConfigInfo routeinfo = getRouteConfiguration(aRouteId);
        return (routeinfo == null) ? false : routeinfo.isIntlRoute();
    }

    public static Integer getHeaderRouteStatus(
            String aHeaderRoute)
    {
        final LoadHeeaderStatus lHeaderRouteStatus = (LoadHeeaderStatus) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.HEADER_ROUTE_STATUS);
        return lHeaderRouteStatus.getHeaderRouteStatus(aHeaderRoute);
    }

    public static boolean getHeaderRouteAvailable(
            String aHeaderRoute)
    {
        final LoadHeeaderStatus lHeaderRouteStatus = (LoadHeeaderStatus) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.HEADER_ROUTE_STATUS);
        return (lHeaderRouteStatus == null) ? false : lHeaderRouteStatus.getHeaderRouteAvailable(aHeaderRoute);
    }

    public static String getDefaultPromoHeader(
            String aRouteId)
    {
        final RouteConfigInfo routeinfo    = getRouteConfiguration(aRouteId);
        final String          lPromoHeader = routeinfo.getPromoHeader();

        if (!lPromoHeader.isEmpty())
            return lPromoHeader;

        return null;
    }

    private static Map<String, String> getSecondAttemptCustomRoutes()
    {
        final Map<String, String>   mSecondAttemptCoustomRoutes = new HashMap<>();
        final LoadCustomerRouteInfo lCustomerRouteInfo          = (LoadCustomerRouteInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_ROUTES);
        final Map<String, String>   tempcustomRoutes            = lCustomerRouteInfo.getCustomRouteInfo();

        final Map<String, String>   onnetTableMap               = LoadOnnetTableInfo.getInstance().getOnnetTableRouteInfo();

        final LoadDefaultRoute      lDefaultRouteInfo           = (LoadDefaultRoute) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.DEFAULT_ROUTES);
        final Map<String, String>   tempdefaultroutes           = lDefaultRouteInfo.getDefaultRoutes();

        if (log.isDebugEnabled())
            log.debug("Default Route Info : " + tempdefaultroutes);

        if ((tempcustomRoutes != null) || (onnetTableMap != null) || (tempdefaultroutes != null))
        {
            mSecondAttemptCoustomRoutes.putAll(tempcustomRoutes);
            mSecondAttemptCoustomRoutes.putAll(onnetTableMap);
            mSecondAttemptCoustomRoutes.putAll(tempdefaultroutes);
        }

        return mSecondAttemptCoustomRoutes;
    }

    public static String getSecondAttemptCustomRoute(
            String aRouteId)
    {
        return getSecondAttemptCustomRoutes().get(aRouteId);
    }

    public static String getRandomHeader(
            String aRouteId)
    {
        final RandomHeaderPool lRandomHeader = (RandomHeaderPool) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.RANDOM_HEADER);
        return lRandomHeader.getRandomHeader(aRouteId);
    }

    public static boolean isCustomPromoHeaderPool(
            String aRouteId,
            String aHeader)
    {
        final CustomPromoHeaderPool lCustomPromoHeaderPool = (CustomPromoHeaderPool) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_PROMO_HEADER);
        return lCustomPromoHeaderPool.isCustomPromoHeader(aRouteId, aHeader);
    }

    public static int getRRPointer(
            String aRRPointerKey,
            int aGgroupSize)
    {
        if (log.isDebugEnabled())
            log.debug("Round Robin Poiter : '" + aRRPointerKey + "' :: GroupSize : '" + aGgroupSize + "'");

        // final String lUdh = CommonUtility.nullCheck(aMessageObject.getUdh(), true);
        // final String lFeaturecd = aMessageRequest.getFeatureCode();
        // final String lMobileNumber = aMessageRequest.getMobileNumber();
        // int lRRPointer = 0;
        //
        // if ((!lUdh.isEmpty()) &&
        // !lFeaturecd.equalsIgnoreCase(FeatureCode.SPECIAL_PORT_UNICODE_SINGLE.getKey())
        // &&
        // !lFeaturecd.equalsIgnoreCase(FeatureCode.SPECIAL_PORT_PLAIN_MESSAGE_SINGLE.getKey()))
        // try
        // {
        // String lUdhRefNo = "00";
        // if (lUdh.startsWith(UdhHeaderInfo.CONCAT_8BIT_HEADER.getKey()))
        // lUdhRefNo = lUdh.substring(6, 8);
        // else
        // if (lUdh.startsWith(UdhHeaderInfo.CONCAT_16BIT_HEADER.getKey()))
        // lUdhRefNo = lUdh.substring(6, 10);
        // else
        // if (lUdh.startsWith(UdhHeaderInfo.CONCAT_PORT_MULTI_HEADER_PREFIX.getKey()))
        // lUdhRefNo = lUdh.substring(18, 20);
        // else
        // lUdhRefNo = String.valueOf(lMobileNumber);
        //
        // final BigInteger lBigIntUdfRefNum = new BigInteger(lUdhRefNo, 16);
        //
        // lRRPointer = CommonUtility.getInteger(lBigIntUdfRefNum.toString()) %
        // aGgroupSize;
        // }
        // catch (final Exception ignore)
        // {
        // //
        // }
        // else
        return RoundRobin.getInstance().getCurrentIndex(aRRPointerKey, aGgroupSize);
    }

    public static String createMapKey(
            BaseMessage aBaseMessage,
            int aRouteLogicId,
            String aAccRouteType,
            boolean isWithOutCluster)
    {
        String                key              = null;

        final String          lClientId        = aBaseMessage.getValue(MiddlewareConstant.MW_CLIENT_ID);
        final String          lCarrier         = CommonUtility.nullCheck(aBaseMessage.getValue(MiddlewareConstant.MW_CARRIER), true).toLowerCase();
        final String          lCircle          = CommonUtility.nullCheck(aBaseMessage.getValue(MiddlewareConstant.MW_CIRCLE), true).toLowerCase();
        String                lAccRouteType    = CommonUtility.nullCheck(aBaseMessage.getValue(MiddlewareConstant.MW_ACC_DEFAULT_ROUTE_ID), true);
        final MessageType     lMsgType         = aBaseMessage.getMessageType();
        final String          lDisallowedRoute = aBaseMessage.getValue(MiddlewareConstant.MW_ROUTE_ID);
        final MessagePriority lMsgPriority     = aBaseMessage.getMessagePriority();
        final String          lHeader          = (aBaseMessage instanceof MessageRequest) ? MessageUtil.getHeaderId((MessageRequest) aBaseMessage)
                : MessageUtil.getHeaderId((DeliveryObject) aBaseMessage);

        final ClusterType     lCluster         = aBaseMessage.getClusterType();

        if (!aAccRouteType.isBlank())
            lAccRouteType = aAccRouteType;

        final ItextosClient lClient = new ItextosClient(lClientId);

        switch (aRouteLogicId)
        {
            case 2:
                key = getKey(lClient.getClientId(), lCarrier, lCircle, Constants.NULL_STRING, lMsgType.getKey());
                break;

            case 3:
                key = getKey(lClient.getClientId(), Constants.NULL_STRING, lCircle, Constants.NULL_STRING, lMsgType.getKey());
                break;

            case 4:
                key = getKey(lClient.getClientId(), lCarrier, Constants.NULL_STRING, Constants.NULL_STRING, lMsgType.getKey());
                break;

            case 5:
                key = getKey(lClient.getClientId(), Constants.NULL_STRING, Constants.NULL_STRING, Constants.NULL_STRING, lMsgType.getKey());
                break;

            case 6:
                key = getKey(lClient.getAdmin(), lCarrier, lCircle, Constants.NULL_STRING, lMsgType.getKey());
                break;

            case 7:
                key = getKey(lClient.getAdmin(), Constants.NULL_STRING, lCircle, Constants.NULL_STRING, lMsgType.getKey());
                break;

            case 8:
                key = getKey(lClient.getAdmin(), lCarrier, Constants.NULL_STRING, Constants.NULL_STRING, lMsgType.getKey());
                break;

            case 9:
                key = getKey(lClient.getAdmin(), Constants.NULL_STRING, Constants.NULL_STRING, Constants.NULL_STRING, lMsgType.getKey());
                break;

            case 10:
                key = getKey(lClient.getSuperAdmin(), lCarrier, lCircle, Constants.NULL_STRING, lMsgType.getKey());
                break;

            case 11:
                key = getKey(lClient.getSuperAdmin(), Constants.NULL_STRING, lCircle, Constants.NULL_STRING, lMsgType.getKey());
                break;

            case 12:
                key = getKey(lClient.getSuperAdmin(), lCarrier, Constants.NULL_STRING, Constants.NULL_STRING, lMsgType.getKey());
                break;

            case 13:
                key = getKey(lClient.getSuperAdmin(), Constants.NULL_STRING, Constants.NULL_STRING, Constants.NULL_STRING, lMsgType.getKey());
                break;

            case 14:
                key = getKey(Constants.NULL_STRING, lCarrier, lCircle, lAccRouteType, lMsgType.getKey());
                break;

            case 15:
                key = getKey(Constants.NULL_STRING, Constants.NULL_STRING, lCircle, lAccRouteType, lMsgType.getKey());
                break;

            case 16:
                key = getKey(Constants.NULL_STRING, lCarrier, Constants.NULL_STRING, lAccRouteType, lMsgType.getKey());
                break;

            case 17:
                key = CommonUtility.combine(String.valueOf(lMsgPriority.getPriority()), lMsgType.getKey());
                break;

            case 18:
                key = CommonUtility.combine(lHeader.toUpperCase(), lMsgType.getKey());
                break;

            case 19:
                if (!isWithOutCluster)
                    key = CommonUtility.combine(lCluster.getKey(), lDisallowedRoute, String.valueOf(lMsgPriority.getPriority()));
                else
                    key = CommonUtility.combine(lDisallowedRoute, String.valueOf(lMsgPriority.getPriority()));

                break;

            case 20:
                if (!isWithOutCluster)
                    key = CommonUtility.combine(lCluster.getKey(), Constants.NULL_STRING, String.valueOf(lMsgPriority.getPriority()));
                else
                    key = CommonUtility.combine(Constants.NULL_STRING, String.valueOf(lMsgPriority.getPriority()));

                break;

            default:
                break;
        }

        return key;
    }

    private static String getKey(
            String aClientId,
            String aCarrier,
            String aCircle,
            String aAccRouteType,
            String aMsgType)
    {
        return CommonUtility.combine(aClientId, aCarrier, aCircle, aAccRouteType, aMsgType);
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    public static List<String> getMaskedHeaderPool()
    {
        final LoadMaskHeaderPool lMaskHeaderPool = (LoadMaskHeaderPool) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.MASKED_HEADER_POOL);
        return lMaskHeaderPool.getMaskedHeaderPool();
    }

    public static String getFirstAttemptMobileRoute(
            String aMobileNumber)
    {
        final LoadFirstAttemptMobileRoute lFirstAttemptMobileRoute = (LoadFirstAttemptMobileRoute) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.MOBILE_ROUTES);
        return lFirstAttemptMobileRoute.getFirstMobileRoute(aMobileNumber);
    }

    public static Map<String, String> getCustomRouteTemplates(
            String aClientId)
    {
        final CustomRouteTemplates lCustomRouteTemplate = (CustomRouteTemplates) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_ROUTE_TEMPLATE);
        return lCustomRouteTemplate.getCustomTemplateRouteInfo(aClientId);
    }

}
