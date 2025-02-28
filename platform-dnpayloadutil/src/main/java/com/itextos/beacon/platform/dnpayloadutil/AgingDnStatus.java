package com.itextos.beacon.platform.dnpayloadutil;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.dnpayload.util.DNPUtil;
import com.itextos.beacon.platform.dnpayloadutil.common.PayloadUtil;
import com.itextos.beacon.platform.dnpayloadutil.common.RedisHandler;

public class AgingDnStatus
{

    private static final Log log         = LogFactory.getLog(AgingDnStatus.class);
    private static final int MAX_RETRIES = 3;

    private AgingDnStatus()
    {}

    // To know the status of msg delivery (success/Final dn) - this will help us
    // to stop further retries
    public static boolean agingDnStatus(
            SubmissionObject aSubmissionObject)
    {
        boolean isAgingDNStatus = false;
        int     lNoOfRetries    = 1;

        while (true)
        {
            if (log.isDebugEnabled())
                log.debug("started ..........");

            try
            {
                final String lMNumber  = aSubmissionObject.getMobileNumber();
                final Date   sDate     = aSubmissionObject.getMessageReceivedDate();
                final String lClientId = aSubmissionObject.getClientId();
                final String lMessmid  = aSubmissionObject.getMessageId();

                isAgingDNStatus = RedisHandler.isAgingDnExists(lMNumber, sDate, lClientId, lMessmid);

                if (log.isDebugEnabled())
                    log.debug("Check AgingDN Status for Mobile '" + lMNumber + "' SDate '" + sDate + "' client '" + lClientId + "' mid '" + lMessmid + "' is " + isAgingDNStatus);
                break;
            }
            catch (final Exception e)
            {
                if (log.isDebugEnabled())
                    log.debug("Exception", e);

                ++lNoOfRetries;

                if (lNoOfRetries >= MAX_RETRIES)
                    break;

                CommonUtility.sleepForAWhile(1000);
            }
        }
        return isAgingDNStatus;
    }

    public static boolean findFastDnScheduleTime(
            SubmissionObject aSubmissionObject)
    {
        final String  lClientId                  = aSubmissionObject.getClientId();
        final boolean isFastDnEnabled            = CommonUtility.isEnabled(PayloadUtil.getCutomFeatureValue(lClientId, CustomFeatures.IS_FASTDN_ENABLE));
        final String  fastdnGenerateIntTimeInSec = PayloadUtil.getCutomFeatureValue(lClientId, CustomFeatures.FASTDN_GEN_IN_SEC);

        try
        {

            if (isFastDnEnabled && (aSubmissionObject.getRetryAttempt() == 0))
            {
                final int intervalInSec = CommonUtility.getInteger(fastdnGenerateIntTimeInSec);

                if (intervalInSec > 0)
                {
                    aSubmissionObject.setAgingType(Constants.FASTDN);
                    aSubmissionObject.setAgingScheduleTime(getFastDnSchduleTime(aSubmissionObject, intervalInSec));
                    return true;
                }
                else
                    if (log.isDebugEnabled())
                        log.warn("Account custom features.FASTDN_GEN_IN_SEC is not configrued/positive integer so not sending to INSERT_AGING_DN_TOPIC to generate DN");
            }
        }
        catch (final Exception e)
        {
            log.error("findFastDnScheduleTime exception", e);
        }
        return false;
    }

    public static boolean findAgeDnScheduleTime(
            DeliveryObject aDeliveryObject)
    {
        final String  lClientId      = aDeliveryObject.getClientId();
        final int     lRetryAttempt  = aDeliveryObject.getRetryAttempt();
        final boolean isAgingEnabled = CommonUtility.isEnabled(PayloadUtil.getCutomFeatureValue(lClientId, CustomFeatures.IS_AGING_ENABLE));

        try
        {

            if (isAgingEnabled)
            {
                final int dlrAgeing = DNPUtil.getAgingDNInfo(lClientId, lRetryAttempt);

                if (dlrAgeing > 0)
                {
                    aDeliveryObject.setAgingType(Constants.AGINGDN);
                    aDeliveryObject.setAgingScheduleTime(getAgeDnSchduleTime(aDeliveryObject, dlrAgeing, lRetryAttempt));
                    return true;
                }
            }
        }
        catch (final Exception e)
        {
            log.error("Exception", e);
        }
        return false;
    }

    public static boolean findAgeDnScheduleTime(
            SubmissionObject aSubmissionObject)
    {
        final String  lClientId      = aSubmissionObject.getClientId();
        final int     lRetryAttempt  = aSubmissionObject.getRetryAttempt();
        final boolean isAgingEnabled = CommonUtility.isEnabled(PayloadUtil.getCutomFeatureValue(lClientId, CustomFeatures.IS_AGING_ENABLE));

        try
        {

            if (isAgingEnabled)
            {
                final int dlrAgeing = DNPUtil.getAgingDNInfo(lClientId, lRetryAttempt);

                if (dlrAgeing > 0)
                {
                    aSubmissionObject.setAgingType(Constants.AGINGDN);
                    aSubmissionObject.setAgingScheduleTime(getAgeDnSchduleTime(aSubmissionObject, dlrAgeing, lRetryAttempt));
                    return true;
                }
            }
        }
        catch (final Exception e)
        {
            log.error("Exception", e);
        }
        return false;
    }

    private static Date getFastDnSchduleTime(
            SubmissionObject aSubmissionObject,
            int interval)
    {
        final Date lSDate              = aSubmissionObject.getMessageReceivedTime();
        final long lFastDnScheduleTime = lSDate.getTime() + (interval * 1000);

        if (log.isDebugEnabled())
            log.debug("FastDN ScheduleTime:" + DateTimeUtility.getFormattedDateTime(new Date(lFastDnScheduleTime), DateTimeFormat.DEFAULT));

        return new Date(lFastDnScheduleTime);
    }

    private static Date getAgeDnSchduleTime(
            DeliveryObject aDeliveryObject,
            int interval,
            int aRetryAttempt)
    {
        long lAgingDnScheduleTime;

        if (aRetryAttempt == 0)
            lAgingDnScheduleTime = aDeliveryObject.getMessageReceivedTime().getTime() + (interval * 1000);
        else
            // retry messages let make retry with current time
            lAgingDnScheduleTime = System.currentTimeMillis() + (interval * 1000);

        if (log.isDebugEnabled())
            log.debug("AgingDN ScheduleTime:" + DateTimeUtility.getFormattedDateTime(new Date(lAgingDnScheduleTime), DateTimeFormat.DEFAULT));

        return new Date(lAgingDnScheduleTime);
    }

    private static Date getAgeDnSchduleTime(
            SubmissionObject aSubmissionObject,
            int interval,
            int aRetryAttempt)
    {
        long lAgingDnScheduleTime;

        if (aRetryAttempt == 0)
            lAgingDnScheduleTime = aSubmissionObject.getMessageReceivedTime().getTime() + (interval * 1000);
        else
            // retry messages let make retry with current time
            lAgingDnScheduleTime = System.currentTimeMillis() + (interval * 1000);

        if (log.isDebugEnabled())
            log.debug("AgingDN ScheduleTime:" + DateTimeUtility.getFormattedDateTime(new Date(lAgingDnScheduleTime), DateTimeFormat.DEFAULT));

        return new Date(lAgingDnScheduleTime);
    }

}