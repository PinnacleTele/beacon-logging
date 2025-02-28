package com.itextos.beacon.smppsimulator.interfaces.sessionhandlers.objects;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.smpp.SmppServerSession;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.smppsimulator.interfaces.event.handlers.ItextosSmppSessionHandler;
import com.itextos.beacon.smppsimulator.interfaces.sessionhandlers.ItextosSessionManager;

public class SessionRoundRobin
{

    private static final Log                                      log             = LogFactory.getLog(SessionRoundRobin.class);

    private final AtomicInteger                                   availableIndex  = new AtomicInteger(-1);
    private final CopyOnWriteArrayList<ItextosSmppSessionHandler> sessionHandlers = new CopyOnWriteArrayList<>();

    public List<ItextosSmppSessionHandler> getSessionHandlers()
    {
        return sessionHandlers;
    }

    public synchronized void addSession(
            ItextosSmppSessionHandler aSessionHandler)
            throws Exception
    {
        if (aSessionHandler != null)
            sessionHandlers.add(aSessionHandler);
    }

    public ItextosSmppSessionHandler getAvailableSession()
            throws ItextosException
    {
        final List<ItextosSmppSessionHandler> temp = new ArrayList<>(sessionHandlers);

        if (temp.isEmpty())
            throw new ItextosException("No bind exists...");

        int index = availableIndex.addAndGet(1);

        if (index > (temp.size() - 1))
        {
            availableIndex.set(0);
            index = 0;
        }

        if (log.isInfoEnabled())
            log.info("returning session from index>>>" + index + " for size=" + temp.size());

        ItextosSmppSessionHandler toReturn = temp.get(index);

        if (toReturn.getSendWindowSize() < toReturn.getWindowSize())
        {
            toReturn.setInUse(true);
            return toReturn;
        }

        for (final ItextosSmppSessionHandler handler : temp)
            if (!handler.isInUse())
            {
                toReturn = handler;
                handler.setInUse(true);
                return toReturn;
            }
        return null;
    }

    public synchronized boolean removeSession(
            SmppServerSession session)
    {
        boolean removed = false;

        if (session != null)
        {
            final List<ItextosSmppSessionHandler> temp = new ArrayList<>(sessionHandlers);

            for (final ItextosSmppSessionHandler handler : temp)
            {

                if (log.isDebugEnabled())
                {
                    log.debug("sessionHandlers Map     - " + sessionHandlers);
                    log.debug("sessionHandlers Session - " + handler.getSessionId());
                    log.debug("session val             - " + session);
                    log.debug("Bind Id                 - " + handler.getBindId());
                }

                if (handler.isSameSession(session))
                {
                    removed = sessionHandlers.remove(handler);
                    handler.clearWaitingDn();
                    break;
                }
            }
        }
        return removed;
    }

    public ItextosSmppSessionHandler getSessionHandler(
            SmppServerSession session)
    {

        if (session != null)
        {
            final List<ItextosSmppSessionHandler> temp = new ArrayList<>(sessionHandlers);

            for (final ItextosSmppSessionHandler handler : temp)
                if (handler.isSameSession(session))
                    return handler;
        }
        return null;
    }

    public int unbindExpired(
            long aExpiryTime)
    {
        int                                         unboundCount = 0;
        final LinkedList<ItextosSmppSessionHandler> unbindList   = new LinkedList<>();
        final List<ItextosSmppSessionHandler>       temp         = new ArrayList<>(sessionHandlers);

        for (final ItextosSmppSessionHandler handler : temp)
        {

            if (log.isDebugEnabled())
            {
                log.debug("looping systemid=" + handler.getSystemId());
                log.debug("for ExpiryTime=" + aExpiryTime + " :  lastUsed=" + handler.getLastUsedTime());
                log.debug("System.currentTimeMillis()-anHandler.getLastUsedTime().getTime()=" + (System.currentTimeMillis() - handler.getLastUsedTime().getTime()));
            }

            if (handler.isSessionClosed() || (aExpiryTime <= (System.currentTimeMillis() - handler.getLastUsedTime().getTime())))
                unbindList.add(handler);
        }

        for (final ItextosSmppSessionHandler handler : unbindList)
            try
            {
                handler.setExpired();
                handler.updateAndResetCounters();
                handler.closeSession();
                handler.destroySession();
            }
            catch (final Exception e)
            {
                log.error("Exception while closing and destroying session.", e);
            }
            finally
            {

                try
                {
                    ItextosSessionManager.getInstance().removeSession(false, handler.getSessionId(), handler.getSession());
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
            }

        unboundCount = unbindList.size();

        return unboundCount;
    }

    public void resetCounters()
    {
        for (final ItextosSmppSessionHandler handler : sessionHandlers)
            handler.resetCounters();
    }

    public int getHandlersCount()
    {
        return sessionHandlers.size();
    }

}