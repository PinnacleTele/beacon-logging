package com.itextos.beacon.platform.ch.util;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.platform.blockoutprocess.BlockoutChecker;
import com.itextos.beacon.platform.blockoutprocess.BlockoutType;

public class BlockoutCheck
{

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
       
        	mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" :: sendBlockoutQueueMessage() start Message Id : ");

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
            CHProducer.sendToNextLevel(mMessageRequest);
            return true;
        }

        final boolean lBlockOutPurge = mMessageRequest.isDomesticPromoTraBlockoutPurge();

        if (lBlockOutPurge)
        {
      
        	mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" :: isBlockoutMessage() Rejected Promo msg purge due to trai : ");

            mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.TRAI_BLOCKOUT_FAILED.getStatusCode());
            CHProducer.sendToNextLevel(mMessageRequest);
            return true;
        }

        mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" :: isBlockoutMessage() sending to Trai Blockout Queue : ");

        CHProducer.sendToBlockout(mMessageRequest);
        return true;
    }

    private boolean doCustomBlockout()
    {
        final int lRetryAttempt        = mMessageRequest.getRetryAttempt();
        final int lDomesticSmsBlockout = mMessageRequest.getClientDomesticSmsBlockoutEnabled();
        final int lIntlSmsBlockout     = mMessageRequest.getIntlSmsBlockoutEnabled();

        if (lRetryAttempt == 1)
            return false;

        if ((lDomesticSmsBlockout == 2) || (lIntlSmsBlockout == 2))
        {
      
        	mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" :: Rejected Due to sms_blockout value is 2 ");

            mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.SMS_BLOCKOUT_FAILED.getStatusCode());
            mMessageRequest.setPlatfromRejected(true);

            CHProducer.sendToNextLevel(mMessageRequest);
            return true;
        }

        mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" :: Sending to Custom Blockout Queue : ");

        CHProducer.sendToBlockout(mMessageRequest);
        return true;
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
      
            	mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" :: Message getting dropped due to specific blockout drop Message Id : ");

                mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.SPECIFIC_BLOCKOUT_FAILED.getStatusCode());
                mMessageRequest.setPlatfromRejected(true);
                CHProducer.sendToNextLevel(mMessageRequest);
                return true;
            }
        return false;
    }

    private boolean doSpecificBlockout()
    {
      
    	mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" :: Sending to specific Blockout Queue : ");

        return true;
    }

    private boolean isBlockoutMessage()
    {
    	mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" :: isBlockoutMessage() start Message Id : ");

        return BlockoutChecker.blockoutCheck(mMessageRequest);
    }

}
