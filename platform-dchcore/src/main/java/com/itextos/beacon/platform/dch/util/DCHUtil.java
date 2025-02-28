package com.itextos.beacon.platform.dch.util;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.errorinfo.ErrorCodeUtil;
import com.itextos.beacon.inmemory.errorinfo.data.CarrierErrorInfo;
import com.itextos.beacon.inmemory.errorinfo.data.ErrorCategory;
import com.itextos.beacon.inmemory.errorinfo.data.PlatformErrorInfo;

public class DCHUtil
{

    private static final Log log = LogFactory.getLog(DCHUtil.class);

    private DCHUtil()
    {}

    public static DeliveryObject processDR(
            SubmissionObject aSubmissionObject)
            throws Exception
    {
        final DeliveryObject lDeliveryObject = aSubmissionObject.getDeliveryObject();
        lDeliveryObject.setDlrFromInternal("dummyroute_dlr_came_from_MW");

        resetWalletInfo(lDeliveryObject);

        String lCarrierAckId     = null;
        String lCarrierStatus    = null;
        String lCarrierErrorCode = null;

        String lCarrierFullDn    = CommonUtility.nullCheck(aSubmissionObject.getCarrierFullDn(), true);

        if (log.isDebugEnabled())
            log.debug("carrier_full_dn received from route_configuration is:" + lCarrierFullDn);

        final Date   lSubAndDoneDate = getSubAndDnDate(aSubmissionObject);
        final String subAndDoneDate  = DateTimeUtility.getFormattedDateTime(lSubAndDoneDate, DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM);
        lCarrierFullDn = lCarrierFullDn.replace("{0}", subAndDoneDate);
        lCarrierFullDn = lCarrierFullDn.replace("{1}", subAndDoneDate);

        lCarrierFullDn = lCarrierFullDn.toUpperCase();

        try
        {
            if (lCarrierFullDn.indexOf("ID:") != -1)
                lCarrierAckId = lCarrierFullDn.substring(lCarrierFullDn.indexOf("ID:") + 3, lCarrierFullDn.indexOf(" ", lCarrierFullDn.indexOf("ID:") + 3));
            if (lCarrierFullDn.indexOf("STAT:") != -1)
                lCarrierStatus = lCarrierFullDn.substring(lCarrierFullDn.indexOf("STAT:") + 5, lCarrierFullDn.indexOf(" ", lCarrierFullDn.indexOf("STAT:") + 5)).trim();
            if (lCarrierFullDn.indexOf("ERR:") != -1)
                lCarrierErrorCode = lCarrierFullDn.substring(lCarrierFullDn.indexOf("ERR:") + 4, lCarrierFullDn.indexOf(" ", lCarrierFullDn.indexOf("ERR:") + 4)).trim();
        }
        catch (final Exception e)
        {
            log.error("set carrier_full_dn value properly in route_config table for the route:" + aSubmissionObject.getRouteId());
        }

        if (log.isDebugEnabled())
            log.debug("before swapping all values carrier_full_dn:" + lCarrierFullDn + " id:" + lCarrierAckId + " stat:" + lCarrierStatus + " err:" + lCarrierErrorCode + " subAndDoneDate:"
                    + subAndDoneDate);

        lCarrierStatus    = lCarrierStatus == null ? "DELIVRD" : lCarrierStatus;
        lCarrierErrorCode = lCarrierErrorCode == null ? "000" : lCarrierErrorCode;
        lCarrierAckId     = lCarrierAckId == null ? "1" : lCarrierAckId;

        final String lRouteId = CommonUtility.nullCheck(aSubmissionObject.getRouteId(), true);

        lDeliveryObject.setCarrierFullDn(lCarrierFullDn);
        lDeliveryObject.setCarrierReceivedTime(lSubAndDoneDate);
        lDeliveryObject.setDeliveryTime(lSubAndDoneDate);
        lDeliveryObject.setCarrierStatusCode(lCarrierErrorCode);
        lDeliveryObject.setCarrierOrigianlStatusCode(lCarrierErrorCode);
        lDeliveryObject.setCarrierAcknowledgeId(lCarrierAckId);
        lDeliveryObject.setCarrierSystemId("smpp");
        lDeliveryObject.setSmscId("smpp");

        setPlatformErrorCodeBasedOnCarrierErrorCode(lDeliveryObject);
        if (log.isDebugEnabled())
            log.debug("After swapping all values carrier_full_dn:" + lCarrierFullDn + " id:" + lCarrierAckId + " stat:" + lCarrierStatus + " err:" + lCarrierErrorCode + " subAndDoneDate:"
                    + subAndDoneDate + " status_id:" + lDeliveryObject.getSubOriStatusCode());

        lDeliveryObject.setDnOriStatusDesc(lCarrierStatus);
        lDeliveryObject.setCarrierDeliveryStatus(lCarrierStatus);

        return lDeliveryObject;
    }

    private static Date getSubAndDnDate(
            SubmissionObject aSubmissionObject)
    {
        Date lActualTsDate = null;

        try
        {
            lActualTsDate = aSubmissionObject.getActualCarrierSubmitTime();
            if (log.isDebugEnabled())
                log.debug("Actual_ts:" + lActualTsDate);
            return lActualTsDate;
        }
        catch (final Exception e)
        {
            log.error("Exception while parsing and getting SubAndDnDate:", e);
            lActualTsDate = new Date();
            return lActualTsDate;
        }
    }

    public static void setPlatformErrorCodeBasedOnCarrierErrorCode(
            DeliveryObject aDeliveryObject)
    {
        final CarrierErrorInfo ceii          = (CarrierErrorInfo) ErrorCodeUtil.getCarrierErrorCode(aDeliveryObject.getRouteId(), aDeliveryObject.getCarrierStatusCode(),
                aDeliveryObject.getCarrierStatusDesc());
        final String           dnErrorStatus = ceii.getPlatformErrorCode();

        if (log.isDebugEnabled())
            log.debug("Carrier Error Status : " + dnErrorStatus);

        final PlatformErrorInfo lPlatformErrorInfo = (PlatformErrorInfo) ErrorCodeUtil.getPlatformErrorCode(ErrorCategory.OPERATOR, dnErrorStatus);

        if (log.isDebugEnabled())
            log.debug("Platform Error Code : " + lPlatformErrorInfo.toString());

        aDeliveryObject.setSubOriStatusCode(lPlatformErrorInfo.getErrorCode());
        aDeliveryObject.setDnOrigianlstatusCode(lPlatformErrorInfo.getErrorCode());
        aDeliveryObject.setDnOriStatusDesc(lPlatformErrorInfo.getDisplayError());
        aDeliveryObject.setDnFilureType(ceii.getFailureType().getKey());
        aDeliveryObject.setDeliveryStatus(lPlatformErrorInfo.getStatusFlag().getKey());
    }

    private static void resetWalletInfo(
            DeliveryObject aDeliveryObject)
    {
        aDeliveryObject.setBaseSmsRate(0.0d);
        aDeliveryObject.setBaseAddFixedRate(0.0d);
        aDeliveryObject.setBillingSmsRate(0.0d);
        aDeliveryObject.setBillingAddFixedRate(0.0d);
    }

}
