package com.itextos.beacon.platform.topic2table.es;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.IMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.errorlog.K2ESDataLog;
import com.itextos.beacon.errorlog.K2ESLog;

public class Kafka2ESDeliveries
      
{

    private static final K2ESLog                              log                     = K2ESLog.getInstance();
    private static final K2ESDataLog                              logdata                     = K2ESDataLog.getInstance();

    private final AtomicBoolean             stopped         = new AtomicBoolean(false);


    public String                           ConsumerThreadName;
    private final String                    ConsumerMode;
    private final AppConfiguration          AppConfig;
    private final String                    ESIndexName;
    private final String                    ESIndexUniqueColumn;
    public final String                     ESFmsgIndexName;
    public final String                     ESFmsgIndexUniqueColumn;

    private final int                       ESRetryConflictCount;

    private RestHighLevelClient             ESClient        = null;
    private BulkRequest                     bulkRequest     = null;
    private BulkRequest                     fmsgBulkRequest = null;

    private final int                       IdleFlushTime;
    private final int                       FlushLimit;
    private final int                       LogProcLimit;

    private long                            IdleTimeMS      = 0L;
    private int                             BatchCount      = 0;
    private int                             ProcCount       = 0;
    private int                             LogProcCount    = 0;

    public Kafka2ESDeliveries(
            String pThreadName,String tpoicgroupname,String topicname)
    {
        this.ConsumerMode            = DeliveriesK2ES.AppMode;
        this.AppConfig               = DeliveriesK2ES.AppConfig;
        this.ESIndexName             = DeliveriesK2ES.ESIndexName;
        this.ESIndexUniqueColumn     = DeliveriesK2ES.ESIndexUniqueColumn;
        this.ESFmsgIndexName         = DeliveriesK2ES.ESFmsgIndexName;
        this.ESFmsgIndexUniqueColumn = DeliveriesK2ES.ESFmsgIndexUniqueColumn;
        this.ESRetryConflictCount    = this.AppConfig.getInt("es.update.retry.count");
        this.FlushLimit              = this.AppConfig.getInt("es.index.flush.limit");
        this.IdleFlushTime           = this.AppConfig.getInt("consumer.idle.flushtime.ms");
        this.LogProcLimit            = this.AppConfig.getInt("consumer.log.proc.limit");

    }

    RestHighLevelClient esConnect()
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

        final RestClientBuilder   builder    = RestClient.builder(
                httpHosts.toArray(new HttpHost[httpHosts.size()]))
                .setRequestConfigCallback(
                        requestConfigBuilder -> requestConfigBuilder
                                .setConnectTimeout(ESConnecTimeOut)
                                .setSocketTimeout(ESSocketTimeout));
        final RestHighLevelClient restClient = new RestHighLevelClient(builder);

        return restClient;
    }

    


    public void pushtoElasticSearch( List<BaseMessage> mMessagesToInsert) {
    	
        for (final BaseMessage data : mMessagesToInsert)
        {
            final IMessage iMsg     = (IMessage)data;
            JSONObject     dataJSON = null;

            try
            {
                if (this.ConsumerMode.equals(Kafka2ESConstants.subMode))
                    dataJSON = Kafka2ESJSONUtilDeliveries.buildSubJSON(iMsg);
                else
                    if (this.ConsumerMode.equals(Kafka2ESConstants.delMode))
                        dataJSON = Kafka2ESJSONUtilDeliveries.buildDelJSON(iMsg);
            }
            catch (final Exception ex)
            {
                log.error("Error while processing Message Object", ex);
                ex.printStackTrace(System.err);
                if (log.isDebugEnabled())
                    log.debug(iMsg.toString());
            }

            if (dataJSON == null)
            {
                log.error("Unable to build JSON Object from Message Object");

                if (log.isDebugEnabled())
                    log.debug(iMsg.toString());

                continue;
            }

            final String        msgId         = CommonUtility.nullCheck(dataJSON.get(this.ESIndexUniqueColumn), true);
            final UpdateRequest updateRequest = new UpdateRequest(this.ESIndexName, msgId)
                    .doc(dataJSON.toJSONString(), XContentType.JSON)
                    .docAsUpsert(true);
            updateRequest.retryOnConflict(ESRetryConflictCount);
            bulkRequest.add(updateRequest);

            final String baseMsgId = CommonUtility.nullCheck(dataJSON.get(this.ESFmsgIndexUniqueColumn), true);

            if (!"".equals(baseMsgId))
            {
                JSONObject fmsgJSON = null;
                if (this.ConsumerMode.equals(Kafka2ESConstants.subMode))
                    fmsgJSON = Kafka2ESJSONUtilDeliveries.buildSubFMSGJSON(dataJSON, baseMsgId);
                else
                    if (this.ConsumerMode.equals(Kafka2ESConstants.delMode))
                        fmsgJSON = Kafka2ESJSONUtilDeliveries.buildDelFMSGJSON(dataJSON, baseMsgId);

                if (fmsgJSON != null)
                {
                    final UpdateRequest fmsgupdateRequest = new UpdateRequest(this.ESFmsgIndexName, baseMsgId)
                            .doc(fmsgJSON.toJSONString(), XContentType.JSON)
                            .docAsUpsert(true);
                    fmsgupdateRequest.retryOnConflict(ESRetryConflictCount);
                    fmsgBulkRequest.add(fmsgupdateRequest);
                }
            }

            BatchCount++;

            if (BatchCount == FlushLimit)
            {
                if (log.isDebugEnabled())
                    log.debug("Batch Count: " + BatchCount + ", Flusing Data...");

                try {
					writeData();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
    
    }

    private void writeData()
            throws Exception
    {

        if (BatchCount == 0)
        {
            if (log.isDebugEnabled())
                log.debug("BulkRequest is empty");
            return;
        }

        if (log.isDebugEnabled())
            log.debug("Executing ES BulkAsync ...");

        // ESClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        final ESBulkAsyncListener bal = new ESBulkAsyncListener(bulkRequest, false);
        ESClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, bal);
        bulkRequest = new BulkRequest();

        if (fmsgBulkRequest.requests().size() > 0)
        {
            if (log.isDebugEnabled())
                log.debug("Executing ES BulkAsync for Full Message Info...");
            // ESClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            final ESBulkAsyncListener balFmsg = new ESBulkAsyncListener(fmsgBulkRequest, true);
            ESClient.bulkAsync(fmsgBulkRequest, RequestOptions.DEFAULT, balFmsg);
            fmsgBulkRequest = new BulkRequest();
        }

        if (log.isDebugEnabled())
            log.debug("Executing Kafka Consumer CommitAsync ...");

   

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
    }

 

    public String getConsumerThreadName()
    {
        return this.ConsumerThreadName;
    }

   
  
 

    

}
