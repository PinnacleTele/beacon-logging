package com.itextos.beacon.platform.dnpayloadutil.common;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.protocol.Message;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.dnpayload.slab.ChildSlab;
import com.itextos.beacon.inmemory.dnpayload.slab.DNSlab;

/**
 * This utility will be used to setup the DTime based on the other values and
 * SLS timings. Should be used from DN , Router Queue Use
 * <OL>
 * <LI>{@link #adjustAndSetDTime(Message)} : To adjust the DTime value
 * <LI>{@link #calculateSubmissionLatencies(Message)} : To calculate the
 * submission latencies
 * <LI>{@link #calculateDeliveryLatencies(Message)} : To calculate the
 * deliveries latencies
 * </ul>
 * <p>
 * CAUTION - START
 * <UL>
 * <LI>Still need to work for the multiple time zones.
 * <LI>Still need to work on the same time zone, but with different time at OS
 * level between the our servers and operator servers.
 * </UL>
 * CAUTION - END
 */
public class TimeAdjustmentUtility
{

    private static final Log                   log                        = LogFactory.getLog(TimeAdjustmentUtility.class);

    private static final double                MAX_DATABASE_ALLOWED_VALUE = 9999999.999;
    private static final long                  MAX_ALLOWED_VALUE          = 9999999999L;
    private static final DateTimeFormat        FINAL_DATE_TIME_FORMAT     = DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM_SS;
    private static final String                SECOND                     = "ss";
    private static final String                DELIVERD_STATUS            = "Delivered";
    private static final String                CARRIER_DELIVERD_STATUS    = "DELIVRD";

    private static Map<String, DateTimeFormat> kannelDateTimeFormats      = new HashMap<>();

    static
    {
        kannelDateTimeFormats.put("yyMMddHHmmss", DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM);
        kannelDateTimeFormats.put("yyMMddHHmm", DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM_SS);
    }

    private TimeAdjustmentUtility()
    {}

    public static void maskFailToSuccessCode(
            DeliveryObject aDeliveryObject)
    {

        if (!CARRIER_DELIVERD_STATUS.equals(aDeliveryObject.getCarrierStatusDesc()))
        {
            final String lClientId          = aDeliveryObject.getClientId();
            final String lMsgType           = aDeliveryObject.getMessageType().getKey();
            final String lPriority          = aDeliveryObject.getMessagePriority().getKey();
            final String lRouteId           = aDeliveryObject.getRouteId();
            final String lCarrierStatusCode = aDeliveryObject.getCarrierStatusCode();
            final String lMNumber           = aDeliveryObject.getMobileNumber();
            final String lCircle            = aDeliveryObject.getCircle();

            final String lMaskedRouteId     = MaskingErrorCodeUtil.getInstance().getMaskedRouteId(lClientId, lMsgType, lPriority, lRouteId, lCarrierStatusCode, lMNumber, lCircle);

            if (log.isDebugEnabled())
                log.debug("lMaskedRouteId: " + lMaskedRouteId + " Message Id:" + aDeliveryObject.getMessageId());

            if (lMaskedRouteId != null)
            {
                aDeliveryObject.setCarrierStatusCode(PlatformStatusCode.DEFAULT_CARRIER_STATUS_ID.getStatusCode());
                aDeliveryObject.setCarrierStatusDesc(DELIVERD_STATUS);
                aDeliveryObject.setCarrierDeliveryStatus(CARRIER_DELIVERD_STATUS);
                aDeliveryObject.setRouteId(lMaskedRouteId);

                PayloadUtil.setPlatformErrorCodeBasedOnCarrierErrorCode(aDeliveryObject);
            }
        }
    }

