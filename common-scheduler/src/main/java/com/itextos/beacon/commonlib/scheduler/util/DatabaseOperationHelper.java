package com.itextos.beacon.commonlib.scheduler.util;

public abstract class DatabaseOperationHelper
{

    protected static final int    COL_INDEX_SCHEDULE_ID                  = 1;
    protected static final int    COL_INDEX_SCHEDULE_GROUP_ID            = 2;
    protected static final int    COL_INDEX_SCHEDULE_NAME                = 3;
    protected static final int    COL_INDEX_SCHEDULE_JOB_CLASS_NAME      = 4;
    protected static final int    COL_INDEX_SCHEDULE_CRON_EXPRESSION     = 5;
    protected static final int    COL_INDEX_SCHEDULE_STATE               = 6;
    protected static final int    COL_INDEX_SCHEDULE_MISFIRE_INSTRUCTION = 7;
    protected static final int    COL_INDEX_SCHEDULE_PARAM_NAME          = 8;
    protected static final int    COL_INDEX_SCHEDULE_PARAM_VALUE         = 9;
    protected static final int    COL_INDEX_SCHEDULE_DATA_TYPE           = 10;
    protected static final int    COL_INDEX_SCHEDULE_DATE_TIME_FORMAT    = 11;

    protected static final String SQL_INSERT_SCHEDULER_LOG               = "insert into scheduler.scheduler_log values (?, ?, ?, ?, ?, ?, ?, ?, ?, now())";

    protected static final String SELECT_QUERY                           = "select" //
            + " si.scheduler_id,  si.scheduler_group_id,  si.scheduler_name," //
            + " si.schedule_job_class, si.schedule_cron_expression, si.state," //
            + " si.misfire_instruction, sd.param_name, sd.param_value," //
            + " sd.param_data_type, sd.date_time_format" //
            + " from" //
            + " scheduler.scheduler_info si left outer join scheduler.scheduler_detail sd" //
            + " on (si.scheduler_id = sd.scheduler_id and si.is_active = sd.is_active )" //
            + " where" //
            + " si.is_active = 1 and si.state in ('0', '1')";

    protected static final String INITIAL_SELECT_QUERY                   = "select" //
            + " si.scheduler_id,  si.scheduler_group_id,  si.scheduler_name," //
            + " si.schedule_job_class, si.schedule_cron_expression, si.state," //
            + " si.misfire_instruction, sd.param_name, sd.param_value," //
            + " sd.param_data_type, sd.date_time_format" //
            + " from" //
            + " scheduler.scheduler_info si left outer join scheduler.scheduler_detail sd" //
            + " on (si.scheduler_id = sd.scheduler_id and si.is_active = sd.is_active )" //
            + " where" //
            + " si.is_active = 1 and si.state in ('0', '1', '2')";

    protected static final String UNSCHDULE_SELECT_QUERY                 = "select" + " si.scheduler_id, si.scheduler_group_id " //
            + " from" //
            + " scheduler.scheduler_info si" //
            + " where" //
            + " si.is_active = 0" //
            + " or (si.is_active = 1 and si.state in ('3'))";

    protected static final String UPDATE_QUERY                           = "update scheduler.scheduler_info set state = 2 where scheduler_id =?";
    protected static final String UNSCHEDULE_UPDATE_QUERY                = "update scheduler.scheduler_info set state = 4 where scheduler_id =?";

}
