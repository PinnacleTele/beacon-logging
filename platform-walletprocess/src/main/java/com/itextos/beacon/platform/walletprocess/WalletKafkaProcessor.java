package com.itextos.beacon.platform.walletprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.platform.walletbase.data.WalletInput;
import com.itextos.beacon.platform.walletbase.database.DbInserter;
import com.itextos.beacon.platform.walletbase.util.WalletHistoryKafkaProperties;

class WalletKafkaProcessor
        implements
        ITimedProcess
{

    private static final Log log = LogFactory.getLog(WalletKafkaProcessor.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final WalletKafkaProcessor INSTANCE = new WalletKafkaProcessor();

    }

    static WalletKafkaProcessor getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final BlockingQueue<WalletInput> mWalletInputInMemory = new LinkedBlockingQueue<>(10000);
    private boolean                          mCanContinue         = true;
    private TimedProcessor                   mTimedProcessor      = null;
    private boolean                          inProcess            = false;

    private WalletKafkaProcessor()
    {

        try
        {
        	
            mTimedProcessor = new TimedProcessor("WalletHistoryProducer", this, TimerIntervalConstant.KANNEL_RESPONSE_REFRESH);
       
            ExecutorSheduler.getInstance().addTask(mTimedProcessor, "WalletHistoryProducer");
        }
        catch (final Exception e)
        {
            log.error("Exception while starting the Wallet History Producer / Consumer", e);
            System.exit(-1);
        }
    }

    void addWalletInfo(
            WalletInput aWalletDeductRefundInput)
    {

        if (WalletHistoryKafkaProperties.getInstance().isWalletHistoryLogRequired())
        {
            boolean success    = false;
            int     retryCount = 0;

            while (!success)
            {
                retryCount++;

                try
                {
                    success = mWalletInputInMemory.offer(aWalletDeductRefundInput, 100, TimeUnit.MILLISECONDS);
                }
                catch (final Exception e1)
                {
                    log.error("Exception while adding the WalletInput into inmemory queue. Retry count :'" + retryCount + "'. Will do retry " + aWalletDeductRefundInput, e1);
                }
            }
        }
    }

    boolean isCompleted()
    {
        return !inProcess && mWalletInputInMemory.isEmpty();
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {

        if (!mWalletInputInMemory.isEmpty())
        {
            if (log.isDebugEnabled())
                log.debug("Inmemory wallet history size " + mWalletInputInMemory.size());

            inProcess = true;

            for (int index = 0; (index < 10) || (!mWalletInputInMemory.isEmpty()); index++)
            {
                int inMemSize = mWalletInputInMemory.size();
                if (inMemSize > 1000)
                    inMemSize = 1000;

                final List<WalletInput> toInsert = new ArrayList<>(inMemSize);
                mWalletInputInMemory.drainTo(toInsert, inMemSize);

                /*
                final Thread t = new Thread(() -> {
                    insertIntoDb(toInsert);
                }, "WalletInputToDb-" + (index + 1));

                t.start();
                */
                Thread t=  Thread.startVirtualThread(() -> {
                    insertIntoDb(toInsert);
                });
                t.setName( "WalletInputToDb-" + (index + 1));
            }

            inProcess = false;
        }

        return false;
    }

    private static void insertIntoDb(
            List<WalletInput> aToInsert)
    {
        if (log.isDebugEnabled())
            log.debug("Inserting data with the size " + aToInsert.size());

        try
        {
            DbInserter.insertIntoDb(aToInsert);
        }
        catch (final Exception e)
        {
            log.error("Exception whild doing a bulk insert. Try to insert individual records.", e);
            doIndividualInsert(aToInsert);
        }
    }

    private static void doIndividualInsert(
            List<WalletInput> aToInsert)
    {
        for (final WalletInput wdri : aToInsert)
            try
            {
                DbInserter.insertIntoDb(wdri);
            }
            catch (final Exception e)
            {
                log.error("Need to handle this.", e);
            }
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
    }

}