    /**
     * Based on the <code>DateTimeFormat</code> specified in the
     * <code>route_configuration</code> table, the DTime will be parsed.
     * <ol>
     * <li>For SMSCs we will always get the DTime in the format of
     * <code>'yyyy-MM-dd HH:mm:ss'</code>. This should be configured in the
     * <code>route_config</code> always.
     * <li>For Kannels we may get the DTime either with seconds or without
     * seconds. If the Dtime is not in the format specified in
     * <code>DateTimeFormat</code>, then we have to use the other format.
     * <li>If still we have problem we will pass the current time as DTime.
     * </ol>
     * Final value for 'DTime' and 'Actual DTime' will be in
     * <code>'yyMMddHHmmss'</code> format, with the value obtained.
     *
     * @param aDeliveryObject
     */
    public static void adjustAndSetDTime(
            DeliveryObject aDeliveryObject)
    {
        long lDtime       = System.currentTimeMillis();
        long lActualDtime = lDtime;

        try
        {
            // Get the DTime
            final long[] lDTimes = getDTimeFromMap(aDeliveryObject);

            lDtime       = lDTimes[0];
            lActualDtime = lDTimes[1];

            final Date lSTimeDate = aDeliveryObject.getMessageReceivedTime();
            final long lSts       = aDeliveryObject.getCarrierSubmitTime() == null ? 0 : aDeliveryObject.getCarrierSubmitTime().getTime();

            if (log.isDebugEnabled())
                log.debug("lDtime: " + lDtime + " lActualDtime:" + lActualDtime + " :" + aDeliveryObject.getActualDeliveryTime() + ", Received Time:" + lSTimeDate + ", Sts:" + lSts);

            // If the DTime is less than STS then add some seconds to the DTime.
            if (lDtime < lSts)
            {
                lDtime = addSecondToTime(lSts);
                if (log.isDebugEnabled())
                    log.debug("Due to lDtime < sts - after adjusted lDtime:" + lDtime);
            }

            final int lRetryAttempt = aDeliveryObject.getRetryAttempt();

            if (log.isDebugEnabled())
                log.debug("lDtime:" + lDtime + " sTimeDate:" + lSTimeDate + " sts:" + lSts + " repeat:" + lRetryAttempt);

            final boolean isNumberInWhitelist = PayloadUtil.checkNumberWhiteListed(aDeliveryObject.getMobileNumber());
            final boolean isCircleExcludeList = PayloadUtil.isCircleInExcludeList(aDeliveryObject.getClientId(), aDeliveryObject.getCircle());

            if (log.isDebugEnabled())
                log.debug("isNumberInWhitelist:" + isNumberInWhitelist + " isCircleExcludeList:" + isCircleExcludeList);

            if (canDoSlabAdjustments(aDeliveryObject, isNumberInWhitelist, isCircleExcludeList))
            {
                final long lSlabDtime = doSlabAdjustments(aDeliveryObject, lDtime, lSts);
                if (lSlabDtime != 0)
                    lDtime = lSlabDtime;

                if (log.isDebugEnabled())
                    log.debug("After dn slab adjustment lSlabDtime--:" + lSlabDtime);
            }

            if (log.isDebugEnabled())
                log.debug("dtime:" + DateTimeUtility.getFormattedDateTime(lDtime, FINAL_DATE_TIME_FORMAT) + " actual_dtime:"
                        + DateTimeUtility.getFormattedDateTime(lActualDtime, FINAL_DATE_TIME_FORMAT));

            aDeliveryObject.setDeliveryTime(new Date(lDtime));
            aDeliveryObject.setCarrierDateTimeFormat(FINAL_DATE_TIME_FORMAT.getFormat());

            // this will not reset for retry messages - need to maintain actual dtime recvd
            // from carrier
            if (aDeliveryObject.getActualDeliveryTime() == null)
                aDeliveryObject.setActualDeliveryTime(new Date(lActualDtime));
        }
        catch (final Exception e)
        {
            log.warn("Some exception while calculating and setting the DTime value. Setting the current time as DTime.", e);
            setDefaultTime(aDeliveryObject);
        }
    }

    private static void setDefaultTime(
            DeliveryObject aDeliveryObject)
    {

        try
        {
            final long lDtime       = System.currentTimeMillis();
            final long lActualDtime = System.currentTimeMillis();
            aDeliveryObject.setDeliveryTime(new Date(lDtime));
            aDeliveryObject.setCarrierDateTimeFormat(FINAL_DATE_TIME_FORMAT.getFormat());
            aDeliveryObject.setActualDeliveryTime(new Date(lActualDtime));
        }
        catch (final Exception e1)
        {
            log.error("Some exception while Setting the DTime values. Setting the current time as DTime.", e1);
        }
    }

