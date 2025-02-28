package com.itextos.beacon.platform.topic2table.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mariadb.jdbc.internal.util.dao.ReconnectDuringTransactionException;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.topic2table.dbinfo.ColumnInfo;
import com.itextos.beacon.platform.topic2table.dbinfo.DataType;
import com.itextos.beacon.platform.topic2table.inserter.ITableInserter;
import com.itextos.beacon.platform.topic2table.tablename.ITablenameFinder;

public class T2TUtility
{

    private static final Log log = LogFactory.getLog(T2TUtility.class);

    private T2TUtility()
    {}

    public static final String ALL_COLUMNS_QUERY = "select " + " ordinal_position, " + " column_name, " + " data_type, " + " character_maximum_length " + " from " + " information_schema.columns "
            + " where " + " table_name = ? " + " and table_schema = ? " + " order by ordinal_position";

    public static DataType getColumnDataType(
            String string)
    {
        DataType returnValue = DataType.STRING;

        switch (string.toUpperCase())
        {
            case "INT":
            case "TINYINT":
                returnValue = DataType.INT;
                break;

            case "BIGINT":
                returnValue = DataType.BIGINT;
                break;

            case "DATE":
                returnValue = DataType.DATE;
                break;

            case "TIMESTAMP":
            case "DATETIME":
                returnValue = DataType.DATETIME;
                break;

            case "FLOAT":
            case "DECIMAL":
            case "DOUBLE":
                returnValue = DataType.DECIMAL;
                break;

            default:
                returnValue = DataType.STRING;
                break;
        }
        return returnValue;
    }

    public static void populateColumnDataValues(
            PreparedStatement aPstmt,
            BaseMessage aCurrentMessage,
            List<String> aAllMiddlewareConstantNames,
            Map<Integer, ColumnInfo> aAllColumnInfo,
            List<Integer> aAllColumnIndices,
            boolean aTrimIt)
            throws SQLException
    {
        ColumnInfo ci;
        Date       tempDate;

        for (int keyIndex = 0, keySize = aAllMiddlewareConstantNames.size(); keyIndex < keySize; keyIndex++)
        {
            ci = aAllColumnInfo.get(aAllColumnIndices.get(keyIndex));

            switch (ci.getColumnDataType())
            {
                case STRING:
                    final String temp = getStringValue(aCurrentMessage, aAllMiddlewareConstantNames, keyIndex, aTrimIt, ci);

                    // This log has to be removed....
                    if ((temp == null) || temp.isBlank())
                        if (log.isDebugEnabled())
                            log.debug("This log has to be removed.... Column value " + keyIndex + " '" + aAllMiddlewareConstantNames.get(keyIndex) + "' '"
                                    + getMwConstant(aAllMiddlewareConstantNames, keyIndex) + "' messageValue '" + getStringValue(aCurrentMessage, aAllMiddlewareConstantNames, keyIndex)
                                    + "' column info " + ci + " Message " + aCurrentMessage + " >>>>>>>>>>> " + aAllMiddlewareConstantNames); 

                    aPstmt.setString((keyIndex + 1), temp);
                    break;

                case DATE:
                    tempDate = getDateFromString(getStringValue(aCurrentMessage, aAllMiddlewareConstantNames, keyIndex), ci);
                    aPstmt.setTimestamp((keyIndex + 1), tempDate == null ? null : new Timestamp(tempDate.getTime()));
                    break;

                case DATETIME:
                    final String dateTimeString = getStringValue(aCurrentMessage, aAllMiddlewareConstantNames, keyIndex);
                    tempDate = getDateTimeFromString(dateTimeString, ci, getStringValue(aCurrentMessage, MiddlewareConstant.MW_CARRIER_DATE_TIME_FORMAT), true);

                    if (log.isDebugEnabled())
                        log.debug("For Date Time column " + keyIndex + " '" + getMwConstant(aAllMiddlewareConstantNames, keyIndex) + "' '" + dateTimeString + "' '"
                                + getStringValue(aCurrentMessage, MiddlewareConstant.MW_CARRIER_DATE_TIME_FORMAT) + "' Temp Date '" + tempDate + "' final Value '"
                                + (tempDate == null ? null : DateTimeUtility.getFormattedDateTime(tempDate, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS)) + "'");

                    /**
                     * DON'T CHANGE THE BELOW CODE
                     * ===========================
                     * Due to the MYSQL Driver limitation we have to use String equivalent for the
                     * the timestamp to pass the milliseconds
                     * ===========================
                     * non-sense code.
                     */
                    aPstmt.setString((keyIndex + 1), (tempDate == null ? null : DateTimeUtility.getFormattedDateTime(tempDate, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS)));
                    break;

                case INT:
                    aPstmt.setInt((keyIndex + 1), CommonUtility.getInteger(getStringValue(aCurrentMessage, aAllMiddlewareConstantNames, keyIndex), 0));
                    break;

                case BIGINT:
                    aPstmt.setLong((keyIndex + 1), CommonUtility.getLong(getStringValue(aCurrentMessage, aAllMiddlewareConstantNames, keyIndex), 0));
                    break;

                case DECIMAL:
                    aPstmt.setDouble((keyIndex + 1), CommonUtility.getDouble(getStringValue(aCurrentMessage, aAllMiddlewareConstantNames, keyIndex), 0));
                    break;

                default:
                    if (log.isDebugEnabled())
                        log.debug("I am coming here. It should be the case " + aAllMiddlewareConstantNames.get(keyIndex) + " --- Column Info " + ci);
                    /******** WARNING: This should not happened. ********/
                    /*****
                     * We may need to pass the null values in some case, hence don't check for the
                     * nulls here.
                     */
                    aPstmt.setString((keyIndex + 1), getStringValue(aCurrentMessage, aAllMiddlewareConstantNames, keyIndex, aTrimIt, ci));
                    break;
            }
        }
    }

