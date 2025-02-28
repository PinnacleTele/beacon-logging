package com.itextos.beacon.smpp.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SmppDateHandler
{

    private final static Log    log                            = LogFactory.getLog(SmppDateHandler.class);

    private static final String DATE_FORMAT                    = "yyMMddHHmmss";
    private static final String OPERATION_PLUS                 = "+";
    private static final String OPERATION_MINUS                = "-";
    private static final String OPERATION_RELATIVE             = "R";
    private static final int    ONE_QUARTER_OF_HOUR_IN_MINUTES = 15;
    private static final int    FIVE_HOURS_30_MINUTES          = (5 * 4 * ONE_QUARTER_OF_HOUR_IN_MINUTES) + (2 * ONE_QUARTER_OF_HOUR_IN_MINUTES);

    private final String        mSmppDateString;

    private String              mDateString;
    private Date                mParsedDate;
    private int                 mOneTenthOfSec;
    private int                 mNQuarter;
    private String              mOperation;

    public SmppDateHandler(
            String aSmppDateString)
    {
        mSmppDateString = aSmppDateString;
    }

    private boolean validate()
    {
        boolean returnValue = true;

        try
        {
            if ((mSmppDateString == null) || (mSmppDateString.length() < 16))
                throw new RuntimeException("Invalid date passed.");

            mDateString    = mSmppDateString.substring(0, 12);
            mOneTenthOfSec = Integer.parseInt(mSmppDateString.substring(12, 13));
            mNQuarter      = Integer.parseInt(mSmppDateString.substring(13, 15));
            mOperation     = mSmppDateString.substring(15, 16);

            if (!OPERATION_RELATIVE.equalsIgnoreCase(mOperation))
            {
                final SimpleDateFormat sdfSMPP = new SimpleDateFormat(DATE_FORMAT);
                sdfSMPP.setLenient(false);
                mParsedDate = sdfSMPP.parse(mDateString);
            }

            if (log.isDebugEnabled())
            {
                log.debug("Date String           : '" + mDateString + "'");
                log.debug("Parsed Date           : '" + mParsedDate + "'");
                log.debug("One Tengt of Sec      : '" + mOneTenthOfSec + "'");
                log.debug("No of Quarter of Hour : '" + mNQuarter + "'");
                log.debug("Relative Operation    : '" + mOperation + "'");
            }
        }
        catch (final Exception e)
        {
            returnValue = false;
            log.error("Exception while validating the SMPP date passed. SMPP Date : '" + mSmppDateString + "'", e);
        }
        return returnValue;
    }

    public Date getScheduledTime()
    {
        final boolean isValid = validate();

        if (isValid)
        {
            int            minutesToAdd = 0;
            final Calendar calendar     = Calendar.getInstance();
            calendar.setLenient(false);

            switch (mOperation)
            {
                case OPERATION_PLUS:
                    minutesToAdd = FIVE_HOURS_30_MINUTES - (mNQuarter * ONE_QUARTER_OF_HOUR_IN_MINUTES);
                    calendar.setTime(mParsedDate);
                    calendar.add(Calendar.MINUTE, minutesToAdd);

                    if (log.isDebugEnabled())
                    {
                        log.debug("Minutes to add        : '" + minutesToAdd + "'");
                        log.debug("Schedule Time         : '" + calendar.getTime() + "'");
                    }
                    break;

                case OPERATION_MINUS:
                    minutesToAdd = FIVE_HOURS_30_MINUTES + (mNQuarter * ONE_QUARTER_OF_HOUR_IN_MINUTES);
                    calendar.setTime(mParsedDate);
                    calendar.add(Calendar.MINUTE, minutesToAdd);
                    if (log.isDebugEnabled())
                    {
                        log.debug("Minutes to add        : '" + minutesToAdd + "'");
                        log.debug("Schedule Time         : '" + calendar.getTime() + "'");
                    }

                    break;

                case OPERATION_RELATIVE:
                    final int years = Integer.parseInt(mDateString.substring(0, 2));
                    final int months = Integer.parseInt(mDateString.substring(2, 4));
                    final int days = Integer.parseInt(mDateString.substring(4, 6));
                    final int hours = Integer.parseInt(mDateString.substring(6, 8));
                    final int minutes = Integer.parseInt(mDateString.substring(8, 10));
                    final int seconds = Integer.parseInt(mDateString.substring(10, 12));

                    calendar.add(Calendar.YEAR, years);
                    calendar.add(Calendar.MONTH, months);
                    calendar.add(Calendar.DATE, days);
                    calendar.add(Calendar.HOUR, hours);
                    calendar.add(Calendar.MINUTE, minutes);
                    calendar.add(Calendar.SECOND, seconds);

                    if (log.isDebugEnabled())
                    {
                        log.debug("Years to add       : '" + years + "'");
                        log.debug("Hours to add       : '" + months + "'");
                        log.debug("Days to add        : '" + days + "'");
                        log.debug("Hours to add       : '" + hours + "'");
                        log.debug("Minutes to add     : '" + minutes + "'");
                        log.debug("secondsto add      : '" + seconds + "'");
                        log.debug("Schedule Time      : '" + calendar.getTime() + "'");
                    }

                    break;

                default:
                    throw new RuntimeException("Invalid Operation specified. Operation : '" + mOperation + "'");
            }

            return calendar.getTime();
        }
        return null;
    }

    public static void main(
            String[] args)
    {
        final String          s             = "000000020000000R";
        final SmppDateHandler sdh           = new SmppDateHandler(s);

        final Date            scheduledDate = sdh.getScheduledTime();
        System.out.println(scheduledDate.getTime());
    }

}
