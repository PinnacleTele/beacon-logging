package com.itextos.beacon.inmemory.dnpayload.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.dnpayload.cache.AgingInfo;
import com.itextos.beacon.inmemory.dnpayload.cache.ClientIngoreDnGenerate;
import com.itextos.beacon.inmemory.dnpayload.cache.CustomSlabs;
import com.itextos.beacon.inmemory.dnpayload.cache.DlrClientPercentage;
import com.itextos.beacon.inmemory.dnpayload.cache.DlrExclude;
import com.itextos.beacon.inmemory.dnpayload.cache.DlrPercentage;
import com.itextos.beacon.inmemory.dnpayload.cache.ExcludeCircleInfo;
import com.itextos.beacon.inmemory.dnpayload.cache.PayloadExpiry;
import com.itextos.beacon.inmemory.dnpayload.cache.pojo.DlrClientPercentageInfo;
import com.itextos.beacon.inmemory.dnpayload.cache.pojo.DlrPercentageInfo;
import com.itextos.beacon.inmemory.dnpayload.slab.ChildSlab;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class DNPUtil
{

    private static final Log log = LogFactory.getLog(DNPUtil.class);

    private DNPUtil()
    {}

    public static List<String> getExcudeCircles(
            String aClientId)
    {
        final ExcludeCircleInfo lExcludeCircleInfo = (ExcludeCircleInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.EXCLUDE_CIRCLE);
        return lExcludeCircleInfo.getExcludeCircles(aClientId);
    }

    public static List<String> getMasterSlabList(
            String aClientId)
    {
        final CustomSlabs lCustomSlabInfo = (CustomSlabs) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_SLABS);
        return lCustomSlabInfo.getMasterSlabList(aClientId);
    }

    public static List<ChildSlab> getChildSlabList(
            String aClientId)
    {
        final CustomSlabs lCustomSlabInfo = (CustomSlabs) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_SLABS);
        return lCustomSlabInfo.getChildSlabList(aClientId);
    }

    public static DlrPercentageInfo getDlrPercentageInfo(
            String aMsgType,
            String aPriority,
            String aRouteID,
            String aErrorCode)
    {
        final DlrPercentage lDlrPercentage = (DlrPercentage) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.DLR_PERCENTAGE_INFO);

        return lDlrPercentage.getDlrPercentageInfo(aMsgType, aPriority, aRouteID, aErrorCode);
    }

    public static DlrClientPercentageInfo getDlrClientPercentageInfo(
            String aClientId,
            String aRouteId,
            String aErrorCode)
    {
        final DlrClientPercentage lDlrClientPercentage = (DlrClientPercentage) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.DLR_CLIENT_PERCENTAGE_INFO);
        return lDlrClientPercentage.getDlrClientPercentageInfo(aClientId, aRouteId, aErrorCode);
    }

    public static boolean isClientDlrExclude(
            String aClientId)
    {
        final DlrExclude lDlrExclude = (DlrExclude) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.DLR_CLIENT_EXCLUDE);
        return lDlrExclude.isClientDlrExclude(aClientId);
    }

    public static int getAgingDNInfo(
            String aClientId,
            int aRetryAttempt)
    {
        final AgingInfo lAgingInfo = (AgingInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.AGING_DN_INFO);
        return lAgingInfo.getAgingDNInfo(aClientId, String.valueOf(aRetryAttempt));
    }

    public static int getPayloadExpiry(
            String aClientId)
    {
        final PayloadExpiry lPayloadExpiry = (PayloadExpiry) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CLIENT_IGNORE_DN_GEN);
        return lPayloadExpiry.getExpiry(aClientId);
    }

    public static boolean isCircleInExcludeList(
            String aClientId,
            String aCircle)
    {

        try
        {
            final List<String> lExcludeCircles = getExcudeCircles(aClientId);
            if (log.isDebugEnabled())
                log.debug("Exclude Circle List:" + lExcludeCircles);

            final String tempCircle = aCircle.toLowerCase();

            if (lExcludeCircles != null)
                return lExcludeCircles.contains(tempCircle);
        }
        catch (final Exception e)
        {
            log.error("isCircleInExcludeList() exception", e);
        }
        return false;
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    public static boolean isDlrGenIgnoreable(
            String aClientId)
    {
        final ClientIngoreDnGenerate lClientIgnoreDlrGen = (ClientIngoreDnGenerate) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.IGNORE_DLR_GEN);
        return lClientIgnoreDlrGen.isIgnoreAcc(aClientId);
    }

}
