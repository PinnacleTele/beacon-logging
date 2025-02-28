package com.itextos.beacon.platform.bwc.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.AbstractKafkaComponentProcessor;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.bwc.util.BWCProducer;
import com.itextos.beacon.platform.bwc.util.WalletUtil;
import com.itextos.beacon.platform.walletprocess.WalletDeductRefundProcessor;

public class BlockoutWalletProcessor
        extends
        AbstractKafkaComponentProcessor
{

    private static final Log log = LogFactory.getLog(BlockoutWalletProcessor.class);

    public BlockoutWalletProcessor(
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
            log.debug("BWC Message received : " + lMessageRequest);

        BlockoutWalletProcessor.forBWC(lMessageRequest);
     }

    public static void forBWC(MessageRequest lMessageRequest) {
    
    	  try
          {
              WalletUtil.prepaidRefund(lMessageRequest);

              if (log.isDebugEnabled())
                  log.debug("Successfully refund the balance...");
              final boolean lStatus = WalletUtil.resetWalletInfo(lMessageRequest);

              if (!lStatus)
                  return;

              WalletUtil.prepaidDeduct(lMessageRequest);

              if (log.isDebugEnabled())
                  log.debug("Successfully deduct the balance...");
          }
          catch (final Exception e)
          {
              log.error("Exception occer while processing Wallet deduct..", e);
              BWCProducer.sendToErrorLog(Component.BWC, lMessageRequest, e);
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