    private static boolean canDoSlabAdjustments(
            DeliveryObject aDeliveryObject,
            boolean aIsNumberInWhitelist,
            boolean aIsCircleExclude)
    {

        try
        {
            final boolean isDNSlabAdjustEnable = CommonUtility.isEnabled(PayloadUtil.getCutomFeatureValue(aDeliveryObject.getClientId(), CustomFeatures.DN_SLAB_ENABLE));

            if (!aIsNumberInWhitelist && !aIsCircleExclude && isDNSlabAdjustEnable && (aDeliveryObject.getCarrierStatusCode() != null)
                    && (aDeliveryObject.getCarrierStatusCode().equals(PlatformStatusCode.DEFAULT_CARRIER_STATUS_ID.getStatusCode())))
                return true;
            else
                if (log.isDebugEnabled())
                    log.debug("No dn slab adjustment done due to DN_SLAB_ENABLE disabled/not delivered msg/LASTTS_ADJUST is not 0/number is in whitelist_mobile/circle excluded");
        }
        catch (final Exception e)
        {
            log.error("Exception", e);
        }
        return false;
    }

    private static long doSlabAdjustments(
            BaseMessage aBaseMessage,
            long aDtime,
            long aSts)
    {
        long dtime = 0;

        try
        {
            final ChildSlab lDNSlabInfo = DNSlab.getInstance().doSlabAdjustments(aBaseMessage.getValue(MiddlewareConstant.MW_CLIENT_ID), aDtime, aSts);

            if (lDNSlabInfo != null)
            {
                if (log.isDebugEnabled())
                    log.debug("Dnslab : " + lDNSlabInfo);

                final int lStart        = CommonUtility.getInteger(lDNSlabInfo.getChildStartInSec());
                final int lEnd          = CommonUtility.getInteger(lDNSlabInfo.getChildEndInSec());
                final int lRandomSecond = getRandomSecond(lStart, lEnd);

                if (log.isDebugEnabled())
                    log.debug("RandomSecond : " + lRandomSecond);

                final Calendar stscal = Calendar.getInstance();
                stscal.setLenient(false);
                stscal.setTimeInMillis(aSts);
                stscal.add(Calendar.SECOND, lRandomSecond);

                dtime = stscal.getTimeInMillis();
            }
            else
                if (log.isDebugEnabled())
                    log.debug("No Slabs configured");
        }
        catch (final Exception e)
        {
            log.error("Exception", e);
        }
        return dtime;
    }

    private static int getRandomSecond(
            int aStart,
            int aEnd)
    {
        final Random r = new Random();
        return aStart + r.nextInt(aEnd - aStart);
    }

    private static long[] getDTimeFromMap(
            DeliveryObject aDeliveryObject)
    {
        Date lDTime       = null;
        Date lActualDtime = null;

        try
        {
            lDTime = aDeliveryObject.getDeliveryTime();
            if (log.isDebugEnabled())
                log.debug("Delivery Time : " + lDTime);

            final String lDTimeFormat = aDeliveryObject.getCarrierDateTimeFormat();

            // Still unable to parse the DTime then set the current time to the
            // DTime.
            if (lDTime == null)
            {
                if (log.isInfoEnabled())
                    log.info("Setting the current time as DTime as there is some problem in gettting the DTime from the operator");

                lDTime       = new Date();
                lActualDtime = lDTime;
            }
            else
            {
                // lDtime = lDTimeDate.getTime();
                lActualDtime = lDTime;

                // If the DTime is parsed based on the format in which seconds
                // are not specified
                // then add some random seconds value to the DTime.
                if (lDTimeFormat.indexOf(SECOND) == -1)
                {
                    if (log.isInfoEnabled())
                        log.info("As the Date Time Format " + lDTimeFormat + " does not have seconds, adding some random seconds to the DTime.");

                    final long lTempDate = addSecondToTime(lDTime.getTime());
                    lDTime = new Date(lTempDate);
                    // if no sla configured actualDtime and lDtime both should
                    // have same value in DB
                    // actualDtime = lDtime;

                    if (log.isDebugEnabled())
                        log.debug("After Adding Seconds DTime :'" + lDTime + "', ActualDTime:'" + lActualDtime + "'");
                }
            }
        }
        catch (final Exception e)
        {
            // On any exception in finding the DTime set the current time as
            // DTime
            log.error("Unable to get the DTime value. Setting current time as DTime", e);
            lDTime       = new Date();
            lActualDtime = lDTime;
        }
        return new long[]
        { lDTime.getTime(), lActualDtime.getTime() };
    }

