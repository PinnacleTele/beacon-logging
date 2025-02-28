package com.itextos.beacon.commonlib.messageprocessor.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.kafkaservice.common.KafkaCustomProperties;
import com.itextos.beacon.commonlib.kafkaservice.consumer.Consumer;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;
import com.itextos.beacon.commonlib.kafkaservice.consumer.KafkaConsumerProperties;
import com.itextos.beacon.commonlib.kafkaservice.producer.KafkaProducerProperties;
import com.itextos.beacon.commonlib.kafkaservice.producer.Producer;
import com.itextos.beacon.commonlib.kafkaservice.producer.ProducerInMemCollection;
import com.itextos.beacon.commonlib.messageprocessor.data.db.KafkaClusterComponentMap;
import com.itextos.beacon.commonlib.messageprocessor.data.db.KafkaClusterInfo;
import com.itextos.beacon.commonlib.messageprocessor.request.ProducerKafkaRequest;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.RoundRobin;
import com.itextos.beacon.commonlib.utility.tp.ExecutorKafkaConsumer;
import com.itextos.beacon.errorlog.ErrorLog;
import com.itextos.beacon.smslog.KILog;
import com.itextos.beacon.smslog.ProducerTopicLog;
import com.itextos.beacon.smslog.StartupFlowLog;

public class KafkaInformation
{

    private static final Log    log                  = LogFactory.getLog(KafkaInformation.class);

