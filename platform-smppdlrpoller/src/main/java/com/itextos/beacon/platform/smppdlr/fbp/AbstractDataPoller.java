package com.itextos.beacon.platform.smppdlr.fbp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.platform.smppdlrutil.dao.SmppDlrFallBackDao;
import com.itextos.beacon.platform.smppdlrutil.util.SmppDlrUtil;

public abstract class AbstractDataPoller
        implements
        ITimedProcess
{

    private static final Log     log         = LogFactory.getLog(AbstractDataPoller.class);

    private final TimedProcessor mTimedProcessor;
    private boolean              canContinue = true;

    protected AbstractDataPoller()
    {
        super();

        mTimedProcessor = new TimedProcessor("SmppDlrFallbackTableReader", this, TimerIntervalConstant.SMPP_DLR_FALLBACK_TABLE_READER);
  
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "SmppDlrFallbackTableReader");
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        
        return doProcess();
    }

    private static boolean doProcess()
    {

        try
        {
            final Map<Long, DeliveryObject> lRecords  = SmppDlrFallBackDao.getRecordsFromTable();
            final List<Long>                toDelete  = new ArrayList<>(lRecords.keySet());
            final List<DeliveryObject>      toProcess = new ArrayList<>(lRecords.values());

            if(log.isDebugEnabled()) {
            log.debug("SmppDlrFallBackDao.getRecordsFromTable() : data size : "+ lRecords.size());
            }
            SmppDlrUtil.smppDeliveryProcess(toProcess);

            SmppDlrFallBackDao.deleteRecordsFromTable(toDelete);
            
            return toDelete.size()>0;
        }
        catch (final Exception e)
        {
            log.error("Exception while sending the message to Client wise DN Redis..", e);
        }
        
        return false;
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}