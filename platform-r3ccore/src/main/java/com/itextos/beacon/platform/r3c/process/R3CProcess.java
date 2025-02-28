package com.itextos.beacon.platform.r3c.process;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.MessagePart;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.platform.r3c.util.R3CProducer;
import com.itextos.beacon.platform.r3c.util.R3CUtil;
import com.itextos.beacon.platform.r3c.util.SmartlinkIdReplacer;

public class R3CProcess
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(R3CProcess.class);

    public R3CProcess(
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
            throws Exception
    {
        final MessageRequest lMessageRequest = (MessageRequest) aBaseMessage;

        if (log.isDebugEnabled())
            log.debug("doProcess() R3C Received Object .. " + lMessageRequest);

     
        R3CProcess.forR3C(lMessageRequest);
    }

    public static void forR3C(MessageRequest lMessageRequest) throws ItextosRuntimeException {
    	
    	   if (R3CUtil.isDuplicate(lMessageRequest))
           {
               lMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.DUPLICATE_CHECK_FAILED.getStatusCode());
               R3CProducer.sendToPrc(lMessageRequest);
               return;
           }

           lMessageRequest.setUrlShortned(false);

           urlShortnerCheckProcess(lMessageRequest);

           smartLinkCheckProcess(lMessageRequest);
    }
    private static void urlShortnerCheckProcess(
            MessageRequest aMessageRequest)
    {
        if (log.isDebugEnabled())
            log.debug("VL Shortner Enabled ? " + aMessageRequest.getVlShortner());

        boolean isVLShortnerEnable = aMessageRequest.getVlShortner() > 0;

        if (aMessageRequest.getInterfaceType() == InterfaceType.GUI)
            isVLShortnerEnable = aMessageRequest.getVlShortnerFromUI() > 0;

        if (isVLShortnerEnable)
        {
            final UrlShortner r3cWrapperMessage = new UrlShortner(aMessageRequest);
            r3cWrapperMessage.process();
        }
    }

    private static void smartLinkCheckProcess(
            MessageRequest aMessageRequest) throws ItextosRuntimeException
    {

        if (aMessageRequest.getUrlSmartlinkEnable() != 0)
        {
            final int lTotalMessageParts = aMessageRequest.getSubmissions().size();

            if (lTotalMessageParts == 1)
            {
                if (log.isDebugEnabled())
                    log.debug("Executing Smartlink logic");

                final SmartlinkIdReplacer lSmartlinkIdReplacer = new SmartlinkIdReplacer(aMessageRequest);
                final boolean             lProcess             = lSmartlinkIdReplacer.process();

                if (!lProcess)
                {
                    if (log.isDebugEnabled())
                        log.debug("Smartlink validation failed sending to Platfrom Rejection");

                    aMessageRequest.setPlatfromRejected(true);
                    aMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.URL_SHORTNER_PROCESS_FAILED.getStatusCode());
                    R3CProducer.sendToPrc(aMessageRequest);

                    return;
                }
            }
            else
                if (log.isDebugEnabled())
                    log.debug("Smartlink process will not support for Interface Splited Multipart messages");
        }
        sendToNextProducer(aMessageRequest);
    }

    public static void sendToNextProducer(
            MessageRequest aMessageRequest)
    {
        if (log.isDebugEnabled())
            log.debug("Sending request to SBVC : " + aMessageRequest);

        R3CProducer.sendToSBVCComponent(aMessageRequest);
    }

    @Override
    public void doCleanup()
    {}

    @Override
    protected void updateBeforeSendBack(
            IMessage aArg0)
    {}

    public static void main(
            String[] args) throws ItextosRuntimeException
    {
        final MessageRequest lMessageRequest = new MessageRequest(ClusterType.BULK, InterfaceType.HTTP_JAPI, InterfaceGroup.API, MessageType.TRANSACTIONAL, MessagePriority.PRIORITY_2,
                RouteType.DOMESTIC);

        lMessageRequest.setUrlSmartLink(1);
        lMessageRequest.setLongMessage("Test Message www.google.com/test and [~VL:1234566~]");
        lMessageRequest.setIsHexMessage(false);
        lMessageRequest.setMobileNumber("919500045053");
        lMessageRequest.setVlShortner(1);
        lMessageRequest.setClientId("6000000200000000");

        final MessagePart lbj = new MessagePart("200122334343534322");
        lbj.setMessageReceivedTime(new Date(System.currentTimeMillis()));
        lMessageRequest.addMessagePart(lbj);

        smartLinkCheckProcess(lMessageRequest);
    }

}
