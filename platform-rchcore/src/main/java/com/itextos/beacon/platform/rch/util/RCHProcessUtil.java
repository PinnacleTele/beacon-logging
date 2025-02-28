package com.itextos.beacon.platform.rch.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.customfeatures.InmemCustomFeatures;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.routeinfo.cache.RouteConfigInfo;
import com.itextos.beacon.inmemory.routeinfo.util.RouteUtil;
import com.itextos.beacon.platform.kannelstatusupdater.process.response.KannelAvailability;

public class RCHProcessUtil
{

    private static final Log log = LogFactory.getLog(RCHProcessUtil.class);

    private RCHProcessUtil()
    {}

    public static boolean isKannelAvailable(
            String aRouteId)
    {
        return KannelAvailability.getInstance().isKannelAvailable(aRouteId, 1);
    }

    public static boolean isAbsoluteRoute(
            String aRouteId)
    {
        if (log.isDebugEnabled())
            log.debug("isAbsoluteRoute()");

        return RouteUtil.isRouteAvailable(aRouteId);
    }

    public static boolean isMsgTypeEnabled(
            MessageType aMessageType,
            String lRetryRouteId)
    {
        return (((MessageType.TRANSACTIONAL == aMessageType) && RouteUtil.isTXNRoute(lRetryRouteId)) || ((MessageType.PROMOTIONAL == aMessageType) && RouteUtil.isPromoRoute(lRetryRouteId)));
    }

    public static boolean isDummyRoute(
            String aRouteId)
    {
        if (log.isDebugEnabled())
            log.debug("isDummyRoute()");

        final RouteConfigInfo lRouteConfig = RouteUtil.getRouteConfiguration(aRouteId);
        return lRouteConfig.isDummyRoute();
    }

    public static boolean isMessageBlockout(
            MessageRequest aMessageRequest)
    {
        if (ClusterType.OTP == aMessageRequest.getClusterType())
            return false;

        return new BlockoutCheck(aMessageRequest).isBlockoutProcess();
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    @Deprecated
    public static String getAppConfigValueAsString(
            String aConfigKey)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigKey);
    }

    public static String getCutomFeatureValue(
            String aClientId,
            CustomFeatures aCustomFeature)
    {
        final InmemCustomFeatures lCustomFeatures = (InmemCustomFeatures) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_FEATURES);
        return lCustomFeatures.getValueOfCustomFeature(aClientId, aCustomFeature.getKey());
    }

}
