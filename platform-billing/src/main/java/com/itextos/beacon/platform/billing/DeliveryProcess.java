package com.itextos.beacon.platform.billing;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.BillType;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.errorlog.SMSLog;
import com.itextos.beacon.inmemory.errorinfo.ErrorCodeUtil;
import com.itextos.beacon.inmemory.errorinfo.data.ErrorCategory;
import com.itextos.beacon.inmemory.errorinfo.data.IErrorInfo;
import com.itextos.beacon.inmemory.errorinfo.data.PlatformErrorInfo;
import com.itextos.beacon.platform.billing.support.BillingUtility;
import com.itextos.beacon.platform.dnpayloadutil.common.TimeAdjustmentUtility;

public class DeliveryProcess
        extends
        CommonProcess
{

    private static final Log log               = LogFactory.getLog(DeliveryProcess.class);

    private boolean          isInterfaceReject = false;
    private boolean          isPlatformReject  = false;
    private DeliveryObject   mDeliveryObject;

    public DeliveryProcess(
            DeliveryObject aDeliveryObject)
    {
        super(aDeliveryObject);

        isInterfaceReject = aDeliveryObject.isInterfaceRejected();
        isPlatformReject  = aDeliveryObject.isPlatfromRejected();
    }

    @Override
    public void process(SMSLog sb)
    {
        mDeliveryObject = (DeliveryObject) mBaseMessage;

        identifySuffix();

        updatePartNumberDetails();

        updateDTime();

        updateTerminatorCarrierCircle();

        String lDndDeliveryStatus  = BillingUtility.getAppConfigValueAsString(ConfigParamConstants.DELV_DND_STATUS);
        String lDndDeliveryRouteId = BillingUtility.getAppConfigValueAsString(ConfigParamConstants.DELV_DND_ROUTE_ID);
        lDndDeliveryStatus  = lDndDeliveryStatus == null ? "" : lDndDeliveryStatus;
        lDndDeliveryRouteId = lDndDeliveryRouteId == null ? "" : lDndDeliveryRouteId;

        if ((!isInterfaceReject) || ((isPlatformReject) && lDndDeliveryStatus.equals(mDeliveryObject.getDnOrigianlstatusCode()) && lDndDeliveryRouteId.equals(mDeliveryObject.getRouteId())))
        {
            updateFailToSuccessCode();
            updateCarrierReceivedTime();
        }

        updateDeliveriesStatus();

        prepaidRefund();

        encryptMobile();

        updateDeliveryLatencies();
    }

    private void prepaidRefund()
    {
        final boolean isCreditCheckEnabled = CommonUtility.isEnabled(mDeliveryObject.getValue(MiddlewareConstant.MW_CREDIT_CHECK));

        if (log.isDebugEnabled())
            log.debug("Credit Check Enabled : " + isCreditCheckEnabled);

        if (isRefundRequired())
        {
            final String  lClientId            = mDeliveryObject.getClientId();
            final String  lFileId              = mDeliveryObject.getFileId();
            final String  lBaseMsgId           = mDeliveryObject.getBaseMessageId();
            final String  lMsgId               = mDeliveryObject.getMessageId();
            final String  lReason              = mDeliveryObject.getDnClientStatusDesc();
            final double  lSmsRate             = mDeliveryObject.getBillingSmsRate();
            final boolean isIntl               = mDeliveryObject.isIsIntl();
            boolean       isDltRefund          = false;
            double        lBillingAddFixedRate = 0.0d;
            double        lBaseAddFixedRate    = 0.0d;
            double        lRefAddFixedRate     = 0.0d;

            if (isDltRefundabel(mDeliveryObject.getDnOrigianlstatusCode()))
            {
                isDltRefund          = true;
                lBillingAddFixedRate = mDeliveryObject.getBillingAddFixedRate();
                lBaseAddFixedRate    = mDeliveryObject.getBaseAddFixedRate();
                lRefAddFixedRate     = mDeliveryObject.getRefAddFixedRate();
            }

            final int lTotalParts = 1;

            if (log.isDebugEnabled())
                log.debug("Wallet Deduct SMS Rate :  '" + lSmsRate + "', Billing Add Fixed Rate:'" + lBillingAddFixedRate + "'");

            doWalletRefund(lClientId, lFileId, lBaseMsgId, lMsgId, lTotalParts, lSmsRate, lBillingAddFixedRate, lReason, isIntl);

            setSmsRate(lSmsRate, lBillingAddFixedRate, lBaseAddFixedRate, lRefAddFixedRate, isDltRefund);
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("Other Billing cases...");

            // Doing refund for NACK with post paid accounts.
            if (((mDeliveryObject.getBillType() == CommonUtility.getInteger(BillType.POST_PAID.getKey())) && !isCreditCheckEnabled) && isDltRefundabel(mDeliveryObject.getDnOrigianlstatusCode()))
                setSmsRate(mDeliveryObject.getBillingSmsRate(), mDeliveryObject.getBillingAddFixedRate(), mDeliveryObject.getBaseAddFixedRate(), mDeliveryObject.getRefAddFixedRate(), true);

            else
                resetWalletInfo();
        }
    }

    private void resetWalletInfo()
    {
        mDeliveryObject.setBaseSmsRate(0.0d);
        mDeliveryObject.setBaseAddFixedRate(0.0d);
        mDeliveryObject.setBillingSmsRate(0.0d);
        mDeliveryObject.setBillingAddFixedRate(0.0d);
        mDeliveryObject.setRefSmsRate(0.0d);
        mDeliveryObject.setRefAddFixedRate(0.0d);
    }

    private void setSmsRate(
            double aBillingSmsRate,
            double aBillingAddFixedRate,
            double aBaseAddFixedRate,
            double aRefAddFixedRate,
            boolean isDltRefund)
    {
        mDeliveryObject.setBillingSmsRate(getNagativeValue(aBillingSmsRate));
        mDeliveryObject.setBaseSmsRate(getNagativeValue(mDeliveryObject.getBaseSmsRate()));
        mDeliveryObject.setRefSmsRate(getNagativeValue(mDeliveryObject.getRefSmsRate()));

        if (isDltRefund)
        {
            mDeliveryObject.setBillingAddFixedRate(getNagativeValue(aBillingAddFixedRate));
            mDeliveryObject.setBaseAddFixedRate(getNagativeValue(aBaseAddFixedRate));
            mDeliveryObject.setRefAddFixedRate(getNagativeValue(aRefAddFixedRate));
        }
        else
        {
            mDeliveryObject.setBillingAddFixedRate(aBillingAddFixedRate);
            mDeliveryObject.setBaseAddFixedRate(aBaseAddFixedRate);
            mDeliveryObject.setRefAddFixedRate(aRefAddFixedRate);
        }
    }

    private static double getNagativeValue(
            double aNumber)
    {
        if (aNumber > 0)
            return aNumber *= -1;

        return 0.0d;
    }

    private boolean isRefundRequired()
    {
        final String lDnErrorCode = mDeliveryObject.getDnOrigianlstatusCode();
        if (log.isDebugEnabled())
            log.debug("Delivery Status Code : " + lDnErrorCode);

        final boolean isCreditCheckEnabled = CommonUtility.isEnabled(mDeliveryObject.getValue(MiddlewareConstant.MW_CREDIT_CHECK));

        if (log.isDebugEnabled())
            log.debug("Credit Check Enabled : " + isCreditCheckEnabled);

        return ((((mDeliveryObject.getBillType() == CommonUtility.getInteger(BillType.PRE_PAID.getKey())) || isCreditCheckEnabled) && !isPlatformReject && !isInterfaceReject
                && mDeliveryObject.isWalletDeduct() && isRefundabel(lDnErrorCode) && isIntlRefund(lDnErrorCode) && canRefundBasedOnInvoice()));
    }

    private boolean canRefundBasedOnInvoice()
    {
        if (log.isDebugEnabled())
            log.debug("Invoice based on : " + mDeliveryObject.getInvoiceBasedOn());

        return CommonUtility.isEnabled(mDeliveryObject.getInvoiceBasedOn());
    }

    private boolean isRefundabel(
            String aDnErrorCode)
    {
        ErrorCategory lCategory = ErrorCategory.OPERATOR;
        if (isInterfaceReject)
            lCategory = ErrorCategory.INTERFACE;
        else
            if (isPlatformReject)
                lCategory = ErrorCategory.PLATFORM;

        final PlatformErrorInfo lPlatformErrorInfo     = (PlatformErrorInfo) ErrorCodeUtil.getPlatformErrorCode(lCategory, aDnErrorCode);

        final boolean           isDomSmsRateRefundable = lPlatformErrorInfo.isDomSmsRateRefundable();

        if (log.isDebugEnabled())
        {
            log.debug("IS Domestic SMS Rate Refundable ? " + isDomSmsRateRefundable);
            log.debug("Operator Mapping error code : " + lPlatformErrorInfo.getErrorCode());
        }

        return isDomSmsRateRefundable;
    }

    private boolean isDltRefundabel(
            String aDnErrorCode)
    {
        ErrorCategory lCategory = ErrorCategory.OPERATOR;
        if (isInterfaceReject)
            lCategory = ErrorCategory.INTERFACE;
        else
            if (isPlatformReject)
                lCategory = ErrorCategory.PLATFORM;

        final PlatformErrorInfo lPlatformErrorInfo     = (PlatformErrorInfo) ErrorCodeUtil.getPlatformErrorCode(lCategory, aDnErrorCode);

        final boolean           isDomDltRateRefundable = lPlatformErrorInfo.isDomDltRateRefundable();

        if (log.isDebugEnabled())
            log.debug("IS Domestic DLT Rate Refundable ? " + isDomDltRateRefundable);

        return isDomDltRateRefundable;
    }

    private boolean isIntlRefund(
            String aDnErrorCode)
    {
        ErrorCategory lCategory = ErrorCategory.OPERATOR;
        if (isInterfaceReject)
            lCategory = ErrorCategory.INTERFACE;
        else
            if (isPlatformReject)
                lCategory = ErrorCategory.PLATFORM;

        boolean                 canRefund          = true;
        final PlatformErrorInfo lPlatformErrorInfo = (PlatformErrorInfo) ErrorCodeUtil.getPlatformErrorCode(lCategory, aDnErrorCode);

        if (mDeliveryObject.isIsIntl())
        {
            if (log.isDebugEnabled())
                log.debug("Operator Mapping error code : " + lPlatformErrorInfo.getErrorCode());

            if (lPlatformErrorInfo.getErrorCode().equals("642"))
                canRefund = true;
            else
                canRefund = false;
        }
        return canRefund;
    }

    // Deliveries
    private void updateDTime()
    {
        Date         lDTime               = null;
        Date         lActualDTime         = null;

        // for all failures we should have null for dtime column

        final String lDnDefaultStatusCode = getDNDeliveredStatusCode();

        if (lDnDefaultStatusCode.equals(mDeliveryObject.getDnOrigianlstatusCode()))
        {
            lDTime       = mDeliveryObject.getDeliveryTime();
            lActualDTime = mDeliveryObject.getActualDeliveryTime();
        }

        if (lDTime != null)
            mDeliveryObject.setDeliveryTime(lDTime);

        if (lActualDTime != null)
            mDeliveryObject.setDeliveryTime(lActualDTime);
    }

    private void updateTerminatorCarrierCircle()
    {
        String termCarrier = CommonUtility.nullCheck(mDeliveryObject.getCarrier(), true);
        String termCircle  = CommonUtility.nullCheck(mDeliveryObject.getCircle(), true);

        // final String middlwareRejectedTermOperatorCircle =
        // mMessageRequest.getValue(MiddlewareConstant.MW_REJ_TERMOPERATOR_CIRCLE_NULL);

        // For all middle ware rejections sms_deliveris.term_operator and term_circle
        // should be null
        if (isPlatformReject)
        {
            termCarrier = null;
            termCircle  = null;
        }

        mDeliveryObject.setTerminatedCarrier(termCarrier);
        mDeliveryObject.setTerminatedCircle(termCircle);
    }

    private void updateDeliveriesStatus()
    {
        // dn_ori
        // dn_cli

        final String dnErrorCode = mDeliveryObject.getDnOrigianlstatusCode();
        final String lClientId   = mDeliveryObject.getClientId();

        if (log.isDebugEnabled())
            log.debug("Dn Status Code : '" + dnErrorCode + ", ClientId : '" + lClientId + "'");

        final IErrorInfo cei = ErrorCodeUtil.getClientErrorCode(lClientId, dnErrorCode);

        if (log.isDebugEnabled())
            log.debug("Client Error Code info : " + cei.toString());

        mDeliveryObject.setDnClientStatusCode(cei.getErrorCode());
        mDeliveryObject.setDnClientStatusDesc(cei.getErrorDesc());
    }

    private void updateCarrierReceivedTime()
    {
        TimeAdjustmentUtility.adjustAndSetDTime(mDeliveryObject);
    }

    private void updateDeliveryLatencies()
    {
        final String lDnDefaultStatusCode         = getDNDeliveredStatusCode();
        final long[] calculateDeliveriesLatencies = TimeAdjustmentUtility.calculateDeliveryLatencies(mDeliveryObject);

        // failure delivery - insert 0 for DELIVE_LATENCY_SLA_SEC column
        if (!CommonUtility.nullCheck(mDeliveryObject.getDnOrigianlstatusCode()).equals(lDnDefaultStatusCode))
            calculateDeliveriesLatencies[1] = 0;

        mDeliveryObject.setDnLatencyOrigianlInMillis(calculateDeliveriesLatencies[0]);
        mDeliveryObject.setDnLatencySlaInMillis(calculateDeliveriesLatencies[1]);
        mDeliveryObject.putValue(MiddlewareConstant.MW_OVERALL_LATENCY_IN_MILLIS, Long.toString(calculateDeliveriesLatencies[2]));
    }

    private static String getDNDeliveredStatusCode()
    {
        String lDnDefaultStatusCode = CommonUtility.nullCheck(BillingUtility.getAppConfigValueAsString(ConfigParamConstants.DN_DELV_STATUS_CODE), true);
        if (lDnDefaultStatusCode.isBlank())
            lDnDefaultStatusCode = "699";

        return lDnDefaultStatusCode;
    }

    private void updateFailToSuccessCode()
    {
        // Adjust here
        TimeAdjustmentUtility.maskFailToSuccessCode(mDeliveryObject);
    }

    private void updatePartNumberDetails()
    {

        if (isInterfaceReject || isPlatformReject)
        {
            if (log.isDebugEnabled())
                log.debug("Update the rejection details.");

            updateRejectionInfo();
        }
        else
        {
            final int curPartNo = mDeliveryObject.getMessageTotalParts();

            if (curPartNo == 0)
            {
                // Make sure that for a single part message set the part values as 1.
                mDeliveryObject.setMessagePartNumber(SINGLE_PART);
                mDeliveryObject.setMessageTotalParts(SINGLE_PART);
            }
        }
    }

    private void updateRejectionInfo()
    {
        final int lValue = mDeliveryObject.getMessagePartNumber();
        if (lValue == 0)
            mDeliveryObject.setMessagePartNumber(NO_PARTS);

        if (!PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED.getStatusCode().equals(mDeliveryObject.getDnOrigianlstatusCode()))
            mDeliveryObject.setMessageTotalParts(SINGLE_PART);

        /*
         * lValue = mDeliveryObject.getMessageTotalParts();
         * if (lValue == 0)
         * mDeliveryObject.setMessageTotalParts(NO_PARTS);
         */
        // Update the Carrier & Circle to DUMMY.
        mDeliveryObject.setCarrier(DUMMY_CARRIER);
        mDeliveryObject.setCircle(DUMMY_CIRCLE);
    }

    private void encryptMobile()
    {
        BillingUtility.encryptMobile(mDeliveryObject);
    }

}