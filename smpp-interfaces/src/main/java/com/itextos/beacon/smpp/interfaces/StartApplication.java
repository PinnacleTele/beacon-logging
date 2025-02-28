package com.itextos.beacon.smpp.interfaces;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.smpp.type.SmppChannelException;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.messageidentifier.MessageIdentifier;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.http.interfacefallback.inmem.FallbackQReaper;
import com.itextos.beacon.platform.smpputil.ISmppInfo;
import com.itextos.beacon.smpp.concatenate.CompletedMessageChecker;
import com.itextos.beacon.smpp.concatenate.CompletedMessagePoller;
import com.itextos.beacon.smpp.concatenate.DbInmemoryCollectionFactory;
import com.itextos.beacon.smpp.concatenate.ExpiryMessageCollectionFactory;
import com.itextos.beacon.smpp.concatenate.OrphanExpiryMessageProcessor;
import com.itextos.beacon.smpp.dboperations.DbBindOperation;
import com.itextos.beacon.smpp.interfaces.admin.ItextosAdminServer;
import com.itextos.beacon.smpp.interfaces.inmemdrainer.BindInfoInvalidInmemDrainer;
import com.itextos.beacon.smpp.interfaces.inmemdrainer.BindInfoValidInmemDrainer;
import com.itextos.beacon.smpp.interfaces.inmemdrainer.UnbindInfoDbInmemDrainer;
import com.itextos.beacon.smpp.interfaces.monitor.Monitor;
import com.itextos.beacon.smpp.interfaces.shutdown.SmppShutdownhook;
import com.itextos.beacon.smpp.interfaces.timertasks.DisabledAccountCheckTask;
import com.itextos.beacon.smpp.interfaces.timertasks.IdleSessionRemoverTask;
import com.itextos.beacon.smpp.interfaces.timertasks.SessionCountUpdateTask;
import com.itextos.beacon.smpp.objects.SmppObjectType;
import com.itextos.beacon.smpp.objects.inmem.InfoCollection;
import com.itextos.beacon.smpp.redisoperations.RedisBindOperation;
import com.itextos.beacon.smpp.redisoperations.SessionInfoRedisUpdate;
import com.itextos.beacon.smpp.utils.AccountDetails;
import com.itextos.beacon.smpp.utils.SmppApplicationParams;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;
import com.itextos.beacon.smslog.SmppServerLog;

public class StartApplication
{

    private static final Log   log         = LogFactory.getLog(StartApplication.class);
    private ItextosAdminServer adminServer = null;

    public void start()
    {

        try
        {
            System.out.println("Entering start()");
            SmppServerLog.log("Entering start()");

            initialize();

            System.out.println("Entering after initialize()");

            SmppServerLog.log("Entering after initialize()");

            startServersAndInmemory();

            System.out.println("Entering after startServersAndInmemory()");

            SmppServerLog.log("Entering after startServersAndInmemory()");
            updateRedisRelatedEntries();

            System.out.println("Entering after updateRedisRelatedEntries()");
            
            SmppServerLog.log("Entering after updateRedisRelatedEntries()");


   //         startConcatenateAndExpiryProcessors();

            System.out.println("Entering after startConcatenateAndExpiryProcessors()");
            
            SmppServerLog.log("Entering after startConcatenateAndExpiryProcessors()");


            addShutdownhook();

            System.out.println("Entering after addShutdownhook()");

            SmppServerLog.log("Entering after addShutdownhook()");
            FallbackQReaper.getInstance();
            
            System.out.println("going to start Monitroing thread");
    //        new Monitor().start();
            System.out.println("Monitroing thread Started");

        }
        catch (final Exception exp)
        {
            log.error("Problem starting server...", exp);
        }
    }

    private static void initialize() throws ItextosRuntimeException
    {
        final String lInstanceId = SmppProperties.getInstance().getInstanceId();

        log.fatal("Smpp interface starting with Instance Id : '" + lInstanceId + "' on port '" + SmppProperties.getInstance().getApiListenPort() + "'");

        PrometheusMetrics.registerServer();
        PrometheusMetrics.registerSmppMetrics();

        final MessageIdentifier lMsgIdentifier = MessageIdentifier.getInstance();
        lMsgIdentifier.init(InterfaceType.SMPP);

        clearOldBindRecords(lInstanceId);

        loadAccountInfo();
    }

    private void startServersAndInmemory()
            throws SmppChannelException
    {
        startSmppServer();
        startInMemDrainers();
        startTimerBasedTasks();
   //     startAdminServer();
    }