    public static void setCarrierTime(
            DeliveryObject aDeliveryObject)
    {
        final String lCarrierDateFormat = aDeliveryObject.getCarrierDateTimeFormat();

        if (lCarrierDateFormat != null)
        {
            Date lCarrierReceivedTime = aDeliveryObject.getCarrierReceivedTime();
            // Date lCarrierReceivedTime =
            // DateTimeUtility.getDateFromString(lCarrierRcvdTime, lCarrierDateFormat);

            /*
             * if (lCarrierReceivedTime == null)
             * DateTimeUtility.getDateFromString(lCarrierRcvdTime,
             * kannelDateTimeFormats.get(lCarrierDateFormat));
             */

            if (lCarrierReceivedTime == null)
                lCarrierReceivedTime = new Date();
            aDeliveryObject.setCarrierReceivedTime(lCarrierReceivedTime);
        }
    }

    private static long addSecondToTime(
            long aTimeInLong)
    {
        final int lMaxRandomSeed = CommonUtility.getInteger(PayloadUtil.getAppConfigValueAsString(ConfigParamConstants.GLOBAL_DN_ADJUSTMENT_IN_SEC), 10);
        return addSecondToTime(aTimeInLong, lMaxRandomSeed);
    }

    private static long addSecondToTime(
            long aTimeInLong,
            int aMaxRandomSeed)
    {
        final Calendar lCal = Calendar.getInstance();
        lCal.setLenient(false);
        lCal.setTimeInMillis(aTimeInLong);
        lCal.add(Calendar.SECOND, (int) Math.ceil(Math.random() * aMaxRandomSeed));
        return lCal.getTimeInMillis();
    }

    /**
     * Method to calculate the Submission Latencies. On exception 0.0 will be
     * returned for both original and SLA.
     *
     * @param aMessage
     *
     * @return
     */