    private static String getStringValue(
            BaseMessage aCurrentMessage,
            List<String> aAllMiddlewareConstantNames,
            int aMCKeyIndex,
            boolean aTrimIt,
            ColumnInfo aColumnInfo)
    {
        final String temp = getStringValue(aCurrentMessage, getMwConstant(aAllMiddlewareConstantNames, aMCKeyIndex));

        /**
         * We may need to pass the null values in some case, hence don't check for the
         * nulls here.
         */
        if (temp == null)
            return null;

        if (aTrimIt && (temp.length() > aColumnInfo.getColumnLength()))
            return temp.substring(0, aColumnInfo.getColumnLength());
        return temp;
    }

    private static String getStringValue(
            BaseMessage aCurrentMessage,
            List<String> aAllMiddlewareConstantNames,
            int aMCKeyIndex)
    {
        return getStringValue(aCurrentMessage, getMwConstant(aAllMiddlewareConstantNames, aMCKeyIndex));
    }

    private static MiddlewareConstant getMwConstant(
            List<String> aAllMiddlewareConstantNames,
            int aMCKeyIndex)
    {
        final MiddlewareConstant lMiddlewareConstantByName = MiddlewareConstant.getMiddlewareConstantByName(aAllMiddlewareConstantNames.get(aMCKeyIndex));

        if (lMiddlewareConstantByName == null)
            log.error("Unable to identify the Constant for the name '" + aAllMiddlewareConstantNames.get(aMCKeyIndex) + "' index is '" + aMCKeyIndex + "' and Column Name List "
                    + aAllMiddlewareConstantNames);

        return lMiddlewareConstantByName;
    }

    private static String getStringValue(
            BaseMessage aCurrentMessage,
            MiddlewareConstant aMwConstant)
    {
        return aCurrentMessage.getValue(aMwConstant);
    }

    private static Date getDateFromString(
            String aDateTimeString,
            ColumnInfo aDBColumnInfo)
    {

        if ("".equals(CommonUtility.nullCheck(aDateTimeString, true)))
        {
            if (log.isInfoEnabled())
                log.info(getUnableParseErrorString(aDateTimeString, aDBColumnInfo.getColumnName()));
            return null;
        }

        Date tempDate = DateTimeUtility.getDateFromString(aDateTimeString, DateTimeFormat.DEFAULT_DATE_ONLY);
        if (tempDate != null)
            return tempDate;

        if (log.isInfoEnabled())
            log.info(getParseErrorString(aDateTimeString, aDBColumnInfo.getColumnName(), DateTimeFormat.DEFAULT_DATE_ONLY.getFormat()));

        try
        {
            final long longtime = Long.parseLong(aDateTimeString);

            if (longtime > 0)
                tempDate = new Date(longtime);
        }
        catch (final Exception e) // ignore the exception
        {}

        if (tempDate != null)
            return tempDate;

        if (log.isInfoEnabled())
            log.info(getParseErrorString(aDateTimeString, aDBColumnInfo.getColumnName(), "Date in Long format (Epoch Time)"));

        return getDateTimeFromString(aDateTimeString, aDBColumnInfo, null, false);
    }

