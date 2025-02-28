package com.itextos.beacon.commonlib.componentconsumer.processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.componentconsumer.processor.extend.Utility;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.daemonprocess.ShutdownHandler;
import com.itextos.beacon.commonlib.daemonprocess.ShutdownHook;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.messageprocessor.data.KafkaDBConstants;
import com.itextos.beacon.commonlib.messageprocessor.data.KafkaDataLoader;
import com.itextos.beacon.commonlib.messageprocessor.data.KafkaInformation;
import com.itextos.beacon.commonlib.messageprocessor.data.StartupRuntimeArguments;
import com.itextos.beacon.commonlib.messageprocessor.data.db.KafkaClusterComponentMap;
import com.itextos.beacon.commonlib.messageprocessor.data.db.KafkaComponentInfo;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.tp.ExecutorTopic;
import com.itextos.beacon.errorlog.ErrorLog;
import com.itextos.beacon.smslog.DebugLog;
import com.itextos.beacon.smslog.StartupFlowLog;
import com.itextos.beacon.smslog.TopicLog;

public class ProcessorInfo
        implements
        ShutdownHook
{

    private static final Log                log                               = LogFactory.getLog(ProcessorInfo.class);

    private static final String             ALL                               = "ALL";

    private static final String             PROP_KEY_APPLICATION_PROCESS_ID   = "process.id";
    private static final String             PROP_KEY_APPLICATION_STARTED_TIME = "app.started.time";
    private static final String             PROP_KEY_SHUTDOWN_STARTED_TIME    = "shutdown.started.time";
    private static final String             PROP_KEY_SHUTDOWN_COMPLETED_TIME  = "shutdown.completed.time";

    private static final int                RESTART_WAIT_SECONDS              = 10;

    private final Component                 mComponent;
    private final StartupRuntimeArguments   mStartupRuntimeArguments;
    private final List<IComponentProcessor> allProcessors                     = new ArrayList<>();

    private boolean                         clusterNotSpecified               = false;

    public ProcessorInfo(
            Component aComponent)
    {
        this(aComponent, true);
    }

    public ProcessorInfo(
            Component aComponent,
            boolean aStartJettyServer)
    {
        mComponent = aComponent;

    //    readLockFile();
    //    createLockFile();

        mStartupRuntimeArguments = new StartupRuntimeArguments();
        

        if (log.isDebugEnabled())
            log.debug("Invoking for Component '" + mComponent + "' with Startup Arguments " + mStartupRuntimeArguments);
        
        StartupFlowLog.log("Invoking for Component '" + mComponent + "' with Startup Arguments " + mStartupRuntimeArguments);
        

    	DebugLog.log("Invoking for Component '" + mComponent + "' with Startup Arguments " + mStartupRuntimeArguments);
    	
        ShutdownHandler.getInstance().addHook(aComponent.toString(), this);

    //    startPrometheusServer(aStartJettyServer);

        // Processing the Fallback Data
        /** This is not required now as it will be taken there itself. */
        // new FallbackProducerDataProcess(aComponent);
    }

   

    public void process()
            throws Exception
    {
        
        final ClusterType    cluster = ClusterType.COMMON;

        final String priority=System.getenv("priority");
            
            StartupFlowLog.log("Processing for the cluster Type '" + cluster + "'");
            
            
      

            if (priority == null)
            {
                
                StartupFlowLog.log("Processing for the priority Type '" + priority + "' given env priorityr : "+System.getenv("cluster")+" not present in DataBase");
                System.exit(0);
            }
            
            StringTokenizer st=new StringTokenizer(priority,",");
            
            while(st.hasMoreTokens()) {
            	
            	String p=st.nextToken();
            	
            	String componentname=mComponent.getKey();
            	
            	if(mComponent.getKey().equals(Component.K2E_DELIVERIES.getKey())) {
            		
            		componentname=Component.T2DB_DELIVERIES.getKey();
            		
            	}else if(mComponent.getKey().equals(Component.K2E_SUBMISSION.getKey())) {
            	
            		componentname=Component.T2DB_SUBMISSION.getKey();

            	}
            	
            	
                String topicName=componentname+"-"+p;
               
                if(p.equals("default")) {
                	
                	topicName=componentname;
                }
                        
        	
            final Map<String, Map<String, ConsumerInMemCollection>> lConsumerInmemCollection = createConsumersBeforeStartingThread(cluster,topicName);

            createConsumerThreads(cluster,topicName, lConsumerInmemCollection);

            }
    }

    private ClusterType getClustersToProcess()
    {
        
        return mStartupRuntimeArguments.getCluster();
        
    }

    private void createConsumerThreads(
    		ClusterType platformCluster, String topicName,
           Map<String, Map<String, ConsumerInMemCollection>> aConsumerInmemCollection)
    {

        int totalThreadsCount = 0;

        StartupFlowLog.log("aTopicsToConsume : '" + topicName );

       
        final KafkaComponentInfo   aKafkaComponentInfo  = KafkaDataLoader.getInstance().getKafkaProcessorInfo(mComponent);



            StartupFlowLog.log("platformCluster : '" + platformCluster );

  
            final KafkaClusterComponentMap             lKafkaCLusterInformation = KafkaDataLoader.getInstance().getKafkaClusterComponentMap(mComponent, platformCluster);
            final String                               className                = aKafkaComponentInfo.getComponentProcessClass();
            final int                                  threadsCount             = lKafkaCLusterInformation.getThreadsCount();
            final int                                  sleepInMillis            = lKafkaCLusterInformation.getSleepTimeInMillis();

            final Map<String, ConsumerInMemCollection> topicInMemCollection     = aConsumerInmemCollection.get(platformCluster.getKey());

    
            StartupFlowLog.log("Consumer to start for the Cluster Type Name : '" + platformCluster.getKey() + "' Cluster " + platformCluster+ " topics : "+platformCluster.getKey()+" topicInMemCollection : "+topicInMemCollection);
       
         


                final ConsumerInMemCollection inMemCollection = topicInMemCollection.get(topicName);

      	  for (int threadIndex = 1; threadIndex <= threadsCount; threadIndex++)
          {
               totalThreadsCount++;
                startANewThread(platformCluster.getKey(), platformCluster, topicName, className, inMemCollection, sleepInMillis, threadIndex);
          }
   	}

    private Map<String, Map<String, ConsumerInMemCollection>> createConsumersBeforeStartingThread(
    		ClusterType platformCluster, String topicName)
    {
    
    	
        final Map<String, Map<String, ConsumerInMemCollection>> clusterInMemCollection = new HashMap<>();
      
        final Map<String, ConsumerInMemCollection> topicInMemCollection = clusterInMemCollection.computeIfAbsent(platformCluster.getKey(), k -> new HashMap<>());

     
       
            	StartupFlowLog.log("platformCluster : "+platformCluster+" topicName : "+topicName);

                final ConsumerInMemCollection temp = KafkaInformation.getInstance().createConsumer(mComponent, platformCluster, topicName);
                topicInMemCollection.put(topicName, temp);
                
       
    	StartupFlowLog.log("clusterInMemCollection : "+clusterInMemCollection);

        return clusterInMemCollection;
    }

    private static void printTopicList(
            Map<String, List<String>> aTopicsToConsume)
    {
        if (log.isDebugEnabled())
            for (final Entry<String, List<String>> entry : aTopicsToConsume.entrySet())
            {
                log.debug("Platform Cluster : '" + entry.getKey() + "'");

            	StartupFlowLog.log("Platform Cluster : '" + entry.getKey() + "'");

                for (final String s : entry.getValue()) {
 
                	log.debug("Topic Name :              '" + s + "'");
                	
                	StartupFlowLog.log("Topic Name :              '" + s + "'");
            }
            }
    }

  

    private void startANewThread(
            String aClusterName,
            ClusterType aPlatformCluster,
            String aTopicName,
            String aClassName,
            ConsumerInMemCollection aInMemCollection,
            int aSleepInMillis,
            int aThreadIndex)
    {
        final String threadName = CommonUtility.combine("Thread", aClusterName, mComponent.getKey(), aTopicName, Integer.toString(aThreadIndex));

         if (log.isDebugEnabled())
            log.debug("Creating a thread with name '" + threadName + "' for the class '" + aClassName + "'");

         StartupFlowLog.log("createConsumerThreads : aClusterName : "+aClusterName+" aPlatformCluster :  "+aPlatformCluster+" aTopicName : "+aTopicName+" aClassName : "+aClassName );
         
         
        try
        {
            final Class<?>            cls                       = Class.forName(aClassName);
            final Constructor<?>      constructor               = cls.getDeclaredConstructor(Utility.getDeclaredConstrutorArgumentTypes());
            final IComponentProcessor currentComponentProcessor = (IComponentProcessor) constructor.newInstance(threadName, mComponent, aPlatformCluster, aTopicName, aInMemCollection, aSleepInMillis);
            allProcessors.add(currentComponentProcessor);

            TopicLog.getInstance(aTopicName+"_initiated").log(aTopicName+" : "+new Date());
            ExecutorTopic.getInstance().addTask(currentComponentProcessor, threadName);
            
            StartupFlowLog.log("Thread '" + threadName + "'started for Component '" + mComponent + "' Cluster '" + aClusterName + "' Actual Cluster '" + aPlatformCluster + "' Topic name '" + aTopicName
                    + "' Thread index '" + aThreadIndex + "' with sleep time millis '" + aSleepInMillis + "'");
            
            if (log.isInfoEnabled())
                log.info("Thread '" + threadName + "'started for Component '" + mComponent + "' Cluster '" + aClusterName + "' Actual Cluster '" + aPlatformCluster + "' Topic name '" + aTopicName
                        + "' Thread index '" + aThreadIndex + "' with sleep time millis '" + aSleepInMillis + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while creating " + aThreadIndex + " thread for the component '" + mComponent + "' for Cluster '" + aClusterName + "' Topic name '" + aTopicName + "' Class Name '"
                    + aClassName + "' ThreadIndex '" + aThreadIndex + "'", e);
            
            ErrorLog.log("Exception while creating " + aThreadIndex + " thread for the component '" + mComponent + "' for Cluster '" + aClusterName + "' Topic name '" + aTopicName + "' Class Name '"
                    + aClassName + "' ThreadIndex '" + aThreadIndex + "'"+ErrorMessage.getStackTraceAsString(e));
        }
    }

   

   
    private void addPrioritySpecificTopics(
            ClusterType cluster,
            List<InterfaceGroup> aInterfaceGroupList,
            List<MessagePriority> aMessagePriorityList,
            Map<String, List<String>> aTopicsToConsume,
            Map<String, List<String>> aPriorityBasedTopics)
    {}

    private static void addToList(
            List<String> aTopicsToConsume,
            List<String> aList)
    {
        for (final String s : aList)
            if (!aTopicsToConsume.contains(s))
                aTopicsToConsume.add(s);
    }

    private static void logPriorityError(
            String aKey)
    {
        log.error("'" + aKey + "' does not have a proper configuration for the client specific topics in '" + KafkaDBConstants.TABLE_NAME_PLATFORM_CLUSTER_KAFKA_TOPIC_MAP + "'");
    }

    private void logAndThrowException(
            String aErrorInfo)
    {
        final ItextosRuntimeException lItextosRuntimeException = new ItextosRuntimeException(aErrorInfo);
        log.error("Exception while loading the Topics for " + mStartupRuntimeArguments, lItextosRuntimeException);
       // throw lItextosRuntimeException;
    }

  

   

   
   

    /**
     * This method should be called in-case the shutdown hook default behavior is
     * not happening.
     */
    @Override
    public void shutdown()
    {
        log.fatal("Shutdownhook process Started for Component '" + mComponent + "'", new Exception("Called from"));
        updateLockFileForShutdownStart();

        KafkaInformation.getInstance().stopMe();

        for (final IComponentProcessor componentProcessor : allProcessors)
        {
            componentProcessor.stopProcessing();
            componentProcessor.doCleanup();
        }
        KafkaInformation.getInstance().flushProducers();
        KafkaInformation.getInstance().isAllProducersCompleted();

        updateLockFileShutdownCompleted();

        log.fatal(mComponent + " Process Completed Successfully ...............");
    }

    private String getLockFilename()
    {
        return mComponent + ".lock";
    }

    private void updateLockFileShutdownCompleted()
    {
        final File lFile = new File(getLockFilename());

        if (!lFile.exists())
        {
            final String s = mComponent + " Lock file not found. Something went wrong. Check the lock file settings.";
            logContent(s);
        }

        try (
                BufferedWriter bw = new BufferedWriter(new FileWriter(lFile, true));)
        {
            bw.write(PROP_KEY_SHUTDOWN_COMPLETED_TIME + "=" + DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) + CommonUtility.getLineSeparator());
        }
        catch (final Exception ex)
        {}
        final String s = "Lock file updated ...";
        logContent(s);
    }

    private void updateLockFileForShutdownStart()
    {
    	/*
        final File lFile = new File(getLockFilename());

        if (!lFile.exists())
        {
            final String s = mComponent + " Lock file not found. Something went wrong. Check the lock file settings.";
            logContent(s);
        }

        try (
                BufferedWriter bw = new BufferedWriter(new FileWriter(lFile, true));)
        {
            bw.write(PROP_KEY_SHUTDOWN_STARTED_TIME + "=" + DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) + CommonUtility.getLineSeparator());
        }
        catch (final Exception e)
        {
            final String s = mComponent + " Problem in updating the lock file for shutdown start";
            logContent(s, e);
        }
        final String s = mComponent + " Lock file updated for shutdown start";
        logContent(s);
        */
    }

    private void createLockFile()
    {
    	/*
        final File lFile = new File(getLockFilename());

        if (!lFile.exists())
        {
            final String s = mComponent + " Lock file not found. No need to delete. May be the application starting without lock file...";
            logContent(s);
        }
        else
            try
            {
                Files.delete(lFile.toPath());
                final String s = mComponent + " Lock file deleted successfully";
                logContent(s);
            }
            catch (final Exception e)
            {
                final String s = mComponent + " Unable to delete the lock file : '" + lFile.getAbsolutePath() + "'";
                logContent(s, e);
                System.exit(-9);
            }

        createNewLockFile();
        final String s = mComponent + " Lock file created ...";
        logContent(s);
        
        */
    }

    private void createNewLockFile()
    {
        final File lFile = new File(getLockFilename());

        try (
                BufferedWriter bw = new BufferedWriter(new FileWriter(lFile));)
        {
            bw.write(PROP_KEY_APPLICATION_PROCESS_ID + "=" + CommonUtility.getJvmProcessId() + System.getProperty("line.separator"));
            bw.write(PROP_KEY_APPLICATION_STARTED_TIME + "=" + DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) + CommonUtility.getLineSeparator());
        }
        catch (final Exception e)
        {
            final String s = mComponent + " Unable to create the lock file : '" + lFile.getAbsolutePath() + "'";
            logContent(s, e);
            System.exit(-11);
        }
    }

    private void readLockFile()
    {
        final File lFile = new File(getLockFilename());

        if (!lFile.exists())
        {
            final String s = "Lock file not found. Starting application normally without checking...";
            logContent(s);
        }
        else
            validateLockFile(lFile);

        final String s = mComponent + " Application Starting......";
        logContent(s);
    }

    private void validateLockFile(
            File aFile)
    {

        try (
                final FileInputStream fis = new FileInputStream(aFile);)
        {
            final Properties props = new Properties();
            props.load(fis);

            final String appProcessId          = props.getProperty(PROP_KEY_APPLICATION_PROCESS_ID);
            final Date   appStartedTime        = getDateFromProperty(props, PROP_KEY_APPLICATION_STARTED_TIME);
            final Date   shutdownStartedTime   = getDateFromProperty(props, PROP_KEY_SHUTDOWN_STARTED_TIME);
            final Date   shutdownCompletedTime = getDateFromProperty(props, PROP_KEY_SHUTDOWN_COMPLETED_TIME);

            final String info                  = "Application Process id '" + appProcessId + "' Started '" + appStartedTime + "' Shutdown started '" + shutdownStartedTime + "' Shutdown Completed '"
                    + shutdownCompletedTime + "'";
            logContent(info);

            if ((appProcessId == null) || (appStartedTime == null))
            {
                final String s = mComponent + " Not a valid lock file. Unable to start the application.";
                logContent(s);
                System.exit(-1);
                return;
            }

            if (shutdownStartedTime != null)
            {
                checkForStartTime(appStartedTime, shutdownStartedTime);
                checkForShuttingdownProcess(appProcessId, appStartedTime, shutdownCompletedTime);
                compareShutdownStartAndCompleteTime(appProcessId, shutdownStartedTime, shutdownCompletedTime);
                checkFoeWaitTime(shutdownCompletedTime);
            }
            else
            {
                // Very very Rare case
                final String s = mComponent + " It seems application is already running. It started at '" + appStartedTime + "'. Unable to start the application one more time.";
                logContent(s);
                System.exit(-3);
            }
        }
        catch (final Exception e)
        {
            final String s = mComponent + " Exception while validating the lock file.";
            logContent(s, e);
            System.exit(-99);
        }
    }

    private void checkForStartTime(
            Date aAppStartedTime,
            Date aShutdownStartedTime)
    {

        if (aAppStartedTime.after(aShutdownStartedTime))
        {
            final String s = mComponent + " Not a valid lock file. Shutdown start time is greater than application start time.";
            logContent(s);
            System.exit(-1);
        }
    }

    private void checkForShuttingdownProcess(
            String aAppProcessId,
            Date aAppStartedTime,
            Date aShutdownCompletedTime)
    {

        if ((aShutdownCompletedTime == null))
        {
            final String currentProcessId = CommonUtility.getJvmProcessId();

            if (currentProcessId.equals(aAppProcessId))
            {
                final String s = mComponent + " It seems application with process id '" + CommonUtility.getJvmProcessId() + "' started at '" + aAppStartedTime
                        + "' is under shutdown process. Unable to start the application one more time.";
                logContent(s);
                System.exit(-5);
            }
        }
    }

    private void compareShutdownStartAndCompleteTime(
            String aAppProcessId,
            Date aShutdownStartedTime,
            Date aShutdownCompletedTime)
    {

        if ((aShutdownCompletedTime != null) && (aShutdownStartedTime.after(aShutdownCompletedTime)))
        {
            final String currentProcessId = CommonUtility.getJvmProcessId();

            if (currentProcessId.equals(aAppProcessId))
            {
                final String s = mComponent + " Shutdown completed time (" + aShutdownCompletedTime + ") is before shutdown started time (" + aShutdownStartedTime
                        + "). Some problem in the lock file. Unable to start the application.";
                logContent(s);
                System.exit(-7);
            }
        }
    }

    private void checkFoeWaitTime(
            Date aShutdownCompletedTime)
    {

        if (aShutdownCompletedTime != null)
        {
            final Calendar nextMinStart = Calendar.getInstance();
            nextMinStart.setLenient(false);
            nextMinStart.setTime(aShutdownCompletedTime);
            nextMinStart.add(Calendar.SECOND, RESTART_WAIT_SECONDS);

            if (nextMinStart.getTime().getTime() > System.currentTimeMillis())
            {
                final String s1 = mComponent + " Shutdown completed by " + aShutdownCompletedTime + ". Need to wait for some time to start it again. Next allowed restart is at '"
                        + DateTimeUtility.getFormattedDateTime(nextMinStart.getTime(), DateTimeFormat.DEFAULT) + "'";
                logContent(s1);
                System.exit(-9);
            }
        }
    }

    private static Date getDateFromProperty(
            Properties aProps,
            String aPropKey)
    {
        return DateTimeUtility.getDateFromString(aProps.getProperty(aPropKey), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
    }

    private static void logContent(
            String aString)
    {
        logContent(aString, null);
    }

    private static void logContent(
            String aString,
            Throwable aThrowable)
    {
        log.fatal(aString, aThrowable);
        System.out.println(DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS) + " " + aString);
        if (aThrowable != null)
            aThrowable.printStackTrace();
    }

    public static void main(
            String[] args)
    {
        final ProcessorInfo processorInfo = new ProcessorInfo(Component.SBCV);

        try
        {
            processorInfo.process();
        }
        catch (final Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}