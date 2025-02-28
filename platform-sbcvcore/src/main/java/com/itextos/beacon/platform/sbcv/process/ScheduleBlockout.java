package com.itextos.beacon.platform.sbcv.process;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.blockoutprocess.BlockoutChecker;
import com.itextos.beacon.platform.blockoutprocess.BlockoutType;

public class ScheduleBlockout
{

    private static final Log     log            = LogFactory.getLog(ScheduleBlockout.class);
    private static final String  SCHEDULE_BLOCK = "VC_SCHDBLOCK";

    private final MessageRequest mMessageRequest;

    public ScheduleBlockout(
            MessageRequest aMessageRequest)
    {
        mMessageRequest = aMessageRequest;
    }

    public boolean validateScheduleBlockoutMsg()
    {
        boolean canProcess = true;

        if (log.isDebugEnabled())
            log.debug("Check for Schedule Blockout. Base Message Id : " + mMessageRequest.getBaseMessageId());

        try
        {
            final boolean isScheduleMessage = isScheduleMessage();

            if (log.isDebugEnabled())
                log.debug("Is schedule Message ? " + isScheduleMessage);

            if (isScheduleMessage)
            {
                mMessageRequest.setBlockoutType("schedule");
                mMessageRequest.setScheduleBlockoutMessage(Constants.SCHEDULE_MSG);
                mMessageRequest.setFromScheduleBlockout(SCHEDULE_BLOCK);
                canProcess = false;
            }
            else
            {
                final boolean lBlockoutMessage = isBlockoutMessage();

                if (lBlockoutMessage)
                {
                    canProcess = false;
                    mMessageRequest.setFromScheduleBlockout(SCHEDULE_BLOCK);
                    mMessageRequest.setScheduleBlockoutMessage(Constants.BLOCKOUT_MSG);

                    final String       lBlockOutType = mMessageRequest.getBlockoutType();
                    final BlockoutType blockout      = BlockoutType.getType(lBlockOutType);

                    switch (blockout)
                    {
                        case TRAI:
                            doTraiBlockout();
                            break;

                        case CUSTOM:
                            doCustomBlockout();
                            break;

                        case SPECIFIC:
                            doSpecificBlockout();
                            break;

                        default:
                            // If the flow comes here, then it means it is a Specific drop
                            doSpecificDrop();
                            break;
                    }
                }
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while checking Schedule / Blockout. " + mMessageRequest, e);
        }
        return canProcess;
    }

    private void doSpecificDrop()
    {
        final boolean lSpecificDrop = mMessageRequest.isSpecificDrop();

        if (lSpecificDrop)
        {
            if (log.isDebugEnabled())
                log.debug("Message getting dropped due to specific blockout drop Base Message Id : " + mMessageRequest.getBaseMessageId());

            mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.SPECIFIC_BLOCKOUT_FAILED.getStatusCode());
            mMessageRequest.setPlatfromRejected(true);
        }
    }

    private void doSpecificBlockout()
    {
        if (log.isDebugEnabled())
            log.debug("Sending to specific Blockout Queue : " + mMessageRequest.getBaseMessageId());
    }

    private void doCustomBlockout()
    {
        final int lDomesticSmsBlockout = mMessageRequest.getClientDomesticSmsBlockoutEnabled();
        final int lIntlSmsBlockout     = mMessageRequest.getIntlSmsBlockoutEnabled();

        // TODO We need to check for the intl message and then reject.
        if ((lDomesticSmsBlockout == 2) || (lIntlSmsBlockout == 2))
        {
            if (log.isDebugEnabled())
                log.debug("Rejected Due to sms_blockout value is 2 " + mMessageRequest.getBaseMessageId());

            mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.SMS_BLOCKOUT_FAILED.getStatusCode());
            mMessageRequest.setPlatfromRejected(true);
        }
        else
            if (log.isDebugEnabled())
                log.debug("Sending to Custom Blockout Queue : " + mMessageRequest.getBaseMessageId());
    }

    private void doTraiBlockout()
    {
        final boolean lBlockOutPurge = mMessageRequest.isDomesticPromoTraBlockoutPurge();

        if (lBlockOutPurge)
        {
            if (log.isDebugEnabled())
                log.debug("isBlockoutMessage() Rejected Promo msg purge due to trai : " + mMessageRequest.getBaseMessageId());

            mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.TRAI_BLOCKOUT_FAILED.getStatusCode());
            mMessageRequest.setPlatfromRejected(true);
        }
        else
            if (log.isDebugEnabled())
                log.debug("isBlockoutMessage() sending to Trai Blockout Queue : " + mMessageRequest.getBaseMessageId());
    }

    private boolean isScheduleMessage()
    {
        if (log.isDebugEnabled())
            log.debug("Base Message Id : " + mMessageRequest.getBaseMessageId());

        try
        {
            /**
             * For International Message Request we are considered as current message.
             * Schedule feature is applicable only for domestic.
             */
            if (mMessageRequest.isIsIntl())
                return false;

            final Date lTempScheduleTime = mMessageRequest.getScheduleDateTime();

            if (log.isDebugEnabled())
                log.debug("Schedule Date Time : " + lTempScheduleTime);

            if ((lTempScheduleTime != null) && (lTempScheduleTime.getTime() > System.currentTimeMillis()))
                return true;
            // TODO Don't we need to set expire as the schedule time crossed?
        }
        catch (final Exception e)
        {
            // ignore
        }
        return false;
    }

    private boolean isBlockoutMessage()
    {
        if (log.isDebugEnabled())
            log.debug("Base Message Id : " + mMessageRequest.getBaseMessageId());
        return BlockoutChecker.blockoutCheck(mMessageRequest);
    }

    public static void main(
            String[] args)
    {
        final String lScheduleTime = "2021-06-20 19:51:20";
        final Date   d             = DateTimeUtility.getDateFromString(lScheduleTime, DateTimeFormat.DEFAULT);

        System.out.println("d : " + d);
    }

}