package com.itextos.beacon.smpp.interfaces.sessionhandlers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppServerSession;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.smpputil.ISmppInfo;
import com.itextos.beacon.smpp.dboperations.DbBindOperation;
import com.itextos.beacon.smpp.interfaces.event.handlers.ItextosSmppSessionHandler;
import com.itextos.beacon.smpp.interfaces.sessionhandlers.objects.SessionRoundRobin;
import com.itextos.beacon.smpp.objects.SmppObjectType;
import com.itextos.beacon.smpp.objects.SmppRequestType;
import com.itextos.beacon.smpp.objects.bind.UnbindInfo;
import com.itextos.beacon.smpp.objects.inmem.InfoCollection;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;

public class RemoveSession
{

    private static final Log        log = LogFactory.getLog(RemoveSession.class);
    private final SmppServerSession mSession;
    private final SessionRoundRobin mSrr;

    RemoveSession(
            SmppServerSession aSession,
            SessionRoundRobin srrObj)
    {
        this.mSession = aSession;
        this.mSrr     = srrObj;
    }

    void insertIntoMemory(
            boolean aDirectDbInsert)
    {

        try
        {
            insertIntoUnbindRequest(mSession, mSrr, aDirectDbInsert);
        }
        catch (final Exception e)
        {
            log.error("Exception while inserting Unbind Info to table..", e);
        }
    }

    private static void insertIntoUnbindRequest(
            SmppServerSession session,
            SessionRoundRobin srr,
            boolean aDirectDbInsert)
    {
        if (log.isDebugEnabled())
            log.debug("insertIntoUnbindRequest() SessionRoundRobin : " + srr);

        if (srr == null)
            return;

        final ItextosSmppSessionHandler sessionEventHandler = srr.getSessionHandler(session);

        if (log.isDebugEnabled())
            log.debug("insertIntoUnbindRequest() sessionEventHandler : " + sessionEventHandler);

        if (sessionEventHandler == null)
            return;

        final List<ISmppInfo> lBindInfo = InfoCollection.getInstance().getObjects(SmppObjectType.BIND_INFO_VALID, 1000);

        if (!lBindInfo.isEmpty())
        {
            log.error("sessionUnbind() - Updating the bind db info to db.." + lBindInfo);
            CommonUtility.sleepForAWhile();
        }

        /*
         * final int bindFailuerError = sessionEventHandler.getBindFailureError();
         * if (bindFailuerError == 0)
         * {
         */
        final SmppBindType bindType      = session.getBindType();
        final String       systemId      = session.getConfiguration().getSystemId();
        final String       sourceIp      = session.getConfiguration().getHost();
        final String       bindId        = sessionEventHandler.getBindId();
        final String       clientId      = sessionEventHandler.getClientId();
        // final long bindTime = session.getBoundTime();
        final String       lThreadName   = sessionEventHandler.getSessionDetail().getThreadName();

        final String       lInstanceId   = SmppProperties.getInstance().getInstanceId();
        final int          instancePort  = SmppProperties.getInstance().getApiListenPort();

        final UnbindInfo   unBindinfoLog = new UnbindInfo(lInstanceId, clientId, SmppRequestType.UNBIND, bindType, bindId, CommonUtility.getApplicationServerIp(), instancePort, systemId, sourceIp,
                lThreadName);
        unBindinfoLog.setErrorcode(sessionEventHandler.getBindFailureError());

        unBindinfoLog.setBindTime(DateTimeUtility.getFormattedDateTime(sessionEventHandler.getSessionDetail().getBindTime(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
        unBindinfoLog.setBindDate(DateTimeUtility.getFormattedDateTime(sessionEventHandler.getSessionDetail().getBindDate(), DateTimeFormat.DEFAULT_DATE_ONLY));

        if (!aDirectDbInsert)
            unBindinfoLog.setReason("Session Expired");
        else
            unBindinfoLog.setReason("Unbind Request By Client/ Network Unbind");

        if (log.isDebugEnabled())
            log.debug("Unbind Info Log : " + unBindinfoLog);
        boolean inserted = false;

        try
        {
            if (!aDirectDbInsert)
                inserted = InfoCollection.getInstance().addInfoObject(SmppObjectType.UNBIND_INFO_DB, unBindinfoLog);
        }
        catch (final Exception e)
        {
            log.error("Unable to add into inmemory.., Hence insert into DB", e);
        }

        if (!inserted)
        {
            /*
             * final List<ISmppInfo> bindLogList = new ArrayList<>();
             * bindLogList.add(unBindinfoLog);
             * try
             * {
             * DbBindOperation.insertUnBindInfo(bindLogList);
             * }
             * catch (final Exception e1)
             * {
             * log.error("Exception while inserting Unbind Request...", e1);
             * }
             */

            // Make a separate thread for the Direct DB Insert
            if (log.isDebugEnabled())
                log.debug("Insert the record using a separate thread to avoid the interruptted exception");

            /*
            new Thread((Runnable) () -> {
                final List<ISmppInfo> bindLogList = new ArrayList<>();
                bindLogList.add(unBindinfoLog);

                try
                {
                    DbBindOperation.insertUnBindInfo(bindLogList);
                }
                catch (final Exception e1)
                {
                    log.error("Exception while inserting Unbind Request...", e1);
                }
            }, "Thread Db Insert").start();
            */
           Thread t= Thread.startVirtualThread((Runnable) () -> {
                final List<ISmppInfo> bindLogList = new ArrayList<>();
                bindLogList.add(unBindinfoLog);

                try
                {
                    DbBindOperation.insertUnBindInfo(bindLogList);
                }
                catch (final Exception e1)
                {
                    log.error("Exception while inserting Unbind Request...", e1);
                }
            });
           t.setName("Thread Db Insert");
        }
    }

}
