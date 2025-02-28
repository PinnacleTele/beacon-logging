package com.itextos.beacon.httpclienthandover.common;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverData;
import com.itextos.beacon.httpclienthandover.process.IDLRProcess;
import com.itextos.beacon.httpclienthandover.process.MultipleDLRProcess;
import com.itextos.beacon.httpclienthandover.process.SingleDLRProcess;
import com.itextos.beacon.httpclienthandover.utils.ClientHandoverUtils;

public abstract class AbstractHandoverProcessor
        implements
        IHandoverProcessor
{

    protected final List<BaseMessage> messageList;
    protected final String            customerId;
    private final boolean             isClientSpecific;

    AbstractHandoverProcessor(
            List<BaseMessage> aMessageList,
            boolean aIsClientSpecific,
            String aCustomerId)
    {
        this.messageList      = aMessageList;
        this.isClientSpecific = aIsClientSpecific;
        this.customerId       = aCustomerId;
    }

    public IDLRProcess getProcess()
    {
        final boolean isBatch = checkIfBatch();
        IDLRProcess   dlrProcess;

        if (isBatch && isClientSpecific)
            dlrProcess = new MultipleDLRProcess(customerId);
        else
            dlrProcess = new SingleDLRProcess();
        return dlrProcess;
    }

    private boolean checkIfBatch()
    {
        if (StringUtils.isEmpty(customerId))
            return false;

        final ClientHandoverData lClientHandoverData = ClientHandoverUtils.getClientHandoverData(customerId);
        return lClientHandoverData.getBatchSize() > 0;
    }

}