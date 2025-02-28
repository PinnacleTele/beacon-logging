package com.itextos.beacon.commonlib.timezoneutility;

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

public abstract class TimeZoneUtility
{

    private static final Log                     log                               = LogFactory.getLog(TimeZoneUtility.class);

    private static final String                  PROPERTY_FILE_LOCATION            = "timezone.file.location";
    private static final String                  PROPERTY_KEY_TIMEZONE             = "platform.timezone";
    private static final String                  PROPERTY_KEY_TIMEZONE_OFFSET      = "platform.timezone.offset";
    private static final String                  PROPERTY_KEY_DATABASE             = "platform.database";
    private static final String                  DEFAULT_PLATFORM_TIME_ZONE_IST    = "Asia/Kolkata";
    private static final String                  DEFAULT_PLATFORM_TIME_ZONE_OFFSET = "+05:30";

    private static final String                  DEFAULT_PLATFORM_DATABASE         = "MYSQL";
    private static final String                  DATABAE_TYPE_MYSQL                = "MYSQL";
    private static final String                  DATABAE_TYPE_ORACLE               = "ORACLE";

    private static final PropertiesConfiguration propConf                          = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.TIME_ZONE_PROPERTIES, false);

    // private static final String TIMEZONE_OFFSET =
    // propConf.getString(PROPERTY_KEY_TIMEZONE_OFFSET,
    // DEFAULT_PLATFORM_TIME_ZONE_OFFSET);
    private static final String                  DATABASE                          = propConf.getString(PROPERTY_KEY_DATABASE, DEFAULT_PLATFORM_DATABASE);
    private static final String                  TEMP_TIME_ZONE                    = propConf.getString(PROPERTY_KEY_TIMEZONE, DEFAULT_PLATFORM_TIME_ZONE_IST);
    private static final TimeZone                PLATFORM_TIMEZONE                 = TimeZone.getTimeZone(TEMP_TIME_ZONE);

    private TimeZoneUtility()
    {}

    public static TimeZone getTimeZone(
            String aFromTimeZoneId)
    {
        return TimeZone.getTimeZone(aFromTimeZoneId);
    }

    public static Date getDateBasedOnTimeZone(
            String aDateString)
    {
        return getDateBasedOnTimeZone(aDateString, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    }

    public static Date getDateBasedOnTimeZone(
            String aDateString,
            DateTimeFormat aDateTimeFormat)
    {
        return getDateBasedOnTimeZone(aDateString, aDateTimeFormat, PLATFORM_TIMEZONE);
    }

    public static Date getDateBasedOnTimeZone(
            String aDateString,
            TimeZone aFromTimeZone)
    {
        return getDateBasedOnTimeZone(aDateString, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS, aFromTimeZone);
    }

    public static Date getDateBasedOnTimeZone(
            String aDateString,
            DateTimeFormat aDateTimeFormat,
            String aFromTimeZoneId)
    {
        return getDateBasedOnTimeZone(aDateString, aDateTimeFormat, TimeZone.getTimeZone(aFromTimeZoneId));
    }

    public static Date getDateBasedOnTimeZone(
            String aDateString,
            DateTimeFormat aDateTimeFormat,
            TimeZone aFromTimeZone)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("DateInString : '" + aDateString + "', DateFormat : '" + aDateTimeFormat + "', Timezone : '" + aFromTimeZone.getDisplayName() + "'");

            return DateTimeUtility.getDateFromString(aDateString, aDateTimeFormat, aFromTimeZone);
        }
        catch (final Exception e)
        {
            log.error("Exception while parsing the Date. DateInString : '" + aDateString + "', DateFormat : '" + aDateTimeFormat + "', Timezone : '" + aFromTimeZone.getDisplayName() + "'", e);
        }
        return null;
    }

    public static String getDateStringBasedOnTimeZone(
            Date aDate)
    {
        return getDateStringBasedOnTimeZone(aDate, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    }

    public static String getDateStringBasedOnTimeZone(
            Date aDate,
            DateTimeFormat aDateTimeFormat)
    {
        return getDateStringBasedOnTimeZone(aDate, aDateTimeFormat, PLATFORM_TIMEZONE);
    }

    public static String getDateStringBasedOnTimeZone(
            Date aDate,
            TimeZone aToTimeZone)
    {
        return getDateStringBasedOnTimeZone(aDate, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS, aToTimeZone);
    }

    public static String getDateStringBasedOnTimeZone(
            Date aDate,
            DateTimeFormat aDateTimeFormat,
            String aToTimeZone)
    {
        return getDateStringBasedOnTimeZone(aDate, aDateTimeFormat, TimeZone.getTimeZone(aToTimeZone));
    }

    public static String getDateStringBasedOnTimeZone(
            Date aDate,
            DateTimeFormat aDateTimeFormat,
            TimeZone aToTimeZone)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Date : '" + aDate + "', DateFormat : '" + aDateTimeFormat + "', Timezone : '" + aToTimeZone.getDisplayName() + "'");

            return DateTimeUtility.getFormattedDateTime(aDate, aDateTimeFormat, aToTimeZone);
        }
        catch (final Exception e)
        {
            log.error("Exception while parsing the Date. Date : '" + aDate + "', DateFormat : '" + aDateTimeFormat + "', Timezone : '" + aToTimeZone.getDisplayName() + "'", e);
        }
        return null;
    }

    @Deprecated
    public static String getDateStringBasedOnTimeZone(
            Date aDate,
            String aDateTimeFormat)
    {
        return getDateStringBasedOnTimeZone(aDate, aDateTimeFormat, PLATFORM_TIMEZONE);
    }

    @Deprecated
    public static String getDateStringBasedOnTimeZone(
            Date aDate,
            String aDateTimeFormat,
            String aToTimeZone)
    {
        return getDateStringBasedOnTimeZone(aDate, aDateTimeFormat, TimeZone.getTimeZone(aToTimeZone));
    }

    @Deprecated
    public static String getDateStringBasedOnTimeZone(
            Date aDate,
            String aDateTimeFormat,
            TimeZone aToTimeZone)
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Date : '" + aDate + "', DateFormat : '" + aDateTimeFormat + "', Timezone : '" + aToTimeZone.getDisplayName() + "'");

            return DateTimeUtility.getFormattedDateTime(aDate, aDateTimeFormat, aToTimeZone);
        }
        catch (final Exception e)
        {
            log.error("Exception while parsing the Date. Date : '" + aDate + "', DateFormat : '" + aDateTimeFormat + "', Timezone : '" + aToTimeZone.getDisplayName() + "'", e);
        }
        return null;
    }

    // public static Date getDateBasedOnTimeZoneOffset(
    // String aDateString)
    // {
    // return getDateBasedOnTimeZoneOffset(aDateString,
    // DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    // }

    // public static Date getDateBasedOnTimeZoneOffset(
    // String aDateString,
    // DateTimeFormat aDateTimeFormat)
    // {
    // return getDateBasedOnTimeZoneOffset(aDateString, aDateTimeFormat,
    // TIMEZONE_OFFSET);
    // }

    // public static Date getDateBasedOnTimeZoneOffset(
    // String aDateString,
    // TimeZone aFromTimeZone)
    // {
    // return getDateBasedOnTimeZoneOffset(aDateString,
    // DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS, aFromTimeZone);
    // }
    //
    // public static Date getDateBasedOnTimeZoneOffset(
    // String aDateString,
    // DateTimeFormat aDateTimeFormat,
    // String aFromTimeZoneOffset)
    // {
    // String lFromTimeZoneOffset = aFromTimeZoneOffset;
    //
    // if (log.isDebugEnabled())
    // log.debug("From Time Zone String : '" + lFromTimeZoneOffset + "'");
    //
    // if (!aFromTimeZoneOffset.startsWith("GMT"))
    // lFromTimeZoneOffset = "GMT" + aFromTimeZoneOffset;
    //
    // return getDateBasedOnTimeZoneOffset(aDateString, aDateTimeFormat,
    // TimeZone.getTimeZone(lFromTimeZoneOffset));
    // }
    //
    // public static Date getDateBasedOnTimeZoneOffset(
    // String aDateString,
    // DateTimeFormat aDateTimeFormat,
    // TimeZone aFromTimeZone)
    // {
    //
    // try
    // {
    // if (log.isDebugEnabled())
    // log.debug("DateInString : '" + aDateString + "', DateFormat : '" +
    // aDateTimeFormat + "', TimeZoneOffset : '" + aFromTimeZone.getDisplayName() +
    // "'");
    //
    // return DateTimeUtility.getDateFromString(aDateString, aDateTimeFormat,
    // aFromTimeZone);
    // }
    // catch (final Exception e)
    // {
    // log.error("Exception while parsing the Date. DateInString : '" + aDateString
    // + "', DateFormat : '" + aDateTimeFormat + "', TimeZoneOffset : '" +
    // aFromTimeZone.getDisplayName() + "'", e);
    // }
    // return null;
    // }
    //
    // public static String getDateStringBasedOnTimeZoneOffset(
    // Date aDate)
    // {
    // return getDateStringBasedOnTimeZoneOffset(aDate,
    // DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    // }
    //
    // public static String getDateStringBasedOnTimeZoneOffset(
    // Date aDate,
    // DateTimeFormat aDateTimeFormat)
    // {
    // return getDateStringBasedOnTimeZoneOffset(aDate, aDateTimeFormat,
    // TIMEZONE_OFFSET);
    // }
    //
    // public static String getDateStringBasedOnTimeZoneOffset(
    // Date aDate,
    // TimeZone aToTimeZone)
    // {
    // return getDateStringBasedOnTimeZoneOffset(aDate,
    // DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS, aToTimeZone);
    // }
    //
    // public static String getDateStringBasedOnTimeZoneOffset(
    // Date aDate,
    // DateTimeFormat aDateTimeFormat,
    // String aToTimeZoneOffset)
    // {
    // String lToTimeZoneOffset = aToTimeZoneOffset;
    //
    // if (log.isDebugEnabled())
    // log.debug("To Time Zone String : '" + lToTimeZoneOffset + "'");
    //
    // if (!aToTimeZoneOffset.startsWith("GMT"))
    // lToTimeZoneOffset = "GMT" + aToTimeZoneOffset;
    // return getDateStringBasedOnTimeZoneOffset(aDate, aDateTimeFormat,
    // TimeZone.getTimeZone(lToTimeZoneOffset));
    // }
    //
    // public static String getDateStringBasedOnTimeZoneOffset(
    // Date aDate,
    // DateTimeFormat aDateTimeFormat,
    // TimeZone aToTimeZone)
    // {
    //
    // try
    // {
    // if (log.isDebugEnabled())
    // log.debug("Date : '" + aDate + "', DateFormat : '" + aDateTimeFormat + "',
    // TimeZoneOffset : '" + aToTimeZone.getDisplayName() + "'");
    //
    // return DateTimeUtility.getFormattedDateTime(aDate, aDateTimeFormat,
    // aToTimeZone);
    // }
    // catch (final Exception e)
    // {
    // log.error("Exception while parsing the Date. Date : '" + aDate + "',
    // DateFormat : '" + aDateTimeFormat + "', TimeZoneOffset : '" +
    // aToTimeZone.getDisplayName() + "'", e);
    // }
    // return null;
    // }

    public static String getDBTimeZoneConversion(
            String aColumnName,
            String aFromTimeZoneOffset,
            String aToTimeZoneOffset)
    {

        switch (DATABASE)
        {
            case DATABAE_TYPE_MYSQL:
                return new StringBuffer("CONVERT_TZ (").append(aColumnName).append(",'").append(aFromTimeZoneOffset).append("','").append(aToTimeZoneOffset).append("') ").toString();

            case DATABAE_TYPE_ORACLE:
                return new StringBuffer("FROM_TZ (").append(aColumnName).append(",'").append(aFromTimeZoneOffset).append("') at timezone '").append(aToTimeZoneOffset).append("' ").toString();

            default:
                return new StringBuffer("CONVERT_TZ (").append(aColumnName).append(",'").append(aFromTimeZoneOffset).append("','").append(aToTimeZoneOffset).append("') ").toString();
        }
    }

}
