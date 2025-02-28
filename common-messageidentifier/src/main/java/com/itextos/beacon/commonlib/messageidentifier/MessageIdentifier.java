package com.itextos.beacon.commonlib.messageidentifier;

import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler2;

public class MessageIdentifier
{

    private static final Log           log                 = LogFactory.getLog(MessageIdentifier.class);

    private static final String        FIXED_LAST_DIGITS   = "00";
    private static final int           MAX_S2_INDEX           = 99;
    private static final int           MAX_S3_INDEX           = 999;

    private static final int           MONTHS_IN_A_YEAR    = 12;
    private static final int           HOURS_IN_A_DAY      = 24;
    private static final int           NO_OF_YEARS         = 8;


    // Used to format the combined year & month
    private static final DecimalFormat DECIMAL_FORMATTER_2 = new DecimalFormat("00");

    private static final Random RANDOM = new Random();
    
    private static final DecimalFormat DECIMAL_FORMATTER_8 = new DecimalFormat("00000000");

    // Used to format the combined date & hour
    private static final DecimalFormat DECIMAL_FORMATTER_3 = new DecimalFormat("000");

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final MessageIdentifier INSTANCE = new MessageIdentifier();

    }

    public static MessageIdentifier getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private String                   mLocalIP               = null;
    private boolean                  mIsAppInstanceIDSet    = false;
    private int                      ms2Index          = 10;
    private int                      ms3Index          = 100;

    private InterfaceType            mInterfaceType;
    private String                   mAppInstanceId         = null;
    private RedisAppInstnaceIDPool   mAppInstanceRedis      = null;
    private MessageIdentifierUpdater mInstanceStatusUpdater = null;

    private final Date               mDate                  = new Date();
    private final SimpleDateFormat   mIdDateFormat          = new SimpleDateFormat("yyMMddHHmmss");

    private MessageIdentifier() 
    {
        try {
			getIPAddress();
		} catch (ItextosRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        mIdDateFormat.setLenient(false);
    }

    public void init(
            final InterfaceType aInterfaceType) throws ItextosRuntimeException
    {
        mInterfaceType = aInterfaceType;

        if (!mIsAppInstanceIDSet)
        {
            final boolean validInterfaceType = MessageIdentifierProperties.getInstance().isValidInterfaceType(mInterfaceType);

            if (!validInterfaceType)
            {
                final String message = "Interface Type is not specified in properties. Interface Type : '" + mInterfaceType + "' (" + mInterfaceType.getKey() + ").";
                log.error(message, new ItextosRuntimeException(message));
                throw new ItextosRuntimeException(message);
            }

            mAppInstanceRedis = new RedisAppInstnaceIDPool(mInterfaceType);
            mAppInstanceId    = mAppInstanceRedis.getNextAppInstanceID();

            if (mAppInstanceId == null)
            {
                final String message = "ERROR " + "::::" //
                        + " Unable to get an appInstanceID from data source for the interface type '" + mInterfaceType //
                        + "'. Cannot proceed further without a proper appInstanceID."//
                        + " Check the configuration in property file and in '"//
                        + mAppInstanceRedis.getMessageIdentifierSourceType() + "'";

                System.err.println(new Date() + " ::: " + message);
                log.error(message);

                throw new ItextosRuntimeException(message);
            }

            log.warn("AppInstance ID set to '" + mAppInstanceId + "' for the interface type '" + mInterfaceType + "' in IP '" + mLocalIP + "'");

            mIsAppInstanceIDSet = true;
            startUpdaterThread();
        }
        else
        {
            final String message = "Message Identifier is already initialized for the Interface Type : '" + mInterfaceType + "' and assigned AppInstanceID is : '" + mAppInstanceId + "'";
 //           log.error(message, new ItextosRuntimeException(message));
         //   throw new ItextosRuntimeException(message);
        }
    }

    private void getIPAddress() throws ItextosRuntimeException
    {

        try
        {
            mLocalIP = InetAddress.getLocalHost().getHostAddress();
        }
        catch (final Exception e)
        {
            final String message = "Unable to identify the IP of the machine in which this application is running.";
            log.error(message, e);
            throw new ItextosRuntimeException(message, e);
        }
    }

    private void startUpdaterThread()
    {
        mInstanceStatusUpdater = new MessageIdentifierUpdater(mAppInstanceRedis);
   
        
        ExecutorSheduler2.getInstance().addTask(mInstanceStatusUpdater,"MessageIdentifier-" + mInterfaceType + "-" + mAppInstanceRedis.getCurrentAppInstanceID());
   
        if (log.isDebugEnabled())
            log.debug("Message Identifier Status updater thread started for the interface type :'" + mInterfaceType + "' and Instance ID : '" + mAppInstanceRedis.getCurrentAppInstanceID() + "'");
   
    }

    /**
     * @return String generated MessageIdentifier.
     */
    public synchronized String getNextId()
    {

    	/*
        if (!mIsAppInstanceIDSet)
        {
            final String message = "WARNING ::: " + "APP INSTANCE ID is not. " //
                    + "You need to initialize Message Identifier by calling " //
                    + "'MessageIdentifier.getInstance().init(<interfacetype>)' " //
                    + "before invoking " + "'MessageIdentifier.getInstance().getNextId()' method. " //
                    + "Interface Type : '" + mInterfaceType + "' in IP '" + mLocalIP + "' ";

            log.error(message, new RuntimeException(message));
            System.err.println(new Date() + " ::: " + message);
            System.exit(-99);
        }
        mDate.setTime(DateTimeUtility.getCurrentTimeInMillis());
        return mAppInstanceId + mIdDateFormat.format(mDate) + getNextIndex() + FIXED_LAST_DIGITS;
    	*/
    	return getNextSmallId();
    }

    public synchronized String getNextSmallId()
    {

        if (!mIsAppInstanceIDSet)
        {
            final String message = "WARNING ::: " + "APP INSTANCE ID is not. " //
                    + "You need to initialize Message Identifier by calling " //
                    + "'MessageIdentifier.getInstance().init(<interfacetype>)' " //
                    + "before invoking " + "'MessageIdentifier.getInstance().getNextId()' method. " //
                    + "Interface Type : '" + mInterfaceType + "' in IP '" + mLocalIP + "' ";

            log.error(message, new RuntimeException(message));
            System.err.println(new Date() + " ::: " + message);
            System.exit(-99);
        }
     //   mDate.setTime(DateTimeUtility.getCurrentTimeInMillis());
 //       return mAppInstanceId + getUniqueDateWithMillis() + getNextIndex() + FIXED_LAST_DIGITS;
        return mAppInstanceId + getUniqueDateWithMillis() + getNextIndex() +(RANDOM.nextInt(80) + 10)+ FIXED_LAST_DIGITS;
        
    }

    public synchronized String getUniqueSequence()
    {

        if (!mIsAppInstanceIDSet)
        {
            final String message = "WARNING ::: " + "APP INSTANCE ID is not. " //
                    + "You need to initialize Message Identifier by calling " //
                    + "'MessageIdentifier.getInstance().init(<interfacetype>)' " //
                    + "before invoking " + "'MessageIdentifier.getInstance().getNextId()' method. " //
                    + "Interface Type : '" + mInterfaceType + "' in IP '" + mLocalIP + "' ";

            log.error(message, new RuntimeException(message));
            System.err.println(new Date() + " ::: " + message);
            System.exit(-99);
        }
        mDate.setTime(DateTimeUtility.getCurrentTimeInMillis());
        return mAppInstanceId + getUniqueDate() + getNextIndex();
    }
    
    /**
     * <b>First two digits</b> - Get the last two digits of the year and get the
     * modulus based on 8.
     * Multiply the value with the number of months in a Year (12). Add the current
     * month to this value.
     * Refer {@link Calendar#MONTH} for the value as it runs from 0 to 12.
     * <p>
     * <b>Last three digits</b> - Get current date - 1 and multiply with hours in a
     * day (24) then add the hour of the day.
     * Refer {@link Calendar#HOUR_OF_DAY} for the values. <b>Don't use
     * {@link Calendar#HOUR}</b>.
     *
     * @return a unique String with 5 characters which represents the current year,
     *         month, date and hour.
     */
    private static String getUniqueDate()
    {
        final Calendar cal = Calendar.getInstance();
        cal.setLenient(false);

        // Used to format the date with minutes and seconds.
        final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("mmss");
        // Add 1 as Calendar.MONTH will return 0 to 11.
        final int              ym            = ((cal.get(Calendar.YEAR) % NO_OF_YEARS) * MONTHS_IN_A_YEAR) + (cal.get(Calendar.MONTH) + 1);
        final int              dhh           = ((cal.get(Calendar.DATE) - 1) * HOURS_IN_A_DAY) + cal.get(Calendar.HOUR_OF_DAY);
        return DECIMAL_FORMATTER_2.format(ym) + DECIMAL_FORMATTER_3.format(dhh) + DATE_FORMATER.format(cal.getTime());
    }

    private static String getUniqueDateWithMillis()
    {
        final Calendar cal = Calendar.getInstance();
        cal.setLenient(false);

        // Used to format the date with minutes and seconds.
        final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("mmssSSS");
        // Add 1 as Calendar.MONTH will return 0 to 11.
        final int              ym            = ((cal.get(Calendar.YEAR) % NO_OF_YEARS) * MONTHS_IN_A_YEAR) + (cal.get(Calendar.MONTH) + 1);
        final int              dhh           = ((cal.get(Calendar.DATE) - 1) * HOURS_IN_A_DAY) + cal.get(Calendar.HOUR_OF_DAY);
        return DECIMAL_FORMATTER_2.format(ym) + DECIMAL_FORMATTER_3.format(dhh) + DATE_FORMATER.format(cal.getTime());
    }
    
    
    private static String getUniqueDateWithNanoSecond()
    {
        final Calendar cal = Calendar.getInstance();
        cal.setLenient(false);

        // Used to format the date with minutes and seconds.
        final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("SSSSSS");
        int dayoftheyear=cal.get(Calendar.DAY_OF_YEAR)+1;
        int hour=cal.get(Calendar.HOUR_OF_DAY)+1;
        int minute=cal.get(Calendar.MINUTE)+1;
        int second=cal.get(Calendar.SECOND)+1;
   
        int day=dayoftheyear*hour*minute*second;
        
        return DECIMAL_FORMATTER_8.format(day) + DATE_FORMATER.format(cal.getTime());
    }
    
    private synchronized String getNextIndex()
    {

       
        return getS2()+""+getS3();
    }

    private synchronized int getS2() {
    	
    	 if (ms2Index >= MAX_S2_INDEX)
         {
    		 ms2Index = 10;

   
         }
         else {
             ++ms2Index;
         }
         return ms2Index;

    	
    }
    
    private synchronized int getS3() {
    	
   	 if (ms3Index >= MAX_S3_INDEX)
        {
   		 ms3Index = 100;

  
        }
        else {
            ++ms3Index;
        }
        return ms3Index;

   	
   }
    /**
     * This method should be called in all the interface instances to reset the
     * instance id.
     */
    public void resetMessageIdentifier()
    {
        log.warn("Resetting the status to ready for the interface type :'" + mInterfaceType //
                + "' and Instance ID : '" + mAppInstanceRedis.getCurrentAppInstanceID() + "'");

        if (mInstanceStatusUpdater != null)
            mInstanceStatusUpdater.stopMe();

        mAppInstanceRedis.resetAppInstanceID();
    }

    public String getAppInstanceId()
    {
        return mAppInstanceId;
    }

}