package com.itextos.smpp.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.EnquireLinkResp;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.itextos.smpp.client.pojo.BindRequest;
import com.itextos.smpp.client.util.SmppClientProperties;
import com.itextos.smpp.client.util.SmppClientUtil;

public class SmppClient
{

    private static final Log log = LogFactory.getLog(SmppClient.class);

    public static void main(
            String[] args)
    {

        try
        {
            SmppClient.doProcess();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void doProcess()
            throws Exception
    {
        final List<SmppSession> aSession = serverConfig();
        generateSMRequest(aSession);
    }

    private static List<SmppSession> serverConfig()
            throws Exception
    {
        final ScheduledThreadPoolExecutor monitorExecutor = getMonitorExecutor();
        final DefaultSmppClient           clientBootstrap = new DefaultSmppClient(Executors.newCachedThreadPool(), 1, monitorExecutor);
        final DefaultSmppSessionHandler   sessionHandler  = new ClientSmppSessionHandler();
        // final DefaultSmppSessionHandler sessionHandler = new
        // DefaultSmppSessionHandler();
        final SmppSessionConfiguration    sessionConfig   = getSessionConfiguration();
        return bindRequest(sessionHandler, sessionConfig, clientBootstrap);
    }

    private static ScheduledThreadPoolExecutor getMonitorExecutor()
    {
        return (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, new ThreadFactory()
        {

            private final AtomicInteger sequence = new AtomicInteger(0);

            @Override
            public Thread newThread(
                    Runnable r)
            {
                final Thread t = new Thread(r);
                t.setName("SmppClientSessionWindowMonitorPool-" + sequence.getAndIncrement());
                return t;
            }

        });
    }

    private static SmppSessionConfiguration getSessionConfiguration()
    {
        final BindRequest              lBindRequest              = SmppClientUtil.getBindRequest();
        final SmppSessionConfiguration lSmppSessionConfiguration = new SmppSessionConfiguration();

        lSmppSessionConfiguration.setWindowSize(lBindRequest.getWindowSize());
        lSmppSessionConfiguration.setName("Tester.Session.0");
        lSmppSessionConfiguration.setType(SmppClientUtil.getBindType(lBindRequest.getBindType()));
        lSmppSessionConfiguration.setHost(lBindRequest.getHost());
        lSmppSessionConfiguration.setPort(lBindRequest.getPort());
        lSmppSessionConfiguration.setSystemId(lBindRequest.getSystemId());
        lSmppSessionConfiguration.setPassword(lBindRequest.getPassword());
        lSmppSessionConfiguration.setConnectTimeout(10000);
        lSmppSessionConfiguration.getLoggingOptions().setLogBytes(true);
        lSmppSessionConfiguration.setRequestExpiryTimeout(10000);
        lSmppSessionConfiguration.setWindowMonitorInterval(15000);
        lSmppSessionConfiguration.setCountersEnabled(true);
        return lSmppSessionConfiguration;
    }

    private static List<SmppSession> bindRequest(
            DefaultSmppSessionHandler aDefaultSmppSessionHandler,
            SmppSessionConfiguration sessionConfig,
            DefaultSmppClient aDefaultSmppClient)
            throws Exception
    {
        final int               count        = SmppClientProperties.getInstance().getSessionBindCounts();
        final List<SmppSession> smppSessions = new ArrayList<>(count);

        for (int index = 0; index < count; index++)
            try
            {
                final SmppSession                                    smppSession  = aDefaultSmppClient.bind(sessionConfig, aDefaultSmppSessionHandler);
                final WindowFuture<Integer, PduRequest, PduResponse> windowFuture = smppSession.sendRequestPdu(new EnquireLink(), 10000, false);

                if (!windowFuture.await())
                    log.error("Failed to receive enquire_link_resp within specified time");
                else
                    if (windowFuture.isSuccess())
                    {
                        final EnquireLinkResp enquireLinkResp2 = (EnquireLinkResp) windowFuture.getResponse();
                        log.debug(enquireLinkResp2);
                    }
                    else
                        log.error("Failed to properly receive enquire_link_resp: " + windowFuture.getCause());

                log.debug("Successfully bind the account .." + smppSession.getConfiguration().getSystemId());

                smppSessions.add(smppSession);
            }
            catch (final Exception e)
            {
                log.error("Exception while binding", e);
            }
        log.fatal("Bound session count " + smppSessions.size());
        return smppSessions;
    }

    private static void generateSMRequest(
            List<SmppSession> aSessionList)
    {
        int sessionCount = 0;

        for (final SmppSession smppSession : aSessionList)
        {
            log.debug("Starting sending message with session " + (++sessionCount));
            final Thread t = new Thread(new SendMessageToServer(smppSession), "Session " + sessionCount);
            t.start();
        }
    }

    private static void stopSession(
            SmppSession aSession)
    {
        log.debug("Going to unbind the request......");
        aSession.unbind(10000);
        log.debug("Unbid Successfully ...........");

        log.debug("Going to disconnect the service....");
        aSession.close();

        log.debug("Service closed successfully......");

        System.exit(-1);
    }

}