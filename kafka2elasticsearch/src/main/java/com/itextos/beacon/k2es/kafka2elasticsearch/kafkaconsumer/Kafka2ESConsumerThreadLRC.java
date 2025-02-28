package com.itextos.beacon.k2es.kafka2elasticsearch.kafkaconsumer;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.k2es.kafka2elasticsearch.start.StartApplication;

public class Kafka2ESConsumerThreadLRC
        extends
        Kafka2ESConsumerThread
        implements
        ConsumerRebalanceListener
{

    private static final Log                    log           = LogFactory.getLog(StartApplication.class);
    private final AtomicBoolean                 stopped       = new AtomicBoolean(false);

    private final SimpleDateFormat              sdfHour       = new SimpleDateFormat("HH");

    private KafkaConsumer<String, IMessage>     TopicConsumer = null;
    private final String                        KafkaTopicName;
    private final String                        KafkaConsumerGroupId;

    public String                               ConsumerThreadName;
    private final String                        ConsumerMode;
    private final String                        ConsumerClientID;
    private final AppConfiguration              AppConfig;
    private final ArrayList<ESIndexColMapValue> ListESColMap;
    private final String                        ESIndexName;
    private final String                        ESIndexUniqueColumn;
    private final String                        ESDocUpdTmColumn;
    private String                              ESRetryConflictCount;

    private long                                IdleTimeMS    = 0L;
    private int                                 IdleFlushTime = 0;
    private int                                 FlushLimit    = 0;

    private RestClient                          ESClient      = null;
    private RestClient                          ESErrClient   = null;

    private int                                 BatchCount    = 0;
    private int                                 ProcCount     = 0;
    private int                                 LogProcLimit  = 0;
    private int                                 LogProcCount  = 0;

    private ArrayList<JSONObject>               alESBatch     = new ArrayList<>();

    public Kafka2ESConsumerThreadLRC(
            String pThreadName,String appmode,String groupname,String topicname,RestClient ES_LRC_Client,RestClient ESErr_LRC_Client )
    {
        super(pThreadName,appmode,groupname,topicname);
        this.setName(pThreadName);
        ConsumerThreadName        = pThreadName;
        this.ConsumerMode         = appmode;
        this.AppConfig            = StartApplication.AppConfig;
        this.ListESColMap         = StartApplication.ListESColMap;
        this.ESIndexName          = StartApplication.ESIndexName;
        this.ESIndexUniqueColumn  = StartApplication.ESIndexUniqueColumn;
        this.KafkaTopicName       = topicname;
        this.KafkaConsumerGroupId = groupname;
        this.ESClient             = ES_LRC_Client;
        this.ESErrClient          = ESErr_LRC_Client;

        if (this.ConsumerMode.equals(Kafka2ESConstants.subMode))
            this.ESDocUpdTmColumn = Kafka2ESConstants.subUpdTmColumn;
        else
            if (this.ConsumerMode.equals(Kafka2ESConstants.delMode))
                this.ESDocUpdTmColumn = Kafka2ESConstants.delUpdTmColumn;
            else
                this.ESDocUpdTmColumn = null;

        ConsumerClientID = StartApplication.HostIPAddr + ":"
                + StartApplication.AppProcID + ":"
                + pThreadName;
    }

    /*
    RestClient esConnect()
    {
        final String         ESHosts         = this.AppConfig.getString("es.servers");
        final String[]       ESHSplit        = ESHosts.split("[,]");
        final int            ESConnecTimeOut = this.AppConfig.getInt("es.connection.timeout");
        final int            ESSocketTimeout = this.AppConfig.getInt("es.socket.timeout");

        final List<HttpHost> httpHosts       = Arrays.stream(ESHSplit)
                .map(s -> s.split("[:]"))
                .map(strings -> new HttpHost(strings[0], Integer.valueOf(strings[1])))
                .collect(Collectors.toList());

        log.info("Connecting to ElasticSearch Nodes: " + httpHosts.toString());

        final RestClientBuilder builder    = RestClient.builder(
                httpHosts.toArray(new HttpHost[httpHosts.size()]))
                .setRequestConfigCallback(
                        requestConfigBuilder -> requestConfigBuilder
                                .setConnectTimeout(ESConnecTimeOut)
                                .setSocketTimeout(ESSocketTimeout));
        final RestClient        restClient = builder.build();

        return restClient;
    }

*/
    KafkaConsumer<String, IMessage> kafkaConnect()
    {
        final Properties ConsumerProps = new Properties();
        final String     KafkaServers  = this.AppConfig.getString("kafka.bootstrap.servers");
        ConsumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaServers);
        ConsumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, this.KafkaConsumerGroupId);
        ConsumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, this.AppConfig.getString("kafka.key.deserializer"));
        ConsumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, this.AppConfig.getString("kafka.value.deserializer"));
        ConsumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, this.AppConfig.getString("kafka.enable.auto.commit"));
        ConsumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, this.AppConfig.getString("kafka.auto.offset.reset"));
        ConsumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, this.AppConfig.getString("kafka.max.poll.records"));
        ConsumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, this.AppConfig.getString("kafka.session.timeout.ms"));
        ConsumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, this.ConsumerClientID);

        log.info("Kafka Bootstap Servers: " + KafkaServers);
        final KafkaConsumer<String, IMessage> consumer = new KafkaConsumer<>(ConsumerProps);
        return consumer;
    }

    // private JSONObject buildSubJSON(
    // IMessage iMsg)
    // throws Exception
    // {
    // boolean isEmptyJSON = true;
    // final SubmissionObject subObject = (SubmissionObject) iMsg;
    // final BaseMessage baseMessage = subObject;
    //
    // final JSONObject subJSON = new JSONObject();
    //
    // final String msgId = subObject.getMessageId();
    // subJSON.put(this.ESIndexUniqueColumn, msgId);
    //
    // final String dataUpdTime =
    // DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT);
    // subJSON.put(this.ESDocUpdTmColumn, dataUpdTime);
    //
    // for (final ESIndexColMapValue esColMap : this.ListESColMap)
    // {
    // final String colName = esColMap.ColumnName;
    // // final String colType = esColMap.ColumnType;
    // final String mapColName = esColMap.MappedName;
    // final boolean ciRequired = esColMap.CIColumnRequired;
    // // final String defaultVal = esColMap.DefaultValue;
    //
    // String colValue = Kafka2ESConstants.colInitNullValue;
    //
    // switch (colName)
    // {
    // case "campaign_id":
    // colValue = subObject.getCampaignId();
    // break;
    //
    // case "campaign_name":
    // colValue = subObject.getCampaignName();
    // break;
    //
    // case "carrier":
    // colValue = subObject.getCarrier();
    // break;
    //
    // case "circle":
    // colValue = subObject.getCircle();
    // break;
    //
    // case "cli_id":
    // colValue = subObject.getClientId();
    // break;
    //
    // case "country":
    // colValue = subObject.getCountry();
    // break;
    //
    // case "dest":
    // colValue = subObject.getMobileNumber();
    // break;
    //
    // case "file_id":
    // colValue = subObject.getFileId();
    // break;
    //
    // case "hdr":
    // colValue = subObject.getHeader();
    // break;
    //
    // case "intf_grp_type":
    // colValue = subObject.getInterfaceGroupType().getKey();
    // break;
    //
    // case "intf_type":
    // colValue = subObject.getInterfaceType().getKey();
    // break;
    //
    // case "msg":
    // if (subObject.isHexMessage())
    // colValue = new String(HexUtil.toByteArray(subObject.getMessage()));
    // else
    // colValue = subObject.getMessage();
    // break;
    //
    // case "msg_tag":
    // colValue = subObject.getMessageTag();
    // break;
    //
    // case "msg_tag1":
    // colValue = subObject.getMsgTag1();
    // break;
    //
    // case "msg_tag2":
    // colValue = subObject.getMsgTag2();
    // break;
    //
    // case "msg_tag3":
    // colValue = subObject.getMsgTag3();
    // break;
    //
    // case "msg_tag4":
    // colValue = subObject.getMsgTag4();
    // break;
    //
    // case "msg_tag5":
    // colValue = subObject.getMsgTag5();
    // break;
    //
    // case "cluster_type":
    // colValue = subObject.getClusterType().getKey();
    // break;
    //
    // case "recv_date":
    // colValue =
    // DateTimeUtility.getFormattedDateTime(subObject.getMessageReceivedDate(),
    // DateTimeFormat.DEFAULT_DATE_ONLY);
    // break;
    //
    // case "recv_time":
    // final Date rcvTime = subObject.getMessageReceivedTime();
    // colValue = DateTimeUtility.getFormattedDateTime(rcvTime,
    // DateTimeFormat.DEFAULT);
    // final int rcvHour = Integer.parseInt(sdfHour.format(rcvTime));
    // subJSON.put("recv_hour", rcvHour);
    // break;
    //
    // case "retry_attempt":
    // colValue = "" + subObject.getRetryAttempt();
    // break;
    //
    // case "route_id":
    // colValue = subObject.getRouteId();
    // break;
    //
    // case "sub_cli_sts_code":
    // colValue = baseMessage.getValue(MiddlewareConstant.MW_SUB_CLI_STATUS_CODE);
    // break;
    //
    // case "sub_cli_sts_desc":
    // colValue = baseMessage.getValue(MiddlewareConstant.MW_SUB_CLI_STATUS_DESC);
    // break;
    //
    // case "sub_ori_sts_code":
    // colValue = baseMessage.getValue(MiddlewareConstant.MW_SUB_ORI_STATUS_CODE);
    // break;
    //
    // case "sub_ori_sts_desc":
    // colValue = baseMessage.getValue(MiddlewareConstant.MW_SUB_ORI_STATUS_DESC);
    // break;
    //
    // case "sub_status":
    // colValue = subObject.getSubStatus();
    // break;
    // }
    //
    // if (Kafka2ESConstants.colInitNullValue.equals(colValue))
    // continue;
    //
    // colValue = CommonUtility.nullCheck(colValue, true);
    // if ("".equals(colValue))
    // continue;
    //
    // isEmptyJSON = false;
    //
    // subJSON.put(mapColName, colValue);
    //
    // if (ciRequired)
    // {
    // final String ciColName = mapColName + "_ci";
    // final String ciColValue = colValue.toLowerCase();
    // subJSON.put(ciColName, ciColValue);
    // }
    // }
    //
    // if (isEmptyJSON)
    // return null;
    //
    // return subJSON;
    // }
    //
    // private JSONObject buildDelJSON(
    // IMessage iMsg)
    // throws Exception
    // {
    // boolean isEmptyJSON = true;
    // final DeliveryObject delObject = (DeliveryObject) iMsg;
    // final BaseMessage baseMessage = delObject;
    //
    // final JSONObject delJSON = new JSONObject();
    //
    // final String msgId = delObject.getMessageId();
    // delJSON.put(this.ESIndexUniqueColumn, msgId);
    //
    // final String dataUpdTime =
    // DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT);
    // delJSON.put(this.ESDocUpdTmColumn, dataUpdTime);
    //
    // for (final ESIndexColMapValue esColMap : this.ListESColMap)
    // {
    // final String colName = esColMap.ColumnName;
    // // final String colType = esColMap.ColumnType;
    // final String mapColName = esColMap.MappedName;
    // // final String defaultVal = esColMap.DefaultValue;
    // final boolean ciRequired = esColMap.CIColumnRequired;
    //
    // String colValue = Kafka2ESConstants.colInitNullValue;
    //
    // switch (colName)
    // {
    // case "carrier_ack_id":
    // colValue = delObject.getCarrierAcknowledgeId();
    // break;
    //
    // case "delivery_status":
    // colValue = delObject.getDeliveryStatus();
    // break;
    //
    // case "dly_time":
    // final Date dlyTime = delObject.getDeliveryTime();
    // if (dlyTime == null)
    // colValue = "1900-01-01 00:00:00";
    // else
    // colValue = DateTimeUtility.getFormattedDateTime(delObject.getDeliveryTime(),
    // DateTimeFormat.DEFAULT);
    // break;
    //
    // case "dn_cli_sts_code":
    // colValue = baseMessage.getValue(MiddlewareConstant.MW_DN_CLI_STATUS_CODE);
    // break;
    //
    // case "dn_cli_sts_desc":
    // colValue = baseMessage.getValue(MiddlewareConstant.MW_DN_CLI_STATUS_DESC);
    // break;
    //
    // case "dn_ori_sts_code":
    // colValue = baseMessage.getValue(MiddlewareConstant.MW_DN_ORI_STATUS_CODE);
    // break;
    //
    // case "dn_ori_sts_desc":
    // colValue = baseMessage.getValue(MiddlewareConstant.MW_DN_ORI_STATUS_DESC);
    // break;
    // }
    //
    // if (Kafka2ESConstants.colInitNullValue.equals(colValue))
    // continue;
    //
    // isEmptyJSON = false;
    // delJSON.put(mapColName, colValue);
    //
    // if (ciRequired)
    // {
    // final String ciColName = mapColName + "_ci";
    // final String ciColValue = colValue.toLowerCase();
    // delJSON.put(ciColName, ciColValue);
    // }
    // }
    //
    // if (isEmptyJSON)
    // return null;
    //
    // return delJSON;
    // }

    private void processData(
            ConsumerRecords<String, IMessage> pDataList)
            throws Exception
    {

        for (final ConsumerRecord<String, IMessage> data : pDataList)
        {
            final IMessage iMsg     = data.value();
            JSONObject     dataJSON = null;
            if (this.ConsumerMode.equals(Kafka2ESConstants.subMode))
                dataJSON = Kafka2ESJSONConverter.buildSubJSON(iMsg);
            else
                if (this.ConsumerMode.equals(Kafka2ESConstants.delMode))
                    dataJSON = Kafka2ESJSONConverter.buildDelJSON(iMsg);

            if (dataJSON == null)
            {
                log.error("Unable to build JSON Object from Message Object");

                if (log.isDebugEnabled())
                    log.debug(iMsg.toString());

                continue;
            }

            final JSONObject ESDoc = new JSONObject();
            ESDoc.put("doc", dataJSON);
            ESDoc.put("doc_as_upsert", true);

            alESBatch.add(ESDoc);

            BatchCount++;

            if (BatchCount == FlushLimit)
            {
                if (log.isDebugEnabled())
                    log.debug("Executing ES Batch Update Async");
                executeESBachUpdateAsync();
            }
        }
    }

    @SuppressWarnings("unused")
    private void executeESBachUpdateAsync()
            throws Exception
    {

        if (BatchCount == 0)
        {
            if (log.isDebugEnabled())
                log.debug("BulkRequest is empty");
            return;
        }

        if (log.isDebugEnabled())
            log.debug("Executing Aync Batch...");

        synchronized (ESClient)
        {

            for (final JSONObject ESDoc : alESBatch)
            {
                final ESUpdateResponseListener uRL    = new ESUpdateResponseListener(ESDoc,
                        ESErrClient);
                final JSONObject               esData = (JSONObject) ESDoc.get("doc");
                final String                   msgId  = (String) esData.get(this.ESIndexUniqueColumn);

                final Request                  ESReq  = new Request("POST", "/" + this.ESIndexName +
                        "/_update/" + msgId + "?retry_on_conflict=" + ESRetryConflictCount);
                ESReq.setJsonEntity(ESDoc.toJSONString());

                ESClient.performRequestAsync(ESReq, uRL);
            }
        }

        if (log.isDebugEnabled())
            log.debug("Batch Count: " + BatchCount + ", Executing Consumer commit ...");

        TopicConsumer.commitAsync();
        CommonUtility.sleepForAWhile();

        ProcCount += BatchCount;

        if (log.isDebugEnabled())
            log.debug(ProcCount + " records procesed");
        else
        {
            LogProcCount += BatchCount;

            if (LogProcCount >= LogProcLimit)
            {
                log.info(ProcCount + " records procesed");
                LogProcCount = 0;
            }
        }

        BatchCount = 0;
        alESBatch  = new ArrayList<>();
    }

    @Override
    public void run()
    {

        try
        {
            log.info("Consumer Thread started");

            log.info("Creating Kafka Consumer...");
            TopicConsumer = kafkaConnect();
            log.info("subscribing topic: " + this.KafkaTopicName);
            TopicConsumer.subscribe(Collections.singleton(this.KafkaTopicName), this);

            ESRetryConflictCount = this.AppConfig.getString("es.update.retry.count");
            FlushLimit           = this.AppConfig.getInt("es.index.flush.limit");
            IdleFlushTime        = this.AppConfig.getInt("consumer.idle.flushtime.ms");
            LogProcLimit         = this.AppConfig.getInt("consumer.log.proc.limit");

            final int PollMS    = this.AppConfig.getInt("consumer.poll.ms");
            final int PollDelay = this.AppConfig.getInt("consumer.poll.delay.ms");

            log.info("polling topic");

            // bulkRequest = new BulkRequest();
            IdleTimeMS = DateTimeUtility.getCurrentTimeInMillis();

            while (!stopped.get())
            {

                if ((BatchCount > 0) &&
                        ((DateTimeUtility.getCurrentTimeInMillis() - IdleTimeMS) >= IdleFlushTime))
                {
                    if (log.isDebugEnabled())
                        log.debug("Idle Time threshold has exceeded, Flushing data, Batch Count: " + BatchCount);

                    executeESBachUpdateAsync();
                    IdleTimeMS = DateTimeUtility.getCurrentTimeInMillis();
                }

                final ConsumerRecords<String, IMessage> pollRecords = TopicConsumer.poll(
                        Duration.of(PollMS, ChronoUnit.MILLIS));

                final int                               pollCount   = pollRecords.count();

                if (log.isDebugEnabled())
                    log.debug("Poll Count: " + pollCount);

                if (pollCount == 0)
                {
                    CommonUtility.sleepForAWhile(PollDelay);
                    continue;
                }

                processData(pollRecords);
                IdleTimeMS = DateTimeUtility.getCurrentTimeInMillis();
            }
        }
        catch (final WakeupException we)
        {

            // Ignore exception if closing
            if (!this.stopped.get())
            {
                if (log.isDebugEnabled())
                    log.debug("Stop Flag is already set in the WakeupException");
                throw we;
            }
        }
        catch (final Exception ex)
        {
            log.error(ex.getMessage(), ex);
        }
        finally
        {

            try
            {
                if (!this.stopped.get())
                    this.stopped.set(true);

                if (BatchCount > 0)
                    executeESBachUpdateAsync();

                if (ESClient != null)
                    ESClient.close();

                if (ESErrClient != null)
                    ESClient.close();

                if (TopicConsumer != null)
                    TopicConsumer.close();

                log.info("Total records processed: " + ProcCount);
                log.info("Consumer Thread stopped");
                StartApplication.logMsg("Total records processed: " + ProcCount);
                StartApplication.logMsg("Consumer Thread stopped");
            }
            catch (final Exception ex2)
            {
                log.error(ex2.getMessage(), ex2);
                ex2.printStackTrace(System.err);
            }
        }
    }

    @Override
    public String getConsumerThreadName()
    {
        return this.ConsumerThreadName;
    }

    @Override
    public boolean isConsumerStopped()
    {
        return stopped.get();
    }

    @Override
    public void stopConsumer()
    {
        if (stopped.get())
            return;
        stopped.set(true);
        StartApplication.logMsg("Stop Flag has been set");
        CommonUtility.sleepForAWhile(500);
        if (TopicConsumer != null)
            TopicConsumer.wakeup();
    }

    @Override
    public void onPartitionsRevoked(
            Collection<TopicPartition> partitions)
    {
        log.info("Partitions revoked: " + partitions);
    }

    @Override
    public void onPartitionsAssigned(
            Collection<TopicPartition> partitions)
    {
        log.info("Partitions assigned: " + partitions);
    }

}