    public static long[] calculateSubmissionLatencies(
            SubmissionObject aSubmissionObject)
    {
        long[] returnValue = new long[]
        { 0, 0 };

        try
        {
            final Date lActualCarrierSubmitTime = aSubmissionObject.getActualCarrierSubmitTime();

            if (lActualCarrierSubmitTime != null)
            {
                final Date   lScheduleTime                        = aSubmissionObject.getScheduleDateTime();

                final Date   lReceivedTime                        = lScheduleTime == null ? aSubmissionObject.getMessageReceivedTime() : lScheduleTime;
                final long   lCarrierSubmitTime                   = aSubmissionObject.getCarrierSubmitTime().getTime();
                final long   lPlatformLatencyOriginalMillies      = lActualCarrierSubmitTime.getTime() - lReceivedTime.getTime();
                final long   lPlatformLatencySLAMillies           = lCarrierSubmitTime - lReceivedTime.getTime();
                final long[] lCheckMinMax                         = checkMinMax(lPlatformLatencyOriginalMillies, lPlatformLatencySLAMillies);
                final long   lFinalPlatformLatencyOriginalMillies = lCheckMinMax[0];
                final long   lFinalPlatformLatencySLAMillies      = lCheckMinMax[1];

                if (log.isInfoEnabled())
                    log.info("Received Time : '" + DateTimeUtility.getFormattedDateTime(lReceivedTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) + ", Carrier Submitime : '"
                            + DateTimeUtility.getFormattedDateTime(lCarrierSubmitTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) + ", Actual Carrier Submittime : '"
                            + DateTimeUtility.getFormattedDateTime(lActualCarrierSubmitTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) + "', platformLatencyOriginalMillies : '"
                            + lPlatformLatencyOriginalMillies + "', platformLatencySLAMillies : '" + lPlatformLatencySLAMillies + "' PF Latency Ori : '" + lFinalPlatformLatencyOriginalMillies
                            + "' PL Latency SLA : '" + lFinalPlatformLatencySLAMillies + "'");

                returnValue = new long[]
                { lFinalPlatformLatencyOriginalMillies, lFinalPlatformLatencySLAMillies };
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while calculating the platform latency. Sending back 0 as latency values", e);
        }
        return returnValue;
    }

    public static long[] calculateDeliveryLatencies(
            DeliveryObject aDeliveryObject)
    {
        long       oriOvarallLatency     = 0;
        long       slaOvarallLatency     = 0;
        long       oriDeliveryLatency    = 0;
        long       slaDeliveryLatency    = 0;

        final Date lReceivedTime         = aDeliveryObject.getMessageReceivedTime();
        final Date lActualReceivedTime   = aDeliveryObject.getMessageActualReceivedTime();
        final Date lCarrierSubTime       = aDeliveryObject.getCarrierSubmitTime();
        final Date lActualCarrierSubTime = aDeliveryObject.getActualCarrierSubmitTime();
        final Date lDeliveryTime         = aDeliveryObject.getDeliveryTime();
        final Date lActualDeliveryTime   = aDeliveryObject.getActualDeliveryTime();

        try
        {

            // Platform rejections actualDTime will be null
            if ((lActualDeliveryTime != null) && (lActualCarrierSubTime != null))
            {
                oriOvarallLatency = lActualDeliveryTime.getTime() - lActualReceivedTime.getTime();
                slaOvarallLatency = lDeliveryTime.getTime() - lReceivedTime.getTime();

                long[] lCheckMinMax = checkMinMax(oriOvarallLatency, slaOvarallLatency);
                oriOvarallLatency  = lCheckMinMax[0];
                slaOvarallLatency  = lCheckMinMax[1];

                oriDeliveryLatency = lActualDeliveryTime.getTime() - lActualCarrierSubTime.getTime();
                slaDeliveryLatency = lDeliveryTime.getTime() - lCarrierSubTime.getTime();

                lCheckMinMax       = checkMinMax(oriDeliveryLatency, slaDeliveryLatency);
                oriDeliveryLatency = lCheckMinMax[0];
                slaDeliveryLatency = lCheckMinMax[1];
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while calculating the delivery latency . Sending back 0 as latency values" + aDeliveryObject, e);
        }

        if (log.isInfoEnabled())
            log.info("Received Time : '" + DateTimeUtility.getFormattedDateTime(lReceivedTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) //
                    + ", Actual Received  Time : '" + DateTimeUtility.getFormattedDateTime(lActualReceivedTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) //
                    + ", Carrier Submit Time : '" + DateTimeUtility.getFormattedDateTime(lCarrierSubTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) //
                    + ", Actual Carrier Submit Time : '" + DateTimeUtility.getFormattedDateTime(lActualCarrierSubTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) //
                    + ", Delivered Time : '" + DateTimeUtility.getFormattedDateTime(lDeliveryTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) //
                    + ", Actual Delivered Time : '" + DateTimeUtility.getFormattedDateTime(lActualDeliveryTime, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) //
                    + "', DeliveryLatencyOriginalMillies : '" + oriDeliveryLatency //
                    + "', DeliveryLatencySLAMillies : '" + slaDeliveryLatency //
                    + "' Overall Latency Ori : '" + oriOvarallLatency //
                    + "' Overall Latency SLA : '" + slaOvarallLatency + "'");

        return new long[]
        { oriDeliveryLatency, slaDeliveryLatency, oriOvarallLatency, slaOvarallLatency };
    }

    private static long[] checkMinMax(
            long aOriginal,
            long aSla)
    {
        final long retOriginal = checkMinMax(aOriginal);
        final long retSla      = checkMinMax(aSla);
        return new long[]
        { retOriginal, retSla };
    }

    private static long checkMinMax(
            long aValue)
    {
        if (aValue < 0)
            return 0;

        if (aValue > MAX_DATABASE_ALLOWED_VALUE)
            return MAX_ALLOWED_VALUE;
        return aValue;
    }

}