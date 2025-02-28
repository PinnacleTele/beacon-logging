package com.itextos.beacon.commonlib.utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;

public final class DateTimeUtility
{

    private static final Log log              = LogFactory.getLog(DateTimeUtility.class);

    public static final long ONE_MILLI_SECOND = 1;
    public static final long ONE_SECOND       = 1000 * ONE_MILLI_SECOND;
    public static final long ONE_MINUTE       = 60 * ONE_SECOND;
    public static final long ONE_HOUR         = 60 * ONE_MINUTE;
    public static final long ONE_DAY          = 60 * ONE_HOUR;

    private DateTimeUtility()
    {}

    public static Date getDateFromString(
            String aDateString,
            DateTimeFormat aInputDateFormat)
    {
        return getDateFromString(aDateString, aInputDateFormat, null);
    }

    public static Date getDateFromString(
            String aDateString,
            DateTimeFormat aInputDateFormat,
            TimeZone aToTimeZone)
    {
        Date returnValue = null;

        try
        {
            final SimpleDateFormat sdf = new SimpleDateFormat(aInputDateFormat.getFormat());
            sdf.setLenient(false);

            if (aToTimeZone != null)
                sdf.setTimeZone(aToTimeZone);
            returnValue = sdf.parse(aDateString);
        }
        catch (final Exception e)
        {
            if (log.isWarnEnabled())
                log.warn("Exception while parsing the date string '" + aDateString + "' with the format '" + aInputDateFormat + "'", e);
       
            try {
            	
            	returnValue=new Date(Long.parseLong(aDateString));
            	
            }catch(final Exception e1) {
            	
            	
            }
        }

        return returnValue;
    }

    @Deprecated
    public static Date getDateFromString(
            String aDateString,
            String aInputDateFormat)
    {
        Date returnValue = null;

        try
        {
            final SimpleDateFormat sdf = new SimpleDateFormat(aInputDateFormat);
            sdf.setLenient(false);
            returnValue = sdf.parse(aDateString);
        }
        catch (final Exception e)
        {
            if (log.isWarnEnabled())
                log.warn("Exception while parsing the date string '" + aDateString + "' with the format '" + aInputDateFormat + "'", e);
        }

        return returnValue;
    }

    public static String getFormattedCurrentDateTime(
            DateTimeFormat aOutputDateFormat)
    {
        return getFormattedDateTime(new Date(), aOutputDateFormat);
    }

    public static String getFormattedDateTime(
            long aDateAsLong,
            DateTimeFormat aOutputDateFormat)
    {
        return getFormattedDateTime(new Date(aDateAsLong), aOutputDateFormat);
    }

    public static String getFormattedDateTime(
            Date aDate,
            DateTimeFormat aOutputDateFormat)
    {
        return getFormattedDateTime(aDate, aOutputDateFormat, null);
    }

    public static String getFormattedDateTime(
            Date aDate,
            DateTimeFormat aOutputDateFormat,
            TimeZone aToTimeZone)
    {
        String returnValue = null;

        try
        {
            final SimpleDateFormat sdf = new SimpleDateFormat(aOutputDateFormat.getFormat());
            sdf.setLenient(false);

            if (aToTimeZone != null)
                sdf.setTimeZone(aToTimeZone);

            returnValue = sdf.format(aDate);
        }
        catch (final Exception e)
        {
        	/*
            if (log.isWarnEnabled())
                log.warn("Exception while converting date '" + aDate + "' to String in the format '" + aOutputDateFormat + "'", e);
        	*/
        }
        return returnValue;
    }

    @Deprecated
    public static String getFormattedDateTime(
            Date aDate,
            String aOutputDateFormat,
            TimeZone aToTimeZone)
    {
        String returnValue = null;

        try
        {
            final SimpleDateFormat sdf = new SimpleDateFormat(aOutputDateFormat);
            sdf.setLenient(false);

            if (aToTimeZone != null)
                sdf.setTimeZone(aToTimeZone);

            returnValue = sdf.format(aDate);
        }
        catch (final Exception e)
        {
            if (log.isWarnEnabled())
                log.warn("Exception while converting date '" + aDate + "' to String in the format '" + aOutputDateFormat + "'", e);
        }
        return returnValue;
    }

    @Deprecated
    public static String getFormattedDateTime(
            Date aDate,
            String aOutputDateFormat)
    {
        String returnValue = null;

        try
        {
        	if(aOutputDateFormat.equalsIgnoreCase("e")) {
         
        		returnValue=""+(aDate.getTime()/1000);
        		
        	}else {
        		
        		   final SimpleDateFormat sdf = new SimpleDateFormat(aOutputDateFormat);
                   sdf.setLenient(false);
                   returnValue = sdf.format(aDate);
        	}
        }
        catch (final Exception e)
        {
            if (log.isWarnEnabled())
                log.warn("Exception while converting date '" + aDate + "' to String in the format '" + aOutputDateFormat + "'", e);
        }
        return returnValue;
    }

    public static long getCurrentTimeInNanos()
    {
        return System.nanoTime();
    }

    public static long getCurrentTimeInMillis()
    {
        return System.currentTimeMillis();
    }

    public static double getMillisFromNanoSecond(
            long aNanotime)
    {
        return aNanotime / (ONE_MILLI_SECOND * 1d);
    }

    public static double getSecondsFromNanoSecond(
            long aNanotime)
    {
        return aNanotime / (ONE_SECOND * 1d);
    }

    public static double getTimeDifferenceInMillisFromNanoSecond(
            long aStartTime)
    {
        return getTimeDifferenceInMillisFromNanoSecond(aStartTime, getCurrentTimeInNanos());
    }

    public static double getTimeDifferenceInMillisFromNanoSecond(
            long aStartTime,
            long aEndTime)
    {
        return getMillisFromNanoSecond(aEndTime - aStartTime);
    }

    public static double getTimeDifferenceInSecondsFromNanoSecond(
            long aStartTime)
    {
        return getTimeDifferenceInSecondsFromNanoSecond(aStartTime, getCurrentTimeInNanos());
    }

    public static double getTimeDifferenceInSecondsFromNanoSecond(
            long aStartTime,
            long aEndTime)
    {
        return getSecondsFromNanoSecond(aEndTime - aStartTime);
    }

    public static Date getCurrentDateWithoutTime()
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.setLenient(false);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

}