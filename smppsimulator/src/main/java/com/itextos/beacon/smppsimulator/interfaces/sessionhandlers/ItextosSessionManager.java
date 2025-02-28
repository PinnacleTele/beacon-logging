package com.itextos.beacon.smppsimulator.interfaces.sessionhandlers;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppServerSession;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.itextos.beacon.smppsimulator.interfaces.event.handlers.ItextosSmppSessionHandler;
import com.itextos.beacon.smppsimulator.interfaces.sessionhandlers.objects.SessionRoundRobin;
import com.itextos.beacon.smppsimulator.interfaces.util.counters.BindUnbindCounter;
import com.itextos.beacon.smppsimulator.interfaces.util.counters.LiveSessionsCounter;
import com.itextos.beacon.smppsimulator.interfaces.util.counters.SessionCounter;
import com.itextos.beacon.smppsimulator.objects.SessionCounterStats;
import com.itextos.beacon.smppsimulator.objects.SmppObjectType;
import com.itextos.beacon.smppsimulator.objects.bind.UnbindInfoRedis;
import com.itextos.beacon.smppsimulator.objects.counters.ServerRequestCounter;
import com.itextos.beacon.smppsimulator.objects.inmem.InfoCollection;
import com.itextos.beacon.smppsimulator.redisoperations.RedisBindOperation;
import com.itextos.beacon.smppsimulator.redisoperations.SessionInfoRedisUpdate;
import com.itextos.beacon.smppsimulator.utils.AccountDetails;
import com.itextos.beacon.smppsimulator.utils.properties.SmppProperties;

public class ItextosSessionManager
{

    private static final Log     log                = LogFactory.getLog(ItextosSessionManager.class);

    private static final boolean ALLOW_DLR_SESSIONS = SmppProperties.getInstance().isAllowDlrSessions();

    private static class SingletonHolder
    {

        static final ItextosSessionManager INSTANCE = new ItextosSessionManager();

    }

    public static ItextosSessionManager getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<String, SessionRoundRobin>  txSessionsMap    = new ConcurrentHashMap<>();
    private final Map<String, SessionRoundRobin>  rxTrxSessionsMap = new ConcurrentHashMap<>();
    private final Map<String, LinkedList<Thread>> dnWorkerMap      = new ConcurrentHashMap<>();

    private ItextosSessionManager()
    {}

    public SessionRoundRobin getSessionRoundRobin(
            SmppServerSession session)
    {
        if (session.getBindType().equals(SmppBindType.TRANSMITTER))
            return txSessionsMap.get(session.getConfiguration().getSystemId().toLowerCase());
        return rxTrxSessionsMap.get(session.getConfiguration().getSystemId().toLowerCase());
    }

    public int removeExpiredSessions(
            long aInactiveInterval)
    {
        int                                 lTotalRemovedSessions = 0;
        final Collection<SessionRoundRobin> col1                  = txSessionsMap.values();
        final Collection<SessionRoundRobin> col2                  = rxTrxSessionsMap.values();

        for (final SessionRoundRobin srr : col1)
            lTotalRemovedSessions += srr.unbindExpired(aInactiveInterval);

        for (final SessionRoundRobin srr : col2)
            lTotalRemovedSessions += srr.unbindExpired(aInactiveInterval);

        return lTotalRemovedSessions;
    }

    public void checkDisabledAccounts()
    {
        checkForTxBoundUsers();
        checkForRxTrxBoundUsers();
    }

    private void checkForRxTrxBoundUsers()
    {
        removeDisabledUser(rxTrxSessionsMap.values());
    }

    private void checkForTxBoundUsers()
    {
        removeDisabledUser(txSessionsMap.values());
    }

    private static void removeDisabledUser(
            Collection<SessionRoundRobin> aCollection)
    {

        for (final SessionRoundRobin srr : aCollection)
        {
            final List<ItextosSmppSessionHandler> lSessionHandlers = srr.getSessionHandlers();
            final List<ItextosSmppSessionHandler> tempHandler      = new ArrayList<>(lSessionHandlers);

            for (final ItextosSmppSessionHandler handler : tempHandler)
            {
                if (log.isDebugEnabled())
                    log.debug(handler.getSystemId() + " checking account status...");

                final String   systemId = handler.getSystemId();
                final UserInfo userInfo = AccountDetails.getUserInfo(systemId);

                if (userInfo.getStatus() != 0)
                {
                    log.error("account bound after disabling going to unbind..." + handler.getSystemId());
                    handler.forceUnbind();
                }
            }
        }
    }

    public void listClients(
            BufferedReader aBr,
            PrintWriter aOut)
    {
        listTxClients(aBr, aOut);
        listRxTrxClients(aBr, aOut);
    }