    private static final String PROPERTY_KAFKA_TOPIC = "kafka.topic.name";
    private static final String PROPERTY_CLIENT_ID   = "client.id";
    private static final String PROPERTY_GROUP_ID    = "group.id";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final KafkaInformation INSTANCE = new KafkaInformation();

    }

    public static KafkaInformation getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    // Map<Component~Cluster, Map<Topic, Map<ProducerIndex, Producer>>>
    private final Map<String, Map<String, Map<Integer, Producer>>> mKafkaProducerCollection                        = new ConcurrentHashMap<>();

    // Map<Component~Cluster~Topic~ThreadId, PorducerIndex>
    private final Map<String, Integer>                             mKafkaProducerThreadMap                         = new ConcurrentHashMap<>();
    private final Map<String, BlockingQueue<Integer>>              topicProducerIndexMap                           = new ConcurrentHashMap<>();

    // Map<Component~Cluster, Map<Topic, List<Consumers>>>
    private final Map<String, Map<String, List<Consumer>>>         mKafkaConsumerCollection                        = new ConcurrentHashMap<>();
    private final Map<String, List<Consumer>>                      mKafkaConsumerCollectionAtClusterComponentLevel = new ConcurrentHashMap<>();

    // Map<Component~Cluster~Topic, ConsumerInMemCollection>
    private final Map<String, ConsumerInMemCollection>             mKafkaInMemoryConsumerCollection                = new ConcurrentHashMap<>();
    private final Map<String, ProducerInMemCollection>             mKafkaInMemoryProducerCollection                = new ConcurrentHashMap<>();

    private boolean                                                mStopInitiated                                  = false;
    private int                                                    mTotalConsumersCount                            = 0;

    private KafkaInformation()
    {
        PrometheusMetrics.registerKafkaCountersMetrics();
    }

    public Producer getProducer(
            ProducerKafkaRequest aProducerKafkaRequest)
            throws ItextosException
    {
        if (log.isDebugEnabled())
            log.debug("ProducerKafkaRequest " + aProducerKafkaRequest);

        Producer returnValue = null;

        /*
        if (aProducerKafkaRequest.isClientSpecific())
            returnValue = getProducerBasedOnClient(aProducerKafkaRequest);
	
        if (returnValue != null)
            return returnValue;
*/
        if (aProducerKafkaRequest.isPriorityTopic())
            returnValue = getProducerBasedOnPriority(aProducerKafkaRequest);

    //    if (returnValue != null)
            return returnValue;

  //      return getDefaultProducer(aProducerKafkaRequest);
    }

    private Producer getDefaultProducer(
            ProducerKafkaRequest aProducerKafkaRequest)
            throws ItextosException
    {
        String topicName = null;

      
        if (aProducerKafkaRequest.isIntlFlag()) {
            topicName = KafkaDataLoader.getInstance().getDefaultTopicName(aProducerKafkaRequest.getNextComponent(), ClusterType.INTL);
        }else {
        
        	if(aProducerKafkaRequest.getInterfaceGroup()==InterfaceGroup.UI) {
                topicName = KafkaDataLoader.getInstance().getDefaultTopicName(aProducerKafkaRequest.getNextComponent(), ClusterType.GUI);
        	}else {
        		topicName = KafkaDataLoader.getInstance().getDefaultTopicName(aProducerKafkaRequest.getNextComponent(), aProducerKafkaRequest.getPlatformCluster());
        	}
        	
        }

        topicName = KafkaDataLoaderUtility.updateTopicName(topicName);
        
        ProducerTopicLog.log("getDefaultProducer :  topicName : "+topicName);


        return getOrCreateProducer(aProducerKafkaRequest, topicName);
    }

    private Producer getProducerBasedOnPriority(
            ProducerKafkaRequest aProducerKafkaRequest)
            throws ItextosException
    {
        String topicName = null;
        
        if (aProducerKafkaRequest.isIntlFlag()) {
        	
        	topicName = KafkaDataLoader.getInstance().getTopicNameBasedOnPriorityForProducer(aProducerKafkaRequest.getNextComponent(),  ClusterType.INTL,
                    aProducerKafkaRequest.getInterfaceGroup(), aProducerKafkaRequest.getMessageType(), aProducerKafkaRequest.getMessagePriority());      
        }else {
        
        	if(aProducerKafkaRequest.getInterfaceGroup()==InterfaceGroup.UI) {
        		
        		topicName = KafkaDataLoader.getInstance().getTopicNameBasedOnPriorityForProducer(aProducerKafkaRequest.getNextComponent(), ClusterType.GUI,
        	                        aProducerKafkaRequest.getInterfaceGroup(), aProducerKafkaRequest.getMessageType(), aProducerKafkaRequest.getMessagePriority());
          
        	}else {
        		topicName = KafkaDataLoader.getInstance().getTopicNameBasedOnPriorityForProducer(aProducerKafkaRequest.getNextComponent(), aProducerKafkaRequest.getPlatformCluster(),
                        aProducerKafkaRequest.getInterfaceGroup(), aProducerKafkaRequest.getMessageType(), aProducerKafkaRequest.getMessagePriority());
            		
        	}
        	
        }

        if(topicName==null) {
        
        	topicName=aProducerKafkaRequest.getNextComponent().getKey();
        
        }else {
     
        	topicName = CommonUtility.combine(KafkaDBConstants.TOPIC_SEPARATOR, aProducerKafkaRequest.getNextComponent().getKey(), topicName);
        
        }


        topicName = KafkaDataLoaderUtility.updateTopicName(topicName);

        ProducerTopicLog.log("getProducerBasedOnPriority :  topicName : "+topicName+" aProducerKafkaRequest.isIntlFlag() : "+aProducerKafkaRequest.isIntlFlag()+" aProducerKafkaRequest.getNextComponent()  :  "+aProducerKafkaRequest.getNextComponent()+" aProducerKafkaRequest.getPlatformCluster() : "+aProducerKafkaRequest.getPlatformCluster()+ " aProducerKafkaRequest.getMessagePriority() :  "+ aProducerKafkaRequest.getMessagePriority());
        
        return getOrCreateProducer(aProducerKafkaRequest, topicName);
    }

    private Producer getProducerBasedOnClient(
            ProducerKafkaRequest aProducerKafkaRequest)
            throws ItextosException
    {
        String topicName = KafkaDataLoader.getInstance().getClientBasedTopic(aProducerKafkaRequest.getNextComponent(), aProducerKafkaRequest.getClientId());
        if (topicName == null)
            return null;

        topicName = KafkaDataLoaderUtility.updateTopicName(topicName);
        
        ProducerTopicLog.log("getProducerBasedOnClient :  topicName : "+topicName);

        return getOrCreateProducer(aProducerKafkaRequest, topicName);
    }

    private Producer getOrCreateProducer(
            ProducerKafkaRequest aProducerKafkaRequest,
            String aTopicName)
            throws ItextosException
    {
        final String lComponentClusterKey = KafkaDataLoader.getKafkaClusterComponentMapKeyName(aProducerKafkaRequest.getFromComponent(), aProducerKafkaRequest.getPlatformCluster());

        if (lComponentClusterKey != null)
        {
        	
        	KILog.log("mKafkaProducerCollection : "+ mKafkaProducerCollection.toString());
        	
            final Map<String, Map<Integer, Producer>> lTopicProducerMap  = mKafkaProducerCollection.computeIfAbsent(lComponentClusterKey, k -> new ConcurrentHashMap());
            final Map<Integer, Producer>              lThreadProducerMap = lTopicProducerMap.computeIfAbsent(aTopicName, k -> new ConcurrentHashMap<>());

            final long                                threadId           = Thread.currentThread().getId();
            final Integer                             lNextProducerIndex = getNextProducer(threadId, aProducerKafkaRequest, aTopicName);
            Producer                                  lProducer          = lThreadProducerMap.get(lNextProducerIndex);

            if (lProducer == null)
            {
                if (log.isDebugEnabled())
                    log.debug("Creating a new producer for cluster " + lComponentClusterKey + " topic '" + aTopicName + "' thread '" + threadId + "'");
                lProducer = createNewProducer(aProducerKafkaRequest, aTopicName);

                if (lProducer != null)
                {
                    lThreadProducerMap.put(lNextProducerIndex, lProducer);
                    attachProducerToConsumer(aProducerKafkaRequest.getFromComponent(), aProducerKafkaRequest.getPlatformCluster(), aTopicName, threadId, lProducer);
                }
            }
            return lProducer;
        }
        return null;
    }

    private Integer getNextProducer(
            long aThreadId,
            ProducerKafkaRequest aProducerKafkaRequest,
            String aTopicName)
    {
        final String             platformName              = KafkaDataLoaderUtility.getNameOrDefault(aProducerKafkaRequest.getPlatformCluster());
        final String             fromComponent             = aProducerKafkaRequest.getFromComponent().getKey();
        final String             key                       = CommonUtility.combine(fromComponent, platformName, aTopicName, Long.toString(aThreadId));
        final String             keyForQueue               = CommonUtility.combine(fromComponent, platformName, aTopicName);
        KafkaClusterComponentMap lKafkaClusterComponentMap = KafkaDataLoader.getInstance().getKafkaClusterComponentMap(aProducerKafkaRequest.getFromComponent(),
                aProducerKafkaRequest.getPlatformCluster());

        if (lKafkaClusterComponentMap == null)
            lKafkaClusterComponentMap = KafkaDataLoader.getInstance().getKafkaClusterComponentMap(aProducerKafkaRequest.getFromComponent(), null);

        // lKafkaClusterComponentMap will come as null for the interfaces. So we will
        // make 10 as the maximum count for it.
        final int                    maxProducersPerTopic = lKafkaClusterComponentMap == null ? 10 : lKafkaClusterComponentMap.getMaxProducersPerTopic();
        final BlockingQueue<Integer> queue                = topicProducerIndexMap.computeIfAbsent(keyForQueue, k -> new LinkedBlockingQueue<>(maxProducersPerTopic));
        Integer                      producerIndex        = null;

        if (queue.size() == maxProducersPerTopic)
        {
            producerIndex = queue.poll();
            mKafkaProducerThreadMap.remove(key);
        }
        else
            producerIndex = mKafkaProducerThreadMap.get(key);

        if (producerIndex == null)
            producerIndex = RoundRobin.getInstance().getCurrentIndex(keyForQueue, maxProducersPerTopic);

        mKafkaProducerThreadMap.put(key, producerIndex);

        addOrUpdate(keyForQueue, producerIndex, maxProducersPerTopic);
        return producerIndex;
    }

    private void addOrUpdate(
            String aKeyForQueue,
            Integer aProducerIndex,
            int aMaxProducersPerTopic)
    {
        final BlockingQueue<Integer> queue = topicProducerIndexMap.computeIfAbsent(aKeyForQueue, k -> new LinkedBlockingQueue<>(aMaxProducersPerTopic));
        if (queue.contains(aProducerIndex))
            queue.remove(aProducerIndex);

        queue.offer(aProducerIndex);
    }

    /**
     * Attach the new producer with all the consumers created. Because we are using
     * a multiple producers and consumers. It is not one to one relationship.
     */
    private void attachProducerToConsumer(
            Component aComponent,
            ClusterType aClusterType,
            String aTopicName,
            long aThreadId,
            Producer aProducer)
    {
        String componentClusterTypeKey = CommonUtility.combine(aComponent.getKey(), KafkaDataLoaderUtility.getNameOrDefault(aClusterType));
        log.fatal("Trying to attach producer " + aProducer + " to the consumers of '" + componentClusterTypeKey );

        List<Consumer> consumersList = mKafkaConsumerCollectionAtClusterComponentLevel.get(componentClusterTypeKey);

        if (consumersList == null)
        {
            log.fatal("Consumers are not available for the component '" + aComponent + "' and Cluster '" + aClusterType + "' Trying with the default cluster group.");
            componentClusterTypeKey = CommonUtility.combine(aComponent.getKey(), KafkaDataLoaderUtility.getNameOrDefault(null));
            consumersList           = mKafkaConsumerCollectionAtClusterComponentLevel.get(componentClusterTypeKey);
        }

        log.fatal("Trying to attach producer '" + aProducer + "' to the consumers of '" + componentClusterTypeKey);

        // This also should not be null here.
        if ((consumersList != null) && (!consumersList.isEmpty()))
            for (final Consumer consumer : consumersList)
                consumer.addProducer(aThreadId, aProducer);
        else
        {
            log.fatal(">>>>>>>>>> Again. Consumers List is coming as null even for the Component Cluster '" + componentClusterTypeKey + "'");

            if (aComponent == Component.INTERFACES)
                log.fatal("We will not have any consumers for the component " + Component.INTERFACES + ". So we can ignore it.");
            else
                for (final String s : mKafkaConsumerCollection.keySet())
                    log.fatal("Available keys '" + s + "'");
        }
    }

    private synchronized Producer createNewProducer(
            ProducerKafkaRequest aProducerKafkaRequest,
            String aKafkaTopicName)
            throws ItextosException
    {
        if (log.isDebugEnabled())
            log.debug("Creating a new Kafka Producer for " + aProducerKafkaRequest);

        final KafkaClusterComponentMap lKafkaClusterComponentMap = KafkaDataLoader.getInstance().getKafkaClusterComponentMap(aProducerKafkaRequest.getNextComponent(),
                aProducerKafkaRequest.getPlatformCluster());

        if (log.isDebugEnabled())
            log.debug("Kafka Cluster Component map : " + lKafkaClusterComponentMap);

        if (lKafkaClusterComponentMap != null)
        {
            final String lKafkaProducerClusterName = lKafkaClusterComponentMap.getKafkaProducerClusterName();

            if (log.isDebugEnabled())
                log.debug("Kafka Producer Cluster name : '" + lKafkaProducerClusterName + "'");

            final KafkaClusterInfo lKafkaClusterInfo = KafkaDataLoader.getInstance().getKafkaClusterInfo(lKafkaProducerClusterName);

            if (log.isDebugEnabled())
                log.debug("Kafka Cluster found : '" + lKafkaClusterInfo + "'");

            if (lKafkaClusterInfo != null)
            {
                final String lKafkaServerProperties = lKafkaClusterInfo.getKafkaProducerProperties();

                if (log.isDebugEnabled())
                    log.debug("Kafka Cluster Properties found : '" + lKafkaServerProperties + "'");

                final Properties props = PropertyLoader.getInstance().getPropertiesByFileName(lKafkaServerProperties);
                final String     ip    = CommonUtility.getApplicationServerIp();

                props.setProperty(PROPERTY_KAFKA_TOPIC, aKafkaTopicName);
                props.setProperty(PROPERTY_CLIENT_ID, CommonUtility.combine(aProducerKafkaRequest.getNextComponent().getKey(), "producer", ip, Long.toString(Thread.currentThread().getId())));

                final String                  key = CommonUtility.combine(lKafkaProducerClusterName, aProducerKafkaRequest.getNextComponent().getKey(), aKafkaTopicName);
                final ProducerInMemCollection pic = new ProducerInMemCollection(aKafkaTopicName);
                mKafkaInMemoryProducerCollection.put(key, pic);
                final Producer producer = new Producer(aProducerKafkaRequest.getNextComponent(), aKafkaTopicName, new KafkaProducerProperties(props), pic);

                if (log.isDebugEnabled())
                    log.debug("Kafka producer created successfully for Kafka Producer Request Object : '" + aProducerKafkaRequest + "'");

                return producer;
            }
            log.error("Kafka Cluser information not available for '" + lKafkaProducerClusterName + "'");
        }
        log.error("Component Platform Cluster map is not available for component '" + aProducerKafkaRequest.getNextComponent() + "' Platform Cluster '" + aProducerKafkaRequest.getPlatformCluster()
                + "'");
        log.fatal("Unable to create a Kafka Producer for " + aProducerKafkaRequest);
        return null;
    }

    public ConsumerInMemCollection createConsumer(
            Component aComponent,
            ClusterType aClusterType,
            String aTopicName)
    {
        final String platformCluster = KafkaDataLoaderUtility.getNameOrDefault(aClusterType);
        StartupFlowLog.log("platformCluster :  "+ platformCluster);
        final String key             = CommonUtility.combine(aComponent.getKey(), platformCluster, aTopicName);
        StartupFlowLog.log("key :  "+ key);

        return mKafkaInMemoryConsumerCollection.computeIfAbsent(key, k -> createNewConsumers(aComponent, aClusterType, aTopicName));
    }

    private ConsumerInMemCollection createNewConsumers(
            Component aComponent,
            ClusterType aClusterType,
            String aTopicName)
    {
        final String logKey = aComponent + "-" + aClusterType + "-" + aTopicName;
   
        StartupFlowLog.log("Creating Kafka Consumers for " + logKey);
        
        final KafkaClusterComponentMap lKafkaClusterComponentMap = KafkaDataLoader.getInstance().getKafkaClusterComponentMap(aComponent, aClusterType);

    
        StartupFlowLog.log("Kafka Cluster Component map : " + lKafkaClusterComponentMap);

        if (lKafkaClusterComponentMap != null)
        {
            final String lKafkaConsumerClusterName = lKafkaClusterComponentMap.getKafkaConsumerClusterName();

           StartupFlowLog.log("Consumer Kafka Cluster name : '" + lKafkaConsumerClusterName + "'");

            final KafkaClusterInfo lKafkaClusterInfo = KafkaDataLoader.getInstance().getKafkaClusterInfo(lKafkaConsumerClusterName);

           StartupFlowLog.log("Kafka Cluster found : '" + lKafkaClusterInfo + "'");

            if (lKafkaClusterInfo != null)
            {
                 
                StartupFlowLog.log("Topic Name to use :'" + aTopicName + "'");


                if (aTopicName != null)
                    return createConsumerClients(aComponent, aClusterType, aTopicName, lKafkaClusterInfo);
            }
        }
        StartupFlowLog.log("Component Platform Cluster map is not available for component '" + aComponent + "'");
        ErrorLog.log("Component Platform Cluster map is not available for component '" + aComponent + "'");

        return null;
    }

    private ConsumerInMemCollection createConsumerClients(
            Component aComponent,
            ClusterType aClusterType,
            String aTopicName,
            KafkaClusterInfo aKafkaClusterInfo)
    {
        final KafkaClusterComponentMap lKafkaCLusterInformation = KafkaDataLoader.getInstance().getKafkaClusterComponentMap(aComponent, aClusterType);
        final int                      consumerClientCount      = lKafkaCLusterInformation.getKafkaClientConsumerCount();
        final String                   topicName                = KafkaDataLoaderUtility.updateTopicName(aTopicName);

        if (log.isDebugEnabled())
            log.debug("Final Topic name to use : '" + topicName + "'");
        
        StartupFlowLog.log("Final Topic name to use : '" + topicName + "'");

        final String                      key                                = CommonUtility.combine(aComponent.getKey(), KafkaDataLoaderUtility.getNameOrDefault(aClusterType));
        final Map<String, List<Consumer>> consumerMap                        = mKafkaConsumerCollection.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
        final List<Consumer>              consumersList                      = consumerMap.computeIfAbsent(topicName, k -> new ArrayList<>());
        final List<Consumer>              componentConsumersList             = mKafkaConsumerCollectionAtClusterComponentLevel.computeIfAbsent(key, k -> new ArrayList<>());
        final String                      lKafkaServerConsumerPropertiesPath = aKafkaClusterInfo.getKafkaConsumerProperties();
  //      final String                      lKafkaServerProducerPropertiesPath = aKafkaClusterInfo.getKafkaProducerProperties();

     
        StartupFlowLog.log("Kafka Cluster Consumer Properties found : '" + lKafkaServerConsumerPropertiesPath + "'");

        final Properties              consumerProps           = PropertyLoader.getInstance().getPropertiesByFileName(lKafkaServerConsumerPropertiesPath);
        final String                  ip                      = CommonUtility.getApplicationServerIp();

        final ConsumerInMemCollection consumerInMemCollection = new ConsumerInMemCollection(topicName);
        
        
         


                for (int consumerClientIndex = 1; consumerClientIndex <= consumerClientCount; consumerClientIndex++)
                {
                    final String clientId = CommonUtility.combine(Thread.currentThread().getName(), aComponent.getKey(), "consumer", Integer.toString(consumerClientIndex), ip);

                    if (log.isDebugEnabled())
                        log.debug("Topic Name : " + topicName + " Consumer Group Name " + lKafkaCLusterInformation.getKafkaConsumerGroupName());

                    consumerProps.setProperty(PROPERTY_KAFKA_TOPIC, topicName);
                    consumerProps.setProperty(PROPERTY_GROUP_ID, lKafkaCLusterInformation.getKafkaConsumerGroupName());
                    consumerProps.setProperty(PROPERTY_CLIENT_ID, clientId);

                    if (log.isDebugEnabled())
                        log.debug("Creating Kafka Consumer Client with the client Id '" + clientId + "' for the topic '" + topicName + "'");

                    final Consumer consumer = new Consumer(aComponent, topicName, new KafkaConsumerProperties(consumerProps), consumerInMemCollection, consumerClientIndex);
                    consumersList.add(consumer);
                    componentConsumersList.add(consumer);

                    mTotalConsumersCount++;
                    
                   
                    ExecutorKafkaConsumer.getInstance().addTask(consumer,  topicName + "-" + consumerClientIndex);
        
                    StartupFlowLog.log("createConsumerClients : "+clientId+"  topicName : "+topicName);
                    
                    if (log.isDebugEnabled())
                        log.debug("Started consumer " + clientId);
                }

          	
      

        
        return consumerInMemCollection;
    }

    public void stopMe()
    {

        if (mStopInitiated)
        {
            log.warn("Stop already initialted. NO NEED TO CALL CLOSING PRODUCERS AND CONSUMERS.");
            return;
        }

        mStopInitiated = true;

        log.fatal("Stop initiated. Stopping all consumers.");

        closeConsumers();

        final int sleepSeconds = KafkaCustomProperties.getInstance().getConsumerFinalSleepTime();

        log.fatal("Wait for " + sleepSeconds + " seconds to process the pending messages.");

        CommonUtility.sleepForAWhile(sleepSeconds * 1000L);

        log.fatal("Got up after " + sleepSeconds + " seconds of sleep.");

        waitForInmemToClear();
    }

    private void waitForInmemToClear()
    {
        waitForConsumerInmemory();
        waitForConsumerStopped();
        waitForProducerInmemory();
    }

    private void waitForProducerInmemory()
    {
        boolean isCompleted    = false;
        int     attemptCounter = 0;

        log.fatal("Although some of the Producer messages may be stuck in memory. Try to clear them.");

        while (!isCompleted)
        {
            attemptCounter++;
            isCompleted = true;

            for (final Entry<String, ProducerInMemCollection> entry : mKafkaInMemoryProducerCollection.entrySet())
            {
                final ProducerInMemCollection lValue = entry.getValue();
                final int                     size   = lValue.getInMemSize();
                log.fatal("Attempt Count : " + attemptCounter + " Checking for the component '" + entry.getKey() + "'Producer Inmem size = '" + size + "'");

                isCompleted = (size == 0);

                if (!isCompleted)
                {
                    CommonUtility.sleepForAWhile(10);
                    break;
                }
            }
        }
    }

    private void waitForConsumerInmemory()
    {
        boolean isCompleted    = false;
        int     attemptCounter = 0;

        log.fatal("Although some of the consumer messages may be stuck in memory. Try to clear them.");

        while (!isCompleted)
        {
            attemptCounter++;
            isCompleted = true;

            for (final Entry<String, ConsumerInMemCollection> entry : mKafkaInMemoryConsumerCollection.entrySet())
            {
                final ConsumerInMemCollection lValue = entry.getValue();
                final int                     size   = lValue.getInMemSize();
                log.fatal("Attempt Count : " + attemptCounter + " Checking for the component '" + entry.getKey() + "'Consumer Inmem size = '" + size + "'");

                isCompleted = (size == 0);

                if (!isCompleted)
                {
                    CommonUtility.sleepForAWhile(10);
                    break;
                }
            }
        }
    }

    private void waitForConsumerStopped()
    {
        boolean isCompleted    = false;
        int     attemptCounter = 0;

        while (!isCompleted)
        {
            attemptCounter++;
            log.fatal("Waiting for the inmemory to clear... Attempt Count : " + attemptCounter);
            isCompleted = true;

            a:
            for (final Entry<String, Map<String, List<Consumer>>> entry : mKafkaConsumerCollection.entrySet())
            {
                if (log.isInfoEnabled())
                    log.info("Closing the consumers for component and / or cluster '" + entry.getKey() + "'");

                for (final Entry<String, List<Consumer>> consumerEntry : entry.getValue().entrySet())
                    try
                    {
                        if (log.isInfoEnabled())
                            log.info("Closing Consumer for the key : '" + consumerEntry.getKey() + "'");

                        for (final Consumer c : consumerEntry.getValue())
                        {
                            isCompleted = c.isCompletelyStopped();

                            if (!isCompleted)
                                break a;
                        }
                    }
                    catch (final Exception e)
                    {
                        log.error("Exception while stoppping the Producer '" + entry.getKey() + "' and '" + consumerEntry.getKey() + "'", e);
                    }
            }

            if (!isCompleted)
                CommonUtility.sleepForAWhile(10);
        }
    }

    private void closeConsumers()
    {

        for (final Entry<String, Map<String, List<Consumer>>> entry : mKafkaConsumerCollection.entrySet())
        {
            if (log.isInfoEnabled())
                log.info("Closing the consumers for component and / or cluster '" + entry.getKey() + "'");

            for (final Entry<String, List<Consumer>> consumerEntry : entry.getValue().entrySet())
                try
                {
                    if (log.isInfoEnabled())
                        log.info("Closing Consumer for the key : '" + consumerEntry.getKey() + "'");
                    for (final Consumer c : consumerEntry.getValue())
                        c.stopConsuming();
                }
                catch (final Exception e)
                {
                    log.error("Exception while stoppping the Producer '" + entry.getKey() + "' and '" + consumerEntry.getKey() + "'", e);
                }
        }
    }

    public void flushProducers()
    {
        log.fatal("Calling FLUSH PRODUCERS METHOD " + mKafkaProducerCollection);

        for (final Entry<String, Map<String, Map<Integer, Producer>>> entry : mKafkaProducerCollection.entrySet())
        {
            log.fatal("Flushing the producers for component and cluster '" + entry.getKey() + "'");

            for (final Entry<String, Map<Integer, Producer>> producerEntry : entry.getValue().entrySet())
            {
                log.fatal("Flushing producer From component and cluster '" + entry.getKey() + "' and the Topic : '" + producerEntry.getKey() + "'");

                for (final Entry<Integer, Producer> threadBasedProducers : producerEntry.getValue().entrySet())
                {
                    log.fatal("Flushing producer from component and cluster '" + entry.getKey() + "' and the Topic : '" + producerEntry.getKey() + "' and Producer Index '"
                            + threadBasedProducers.getKey() + "'");

                    try
                    {
                        threadBasedProducers.getValue().flushMessages();
                    }
                    catch (final Exception e)
                    {
                        log.error("Exception while stoppping the Producer '" + entry.getKey() + "' and  '" + producerEntry.getKey() + "'", e);
                    }
                }
            }
        }
    }

    public int getTotalConsumersCount()
    {
        return mTotalConsumersCount;
    }

    boolean isCheckingStarted = false;

    public boolean isAllProducersCompleted()
    {

        if (isCheckingStarted)
        {
            log.fatal("Checking already stated. No need to have one more time.");
            return false;
        }

        boolean isCompleted = false;

        log.fatal("Checking for the producers to close.");

        if (mKafkaProducerCollection.isEmpty())
            return true;

        int count = 0;
        isCheckingStarted = true;

        while (!isCompleted)
        {
            ++count;
            log.fatal("Checking for the producer close " + count);

            boolean temp = false;

            a:
            for (final Entry<String, Map<String, Map<Integer, Producer>>> entry : mKafkaProducerCollection.entrySet())
            {
                log.fatal("Checking the producers from component and cluster '" + entry.getKey() + "'");

                for (final Entry<String, Map<Integer, Producer>> producerEntry : entry.getValue().entrySet())
                {
                    log.fatal("Checking producer from component and cluster '" + entry.getKey() + "' and the Topic : '" + producerEntry.getKey() + "'");

                    for (final Entry<Integer, Producer> threadBasedProducers : producerEntry.getValue().entrySet())
                    {
                        log.fatal("Checking producer for  component and cluster '" + entry.getKey() + "' and the Topic : '" + producerEntry.getKey() + "' and Producer Index '"
                                + threadBasedProducers.getKey() + "' is Completed " + threadBasedProducers.getValue().isCompleted());

                        temp = threadBasedProducers.getValue().isCompleted();
                        if (!temp)
                            break a;
                    }
                }
            }

            isCompleted = temp;
            if (!isCompleted)
                CommonUtility.sleepForAWhile(100);
        }
        return isCompleted;
    }

}