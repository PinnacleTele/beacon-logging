package com.itextos.beacon.httpclienthandover.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler2;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverData;
import com.itextos.beacon.httpclienthandover.retry.ExpiredMessageLogger;
import com.itextos.beacon.httpclienthandover.retry.HandoverRetryReaper;
import com.itextos.beacon.httpclienthandover.retry.RetryProcessPoller;
import com.itextos.beacon.httpclienthandover.utils.ClientHandoverConstatnts;
import com.itextos.beacon.httpclienthandover.utils.ClientHandoverUtils;
import com.itextos.beacon.inmemory.clidlrpref.ClientDlrConfig;
import com.itextos.beacon.inmemory.clidlrpref.ClientDlrConfigCollection;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class Inmemorydata
{

    private static final Log log    = LogFactory.getLog(Inmemorydata.class);

    private static final int BUFFER = 100;

    private static class SingletonHolder
    {

        static final Inmemorydata INSTANCE = new Inmemorydata();

    }

    public static Inmemorydata getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final List<ProcessStarter>                    processStartersList = new ArrayList<>();
    private final Map<String, BlockingQueue<BaseMessage>> kafkaDataMap        = new ConcurrentHashMap<>();

    private Inmemorydata()
    {}

    public void add(
            DeliveryObject aDeliveryObject)
    {
        final String                    clientId                   = aDeliveryObject.getClientId();
        final ClientDlrConfigCollection lClientDlrConfigCollection = (ClientDlrConfigCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CLIENT_DLR_PREF);
        final ClientDlrConfig           aClientDlrConfig           = lClientDlrConfigCollection.getDlrHandoverConfig(clientId, aDeliveryObject.getAppType(), aDeliveryObject.getInterfaceType(),
                aDeliveryObject.isDlrRequestFromClient());

        BlockingQueue<BaseMessage>      tempQueue                  = null;

        if (log.isDebugEnabled())
            log.debug("Client Specific topic: '" + aClientDlrConfig.isClientSpecificHttpTopic() + "'");

        if (aClientDlrConfig.isClientSpecificHttpTopic())
            tempQueue = kafkaDataMap.computeIfAbsent(clientId, k -> createClientSpecificQueue(clientId));
        else
            tempQueue = kafkaDataMap.computeIfAbsent(ClientHandoverConstatnts.DEFAULT_KEY, k -> createCommonQueue());

        try
        {
            tempQueue.put(aDeliveryObject);
        }
        catch (final InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private LinkedBlockingQueue<BaseMessage> createCommonQueue()
    {
        final ApplicationConfiguration applicationConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        final int                      batchSize                = CommonUtility.getInteger(applicationConfiguration.getConfigValue(ClientHandoverConstatnts.GLOBAL_DEFAULT_BATCH_SIZE), 1000);
        final int                      threadCount              = CommonUtility.getInteger(applicationConfiguration.getConfigValue(ClientHandoverConstatnts.GLOBAL_DEFAULT_THREAD_COUNT), 10);
        startAThread(ClientHandoverConstatnts.DEFAULT_KEY, threadCount);
        return new LinkedBlockingQueue<>(batchSize);
    }

    private LinkedBlockingQueue<BaseMessage> createClientSpecificQueue(
            String clientId)
    {
        final ClientHandoverData clientHandoverData = ClientHandoverUtils.getClientHandoverData(clientId);
        startAThread(clientId, clientHandoverData.getThreadCount());
        return new LinkedBlockingQueue<>((clientHandoverData.getThreadCount() * clientHandoverData.getBatchSize()) + BUFFER);
    }

    private void startAThread(
            String aClientId,
            int aThreadCount)
    {

        for (int index = 0; index < aThreadCount; index++)
        {
            if (log.isDebugEnabled())
                log.debug("Starting the client handover thread for client id '" + aClientId + "' thread count '" + (index + 1) + "'");

            final ProcessStarter processStarter = new ProcessStarter(aClientId);
           
            ExecutorSheduler2.getInstance().addTask(processStarter, "Process Starter - " + aClientId + "-" + index);
            processStartersList.add(processStarter);
        }

        final boolean isClientSpecific = (!aClientId.equals(ClientHandoverConstatnts.DEFAULT_KEY));
        final String  custId           = isClientSpecific ? aClientId : "";

        new RetryProcessPoller(isClientSpecific, custId);
        new HandoverRetryReaper(isClientSpecific, custId);
        new ExpiredMessageLogger(isClientSpecific, custId);

        if (log.isDebugEnabled())
            log.debug("Process started started for the client: '" + aClientId + "'");
    }

    public List<BaseMessage> getMessages(
            String aClientId,
            int aBatchSize)
    {
        final List<BaseMessage>          list            = new ArrayList<>();
        final BlockingQueue<BaseMessage> deliveryObjects = kafkaDataMap.get(aClientId);

        if ((deliveryObjects == null) || deliveryObjects.isEmpty())
            return list;

        deliveryObjects.drainTo(list, aBatchSize);
        return list;
    }

    public void stopProcessStarters()
    {
        processStartersList.stream().forEach(ProcessStarter::stopMe);
    }

}
