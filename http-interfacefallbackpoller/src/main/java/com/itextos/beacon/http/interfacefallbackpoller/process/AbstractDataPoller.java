package com.itextos.beacon.http.interfacefallbackpoller.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.http.interfacefallback.dao.FallBackDao;
import com.itextos.beacon.smslog.FallbackPollerLog;

public abstract class AbstractDataPoller
        implements
        ITimedProcess
{

    private static final Log     log         = LogFactory.getLog(AbstractDataPoller.class);

    private final TimedProcessor mTimedProcessor;
    private boolean              canContinue = true;

    protected AbstractDataPoller()
    {
        super();

        
        mTimedProcessor = new TimedProcessor("FallbackTableReader", this, TimerIntervalConstant.INTERFACE_FALLBACK_TABLE_READER);
        
		
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "FallbackTableReader");
        
  
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        final boolean isKafkaAvailable = CommonUtility.isEnabled(IFBUtil.getConfigParamsValueAsString(ConfigParamConstants.IS_KAFKA_AVAILABLE));

        if (isKafkaAvailable)
            doProcess();
        else
            log.fatal("Kafka Service is disabled in app_config_values table..");

        return false;
    }

    private static boolean doProcess()
    {

        try
        {
            final Map<String, MessageRequest> lRecords       = FallBackDao.getFallbackData();
            final List<String>                toDelete       = new ArrayList<>(lRecords.keySet());
            final List<MessageRequest>        toProcess      = new ArrayList<>(lRecords.values());

            FallbackPollerLog.log("do Process() lRecords size ;  "+lRecords.size());
            
            if(!toProcess.isEmpty()) {
            	
            final List<String>                failedMessages = sendToNextQueue(toProcess);

            if (!failedMessages.isEmpty())
                toDelete.removeAll(failedMessages);

            FallBackDao.deleteRecords(toDelete);
            
            return true;
            }
            
        }
        catch (final Exception e)
        {
            log.error("Exception while sending the message to " + Component.IC, e);
        }
        return false;

    }

    private static List<String> sendToNextQueue(
            List<MessageRequest> aToProcess)
    {
        final List<String> errorInWritingToKafka = new ArrayList<>();
        for (final MessageRequest lMessageRequest : aToProcess)
            try
            {
                MessageProcessor.writeMessage(Component.FBP, Component.IC, lMessageRequest);
            }
            catch (final Exception e)
            {
                errorInWritingToKafka.add(lMessageRequest.getBaseMessageId());
                log.error("Exception while writing to the kafka. Loosing the message.", e);
            }
        return errorInWritingToKafka;
    }

    public void run() {
    	
    	long startTime=System.currentTimeMillis();
    	int loopcount=0;
    	while(true) {
    		loopcount++;
    
    		boolean status=processNow();
    		
    		if(status) {
    			
    			if((System.currentTimeMillis()-startTime)>500||loopcount>10) {
    				
    				break;
    			}
    			
    		}else {
    			
    			break;
    			
    		}
    	}
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}