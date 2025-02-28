package com.itextos.beacon.queryprocessor.commonutils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Utility
{

    public static String DATE_LOG_FILE_SUFFIX             = "yyyyMMdd_HHmmss";
    public static String DATE_DEFAULT_format              = "yyyy-MM-dd HH:mm:ss";
    public static String DATE_TIME_NO_SEP                 = "yyyyMMdd_HHmmss";
    public static String DATE_WITH_MILLI_SECONDS          = "yyyy-MM-dd HH:mm:ss:SSS";
    public static String DATE_WITH_MILLI_SECONDS_WITH_DOT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static String DATE_YYYY_MM_DD                  = "yyyy-MM-dd";
    public static String DATE_YYYY_MM                     = "yyyy-MM";
    public static String DATE_YYYYMMDD                    = "yyyyMMdd";
    public static String DATE_YYYYMM                      = "yyyyMM";
    public static String DATE_YY_MM_DD_HH_MM              = "yyMMddHHmm";
    public static String DATE_YY_MM_DD_HH_MM_SS           = "yyMMddHHmmss";

    public static HashMap<String, String> parseQueryString(
            String toParse)
    {
        final String[]                fields = toParse.split("&");
        String[]                      kv;

        final HashMap<String, String> things = new HashMap<>();

        for (final String lField : fields)
        {
            kv = lField.split("=");
            if (2 == kv.length)
                things.put(kv[0], kv[1]);
        }
        return things;
    }

    public static String nullCheck(
            Object obj,
            boolean trimIt)
    {
        return (obj == null) ? "" : (trimIt ? obj.toString().trim() : obj.toString());
    }

    public static String capitalizeFirstLetter(
            String aStrValue)
    {
        if (aStrValue == null)
            return null;
        else
            if ("".equals(aStrValue))
                return aStrValue;
            else
                if (aStrValue.length() == 1)
                    return aStrValue.toUpperCase();

        return aStrValue.substring(0, 1).toUpperCase() + aStrValue.substring(1);
    }

    public static int getInteger(
            String aIntValue)
    {
        return getInteger(aIntValue, 0);
    }

    public static int getInteger(
            String aIntValue,
            int aDefaultValue)
    {

        try
        {
            return Integer.parseInt(aIntValue);
        }
        catch (final Exception exception)
        {
            return aDefaultValue;
        }
    }

    public static long getLong(
            String aLongValue)
    {
        return getLong(aLongValue, 0L);
    }

    public static long getLong(
            String aLongValue,
            long aDefaultValue)
    {

        try
        {
            return Long.parseLong(aLongValue);
        }
        catch (final Exception exception)
        {
            return aDefaultValue;
        }
    }

    public static float getFloat(
            String aFloatValue)
    {
        return getFloat(aFloatValue, 0.0f);
    }

    public static float getFloat(
            String aFloatValue,
            float aDefaultValue)
    {

        try
        {
            return Float.parseFloat(aFloatValue);
        }
        catch (final Exception exception)
        {
            return aDefaultValue;
        }
    }

    public static double getDouble(
            String aDoubleValue)
    {
        return getDouble(aDoubleValue, 0.0D);
    }

    public static double getDouble(
            String aDoubleValue,
            double aDefaultValue)
    {

        try
        {
            return Double.parseDouble(aDoubleValue);
        }
        catch (final Exception exception)
        {
            return aDefaultValue;
        }
    }

    public static Date getDateTime(
            String aDateString)
    {
        return getDateTime(aDateString, DATE_DEFAULT_format);
    }

    public static Date getDateTime(
            String aDateString,
            String aDateFormat)
    {

        try
        {
            final SimpleDateFormat sdf = new SimpleDateFormat(aDateFormat);
            sdf.setLenient(false);
            return sdf.parse(aDateString);
        }
        catch (final Exception exception)
        {
            return null;
        }
    }

    public static LocalDate getLocalDate(
            String aDateString)
    {

        try
        {
            return getLocalDate(aDateString, DATE_DEFAULT_format);
        }
        catch (final Exception exception)
        {
            return null;
        }
    }

    public static LocalDate getLocalDate(
            String aDateString,
            String aDateFormat)
    {

        try
        {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(aDateFormat);
            return LocalDate.parse(aDateString, formatter);
        }
        catch (final Exception exception)
        {
            return null;
        }
    }

    public static LocalDateTime getLocalDateTime(
            String aDateString)
    {

        try
        {
            return getLocalDateTime(aDateString, DATE_DEFAULT_format);
        }
        catch (final Exception exception)
        {
            return null;
        }
    }

    public static LocalDateTime getLocalDateTime(
            String aDateString,
            String aDateFormat)
    {

        try
        {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(aDateFormat);
            return LocalDateTime.parse(aDateString, formatter);
        }
        catch (final Exception exception)
        {
            return null;
        }
    }

    public static String formatDateTime(
            Date aDate)
    {
        return formatDateTime(aDate, DATE_DEFAULT_format);
    }

    public static String formatDateTime(
            Date aDate,
            String aDateFormat)
    {

        try
        {
            final SimpleDateFormat sdf = new SimpleDateFormat(aDateFormat);
            sdf.setLenient(false);
            return sdf.format(aDate.getTime());
        }
        catch (final Exception exception)
        {
            return null;
        }
    }

    public static String formatDateTime(
            LocalDateTime aDate)
    {
        return formatDateTime(aDate, DATE_DEFAULT_format);
    }

    public static String formatDateTime(
            LocalDate aDate)
    {
        return formatDateTime(aDate.atStartOfDay(), DATE_YYYY_MM_DD);
    }

    public static String formatDateTime(
            LocalDate aDate,
            String aDateFormat)
    {
        return formatDateTime(aDate.atStartOfDay(), aDateFormat);
    }

    public static String formatDateTime(
            LocalDateTime aDate,
            String aDateFormat)
    {

        try
        {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(aDateFormat);

            return aDate.format(formatter);
        }
        catch (final Exception exception)
        {
            return null;
        }
    }

    public static String getCSV(
            List<Long> client_ids)
    {
        final StringBuilder str = new StringBuilder("");

        for (final Long eachstring : client_ids)
            str.append(eachstring).append(",");

        return str.toString().substring(0, str.length() - 1);
    }

    public static String getApplicationServerIp()
    {

        try
        {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (final UnknownHostException e)
        {
            return "unknown";
        }
    }

}
