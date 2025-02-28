package com.itextos.beacon.platform.singledn.redis.delete;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.inmemory.customfeatures.pojo.DlrTypeInfo;
import com.itextos.beacon.platform.singledn.process.RedisOperation;
import com.itextos.beacon.platform.singledn.process.SingleDNUtil;

public class SingleDnDuplicateCheckRemove
        implements
        ITimedProcess
{

    private static final Log log             = LogFactory.getLog(SingleDnDuplicateCheckRemove.class);

    private TimedProcessor   mTimedProcessor = null;
    private boolean          mCanPrrocess    = true;

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final SingleDnDuplicateCheckRemove INSTANCE = new SingleDnDuplicateCheckRemove();

    }

    public static SingleDnDuplicateCheckRemove getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    public SingleDnDuplicateCheckRemove()
    {
    	
        mTimedProcessor = new TimedProcessor("SingleDnDuplicateCheckRemove", this, TimerIntervalConstant.SINGLE_DN_DUPCHECK_PROCESS);
    
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "SingleDnDuplicateCheckRemove");
        
         log.info("SingleDnDuplicateCheckRemove Processor started ........");
    }

    @Override
    public boolean processNow()
    {

        try
        {
            final Map<String, DlrTypeInfo> lDlrTypeLs = SingleDNUtil.getDnTypeInfoMap();

            if (log.isDebugEnabled())
                log.debug("DlrInfo Map : " + lDlrTypeLs);

            for (final Entry<String, DlrTypeInfo> entry : lDlrTypeLs.entrySet())
            {
                final DlrTypeInfo lDlrInfo = entry.getValue();

                if (log.isDebugEnabled())
                    log.debug("DlrInfo : " + lDlrInfo);

                if (CommonUtility.isEnabled(lDlrInfo.getDnType()))
                {
                    final String  lClientId = lDlrInfo.getClientId();

                    final boolean delStatus = RedisOperation.removeDuplicateCheckData(lClientId);

                    if (log.isDebugEnabled())
                        log.debug("Single DN Proceed data removed for Client='" + lClientId + "', status :" + delStatus);
                }
            }
        }
        catch (final Exception e)
        {
            log.error("Exception occer while removing Duplicate Check singleDn data..", e);
        }

        return false;
    }

    @Override
    public boolean canContinue()
    {
        return mCanPrrocess;
    }

    @Override
    public void stopMe()
    {
        mCanPrrocess = false;
    }

}