    private void listRxTrxClients(
            BufferedReader aBr,
            PrintWriter aOut)
    {
        final Set<String> keySet = rxTrxSessionsMap.keySet();
        for (final String user : keySet)
            aOut.println(user + " RX/TRX active " + rxTrxSessionsMap.get(user).getHandlersCount());
    }
    
    
    public Map<String,String> getBindDetails(){
    	
    	
    	Map<String,String> result=new HashMap<String,String>();
    	
        final Set<String> keySet = rxTrxSessionsMap.keySet();
        for (final String user : keySet)
        	result.put(user + " RX/TRX active " ,""+ rxTrxSessionsMap.get(user).getHandlersCount());
  
        final Set<String> keySet2 = txSessionsMap.keySet();
        
        for (final String user : keySet)
        	result.put(user + " TR active " ,""+ rxTrxSessionsMap.get(user).getHandlersCount());
 
        return result;
    }

    private void listTxClients(
            BufferedReader aBr,
            PrintWriter aOut)
    {
        final Set<String> keySet = txSessionsMap.keySet();
        for (final String user : keySet)
            aOut.println(user + " TX active " + txSessionsMap.get(user).getHandlersCount());
    }

    public Map<String, SessionRoundRobin> getRxTrxSessionCounts()
    {
        return rxTrxSessionsMap;
    }

    public void addSessionHandler(
            ItextosSmppSessionHandler aSessionHandler)
            throws Exception
    {
        final String                   systemId    = aSessionHandler.getSystemId();
        Map<String, SessionRoundRobin> sessionsMap = null;

        if (aSessionHandler.getBindType().equals(SmppBindType.TRANSMITTER))
            sessionsMap = txSessionsMap;
        else
            sessionsMap = rxTrxSessionsMap;

        if (!aSessionHandler.getBindType().equals(SmppBindType.TRANSMITTER) && ALLOW_DLR_SESSIONS)
        {
          
        }

        final SessionRoundRobin srrObj = sessionsMap.computeIfAbsent(systemId, k -> createNewSessionRR(aSessionHandler));
        srrObj.addSession(aSessionHandler);
    }

    private SessionRoundRobin createNewSessionRR(
            ItextosSmppSessionHandler aSessionHandler)
    {
        final SessionRoundRobin srrObj = new SessionRoundRobin();

        if (!aSessionHandler.getBindType().equals(SmppBindType.TRANSMITTER) && !ALLOW_DLR_SESSIONS)
        {
           
        }
        return srrObj;
    }

    public boolean removeSession(
            boolean aDirectDbInsert,
            Long aSessionId,
            SmppServerSession aSession)
            throws Exception
    {
        final String                         systemId    = aSession.getConfiguration().getSystemId().toLowerCase();

        final Map<String, SessionRoundRobin> sessionsMap = (aSession.getBindType().equals(SmppBindType.TRANSMITTER)) ? txSessionsMap : rxTrxSessionsMap;
        final SessionRoundRobin              srrObj      = sessionsMap.get(systemId);
        boolean                              removed     = false;

        if (log.isDebugEnabled())
            log.debug(systemId + " Removing session Id : " + aSessionId);

        if (srrObj != null)
        {
            final boolean isDBInsertRequired = SmppProperties.getInstance().isDbInsertRequired();

            if (log.isDebugEnabled())
                log.debug("Is DB insert required - '" + isDBInsertRequired + "'");

            if (isDBInsertRequired)
            {
                final RemoveSession lRemoveSession = new RemoveSession(aSession, srrObj);
                lRemoveSession.insertIntoMemory(aDirectDbInsert);
            }

            removed = srrObj.removeSession(aSession);

            if (log.isInfoEnabled())
                log.info("Session removed for systemid '" + systemId + "' is '" + removed + "', HandlersCount :'" + srrObj.getHandlersCount() + "'");

            if (srrObj.getHandlersCount() == 0)
            {
                sessionsMap.remove(systemId);

                if (log.isDebugEnabled())
                    log.debug("Checking for Customer redis DLR Queue Worker for remove..." + systemId);

                if (dnWorkerMap.containsKey(systemId) && !aSession.getBindType().equals(SmppBindType.TRANSMITTER)) // && !ALLOW_DLR_SESSIONS
                {
                    final LinkedList<Thread> lLinkedList = dnWorkerMap.get(systemId);

                  
                    dnWorkerMap.remove(systemId);
                }
            }

            if (removed)
                removeSessionsFromRedis(aSessionId, aSession);
        }
        return removed;
    }

