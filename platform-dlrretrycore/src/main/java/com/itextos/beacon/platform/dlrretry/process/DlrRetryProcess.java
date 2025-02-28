package com.itextos.beacon.platform.dlrretry.process;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.platform.dlrretry.util.DlrRetryProducer;
import com.itextos.beacon.platform.dlrretry.util.DlrRetryUtil;

public class DlrRetryProcess
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(DlrRetryProcess.class);

    public DlrRetryProcess(
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
        final DeliveryObject lDeliveryObject = (DeliveryObject) aBaseMessage;

        if (log.isDebugEnabled())
            log.debug("Message Received Dlr Retry component : " + lDeliveryObject);

        Map<Component, DeliveryObject> lNextComponentMap = new HashMap<>();

        try
        {
            lNextComponentMap = DlrRetryUtil.processDNRetry(lDeliveryObject);

            if (log.isDebugEnabled())
                log.debug(" Next Retry Queue info -" + lNextComponentMap);

            // DlrRetryProducer.sendToNextComponents(lNextComponentMap);
        }
        catch (final Exception exp)
        {
            log.error(" problem processing dn retry aborting retry sending to deliversm queue avoiding retry due to...", exp);

            try
            {
                DlrRetryProducer.sendToErrorLog(lDeliveryObject, exp);
            }
            catch (final Exception error)
            {
                log.error(" problem sending to deliver sm queue lost deliver sm " + lDeliveryObject, error);
            }
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
