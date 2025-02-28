package com.itextos.beacon.platform.dnpcore.util;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.msgutil.cache.CarrierCircle;
import com.itextos.beacon.inmemory.msgutil.util.IndiaNPFinder;
import com.itextos.beacon.platform.dnpayloadutil.common.TimeAdjustmentUtility;
import com.itextos.beacon.platform.dnpcore.redis.RedisProcess;

public class AgingDlrProcessUtil
{

    private static final Log       log      = LogFactory.getLog(AgingDlrProcessUtil.class);

    private final DeliveryObject   mDeliveryObject;
    private static final String    FAST_DLR = "fast_dn";

    Map<Component, DeliveryObject> mNnextComponentMap;

    public AgingDlrProcessUtil(
            DeliveryObject aDeliveryObject,
            Map<Component, DeliveryObject> aNextComponentMap)
    {
        this.mDeliveryObject    = aDeliveryObject;
        this.mNnextComponentMap = aNextComponentMap;
    }

    // fastdn generated,success dn,final dn failure (for fastdn or ageing dn or both
    // enabled)
    // it has to mark success, hand over to client,hand over to
    // sms_interim_deliveries

    public void processAgeingDn()
            throws Exception
    {
        // generated fast dn
        boolean      handoverSuccess = false;
        boolean      interimMarked   = false;

        final String lMessageId      = mDeliveryObject.getMessageId();

        if (isFastDN(mDeliveryObject) && !isHandoverSuccess(mDeliveryObject))
        {
            // if generated fastdn already client handover not happen handover to client

            if (log.isDebugEnabled())
                log.debug("fast dn sending to deliver sm queue ->DN_QUEUE");

            sendToDeliverSmQueue(mDeliveryObject);

            handoverSuccess = true;

            if (log.isDebugEnabled())
                log.debug("fast dn sent to deliver sm queue ->DN_QUEUE mid:" + lMessageId);
        }
        else
            if (isSuccessDN(mDeliveryObject) && !isHandoverSuccess(mDeliveryObject))
            {
                // if successdn not handover--->handover client

                if (log.isDebugEnabled())
                    log.debug("success dn marking and sending to deliver sm queue ->DN_QUEUE mid:" + lMessageId);

                updateDeliverySuccessfull(mDeliveryObject);
                sendToDeliverSmQueue(mDeliveryObject);
                handoverSuccess = true;

                if (log.isDebugEnabled())
                    log.debug("success dn marked and sent to deliver sm queue done->DN_QUEUE mid:" + lMessageId);
            }
            else
                if ((isFinalDN(mDeliveryObject) || isUpdateElasticFinalDN(mDeliveryObject)) && !isHandoverSuccess(mDeliveryObject))
                {
                    // if finaldn not handover--->handover client

                    if (log.isDebugEnabled()) //// final dn will be always failure..
                        log.debug("final dn sending to deliver sm queue ->DN_QUEUE mid:" + lMessageId);

                    sendToDeliverSmQueue(mDeliveryObject);

                    handoverSuccess = true;
                    if (log.isDebugEnabled())
                        log.debug("final dn send to deliver sm queue ->DN_QUEUE" + "\n" + mDeliveryObject);
                }
                else
                    if (isFastDnEnabled(mDeliveryObject.getClientId()) && !isHandoverSuccess(mDeliveryObject))
                    {
                        // not handovered already just handover

                        if (log.isDebugEnabled()) //// final dn will be always failure..
                            log.debug("failure dn sending to deliver sm queue ->DN_QUEUE mid:" + lMessageId);

                        sendToDeliverSmQueue(mDeliveryObject);
                        handoverSuccess = true;

                        if (log.isDebugEnabled())
                            log.debug("failure dn send to deliver sm queue ->DN_QUEUE mid:" + lMessageId);
                    }

        // only success dn and fastdn only enabled account not hand over to client need
        // to be marked in interim others will be ignored

        if (isCarrierDN(mDeliveryObject) && !handoverSuccess)
        {
            if (log.isDebugEnabled())
                log.debug("no matches sending to queue ->INTERIM_DELIVERIES_QUEUE mid:" + lMessageId);

            if (isSuccessDN(mDeliveryObject))
                updateDeliverySuccessfull(mDeliveryObject);

            // TODO : Yet to implement
            // final ErrorCodeInfo lErrorCodeInfo =
            // DlrErrorUtil.getErrorCodeInfo(mMessageRequest.getValue(MiddlewareConstant.MW_DN_ORI_STATUS_CODE));

            // mMessageRequest.putValue(MiddlewareConstant.MW_DN_ORI_STATUS_DESC,
            // ((lErrorCodeInfo == null) || (lErrorCodeInfo.getStatusFlag() == null)) ?
            // "FAILED" : lErrorCodeInfo.getStatusFlag());

            TimeAdjustmentUtility.setCarrierTime(mDeliveryObject);

            final String[] lCarrierCircleInfo = getDeliveriesCarrierAndCircle(mDeliveryObject);

            mDeliveryObject.setCarrier(lCarrierCircleInfo[0]);
            mDeliveryObject.setCircle(lCarrierCircleInfo[1]);

            if (lCarrierCircleInfo[2] != null)
                mDeliveryObject.setTerminatedCarrier(lCarrierCircleInfo[2]);
            if (lCarrierCircleInfo[3] != null)
                mDeliveryObject.setTerminatedCircle(lCarrierCircleInfo[3]);

            sendToSMSInterimDeliveries(mDeliveryObject);

            interimMarked = true;
        }

        if (log.isInfoEnabled())
        {
            log.info("handoverSuccess=" + handoverSuccess);
            log.info("interimMarked=" + interimMarked);

            if (!interimMarked && !handoverSuccess)
                log.info("ignoring unexpected dn--->" + mDeliveryObject);
        }
    }

