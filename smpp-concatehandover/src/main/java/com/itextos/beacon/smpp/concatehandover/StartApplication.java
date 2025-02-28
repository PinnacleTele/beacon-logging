package com.itextos.beacon.smpp.concatehandover;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.messageidentifier.MessageIdentifier;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.smpputil.ISmppInfo;
import com.itextos.beacon.smpp.concatenate.CompletedMessageChecker;
import com.itextos.beacon.smpp.concatenate.CompletedMessagePoller;
import com.itextos.beacon.smpp.concatenate.ExpiryMessageCollectionFactory;
import com.itextos.beacon.smpp.concatenate.OrphanExpiryMessageProcessor;
import com.itextos.beacon.smpp.objects.SmppObjectType;
import com.itextos.beacon.smpp.objects.inmem.InfoCollection;
import com.itextos.beacon.smpp.utils.AccountDetails;
import com.itextos.beacon.smpp.utils.SmppApplicationParams;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;
import com.itextos.beacon.smslog.DebugLog;

public class StartApplication
{

    private static final Log   log         = LogFactory.getLog(StartApplication.class);

    public void start()
    {

        try
        {
            System.out.println("Entering start()");

            initialize();

            System.out.println("Entering after initialize()");

        

            System.out.println("Entering after updateRedisRelatedEntries()");

            startConcatenateAndExpiryProcessors();

            System.out.println("Entering after startConcatenateAndExpiryProcessors()");

            addShutdownhook();

       

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


        loadAccountInfo();
    }


   

    private static void loadAccountInfo()
    {
        log.fatal("Started Loading the accounts...");

        AccountDetails.loadAccounts();

        log.fatal("Completed Loading the accounts...");
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
    	DebugLog.log("Concate Handover Start");
    	
    	System.out.println("System going to Start");
        new StartApplication().start();
    }

}