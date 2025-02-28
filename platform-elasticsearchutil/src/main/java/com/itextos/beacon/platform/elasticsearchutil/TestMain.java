package com.itextos.beacon.platform.elasticsearchutil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.MessagePart;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.elasticsearchutil.types.EsConstant;
import com.itextos.beacon.platform.elasticsearchutil.types.EsCreateTimeStamp;
import com.itextos.beacon.platform.elasticsearchutil.types.EsSortOrder;
import com.itextos.beacon.platform.elasticsearchutil.types.EsTypeParent;
import com.itextos.beacon.platform.elasticsearchutil.utility.EsUtility;

public class TestMain
{

    public static void main(
            String[] args)
    {

        try
        {
            // insertData();
            // insertDataSync();
            // printDateFormat();
            // Thread.sleep(20 * 1000);
            // readData();
            // searchData();
            // singleDnInsert();
            // singleDnQuery();
            // singelDnDelete();

            insertDLRQuery();
        }
        catch (final Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // catch (final InterruptedException e)
        // {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        System.out.println("Completed.");
    }

    private static void insertDLRQuery()
            throws Exception
    {
        final int    testIterations = 12;

      
        
        Thread virtualThread1 = Thread.ofVirtual().start(new DlrQueryInsert(testIterations));

        virtualThread1.setName( "T1");
        Thread virtualThread2 = Thread.ofVirtual().start(new DlrQueryInsert(testIterations));

        virtualThread2.setName( "T2");
        Thread virtualThread3 = Thread.ofVirtual().start(new DlrQueryInsert(testIterations));

        virtualThread3.setName( "T3");
        Thread virtualThread4 = Thread.ofVirtual().start(new DlrQueryInsert(testIterations));

        virtualThread4.setName( "T4");
    }

    private static void singelDnDelete()
    {
        // EsProcess.deleteSingleDnBasedOnMessageId(ClusterType.BULK, "kp",
        // "210518200858_1");
        EsProcess.deleteSingleDn("kp", "210518200858");
    }

    private static void singleDnQuery()
    {
        final Map<MiddlewareConstant, String> lSingleDn0 = EsProcess.getSingleDn("kp", "210519122911", MiddlewareConstant.MW_DELIVERY_TIME, EsSortOrder.ASCENDING);
        final Map<MiddlewareConstant, String> lSingleDn1 = EsProcess.getSingleDn("kp", "210519122911", MiddlewareConstant.MW_DELIVERY_TIME, EsSortOrder.DECESNDING);
        System.out.println(lSingleDn0);
        System.out.println(lSingleDn1);
    }

    private static void singleDnInsert()
    {
        final Calendar c = Calendar.getInstance();
        c.setLenient(false);

        final String lFormattedCurrentDateTime = DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM_SS);

        for (int index = 0; index < 10; index++)
        {
            MessageRequest message;
			try {
				message = new MessageRequest(ClusterType.BULK, InterfaceType.HTTP_JAPI, InterfaceGroup.API, MessageType.TRANSACTIONAL, MessagePriority.PRIORITY_0, RouteType.DOMESTIC);
			

            c.add(Calendar.SECOND, 1);

            message.putValue(MiddlewareConstant.MW_BASE_MESSAGE_ID, lFormattedCurrentDateTime);
            message.putValue(MiddlewareConstant.MW_MESSAGE_ID, lFormattedCurrentDateTime + "_" + index);
            message.putValue(MiddlewareConstant.MW_CLIENT_ID, "kp");
            if ((index % 2) == 0)
                message.putValue(MiddlewareConstant.MW_MOBILE_NUMBER, "919884227203");
            else
                message.putValue(MiddlewareConstant.MW_MOBILE_NUMBER, "919884227204");
            message.putValue(MiddlewareConstant.MW_DELIVERY_TIME, DateTimeUtility.getFormattedDateTime(c.getTime(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
            // EsProcess.insertSingleDn(message);
			} catch (ItextosRuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    private static void readData()
            throws IOException
    {

        try (
                final RestHighLevelClient lEsClient = EsProcess.getInstance().getEsConnection();)
        {
            final String                          indexName    = EsUtility.getEsIndexName(EsTypeParent.SINGLE_DN);
            final GetRequest                      getRequest   = new GetRequest(indexName, "210517204231_9");
            final GetResponse                     lGetResponse = lEsClient.get(getRequest, RequestOptions.DEFAULT);
            final Map<String, Object>             lSource      = lGetResponse.getSource();
            final Map<MiddlewareConstant, String> returnValue  = EsUtility.getGenericResponseReader(EsConstant.SINGLE_DN_INSERT_FIELDS, lSource);
            EsProcess.getInstance().updateLastUsed();

            System.out.println(lSource);
            System.out.println(returnValue);
        }
    }

    private static void searchData()
            throws IOException
    {

        try (
                final RestHighLevelClient lEsClient = EsProcess.getInstance().getEsConnection();)
        {
            final String              indexName           = EsUtility.getEsIndexName(EsTypeParent.SINGLE_DN);
            final SearchRequest       searchRequest       = new SearchRequest(indexName);
            final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            // searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            // final MatchQueryBuilder matchQueryBuilder = new
            // MatchQueryBuilder(MiddlewareConstant.MW_FILE_ID.getKey(), "210518095118");
            // searchSourceBuilder.query(matchQueryBuilder);
            final MatchQueryBuilder   matchQueryBuilder   = new MatchQueryBuilder(MiddlewareConstant.MW_BASE_MESSAGE_ID.getKey(), "210518194554");
            searchSourceBuilder.query(matchQueryBuilder);

            searchSourceBuilder.from(0);
            searchSourceBuilder.size(1);
            searchSourceBuilder.sort(new FieldSortBuilder(MiddlewareConstant.MW_DELIVERY_TIME.getKey()).order(SortOrder.ASC));
            searchRequest.source(searchSourceBuilder);
            System.out.println(searchRequest);
            final SearchResponse searchResponse = lEsClient.search(searchRequest, RequestOptions.DEFAULT);
            EsProcess.getInstance().updateLastUsed();

            final RestStatus status          = searchResponse.status();
            final TimeValue  took            = searchResponse.getTook();
            final Boolean    terminatedEarly = searchResponse.isTerminatedEarly();
            final boolean    timedOut        = searchResponse.isTimedOut();
            final SearchHits hits            = searchResponse.getHits();
            final TotalHits  totalHits       = hits.getTotalHits();

            System.out.println("status          :" + status);
            System.out.println("took            :" + took);
            System.out.println("terminatedEarly :" + terminatedEarly);
            System.out.println("timout          :" + timedOut);
            System.out.println("hits            :" + hits);
            System.out.println("totalhits       :" + totalHits);

            final SearchHit[] searchHits = hits.getHits();

            for (final SearchHit hit : searchHits)
            {
                final String                          index          = hit.getIndex();
                final String                          id             = hit.getId();
                final float                           score          = hit.getScore();
                final String                          sourceAsString = hit.getSourceAsString();
                final Map<String, Object>             sourceAsMap    = hit.getSourceAsMap();

                // System.out.println("index :" + index);
                // System.out.println("id :" + id);
                // System.out.println("score :" + score);
                // System.out.println("source String :" + sourceAsString);
                // System.out.println("source Map :" + sourceAsMap);

                final Map<MiddlewareConstant, String> returnValue    = EsUtility.getGenericResponseReader(EsConstant.SINGLE_DN_INSERT_FIELDS, sourceAsMap);
                System.out.println("source Map    :" + returnValue);
            }
        }
    }

    public static void printDateFormat()
    {
        final long[] datelong =
        { 1621264441806L, 1621264625806L, 1621264737806L, 1621264806806L, 1621264875806L, 1621264932806L, 1621265035806L, 1621265066806L, 1621265243806L, 1621265429806L };

        for (final long l : datelong)
            System.out.println(DateTimeUtility.getFormattedDateTime(new Date(l), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
    }

    // public static void insertData()
    // {
    // final List<ITextosMessage> messageList = new ArrayList<>();
    // final Calendar c = Calendar.getInstance();
    // c.setLenient(false);
    //
    // final String lFormattedCurrentDateTime =
    // DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM_SS);
    //
    // for (int index = 0; index < 10; index++)
    // {
    // final BaseMessage message = new BaseMessage(ClusterType.BULK,
    // InterfaceType.HTTP_JAPI, InterfaceGroup.API, MessageType.TRANSACTIONAL,
    // MessagePriority.PRIORITY_0, RouteType.DOMESTIC);
    //
    // c.add(Calendar.SECOND, CommonUtility.getRandomNumber(30, 200));
    //
    // System.out.println("Time " +
    // DateTimeUtility.getFormattedDateTime(c.getTime(),
    // DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
    //
    // message.putValue(MiddlewareConstant.MW_FILE_ID, lFormattedCurrentDateTime);
    // message.putValue(MiddlewareConstant.MW_MESSAGE_ID, lFormattedCurrentDateTime
    // + "_" + index);
    // message.putValue(MiddlewareConstant.MW_CLIENT_ID, "kp");
    // message.putValue(MiddlewareConstant.MW_DELIVERY_TIME,
    // DateTimeUtility.getFormattedDateTime(c.getTime(),
    // DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
    // messageList.add(message);
    // }
    //
    // EsInmemoryCollectionFactory.getInstance().getInmemCollection(EsOperation.SINGLE_DN).add(messageList);
    // }

    public static void insertDataSync()
            throws IOException
    {
        final Calendar c = Calendar.getInstance();
        c.setLenient(false);

        final String              lFormattedCurrentDateTime = DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM_SS);
        final String              indexName                 = EsUtility.getEsIndexName(EsTypeParent.SINGLE_DN);
        final BulkRequest         bulkRequest               = new BulkRequest();
        final RestHighLevelClient lEsClient                 = EsProcess.getInstance().getEsConnection();

        for (int index = 0; index < 10; index++)
        {
            MessageRequest message;
			try {
				message = new MessageRequest(ClusterType.BULK, InterfaceType.HTTP_JAPI, InterfaceGroup.API, MessageType.TRANSACTIONAL, MessagePriority.PRIORITY_0, RouteType.DOMESTIC);
			

            c.add(Calendar.SECOND, 1);

            System.out.println("Time " + DateTimeUtility.getFormattedDateTime(c.getTime(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));

            message.putValue(MiddlewareConstant.MW_FILE_ID, lFormattedCurrentDateTime);
            message.putValue(MiddlewareConstant.MW_MESSAGE_ID, lFormattedCurrentDateTime + "_" + index);
            message.putValue(MiddlewareConstant.MW_CLIENT_ID, "kp");
            message.putValue(MiddlewareConstant.MW_DELIVERY_TIME, DateTimeUtility.getFormattedDateTime(c.getTime(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));

            final String       messageId    = message.getValue(MiddlewareConstant.MW_MESSAGE_ID);
            final IndexRequest indexRequest = new IndexRequest(indexName);
            indexRequest.id(messageId);

            final String commonJson = EsUtility.getJsonContent(message, EsConstant.SINGLE_DN_INSERT_FIELDS, EsCreateTimeStamp.SINGLE_DN_CTIME);
            indexRequest.source(commonJson, XContentType.JSON);
            bulkRequest.add(indexRequest);
			} catch (ItextosRuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        final BulkResponse lBulkResponse = lEsClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        EsProcess.getInstance().updateLastUsed();

        if (lBulkResponse.hasFailures())
            System.err.println("Index '" + indexName + "' has some failure");

        for (final BulkItemResponse bulkItemResponse : lBulkResponse)
            if (bulkItemResponse.isFailed())
            {
                System.err.println("Index " + indexName + " id " + bulkItemResponse.getId() + " Fail IMessage " + bulkItemResponse.getFailureMessage());
                System.err.println(">>>>>>>>>>> Need to handle this.");
            }
    }

}

class DlrQueryInsert
        implements
        Runnable
{

    private final String testIterations;

    public DlrQueryInsert(
            int aTestIterations)
    {
        testIterations = Thread.currentThread().getName() + "-" + aTestIterations;
    }

    @Override
    public void run()
    {
        final String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        for (int index = 1; index <= 10; index++)
        {
            MessageRequest mr;
			try {
				mr = new MessageRequest(ClusterType.BULK, InterfaceType.HTTP_JAPI, InterfaceGroup.API, MessageType.PROMOTIONAL, MessagePriority.PRIORITY_0, RouteType.DOMESTIC);
			

            mr.setClientId("7000000200000000-" + testIterations + "-" + index);
            mr.setClientMessageId("NoMsgId-" + testIterations + "-" + index);
            mr.setMobileNumber("919884227204-" + testIterations + "-" + index);
            mr.setFileId("fileId-" + index);
            mr.setBaseMessageId("basemesssageid-" + date + "-" + testIterations + "-" + index);
            mr.setHeader("header-" + testIterations + "-" + index);

            final MessagePart mp = new MessagePart("messageid-" + date + "-" + testIterations + "-" + index);
            final Date        d  = new Date();
            mp.setMessageReceivedDate(d);
            mp.setMessageReceivedTime(d);
            mp.setMessageActualReceivedDate(d);
            mp.setMessageActualReceivedTime(d);
            mp.setCarrierSubmitTime(d);
            mp.setActualCarrierSubmitTime(d);
            mp.setCarrierReceivedTime(d);

            mr.addMessagePart(mp);

            final long              startTime    = System.currentTimeMillis();
            final List<BaseMessage> lSubmissions = mr.getSubmissions();
            final long              endTime      = System.currentTimeMillis();
            System.out.println(mp.getMessageId() + " time takeb " + (endTime - startTime));

            for (final BaseMessage bm : lSubmissions)
            {
                final SubmissionObject so = (SubmissionObject) bm;

                so.setCarrierSubmitTime(new Date());
                so.setMessagePartNumber(1);
                so.setMessageTotalParts(1);
                so.setSubClientStatusCode("400");
                so.setSubClientStatusDesc("SUCCESS");

                final DeliveryObject doo = so.getDeliveryObject();

                doo.setCarrierReceivedTime(new Date());
                doo.setDnClientStatusCode("600");
                doo.setDnClientStatusDesc("SUCESS");
                doo.setHeader("DN-Header-" + testIterations + "-" + index);

                try
                {
                    EsProcess.insertDlrQueryDn(doo);
                }
                catch (final Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                
            }
			} catch (ItextosRuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

}