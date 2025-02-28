package com.itextos.beacon.platform.blockoutprocess;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.pattern.PatternCache;
import com.itextos.beacon.commonlib.pattern.PatternCheckCategory;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.specificblockoutdata.SpecificBlockoutData;
import com.itextos.beacon.inmemory.specificblockoutdata.SpecificBlockoutDataMap;

public class BlockoutChecker
{

    private static final Log log                     = LogFactory.getLog(BlockoutChecker.class);
    private static final int VALIDATION_NOT_REQUIRED = 0;
    private static final int VALIDATION_REQUIRED     = 1;

    private BlockoutChecker()
    {}

    public static boolean blockoutCheck(
            MessageRequest aMessageRequest)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("BlockoutCheck Started. ");

            // If international message check it for INTL Block-out only.
            // No need to check for other block-outs.
            if (aMessageRequest.isIsIntl())
                return checkForCustomIntlBlockout(aMessageRequest);

            final boolean isDomesticBlockout = checkForCustomDomesticBlockout(aMessageRequest);

            if (isDomesticBlockout)
                return true;

            final boolean isTraBlockout = checkForTraBlockout(aMessageRequest);

            if (isTraBlockout)
                return true;

            final boolean isSpecificBlockout = checkForSpecificBlockout(aMessageRequest);
            if (isSpecificBlockout)
                return true;
        }
        catch (final Exception e)
        {
            log.error("problem checking blockout...", e);
        }

        return false;
    }

    private static boolean checkForCustomIntlBlockout(
            MessageRequest aMessageRequest)
    {

        try
        {
            final int    smsBlockoutType   = aMessageRequest.getIntlSmsBlockoutEnabled();
            final String blockoutStartTime = CommonUtility.nullCheck(aMessageRequest.getIntlSmsBlockoutStart());
            final String blockoutEndTime   = CommonUtility.nullCheck(aMessageRequest.getIntlSmsBlockoutStop());
            return checkBlockout(BlockoutType.CUSTOM, true, aMessageRequest, smsBlockoutType, blockoutStartTime, blockoutEndTime);
        }
        catch (final Exception e)
        {
            log.error("Exception while doing International Blockout check. Message " + aMessageRequest, e);
        }
        return false;
    }

    private static boolean checkForCustomDomesticBlockout(
            MessageRequest aMessageRequest)
    {

        try
        {
            final int smsBlockoutType = aMessageRequest.getClientDomesticSmsBlockoutEnabled();
            if (log.isDebugEnabled())
                log.debug("Domestic  SMS Blockot  from account :" + smsBlockoutType);
            final String blockoutStartTime = CommonUtility.nullCheck(aMessageRequest.getClientDomesticSmsBlockoutStart());
            final String blockoutEndTime   = CommonUtility.nullCheck(aMessageRequest.getClientDomesticSmsBlockoutStop());
            return checkBlockout(BlockoutType.CUSTOM, false, aMessageRequest, smsBlockoutType, blockoutStartTime, blockoutEndTime);
        }
        catch (final Exception e)
        {
            log.error("Exception while doing Domestic Blockout check. Message " + aMessageRequest, e);
        }
        return false;
    }

    private static boolean checkForTraBlockout(
            MessageRequest aMessageRequest)
    {
        final boolean isTraCheckREquired = (aMessageRequest.getMessageType() == MessageType.PROMOTIONAL);

        if (isTraCheckREquired)
            return traBlockoutCheck(aMessageRequest);
        return false;
    }

    public static boolean traBlockoutCheck(
            MessageRequest aMessageRequest)
    {

        try
        {
            final String startTime = getConfigValueAsString(ConfigParamConstants.TRAI_BLOCKOUT_START.getKey());
            final String stopTime  = getConfigValueAsString(ConfigParamConstants.TRAI_BLOCKOUT_STOP.getKey());
            return checkBlockout(BlockoutType.TRAI, false, aMessageRequest, VALIDATION_REQUIRED, startTime, stopTime);
        }
        catch (final Exception e)
        {
            log.error("Exception while doing TRA Blockout check. Message " + aMessageRequest, e);
        }
        return false;
    }

    private static boolean checkForSpecificBlockout(
            MessageRequest aMessageRequest)
    {
        aMessageRequest.setSpecificDrop(false);  // assume drop is "false"
        final int specificBlockout = aMessageRequest.getSpecificBlockoutCheck();

        if (VALIDATION_REQUIRED == specificBlockout)
            return specificBlockoutCheck(aMessageRequest);
        return false;
    }

    public static boolean specificBlockoutCheck(
            MessageRequest aMessageRequest)
    {
        boolean returnValue = false;

        try
        {
            final String                     lClientId                = aMessageRequest.getClientId();
            final String                     lMobileNumber            = aMessageRequest.getMobileNumber();
            final String                     lLongMessage             = CommonUtility.nullCheck(aMessageRequest.getLongMessage());

            final SpecificBlockoutDataMap    lSpecificBlockoutDataMap = (SpecificBlockoutDataMap) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.SPECIFIC_BLOCKOUT);
            final List<SpecificBlockoutData> lSpecificBlockoutList    = lSpecificBlockoutDataMap.getSpecificBlockoutData(lClientId, lMobileNumber);

            if (log.isDebugEnabled())
                log.debug("Specific blockout check started for Client : " + lClientId + " Mobile number : " + lMobileNumber + " Message : " + lLongMessage + " BlockList size "
                        + lSpecificBlockoutList.size());

            for (final SpecificBlockoutData specificBlockoutData : lSpecificBlockoutList)
            {
                final String  blockoutPattern   = specificBlockoutData.getMessagePattern();
                final String  blockoutStartTime = specificBlockoutData.getBlockoutStartTime();
                final String  blockoutEndTime   = specificBlockoutData.getBlockoutEndTime();
                final boolean dropOut           = specificBlockoutData.IsDropMessage();

                // TODO Need to change the PatternCheckCategory
                final boolean lPatternMatch     = PatternCache.getInstance().isPatternMatch(PatternCheckCategory.SPAM_CHECK, blockoutPattern, lLongMessage);

                if (lPatternMatch)
                    returnValue = checkToBlock(aMessageRequest, blockoutStartTime, blockoutEndTime, dropOut);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while doing Specific Blockout check. Message " + aMessageRequest, e);
        }
        return returnValue;
    }

    private static boolean checkBlockout(
            BlockoutType aBlockoutType,
            boolean aIsIntl,
            MessageRequest aMessageRequest,
            int aBlockOutType,
            String aBlockoutStart,
            String aBlockoutStop)
    {
        if (log.isDebugEnabled())
            log.debug("Blockout check type : " + aBlockoutType + " Is Intl Check : " + aIsIntl + " Blockout type : " + aBlockOutType + " Start Time : " + aBlockoutStart + " End Time : "
                    + aBlockoutStop);
        boolean returnValue = false;

        if (aBlockOutType != VALIDATION_NOT_REQUIRED)
        {
            final Date blockoutScheduleTime = getBlockoutScheduleTime(aBlockoutStart, aBlockoutStop);

            if (blockoutScheduleTime != null)
            {
                aMessageRequest.setBlockoutType(aBlockoutType.getKey());
                aMessageRequest.setScheduleDateTime(blockoutScheduleTime);
                aMessageRequest.setProcessBlockoutTime(blockoutScheduleTime);
                returnValue = true;
            }
        }
        if (log.isDebugEnabled())
            log.debug("Result Blockout check type : " + aBlockoutType + " Is Intl Check : " + aIsIntl + " Blockout type : " + aBlockOutType + " Start Time : " + aBlockoutStart + " End Time : "
                    + aBlockoutStop + " is " + returnValue);

        return returnValue;
    }

    private static boolean checkToBlock(
            MessageRequest aMessageRequest,
            String aBlockoutStart,
            String aBlockoutStop,
            boolean isMsgSpecificDrop)
    {

        if (isMsgSpecificDrop)
        {
            aMessageRequest.setSpecificDrop(true);
            return true;
        }

        final Date blockoutScheduleTime = getBlockoutScheduleTime(aBlockoutStart, aBlockoutStop);

        if (blockoutScheduleTime != null)
        {
            aMessageRequest.setBlockoutType(BlockoutType.SPECIFIC.getKey());
            aMessageRequest.setScheduleDateTime(blockoutScheduleTime);
            aMessageRequest.setProcessBlockoutTime(blockoutScheduleTime);
            return true;
        }
        return false;
    }

    private static String getConfigValueAsString(
            String aConfigKey)
    {
        final ApplicationConfiguration lAppConfig = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfig.getConfigValue(aConfigKey);
    }

    private static Date getBlockoutScheduleTime(
            String aBlockoutStart,
            String aBlockoutStop)
    {
        final float    startTime       = CommonUtility.getFloat(aBlockoutStart.replace(':', '.'));
        final float    stopTime        = CommonUtility.getFloat(aBlockoutStop.replace(':', '.'));

        final Calendar currentDateTime = Calendar.getInstance();
        currentDateTime.setLenient(false);

        //
        // currentDateTime.set(Calendar.HOUR_OF_DAY, 00);
        // currentDateTime.set(Calendar.MINUTE, 00);
        // currentDateTime.set(Calendar.SECOND, 00);

        final float currentTime = CommonUtility.getFloat(new SimpleDateFormat("HH.mm").format(currentDateTime.getTime()));

        if ((startTime == 0) || (stopTime == 0) || (startTime == stopTime))
            return null;

        int   daysToAdd = 0;
        float timeSet   = 0.0f;

        if (startTime >= stopTime) // Starts on current day and ends in next day
        {
            if (log.isDebugEnabled())
                log.debug(getPrintString(startTime, stopTime, currentTime) + " Blockout starts on current day and ends on next day");

            if ((currentTime > stopTime) && (currentTime < startTime))
            {
                if (log.isDebugEnabled())
                    log.debug(getPrintString(startTime, stopTime, currentTime) + " Time is not in blockout. Message will deliver immediatly.");

                daysToAdd = 0;
                timeSet   = 0f;
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug(getPrintString(startTime, stopTime, currentTime) + " Time is not falling in under blockout.");

                if (currentTime >= startTime)
                {
                    if (log.isDebugEnabled())
                        log.debug(getPrintString(startTime, stopTime, currentTime) + " Current Time is more than Start time. Under Blockout time. Message will dliver on next day");

                    daysToAdd = 1;
                    timeSet   = stopTime;
                }
                else
                    if (currentTime < stopTime)
                    {
                        if (log.isDebugEnabled())
                            log.debug(getPrintString(startTime, stopTime, currentTime) + " Current Time is less than stop time. Under Blockout time. Message will delvier today after blockout time.");

                        daysToAdd = 0;
                        timeSet   = stopTime;
                    }
                    else
                    {
                        daysToAdd = 0;
                        timeSet   = 0;
                    }
            }
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug(getPrintString(startTime, stopTime, currentTime) + " Blockout starts on current day and ends on Current day");

            if (currentTime < startTime)
            {
                if (log.isDebugEnabled())
                    log.debug(getPrintString(startTime, stopTime, currentTime) + " Blockout yet to start for Current day. Message will deliver immediatly.");

                daysToAdd = 0;
                timeSet   = 0f;
            }
            else
                // if ((currentTime >= startTime) && (currentTime < stopTime))
                if ((currentTime < stopTime))
                {
                    if (log.isDebugEnabled())
                        log.debug(getPrintString(startTime, stopTime, currentTime) + " Blockout already started for Current day. Message will deliver after blockout time.");

                    timeSet   = stopTime;
                    daysToAdd = 0;
                }
                else
                // if (currentTime >= stopTime)
                {
                    if (log.isDebugEnabled())
                        log.debug(getPrintString(startTime, stopTime, currentTime) + " Blockout already over for Current day. Message will deliver immediatly.");

                    timeSet   = 0;
                    daysToAdd = 0;
                }
        }

        if ((daysToAdd == 0) && (timeSet == 0))
        {
            if (log.isDebugEnabled())
                log.debug("Check BlockoutScheduleTime for the start time '" + aBlockoutStart + "' and end time '" + aBlockoutStop + "' is null");
            return null;
        }

        if (daysToAdd > 0)
            currentDateTime.add(Calendar.DATE, daysToAdd);

        if (timeSet > 0)
        {
            final int[] hourMin = getHourAndtime(timeSet);

            currentDateTime.set(Calendar.HOUR_OF_DAY, hourMin[0]);
            currentDateTime.set(Calendar.MINUTE, hourMin[1]);
            currentDateTime.set(Calendar.SECOND, 0);
        }

        if (log.isDebugEnabled())
            log.debug("Check BlockoutScheduleTime for the start time '" + aBlockoutStart + "' and end time '" + aBlockoutStop + "' is " + currentDateTime.getTime());

        return currentDateTime.getTime();
    }

    private static int[] getHourAndtime(
            float aValue)
    {
        final String decimalString  = String.valueOf(aValue);
        final int    indexOfDecimal = decimalString.indexOf(".");
        final String hour           = decimalString.substring(0, indexOfDecimal);
        String       min            = decimalString.substring(indexOfDecimal + 1);

        if (min.length() == 1)
            min += "0";

        return new int[]
        { CommonUtility.getInteger(hour), CommonUtility.getInteger(min) };
    }

    private static String getPrintString(
            float aBlockoutStart,
            float aBlockoutStop,
            float aCurrentTime)
    {
        final DecimalFormat df = new DecimalFormat("00.00");
        return CommonUtility.combine(' ', "Blockout Start time :", df.format(aBlockoutStart), "Blockout End Time :", df.format(aBlockoutStop), "Current Time :", df.format(aCurrentTime));
    }

    public static void main(
            String[] args)
    {
        final Date lBlockoutScheduleTime = getBlockoutScheduleTime("18:00", "13:02");
        System.out.println(lBlockoutScheduleTime);
    }

}