    public static void removeSessionsFromRedis(
            Long aSessionId,
            SmppServerSession aSession)
            throws Exception
    {
        final String   instanceId = SmppProperties.getInstance().getInstanceId();
        final String   systemId   = aSession.getConfiguration().getSystemId().toLowerCase();
        final UserInfo userInfo   = AccountDetails.getUserInfo(systemId);
        final String   clientId   = userInfo.getClientId();

        final boolean  isDnAllow  = (aSession.getBindType() == SmppBindType.TRANSCEIVER) || (aSession.getBindType() == SmppBindType.RECEIVER);

        decRedisCounters(new UnbindInfoRedis(clientId, instanceId, systemId, isDnAllow));
        decTransRedisCounter(new UnbindInfoRedis(clientId, instanceId, systemId, isDnAllow));

        LiveSessionsCounter.getInstance().removeCounter(aSessionId);

        final ServerRequestCounter counter = ServerRequestCounter.getInstance();
        counter.setDeliverSm(counter.getDeliverSm() + aSession.getCounters().getTxDeliverSM().getRequest());
        counter.setDeliverSmResp(counter.getDeliverSmResp() + aSession.getCounters().getTxDeliverSM().getResponse());
        counter.setEnquireLink(counter.getEnquireLink() + aSession.getCounters().getRxEnquireLink().getResponse());
        counter.setSubmitSm(counter.getSubmitSm() + aSession.getCounters().getRxSubmitSM().getRequest());
        counter.setSubmitSmResp(counter.getSubmitSmResp() + aSession.getCounters().getRxSubmitSM().getResponse());

        if (log.isInfoEnabled())
            log.info("Session destroyed " + aSessionId);
    }

    public static void decRedisCounters(
            UnbindInfoRedis aUnbindInfoRedis)
    {

        try
        {
            RedisBindOperation.decreaseBindCount(aUnbindInfoRedis.getClientId(), aUnbindInfoRedis.getInstanceId());
        }
        catch (final Exception exp)
        {
            InfoCollection.getInstance().addInfoObject(SmppObjectType.UNBIND_INFO_REDIS, aUnbindInfoRedis);
        }

        BindUnbindCounter.getInstance().addUnBind(aUnbindInfoRedis.getSystemId());
    }

    public static void decTransRedisCounter(
            UnbindInfoRedis aUnbindInfoRedis)
    {
        if (log.isDebugEnabled())
            log.debug("Session remove calling from 'removeSessions()..");

        try
        {
            SessionInfoRedisUpdate.decreaseTransactionBindCount(aUnbindInfoRedis.getClientId(), aUnbindInfoRedis.getInstanceId(), aUnbindInfoRedis.isDn());
        }
        catch (final Exception exp)
        {
            InfoCollection.getInstance().addInfoObject(SmppObjectType.UNBIND_TRANS_INFO_REDIS, aUnbindInfoRedis);
        }
    }

   
    public ItextosSmppSessionHandler getAvailableSession(
            String aSystemId)
            throws ItextosException
    {
        final SessionRoundRobin   srrObj          = rxTrxSessionsMap.get(aSystemId);
        ItextosSmppSessionHandler aSessionHandler = null;

        if (srrObj != null)
            aSessionHandler = srrObj.getAvailableSession();
        else
            throw new ItextosException("No bind exists-" + aSystemId);

        return aSessionHandler;
    }

    public Map<Long, SessionCounter> getRxTrxStatistics()
    {
        return getRxTrxStatistics(null);
    }

    public Map<Long, SessionCounter> getRxTrxStatistics(
            Map<Long, SessionCounter> aExistingStatsMap)
    {
        final Map<Long, SessionCounter> result = new HashMap<>();

        for (final Entry<String, SessionRoundRobin> entry : rxTrxSessionsMap.entrySet())
        {
            final List<ItextosSmppSessionHandler> handlerList = entry.getValue().getSessionHandlers();

            for (final ItextosSmppSessionHandler handler : handlerList)
            {
                SessionCounter counter = null;
                if (aExistingStatsMap != null)
                    counter = aExistingStatsMap.get(handler.getSessionId());

                if (counter == null)
                    counter = new SessionCounter(entry.getKey(), handler.getSessionId());
                else
                    counter = counter.clone();

                final SessionCounterStats sessionCounterStatistics = handler.getSessionCounterStatistics();
                counter.addRequest(sessionCounterStatistics.getTxDeliverSmRequest());
                counter.addResponse(sessionCounterStatistics.getTxDeliverSmResponse());
                counter.addExpired(sessionCounterStatistics.getTxDeliverSmRequestExpired());
                counter.setAvgResponseTime(sessionCounterStatistics.getTxDeliverSmRequestResponseTime());
                counter.setAvgWaitTime(sessionCounterStatistics.getTxDeliverSmRequestWaitTime());

                result.put(handler.getSessionId(), counter);
            }
        }
        return result;
    }

}