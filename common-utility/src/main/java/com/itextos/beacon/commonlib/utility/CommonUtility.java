package com.itextos.beacon.commonlib.utility;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;

public final class CommonUtility
{

    private static final Log                      log                             = LogFactory.getLog(CommonUtility.class);
    private static final String                   POSSIBLE_ENABLED                = "1YESTRUE";
    private static final String                   URL_CHARSET_UTF_8               = "UTF-8";
    private static final String                   URL_CHARSET_DEFAULT             = URL_CHARSET_UTF_8;
    private static final List<MiddlewareConstant> ACCOUNTS_MIDDILEWARE_CONST_LIST = getRedisAccountsRequiredFields();
    private static final int                      DEFAULT_MESSAGE_ID_LENGTH       = 22;
    private static final String                   UNKNOWN_PROCESS_ID              = "-999999";
    private static final String                   PROP_LINE_SEPARATOR             = System.getProperty("line.separator");
    private static final String                   JVM_PROCESS_ID                  = getProcessId();

    public static final char                      DEFAULT_CONCAT_CHAR             = '~';
    public static final String                    DOT_STAR                        = ".*";
    public static final String                    ANY_VALUE                       = "anyvalue";
    public static final String                    REST_OF_THE_WORLD               = "row";
    public static final String                    REST_OF_THE_SERIES              = "rest";

    private CommonUtility()
    {}

    public static String nullCheck(
            Object aObject)
    {
        return nullCheck(aObject, false);
    }

    public static String nullCheck(
            Object aObject,
            boolean aTrimIt)
    {

        if (aObject == null)
        {
            if (aTrimIt)
                return "";

            return " ";
        }

        if (aTrimIt)
            return aObject.toString().trim();
        return aObject.toString();
    }

    public static boolean isEnabled(
            String aCompareString)
    {
        return isTrue(aCompareString);
    }

    public static boolean isTrue(
            String aCompareString)
    {
        if ((aCompareString == null) || aCompareString.isBlank())
            return false;
        return POSSIBLE_ENABLED.contains(aCompareString.toUpperCase());
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
        int returnValue = aDefaultValue;

        try
        {
            returnValue = Integer.parseInt(aIntValue);
        }
        catch (final Exception e)
        {
            // ignore
        }
        return returnValue;
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
        long returnValue = aDefaultValue;

        try
        {
            returnValue = Long.parseLong(aLongValue);
        }
        catch (final Exception e)
        {
        	try {
        		returnValue=DateTimeUtility.getDateFromString(aLongValue, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS).getTime();
        
        	}catch(Exception e1) {
        		
        	}
        }
        return returnValue;
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
        float returnValue = aDefaultValue;

        try
        {
            returnValue = Float.parseFloat(aFloatValue);
        }
        catch (final Exception e)
        {
            // ignore
        }
        return returnValue;
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
        double returnValue = aDefaultValue;

        try
        {
            returnValue = Double.parseDouble(aDoubleValue);
        }
        catch (final Exception e)
        {
            // ignore
        }
        return returnValue;
    }

    public static int getRandomNumber(
            int startNumber,
            int endNumber)
    {
        final Random r = new Random();
        return r.nextInt(endNumber - startNumber) + startNumber;
    }

    public static int getIntegerFromHexString(
            String aHexStringValue)
    {
        return Integer.parseInt(aHexStringValue, 16);
    }

