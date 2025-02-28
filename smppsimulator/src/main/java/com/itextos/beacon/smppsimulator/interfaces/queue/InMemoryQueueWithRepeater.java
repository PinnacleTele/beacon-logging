package com.itextos.beacon.smppsimulator.interfaces.queue;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.utility.MessageUtil;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.smpp.account.SmppAccInfo;
import com.itextos.beacon.inmemory.smpp.account.util.SmppAccUtil;
import com.itextos.beacon.platform.smppsimulator.util.DeliverSmInfo;
import com.itextos.beacon.smppsimulator.interfaces.event.handlers.ItextosSmppSessionHandler;
import com.itextos.beacon.smppsimulator.interfaces.sessionhandlers.ItextosSessionManager;
import com.itextos.beacon.smppsimulator.interfaces.sessionhandlers.objects.DelvierySmWindowFutureHolder;
import com.itextos.beacon.smppsimulator.interfaces.workers.DNWorker;

public class InMemoryQueueWithRepeater {

    // Define an in-memory queue with a fixed capacity
    private final BlockingQueue<MessageRequest> queue = new LinkedBlockingQueue<>(100000);
    
    private static final String UNSUPPORTED_MSG_FORMAT = "UNSUPPORTED FORMAT";

    private String systemId;
    
    private final DNWorker   worker;

    
    public InMemoryQueueWithRepeater(String systemId) {
    	
    	this.systemId=systemId;
        worker   = new DNWorker(systemId);

    }

