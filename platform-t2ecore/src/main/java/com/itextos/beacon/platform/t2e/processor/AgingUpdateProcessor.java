package com.itextos.beacon.platform.t2e.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.dnpayloadutil.common.TimeAdjustmentUtility;
import com.itextos.beacon.platform.elasticsearchutil.EsProcess;
import com.itextos.beacon.platform.t2e.util.T2EProducer;
import com.itextos.beacon.platform.t2e.util.T2EUtil;

public class AgingUpdateProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log     log           = LogFactory.getLog(AgingUpdateProcessor.class);
    private static final boolean isDropMessage = CommonUtility.isTrue(System.getProperty("dropmessage"));

    public AgingUpdateProcessor(
            String aThreadName,
            Component aComponent,
            ClusterType aPlatformCluster,
            String aTopicName,
            ConsumerInMemCollection aConsumerInMemCollection,
            int aSleepInMillis)
    {
        super(aThreadName, aComponent, aPlatformCluster, aTopicName, aConsumerInMemCollection, aSleepInMillis);
    }

    @Override
    public void doProcess(
            BaseMessage aBaseMessage)
    {
        if (isDropMessage)
            return;

        final DeliveryObject deliveryObject = (DeliveryObject) aBaseMessage;
        if (log.isDebugEnabled())
            log.debug("doProcess() Aging Update Received Object .. " + aBaseMessage);

        try
        {
            final String  lClientId                = deliveryObject.getClientId();
            final boolean isFurtherRetryConfigured = false;// AgingLookupCache.getInstance().isAgeingRequired(lClientId,
                                                           // aNunMessage.getValue(MiddlewareConstant.MW_RETRY_ATTEMPT));
            if (log.isDebugEnabled())
                log.debug("doProcess() -  isFurtherRetryConfigured - " + isFurtherRetryConfigured);

            if (isFurtherRetryConfigured && !T2EUtil.isFastDnEnabled(lClientId))
            {
                if (log.isDebugEnabled())
                    log.debug("process() -  Handover to INTERIM_DELIVERIES_QUEUE..." + deliveryObject);
                // TODO : Yet to implement
                /*
                 * final ErrorCode errCode =
                 * ErrorCodeHandler.getInstance().getErrorCode(mapMsg.getStringProperty(MapKey.
                 * STATUS_ID));
                 * mapMsg.setStringProperty(MapKey.STATUS_FLAG, ((errCode == null) ||
                 * (errCode.getStatusFlag() == null)) ? "FAILED" : errCode.getStatusFlag());
                 */
                TimeAdjustmentUtility.setCarrierTime(deliveryObject);
                T2EProducer.sendIntrimDeliveriesTopic(deliveryObject, mComponent);
            }
            else
                if (!T2EUtil.isFastDnEnabled(lClientId))
                {
                    if (log.isDebugEnabled())
                        log.debug("process() -  Handover to AGING_DN_PROCESS_QUEUE..." + deliveryObject);
                    T2EProducer.sendAgingDNProcessorTopic(deliveryObject, mComponent);
                }

            if (isFurtherRetryConfigured)
            {
                if (log.isDebugEnabled())
                    log.debug("doProcess() -  updating the Aging record ..");

                EsProcess.updateAgingDn(deliveryObject);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception occer while Update Aging record into Elastic Search.", e);
            T2EProducer.sendToErrorLog(deliveryObject, e, mComponent);
        }
    }

    @Override
    public void doCleanup()
    {
        // TODO Auto-generated method stub
    }

    @Override
    protected void updateBeforeSendBack(
            IMessage aMessage)
    {
        // TODO Auto-generated method stub
    }

}
