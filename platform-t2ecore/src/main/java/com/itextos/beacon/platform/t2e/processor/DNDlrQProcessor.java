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
import com.itextos.beacon.platform.elasticsearchutil.EsProcess;
import com.itextos.beacon.platform.t2e.util.T2EProducer;

public class DNDlrQProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log     log           = LogFactory.getLog(DNDlrQProcessor.class);
    private static final boolean isDropMessage = CommonUtility.isTrue(System.getProperty("dropmessage"));

    public DNDlrQProcessor(
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

        final DeliveryObject lDeliveryObject = (DeliveryObject) aBaseMessage;

        if (log.isDebugEnabled())
            log.debug("doProcess() DN DlrQ Received Object .. " + lDeliveryObject);

        try
        {
            EsProcess.insertDlrQueryDn(lDeliveryObject);
        }
        catch (final Exception e)
        {
            log.error("Exception occer while insert dlrquery Dn record into Elastic Search.", e);
            T2EProducer.sendToErrorLog(lDeliveryObject, e, mComponent);
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