    // Method to add items to the queue
    public void addToQueue(MessageRequest lMessageRequest) {
        try {
            queue.put(lMessageRequest); // Blocks if the queue is full
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Failed to add item to the queue: " + e.getMessage());
        }
    }

    
    public int queuesize() {
    	
    	return queue.size();
    }
    // Repeater process that continuously consumes items from the queue
    public void startRepeaterProcess() {
    	
    	Thread.ofVirtual().start(() -> {
            while (true) {
                try {
                    // Poll the queue with a timeout to avoid indefinite blocking
                	MessageRequest lMessageRequest = queue.poll(2, TimeUnit.SECONDS);

                    if (lMessageRequest != null) {
                        processItem(lMessageRequest); // Process the item
                    } else {
                        System.out.println("Queue is empty, waiting for new items...");
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Repeater process interrupted: " + e.getMessage());
                    break;
                }
            }
        });
    }

    // Example item processing method
    private void processItem(MessageRequest aDeliveryObject) {
       
    	
    	DeliverSmInfo dsm=getDeliveryInfo(aDeliveryObject);
    	send(aDeliveryObject, dsm);
    }

    private static DeliverSmInfo getDeliveryInfo(
    		MessageRequest aDeliveryObject)
    {
        final DeliverSmInfo lDlvInfo = new DeliverSmInfo();

        lDlvInfo.setClientId(aDeliveryObject.getClientId());
        lDlvInfo.setSourceAddress(MessageUtil.getHeaderId(aDeliveryObject));
        lDlvInfo.setDestinationAddress(aDeliveryObject.getMobileNumber());
        lDlvInfo.setEsmClass(aDeliveryObject.getSmppEsmClass());
        lDlvInfo.setServiceType("sms");
        lDlvInfo.setDataCoding(aDeliveryObject.getDcs());
        lDlvInfo.setMsgId(aDeliveryObject.getFileId());
        lDlvInfo.setShortMessage(createSMPPShortMsg(aDeliveryObject,true));
        lDlvInfo.setReceivedTs(aDeliveryObject.getFirstReceivedTime().getTime());

      
        final long lCarrSubmitTs = System.currentTimeMillis();
        lDlvInfo.setCarrierSubmitTs(lCarrSubmitTs);


        final long lCarrReceivedTs = System.currentTimeMillis();
        lDlvInfo.setDNReceivedTs(lCarrReceivedTs);

        return lDlvInfo;
    }
    
    
    private static String createSMPPShortMsg(
    		MessageRequest aDeliveryObject,
            boolean isMsgSupported)
    {
        // "id:<mid> sub:001 dlvrd:001 submit:<smppdate> done date:<ddate> stat:<status>
        // err:<errorCode> Text:<msg>";

        final StringBuilder sb = new StringBuilder();

        try
        {
            String            lDnCustomDateFormat = DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM.getKey();

            final SmppAccInfo lSmppAccInfo        = SmppAccUtil.getSmppAccountInfo(aDeliveryObject.getClientId());

            if (lSmppAccInfo != null)
            {
                    lDnCustomDateFormat = CommonUtility.nullCheck(lSmppAccInfo.getDnDateFormat()).isEmpty() ? lDnCustomDateFormat : lSmppAccInfo.getDnDateFormat();
            }

            String id = aDeliveryObject.getFileId();

            if (id.isEmpty())
                id = aDeliveryObject.getFileId();

            Date sTime = aDeliveryObject.getFirstReceivedTime();
            sTime = sTime == null ? new Date() : sTime;

            final String lFormatedStime = DateTimeUtility.getFormattedDateTime(sTime, lDnCustomDateFormat);

            Date         dTime          = new Date();
            dTime = dTime == null ? new Date() : dTime;
            final String lFormatedDtime  = DateTimeUtility.getFormattedDateTime(dTime, lDnCustomDateFormat);

            String       lReplaceMessage = aDeliveryObject.getLongMessage();
            if ((lReplaceMessage != null) && (lReplaceMessage.length() > 0))
                lReplaceMessage = lReplaceMessage.replaceAll("%A%", "\\|");
            else
                lReplaceMessage = "";

            if (isMsgSupported)
            {
                if (lReplaceMessage.length() > 10)
                    lReplaceMessage = lReplaceMessage.substring(0, 10);
            }
            else
                lReplaceMessage = UNSUPPORTED_MSG_FORMAT;

            final String lDlrStatus = "Success";

            sb.append("id:").append(id);
            sb.append(" sub:001 dlvrd:001 submit date:").append(lFormatedStime);
            sb.append(" done date:").append(lFormatedDtime);
            sb.append(" stat:").append(lDlrStatus);
            sb.append(" err:").append("000");
           // sb.append(" Text:").append(lReplaceMessage);
            sb.append(" Text:").append(" ");
        }
        catch (final Exception e)
        {
            throw e;
        }

    

        return sb.toString();
    }

    
    private void send(MessageRequest aDeliveryObject,DeliverSmInfo deliveryInfo) {
    	


        try
        {
        
                boolean sent = false;

                while (!sent)
                {
                    sent = sendMessage(deliveryInfo);

                    if (!sent)
                    {
                      
                        Thread.sleep(1);
                    }
                }
         
           
        }
        catch (final Exception exp)
        {
           
        	addToQueue(aDeliveryObject);
        }
    
    }
    
    
    private boolean sendMessage(
            DeliverSmInfo aDeliveryInfo)
    {
        final ItextosSmppSessionHandler sessionHandler = getSessionHandler();
        boolean                         sent           = false;

        if (sessionHandler != null)
        {
            final DelvierySmWindowFutureHolder tempBean = send(aDeliveryInfo, sessionHandler);


            if (tempBean.getWindowfuture() != null)
            {
                sessionHandler.updateLastUsedTime();
                sent = true;
            }
            sessionHandler.setInUse(false);
        }
        return sent;
    }
    
    private ItextosSmppSessionHandler getSessionHandler()
    {

        try
        {
            return ItextosSessionManager.getInstance().getAvailableSession(systemId);
        }
        catch (final Exception e)
        {
          
        }
        return null;
    }
    
    private DelvierySmWindowFutureHolder send(
            DeliverSmInfo aDeliverySmInfo,
            ItextosSmppSessionHandler aSessionHandler)
    {
        aDeliverySmInfo.resetDnRts();
        aDeliverySmInfo.resetDnSts();
        final WindowFuture<Integer, PduRequest, PduResponse> windowFuture = worker.sendMessage(aSessionHandler, aDeliverySmInfo);
        return new DelvierySmWindowFutureHolder(aDeliverySmInfo, aSessionHandler, windowFuture);
    }
}
