package com.itextos.beacon.platform.wc.process;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.errorlog.ErrorLog;
import com.itextos.beacon.platform.walletbase.data.WalletDeductInput;
import com.itextos.beacon.platform.walletbase.data.WalletInput;
import com.itextos.beacon.platform.walletbase.data.WalletResult;
import com.itextos.beacon.platform.walletbase.util.WalletUtil;
import com.itextos.beacon.platform.walletprocess.WalletDeductRefundProcessor;
import com.itextos.beacon.platform.wc.util.WCProducer;

public class WalletProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(WalletProcessor.class);

    public WalletProcessor(
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

    
    	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+"  ::: WC Message received : ");

        WalletProcessor.forWC(lMessageRequest);
    }

    public static void forWC(MessageRequest lMessageRequest) {
   
        try
        {
            /*
             * if (lMessageRequest.isIsIntl())
             * lMessageRequest.setBillingAddFixedRate(0.0d);
             */
            final String  lClientId            = lMessageRequest.getClientId();
            final String  lFileId              = lMessageRequest.getFileId();
            final String  lBaseMsgId           = lMessageRequest.getBaseMessageId();
            final String  lMsgId               = lMessageRequest.getBaseMessageId();
            int           lTotalMsgParts       = lMessageRequest.getMessageTotalParts();
            final double  lBillingSmsRate      = lMessageRequest.getBillingSmsRate();
            final double  lBillingAddFixedRate = lMessageRequest.getBillingAddFixedRate();
            final boolean isIntl               = lMessageRequest.isIsIntl();

            if (lTotalMsgParts == 0) {
                lTotalMsgParts = 1;
            }

        
        	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+"  :::: Client Id: '" + lClientId + "', Total Parts:'" + lTotalMsgParts + "', BillingSmsRate:'" + lBillingSmsRate + "', BillingAddFixedRate:'" + lBillingAddFixedRate + "'");

            boolean hasWallectDeductStatus = false;
            boolean hasWallectBallance     = false;
            boolean lStatus                = false;
            while (!lStatus)
                try
                {
                    final WalletDeductInput lWalletInput        = WalletInput.getDeductInput(lClientId, lFileId, lBaseMsgId, lMsgId, lTotalMsgParts, lBillingSmsRate, lBillingAddFixedRate, "", isIntl);
                    final WalletResult      lDeductWalletForSMS = WalletDeductRefundProcessor.deductWalletForSMS(lMessageRequest,lWalletInput);

                    if (lDeductWalletForSMS.isSuccess())
                    {
                        hasWallectDeductStatus = true;

                    
                    	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+"  ::: Wallet Deduct status : " + hasWallectDeductStatus);

                        lMessageRequest.setIsWalletDeduct(true);
                    }
                    else
                        hasWallectBallance = true;

                    lStatus = true;
                }
                catch (final ItextosException ite)
                {
                	
                	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+"  ::: Error : " +ErrorMessage.getStackTraceAsString(ite));

                 //   log.error(ite.getMessage());
                    
                    ErrorLog.log(ErrorMessage.getStackTraceAsString(ite));
                    hasWallectDeductStatus = false;
                    lStatus                = true;
                }
                catch (final Exception e)
                {

                    try
                    {
                    	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+" ::: Sleeping 2 sec since redis is not reachable.. ");

                        log.fatal(" ::: Sleeping 2 sec since redis is not reachable.. ");
                        Thread.sleep(2000);
                    }
                    catch (final InterruptedException e2)
                    {}
                }

            if (hasWallectDeductStatus)
                WCProducer.sendToRouterComponent(lMessageRequest);
            else
            {
                WalletUtil.resetWalletInfo(lMessageRequest);

                if (hasWallectBallance)
                    lMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.INSUFFICIENT_WALLET_BALANCE.getStatusCode());
                else
                    lMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.PREPAID_CHECK_FAILED.getStatusCode());

                WCProducer.sendToPlatformRejection(lMessageRequest);
            }
        }
        catch (final Exception e)
        {
        	
        	lMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(lMessageRequest.getBaseMessageId()+"  ::: Exception occer while processing Wallet deduct.."+ErrorMessage.getStackTraceAsString(e));

            log.error("  ::: Exception occer while processing Wallet deduct..", e);
            WCProducer.sendToErrorLog(Component.WC, lMessageRequest, e);
        }

    }
    
    
    @Override
    public void doCleanup()
    {
        final int itrCount = 0;
        log.fatal("Checking for the Prepaid Inmemory completion.");

        while (WalletDeductRefundProcessor.hasInMemoryCleared())
        {
            log.fatal("Attempt : '" + itrCount + ": Waiting for the inmemory of the Prepaid History to clear.");
            CommonUtility.sleepForAWhile();
        }
    }

    @Override
    protected void updateBeforeSendBack(
            IMessage aMessage)
    {
        // TODO Auto-generated method stub
    }

}