    private static Date getDateTimeFromString(
            String aDateTimeString,
            ColumnInfo aDBColumnInfo,
            String aDateTimeFormat,
            boolean aForTimeOnly)
    {

        if ("".equals(CommonUtility.nullCheck(aDateTimeString, true)))
        {
            if (log.isInfoEnabled())
                log.info(getUnableParseErrorString(aDateTimeString, aDBColumnInfo.getColumnName()));
            return null;
        }

        Date tempDate = DateTimeUtility.getDateFromString(aDateTimeString, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
        if (tempDate != null)
            return tempDate;

        if (log.isInfoEnabled())
            log.info(getParseErrorString(aDateTimeString, aDBColumnInfo.getColumnName(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS.getFormat()));

        tempDate = DateTimeUtility.getDateFromString(aDateTimeString, DateTimeFormat.DEFAULT);
        if (tempDate != null)
            return tempDate;

        if (log.isInfoEnabled())
            log.info(getParseErrorString(aDateTimeString, aDBColumnInfo.getColumnName(), DateTimeFormat.DEFAULT.getFormat()));

        if (aForTimeOnly)
        {

            try
            {
                final long longtime = Long.parseLong(aDateTimeString);

                if (longtime > 0)
                    tempDate = new Date(longtime);
            }
            catch (final Exception e)
            {
                // ignore this exception
            }

            if (tempDate != null)
                return tempDate;

            if (log.isInfoEnabled())
                log.info(getParseErrorString(aDateTimeString, aDBColumnInfo.getColumnName(), "Date in Long format (Epoch Time)"));

            if (!"".equals(CommonUtility.nullCheck(aDateTimeFormat, true)))
            {
                tempDate = DateTimeUtility.getDateFromString(aDateTimeString, aDateTimeFormat);

                if (tempDate != null)
                    return tempDate;

                if (log.isInfoEnabled())
                {
                    log.info(getParseErrorString(aDateTimeString, aDBColumnInfo.getColumnName(), " DateTime Format " + aDateTimeFormat));
                    log.info(getUnableParseErrorString(aDateTimeString, aDBColumnInfo.getColumnName()));
                }
            }
            else
                if (log.isInfoEnabled())
                    log.info(getUnableParseErrorString(aDateTimeString, aDBColumnInfo.getColumnName()));
        }
        else
            if (log.isInfoEnabled())
                log.info(getUnableParseErrorString(aDateTimeString, aDBColumnInfo.getColumnName()));
        return null;
    }

    private static String getUnableParseErrorString(
            String aDateTimeString,
            String aColumnName)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(" Unable to parse '");
        sb.append(aDateTimeString);
        sb.append("' to Date / Time. Sending 'null' value for the column : '");
        sb.append(aColumnName);
        sb.append("'.");
        return sb.toString();
    }

    private static String getParseErrorString(
            String aDateTimeString,
            String aColumnName,
            String aFormat)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(" Unable to parse '");
        sb.append(aDateTimeString);
        sb.append("' to Date / Time with the format '");
        sb.append(aFormat);
        sb.append("' for the column '");
        sb.append(aColumnName);
        sb.append("'.");
        return sb.toString();
    }

