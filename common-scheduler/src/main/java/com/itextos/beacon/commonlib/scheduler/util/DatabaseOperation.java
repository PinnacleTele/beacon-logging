package com.itextos.beacon.commonlib.scheduler.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobKey;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfo;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.scheduler.config.DataType;
import com.itextos.beacon.commonlib.scheduler.config.MisfireInstruction;
import com.itextos.beacon.commonlib.scheduler.config.ParamInfo;
import com.itextos.beacon.commonlib.scheduler.config.ScheduleInfo;
import com.itextos.beacon.commonlib.scheduler.config.ScheduleState;
import com.itextos.beacon.commonlib.scheduler.logging.JobExecutedData;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class DatabaseOperation
        extends
        DatabaseOperationHelper
{

    private static final Log log = LogFactory.getLog(DatabaseOperation.class);

    private DatabaseOperation()
    {}

    public static Map<String, ScheduleInfo> getScheduleInfo(
            boolean aInitialLoad)
            throws Exception
    {
        final Map<String, ScheduleInfo> returnValue = new HashMap<>();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try 
        {
        	  con = getConnection();
              pstmt = con.prepareStatement(aInitialLoad ? INITIAL_SELECT_QUERY : SELECT_QUERY);
              rs = pstmt.executeQuery();
            getScheduleInfo(returnValue, rs);
        }catch(Exception e) {
        	e.printStackTrace();
        }finally
        {
            CommonUtility.closeResultSet(rs);
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
   
        }
        return returnValue;
    }

    public static Map<String, JobKey> getUnScheduleInfo()
            throws Exception
    {
        final Map<String, JobKey> unscheduleData = new HashMap<>();

        try (
                Connection con = getConnection();
                final PreparedStatement pstmt = con.prepareStatement(UNSCHDULE_SELECT_QUERY);
                final ResultSet rs = pstmt.executeQuery();)
        {
            getUnScheduleInfo(unscheduleData, rs);
        }
        return unscheduleData;
    }

    public static void updateSchedule(
            Map<String, Date> aScheduleResult)
    {

        try (
                Connection con = getConnection();
                final PreparedStatement pstmt = con.prepareStatement(UPDATE_QUERY);)
        {
            boolean recordAdded = false;

            for (final Entry<String, Date> entry : aScheduleResult.entrySet())
            {
                final String scheduleId   = entry.getKey();
                final Date   scheduleDate = entry.getValue();

                if (scheduleDate != null)
                {
                    if (log.isDebugEnabled())
                        log.debug("Updating Schedule id '" + scheduleId + "'");

                    pstmt.setString(1, scheduleId);
                    pstmt.addBatch();
                    recordAdded = true;
                }
            }

            if (recordAdded)
                pstmt.executeBatch();
        }
        catch (final Exception e)
        {
            final String s = "Exception on updatings the schedule data from db";
            log.error(s, e);
      //      throw new ItextosRuntimeException(s, e);
        }
    }

    public static void updateUnscheduleRecords(
            Map<String, Boolean> aUnscheduleResult)
    {

        try (
                Connection con = getConnection();
                final PreparedStatement pstmt = con.prepareStatement(UNSCHEDULE_UPDATE_QUERY);)
        {
            boolean recordAdded = false;
            for (final Entry<String, Boolean> entry : aUnscheduleResult.entrySet())
                if (entry.getValue())
                {
                    pstmt.setString(1, entry.getKey());
                    pstmt.addBatch();
                    recordAdded = true;
                }

            if (recordAdded)
                pstmt.executeBatch();
        }
        catch (final Exception e)
        {
            final String s = "Exception on updating records for unschedule data in db";
            log.error(s, e);
         //   throw new ItextosRuntimeException(s, e);
        }
    }

    public static void insertJobWasExecuted(
            List<JobExecutedData> aJobTobeExecuted)
    {

        try (
                Connection con = getConnection();
                final PreparedStatement pstmt = con.prepareStatement(SQL_INSERT_SCHEDULER_LOG);)
        {

            for (final JobExecutedData jed : aJobTobeExecuted)
            {
                pstmt.setString(1, jed.getScheduleId());
                pstmt.setString(2, jed.getScheduleGroupId());
                pstmt.setString(3, jed.getScheduleName());
                pstmt.setTimestamp(4, getTime(jed.getScheduledFireTime()));
                pstmt.setTimestamp(5, getTime(jed.getFiredTime()));

                final int status = getStatus(jed);
                pstmt.setInt(6, status);

                pstmt.setTimestamp(7, getTime(jed.getNextFireTime()));

                pstmt.setLong(8, jed.getDuration());
                pstmt.setString(9, jed.getErrorDesc());

                pstmt.addBatch();
            }

            pstmt.executeBatch();
        }
        catch (final Exception e)
        {
            log.error("Exception while insertin the scheduler log in to db " + aJobTobeExecuted, e);
        }
    }

    private static Timestamp getTime(
            Date aTime)
    {
        if (aTime != null)
            return new Timestamp(aTime.getTime());
        return null;
    }

    private static int getStatus(
            JobExecutedData aJed)
    {
        if (aJed.isMisFired())
            return 2;

        if ((aJed.getErrorDesc() != null) && !aJed.getErrorDesc().isBlank())
            return 1;

        return 0;
    }

    private static Connection getConnection()
            throws Exception
    {
        // TODO Has to be make is as property driven
        return DBDataSourceFactory.getConnectionFromThin(JndiInfo.CONFIGURARION_DB);
    }

    private static void getScheduleInfo(
            Map<String, ScheduleInfo> aReturnValue,
            ResultSet aResultset)
            throws SQLException
    {

        while (aResultset.next())
        {
            final String scheduleId    = CommonUtility.nullCheck(aResultset.getString(COL_INDEX_SCHEDULE_ID), true);
            ScheduleInfo lScheduleInfo = aReturnValue.get(scheduleId);

            if (lScheduleInfo == null)
            {
                lScheduleInfo = getScheduleInfo(scheduleId, aResultset);
                aReturnValue.put(scheduleId, lScheduleInfo);
            }

            final ParamInfo lParamInfo = getParamInfo(scheduleId, aResultset);
            lScheduleInfo.addParamInfo(lParamInfo);
        }
    }

    private static void getUnScheduleInfo(
            Map<String, JobKey> aUnscheduleData,
            ResultSet aRs)
            throws SQLException
    {
        while (aRs.next())
            aUnscheduleData.put(CommonUtility.nullCheck(aRs.getString(COL_INDEX_SCHEDULE_ID), true),
                    JobKey.jobKey(CommonUtility.nullCheck(aRs.getString(COL_INDEX_SCHEDULE_ID), true), CommonUtility.nullCheck(aRs.getString(COL_INDEX_SCHEDULE_GROUP_ID), true)));
    }

    private static ParamInfo getParamInfo(
            String aScheduleId,
            ResultSet aRs)
            throws SQLException
    {
        final String paramName           = CommonUtility.nullCheck(aRs.getString(COL_INDEX_SCHEDULE_PARAM_NAME), true);
        final String paramValue          = CommonUtility.nullCheck(aRs.getString(COL_INDEX_SCHEDULE_PARAM_VALUE), true);
        final String paramDatatype       = CommonUtility.nullCheck(aRs.getString(COL_INDEX_SCHEDULE_DATA_TYPE), true);
        final String paramDateTimeFormat = CommonUtility.nullCheck(aRs.getString(COL_INDEX_SCHEDULE_DATE_TIME_FORMAT), true);

        if (paramName.isBlank())
            return null;
        return new ParamInfo(aScheduleId, paramName, paramValue, getDataType(paramDatatype), paramDateTimeFormat);
    }

    private static ScheduleInfo getScheduleInfo(
            String aScheduleId,
            ResultSet aRs)
            throws SQLException
    {
        return new ScheduleInfo(aScheduleId, CommonUtility.nullCheck(aRs.getString(COL_INDEX_SCHEDULE_GROUP_ID), true), CommonUtility.nullCheck(aRs.getString(COL_INDEX_SCHEDULE_NAME), true),
                CommonUtility.nullCheck(aRs.getString(COL_INDEX_SCHEDULE_JOB_CLASS_NAME), true), CommonUtility.nullCheck(aRs.getString(COL_INDEX_SCHEDULE_CRON_EXPRESSION), true),
                getScheduleState(CommonUtility.nullCheck(aRs.getString(COL_INDEX_SCHEDULE_STATE), true)),
                getMisFireInst(CommonUtility.nullCheck(aRs.getString(COL_INDEX_SCHEDULE_MISFIRE_INSTRUCTION), true)));
    }

    protected static MisfireInstruction getMisFireInst(
            String aMisFire)
    {

        switch (aMisFire)
        {
            case "0":
                return MisfireInstruction.DONT_DO_ANYTHING_ON_MISFIRE;

            case "1":
                return MisfireInstruction.FIRE_ONCE_ON_MISFIRE;

            default:
        }
        return null;
    }

    protected static ScheduleState getScheduleState(
            String aState)
    {

        switch (aState)
        {
            case "0":
                return ScheduleState.READY;

            case "1":
                return ScheduleState.RESCHEDULED;

            case "2":
                return ScheduleState.SCHEDULED;

            case "3":
                return ScheduleState.UNSCHEDULED;

            case "4":
                return ScheduleState.EXPIRED;

            default:
        }
        return null;
    }

    protected static DataType getDataType(
            String aDataType)
    {

        switch (aDataType)
        {
            case "1":
                return DataType.STRING;

            case "2":
                return DataType.INT_OR_LONG;

            case "3":
                return DataType.FLOAT_OR_DOUBLE;

            case "4":
                return DataType.DATE;

            case "5":
                return DataType.TIME;

            case "6":
                return DataType.DATE_AND_TIME;

            case "7":
                return DataType.BOOLEAN;

            default:
        }
        return null;
    }

}
