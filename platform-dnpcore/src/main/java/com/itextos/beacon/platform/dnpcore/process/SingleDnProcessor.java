package com.itextos.beacon.platform.dnpcore.process;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.platform.dnpcore.dlrclienthandover.process.DlrClientHandover;
import com.itextos.beacon.platform.dnpcore.util.DNPProducer;
import com.itextos.beacon.platform.singledn.data.DeliveryInfo;
import com.itextos.beacon.platform.singledn.data.SingleDnInfo;
import com.itextos.beacon.platform.singledn.data.SingleDnRequest;
import com.itextos.beacon.platform.singledn.process.SingleDlrProcessor;

public class SingleDnProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(SingleDnProcessor.class);

    public SingleDnProcessor(
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
        if (log.isDebugEnabled())
            log.debug("SIngle DN Received Message : " + aBaseMessage);

        final DeliveryObject lDeliveryObject = (DeliveryObject) aBaseMessage;

        lDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER).append("\n").append(" LOG START");

        try

        {
            final Map<Component, DeliveryObject> processSingleDNQ = processSingleDNQ(lDeliveryObject);

            if (!processSingleDNQ.isEmpty())
            {
                if (log.isDebugEnabled())
                    log.debug(" process() - Sending to " + processSingleDNQ.keySet() + " json:" + lDeliveryObject.toString());
                DNPProducer.sendToNextComponents(processSingleDNQ,lDeliveryObject.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER));
            }
            if (log.isDebugEnabled())
                log.debug(" process() no data to be processed json:" + lDeliveryObject.toString());
        }
        catch (final Exception e)
        {
            log.error("Exception occer while processing the Single DN Dlr : ", e);
            DNPProducer.sendToErrorLog(lDeliveryObject, e);
        }
    }

    public static Map<Component, DeliveryObject> processSingleDNQ(
            DeliveryObject aDeliveryObject)
            throws Exception
    {
        final Map<Component, DeliveryObject> lNextComponentMap = new HashMap<>();

        if (log.isDebugEnabled())
            log.debug("Process satrt Message Id:" + aDeliveryObject.getMessageId());

        final SingleDnRequest lSinglDNReq   = getSingleDnRequest(aDeliveryObject);
        final SingleDnInfo    lSingleDnInfo = SingleDlrProcessor.processSingleDnProcessor(lSinglDNReq);

        if (log.isDebugEnabled())
            log.debug("SingleDn Info : " + lSingleDnInfo);

        if ((lSingleDnInfo != null) && (lSingleDnInfo.getDNObject() != null))
        {
            if (log.isDebugEnabled())
                log.debug("Single DN Object :" + lSingleDnInfo.getDNObject());

            final DeliveryObject lDeliveryObject = new DeliveryObject(lSingleDnInfo.getDNObject());

            generateNextQueue(lDeliveryObject, lNextComponentMap);
        }

        if (log.isDebugEnabled())
            log.debug("Singlednprocess Message Id:" + aDeliveryObject.getMessageId() + " _nextQueueMap:" + (lNextComponentMap != null ? lNextComponentMap.keySet() : lNextComponentMap));

        return lNextComponentMap;
    }

    private static void generateNextQueue(
            DeliveryObject aDeliveryObject,
            Map<Component, DeliveryObject> aNextComponentMap)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("Handover to Client :" + aDeliveryObject);

        DlrClientHandover.processClientHandover(aDeliveryObject, aNextComponentMap);
    }

    private static SingleDnRequest getSingleDnRequest(
            DeliveryObject aDeliveryObject)
    {
        final String       lClientId      = aDeliveryObject.getClientId();
        final String       lDest          = aDeliveryObject.getMobileNumber();
        final String       lBaseMsgId     = aDeliveryObject.getBaseMessageId();

        final int          lTotalPartsNos = aDeliveryObject.getMessageTotalParts();
        final int          lMsgPartNumber = aDeliveryObject.getMessagePartNumber();
        final String       lJson          = aDeliveryObject.getJsonString();

        final DeliveryInfo lDeliveryInfo  = new DeliveryInfo(lTotalPartsNos, lMsgPartNumber, lJson);

        return new SingleDnRequest(lClientId, lDest, lBaseMsgId, lDeliveryInfo);
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
