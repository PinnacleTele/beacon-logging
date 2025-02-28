package com.itextos.beacon.platform.dnpcore.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.errorinfo.data.FailureType;
import com.itextos.beacon.platform.dnpcore.log.DNPLog;
import com.itextos.beacon.platform.dnpcore.util.DNPUtil;

public class DlrRetryProcess
{

    private static final Log log = LogFactory.getLog(DlrRetryProcess.class);

    private DlrRetryProcess()
    {}

    public static List<Component> processRetry(
            DeliveryObject aDeliveryObject)
            throws Exception
    {
        final List<Component> nextComponets = new ArrayList<>();
        // final DNFailuerType errorRD = null;

        if (aDeliveryObject.isPlatfromRejected())
        {
            if (log.isDebugEnabled())
                log.debug(" Processing middleware rejected msg MessageId:" + aDeliveryObject.getMessageId());
            DNPLog.getInstance(aDeliveryObject.getClientId()).log(aDeliveryObject.getClientId(),aDeliveryObject.getFileId()+ " : "+ aDeliveryObject.getMessageId() + " : Processing middleware rejected msg MessageId:");

            nextComponets.add(Component.T2DB_DELIVERIES);
            return nextComponets;
        }

        final String  lClientId = aDeliveryObject.getClientId();

        final boolean isFastDn  = CommonUtility.isEnabled(DlrProcessUtil.getCutomFeatureValue(lClientId, CustomFeatures.IS_FASTDN_ENABLE));
        final boolean isAging   = CommonUtility.isEnabled(DlrProcessUtil.getCutomFeatureValue(lClientId, CustomFeatures.IS_AGING_ENABLE));

        // voice dn handling coming from dnreceiver

        if (aDeliveryObject.isVoiceDlr())
        {
            if (log.isDebugEnabled())
                log.debug(" Processing requestFromVoice begin mid:" + aDeliveryObject.getMessageId());

            // Final dn indicator will come from queuetomongo - no further retry configured
            // in custom_dlr_pending_aging table
            if (aDeliveryObject.getIndicateDnFinal() == 1)
            {
                nextComponets.add(Component.ADNP);
                return nextComponets;
            }

            if (DlrProcessUtil.getAppConfigValueAsString(ConfigParamConstants.CARRIER_SUCCESS_STATUS_ID).equals(aDeliveryObject.getDnOrigianlstatusCode()))
                nextComponets.add(Component.ADNP);
            else
            {
                nextComponets.add(Component.UADN);
                if (isFastDn)
                    nextComponets.add(Component.ADNP);
            }
            if (log.isDebugEnabled())
                log.debug(" Processing requestFromVoice end mid:" + aDeliveryObject.getMessageId() + " nextQueueLs:" + nextComponets);
            return nextComponets;
        }

        // This parameter will be used to bypass the logic which already executed
        // earlier - like dnretry/scalert/queuetomongo...etc rejections will come here
        // with this flag DN_FROM_INTERNAL
        // expected values for this parameter dn_from_internal
        // "dnretry" - if come from dnretry component rejections
        // "dn_generated_from_payload" - if come from payloaddngenerator
        // "aging-dngen" - dnretry/ageing dn generator if fastdn and ageing enabled
        // "voice-poller" - dnretry/voice poller in case of invalied configurations
        // "queue2mongo" - no retry and ageing enabled
        final String lDlrFromInternal = aDeliveryObject.getDlrFromInternal();
        if (log.isDebugEnabled())
            log.debug(" Processing MW_DLR_FROM_INTERNAL:" + lDlrFromInternal + " mid:" + aDeliveryObject.getMessageId());

        if ((lDlrFromInternal == null) || "dn_generated_from_payload".equals(lDlrFromInternal))
        {
            final String lRouteId    = aDeliveryObject.getRouteId();
            final String lErrorCode  = aDeliveryObject.getCarrierStatusCode();
            final String lStatusFlag = aDeliveryObject.getCarrierStatusDesc();

            DNPUtil.setPlatformErrorCodeBasedOnCarrierErrorCode(aDeliveryObject);

            /*
             * final DLRErrorCode errCode = DNPUtil.getNunErrorCode(lRouteId, lErrorCode,
             * lStatusFlag);
             * if (log.isDebugEnabled())
             * log.debug(" Error Code " + errCode);
             * PlatformStatusCode lNunErrorCode;
             * if (errCode != null)
             * {
             * lNunErrorCode = PlatformStatusCode.getStatusDesc(errCode.getNunError());
             * errorRD = errCode.getRDErrorCode();
             * }
             * else
             * lNunErrorCode = PlatformStatusCode.UNKNOWN_ERROR_CODE;
             * aNunMessage.putValue(MiddlewareConstant.MW_DN_ORI_STATUS_CODE,
             * lNunErrorCode.getStatusCode());
             * aNunMessage.putValue(MiddlewareConstant.MW_DN_FAILURE_TYPE, (errorRD != null
             * ? CommonUtility.nullCheck(errorRD.getKey()) : "2"));
             */

            getFinalMessage(aDeliveryObject);
        }

        // if (aNunMessage.getValue(MiddlewareConstant.MW_BYPASS_DNRETRY_CHK) != null)
        if (aDeliveryObject.getDlrFromInternal() != null)
        {
            if (isFastDn || isAging)
                nextComponets.add(Component.ADNP);
            else
                nextComponets.add(Component.T2DB_DELIVERIES);
            
            log.debug("processRetry : nextComponets :  "+nextComponets);
            
            return nextComponets;
        }

        final int smsRetry = aDeliveryObject.getSmsRetryEnabled();

        if ("DELIVRD".equals(aDeliveryObject.getCarrierStatusDesc()) || (smsRetry == 0))
        {
            if (isFastDn || isAging)
                nextComponets.add(Component.ADNP);
            else
                nextComponets.add(Component.T2DB_DELIVERIES);
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("Retry check execute ...");

            // Although it is temporary failure and multi-part case should not got for
            // dlr-retry.
            if (FailureType.TEMPORARY.getKey().equals(aDeliveryObject.getDnFilureType()) && (aDeliveryObject.getMessageTotalParts() <= 1))
            {
                boolean dnRetry = true;

                if (isAging)
                {
                    nextComponets.add(Component.UADN);
                    dnRetry = false;
                }

                if (isFastDn)
                {
                    nextComponets.add(Component.ADNP);
                    if (!isAging)
                        nextComponets.add(Component.DLRR);
                    dnRetry = false;
                }
                if (dnRetry)
                    nextComponets.add(Component.DLRR);
            }
            else
            {
                boolean handOverClient = true;

                // aging enabled and paramanant failure - handover to UPDATE_AGING_MONGO_QUEUE
                // to process by Aging dn processor
                if (isAging)
                {
                    nextComponets.add(Component.UADN);
                    handOverClient = false;
                }

                if (isFastDn)
                {
                    nextComponets.add(Component.ADNP);
                    handOverClient = false;
                }
                if (handOverClient)
                    nextComponets.add(Component.T2DB_DELIVERIES);
            }
        }
        return nextComponets;
    }

    public static void getFinalMessage(
            DeliveryObject aDeliveryObject)
    {
        final Date lDTime = aDeliveryObject.getDeliveryTime();

        if (lDTime == null)
            aDeliveryObject.setDeliveryTime(new Date());
    }

}
