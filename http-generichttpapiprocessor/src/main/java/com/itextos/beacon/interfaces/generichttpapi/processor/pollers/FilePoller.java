package com.itextos.beacon.interfaces.generichttpapi.processor.pollers;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;
import com.itextos.beacon.http.generichttpapi.common.utils.FileGenUtil;
import com.itextos.beacon.http.generichttpapi.common.utils.Utility;

public class FilePoller
        implements
        ITimedProcess
{

    private static final Log     log         = LogFactory.getLog(FilePoller.class);
    private boolean              canContinue = true;
    private final TimedProcessor mTimedProcessor;

    public FilePoller()
    {
        final String threadName = "TimerThread-FilePoller";
        if (log.isDebugEnabled())
            log.debug(" APIConstants.POLLER_SLEEP_TIME_IN_MILLIS : " + APIConstants.POLLER_SLEEP_TIME_IN_MILLIS);
    
        mTimedProcessor = new TimedProcessor(threadName, this, TimerIntervalConstant.INTERFACE_FILE_POLLER_LOOKUP_INTERVAL);
     
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, threadName);
        
        if (log.isDebugEnabled())
            log.debug("Started File Poller with name " + threadName);
    }

    @Override
    public boolean processNow()
    {
        final boolean isKafkaAvailable = CommonUtility.isEnabled(Utility.getConfigParamsValueAsString(ConfigParamConstants.IS_KAFKA_AVAILABLE));

        if (isKafkaAvailable)
            doProcess();
        return false;
    }

    public static void doProcess()
    {
        if (ThreadExecutor.getInstance().getPendingQueue() > 500)
            return;

        try
        {
            final Set<String> lFileList = FileGenUtil.listFiles(APIConstants.REQUEST_FILE_PATH + APIConstants.CLUSTER_INSTANCE);

            if (log.isDebugEnabled())
                log.debug("Files List : " + lFileList);

            for (final String lFileName : lFileList)
            {
                if (log.isDebugEnabled())
                    log.debug("File Name : " + lFileName);

                if (ThreadExecutor.getInstance().getPendingQueue() > 1500)
                    break;

                if (log.isDebugEnabled())
                    log.debug("Files in WIP  : " + ThreadExecutor.getInstance().getPendingQueue());

                final FileToKafkaPush xprpt = new FileToKafkaPush(lFileName);
                ThreadExecutor.getInstance().addTask(xprpt);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while processing the files.", e);
        }
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}