    public static String getStackTrace(
            Throwable anErrorObject)
    {
        if (anErrorObject == null)
            return null;

        final StringWriter errors = new StringWriter();
        anErrorObject.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    public static String concatenate(
            String... aStrings)
    {
        return concatenate(DEFAULT_CONCAT_CHAR, aStrings);
    }

    public static String concatenate(
            char aJoinChar,
            String... aStrings)
    {
        return combine(aJoinChar, aStrings);
    }

    public static String combine(
            String... aStrings)
    {
        return combine(DEFAULT_CONCAT_CHAR, aStrings);
    }

    public static String combine(
            char aJoinChar,
            String... aStrings)
    {
        final StringJoiner sj = new StringJoiner("" + aJoinChar);

        for (final String temp : aStrings)
            sj.add(temp);
        return sj.toString();
    }

    public static String[] split(
            String aStringToSplit)
    {
        return split(aStringToSplit, DEFAULT_CONCAT_CHAR);
    }

    public static String[] split(
            String aStringToSplit,
            char aJoinChar)
    {
        return split(aStringToSplit, Character.toString(aJoinChar));
    }

    public static String[] split(
            String aStringToSplit,
            String aJoingString)
    {
        return StringUtils.split(aStringToSplit, aJoingString);
    }

    public static void sleepForAWhile()
    {
        sleepForAWhile(100);
    }

    public static void sleepForAWhile(
            long aTimeInMilliSeconds)
    {

        try
        {
            Thread.sleep(aTimeInMilliSeconds);
        }
        catch (final InterruptedException e)
        {
        	
        	e.printStackTrace();
        }
    }

    public static void closeResultSet(
            ResultSet aResultSet)
    {
        Statement lStatement = null;

        try
        {

            if (aResultSet != null)
            {
                lStatement = aResultSet.getStatement();
                close(aResultSet);
            }
        }
        catch (final Exception e)
        {
            // ignore
        }
        closeStatement(lStatement);
    }

    public static void closeStatement(
            Statement aStatement)
    {
        close(aStatement);
    }

    public static void closeConnection(
            Connection aConnection)
    {
        close(aConnection);
    }

    public static void rollbackConnection(
            Connection aConnection)
    {

        try
        {
            if (aConnection != null)
                aConnection.rollback();
        }
        catch (final SQLException e)
        {
            // ignore
        }
    }

    public static void closeJedis(
            Closeable aJedis)
    {
        close(aJedis);
    }

    private static void close(
            AutoCloseable aCloseble)
    {

        try
        {
            if (aCloseble != null)
                aCloseble.close();
        }
        catch (final Exception e)
        {}
    }

    private static void close(
            Closeable aCloseble)
    {

        try
        {
            if (aCloseble != null)
                aCloseble.close();
        }
        catch (final IOException e)
        {}
    }

    @Deprecated
    public static Map<String, String> getMapFromResultSet(
            ResultSet aResultSet,
            ResultSetMetaData aResultSetMetaData)
            throws SQLException
    {
        final Map<String, String> map             = new HashMap<>();
        final int                 numberOfColumns = aResultSetMetaData.getColumnCount();

        for (int i = 1; i <= numberOfColumns; ++i)
        {
            final String name  = aResultSetMetaData.getColumnName(i);
            final String value = aResultSet.getString(i);

            if (value != null)
                map.put(name, value);
        }
        return map;
    }

    public static Map<String, String> getAccountDataMap(
            ResultSet aResultSet)
    {
        final Map<String, String> dataMap = new HashMap<>();

        for (final MiddlewareConstant mc : ACCOUNTS_MIDDILEWARE_CONST_LIST)
        {
            final String colName = mc.getName();

            try
            {
                final String value = aResultSet.getString(colName);
                if (value != null)
                    dataMap.put(colName, value);
            }
            catch (final SQLException e)
            {
                log.error("Exception while getting the value for the column '" + colName + "'", e);
            }
        }
        return dataMap;
    }

    public static String getDotStarString(
            String aString)
    {
        return nullCheck(aString, true).equals("") ? DOT_STAR : aString.toLowerCase();
    }

    public static String getAnyString(
            String aString)
    {
        return (nullCheck(aString, true).equals("") || aString.contains(DOT_STAR)) ? ANY_VALUE : aString.toLowerCase();
    }

    public static String encode(
            String val)
    {

        try
        {
            return URLEncoder.encode(val, URL_CHARSET_DEFAULT);
        }
        catch (final Exception e)
        {
            log.error("Excception while URLEncode. Value '" + val + "'", e);
        }
        return val;
    }

    public static String decode(
            String val)
    {

        try
        {
            return URLDecoder.decode(val, URL_CHARSET_DEFAULT);
        }
        catch (final Exception e)
        {
            log.error("Excception while URLDecode. Value '" + val + "'", e);
        }
        return val;
    }

    private static List<MiddlewareConstant> getRedisAccountsRequiredFields()
    {
        final List<MiddlewareConstant> list = new ArrayList<>();

        list.add(MiddlewareConstant.MW_CLIENT_ID);
        list.add(MiddlewareConstant.MW_USER);
        list.add(MiddlewareConstant.MW_UI_PASS);
        list.add(MiddlewareConstant.MW_API_PASS);
        list.add(MiddlewareConstant.MW_SMPP_PASS);
        list.add(MiddlewareConstant.MW_ACC_STATUS);
        list.add(MiddlewareConstant.MW_MSG_TYPE);
        list.add(MiddlewareConstant.MW_ACC_DEFAULT_ROUTE_ID);
        list.add(MiddlewareConstant.MW_PLATFORM_CLUSTER);
        list.add(MiddlewareConstant.MW_SMS_PRIORITY);
        list.add(MiddlewareConstant.MW_NEWLINE_REPLACE_CHAR);
        list.add(MiddlewareConstant.MW_IS_16BIT_UDH);
        list.add(MiddlewareConstant.MW_ACKNOWLEDGE_ID_LENGTH);
        list.add(MiddlewareConstant.MW_CLIENT_ENCRYPT_ENABLED);
        list.add(MiddlewareConstant.MW_BILLING_ENCRYPT_TYPE);
        list.add(MiddlewareConstant.MW_DOMESTIC_PROMO_TRAI_BLOCKOUT_PURGE);
        list.add(MiddlewareConstant.MW_CLIENT_DOMESTIC_SMS_BLOCKOUT_ENABLED);
        list.add(MiddlewareConstant.MW_CLIENT_DOMESTIC_SMS_BLOCKOUT_START);
        list.add(MiddlewareConstant.MW_CLIENT_DOMESTIC_SMS_BLOCKOUT_STOP);
        list.add(MiddlewareConstant.MW_DLT_TEMPL_GRP_ID);
        list.add(MiddlewareConstant.MW_DUPLICATE_CHK_REQ);
        list.add(MiddlewareConstant.MW_DUPLICATE_CHK_INTERVAL);
        list.add(MiddlewareConstant.MW_INTL_SMS_BLOCKOUT_ENABLED);
        list.add(MiddlewareConstant.MW_INTL_SMS_BLOCKOUT_START);
        list.add(MiddlewareConstant.MW_INTL_SMS_BLOCKOUT_STOP);
        list.add(MiddlewareConstant.MW_TIME_ZONE);
        list.add(MiddlewareConstant.MW_TIME_OFFSET);
        list.add(MiddlewareConstant.MW_IP_VALIDATION);
        list.add(MiddlewareConstant.MW_IP_LIST);
        list.add(MiddlewareConstant.MW_MT_ADJUST_ENABLED);
        list.add(MiddlewareConstant.MW_DN_ADJUST_ENABLED);
        list.add(MiddlewareConstant.MW_DND_REJECT_YN);
        list.add(MiddlewareConstant.MW_VL_SHORTNER);
        list.add(MiddlewareConstant.MW_BILL_TYPE);
        list.add(MiddlewareConstant.MW_DND_PREF);
        list.add(MiddlewareConstant.MW_DND_CHK);
        list.add(MiddlewareConstant.MW_SPAM_CHK);
        list.add(MiddlewareConstant.MW_BLACKLIST_CHK);
        list.add(MiddlewareConstant.MW_SMS_RETRY_ENABLED);
        list.add(MiddlewareConstant.MW_IS_SCHEDULE_ALLOW);
        list.add(MiddlewareConstant.MW_UC_IDEN_ALLOW);
        list.add(MiddlewareConstant.MW_UC_IDEN_CHAR_LEN);
        list.add(MiddlewareConstant.MW_UC_IDEN_OCCUR);
        list.add(MiddlewareConstant.MW_IS_REMOVE_UC_CHARS);
        list.add(MiddlewareConstant.MW_URL_SMARTLINK_ENABLE);
        list.add(MiddlewareConstant.MW_URL_TRACKING_ENABLE);
        list.add(MiddlewareConstant.MW_URL_SHORTCODE_LENGTH);
        list.add(MiddlewareConstant.MW_ACC_IS_ASYNC);
        list.add(MiddlewareConstant.MW_USE_DEFAULT_HEADER_ENABLED);
        list.add(MiddlewareConstant.MW_ACC_DEFAULT_HEADER);
        list.add(MiddlewareConstant.MW_CONSIDER_DEFAULTLENGTH_AS_DOMESTIC);
        list.add(MiddlewareConstant.MW_DOMESTIC_TRA_BLOCKOUT_REJECT);
        list.add(MiddlewareConstant.MW_TIMEBOUND_CHK_ENABLED);
        list.add(MiddlewareConstant.MW_TIMEBOUND_INTERVAL);
        list.add(MiddlewareConstant.MW_TIMEBOUND_MAX_REQ_COUNT);
        list.add(MiddlewareConstant.MW_SMS_RATE);
        list.add(MiddlewareConstant.MW_DLT_RATE);
        list.add(MiddlewareConstant.MW_USE_DEFAULT_HEADER_FAIL_ENABLED);
        list.add(MiddlewareConstant.MW_DOMESTIC_SPECIAL_SERIES_ALLOW);
        list.add(MiddlewareConstant.MW_REQ_HEX_MSG);
        list.add(MiddlewareConstant.MW_BILLING_CURRENCY);
        list.add(MiddlewareConstant.MW_BILLING_CURRENCY_CONVERSION_TYPE);
        list.add(MiddlewareConstant.MW_IS_IDLO);
        list.add(MiddlewareConstant.MW_BASE_SMS_RATE);
        list.add(MiddlewareConstant.MW_BASE_ADD_FIXED_RATE);
        list.add(MiddlewareConstant.MW_INVOICE_BASED_ON);
        list.add(MiddlewareConstant.MW_FORCE_DND_CHK);
        list.add(MiddlewareConstant.MW_MSG_RETRY_ENABLED);
        list.add(MiddlewareConstant.MW_CAPPING_CHK_ENABLED);
        list.add(MiddlewareConstant.MW_CAPPING_INTERVAL_TYPE);
        list.add(MiddlewareConstant.MW_CAPPING_INTERVAL);
        list.add(MiddlewareConstant.MW_CAPPING_MAX_REQ_COUNT);
        list.add(MiddlewareConstant.MW_CREDIT_CHECK);
        list.add(MiddlewareConstant.MW_PU_ID);
        list.add(MiddlewareConstant.MW_SU_ID);

        return list;
    }

    public static List<String> getSplitMessageId(
            String aBaseMessageId,
            int aTotalparts)
            throws ItextosException
    {
        return getSplitMessageId(aBaseMessageId, aTotalparts, DEFAULT_MESSAGE_ID_LENGTH);
    }

    public static List<String> getSplitMessageId(
            String aBaseMessageId,
            int aTotalparts,
            int aMessageIdLength)
            throws ItextosException
    {
        if (aBaseMessageId == null)
            return null;

        if ((aTotalparts <= 1) || (aTotalparts > 100))
            throw new ItextosException("Invalid total parts specified. Total Parts : '" + aTotalparts + "'");

 //       final int len = aBaseMessageId.length();

   //     if (len == aMessageIdLength)
       
            final DecimalFormat df       = new DecimalFormat("00");
            final String        base     = aBaseMessageId.substring(0,  aBaseMessageId.length() - 2);
            final List<String>  toReturn = new ArrayList<>();

            for (int index = 0; index < aTotalparts; index++) {
                toReturn.add(base + df.format(index));
            }
            return toReturn;
        
    //    throw new ItextosException("Invalid Message Id length. Message Id : '" + aBaseMessageId + "'. Length : '" + aBaseMessageId.length() + "'");
    }

    public static String getFormattedDouble(
            double aDoubleValue,
            int aNoOfDecimals)
    {
        if (aNoOfDecimals <= 0)
            return Double.toString(aDoubleValue);

        final StringBuilder format = new StringBuilder("0.");
        for (int index = 0; index < aNoOfDecimals; index++)
            format.append("0");
        final DecimalFormat df = new DecimalFormat(format.toString());
        return df.format(aDoubleValue);
    }

    public static String getApplicationServerIp()
    {

        try
        {
            return System.getenv("hostname")+"-"+InetAddress.getLocalHost().getHostAddress();
        	//  return HostIPFetcher.hostip()+"-"+InetAddress.getLocalHost().getHostAddress();
              
            
        }
        catch (final UnknownHostException e)
        {
            log.error("Exception while getting the IP of the application server.", e);
        }
        return "unknown";
    }

    public static String getLineSeparator()
    {
        return PROP_LINE_SEPARATOR;
    }

    private static String getProcessId()
    {

        try
        {
            final String vmName = ManagementFactory.getRuntimeMXBean().getName();
            if ((vmName != null) && vmName.contains("@"))
                return vmName.substring(0, vmName.indexOf("@"));
            return UNKNOWN_PROCESS_ID;
        }
        catch (final Exception e)
        {
            return UNKNOWN_PROCESS_ID;
        }
    }

    public static String getJvmProcessId()
    {
        return JVM_PROCESS_ID;
    }

    public static String findR3CTableName(
            String aShortCode)
    {
        final int    tableCount = 20;
        final String tableName  = "r3c_";
        int          total      = 0;
        for (final char c : aShortCode.toCharArray())
            total += c;

        return tableName + (total % tableCount);
    }

    public static void main(
            String[] args)
    {
        System.out.println(isTrue(""));
    }

}