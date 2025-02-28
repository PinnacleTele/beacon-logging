package com.itextos.beacon.smpp.interfaces;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerConfiguration;
import com.cloudhopper.smpp.SmppServerHandler;
import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.itextos.beacon.smpp.interfaces.event.handlers.ItextosBindUnbindHandler;
import com.itextos.beacon.smpp.interfaces.event.handlers.ItextosSmppServerHandler;
import com.itextos.beacon.smpp.interfaces.util.counters.ClientCounter;
import com.itextos.beacon.smpp.utils.ItextosSmppConstants;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;
import com.itextos.beacon.smslog.SmppServerLog;

public class ItextosSmppServer
{

    private static final Log log = LogFactory.getLog(ItextosSmppServer.class);

    private static class SingletonHolder
    {

        static final ItextosSmppServer INSTANCE = new ItextosSmppServer();

    }

    public static ItextosSmppServer getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private DefaultSmppServer mDefaultSmppServer = null;
    private SmppServerHandler mSmppServerHandler = null;
    private long              mStartUpTime       = 0;

    private ItextosSmppServer()
    {}

    public void destroy()
    {}

    public DefaultSmppServer getSmppServer()
    {
        return mDefaultSmppServer;
    }

    public ItextosSmppServerHandler getSmppServerHandler()
    {
        return (ItextosSmppServerHandler) mSmppServerHandler;
    }

    public long getStartUpTime()
    {
        return mStartUpTime;
    }

    public void setStartUpTime()
    {
        this.mStartUpTime = System.currentTimeMillis();
    }

    public void start()
            throws SmppChannelException
    {
        setStartUpTime();

        final SmppServerConfiguration     configuration   = getSmppServerConfigurations();
        final ThreadPoolExecutor          executor        = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        final ScheduledThreadPoolExecutor monitorExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, getThreadFactory());

        mSmppServerHandler = new ItextosSmppServerHandler(new ItextosBindUnbindHandler());
        mDefaultSmppServer = new DefaultSmppServer(configuration, mSmppServerHandler, executor, monitorExecutor);

        if (log.isInfoEnabled())
            log.info("Starting SMPP server...");
        
        SmppServerLog.log("Starting SMPP server...");

        final String lClientId = SmppProperties.getInstance().getTraceMonitorClientId();
        ClientCounter.getInstance().init(lClientId);

        this.mDefaultSmppServer.start();

        if (log.isInfoEnabled())
            log.info("SMPP server started");
        
        
        SmppServerLog.log("SMPP server started");

    }

    public void stop()
    {
        if (log.isInfoEnabled())
            log.info("Stopping SMPP server...");

        this.mDefaultSmppServer.stop();

        if (log.isInfoEnabled())
        {
            log.info("Stoped SMPP server....");
            log.info(String.format("Server counters: %s", this.mDefaultSmppServer.getCounters()));
        }
    }

    private static SmppServerConfiguration getSmppServerConfigurations()
    {
        final int apiListenPort           = SmppProperties.getInstance().getApiListenPort();
        final int apiBindTimeout          = SmppProperties.getInstance().getApiBindTimeout();
        final int apiMaxConnectionSize    = SmppProperties.getInstance().getApiMaxConnections();
        final int apiWindowSize           = SmppProperties.getInstance().getApiWindowSize();
        final int apiRequestExpiryTimeout = SmppProperties.getInstance().getApiRequestTimeout();
        final int dnRequestTimeout        = SmppProperties.getInstance().getApiDnReqTimeout();

        if (log.isDebugEnabled())
        {
            log.debug("API Listen port                    " + apiListenPort);
            log.debug("API Bind Request timeout in millis " + apiBindTimeout);
            log.debug("Smpp Interface allow max binds     " + apiMaxConnectionSize);
            log.debug("Smpp Window size                   " + apiWindowSize);
            log.debug("API Request expiry time in millis  " + apiRequestExpiryTimeout);
            log.debug("DN Request expiry time in millis   " + dnRequestTimeout);
        }

        final SmppServerConfiguration configuration = new SmppServerConfiguration();
        configuration.setPort(apiListenPort);
        configuration.setBindTimeout(apiBindTimeout);
        configuration.setMaxConnectionSize(apiMaxConnectionSize);
        configuration.setDefaultRequestExpiryTimeout(apiRequestExpiryTimeout);
        configuration.setDefaultWindowMonitorInterval(dnRequestTimeout);
        configuration.setDefaultWindowSize(apiWindowSize);
        configuration.setDefaultWindowWaitTimeout(apiRequestExpiryTimeout * 2L);

        configuration.setSystemId(ItextosSmppConstants.DEFAULT_SYSTEMID);
        configuration.setAutoNegotiateInterfaceVersion(ItextosSmppConstants.DEFAULT_AUTO_NEGOTIATE_INTERFACE_VERSION);
        configuration.setNonBlockingSocketsEnabled(false);
        configuration.setDefaultSessionCountersEnabled(ItextosSmppConstants.DEFAULT_SESSION_COUNTERS_ENABLED);
        configuration.setInterfaceVersion(SmppConstants.VERSION_3_4);

        return configuration;
    }

    private static ThreadFactory getThreadFactory()
    {
        return new ThreadFactory()
        {

            private final AtomicInteger sequence = new AtomicInteger(0);

            @Override
            public Thread newThread(
                    Runnable r)
            {
            	/*
                final Thread t = new Thread(r);
                t.setName("SmppServerSessionWindowMonitorPool-" + sequence.getAndIncrement());
                */
            	 Thread t = Thread.ofVirtual().unstarted(r);

                 t.setName( "SmppServerSessionWindowMonitorPool-" + sequence.getAndIncrement());
            	return t;
            }

        };
    }

    public boolean isServerStopped()
    {

        if (mDefaultSmppServer != null)
        {
            final boolean isSmppServerStopped   = mDefaultSmppServer.isStopped();
            final boolean isSmppServerDestroyed = mDefaultSmppServer.isDestroyed();

            log.info("Smpp Server Stopped Status : " + isSmppServerStopped);
            log.info("Smpp Server Destroyed Status :" + isSmppServerDestroyed);
            return isSmppServerStopped;
        }
        return true;
    }

    public void shutdownInitiated()
    {
        if (mSmppServerHandler != null)
            ((ItextosSmppServerHandler) mSmppServerHandler).shutdownInitiated();
    }

}