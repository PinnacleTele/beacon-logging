package com.itextos.beacon.platform.sbpcore.process;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.itextos.beacon.commonlib.constants.AccountStatus;
import com.itextos.beacon.commonlib.constants.BillType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.errorlog.SMSLog;
import com.itextos.beacon.inmemdata.account.ClientAccountDetails;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.itextos.beacon.platform.sbpcore.dao.DBPoller;
import com.itextos.beacon.platform.sbpcore.util.SBPProducer;

public abstract class AbstractDataPoller
        implements
        ITimedProcess
{

    private static final Log     log         = LogFactory.getLog(AbstractDataPoller.class);
    private final int            mAppInstanceId;
    private final String         mTableName;
    private final TimedProcessor mTimedProcessor;
    private boolean              canContinue = true;

    private static String        SBC_FROM_RC = "RC_SCHDBLOCK";

    protected AbstractDataPoller(
            int aAppInstanceId,
            String aTableName)
    {
        super();
        mAppInstanceId  = aAppInstanceId;
        mTableName      = aTableName;
  
        mTimedProcessor = new TimedProcessor("TimerThread-ScheduleBlockoutPoller-" + aTableName, this, TimerIntervalConstant.SCHEDULE_MESSAGE_TABLE_READER);
   
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "TimerThread-ScheduleBlockoutPoller-" + aTableName);
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        doProcess();
        return false;
    }

    private void doProcess()
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Fetch records query from Table : '" + mTableName + "', InstanceId : '" + mAppInstanceId + "'");

            final Map<Long, MessageRequest> lRecords = DBPoller.getRecordsFromTable(mAppInstanceId, mTableName);

            if (log.isDebugEnabled())
                log.debug("Records list : " + lRecords.size());

       
            sendToNextQueue(lRecords,mTableName);

        }
        catch (final Exception e)
        {
            log.error("Exception while sending the message to " + Component.VC, e);
        }
    }

    private static void sendToNextQueue(
    		Map<Long, MessageRequest> aToProcess, String mTableName)
    {

    	Iterator itr=aToProcess.keySet().iterator();
    	
    	while(itr.hasNext()) {
    		
    		Long seqNo=(Long)itr.next();
    		
    		final MessageRequest lMessageRequest =aToProcess.get(seqNo);
    		
    		lMessageRequest.getLogBufferValue(MiddlewareConstant.MW_LOG_BUFFER).append("\n").append(" LOG START");
        	
        	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append("##########"+lMessageRequest.getBaseMessageId()+"######################");
          
        	long starttime=System.currentTimeMillis();
           
 

            final String lScheduleBlockOutFrom = CommonUtility.nullCheck(lMessageRequest.getFromScheduleBlockout(), true);

            if (log.isDebugEnabled())
                log.debug("Schedule / Blockout Message Received from : " + lScheduleBlockOutFrom);
            
            try {

            if (!verifyAccountStatus(lMessageRequest))
            {
                SBPProducer.sendToPlatformRejection(lMessageRequest);
                continue;
            }

            final boolean isCreditCheckEnabled = CommonUtility.isEnabled(lMessageRequest.getValue(MiddlewareConstant.MW_CREDIT_CHECK));

            if (log.isDebugEnabled())
                log.debug("Credit Check Enabled : " + isCreditCheckEnabled);

            if (SBC_FROM_RC.equals(lScheduleBlockOutFrom))
            {
                if (log.isDebugEnabled())
                    log.debug("Message sending to CH ..");

                final int lRetryAttempt = lMessageRequest.getRetryAttempt();

                if ((lRetryAttempt != 0) && (lMessageRequest.getMessageType() == MessageType.PROMOTIONAL))
                {
                    lMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.PROMO_TEMPORARY_FAILED.getStatusCode());
                    lMessageRequest.setRetryMessageRejected(true);

                    SBPProducer.sendToPlatformRejection(lMessageRequest);
                }
                else
                    if ((lMessageRequest.getBillType() == CommonUtility.getInteger(BillType.POST_PAID.getKey())) && !isCreditCheckEnabled)
                    {
                        updatePriceDetails(lMessageRequest);

                        if (log.isDebugEnabled())
                            log.debug("Message sending to CH component...");

                        SBPProducer.sendToRouterComponent(lMessageRequest);
                    }
                    else
                        SBPProducer.sendToBlockoutWCComponent(lMessageRequest);
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Message sending to VC component...");

                SBPProducer.sendToVerifyConsumer(lMessageRequest);
            }
            
            DBPoller.deleteRecordsFromTable(mTableName, seqNo);
            
     	   lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" Message Deleted From "+mTableName); 

    	   lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" Time Taken For process :"+(System.currentTimeMillis()-starttime)+" Milli Second"); 
      	
     	
      	   
      	   lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append("##########"+lMessageRequest.getBaseMessageId()+"######################");

           SMSLog.log(lMessageRequest.getLogBuffer().toString());
  

            } catch(Exception e) {
            	
            }
        
    	}
      
    }

    private static void updatePriceDetails(
            MessageRequest aMessageRequest)
    {
        final JSONObject lUserDetails = getAccountInfo(aMessageRequest);

        if (lUserDetails != null)
        {
            aMessageRequest.setSmsRate(CommonUtility.getDouble(CommonUtility.nullCheck(lUserDetails.get(MiddlewareConstant.MW_SMS_RATE.getName()))));
            aMessageRequest.setDltRate(CommonUtility.getDouble(CommonUtility.nullCheck(lUserDetails.get(MiddlewareConstant.MW_DLT_RATE.getName()))));
        }
    }

    private static boolean verifyAccountStatus(
            MessageRequest aMessageRequest)
    {
        final String   lClientId = aMessageRequest.getClientId();
        final UserInfo lUserInfo = ClientAccountDetails.getUserDetailsByClientId(lClientId);
        boolean        isActive  = false;

        if (lUserInfo != null)
        {
            isActive = (lUserInfo.getAccountStatus() == AccountStatus.ACTIVE);

            if (!isActive)
            {
                final PlatformStatusCode lPlatformStatusCode = getAccountStatus(lUserInfo.getAccountStatus());
                if (lPlatformStatusCode != null)
                    aMessageRequest.setSubOriginalStatusCode(lPlatformStatusCode.getStatusCode());
            }
        }
        else
            aMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.ACCOUNT_SUSPENDED.getStatusCode());

        return isActive;
    }

    private static PlatformStatusCode getAccountStatus(
            AccountStatus aAccountStatus)
    {
        PlatformStatusCode lPlatformStatusCode = null;

        switch (aAccountStatus)
        {
            case DEACTIVATED:
                lPlatformStatusCode = PlatformStatusCode.ACCOUNT_DEACTIVATED;
                break;

            case SUSPENDED:
            case INACTIVE:
            case INVALID:
            case EXPIRY:
                lPlatformStatusCode = PlatformStatusCode.ACCOUNT_SUSPENDED;
                break;

            default:
                break;
        }

        return lPlatformStatusCode;
    }

    private static JSONObject getAccountInfo(
            MessageRequest aMessageRequest)
    {
        final String   lClientId = aMessageRequest.getClientId();
        final UserInfo lUserInfo = ClientAccountDetails.getUserDetailsByClientId(lClientId);
        if (lUserInfo.getAccountDetails() != null)
            try
            {
                return parseJSON(lUserInfo.getAccountDetails());
            }
            catch (final ParseException e)
            {
                // ignore
            }

        return null;
    }

    private static JSONObject parseJSON(
            String aJsonString)
            throws ParseException
    {
        return (JSONObject) new JSONParser().parse(aJsonString);
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}