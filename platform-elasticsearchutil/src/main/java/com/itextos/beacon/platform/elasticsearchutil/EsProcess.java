package com.itextos.beacon.platform.elasticsearchutil;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.RestHighLevelClient;

import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.platform.elasticsearchutil.data.R3Info;
import com.itextos.beacon.platform.elasticsearchutil.types.DlrQueryMulti;
import com.itextos.beacon.platform.elasticsearchutil.types.EsSortOrder;

public class EsProcess
        implements
        ITimedProcess
{

    private static final Log log = LogFactory.getLog(EsProcess.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final EsProcess INSTANCE = new EsProcess();

    }

    public static EsProcess getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final TimedProcessor          mTimedProcessor;
    private boolean                       canContinue             = true;
    private final Map<Long, EsConnection> mThreadBasedRestClients = new ConcurrentHashMap<>();
    private final Map<Long, Kafka2ElasticSearchEsConnection> mThreadBasedRestClientsKafka2ElasticSearch = new ConcurrentHashMap<>();


    private EsProcess()
    {
    	
        mTimedProcessor = new TimedProcessor("ESConnectionReaper", this, TimerIntervalConstant.ELASTIC_SEARCH_CONNECTION_REAPER);
     
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "ESConnectionReaper");
    }

    public static boolean insertSingleDn(
            DeliveryObject aDeliveryObject)
    {
        return SingleDnProcess.insert(aDeliveryObject);
    }

    public static Map<MiddlewareConstant, String> getSingleDn(
            String aClientId,
            String aBaseMessageId,
            MiddlewareConstant aSortbasedOn,
            EsSortOrder aSearchOrder)
    {
        return SingleDnProcess.get(aClientId, aBaseMessageId, aSortbasedOn, aSearchOrder);
    }

    public static boolean deleteSingleDn(
            DeliveryObject aDeliveryObject)
    {
        final String clientId      = aDeliveryObject.getValue(MiddlewareConstant.MW_CLIENT_ID);
        final String baseMessageId = aDeliveryObject.getValue(MiddlewareConstant.MW_BASE_MESSAGE_ID);
        final String messageId     = aDeliveryObject.getValue(MiddlewareConstant.MW_MESSAGE_ID);

        return deleteSingleDn(clientId, baseMessageId, messageId);
    }

    public static boolean deleteSingleDn(
            String aClientId,
            String aBaseMessageId)
    {
        return deleteSingleDn(aClientId, aBaseMessageId, null);
    }

    public static boolean deleteSingleDn(
            String aClientId,
            String aBaseMessageId,
            String aMessageId)
    {
        return SingleDnProcess.deleteSingleDn(aClientId, aBaseMessageId, aMessageId);
    }

    public static void insertDlrQuerySub(
            SubmissionObject aSubmissionObject)
            throws Exception
    {
        DlrQuery.insertDlrQuerySub(aSubmissionObject);
    }

    public static void insertDlrQueryDn(
            DeliveryObject aDeliveryObject)
            throws Exception
    {
        DlrQuery.insertDlrQueryDn(aDeliveryObject);
    }

    public static List<Map<MiddlewareConstant, String>> getDlrQueryInfo(
            DlrQueryMulti aRequest)
    {
        return DlrQuery.get(aRequest);
    }

    public static void insertAgingDn(
            SubmissionObject aSubmissionObject)
    {
        // TODO in Second Phase
        System.err.println("TODO in Second Phase");
        // AgingDn.insertAgingDn(aSubmissionObject);
    }

    public static void updateAgingDn(
            DeliveryObject aDeliveryObject)
    {
        // TODO in Second Phase
        System.err.println("TODO in Second Phase");
        // AgingDn.updateAgingDn(aDeliveryObject);
    }

    public static void deleteAgingDn(
            BaseMessage aBaseMessage)
    {
        // TODO in Second Phase
        System.err.println("TODO in Second Phase");
        // AgingDn.deleteAgingDn(aBaseMessage);
    }

    public static void insertR3Message(
            R3Info aR3Info)
            throws Exception
    {
        R3Message.insertR3Message(aR3Info);
    }

    public static Map<String, Object> getShortCodeData(
            String shortCode)

    {
        return R3Message.getShortCodeData(shortCode);
    }

    public RestHighLevelClient getEsConnection()
    {
        final long         threadId  = Thread.currentThread().getId();
        final EsConnection conection = mThreadBasedRestClients.computeIfAbsent(threadId, k -> new EsConnection(threadId));
        return conection.getConnection();
    }

    
    public RestHighLevelClient getKafka2ElasticSearchEsConnection()
    {
        final long         threadId  = Thread.currentThread().getId();
        final Kafka2ElasticSearchEsConnection conection = mThreadBasedRestClientsKafka2ElasticSearch.computeIfAbsent(threadId, k -> new Kafka2ElasticSearchEsConnection(threadId));
        return conection.getConnection();
    }
    
    public void updateLastUsed()
    {

        try
        {
            final long         threadId  = Thread.currentThread().getId();
            final EsConnection conection = mThreadBasedRestClients.get(threadId);
            if (conection == null)
                throw new Exception("Something is not right here. Unable to find the Elasticsearch client for the thread '" + threadId + "'");
            conection.updateLastUsed();
        }
        catch (final Exception e)
        {
            log.error(e);
        }
    }

    private void checkAndCloseClient()
    {

        for (final Entry<Long, EsConnection> entry : mThreadBasedRestClients.entrySet())
        {
            if (log.isDebugEnabled())
                log.debug("Checking the connection for the thread id '" + entry.getKey() + "'");

            entry.getValue().checkAndCloseClient();
        }
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        checkAndCloseClient();
        return false;
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}