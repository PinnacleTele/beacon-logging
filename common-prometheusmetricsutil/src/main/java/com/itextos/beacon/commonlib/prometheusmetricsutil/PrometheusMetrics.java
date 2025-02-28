package com.itextos.beacon.commonlib.prometheusmetricsutil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.prometheusmetricsutil.smpp.Action;
import com.itextos.beacon.commonlib.prometheusmetricsutil.smpp.RequestType;
import com.itextos.beacon.commonlib.prometheusmetricsutil.smpp.SmppPrometheusInfo;
import com.itextos.beacon.commonlib.utility.CommonUtility;

import io.prometheus.client.Histogram.Timer;

public class PrometheusMetrics
        extends
        PrometheusMetricsConstants
{

    private static final Log log = LogFactory.getLog(PrometheusMetrics.class);

    private PrometheusMetrics()
    {}

    public static void registerServer()
    {

        try
        {
            PrometheusDataHolder.startPrometheusServer();

            if (log.isDebugEnabled())
                log.debug("Prometheus server started successfully.");
        }
        catch (final Exception e)
        {
            log.error("Exception while starting the Prometheus Server. It should not impact the application process.", e);
        }
    }

    public static void registerKafkaCountersMetrics()
    {

        try
        {

            if (PrometheusController.getInstance().isKafkaProducerCounterEnabled())
            {
                PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, KAFKA_PRODUCER_COUNT, "Kafka Counter for Producer", TOPIC);
                if (log.isDebugEnabled())
                    log.debug("Prometheus Platform counter registered.");
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while registering Prometheus metrics for Kafka Producer Counter. It should not impact the application process.", e);
        }

        try
        {

            if (PrometheusController.getInstance().isKafkaConsumerCounterEnabled())
            {
                PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, KAFKA_CONSUMER_COUNT, "Kafka Counter for Producer", TOPIC);
                if (log.isDebugEnabled())
                    log.debug("Prometheus Platform counter registered.");
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while registering Prometheus metrics for Kafka Consumer Counter. It should not impact the application process.", e);
        }
    }

    public static void registerPlatformMetrics()
    {

        try
        {

            if (PrometheusController.getInstance().isPlatformCounterEnabled())
            {
                PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, COMPONENT_MESSAGE_COUNT, "Platform Counter for Messages", COMPONENT_NAME, CLUSTER_NAME, TOPIC, IP);
                if (log.isDebugEnabled())
                    log.debug("Prometheus Platform component counter registered.");
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while registering Prometheus metrics for Platform component Counter. It should not impact the application process.", e);
        }

        try
        {

            if (PrometheusController.getInstance().isPlatformLatencyEnabled())
            {
                PrometheusDataHolder.getInstance().createMetrics(MetricType.HISTOGRAM, COMPONENT_PROCESS_LATENCY, PrometheusController.getInstance().getHistogramBuckgets(),
                        "Platform Message Process Latency", COMPONENT_NAME, CLUSTER_NAME, TOPIC);
                if (log.isDebugEnabled())
                    log.debug("Prometheus Platform Latency registered.");
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while registering Prometheus metrics for Platform component Latency. It should not impact the application process.", e);
        }

        try
        {

            if (PrometheusController.getInstance().isComponentMethodLatencyEnabled())
            {
                PrometheusDataHolder.getInstance().createMetrics(MetricType.HISTOGRAM, COMPONENT_METHOD_LATENCY, PrometheusController.getInstance().getHistogramBuckgets(),
                        "Platform Message Process Latency", COMPONENT_NAME, CLUSTER_NAME, METHOD_NAME);
                if (log.isDebugEnabled())
                    log.debug("Prometheus Component Method Latency registered.");
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while registering Prometheus metrics for Platform component Method wise latency. It should not impact the application process.", e);
        }
    }

    public static void registerApiMetrics()
    {

       RegisterApiMetrics.getInstance();
    }

 


   

 

 

    public static void registerUIMetrics()
    {

        try
        {

            if (PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.UI) && PrometheusController.getInstance().isUiCounterEnabled())
            {
                PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, UI_MESSAGE_COUNT, "Counter for UI Messages", USER, IP);
                if (log.isDebugEnabled())
                    log.debug("Prometheus UI counter registered.");
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while registering Prometheus metrics for UI Counter. It should not impact the application process.", e);
        }
    }

    public static void registerFtpMetrics()
    {

        try
        {

            if (PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.FTP) && PrometheusController.getInstance().isFtpCounterEnabled())
            {
                PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, FTP_MESSAGE_COUNT, "Counter for FTP Messages", USER, IP);
                if (log.isDebugEnabled())
                    log.debug("Prometheus FTP counter registered.");
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while registering Prometheus metrics for FTP Counter. It should not impact the application process.", e);
        }
    }

    public static void registerPlatformRejection()
    {

        try
        {

            if (PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.PLATFORM_REJECTIONS))
            {
                PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, PLATFORM_REJECTION_COUNTS, "Counter for Platform Rejection", COMPONENT_NAME, CLUSTER_NAME, IP, PLATFORM_ERROR_CODE,
                        PLATFORM_ERROR_DESC);
                if (log.isDebugEnabled())
                    log.debug("Prometheus Platform Rejection counter registered.");
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while registering Prometheus metrics for Platform Rejection Counter. It should not impact the application process.", e);
        }
    }

    public static void registerGenericError()
    {

        try
        {

            if (PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.GENERIC_ERROR))
            {
                PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, GENERIC_ERROR_COUNTS, "Counter for Generic Error Counts", COMPONENT_NAME, CLUSTER_NAME, IP, GENERIC_ERROR_CODE,
                        GENERIC_ERROR_DESC);
                if (log.isDebugEnabled())
                    log.debug("Prometheus Generic Error Counter registered.");
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while registering Prometheus metrics for Platform Rejection Counter. It should not impact the application process.", e);
        }
    }

    public static void registerSmppMetrics() throws ItextosRuntimeException
    {
        registerBindMetrics();
        registerEnquireLinkMetrics();
        registerSubmitSmMetrics();
        registerDeliverSmMetrics();
        registerUnbindMetrics();
        registerFaiureCountMetrics();
    }

    private static void registerFaiureCountMetrics() throws ItextosRuntimeException
    {
        PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, SMPP_FAILURE_COUNTS, "Smpp Failure Counts", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, CLIENT_IP, BIND_TYPE, ERROR_CODE,
                REASON);
    }

    private static void registerUnbindMetrics() throws ItextosRuntimeException
    {
        PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, SMPP_UNBIND_REQUEST, "Smpp Unbind Request Count", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, CLIENT_IP, BIND_TYPE);
        PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, SMPP_UNBIND_RESPONSE, "Smpp Unbind Response Count", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, CLIENT_IP, BIND_TYPE);
        PrometheusDataHolder.getInstance().createMetrics(MetricType.HISTOGRAM, SMPP_UNBIND_LATENCY, "Smpp Unbind Latency", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, CLIENT_IP, BIND_TYPE);
        PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, SMPP_UNBIND_COUNTS, "Smpp Unbind Count", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, CLIENT_IP, BIND_TYPE, REASON);
    }

    private static void registerDeliverSmMetrics() throws ItextosRuntimeException
    {
        PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, SMPP_DELIVERSM_REQUEST, "Smpp DeliverSm Request Count", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, BIND_TYPE);
        PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, SMPP_DELIVERSM_RESPONSE, "Smpp DeliverSm Response Count", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, BIND_TYPE);
        PrometheusDataHolder.getInstance().createMetrics(MetricType.HISTOGRAM, SMPP_DELIVERSM_LATENCY, "Smpp DeliverSm Latency", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, BIND_TYPE);
        PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, SMPP_DELIVERSM_FAILURE, "Smpp DeliverSm Failure Count", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, BIND_TYPE, REASON);
    }

    private static void registerSubmitSmMetrics() throws ItextosRuntimeException
    {
        PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, SMPP_SUBMITSM_REQUEST, "Smpp SubmitSm Request Count", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, CLIENT_IP, BIND_TYPE);
        PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, SMPP_SUBMITSM_RESPONSE, "Smpp SubmitSm Response Count", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, CLIENT_IP, BIND_TYPE);
        PrometheusDataHolder.getInstance().createMetrics(MetricType.HISTOGRAM, SMPP_SUBMITSM_LATENCY, "Smpp SubmitSm Latency", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, CLIENT_IP, BIND_TYPE);
    }

    private static void registerEnquireLinkMetrics() throws ItextosRuntimeException
    {
        PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, SMPP_ENQUIRE_LINK_REQUEST, "Smpp EnquireLink Request Count", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, CLIENT_IP, BIND_TYPE);
        PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, SMPP_ENQUIRE_LINK_RESPONSE, "Smpp EnquireLink Response Count", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, CLIENT_IP, BIND_TYPE);
        PrometheusDataHolder.getInstance().createMetrics(MetricType.HISTOGRAM, SMPP_ENQUIRE_LINK_LATENCY, "Smpp EnquireLink Latency", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, CLIENT_IP, BIND_TYPE);
    }

    private static void registerBindMetrics() throws ItextosRuntimeException
    {
        PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, SMPP_BIND_REQUEST, "Smpp Bind Request Count", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, CLIENT_IP, BIND_TYPE);
        PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, SMPP_BIND_RESPONSE, "Smpp Bind Response Count", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, CLIENT_IP, BIND_TYPE);
        PrometheusDataHolder.getInstance().createMetrics(MetricType.HISTOGRAM, SMPP_BIND_LATENCY, "Smpp Bind Latency", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, CLIENT_IP, BIND_TYPE);
        PrometheusDataHolder.getInstance().createMetrics(MetricType.GAUGE, SMPP_ACTIVE_BINDS, "Smpp Active Bind Count", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, CLIENT_IP, BIND_TYPE);
        PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, SMPP_BIND_ERROR, "Smpp Bind Error", CLUSTER_NAME, INSTANCE_ID, SYSTEM_ID, CLIENT_IP, BIND_TYPE, REASON);
    }

    public static void kafkaProducerIncrement(
            String aTopicName,
            int aCount)
    {

        try
        {
            if (PrometheusController.getInstance().isKafkaProducerCounterEnabled())
                PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, KAFKA_PRODUCER_COUNT, aCount, aTopicName);
        }
        catch (final Exception e)
        {
            log.error("Exception while incrementing the Prometheus metrics for Kafka Producer. It should not impact the application process.", e);
        }
    }

    public static void kafkaConsumerIncrement(
            String aTopicName,
            int aCount)
    {

        try
        {
            if (PrometheusController.getInstance().isKafkaProducerCounterEnabled())
                PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, KAFKA_CONSUMER_COUNT, aCount, aTopicName);
        }
        catch (final Exception e)
        {
            log.error("Exception while incrementing the Prometheus metrics for Kafka Consumer. It should not impact the application process.", e);
        }
    }

    public static void platformIncrement(
            Component aComponent,
            ClusterType aPlatformCluster,
            String aTopicName)
    {

        try
        {
            if (PrometheusController.getInstance().isPlatformCounterEnabled(aComponent))
                PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, COMPONENT_MESSAGE_COUNT, aComponent.getKey(), getPlatformName(aPlatformCluster), aTopicName,
                        CommonUtility.getApplicationServerIp());
        }
        catch (final Exception e)
        {
            log.error("Exception while incrementing the Prometheus metrics for Platform Components. It should not impact the application process.", e);
        }
    }

    public static Timer platformStartTimer(
            Component aComponent,
            ClusterType aPlatformCluster,
            String aTopicName)
    {
        Timer returnValue = null;

        try
        {
            if (PrometheusController.getInstance().isPlatformLatencyEnabled(aComponent))
                returnValue = PrometheusDataHolder.getInstance().startTimer(MetricType.HISTOGRAM, COMPONENT_PROCESS_LATENCY, aComponent.getKey(), getPlatformName(aPlatformCluster), aTopicName);
        }
        catch (final Exception e)
        {
            log.error("Exception while starting the Prometheus timer for Platform Components. It should not impact the application process.", e);
        }
        return returnValue;
    }

    public static void platformEndTimer(
            Component aComponent,
            Timer aTimer)
    {

        try
        {
            if (PrometheusController.getInstance().isPlatformLatencyEnabled(aComponent))
                PrometheusDataHolder.getInstance().closeTimer(MetricType.HISTOGRAM, COMPONENT_PROCESS_LATENCY, aTimer);
        }
        catch (final Exception e)
        {
            log.error("Exception while Ending the Prometheus timer for Platfoem Components. It should not impact the application process.", e);
        }
    }

    public static Timer componentMethodStartTimer(
            Component aComponent,
            ClusterType aPlatformCluster,
            String aMethodName)
    {
        Timer returnValue = null;

        try
        {
            if (PrometheusController.getInstance().isComponentMethodLatencyEnabled(aComponent))
                returnValue = PrometheusDataHolder.getInstance().startTimer(MetricType.HISTOGRAM, COMPONENT_METHOD_LATENCY, aComponent.getKey(), getPlatformName(aPlatformCluster), aMethodName);
        }
        catch (final Exception e)
        {
            log.error("Exception while starting the Prometheus Timer for Platform Component Method. It should not impact the application process.", e);
        }
        return returnValue;
    }

    public static void componentMethodEndTimer(
            Component aComponent,
            Timer aTimer)
    {

        try
        {
            if (PrometheusController.getInstance().isComponentMethodLatencyEnabled(aComponent))
                PrometheusDataHolder.getInstance().closeTimer(MetricType.HISTOGRAM, COMPONENT_METHOD_LATENCY, aTimer);
        }
        catch (final Exception e)
        {
            log.error("Exception while Ending the Prometheus Timer for Platform Component Method. It should not impact the application process.", e);
        }
    }

    public static void apiIncrementAcceptCount(
            InterfaceType aInterfaceType,
            String aMessageSource,
            String aClusterName,
            String aClientIp)
    {
        apiIncrementAcceptCount(aInterfaceType, aMessageSource, aClusterName, aClientIp, "");
    }

    public static void apiIncrementAcceptCount(
            InterfaceType aInterfaceType,
            String aMessageSource,
            String aClusterName,
            String aClientIp,
            String aUser)
    {

        try
        {
            if ((PrometheusController.getInstance().getApiController().isApiUserAcceptEnabled(aInterfaceType, aUser))
                    || PrometheusController.getInstance().getApiController().isApiListAcceptEnabled(aInterfaceType) //
                    || PrometheusController.getInstance().getApiController().isApiAllAcceptEnabled())
                PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, API_USER_ACCEPT_COUNT, aInterfaceType.getKey(), aMessageSource, aClusterName, CommonUtility.getApplicationServerIp(),
                        aClientIp, aUser);
        }
        catch (final Exception e)
        {
            log.error("Exception while incrementing the Prometheus metrics for API Acceptance. It should not impact the application process.", e);
        }
    }

    public static void apiIncrementStatusCount(
            InterfaceType aInterfaceType,
            String aMessageSource,
            String aClusterName,
            String aClientIp,
            String aStatusCode)
    {
        apiIncrementStatusCount(aInterfaceType, aMessageSource, aClusterName, aClientIp, aStatusCode, "");
    }

    public static void apiIncrementStatusCount(
            InterfaceType aInterfaceType,
            String aMessageSource,
            String aClusterName,
            String aClientIp,
            String aStatusCode,
            String aUsername)
    {

        try
        {
            if ((PrometheusController.getInstance().getApiController().isApiUserStatusEnabled(aInterfaceType, aUsername))
                    || PrometheusController.getInstance().getApiController().isApiListStatusEnabled(aInterfaceType) //
                    || PrometheusController.getInstance().getApiController().isApiAllAcceptEnabled())
                PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, API_USER_STATUS_COUNT, aInterfaceType.getKey(), aMessageSource, aClusterName, CommonUtility.getApplicationServerIp(),
                        aClientIp, aStatusCode, aUsername);
        }
        catch (final Exception e)
        {
            log.error("Exception while incrementing the Prometheus metrics for API Status. It should not impact the application process.", e);
        }
    }

    public static Timer apiStartTimer(
            InterfaceType aInterfaceType,
            String aMessageSource,
            String aClusterName,
            String aClientIp)
    {
        return apiStartTimer(aInterfaceType, aMessageSource, aClusterName, aClientIp, "");
    }

    public static Timer apiStartTimer(
            InterfaceType aInterfaceType,
            String aMessageSource,
            String aClusterName,
            String aClientIp,
            String aUser)
    {
        Timer returnValue = null;

        try
        {
            if (PrometheusController.getInstance().getApiController().isApiUserLatencyEnabled(aInterfaceType, aUser)
                    || PrometheusController.getInstance().getApiController().isApiListLatencyEnabled(aInterfaceType) //
                    || PrometheusController.getInstance().getApiController().isApiAllLatencyEnabled())
                returnValue = PrometheusDataHolder.getInstance().startTimer(MetricType.HISTOGRAM, API_USER_PROCESS_LATENCY, aInterfaceType.getKey(), aMessageSource, aClusterName,
                        CommonUtility.getApplicationServerIp(), aClientIp, aUser);
        }
        catch (final Exception e)
        {
            log.error("Exception while Starting the Prometheus Timer for API Latencey. It should not impact the application process.", e);
        }
        return returnValue;
    }

    public static void apiEndTimer(
            InterfaceType aInterfaceType,
            Timer aTimer)
    {
        apiEndTimer(aInterfaceType, "", aTimer);
    }

    public static void apiEndTimer(
            InterfaceType aInterfaceType,
            String aUser,
            Timer aTimer)
    {

        try
        {
            if (PrometheusController.getInstance().getApiController().isApiUserLatencyEnabled(aInterfaceType, aUser)
                    || PrometheusController.getInstance().getApiController().isApiListLatencyEnabled(aInterfaceType) //
                    || PrometheusController.getInstance().getApiController().isApiAllLatencyEnabled())
                PrometheusDataHolder.getInstance().closeTimer(MetricType.HISTOGRAM, API_USER_PROCESS_LATENCY, aTimer);
        }
        catch (final Exception e)
        {
            log.error("Exception while Ending the Prometheus timer for API Latency. It should not impact the application process.", e);
        }
    }

    private static String getPlatformName(
            ClusterType aPlatformCluster)
    {
        return aPlatformCluster == null ? "DEFAULT" : aPlatformCluster.getKey();
    }

    public static void smppIncBindRequest(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.BIND, Action.REQUEST))
            PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, SMPP_BIND_REQUEST, getPlatformName(aSmppPrometheusInfo.getClusterType()), aSmppPrometheusInfo.getInstanceId(),
                    aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType());
    }

    public static void smppIncBindResponse(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.BIND, Action.RESPONSE))
            PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, SMPP_BIND_RESPONSE, getPlatformName(aSmppPrometheusInfo.getClusterType()), aSmppPrometheusInfo.getInstanceId(),
                    aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType());
    }

    public static void smppIncBindActiveCounts(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.BIND, Action.ACTIVE))
            PrometheusDataHolder.getInstance().increment(MetricType.GAUGE, SMPP_ACTIVE_BINDS, getPlatformName(aSmppPrometheusInfo.getClusterType()), aSmppPrometheusInfo.getInstanceId(),
                    aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType());
    }

    public static void smppDecBindActiveCounts(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.BIND, Action.ACTIVE))
            PrometheusDataHolder.getInstance().decrement(MetricType.GAUGE, SMPP_ACTIVE_BINDS, getPlatformName(aSmppPrometheusInfo.getClusterType()), aSmppPrometheusInfo.getInstanceId(),
                    aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType());
    }

    public static void smppIncBindErrorCounts(
            SmppPrometheusInfo aSmppPrometheusInfo,
            String aErrorReason)
    {
        if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.BIND, Action.ERROR))
            PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, SMPP_BIND_ERROR, getPlatformName(aSmppPrometheusInfo.getClusterType()), aSmppPrometheusInfo.getInstanceId(),
                    aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType(), CommonUtility.nullCheck(aErrorReason, true));
    }

    public static Timer smppBindStartTimer(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        Timer returnValue = null;

        try
        {
            if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.BIND, Action.LATENCY))
                returnValue = PrometheusDataHolder.getInstance().startTimer(MetricType.HISTOGRAM, SMPP_BIND_LATENCY, getPlatformName(aSmppPrometheusInfo.getClusterType()),
                        aSmppPrometheusInfo.getInstanceId(), aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType());
        }
        catch (final Exception e)
        {
            log.error("Exception while Starting the Prometheus Timer for Bind Latencey. It should not impact the application process.", e);
        }
        return returnValue;
    }

    public static void smppBindEndTimer(
            SmppPrometheusInfo aSmppPrometheusInfo,
            Timer aTimer)
    {

        try
        {
            if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.BIND, Action.LATENCY))
                PrometheusDataHolder.getInstance().closeTimer(MetricType.HISTOGRAM, SMPP_BIND_LATENCY, aTimer);
        }
        catch (final Exception e)
        {
            log.error("Exception while Ending the Prometheus timer for Bind Latency. It should not impact the application process.", e);
        }
    }

    public static void smppIncEnquiryLinkRequest(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.ENQUIRE, Action.REQUEST))
            PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, SMPP_ENQUIRE_LINK_REQUEST, getPlatformName(aSmppPrometheusInfo.getClusterType()), aSmppPrometheusInfo.getInstanceId(),
                    aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType());
    }

    public static void smppIncEnquiryLinkResponse(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.ENQUIRE, Action.RESPONSE))
            PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, SMPP_ENQUIRE_LINK_RESPONSE, getPlatformName(aSmppPrometheusInfo.getClusterType()), aSmppPrometheusInfo.getInstanceId(),
                    aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType());
    }

    public static Timer smppEnquiryLinkStartTimer(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        Timer returnValue = null;

        try
        {
            if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.ENQUIRE, Action.LATENCY))
                returnValue = PrometheusDataHolder.getInstance().startTimer(MetricType.HISTOGRAM, SMPP_ENQUIRE_LINK_LATENCY, getPlatformName(aSmppPrometheusInfo.getClusterType()),
                        aSmppPrometheusInfo.getInstanceId(), aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType());
        }
        catch (final Exception e)
        {
            log.error("Exception while Starting the Prometheus Timer for Enquirey Link Latencey. It should not impact the application process.", e);
        }
        return returnValue;
    }

    public static void smppEnquiryLinkEndTimer(
            SmppPrometheusInfo aSmppPrometheusInfo,
            Timer aTimer)
    {

        try
        {
            if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.ENQUIRE, Action.LATENCY))
                PrometheusDataHolder.getInstance().closeTimer(MetricType.HISTOGRAM, SMPP_ENQUIRE_LINK_LATENCY, aTimer);
        }
        catch (final Exception e)
        {
            log.error("Exception while Ending the Prometheus timer for Enquirey Link Latency. It should not impact the application process.", e);
        }
    }

    public static void smppIncSubmitSmRequest(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.SUBMIT_SM, Action.REQUEST))
            PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, SMPP_SUBMITSM_REQUEST, getPlatformName(aSmppPrometheusInfo.getClusterType()), aSmppPrometheusInfo.getInstanceId(),
                    aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType());
    }

    public static void smppIncSubmitSmResponse(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.SUBMIT_SM, Action.RESPONSE))
            PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, SMPP_SUBMITSM_RESPONSE, getPlatformName(aSmppPrometheusInfo.getClusterType()), aSmppPrometheusInfo.getInstanceId(),
                    aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType());
    }

    public static Timer smppSubmitSmStartTimer(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        Timer returnValue = null;

        try
        {
            if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.SUBMIT_SM, Action.LATENCY))
                returnValue = PrometheusDataHolder.getInstance().startTimer(MetricType.HISTOGRAM, SMPP_SUBMITSM_LATENCY, getPlatformName(aSmppPrometheusInfo.getClusterType()),
                        aSmppPrometheusInfo.getInstanceId(), aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType());
        }
        catch (final Exception e)
        {
            log.error("Exception while Starting the Prometheus Timer for SubmitSm Latencey. It should not impact the application process.", e);
        }
        return returnValue;
    }

    public static void smppSubmitSmEndTimer(
            SmppPrometheusInfo aSmppPrometheusInfo,
            Timer aTimer)
    {

        try
        {
            if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.SUBMIT_SM, Action.LATENCY))
                PrometheusDataHolder.getInstance().closeTimer(MetricType.HISTOGRAM, SMPP_SUBMITSM_LATENCY, aTimer);
        }
        catch (final Exception e)
        {
            log.error("Exception while Ending the Prometheus timer for SubmitSm Latency. It should not impact the application process.", e);
        }
    }

    public static void smppIncDeliverSmRequest(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.DELIVERY_SM, Action.REQUEST))
            PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, SMPP_DELIVERSM_REQUEST, getPlatformName(aSmppPrometheusInfo.getClusterType()), aSmppPrometheusInfo.getInstanceId(),
                    aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getBindType());
    }

    public static void smppIncDeliverSmResponse(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.DELIVERY_SM, Action.RESPONSE))
            PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, SMPP_DELIVERSM_RESPONSE, getPlatformName(aSmppPrometheusInfo.getClusterType()), aSmppPrometheusInfo.getInstanceId(),
                    aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getBindType());
    }

    public static void smppIncDeliverSmFailure(
            SmppPrometheusInfo aSmppPrometheusInfo,
            String aReason)
    {
        if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.DELIVERY_SM, Action.ERROR))
            PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, SMPP_DELIVERSM_FAILURE, getPlatformName(aSmppPrometheusInfo.getClusterType()), aSmppPrometheusInfo.getInstanceId(),
                    aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getBindType(), CommonUtility.nullCheck(aReason, true));
    }

    public static Timer smppDeliverSmStartTimer(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        Timer returnValue = null;

        try
        {
            if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.DELIVERY_SM, Action.LATENCY))
                returnValue = PrometheusDataHolder.getInstance().startTimer(MetricType.HISTOGRAM, SMPP_DELIVERSM_LATENCY, getPlatformName(aSmppPrometheusInfo.getClusterType()),
                        aSmppPrometheusInfo.getInstanceId(), aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getBindType());
        }
        catch (final Exception e)
        {
            log.error("Exception while Starting the Prometheus Timer for DeliverySm Latencey. It should not impact the application process.", e);
        }
        return returnValue;
    }

    public static void smppDeliverSmEndTimer(
            SmppPrometheusInfo aSmppPrometheusInfo,
            Timer aTimer)
    {

        try
        {
            if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.DELIVERY_SM, Action.LATENCY))
                PrometheusDataHolder.getInstance().closeTimer(MetricType.HISTOGRAM, SMPP_DELIVERSM_LATENCY, aTimer);
        }
        catch (final Exception e)
        {
            log.error("Exception while Ending the Prometheus timer for DeliverySm Latency. It should not impact the application process.", e);
        }
    }

    public static void smppIncUnbindRequest(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.UNBIND, Action.REQUEST))
            PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, SMPP_UNBIND_REQUEST, getPlatformName(aSmppPrometheusInfo.getClusterType()), aSmppPrometheusInfo.getInstanceId(),
                    aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType());
    }

    public static void smppIncUnbindResponse(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.UNBIND, Action.RESPONSE))
            PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, SMPP_UNBIND_RESPONSE, getPlatformName(aSmppPrometheusInfo.getClusterType()), aSmppPrometheusInfo.getInstanceId(),
                    aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType());
    }

    public static void smppIncUnbindCounts(
            SmppPrometheusInfo aSmppPrometheusInfo,
            String aReason)
    {
        if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.UNBIND, Action.ERROR))
            PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, SMPP_UNBIND_COUNTS, getPlatformName(aSmppPrometheusInfo.getClusterType()), aSmppPrometheusInfo.getInstanceId(),
                    aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType(), CommonUtility.nullCheck(aReason, true));
    }

    public static Timer smppUnbindStartTimer(
            SmppPrometheusInfo aSmppPrometheusInfo)
    {
        Timer returnValue = null;

        try
        {
            if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.UNBIND, Action.LATENCY))
                returnValue = PrometheusDataHolder.getInstance().startTimer(MetricType.HISTOGRAM, SMPP_UNBIND_LATENCY, getPlatformName(aSmppPrometheusInfo.getClusterType()),
                        aSmppPrometheusInfo.getInstanceId(), aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType());
        }
        catch (final Exception e)
        {
            log.error("Exception while Starting the Prometheus Timer for Unbind Latencey. It should not impact the application process.", e);
        }
        return returnValue;
    }

    public static void smppUnbindEndTimer(
            SmppPrometheusInfo aSmppPrometheusInfo,
            Timer aTimer)
    {

        try
        {
            if (isSmppStatsEnabled() && isServiceEnabled(aSmppPrometheusInfo, RequestType.UNBIND, Action.LATENCY))
                PrometheusDataHolder.getInstance().closeTimer(MetricType.HISTOGRAM, SMPP_UNBIND_LATENCY, aTimer);
        }
        catch (final Exception e)
        {
            log.error("Exception while Ending the Prometheus timer for Unbind Latency. It should not impact the application process.", e);
        }
    }

    public static void smppIncFailureCounts(
            SmppPrometheusInfo aSmppPrometheusInfo,
            String aErrorCode,
            String aReason)
    {

        try
        {
            if (isSmppStatsEnabled() && PrometheusController.getInstance().getSmppController().canAddPrometheusCounterForError(aSmppPrometheusInfo.getSystemId()))
                PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, SMPP_FAILURE_COUNTS, getPlatformName(aSmppPrometheusInfo.getClusterType()), aSmppPrometheusInfo.getInstanceId(),
                        aSmppPrometheusInfo.getSystemId(), aSmppPrometheusInfo.getClientIp(), aSmppPrometheusInfo.getBindType(), CommonUtility.nullCheck(aErrorCode, true),
                        CommonUtility.nullCheck(aReason, true));
        }
        catch (final Exception e)
        {
            log.error("Exception while incrementing the SMPP Failure Counts. It should not impact the application process.", e);
        }
    }

    private static boolean isSmppStatsEnabled()
    {
        return PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.SMPP);
    }

    private static boolean isServiceEnabled(
            SmppPrometheusInfo aSmppPrometheusInfo,
            RequestType aRequestType,
            Action aAction)
    {
        return PrometheusController.getInstance().getSmppController().canAddPrometheusCounter(aSmppPrometheusInfo.getSystemId(), aRequestType, aAction);
    }

    public static void incrementPlatformRejection(
            ClusterType aClustertype,
            Component aComponent,
            String aServerIp,
            String aPlatformStatusCode,
            String aPlatformStatusDesc)
    {

        try
        {
            if (PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.PLATFORM_REJECTIONS))
                PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, PLATFORM_REJECTION_COUNTS, getComponentName(aComponent), getPlatformName(aClustertype), aServerIp,
                        aPlatformStatusCode != null ? aPlatformStatusCode : "<UNKNOWN>", CommonUtility.nullCheck(aPlatformStatusDesc, true));
        }
        catch (final Exception e)
        {
            log.error("Exception while incrementing the Platform Rejection Counts. It should not impact the application process.", e);
        }
    }

    public static void incrementGenericError(
            ClusterType aClustertype,
            Component aComponent,
            String aServerIp,
            String aGenericErrorCode,
            String aGenericErrorDesc)
    {

        try
        {
            if (PrometheusController.getInstance().isGenericErrorEnabled(aComponent))
                PrometheusDataHolder.getInstance().increment(MetricType.COUNTER, GENERIC_ERROR_COUNTS, getComponentName(aComponent), getPlatformName(aClustertype), aServerIp,
                        aGenericErrorCode != null ? CommonUtility.nullCheck(aGenericErrorCode, true) : "<UNKNOWN>", CommonUtility.nullCheck(aGenericErrorDesc, true));
        }
        catch (final Exception e)
        {
            log.error("Exception while incrementing the Platform Rejection Counts. It should not impact the application process.", e);
        }
    }

    private static String getComponentName(
            Component aComponent)
    {
        return (aComponent != null) ? aComponent.getKey() : "<NULL>";
    }

}