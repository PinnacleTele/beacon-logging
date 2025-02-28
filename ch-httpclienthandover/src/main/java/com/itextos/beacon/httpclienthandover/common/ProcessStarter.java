package com.itextos.beacon.httpclienthandover.common;

import java.util.List;

import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverData;
import com.itextos.beacon.httpclienthandover.utils.ClientHandoverConstatnts;
import com.itextos.beacon.httpclienthandover.utils.ClientHandoverUtils;

public class ProcessStarter
        implements
        Runnable
{

    private final String  clientId;
    private final boolean isClientSpecific;
    private final int     batchSize;
    private boolean       stopMe;

    public ProcessStarter(
            String aClientId)
    {
        clientId         = aClientId;
        isClientSpecific = !aClientId.equals(ClientHandoverConstatnts.DEFAULT_KEY);

        if (isClientSpecific)
        {
            final ClientHandoverData clientHandoverData = ClientHandoverUtils.getClientHandoverData(clientId);
            batchSize = clientHandoverData.getBatchSize() > 0 ? clientHandoverData.getBatchSize() : 1;
        }
        else
            batchSize = ClientHandoverConstatnts.DEFAULT_BATCH_SIZE;
    }

    public void process(
            List<BaseMessage> deliveryObjects)
    {
        final IHandoverProcessor handoverPorcessor = new DLRProcessor(deliveryObjects, isClientSpecific, isClientSpecific ? clientId : "");
        handoverPorcessor.process();
    }

    @Override
    public void run()
    {

        while (!stopMe)
        {
        	
            final List<BaseMessage> deliveryObjects = Inmemorydata.getInstance().getMessages(clientId, batchSize);

            if (!deliveryObjects.isEmpty()) {
                process(deliveryObjects);
            }else {
           	 CommonUtility.sleepForAWhile(100);

            }
            
        }
    }

    public void stopMe()
    {
        stopMe = true;
    }

}