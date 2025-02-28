package com.itextos.beacon.platform.billing;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.BillType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.errorlog.SMSLog;
import com.itextos.beacon.inmemory.clidlrpref.ClientDlrConfig;
import com.itextos.beacon.inmemory.clidlrpref.ClientDlrConfigUtil;
import com.itextos.beacon.inmemory.errorinfo.ErrorCodeUtil;
import com.itextos.beacon.inmemory.errorinfo.data.ErrorCategory;
import com.itextos.beacon.inmemory.errorinfo.data.IErrorInfo;
import com.itextos.beacon.inmemory.errorinfo.data.PlatformErrorInfo;
import com.itextos.beacon.inmemory.msgutil.cache.CarrierCircle;
import com.itextos.beacon.platform.billing.support.BillingUtility;
import com.itextos.beacon.platform.cappingcheck.CappingIntervalType;
import com.itextos.beacon.platform.cappingcheck.CappingMessageChecker;
import com.itextos.beacon.platform.dnpayloadutil.common.TimeAdjustmentUtility;

public class SubmissionProcess
        extends
        CommonProcess
{

    private static final Log log               = LogFactory.getLog(SubmissionProcess.class);

    private boolean          isInterfaceReject = false;
    private boolean          isPlatformReject  = false;
    private boolean          isFirstPart       = false;

    private SubmissionObject mSubmissionObject;

    public SubmissionProcess(
            SubmissionObject aSubmissionObject)
    {
        super(aSubmissionObject);

        isInterfaceReject = aSubmissionObject.isInterfaceRejected();
        isPlatformReject  = aSubmissionObject.isPlatfromRejected();
    }

    // Common
    @Override
    public void process(SMSLog sb)
            throws ItextosException
    {
        mSubmissionObject = (SubmissionObject) mBaseMessage;

        identifySuffix();

        cappingCheck();

        updatePartNumberDetails();

        prepaidRefund();

        processHexMessage();

        encryptMessageAndMobile();

   //     sendToFullMessageTopic(sb);

        updateAlpha();

        updateHeaders();

        checkForGdprCompilance();

        updateSubmitDateTime();

        updateSubmissionCarrierCircle();

        updateScheduleTime();

        updateSubmissionStatus();

        updateSTSandActualSTS();

        updateFilename();

        updateCountry();

        updateSubmissionLatencies();

        updateTotalPartCount();

        sendToDlrQueryTopic(sb);

        sendToBillingTopic(sb);
    }

    private void cappingCheck()
    {
        final String  lClientId       = mSubmissionObject.getClientId();

        final boolean isCappingEnable = CommonUtility.isEnabled(mSubmissionObject.getValue(MiddlewareConstant.MW_CAPPING_CHK_ENABLED));

        if (log.isDebugEnabled())
        {
            log.debug("Processing Capping check for Message Id : " + mSubmissionObject.getBaseMessageId());
            log.debug("Time Capping Enable :" + isCappingEnable);
        }

        // Capping to be add only for success case.
        if (isCappingEnable && !isInterfaceReject && !isPlatformReject)
        {
            final String lCappingIntervalType = mSubmissionObject.getValue(MiddlewareConstant.MW_CAPPING_INTERVAL_TYPE);
            final int    lCappingInterval     = CommonUtility.getInteger(mSubmissionObject.getValue(MiddlewareConstant.MW_CAPPING_INTERVAL));
            final int    lCappingMaxReqCount  = CommonUtility.getInteger(mSubmissionObject.getValue(MiddlewareConstant.MW_CAPPING_MAX_REQ_COUNT));
            final int    lMsgTotalParts       = mSubmissionObject.getMessageTotalParts();

            if (log.isDebugEnabled())
                log.debug("Capping interval Type : '" + lCappingIntervalType + "', Capping interval : '" + lCappingInterval + "', Capping Max Req Count:'" + lCappingMaxReqCount
                        + "', Message Total Parts :'" + lMsgTotalParts + "'");

            CappingIntervalType lIntervalType = CappingIntervalType.getCappingIntervalType(lCappingIntervalType);

            if (lIntervalType == null)
                lIntervalType = CappingIntervalType.NONE;

            CappingMessageChecker.increaseMsgCounter(lClientId, lIntervalType, lCappingInterval, lCappingMaxReqCount, 1);
        }
    }

    private void updateTotalPartCount()
    {

        if (isInterfaceReject || isPlatformReject)
        {
            if (log.isDebugEnabled())
                log.debug("Update Total Parts ....");

            if (!PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED.getStatusCode().equals(mSubmissionObject.getSubOriginalStatusCode())
                    && (InterfaceType.SMPP != mSubmissionObject.getInterfaceType()))
                mSubmissionObject.setMessageTotalParts(SINGLE_PART);
        }
    }

    private void sendToBillingTopic(SMSLog sb)
    {
        PrometheusMetrics.platformIncrement(Component.T2DB_SUBMISSION, mSubmissionObject.getClusterType(), "SUB_BILLING");
        sendToOtherTopic(NextTopic.SUB_BILLING,sb);
    }

    // Submission
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
            final int curPartNo = mSubmissionObject.getMessageTotalParts();

            if (curPartNo == 0)
            {
                // Make sure that for a single part message set the part values as 1.
                mSubmissionObject.setMessagePartNumber(SINGLE_PART);
                mSubmissionObject.setMessageTotalParts(SINGLE_PART);
            }
        }

        final int curPartNo = mSubmissionObject.getMessagePartNumber();

        if (((InterfaceType.SMPP == mSubmissionObject.getInterfaceType()) && isInterfaceReject) || ((curPartNo == 0) || (curPartNo == 1)))
            isFirstPart = true;
    }

    private void updateRejectionInfo()
    {
        int lValue = mSubmissionObject.getMessagePartNumber();
        if (lValue == 0)
            mSubmissionObject.setMessagePartNumber(NO_PARTS);

        lValue = mSubmissionObject.getMessageTotalParts();
        if (lValue == 0)
            mSubmissionObject.setMessageTotalParts(NO_PARTS);

        // Update the route id to invalid.
        mSubmissionObject.setRouteId(ROUTE_INVALID);

        // Update the Carrier & Circle to DUMMY.
        mSubmissionObject.setCarrier(DUMMY_CARRIER);
        mSubmissionObject.setCircle(DUMMY_CIRCLE);
    }

    private void prepaidRefund()
    {
        // || isInterfaceReject will not be considered for the prepaid refund
        // For the first part alone, do the deduction.

        if (isInterfaceReject)
        {
            resetWalletInfo();
            return;
        }

        final boolean isCreditCheckEnabled = CommonUtility.isEnabled(mSubmissionObject.getValue(MiddlewareConstant.MW_CREDIT_CHECK));

        if (log.isDebugEnabled())
            log.debug("Credit Check Enabled : " + isCreditCheckEnabled);

        if ((mSubmissionObject.getBillType() == CommonUtility.getInteger(BillType.PRE_PAID.getKey())) || isCreditCheckEnabled)
        {
            if (log.isDebugEnabled())
                log.debug("Wallet refund calling ...");

            if (mSubmissionObject.isWalletDeduct() && isPlatformReject)
            {
                final String  lClientId      = mSubmissionObject.getClientId();
                final String  lFileId        = mSubmissionObject.getFileId();
                final String  lBaseMessageId = mSubmissionObject.getBaseMessageId();
                final String  lMessageId     = mSubmissionObject.getMessageId();
                final double  lSmsRate       = mSubmissionObject.getBillingSmsRate();
                final double  lDltRate       = mSubmissionObject.getBillingAddFixedRate();
                final String  lReason        = mSubmissionObject.getSubStatus();
                int           lTotalParts    = mSubmissionObject.getMessageTotalParts();
                final boolean isIntl         = mSubmissionObject.isIsIntl();

                if ((lTotalParts == 0) || (mSubmissionObject.getInterfaceType() == InterfaceType.SMPP))
                    lTotalParts = 1;

                if (PlatformStatusCode.PARTIALLY_CARRIER_HANDOVER_FAILED.getStatusCode().equals(mSubmissionObject.getSubOriginalStatusCode()))
                    lTotalParts = 1;

                if (log.isDebugEnabled())
                    log.debug("Wallet Deduct SMS Rate :  '" + lSmsRate + "', DLT Rate:'" + lDltRate + "'");

                doWalletRefund(lClientId, lFileId, lBaseMessageId, lMessageId, lTotalParts, lSmsRate, lDltRate, lReason, isIntl);
                resetWalletInfo();
            }

            if (!mSubmissionObject.isWalletDeduct() && isPlatformReject)
                resetWalletInfo();
        }
        else
            if (isPlatformReject)
                resetWalletInfo();
    }

    private void resetWalletInfo()
    {
        mSubmissionObject.setBillingSmsRate(0.0d);
        mSubmissionObject.setBillingAddFixedRate(0.0d);
        mSubmissionObject.setBaseSmsRate(0.0d);
        mSubmissionObject.setBaseAddFixedRate(0.0d);
        mSubmissionObject.setBillingAddFixedRate(0.0d);
        mSubmissionObject.setRefSmsRate(0.0d);
        mSubmissionObject.setRefAddFixedRate(0.0d);
        mSubmissionObject.setBillingExchangeRate(0.0d);
        mSubmissionObject.setRefExchangeRate(0.0d);
    }

    private void sendToFullMessageTopic(SMSLog sb)
    {
        // Need to send to Full Message only if the message is multipart.
        final int retryAttempt = mSubmissionObject.getRetryAttempt();
        if (isFirstPart && (retryAttempt <= 0)) {
            sendToOtherTopic(NextTopic.FULL_MESSAGE_INSERT,sb);
        }
    }

    private void encryptMessageAndMobile()
    {
        BillingUtility.encryptData(mSubmissionObject);
    }

    private void updateAlpha()
    {

        try
        {
            final String aAlpha = CommonUtility.nullCheck(mSubmissionObject.getAlpha(), true);

            // AALPHA recvd from middleware 0-Txn, 1-promo, 2-government (from
            // govt_or_exempt_senderids), 3-Exempted (from govt_or_exempt_senderids) ,
            // 4-masking (from senderid_mask)
            if (aAlpha.isEmpty())
            {
                final String header = CommonUtility.nullCheck(mSubmissionObject.getHeader(), true);

                if (!header.isEmpty())
                {
                    if (log.isDebugEnabled())
                        log.debug("Set the Alpha for Promo / Trans");
                    if (StringUtils.isNumeric(header))
                        mSubmissionObject.setAlpha(PROMO_ALPHA);
                    else
                        mSubmissionObject.setAlpha(TRANS_ALPHA);
                }
            }
        }
        catch (final Exception e)
        {
            // Ignore the exception.
        }
    }

    private void updateHeaders()
    {

        try
        {
            final String lClientHeader          = CommonUtility.nullCheck(mSubmissionObject.getClientHeader(), true);
            final String lHeader                = CommonUtility.nullCheck(mSubmissionObject.getHeader(), true);

            // TODO Need to check where are we setting that
            final String lClientHeaderInBilling = CommonUtility.nullCheck(mSubmissionObject.getValue(MiddlewareConstant.MW_ADD_SUB_CLIENT_HEADER));

            if ((lClientHeaderInBilling.isEmpty()) && !lHeader.contentEquals(lClientHeader))
                mSubmissionObject.setClientHeader(lHeader);
        }
        catch (final Exception e)
        {
            log.error("Exception while setting the customer header for the message " + mSubmissionObject, e);
        }
    }

    private void checkForGdprCompilance()
    {
        // No need to do anything as it is already handled in encryption.
    }

    private void updateSubmitDateTime()
    {
        // TODO Not sure what to do here.
    }

    private void updateSubmissionCarrierCircle()
    {

        try
        {
            String carrier = CommonUtility.nullCheck(mSubmissionObject.getCarrier());
            String circle  = CommonUtility.nullCheck(mSubmissionObject.getCircle());

            if (isPlatformReject)
            {
                final CarrierCircle carrierAndCircle = getDefaultCarrierCircle();

                if (carrierAndCircle != null)
                {
                    carrier = carrierAndCircle.getCarrier();
                    circle  = carrierAndCircle.getCircle();
                }
            }

            mSubmissionObject.setCarrier(carrier);
            mSubmissionObject.setCircle(circle);
        }
        catch (final Exception e1)
        {
            log.error("Problem in getting the Carrier and Circle for the Message '" + mSubmissionObject + "'", e1);
        }
    }

    private void updateScheduleTime()
    {
        // TODO Not sure what to do here.
    }

    private void updateSubmissionStatus()
    {
        final String            lPlatformStatusCode = CommonUtility.nullCheck(mSubmissionObject.getSubOriginalStatusCode(), true);

        final PlatformErrorInfo lPlatformErrorInfo  = (PlatformErrorInfo) ErrorCodeUtil.getPlatformErrorCode(ErrorCategory.PLATFORM, lPlatformStatusCode);

        final IErrorInfo        lClientErrorInfo    = ErrorCodeUtil.getClientErrorCode(mSubmissionObject.getClientId(), lPlatformStatusCode);

        if (log.isDebugEnabled())
            log.debug("Client Error Info : " + lClientErrorInfo);

        if (lPlatformStatusCode.isBlank())
            mSubmissionObject.setSubOriginalStatusCode(lPlatformErrorInfo.getErrorCode());

        mSubmissionObject.setSubOriStatusDesc(lPlatformErrorInfo.getDisplayError());
        mSubmissionObject.setSubStatus(lPlatformErrorInfo.getStatusFlag().getKey());
        mSubmissionObject.setSubClientStatusCode(lClientErrorInfo.getErrorCode());
        mSubmissionObject.setSubClientStatusDesc(lClientErrorInfo.getErrorDesc());
        // mMessageRequest.putValue(MiddlewareConstant.MW_SUB_CLI_STATUS_DESC,
        // lClientErrorInfo.getDeliveryStatus());
    }

    private void updateSTSandActualSTS()
    {
        final Date stsDate = getDateFromMessage(MiddlewareConstant.MW_CARRIER_SUBMIT_TIME);
        if (stsDate != null)
            mSubmissionObject.putValue(MiddlewareConstant.MW_CARRIER_SUBMIT_TIME, DateTimeUtility.getFormattedDateTime(stsDate, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));

        final Date actualStsDate = getDateFromMessage(MiddlewareConstant.MW_ACTUAL_CARRIER_SUBMIT_TIME);
        if (actualStsDate != null)
            mSubmissionObject.putValue(MiddlewareConstant.MW_ACTUAL_CARRIER_SUBMIT_TIME, DateTimeUtility.getFormattedDateTime(actualStsDate, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
    }

    private void updateFilename()
    {
        final String fileName = CommonUtility.nullCheck(mBaseMessage.getValue(MiddlewareConstant.MW_FILE_NAME)); // TODO it is not a filename.

        if (fileName.length() > FILE_NAME_MAX_LIMIT)
            mSubmissionObject.putValue(MiddlewareConstant.MW_FILE_NAME, fileName.substring(0, FILE_NAME_MAX_LIMIT));
    }

    private void updateCountry()
    {
        // This is not required as we might have set it in the interface itself.
    }

    private void updateSubmissionLatencies()
    {
        final long[] calculateSubmissionLatencies = TimeAdjustmentUtility.calculateSubmissionLatencies(mSubmissionObject);
        mSubmissionObject.putValue(MiddlewareConstant.MW_SUBMISSION_LATENCY_ORG_IN_MILLIS, Long.toString(calculateSubmissionLatencies[0]));
        mSubmissionObject.putValue(MiddlewareConstant.MW_SUBMISSION_LATENCY_SLA_IN_MILLIS, Long.toString(calculateSubmissionLatencies[1]));
    }

    private void processHexMessage()
    {

        if (isInterfaceReject || isPlatformReject)
        {
            if (log.isDebugEnabled())
                log.debug("For any rejection should not extract the UDH from Mssage...");
            return;
        }

        final int     udhi    = mSubmissionObject.getUdhi();
        final boolean isHexMs = mSubmissionObject.isHexMessage();

        if (isHexMs && (udhi == 1))
        {
            // In this case definitely the message should be in Hexvalue.
            final String fullHexString = mSubmissionObject.getMessage();

            try
            {
                final String udhValueStr  = fullHexString.substring(0, 2);
                final int    udhValue     = CommonUtility.getIntegerFromHexString(udhValueStr);
                final int    udhLen       = (udhValue + 1) * 2;
                final String derivedUdh   = fullHexString.substring(0, udhLen + 1);
                final String finalMessage = fullHexString.substring(udhLen);

                if ("".equals(CommonUtility.nullCheck(mSubmissionObject.getUdh(), true)))
                    mSubmissionObject.setUdh(derivedUdh);

                mSubmissionObject.setMessage(finalMessage);
            }
            catch (final Exception e)
            {
                log.error("Exception while setting the message. Message " + mSubmissionObject, e);
            }
        }
    }

    private void sendToDlrQueryTopic(SMSLog sb)
    {
        final ClientDlrConfig lClientDlrConfig = ClientDlrConfigUtil.getDlrHandoverConfig(mSubmissionObject.getValue(MiddlewareConstant.MW_CLIENT_ID), "sms", mSubmissionObject.getInterfaceType(),
                mSubmissionObject.isDlrRequestFromClient());

        if ((lClientDlrConfig != null) && lClientDlrConfig.isDlrQueryEnabled())
            sendToOtherTopic(NextTopic.DLR_QUERY,sb);
    }

}