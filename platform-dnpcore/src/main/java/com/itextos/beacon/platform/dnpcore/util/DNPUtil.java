package com.itextos.beacon.platform.dnpcore.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.customfeatures.InmemAccountDnTypeMappingInfo;
import com.itextos.beacon.inmemory.customfeatures.InmemCustomFeatures;
import com.itextos.beacon.inmemory.customfeatures.pojo.DlrTypeInfo;
import com.itextos.beacon.inmemory.errorinfo.ErrorCodeUtil;
import com.itextos.beacon.inmemory.errorinfo.data.CarrierErrorInfo;
import com.itextos.beacon.inmemory.errorinfo.data.ErrorCategory;
import com.itextos.beacon.inmemory.errorinfo.data.PlatformErrorInfo;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class DNPUtil
{

    private static final String UNKNOWN_ERROR = "-999";
    private static final String NACK_ERROR    = PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.NACK_ERROR_CODE);
    private static final Log    log           = LogFactory.getLog(DNPUtil.class);

    private DNPUtil()
    {}

    public static DeliveryObject processDR(
            DeliveryObject aDeliveryObject)
    {
        String lCarrierAckId      = null;
        String lCarrierSubmitDate = null;
        String lCarrierDoneDate   = null;
        String lCarrierStatus     = null;
        String lCarrierErrorCore  = null;

        String lCarrierDN         = CommonUtility.nullCheck(aDeliveryObject.getCarrierFullDn(), true);
        lCarrierDN = lCarrierDN.toUpperCase();

        final int idIndex = lCarrierDN.indexOf("ID:");

        if (idIndex == -1)
        {
            if (log.isDebugEnabled())
                log.debug("Carrier Full DN: " + lCarrierDN);
            aDeliveryObject.setCarrierAcknowledgeId("0");
            aDeliveryObject.setCarrierReceivedTime(DateTimeUtility.getDateFromString(lCarrierSubmitDate, aDeliveryObject.getCarrierDateTimeFormat()));
            aDeliveryObject.setDeliveryTime(DateTimeUtility.getDateFromString(lCarrierDoneDate, aDeliveryObject.getCarrierDateTimeFormat()));
            aDeliveryObject.setActualDeliveryTime(DateTimeUtility.getDateFromString(lCarrierDoneDate, aDeliveryObject.getCarrierDateTimeFormat()));
            aDeliveryObject.setCarrierOrigianlStatusDesc("NACK_ERROR");
            aDeliveryObject.setCarrierOrigianlStatusCode(lCarrierErrorCore == null ? NACK_ERROR : lCarrierErrorCore);
            aDeliveryObject.setCarrierStatusDesc(aDeliveryObject.getCarrierOrigianlStatusDesc());
            aDeliveryObject.setCarrierDeliveryStatus("NACK_ERROR");
            aDeliveryObject.setCarrierStatusCode(NACK_ERROR);

            if (log.isDebugEnabled())
                log.debug("process Nack dr - " + aDeliveryObject);
        }
        else
        {
            final int submitDateIndex = lCarrierDN.indexOf("SUBMIT DATE:");
            final int doneDateIndex   = lCarrierDN.indexOf("DONE DATE:");
            final int statusIndex     = lCarrierDN.indexOf("STAT:");
            final int errorIndex      = lCarrierDN.indexOf("ERR:");

            if (idIndex != -1)
                lCarrierAckId = lCarrierDN.substring(idIndex + 3, lCarrierDN.indexOf(" ", idIndex + 3));
            if (submitDateIndex != -1)
                lCarrierSubmitDate = lCarrierDN.substring(submitDateIndex + 12, doneDateIndex).trim();
            if (doneDateIndex != -1)
                lCarrierDoneDate = lCarrierDN.substring(doneDateIndex + 10, statusIndex).trim();
            if (statusIndex != -1)
                lCarrierStatus = lCarrierDN.substring(statusIndex + 5, lCarrierDN.indexOf(" ", statusIndex + 5)).trim();
            if (errorIndex != -1)
                lCarrierErrorCore = lCarrierDN.substring(errorIndex + 4, lCarrierDN.indexOf(" ", errorIndex + 4)).trim();

            aDeliveryObject.setCarrierAcknowledgeId(lCarrierAckId);
            aDeliveryObject.setCarrierReceivedTime(DateTimeUtility.getDateFromString(lCarrierSubmitDate, aDeliveryObject.getCarrierDateTimeFormat()));
            aDeliveryObject.setDeliveryTime(DateTimeUtility.getDateFromString(lCarrierDoneDate, aDeliveryObject.getCarrierDateTimeFormat()));
            aDeliveryObject.setActualDeliveryTime(DateTimeUtility.getDateFromString(lCarrierDoneDate, aDeliveryObject.getCarrierDateTimeFormat()));
            aDeliveryObject.setCarrierOrigianlStatusDesc(lCarrierStatus);
            aDeliveryObject.setCarrierOrigianlStatusCode(lCarrierErrorCore == null ? UNKNOWN_ERROR : lCarrierErrorCore);
            aDeliveryObject.setCarrierStatusDesc(aDeliveryObject.getCarrierOrigianlStatusDesc());
            aDeliveryObject.setCarrierDeliveryStatus(aDeliveryObject.getCarrierOrigianlStatusDesc());
            aDeliveryObject.setCarrierStatusCode(aDeliveryObject.getCarrierOrigianlStatusCode());
        }

        if (log.isDebugEnabled())
            log.debug("process dr - " + aDeliveryObject);

        return aDeliveryObject;
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

    public static String getAppConfigValueAsString(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aConfigParamConstant.getKey());
    }

    public static int getAppConfigValueAsInt(
            ConfigParamConstants aConfigParamConstant)
    {
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return CommonUtility.getInteger(lAppConfiguration.getConfigValue(aConfigParamConstant.getKey()));
    }

    public static String getCutomFeatureValue(
            String aClientId,
            CustomFeatures aCustomFeature)
    {
        final InmemCustomFeatures lCustomFeatures = (InmemCustomFeatures) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_FEATURES);
        return lCustomFeatures.getValueOfCustomFeature(aClientId, aCustomFeature.getKey());
    }

    public static DlrTypeInfo getDnTypeInfo(
            String aClientId)
    {
        final InmemAccountDnTypeMappingInfo lDnTypeInfo = (InmemAccountDnTypeMappingInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.DN_PROCESS_TYPE_CONFIG);
        return lDnTypeInfo.getDnTypeInfo(aClientId);
    }

}