    public static ExceptionHandlerType checkExceptionType(
            Exception aException,
            boolean aIsBatchInsert)
    {
        log.error("Exception : '" + aException + "'. isBatchInsert : '" + aIsBatchInsert + "'");

        if (aException == null)
        {
            log.error("Exception : '" + aException + "'. isBatchInsert : '" + aIsBatchInsert + "', Return Value : '" + ExceptionHandlerType.PUSHBACK_TO_SAME_QUEUE_ALONE + "'");
            return ExceptionHandlerType.PUSHBACK_TO_SAME_QUEUE_ALONE;
        }

        String exceptionMessage = aException.getMessage();
        exceptionMessage = (exceptionMessage == null) ? "" : exceptionMessage.toLowerCase();
        if (log.isDebugEnabled())
            log.debug("ExceptionMessage : '" + exceptionMessage + "'");

        ExceptionHandlerType returnValue = checkForConnectionException(aException);

        if (returnValue != null)
            return returnValue;

        returnValue = checkForIndividualProcessException(aIsBatchInsert, exceptionMessage);

        if (returnValue != null)
            return returnValue;

        returnValue = checkForTrimProcessException(exceptionMessage);

        if (returnValue != null)
            return returnValue;

        returnValue = checkForPushBackProcessException(exceptionMessage);

        if (returnValue != null)
            return returnValue;

        returnValue = checkForAlertAndPushBackProcessException(exceptionMessage);

        if (returnValue != null)
            return returnValue;

        returnValue = checkForAlertAndDropMessageException(exceptionMessage);

        if (returnValue != null)
            return returnValue;

        log.error("Unable to identify the exception hence pushing back to the Queue again. Exception : '" + aException.getMessage(), aException);
        return ExceptionHandlerType.PUSHBACK_TO_SAME_QUEUE_ALONE;
    }

    private static ExceptionHandlerType checkForConnectionException(
            Throwable aException)
    {
        log.warn("Maria DB jar is requried to invoke this method.");

        final boolean isConnectionExeption = aException instanceof ReconnectDuringTransactionException;

        if (isConnectionExeption)
        {
            if (log.isInfoEnabled())
                log.info("CommunicationsException occurred. Exception : '" + aException.getMessage() + "'. Pushing back the message to the Queue");

            return ExceptionHandlerType.INSERT_INDIVIDUALLY_WITHOUT_TRIM;
        }
        return null;
    }

    private static ExceptionHandlerType checkForAlertAndPushBackProcessException(
            String aExceptionMessage)
    {
        return checkForMessageInList(aExceptionMessage, ExceptionMessageList.getAlertAndPushbackList(), ExceptionHandlerType.CREATE_ALERT_AND_PUSHBACK_TO_SAME_QUEUE,
                "Hence creating alert and pushing the message to the Queue.");
    }

    private static ExceptionHandlerType checkForAlertAndDropMessageException(
            String aExceptionMessage)
    {
        return checkForMessageInList(aExceptionMessage, ExceptionMessageList.getAlertAndDropMessage(), ExceptionHandlerType.CREATE_ALERT_AND_DROP_MESSAGE,
                "Hence creating alert and dropping the message from the Queue.");
    }

    private static ExceptionHandlerType checkForPushBackProcessException(
            String aExceptionMessage)
    {
        return checkForMessageInList(aExceptionMessage, ExceptionMessageList.getPushBackList(), ExceptionHandlerType.PUSHBACK_TO_SAME_QUEUE_ALONE, "Hence pushing back the message to the Queue.");
    }

    private static ExceptionHandlerType checkForTrimProcessException(
            String aExceptionMessage)
    {
        return checkForMessageInList(aExceptionMessage, ExceptionMessageList.getTrimAndInsertList(), ExceptionHandlerType.TRIM_DATA_THEN_INSERT_AND_ADD_ERROR_LOG,
                "Trimming all the required columns and insert the message into table.");
    }

    private static ExceptionHandlerType checkForMessageInList(
            String aExceptionMessage,
            List<String> aList,
            ExceptionHandlerType aType,
            String aLogMessage)
    {
        if (log.isDebugEnabled())
            log.debug("List of " + aType + " : '" + aList + "'");

        for (final String errorString : aList)
        {
            if (log.isDebugEnabled())
                log.debug("Error String : '" + errorString + "', Exception Message : '" + aExceptionMessage + "'");

            if (aExceptionMessage.indexOf(errorString) > -1)
            {
                if (log.isInfoEnabled())
                    log.info("'" + aExceptionMessage + "' Occurred. " + aLogMessage);

                return aType;
            }
        }
        return null;
    }

