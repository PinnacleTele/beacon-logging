package com.itextos.beacon.platform.rch.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.platform.blockoutprocess.BlockoutChecker;
import com.itextos.beacon.platform.blockoutprocess.BlockoutType;

public class BlockoutCheck
{

    private static final Log     log = LogFactory.getLog(BlockoutCheck.class);
    private final MessageRequest mMessageRequest;

    public BlockoutCheck(
            MessageRequest aMessageRequest)
    {
        this.mMessageRequest = aMessageRequest;
    }

    public boolean isBlockoutProcess()
    {
        boolean canProcess = false;

        if (isBlockoutMessage())
        {
            if (log.isDebugEnabled())
                log.debug("sendBlockoutQueueMessage() start Message Id : " + mMessageRequest.getBaseMessageId());
            mMessageRequest.setFromScheduleBlockout("RC_SCHDBLOCK");
            mMessageRequest.setScheduleBlockoutMessage(Constants.BLOCKOUT_MSG);

            canProcess = true;

            final String       lBlockOutType = mMessageRequest.getBlockoutType();
            final BlockoutType blockout      = BlockoutType.getType(lBlockOutType);

            switch (blockout)
            {
                case TRAI:
                    canProcess = doTraiBlockout();
                    break;

                case CUSTOM:
                    canProcess = doCustomBlockout();
                    break;

                case SPECIFIC:
                    canProcess = doSpecificBlockout();
                    break;

                default:
                    // If the flow comes here, then it means it is a Specific drop
                    canProcess = doSpecificDrop();
                    break;
            }
        }
        return canProcess;
    }

    private boolean doTraiBlockout()
    {
        final int lRetryAttempt = mMessageRequest.getRetryAttempt();

        if (lRetryAttempt != 0)
        {
            RCHProducer.sendToNextLevel(mMessageRequest);
            return true;
        }

        final boolean lBlockOutPurge = mMessageRequest.isDomesticPromoTraBlockoutPurge();

        if (lBlockOutPurge)
        {
            if (log.isDebugEnabled())
                log.debug("isBlockoutMessage() Rejected Promo msg purge due to trai : " + mMessageRequest.getBaseMessageId());

            mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.TRAI_BLOCKOUT_FAILED.getStatusCode());
            RCHProducer.sendToNextLevel(mMessageRequest);
            return true;
        }

        if (log.isDebugEnabled())
            log.debug("isBlockoutMessage() sending to Trai Blockout Queue : " + mMessageRequest.getBaseMessageId());
        RCHProducer.sendToBlockout(mMessageRequest);
        return true;
    }

    private boolean doCustomBlockout()
    {
        final int lRetryAttempt        = mMessageRequest.getRetryAttempt();
        final int lDomesticSmsBlockout = mMessageRequest.getClientDomesticSmsBlockoutEnabled();
        final int lIntlSmsBlockout     = mMessageRequest.getIntlSmsBlockoutEnabled();

        if (lRetryAttempt == 1)
            return false;
        else
            if ((lDomesticSmsBlockout == 2) || (lIntlSmsBlockout == 2))
            {
                if (log.isDebugEnabled())
                    log.debug("Rejected Due to sms_blockout value is 2 " + mMessageRequest.getBaseMessageId());

                mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.SMS_BLOCKOUT_FAILED.getStatusCode());
                mMessageRequest.setPlatfromRejected(true);

                RCHProducer.sendToNextLevel(mMessageRequest);
                return true;
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Sending to Custom Blockout Queue : " + mMessageRequest.getBaseMessageId());
                RCHProducer.sendToBlockout(mMessageRequest);
                return true;
            }
    }

    private boolean doSpecificDrop()
    {
        final int     lRetryAttempt = mMessageRequest.getRetryAttempt();
        final boolean lSpecificDrop = mMessageRequest.isSpecificDrop();

        if (lRetryAttempt == 1)
        {}
        else
            if (lSpecificDrop)
            {
                if (log.isDebugEnabled())
                    log.debug("Message getting dropped due to specific blockout drop Message Id : " + mMessageRequest.getBaseMessageId());

                mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.SPECIFIC_BLOCKOUT_FAILED.getStatusCode());
                mMessageRequest.setPlatfromRejected(true);
                RCHProducer.sendToNextLevel(mMessageRequest);
                return true;
            }
        return false;
    }

    private boolean doSpecificBlockout()
    {
        if (log.isDebugEnabled())
            log.debug("Sending to specific Blockout Queue : " + mMessageRequest.getBaseMessageId());

        return true;
    }

    private boolean isBlockoutMessage()
    {
        if (log.isDebugEnabled())
            log.debug("isBlockoutMessage() start Message Id : " + mMessageRequest.getBaseMessageId());
        return BlockoutChecker.blockoutCheck(mMessageRequest);
    }

}
