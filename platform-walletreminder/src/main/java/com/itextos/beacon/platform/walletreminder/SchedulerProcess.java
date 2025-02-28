package com.itextos.beacon.platform.walletreminder;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.daemonprocess.ShutdownHandler;
import com.itextos.beacon.commonlib.daemonprocess.ShutdownHook;
import com.itextos.beacon.platform.walletreminder.quartz.schedule.WalletBalanceScheduler;

public class SchedulerProcess
        implements
        ShutdownHook
{

    private final WalletBalanceScheduler mScheduler;

    public SchedulerProcess()
    {
        mScheduler = new WalletBalanceScheduler();
        ShutdownHandler.getInstance().addHook("WalletScheduler", this);
    }

    public void start()
            throws Exception
    {
        mScheduler.start();
    }

    @Override
    public void shutdown()
            throws ItextosException
    {
        if (mScheduler != null)
            mScheduler.shutdown();
    }

}