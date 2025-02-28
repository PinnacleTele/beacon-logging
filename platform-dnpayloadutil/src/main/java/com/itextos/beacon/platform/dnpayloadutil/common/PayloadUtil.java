package com.itextos.beacon.platform.dnpayloadutil.common;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.customfeatures.InmemCustomFeatures;
import com.itextos.beacon.inmemory.dnpayload.util.DNPUtil;
import com.itextos.beacon.inmemory.errorinfo.ErrorCodeUtil;
import com.itextos.beacon.inmemory.errorinfo.data.CarrierErrorInfo;
import com.itextos.beacon.inmemory.errorinfo.data.ErrorCategory;
import com.itextos.beacon.inmemory.errorinfo.data.PlatformErrorInfo;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.whitelistnumbers.MobileWhitelistNumbers;

public class PayloadUtil
{

    private static final Log log = LogFactory.getLog(PayloadUtil.class);

    private PayloadUtil()
    {}

    public static boolean isCircleInExcludeList(
            String aClientId,
            String aCircle)
    {

        try
        {
            final List<String> lExcludeCircles = DNPUtil.getExcudeCircles(aClientId);
            if (log.isDebugEnabled())
                log.debug("Exclude Circle List:" + lExcludeCircles);

            final String tempCircle = CommonUtility.nullCheck(aCircle, true).toLowerCase();

            if (lExcludeCircles != null)
                return lExcludeCircles.contains(tempCircle);
        }
        catch (final Exception e)
        {
            log.error("isCircleInExcludeList() exception", e);
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

    public static boolean checkNumberWhiteListed(
            String aMobileNumber)
    {
        final MobileWhitelistNumbers lWhiteListNumber = (MobileWhitelistNumbers) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.MOBILE_WHITELIST);
        return lWhiteListNumber.isNumberWhitelisted(aMobileNumber);
    }

    public static void setPlatformErrorCodeBasedOnCarrierErrorCode(
            DeliveryObject aDeliveryObject)
    {
        final CarrierErrorInfo ceii          = (CarrierErrorInfo) ErrorCodeUtil.getCarrierErrorCode(aDeliveryObject.getRouteId(), aDeliveryObject.getCarrierStatusCode(),
                aDeliveryObject.getCarrierStatusDesc());
        final String           dnErrorStatus = ceii.getPlatformErrorCode();

        if (log.isDebugEnabled())
            log.debug("Platfrom Error Code : " + dnErrorStatus);

        final PlatformErrorInfo lPlatformErrorInfo = (PlatformErrorInfo) ErrorCodeUtil.getPlatformErrorCode(ErrorCategory.OPERATOR, dnErrorStatus);

        if (log.isDebugEnabled())
            log.debug("Platfrom Error Info : " + lPlatformErrorInfo);

        aDeliveryObject.setDnOrigianlstatusCode(lPlatformErrorInfo.getErrorCode());
        aDeliveryObject.setDnOriStatusDesc(lPlatformErrorInfo.getDisplayError());
        aDeliveryObject.setDnFilureType(ceii.getFailureType().getKey());
        aDeliveryObject.setDeliveryStatus(lPlatformErrorInfo.getStatusFlag().getKey());
    }

}
