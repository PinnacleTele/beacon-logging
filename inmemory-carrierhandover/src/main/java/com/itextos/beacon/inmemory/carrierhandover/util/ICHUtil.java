package com.itextos.beacon.inmemory.carrierhandover.util;

import java.util.Map;

import com.itextos.beacon.inmemory.carrierhandover.ClusterDNReceiverInfo;
import com.itextos.beacon.inmemory.carrierhandover.DNReceiverInfo;
import com.itextos.beacon.inmemory.carrierhandover.KannelInfoCache;
import com.itextos.beacon.inmemory.carrierhandover.KannelInfoHolder;
import com.itextos.beacon.inmemory.carrierhandover.MessageReplaceKeywords;
import com.itextos.beacon.inmemory.carrierhandover.MessageReplaceRules;
import com.itextos.beacon.inmemory.carrierhandover.RouteKannelInfo;
import com.itextos.beacon.inmemory.carrierhandover.SmppRoutingConfig;
import com.itextos.beacon.inmemory.carrierhandover.bean.KannelInfo;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class ICHUtil
{

    private ICHUtil()
    {}

    public static boolean canReplaceKeywordInMessage(
            String aClientId,
            String aCarrier,
            String aCircle,
            String aRouteId)
    {
        final MessageReplaceRules lMessageReplaceKeywords = (MessageReplaceRules) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.MSG_REPLACE_RULES);
        return lMessageReplaceKeywords.canReplaceKeyword(aClientId, aCarrier, aCircle, aRouteId);
    }

    public static String getReplacedMessage(
            String aClientId,
            String aMessage)
    {
        final MessageReplaceKeywords lMessageReplaceKeywords = (MessageReplaceKeywords) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.MSG_REPLACE_KEYWORDS);
        return lMessageReplaceKeywords.getReplacedMessage(aClientId, aMessage);
    }

    public static RouteKannelInfo getDeliveryRouteInfo(
            String aRouteId,
            String aFeatureCode)
    {
        final KannelInfoCache lKannelInfo = (KannelInfoCache) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.KANNEL_INFO);
        return lKannelInfo.getDeliveryRouteInfo(aRouteId, aFeatureCode);
    }

    public static String getAlternateRoute(
            String aRouteId)
    {
        final SmppRoutingConfig lKannelRouteInfo = (SmppRoutingConfig) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ALTERNATE_ROUTE);
        return lKannelRouteInfo.getAlternateRoute(aRouteId);
    }

    public static String getDNReceiverConnInfo(
            String aDNReceiverId)
    {
        final DNReceiverInfo lDnReceiverConnInfo = (DNReceiverInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.DN_RECEIVER_CONN_INFO);
        return lDnReceiverConnInfo.getDNReceiverConnInfo().get(aDNReceiverId);
    }

    public static String getClusterDNReceiverInfo(
            String aCluster)
    {
        final ClusterDNReceiverInfo lClusterDNReceiverInfo = (ClusterDNReceiverInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CLUSTER_DN_RECEIVER_INFO);
        return lClusterDNReceiverInfo.getDlrUrlInfo(aCluster);
    }

    public static Map<String, KannelInfo> getAllRouteConfig()
    {
        final KannelInfoHolder lKannelInfoHolder = (KannelInfoHolder) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.KANNEL_CONFIG_INFO);
        return lKannelInfoHolder.getAllRouteConfigs();
    }

    public static String getHeader(
            RouteKannelInfo routeInfo,
            String aHeader)
    {

        try
        {
            if (routeInfo.isPrefix())
                return routeInfo.getPrefix() + aHeader;
        }
        catch (final Exception ignore)
        {}

        return aHeader;
    }

}
