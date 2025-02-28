package com.itextos.beacon.commonlib.scheduler.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;

@DisallowConcurrentExecution
public abstract class AbstractScheduleJob
        implements
        IItextosScheduleJob
{

    private static final Log log = LogFactory.getLog(AbstractScheduleJob.class);

}