    private void sendToSMSInterimDeliveries(
            DeliveryObject aDeliveryObject)
    {
        // generated payload dn's will not be sent to sms_interim_deliveries table -
        // DN_FROM_INTERNAL value "dn_generated_from_payload" sends by
        // payloaddngenerator component

        if (!"dn_generated_from_payload".equals(aDeliveryObject.getDlrFromInternal()) && !isFinalDN(aDeliveryObject))
            mNnextComponentMap.put(Component.T2DB_INTERIM_DELIVERIES, aDeliveryObject);
        else
            if (log.isDebugEnabled())
                log.debug("Not sending to " + Component.T2DB_INTERIM_DELIVERIES + " since DN generated by payloadgenerator");
    }

    private void sendToDeliverSmQueue(
            DeliveryObject aDeliveryObject)
    {
        mNnextComponentMap.put(Component.T2DB_DELIVERIES, aDeliveryObject);
    }

    private static boolean isFastDN(
            DeliveryObject aDeliveryObject)
    {

        try
        {
            return FAST_DLR.equals(aDeliveryObject.getAgeingType());
        }
        catch (final Exception e)
        {}
        return false;
    }

    private static boolean isSuccessDN(
            DeliveryObject aDeliveryObject)
    {

        try
        {
            return DNPUtil.getAppConfigValueAsString(ConfigParamConstants.CARRIER_SUCCESS_STATUS_ID).equals(aDeliveryObject.getDnOrigianlstatusCode());
        }
        catch (final Exception e)
        {}
        return false;
    }

    private static boolean isFinalDN(
            DeliveryObject aDeliveryObject)
    {
        final int lFinalDnIndicator = aDeliveryObject.getIndicateDnFinal();

        try
        {
            return (lFinalDnIndicator == 1);
        }
        catch (final Exception e)
        {}
        return false;
    }

    private static boolean isUpdateElasticFinalDN(
            DeliveryObject aDeliveryObject)
    {
        final int lFinalDnIndicator = aDeliveryObject.getIndicateDnFinal();

        try
        {
            return (lFinalDnIndicator == 3);
        }
        catch (final Exception e)
        {}
        return false;
    }

    private static boolean isFastDnEnabled(
            String aClientId)
    {
        return CommonUtility.isEnabled(DNPUtil.getCutomFeatureValue(aClientId, CustomFeatures.IS_FASTDN_ENABLE));
    }

    private static boolean isCarrierDN(
            DeliveryObject aDeliveryObject)
    {
        return CommonUtility.nullCheck(aDeliveryObject.getAgeingType(), true).isEmpty();
    }

    private static boolean isHandoverSuccess(
            DeliveryObject aDeliveryObject)
    {
        final boolean done = false;

        while (!done)
        {
            if (log.isDebugEnabled())
                log.debug("is DN Handover status in redis :" + aDeliveryObject.getMessageId());

            try
            {
                return RedisProcess.checkDNHandoverStatus(aDeliveryObject.getMobileNumber(), aDeliveryObject.getMessageReceivedDate(), aDeliveryObject.getClientId(), aDeliveryObject.getMessageId());
            }
            catch (final Exception e)
            {
                log.error("problem checking isHandoverSuccess status retrying in 10 secs-->", e);

                try
                {
                    Thread.sleep(2000);
                }
                catch (final InterruptedException e1)
                {}
            }
        }
    }

    private static boolean updateDeliverySuccessfull(
            DeliveryObject aDeliveryObject)
    {
        final boolean done = false;
        while (!done)
            try
            {
                return RedisProcess.checkAgingDNStatus(aDeliveryObject.getMobileNumber(), aDeliveryObject.getMessageReceivedDate(), aDeliveryObject.getClientId(), aDeliveryObject.getMessageId());
            }
            catch (final Exception e)
            {
                log.error("problem checking isAgingDnSuccess status retrying in 10 secs-->", e);

                try
                {
                    Thread.sleep(2000);
                }
                catch (final InterruptedException e1)
                {}
            }
    }

    private String[] getDeliveriesCarrierAndCircle(
            DeliveryObject aBaseMessage)
    {

        try
        {
            String[] lCarrierCircelInfo =
            { CommonUtility.nullCheck(aBaseMessage.getCarrier()), CommonUtility.nullCheck(aBaseMessage.getCircle()), CommonUtility.nullCheck(aBaseMessage.getCarrier()),
                    CommonUtility.nullCheck(aBaseMessage.getCircle()) };

            try
            {
                final CarrierCircle lCarrierCircle = IndiaNPFinder.getCarrierCircle(aBaseMessage.getMobileNumber());

                if (lCarrierCircle != null)
                    lCarrierCircelInfo = null;// TODO Have to work on this.
            }
            catch (final Exception e)
            {
                log.error("Problem in getting the Carrier and Circle for the Mobile Number : '" + aBaseMessage.getMobileNumber() + "'", e);
            }

            // final String lMWRejectTermCarrierCircle =
            // mMessageRequest.getValue(MiddlewareConstant.MW_REJECT_TERM_CARRIER_CIRCLE_NULL);

            // For all middle ware rejections sms_deliveris.term_carrier and term_circle
            // should be null
            /*
             * if (lMWRejectTermCarrierCircle != null)
             * {
             * lCarrierCircelInfo[2] = null;
             * lCarrierCircelInfo[3] = null;
             * }
             */
            return lCarrierCircelInfo;
        }
        catch (final Exception e)
        {
            log.error("Problem in getting the Carrier and Circle for the BaseMessage Object '" + aBaseMessage + "'", e);
        }
        return null;
    }

}