    private static void startSmppServer()
            throws SmppChannelException
    {
        if (log.isDebugEnabled())
            log.debug("Starting the Smpp Server Instance ....");

        ItextosSmppServer.getInstance().start();
    }

    private static void updateRedisRelatedEntries()
    {
        final String lInstanceId = SmppProperties.getInstance().getInstanceId();

        updateRedisSessions(lInstanceId);
        RedisBindOperation.removeAllBindInfo(lInstanceId);
    }

    private static void loadAccountInfo()
    {
        log.fatal("Started Loading the accounts...");

        AccountDetails.loadAccounts();

        log.fatal("Completed Loading the accounts...");
    }

    private static void clearOldBindRecords(
            String aInstanceId)
    {
        if (log.isDebugEnabled())
            log.debug("Clearing the Bind records for the instance - '" + aInstanceId + "'");

        try
        {
            DbBindOperation.clearSmppBindInfo(aInstanceId);
        }
        catch (final Exception e)
        {
            log.error("Exception occer while executing  the clearing smpp_bind_info query..", e);
        }
    }

    private static void startConcatenateAndExpiryProcessors()
    {
        final boolean isConcatMessageProcessEnable = SmppProperties.getInstance().isConcatMessageProcessEnable();

        if (log.isDebugEnabled())
            log.debug("Can start Concat Message Process ? '" + isConcatMessageProcessEnable + "'");

        if (isConcatMessageProcessEnable)
        {
            if (log.isDebugEnabled())
                log.debug("Going to start Concat Message Process....");

            startCompletedMessageProcessor();
        }
    }

    private void addShutdownhook()
    {
        if (log.isDebugEnabled())
            log.debug("Adding Shutdown hook to the application.");

        final String lInstanceId = SmppProperties.getInstance().getInstanceId();
        Runtime.getRuntime().addShutdownHook(new SmppShutdownhook(this, lInstanceId));
    }

    private static void updateRedisSessions(
            String aInstanceId)
    {
        SessionInfoRedisUpdate.removeAllBindInfo(aInstanceId, false);
        SessionInfoRedisUpdate.removeAllBindInfo(aInstanceId, true);
    }

    private void startAdminServer()
    {
        if (log.isDebugEnabled())
            log.debug("Starting Admin Server ");

        adminServer = new ItextosAdminServer();
        /*
        final Thread adminServerThread = new Thread(adminServer, "SmppAdminServer");
        adminServerThread.start();
	*/
        Thread virtualThread = Thread.ofVirtual().start(adminServer);

        virtualThread.setName("SmppAdminServer");
        if (log.isInfoEnabled())
            log.info("Admin Server started successfully..");
    }

    private static void startTimerBasedTasks()
    {
        if (log.isDebugEnabled())
            log.debug("Starting Time Based Processors");

        new IdleSessionRemoverTask();
        new DisabledAccountCheckTask();
        new SessionCountUpdateTask();

        if (log.isInfoEnabled())
            log.info("Time based processors started");
    }

    private static void startInMemDrainers()
    {
        if (log.isDebugEnabled())
            log.debug("Starting inmemory drainner");

        BindInfoValidInmemDrainer.getInstance();
        BindInfoInvalidInmemDrainer.getInstance();

        UnbindInfoDbInmemDrainer.getInstance();
        DbInmemoryCollectionFactory.getInstance();

        if (log.isInfoEnabled())
            log.info("Inmemory drainners started");
    }