    private static ExceptionHandlerType checkForIndividualProcessException(
            boolean aIsBulkInsert,
            String aExceptionMessage)
    {
        ExceptionHandlerType returnValue                 = null;
        final List<String>   individualInsertMessageList = ExceptionMessageList.getIndividualInsertList();
        if (log.isDebugEnabled())
            log.debug("individualInsertMessageList : '" + individualInsertMessageList + "'");

        if (aIsBulkInsert && (!individualInsertMessageList.isEmpty()))
            for (final String errorString : individualInsertMessageList)
            {
                if (log.isDebugEnabled())
                    log.debug("errorString : '" + errorString + "', exceptionMessage : '" + aExceptionMessage + "'");

                if (aExceptionMessage.indexOf(errorString) > -1)
                {
                    if (log.isInfoEnabled())
                        log.info("'" + aExceptionMessage + "' Occurred. Hence inserting the records individually.");

                    returnValue = ExceptionHandlerType.INSERT_INDIVIDUALLY_WITHOUT_TRIM;
                    break;
                }
            }
        return returnValue;
    }

    public static void handleException(
            ITableInserter aParent,
            ExceptionHandlerType aHandlerType,
            Exception e,
            boolean aCallFromIndividualInsert,
            BaseMessage aCurrentMessage)
    {
        if (log.isDebugEnabled())
            log.debug("aParent : '" + aParent + "', aHandlerType : '" + aHandlerType + "', Exception : '" + e + "', aCallFromIndividualInsert : '" + aCallFromIndividualInsert
                    + "', aCurrentMessage : '" + aCurrentMessage + "'");

        switch (aHandlerType)
        {
            case CREATE_ALERT_AND_PUSHBACK_TO_SAME_QUEUE:
                aParent.createAlert(e);

                if (!aCallFromIndividualInsert)
                    aParent.returnToSameTopic();
                else
                    aParent.returnToSameTopic(aCurrentMessage);
                break;

            case INSERT_INDIVIDUALLY_WITHOUT_TRIM:
                if (!aCallFromIndividualInsert)
                    aParent.processIndividualMessages(false);
                else
                    // this should not occur
                    aParent.returnToSameTopic(aCurrentMessage);
                break;

            case TRIM_DATA_THEN_INSERT_AND_ADD_ERROR_LOG:
                if (!aCallFromIndividualInsert)
                {
                    aParent.createAlert(e);
                    aParent.processIndividualMessages(true);
                }
                else
                    aParent.returnToSameTopic(aCurrentMessage);
                break;

            case CREATE_ALERT_AND_DROP_MESSAGE:
                if (!aCallFromIndividualInsert)
                    aParent.processIndividualMessages(false);
                else
                {
                    aParent.createAlert(e);
                    aParent.dropMessage(e, aCurrentMessage);
                }
                break;

            case PUSHBACK_TO_SAME_QUEUE_ALONE:
            default:
                if (!aCallFromIndividualInsert)
                    aParent.returnToSameTopic();
                else
                    aParent.returnToSameTopic(aCurrentMessage);
                break;
        }
    }

    public static ITablenameFinder getTableNameFinder(
            String aTableNameFinderClassName)
    {
        if (log.isDebugEnabled())
            log.debug("Table name finder class name : '" + aTableNameFinderClassName + "'");

        final String     tableNameFinderClassName = CommonUtility.nullCheck(aTableNameFinderClassName, true);

        ITablenameFinder tableNameFinder          = null;
        if (!"".equals(tableNameFinderClassName))
            try
            {
                final Class<?>       cls         = Class.forName(tableNameFinderClassName);
                final Constructor<?> constructor = cls.getDeclaredConstructor();
                tableNameFinder = (ITablenameFinder) constructor.newInstance();
            }
            catch (
                    ClassNotFoundException
                    | InstantiationException
                    | IllegalAccessException
                    | NoSuchMethodException
                    | SecurityException
                    | IllegalArgumentException
                    | InvocationTargetException e)
            {
              //  throw new ItextosRuntimeException(e.getMessage(), e);
            	log.error(e.getMessage(), e);
            }
        return tableNameFinder;
    }

    public static Map<String, String> getKeyValueMapFromMessage(
            String[] allKeyNames,
            BaseMessage aCurrentMessage)
    {
        final Map<String, String> map = new HashMap<>();
        if ((allKeyNames != null) && (aCurrentMessage != null))
            for (final String lKeyName : allKeyNames)
                map.put(lKeyName, aCurrentMessage.getValue(MiddlewareConstant.getMiddlewareConstantByName(lKeyName)));
        else
            log.error("Either the Message or the Keynames passed as null to this method.");
        return map;
    }

    public static String replaceTableName(
            String aInsertQuery,
            String aCurTableName)
    {
        return MessageFormat.format(aInsertQuery, aCurTableName);
    }

}