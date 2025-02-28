package com.itextos.beacon.platform.customkafkaprocessor.process;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.customkafkaprocessor.InmemoryCollection;

abstract class AbstractCustomProcess
        implements
        ICustomProcess
{

    private static final Log log         = LogFactory.getLog(AbstractCustomProcess.class);

    private boolean          canContinue = true;
    private int              processSize = 1000;

    AbstractCustomProcess(
            int aProcessSize)
    {
        processSize = aProcessSize;
    }

    @Override
    public void run()
    {

        while (canContinue)
        {
            final List<IMessage> messages = InmemoryCollection.getInstance().getList(processSize);

            if (messages.isEmpty())
            {
                CommonUtility.sleepForAWhile();
                log.fatal("No records found in Inmemory collection.");
            }
            else
                process(messages);
        }
    }

    protected abstract void process(
            List<IMessage> aMessages);

    @Override
    public void stopProcess()
    {
        canContinue = false;
    }

}