    private static void startCompletedMessageProcessor()
    {
        final int lConcatPoolerConsumerCount = SmppProperties.getInstance().getConcatMessagePoolerRedisConsumerCount();
        if (log.isDebugEnabled())
            log.debug("Concat Pooler Consumer Count : " + lConcatPoolerConsumerCount);

        for (final ClusterType lClusterType : SmppApplicationParams.getInstance().getClusters())
        {
            if (log.isDebugEnabled())
                log.debug("Cluster Value : " + lClusterType);

            final int lConcatRedisPool = RedisConnectionProvider.getInstance().getRedisPoolCount(lClusterType, Component.SMPP_CONCAT);
            if (log.isDebugEnabled())
                log.debug(lClusterType + ", Concat Redis Pool : " + lConcatRedisPool);

            final List<String> runtimeRedisIndex = getRedisIndices(lConcatRedisPool);

            for (int redisIndex = 0; redisIndex < lConcatRedisPool; redisIndex++)
            {
                if (log.isDebugEnabled())
                    log.debug(lClusterType + ", Concat Redis Pool Index:'" + redisIndex + "'");

                if (runtimeRedisIndex.contains("" + redisIndex))
                {
                    if (log.isDebugEnabled())
                        log.debug("Cluster : " + lClusterType + " :: Redis Index :" + redisIndex);

                    new CompletedMessageChecker(lClusterType, redisIndex);
                    ExpiryMessageCollectionFactory.getInstance().addExpiryProcessor(lClusterType, redisIndex);
                    new OrphanExpiryMessageProcessor(lClusterType, redisIndex);

                    for (int consumer = 0; consumer < lConcatPoolerConsumerCount; consumer++)
                    {
                        if (log.isDebugEnabled())
                            log.debug("Concat Pooler Consumer Start for Cluster:'" + lClusterType + "', RedisIndex:'" + redisIndex + "', Consumer Index:'" + consumer + "'");
                        new CompletedMessagePoller(lClusterType, redisIndex);
                    }
                }
            }
        }
    }

    public void shutdown()
    {
        log.fatal(SmppProperties.getInstance().getInstanceId() + " SMPP Server Instance shutting down....");

        ItextosSmppServer.getInstance().shutdownInitiated();
        ItextosSmppServer.getInstance().getSmppServer().destroy();

        processUnbindInfo();

        ItextosSmppServer.getInstance().stop();

        // TODO: Already called server stop in above statement. Not require to call
        // again.
        // stopOtherThreads();

        // printRunningThreadsInfo();

        while (!ItextosSmppServer.getInstance().isServerStopped())
        {
            log.error("shutdown() - Waiting for the Server to Stop.....");
            CommonUtility.sleepForAWhile();
        }

        log.fatal("Shutdown completed.");
    }

    private void stopOtherThreads()
    {
        // TODO Need to add all the threads started by us.
        if (adminServer != null)
            adminServer.stop();
    }

    private static void printRunningThreadsInfo()
    {
        final Map<Thread, StackTraceElement[]> lAllStackTraces = Thread.getAllStackTraces();

        for (final Entry<Thread, StackTraceElement[]> entry : lAllStackTraces.entrySet())
        {
            final Thread t = entry.getKey();
            log.fatal(">>>>>>> Thread '" + t.getName() + "' is running");

            if (log.isDebugEnabled())
            {
                log.debug("Stack trace for Thread '" + t.getName() + "'. Started in");
                final StackTraceElement[] lValue = entry.getValue();

                for (final StackTraceElement ste : lValue)
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("\tat ").append(ste.getClassName()).append(".").append(ste.getMethodName()).append("(").append(ste.getFileName()).append(":").append(ste.getLineNumber()).append(") ~[")
                            .append(ste.getClassLoaderName()).append("]");
                    log.debug(sb.toString());
                }
                log.debug("");
            }
        }
    }

    private static void processUnbindInfo()
    {

        try
        {
            final List<ISmppInfo> unbindInfoList = InfoCollection.getInstance().getObjects(SmppObjectType.UNBIND_INFO_REDIS, 1000);

            if (log.isDebugEnabled())
                log.debug("UnbindInfoRedis Q Size - " + unbindInfoList.size());

            while (!unbindInfoList.isEmpty())
            {
                log.error("shutdown() - Updating the unbind redis counts" + unbindInfoList);
                CommonUtility.sleepForAWhile();
            }
        }
        catch (final Exception e)
        {
            log.fatal("Exception while processing the unbind info ", e);
        }
    }

    private static List<String> getRedisIndices(
            int aConcatRedisPool)
    {
        final String redisIndices = CommonUtility.nullCheck(System.getProperty("concat.redis.index"), true);

        List<String> arr          = null;

        if (!redisIndices.isEmpty())
        {
            final String[] temp = StringUtils.split(redisIndices, ",");
            arr = new ArrayList(temp.length);
            final int index = 0;
            for (final String s : temp)
                arr.add(s);
        }

        final List<String> returnValue = new ArrayList<>();
        if (arr == null)
            for (int index = 0; index < aConcatRedisPool; index++)
                returnValue.add("" + index);
        else
            for (int index = 0; index < aConcatRedisPool; index++)
                if (arr.contains("" + index))
                    returnValue.add("" + index);

        ((ArrayList<String>) returnValue).trimToSize();

        return returnValue;
    }

    public static void main(
            String[] args)
    {
    	
    	System.out.println("System going to Start");
        new StartApplication().start();
    }

}