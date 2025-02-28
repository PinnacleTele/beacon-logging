package com.itextos.beacon.commonlib.messageprocessor.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfo;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.ItextosEnum;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.messageprocessor.data.db.KafkaClusterComponentMap;
import com.itextos.beacon.commonlib.messageprocessor.data.db.KafkaClusterInfo;
import com.itextos.beacon.commonlib.messageprocessor.data.db.KafkaComponentInfo;
import com.itextos.beacon.commonlib.messageprocessor.data.db.KafkaTopicMap;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.smslog.ProducerTopicLog;

public class KafkaDataLoader
        extends
        KafkaDBConstants
{

    private static final Log log = LogFactory.getLog(KafkaDataLoader.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final KafkaDataLoader INSTANCE = new KafkaDataLoader();

    }

    public static KafkaDataLoader getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<ClusterType, Boolean>                                  mPlatformCluster                             = new ConcurrentHashMap<>();

    /**
     * <ul>
     * <li>key - Cluster Name
     * <li>value - Kafka Server Properties
     * </ul>
     */
    private final Map<String, KafkaClusterInfo>                              mKafkaClusterInfo                            = new ConcurrentHashMap<>();

    /**
     * <ul>
     * <li>Key - Component Name
     * <li>value - Process Class Name
     * </ul>
     */
    private final Map<String, KafkaComponentInfo>                            mKafkaComponentProcessInfo                   = new ConcurrentHashMap<>();

    /**
     * <ul>
     * <li>Key - Component Name
     * <li>Value - Map
     * <ul>
     * <li>Key - Platform Cluster Name
     * <li>Value - Component about Kafka Producer Cluster, Kafka Consumer Cluster,
     * Kafka Client Count, Kafka client sleep time, Platform Thread count.
     * </ul>
     * </ul>
     */
    private final Map<String, Map<String, KafkaClusterComponentMap>>         mComponentPlatformClusterKafkaClusterMapInfo = new ConcurrentHashMap<>();

    /**
     * <ul>
     * <li>Key - Component Name
     * <li>Value - Map
     * <ul>
     * <li>Key - Platform Cluster Name
     * <li>Value - <b>List of Topics</b> specific to the component and platform
     * cluster.
     * </ul>
     * </ul>
     */
    private final Map<String, Map<String, String>>                           mComponentKafkaClusterTopics                 = new ConcurrentHashMap<>();

    /**
     * <ul>
     * <li>Key - Component Name
     * <li>Value - <b>List of Topics</b> specific to the component and Clients.
     * Clients can be at User / AdminUser / SuperUser Level.
     * </ul>
     */
    private final Map<String, List<String>>                                  mComponentClientSpecificKafkaTopics          = new ConcurrentHashMap<>();

    /**
     * <ul>
     * <li>Key - Platform Cluster Name
     * <li>Value - Map
     * <ul>
     * <li>Key - Interface Group
     * <li>Value - Map
     * <ul>
     * <li>Key - IMessage Type
     * <li>Value - Map
     * <ul>
     * <li>Key - IMessage Priority
     * <li>Value - Kafka Topic Name
     * </ul>
     * </ul>
     * </ul>
     * </ul>
     */
    private final Map<String, Map<String, Map<String, Map<String, String>>>> mKafkaPriorityTopics                         = new ConcurrentHashMap<>();

    private KafkaDataLoader()
    {
        loadData();
    }

    private void loadData()
    {
        if (log.isDebugEnabled())
            log.debug("Loading initial Kafka Details");

        try (
                final Connection con = DBDataSourceFactory.getConnectionFromThin(JndiInfo.CONFIGURARION_DB))
        {
            loadPlatformClusterInfo(con);
            loadKafkaClusterInfo(con);
            loadKafkaComponentInfo(con);
            loadComponentPfClusterKafkaClusterMapInfo(con);
            loadPriorityTopicMap(con);
            loadClientSpecificComponent(con);
        }
        catch (final Exception e)
        {
            log.error("Exception while processig the records ", e);
            System.exit(-1);
        }
    }

    private void loadPlatformClusterInfo(
            Connection aCon)
            throws Exception
    {
        final String sql = "select cluster_name, is_specific_instance from " + TABLE_NAME_PLATFORM_CLUSTER;

        try (
                PreparedStatement pstmt = aCon.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();)
        {

            while (rs.next())
            {
                final ClusterType platFormCluster    = ClusterType.getCluster(CommonUtility.nullCheck(rs.getString("cluster_name"), true));
                final boolean     isSeparateInstance = CommonUtility.isEnabled(rs.getString("is_specific_instance"));

                if (platFormCluster == null)
                    throw new ItextosException("Invalid cluster specified. From Database '" + rs.getString("cluster_name") + "'");

                mPlatformCluster.put(platFormCluster, isSeparateInstance);
            }

            if (log.isDebugEnabled())
                log.debug("Kafka Cluster data loaded.");
        }
        catch (final Exception e)
        {
            log.error("Exception while loading the Kafka Cluster information.", e);
            throw e;
        }
    }

    private void loadKafkaClusterInfo(
            Connection aCon)
            throws Exception
    {
        final String sql = "select kafka_cluster_name, producer_properties_file_path, consumer_properties_file_path from " + TABLE_NAME_KAFKA_CLUSTER + " where is_active = 1";

        try (
                PreparedStatement pstmt = aCon.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();)
        {

            while (rs.next())
            {
                final KafkaClusterInfo lKafkaClusterInfo = getKafkaClusterInfo(rs);

                if (log.isDebugEnabled())
                    log.debug("KafkaClusterInfo " + lKafkaClusterInfo);

                if (lKafkaClusterInfo == null)
                    continue;

                mKafkaClusterInfo.put(lKafkaClusterInfo.getKafkaClusterName(), lKafkaClusterInfo);
            }

            if (log.isDebugEnabled())
                log.debug("Kafka Cluster data loaded.");
        }
        catch (final Exception e)
        {
            log.error("Exception while loading the Kafka Cluster information.", e);
            throw e;
        }
    }

    private void loadKafkaComponentInfo(
            Connection aCon)
            throws Exception
    {
        final String sql = "select component_name, processor_class_name from " + TABLE_NAME_KAFKA_COMPONENT;

        try (
                PreparedStatement pstmt = aCon.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();)
        {

            while (rs.next())
            {
                final KafkaComponentInfo lKafkaComponentInfo = getKafkaComponentInfo(rs);

                if (log.isDebugEnabled())
                    log.debug("KafkaComponentInfo " + lKafkaComponentInfo);

                if (lKafkaComponentInfo == null)
                    continue;

                mKafkaComponentProcessInfo.put(lKafkaComponentInfo.getKey(), lKafkaComponentInfo);
            }

            if (log.isDebugEnabled())
                log.debug("Kafka Component Processes data loaded. Total Components : " + mKafkaComponentProcessInfo.size());
        }
        catch (final Exception e)
        {
            log.error("Exception while loading the Kafka Component Processor information.", e);
            throw e;
        }
    }

    public Set<String> getComponentClusters(
            Component aComponent)
    {
        final Map<String, KafkaClusterComponentMap> lMap = mComponentPlatformClusterKafkaClusterMapInfo.get(aComponent.getKey());
        if (lMap != null)
            return lMap.keySet();
        return null;
    }

    private void loadComponentPfClusterKafkaClusterMapInfo(
            Connection aCon)
            throws SQLException
    {
        final String sql = "select" //
                + " component_name, platform_cluster_name, kafka_cluster_producer," //
                + " kafka_cluster_consumer, consumer_group_name, kafka_client_consumer_count," //
                + " sleep_time_in_millis, threads_count, intl_threads_count, max_producers_per_topic " //
                + " from " //
                + TABLE_NAME_PLATFORM_CLUSTER_COMPONENT_KAFKA_CLUSTER_MAP;

        try (
                PreparedStatement pstmt = aCon.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();)
        {

            while (rs.next())
            {
                final KafkaClusterComponentMap lComponentKafkaClusterMap = getKafkaClusterComponentInfo(rs);

                if (log.isDebugEnabled())
                    log.debug("ComponentPlatformClusterKafkaClusterMap " + lComponentKafkaClusterMap);

                if ((lComponentKafkaClusterMap == null))
                    continue;

                final Map<String, KafkaClusterComponentMap> clusterComponentInfo = mComponentPlatformClusterKafkaClusterMapInfo.computeIfAbsent(lComponentKafkaClusterMap.getComponent().getKey(),
                        k -> new ConcurrentHashMap<>());
                final String                                pfClusterName        = KafkaDataLoaderUtility.getNameOrDefault(lComponentKafkaClusterMap.getPlatformClusterType());
                clusterComponentInfo.put(pfClusterName, lComponentKafkaClusterMap);

                addTopicsList(lComponentKafkaClusterMap);
            }

            if (log.isDebugEnabled())
                log.debug("Kafka Cluster component map data loaded.");
        }
        catch (final Exception e)
        {
            log.error("Exception while loading the Kafka cluster platform component information.", e);
            throw e;
        }
    }

    private void loadPriorityTopicMap(
            Connection aCon)
            throws SQLException
    {
        final String sql = "select platform_cluster_name, interface_group_name, msg_type, priority, kafka_topic_prefix from " + TABLE_NAME_PLATFORM_CLUSTER_KAFKA_TOPIC_MAP;

        try (
                PreparedStatement pstmt = aCon.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();)
        {

            while (rs.next())
            {
                final List<KafkaTopicMap> lPfClusterIntfGrpMsgTypeMsgPriKafkaTopicMap = getKafkaTopicMap(rs);

                if (log.isDebugEnabled())
                    log.debug("PlatformClusterIntfGrpMsgTypeMsgPriKafkaTopicMap : " + lPfClusterIntfGrpMsgTypeMsgPriKafkaTopicMap);

                if (lPfClusterIntfGrpMsgTypeMsgPriKafkaTopicMap.isEmpty())
                    continue;

                for (final KafkaTopicMap ktp : lPfClusterIntfGrpMsgTypeMsgPriKafkaTopicMap)
                    addTopicsList(ktp);
            }

            if (log.isDebugEnabled())
                log.debug("Kafka Topic Map data loaded.");
        }
        catch (final Exception e)
        {
            log.error("Exception while loading the Priority based Kafka Topic information.", e);
            throw e;
        }
    }

    private void loadClientSpecificComponent(
            Connection aCon)
            throws SQLException
    {
        final String sql = "select component_name, cli_id from " + TABLE_NAME_CLIENT_SPECIFIC_COMPONENT;

        try (
                PreparedStatement pstmt = aCon.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();)
        {

            while (rs.next())
            {
                final String    componentName = rs.getString(COL_INDEX_CPC_COMPONENT_NAME);
                final String    clientId      = rs.getString(COL_INDEX_CPC_CLIENT_ID);
                final Component component     = Component.getComponent(componentName);

                if (log.isDebugEnabled())
                {
                    log.debug("Component Name : '" + componentName + "' Component : '" + component + "'");
                    log.debug("Client Id      : '" + clientId + "'");
                }

                if (!validObject(component, componentName, Component.class))
                    continue;

                if ("".equals(clientId))
                {
                    log.error("Client Id is not valid for the component '" + componentName + "'");
                    continue;
                }

                addTopicsList(component, clientId);
            }

            if (log.isDebugEnabled())
                log.debug("Client specific components data loaded. ");
        }
        catch (final Exception e)
        {
            log.error("Exception while loading the Client specific Kafka Topic information.", e);
            throw e;
        }
    }

    private static List<KafkaTopicMap> getKafkaTopicMap(
            ResultSet aResultset)
            throws SQLException
    {
        final String            pfClusterName   = aResultset.getString(COL_INDEX_PCKTM_PLATFORM_CLUSTER_NAME);
        final String            intfGroupName   = aResultset.getString(COL_INDEX_PCKTM_INTERFACE_GROUP_NAME);
        final String            msgTypeName     = aResultset.getString(COL_INDEX_PCKTM_MSG_TYPE);
        final String            msgPriName      = aResultset.getString(COL_INDEX_PCKTM_MSG_PRIORITY);
        final String            kafkaPrefix     = aResultset.getString(COL_INDEX_PCKTM_KAFKA_TOPIC_PREFIX);

        final List<ItextosEnum> clusterTypeList = KafkaDataLoaderUtility.getClusters(pfClusterName);
        final List<ItextosEnum> intfGroupList   = KafkaDataLoaderUtility.getInterfaceGroups(intfGroupName);
        final MessageType       msgType         = MessageType.getMessageType(msgTypeName);
        final List<ItextosEnum> msgPriorityList = KafkaDataLoaderUtility.getPriorities(msgPriName);

        if (log.isDebugEnabled())
        {
            log.debug("Platform Cluster Name : '" + pfClusterName + "' Platform Cluster : '" + clusterTypeList + "'");
            log.debug("Interface Group Name  : '" + intfGroupName + "' Interface Group : '" + intfGroupList + "'");
            log.debug("IMessage Type Name     : '" + msgTypeName + "' IMessage Type : '" + msgType + "'");
            log.debug("IMessage Priority Name : '" + msgPriName + "' IMessage Priority : '" + Arrays.asList(msgPriorityList) + "'");
            log.debug("Kafka Prefix Name     : '" + kafkaPrefix + "'");
        }

        final List<KafkaTopicMap> returnValue = new ArrayList<>();

        if ((!clusterTypeList.isEmpty() || !intfGroupList.isEmpty() || (msgType != null) || !msgPriorityList.isEmpty()))
        {
            final List<String> clusterNames = KafkaDataLoaderUtility.getNames(clusterTypeList);
            final List<String> igNames      = KafkaDataLoaderUtility.getNames(intfGroupList);
            final List<String> msgPriNames  = KafkaDataLoaderUtility.getNames(msgPriorityList);
            final String       msgTypeName1 = KafkaDataLoaderUtility.getNameOrDefault(msgType);

            for (final String cluName : clusterNames)
                for (final String igName : igNames)
                    for (final String msgPri : msgPriNames)
                        returnValue.add(new KafkaTopicMap(cluName, igName, msgTypeName1, msgPri, kafkaPrefix));
        }

        return returnValue;
    }

    private static KafkaClusterComponentMap getKafkaClusterComponentInfo(
            ResultSet aResultset)
            throws SQLException
    {
        final String      componentName            = aResultset.getString(COL_INDEX_PCCKCM_COMPONENT_NAME);
        final String      pfClusterName            = aResultset.getString(COL_INDEX_PCCKCM_PLATFORM_CLUSTER_NAME);

        final String      kafkaProducerCluster     = CommonUtility.nullCheck(aResultset.getString(COL_INDEX_PCCKCM_KAFKA_CLUSTER_PRODUCER), true);
        final String      kafkaConsumerCluster     = CommonUtility.nullCheck(aResultset.getString(COL_INDEX_PCCKCM_KAFKA_CLUSTER_CONSUMER), true);
        final String      kafkaConsumerGroupName   = CommonUtility.nullCheck(aResultset.getString(COL_INDEX_PCCKCM_CONSUMER_GROUP_NAME), true);
        int               kafkaClientConsumerCount = aResultset.getInt(COL_INDEX_PCCKCM_KAFKA_CLIENT_CONSUMER_COUNT);
        int               sleepTimeInMillis        = aResultset.getInt(COL_INDEX_PCCKCM_SLEEP_TIME_IN_MILLIS);
        int               threadCounts             = aResultset.getInt(COL_INDEX_PCCKCM_THREADS_COUNT);
        int               intlThreadCounts         = aResultset.getInt(COL_INDEX_PCCKCM_INTL_THREADS_COUNT);
        int               maxProducersPerTopic     = aResultset.getInt(COL_INDEX_PCCKCM_MAX_PRODUCERS_PER_TOPIC);

        final Component   component                = Component.getComponent(componentName);
        final ClusterType clusterType              = ClusterType.getCluster(pfClusterName);

        if (log.isDebugEnabled())
        {
            log.debug("Component Name                 : '" + componentName + "' Component : '" + component + "'");
            log.debug("Platform Cluster Name          : '" + pfClusterName + "' Platform Cluster : '" + clusterType + "'");
            log.debug("Kafka Producer Cluster Name    : '" + kafkaProducerCluster + "'");
            log.debug("Kafka Consumer Cluster Name    : '" + kafkaConsumerCluster + "'");
            log.debug("Kakfa Consumer Group Name      : '" + kafkaConsumerGroupName + "'");
            log.debug("Kakfa Client Consumer count    : '" + kafkaClientConsumerCount + "'");
            log.debug("Component Sleep Time in Millis : '" + sleepTimeInMillis + "'");
            log.debug("Component Thread Count         : '" + threadCounts + "'");
            log.debug("Component Intl Thread Count    : '" + intlThreadCounts + "'");
            log.debug("Max Producers Per Topic        : '" + maxProducersPerTopic + "'");
        }

        if (!validObject(component, componentName, Component.class))
            return null;

        if ("".equals(kafkaProducerCluster))
        {
            log.error("Kafka Producer Cluster is not valid '" + kafkaProducerCluster + "'");
            return null;
        }

        if ("".equals(kafkaConsumerCluster))
        {
            log.error("Kafka Consumer Cluster is not valid '" + kafkaConsumerCluster + "'");
            return null;
        }

        if ("".equals(kafkaConsumerGroupName))
        {
            log.error("Kafka Consumer Group name is not valid '" + kafkaConsumerGroupName + "'");
            return null;
        }

        if (kafkaClientConsumerCount <= 0)
        {
            log.error("Invalid Kafka Client Consumer count specified. " + kafkaClientConsumerCount);
            kafkaClientConsumerCount = 2;
        }

        if (sleepTimeInMillis <= 0)
        {
            log.error("Invalid Component Sleep Time specified. " + sleepTimeInMillis);
            sleepTimeInMillis = 500;
        }

        if (threadCounts <= 0)
        {
            log.error("Invalid Component Thread Count specified. " + threadCounts);
            threadCounts = 2;
        }

        if (intlThreadCounts <= 0)
        {
            log.error("Invalid Component Intl Thread Count specified. " + intlThreadCounts);
            intlThreadCounts = 1;
        }

        if (maxProducersPerTopic <= 0)
        {
            log.error("Invalid Component Max Producers Per Thread specified. " + maxProducersPerTopic);
            maxProducersPerTopic = 1;
        }

        return new KafkaClusterComponentMap(component, clusterType, kafkaProducerCluster, kafkaConsumerCluster, kafkaConsumerGroupName, kafkaClientConsumerCount, sleepTimeInMillis, threadCounts,
                intlThreadCounts, maxProducersPerTopic);
    }

    private void addTopicsList(
            Component aComponent,
            String aClientId)
    {
        final StringJoiner sjTopicName = new StringJoiner(TOPIC_SEPARATOR + "");
        sjTopicName.add(aComponent.getKey());
        sjTopicName.add(aClientId);
        final List<String> clientSpecicTopics = mComponentClientSpecificKafkaTopics.computeIfAbsent(aComponent.getKey(), k -> new ArrayList<>());
        clientSpecicTopics.add(KafkaDataLoaderUtility.updateTopicName(sjTopicName.toString()));
    }

    private void addTopicsList(
            KafkaTopicMap aKafkaTopicMap)
    {
        final Map<String, Map<String, Map<String, String>>> plClusterMap  = mKafkaPriorityTopics.computeIfAbsent(aKafkaTopicMap.getPlatformCluster(), k -> new ConcurrentHashMap<>());
        final Map<String, Map<String, String>>              lIntfGroupMap = plClusterMap.computeIfAbsent(aKafkaTopicMap.getInterfaceGroup(), k -> new ConcurrentHashMap<>());
        final Map<String, String>                           lMsgTypeMap   = lIntfGroupMap.computeIfAbsent(aKafkaTopicMap.getMessageType(), k -> new ConcurrentHashMap<>());
        final String                                        lString       = lMsgTypeMap.get(aKafkaTopicMap.getMessagePriority());

        if (lString != null)
            log.error("Already an entry is available for this combinatin. Please check inn the tbale '" + TABLE_NAME_PLATFORM_CLUSTER_KAFKA_TOPIC_MAP + "'", new ItextosException());
        else
            lMsgTypeMap.put(aKafkaTopicMap.getMessagePriority(), aKafkaTopicMap.getKafkaTopicPrefix());
    }

    private void addTopicsList(
            KafkaClusterComponentMap aKafkaClusterComponentMap)
    {
        final ClusterType  lPlatformCluster = aKafkaClusterComponentMap.getPlatformClusterType();

        final StringJoiner sjTopicName      = new StringJoiner(TOPIC_SEPARATOR + "");
        KafkaDataLoaderUtility.addToTopic(sjTopicName, aKafkaClusterComponentMap.getComponent());
        KafkaDataLoaderUtility.addToTopic(sjTopicName, lPlatformCluster);
        final String              topicName     = KafkaDataLoaderUtility.updateTopicName(sjTopicName.toString());
        final Map<String, String> platformTopic = mComponentKafkaClusterTopics.computeIfAbsent(aKafkaClusterComponentMap.getComponent().getKey(), k -> new HashMap<>());
        final String              pfClusterName = KafkaDataLoaderUtility.getNameOrDefault(lPlatformCluster);
//        final String              pfClusterName = lPlatformCluster.getKey();
        
        platformTopic.put(pfClusterName, topicName);
    }

    private static KafkaComponentInfo getKafkaComponentInfo(
            ResultSet aResultset)
            throws SQLException
    {
        final String    kafkaComponentName      = aResultset.getString(COL_INDEX_KCM_COMPONENT);
        final String    kafkaProcessorClassName = CommonUtility.nullCheck(aResultset.getString(COL_INDEX_KCM_PROCESSOR_CLASS_NAME), true);
        final Component component               = Component.getComponent(CommonUtility.nullCheck(kafkaComponentName, true));

        if (log.isDebugEnabled())
        {
            log.debug("Kafka Component Name       : '" + kafkaComponentName + "' Component '" + component + "'");
            log.debug("Kafka Processor Class Name : '" + kafkaProcessorClassName + "'");
        }

        if (!validObject(component, kafkaComponentName, Component.class))
            return null;

        if (kafkaProcessorClassName.isBlank())
        {
            log.error("Invalid class name specified for the component '" + kafkaComponentName + "'");
            return null;
        }
        return new KafkaComponentInfo(component, kafkaProcessorClassName);
    }

    private static KafkaClusterInfo getKafkaClusterInfo(
            ResultSet aResultset)
            throws SQLException
    {
        final String kafkaClusterName          = aResultset.getString(COL_INDEX_KC_KAFKA_CLUSTER_NAME);
        final String kafkaProducerPropertypath = aResultset.getString(COL_INDEX_KC_PRODUCER_PROPERTIES_FILE_PATH);
        final String kafkaConsumerPropertypath = aResultset.getString(COL_INDEX_KC_CONSUMER_PROPERTIES_FILE_PATH);

        if (log.isDebugEnabled())
            log.debug("Cluster '" + kafkaClusterName + "' Producer Properties file path '" + kafkaProducerPropertypath + "' Consumer Properties file path '" + kafkaConsumerPropertypath + "'");

        if ((kafkaClusterName == null) || kafkaClusterName.isBlank() || (kafkaProducerPropertypath == null) || kafkaProducerPropertypath.isBlank() || (kafkaConsumerPropertypath == null)
                || kafkaConsumerPropertypath.isBlank())
        {
            log.error("Cannot process the record for the cluster " + kafkaClusterName + "', Producer Properties '" + kafkaProducerPropertypath + "' Consumer Properties '" + kafkaConsumerPropertypath
                    + "'", new ItextosException());
            return null;
        }

        return new KafkaClusterInfo(CommonUtility.nullCheck(kafkaClusterName, true), CommonUtility.nullCheck(kafkaProducerPropertypath, true),
                CommonUtility.nullCheck(kafkaConsumerPropertypath, true));
    }

    private static boolean validObject(
            Object aObject,
            String aName,
            Class<?> aClass)
    {

        if (aObject == null)
        {
            final String s = aClass.getTypeName() + " is null";
            log.error("Unable to process as " + aClass.getTypeName() + " is null for '" + aName + "'", new ItextosException(s));
            return false;
        }
        return true;
    }

    public String getClientBasedTopic(
            Component aComponent,
            String aClientId)
    {

        try
        {
            final List<String> lList = mComponentClientSpecificKafkaTopics.get(aComponent.getKey());

            if ((lList == null) || lList.isEmpty())
            {
                if (log.isDebugEnabled())
                    log.debug("There is no client specific topic assigned for the component '" + aComponent + "'");
            }
            else
            {
                final ItextosClient lIextosClient = new ItextosClient(aClientId);

                String              tempTopic     = KafkaDataLoaderUtility.updateTopicName(CommonUtility.combine(TOPIC_SEPARATOR, aComponent.getKey(), lIextosClient.getClientId()));

                if (lList.contains(tempTopic))
                {
                    if (log.isDebugEnabled())
                        log.debug("Topic identified in User level. Topic Name '" + tempTopic + "'");
                    return tempTopic;
                }

                tempTopic = KafkaDataLoaderUtility.updateTopicName(CommonUtility.combine(TOPIC_SEPARATOR, aComponent.getKey(), lIextosClient.getAdmin()));

                if (lList.contains(tempTopic))
                {
                    if (log.isDebugEnabled())
                        log.debug("Topic identified in Admin User level. Topic Name '" + tempTopic + "'");
                    return tempTopic;
                }

                tempTopic = KafkaDataLoaderUtility.updateTopicName(CommonUtility.combine(TOPIC_SEPARATOR, aComponent.getKey(), lIextosClient.getSuperAdmin()));

                if (lList.contains(tempTopic))
                {
                    if (log.isDebugEnabled())
                        log.debug("Topic identified in Super User level. Topic Name '" + tempTopic + "'");
                    return tempTopic;
                }

                if (log.isDebugEnabled())
                    log.debug("Unable to identify the Topic based on client id '" + aClientId + "' for the Component '" + aComponent + "'");
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while Getting the Client specific Kafka Topic.", e);
        }
        return null;
    }

    public String getTopicNameBasedOnPriorityForProducer(
            Component aComponent,
            ClusterType aPassedCluster,
            InterfaceGroup aPassedIntfGroup,
            MessageType aPassedMsgType,
            MessagePriority aPassedMsgPriority)
    {

        try
        {
            final String                                  passedClusterKeyName  = KafkaDataLoaderUtility.getNameOrDefault(aPassedCluster);
           final String                                  passedIntfGroupName   = KafkaDataLoaderUtility.getNameOrDefault(aPassedIntfGroup);
        //    final String                                  passedIntfGroupName   = DEFAULT;
            
            final String                                  passedMessageType     = KafkaDataLoaderUtility.getNameOrDefault(aPassedMsgType);
            final String                                  passedMessagePriority = KafkaDataLoaderUtility.getNameOrDefault(aPassedMsgPriority);

            ProducerTopicLog.log("getTopicNameBasedOnPriorityForProducer : passedClusterKeyName : "+passedClusterKeyName +" used for search : passedClusterKeyName : "+passedClusterKeyName.toLowerCase());

            ProducerTopicLog.log("getTopicNameBasedOnPriorityForProducer : passedMessageType : "+passedMessageType+ " aPassedMsgType : "+aPassedMsgType);
            ProducerTopicLog.log("getTopicNameBasedOnPriorityForProducer : passedMessagePriority : "+passedMessagePriority+ " aPassedMsgPriority : "+aPassedMsgPriority);
            ProducerTopicLog.log("getTopicNameBasedOnPriorityForProducer : passedIntfGroupName : "+passedIntfGroupName+ " aPassedIntfGroup : "+ aPassedIntfGroup);


            Map<String, Map<String, Map<String, String>>> clusterMap            = mKafkaPriorityTopics.get(passedClusterKeyName.toLowerCase());
            
            ProducerTopicLog.log(" mKafkaPriorityTopics : "+mKafkaPriorityTopics);

            if (clusterMap == null)
                clusterMap = mKafkaPriorityTopics.get(KafkaDataLoaderUtility.getNameOrDefault(null));

            if (clusterMap != null)
            {
                Map<String, Map<String, String>> intfGroupMap = clusterMap.get(passedIntfGroupName);

                if (intfGroupMap == null)
                    intfGroupMap = clusterMap.get(DEFAULT);

                if (intfGroupMap != null)
                {
                    Map<String, String> msgTypeMap = intfGroupMap.get(passedMessageType);

                    if (msgTypeMap == null)
                        msgTypeMap = intfGroupMap.get(DEFAULT);

                    if (msgTypeMap != null)
                    {
                        final String topicName = msgTypeMap.get(passedMessagePriority);

                        if (topicName == null)
                            return msgTypeMap.get(DEFAULT);
                        return topicName;
                    }else {
                    	
                    }
                }else {
                    ProducerTopicLog.log("getTopicNameBasedOnPriorityForProducer : intfGroupMap is null : "+intfGroupMap);

                }
            }{
                ProducerTopicLog.log("getTopicNameBasedOnPriorityForProducer : clusterMap is null : "+clusterMap);

            }
        }
        catch (final Exception e)
        {
            log.error("Exception while getting Priority based topics. Component :'" + aComponent + "', ClusterType :'" + aPassedCluster + "', InterfaceGroup :'" + aPassedIntfGroup
                    + "', MessageType :'" + aPassedMsgType + "', MessagePriority :'" + aPassedMsgPriority + "'", e);
        }
        return null;
    }

    public Map<String, String> getDefaultTopicName(
            Component aComponent)
    {
        return mComponentKafkaClusterTopics.computeIfAbsent(aComponent.getKey(), k -> new ConcurrentHashMap<>());
    }

    public String getDefaultTopicName(
            Component aComponent,
            ClusterType aPlatformCluster)
    {

        try
        {
            final Map<String, String> lClusterTopicList   = mComponentKafkaClusterTopics.get(aComponent.getKey());
   //         final String              platformClusterName = KafkaDataLoaderUtility.getNameOrDefault(aPlatformCluster);
     
            final String              platformClusterName = aPlatformCluster.getKey();
            
            return lClusterTopicList.get(platformClusterName);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the default topic name for the component '" + aComponent + "', Platform Cluster '" + aPlatformCluster + "'", e);
        }
        return null;
    }

    public static String getKafkaClusterComponentMapKeyName(
            Component aComponent,
            ClusterType aClusterType)
    {

        try
        {
            final String platformClusterName = KafkaDataLoaderUtility.getNameOrDefault(aClusterType);
            return CommonUtility.combine(aComponent.getKey(), platformClusterName);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Kafka Cluster Component Map Key name for the Cluster " + aClusterType + " Component " + aComponent, e);
        }
        return null;
    }

    public KafkaClusterComponentMap getKafkaClusterComponentMap(
            Component aComponent,
            ClusterType aPlatformCluster)
    {

        try
        {
            final Map<String, KafkaClusterComponentMap> lMap = mComponentPlatformClusterKafkaClusterMapInfo.get(aComponent.getKey());

            if (lMap != null)
            {
                final String                   platformClusterName       = KafkaDataLoaderUtility.getNameOrDefault(aPlatformCluster);
                final KafkaClusterComponentMap lKafkaClusterComponentMap = lMap.get(platformClusterName);
                return lKafkaClusterComponentMap == null ? lMap.get(DEFAULT) : lKafkaClusterComponentMap;
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Kafka Cluster Component Map for Component '" + aComponent + "' Cluster Type : '" + aPlatformCluster + "'", e);
        }
        return null;
    }

    public KafkaClusterInfo getKafkaClusterInfo(
            String aKafkaProducerClusterName)
    {
        return mKafkaClusterInfo.get(aKafkaProducerClusterName);
    }

    public KafkaComponentInfo getKafkaProcessorInfo(
            Component aComponent)
    {
        if (log.isDebugEnabled())
            log.debug("Getting Kafka Processor Info for Component " + aComponent);
        return mKafkaComponentProcessInfo.get(aComponent.getKey());
    }

    public List<String> getClientBasedTopic(
            Component aComponent)
    {

        try
        {
            final List<String> clientBasedTopics = new ArrayList<>();
            final List<String> lClientBasedTopic = mComponentClientSpecificKafkaTopics.get(aComponent.getKey());
            if ((lClientBasedTopic != null) && !lClientBasedTopic.isEmpty())
                for (final String cliId : lClientBasedTopic)
                    clientBasedTopics.add(KafkaDataLoaderUtility.updateTopicName(CommonUtility.combine(TOPIC_SEPARATOR, cliId)));
            return clientBasedTopics;
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Client based topic for the component " + aComponent, e);
        }
        return null;
    }

    public Map<String, List<String>> getTopicNameBasedOnPriorities(
            Component aComponent,
            ClusterType aCluster)
    {

        try
        {
            final Map<String, List<String>> returnValue                 = new HashMap<>();

            final List<String>              lTopicNameBasedOnPriorities = getTopicNameBasedOnPriorities(aComponent, aCluster, null);
            final String                    clusterKey                  = KafkaDataLoaderUtility.getNameOrDefault(aCluster);
            returnValue.put(clusterKey, lTopicNameBasedOnPriorities);
            return returnValue;
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Priority based topic for the component '" + aComponent + "' and Platform Cluster '" + aCluster + "'", e);
        }
        return null;
    }

    private List<String> getTopicNameBasedOnPriorities(
            Component aComponent,
            ClusterType aCluster,
            InterfaceGroup aIntfGroup)
    {
        return getTopicNameBasedOnPriorities(aComponent, aCluster, aIntfGroup, null);
    }

    private List<String> getTopicNameBasedOnPriorities(
            Component aComponent,
            ClusterType aCluster,
            InterfaceGroup aIntfGroup,
            MessageType aMessageType)
    {
        return getTopicNameBasedOnPriorities(aComponent, aCluster, aIntfGroup, aMessageType, null);
    }

    public List<String> getTopicNameBasedOnPriorities(
            Component aComponent,
            ClusterType aCluster,
            InterfaceGroup aIntfGroup,
            MessageType aMsgType,
            MessagePriority aMsgPriority)
    {

        try
        {
            final List<String>                                  priorityBasedTopics = new ArrayList<>();
            final String                                        pfClusterName       = KafkaDataLoaderUtility.getNameOrDefault(aCluster);
            final Map<String, Map<String, Map<String, String>>> clusterMap          = mKafkaPriorityTopics.get(pfClusterName);
            final List<String>                                  tempTopics          = new ArrayList<>();

            if (clusterMap != null)
            {

               

                for (final String s : tempTopics)
                {
                    final StringJoiner sjTopicName = new StringJoiner(TOPIC_SEPARATOR + "");

                    KafkaDataLoaderUtility.addToTopic(sjTopicName, aComponent);
                    // KafkaDataLoaderUtility.addToTopic(sjTopicName, aCluster);
                    sjTopicName.add(s);
                    final String topicName = sjTopicName.toString();


                    priorityBasedTopics.add(KafkaDataLoaderUtility.updateTopicName(topicName));
                }
            }
            return priorityBasedTopics;
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Priority based topic for the Component '" + aComponent + "' ClusterType '" + aCluster + "' InterfaceGroup '" + aIntfGroup + "' MessageType '"
                    + aMsgType + "' MessagePriority '" + aMsgPriority + "'", e);
        }
        return null;
    }

    
  




  
}