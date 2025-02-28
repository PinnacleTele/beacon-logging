package com.itextos.beacon.platform.elasticsearchutil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.platform.elasticsearchutil.types.DlrQueryMulti;
import com.itextos.beacon.platform.elasticsearchutil.types.EsConstant;
import com.itextos.beacon.platform.elasticsearchutil.types.EsOperation;
import com.itextos.beacon.platform.elasticsearchutil.types.EsTypeParent;
import com.itextos.beacon.platform.elasticsearchutil.utility.EsUtility;

public class DlrQuery
{

    private static final Log  log   = LogFactory.getLog(DlrQuery.class);
    private static final long _1_GB = 1_000_000_000L;

    private DlrQuery()
    {}

    static void insertDlrQuerySub(
            SubmissionObject aSubmissionObject)
            throws Exception
    {
        EsInmemoryCollectionFactory.getInstance().getInmemCollection(EsOperation.DLR_QUERY_SUB_INSERT).add(aSubmissionObject);
    }

    static void insertDlrQueryDn(
            DeliveryObject aDeliveryObject)
            throws Exception
    {
        EsInmemoryCollectionFactory.getInstance().getInmemCollection(EsOperation.DLR_QUERY_DN_INSERT).add(aDeliveryObject);
    }

    static List<Map<MiddlewareConstant, String>> get(
            DlrQueryMulti aRequest)
    {
        final List<Map<MiddlewareConstant, String>> returnValue = new ArrayList<>();

        try
        {
            final String indexName = EsUtility.getEsIndexName(EsTypeParent.DLR_QUERY);

            if (log.isDebugEnabled())
                log.debug("Index Name : '" + indexName + "'");

            getResults(indexName, aRequest, returnValue);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the DLR Query record from Elastic search for " + aRequest, e);
        }
        return returnValue;
    }

    private static void getResults(
            String aIndexName,
            DlrQueryMulti aRequest,
            List<Map<MiddlewareConstant, String>> aReturnValue)
            throws IOException
    {
        boolean                   hasResults          = true;
        final Scroll              scroll              = new Scroll(TimeValue.timeValueMinutes(10L));
        String                    scrollId            = null;

        final long                startTime           = System.currentTimeMillis();

        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(10000);

        buildMatchQuery(searchSourceBuilder, aRequest);

        while (hasResults)
        {
            final SearchResponse searchResponse = getResponseFromEs(aIndexName, searchSourceBuilder, scroll, scrollId);

            if (searchResponse != null)
            {
                final SearchHits  hits       = searchResponse.getHits();
                final TotalHits   totalHits  = hits.getTotalHits();
                final SearchHit[] searchHits = hits.getHits();
                scrollId = searchResponse.getScrollId();

                if (log.isTraceEnabled())
                    log.trace("For the filter " + aRequest + " Total Records : '" + totalHits + "' Search results : " + (searchHits != null ? searchHits.length : "-1"));

                if ((searchHits != null) && (searchHits.length > 0))
                {

                    for (final SearchHit hit : searchHits)
                    {
                        final Map<String, Object>             sourceAsMap = hit.getSourceAsMap();
                        final Map<MiddlewareConstant, String> temp        = EsUtility.getGenericResponseReader(EsConstant.DNQUERY_RESPONSE_FIELDS, sourceAsMap);
                        aReturnValue.add(temp);
                    }
                    hasResults = true;
                }
                else
                {
                    hasResults = false;
                    if (log.isDebugEnabled())
                        log.debug("No Records found for DLR Query record from Elastic search for " + aRequest);
                }
            }
            else
            {
                hasResults = false;
                if (log.isDebugEnabled())
                    log.debug("Response is NULL. No Records found for DLR Query record from Elastic search for " + aRequest);
            }

            if (log.isDebugEnabled())
            {
                final Runtime lRuntime = Runtime.getRuntime();
                log.debug("Results count : '" + aReturnValue.size() + "' Max Memory '" + (lRuntime.maxMemory() / (1.0 * _1_GB)) + "' GB Total Memory '" + (lRuntime.totalMemory() / (1.0 * _1_GB))
                        + "' GB Free Memory '" + (lRuntime.freeMemory() / (1.0 * _1_GB)) + "' GB");
            }
        }

        clearScrollSearch(scrollId);
        final long endTime = System.currentTimeMillis();

        if (log.isInfoEnabled())
            log.info("Time taken : '" + ((endTime - startTime) / 1000.0) + "' seconds");
    }

    private static void clearScrollSearch(
            String aScrollId)
            throws IOException
    {

        if (aScrollId != null)
        {
            final ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(aScrollId);

            final RestHighLevelClient tempEsClient        = EsProcess.getInstance().getEsConnection();
            final ClearScrollResponse clearScrollResponse = tempEsClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);

            if (log.isDebugEnabled())
                log.debug("Cleared Scroll Request " + clearScrollResponse);

            EsProcess.getInstance().updateLastUsed();
        }
    }

    private static SearchResponse getResponseFromEs(
            String aIndexName,
            SearchSourceBuilder searchSourceBuilder,
            Scroll scroll,
            String aScrollId)
            throws IOException
    {
        SearchResponse searchResponse = null;

        if (aScrollId == null)
        {
            final SearchRequest searchRequest = new SearchRequest(aIndexName);
            searchRequest.scroll(scroll);
            searchRequest.source(searchSourceBuilder);

            if (log.isTraceEnabled())
                log.trace("Search Criteria : '" + searchRequest + "'");

            final RestHighLevelClient tempEsClient = EsProcess.getInstance().getEsConnection();
            searchResponse = tempEsClient.search(searchRequest, RequestOptions.DEFAULT);
            EsProcess.getInstance().updateLastUsed();
        }
        else
        {
            final SearchScrollRequest scrollRequest = new SearchScrollRequest(aScrollId);
            scrollRequest.scroll(scroll);
            scrollRequest.scrollId(aScrollId);

            if (log.isTraceEnabled())
                log.trace("Search Criteria : '" + scrollRequest + "'");

            final RestHighLevelClient tempEsClient = EsProcess.getInstance().getEsConnection();
            searchResponse = tempEsClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            EsProcess.getInstance().updateLastUsed();
        }

        return searchResponse;
    }

    private static void buildMatchQuery(
            SearchSourceBuilder aSearchSourceBuilder,
            DlrQueryMulti aDlrQueryRequest)
    {
        QueryBuilder       mqMessage         = null;
        QueryBuilder       mqMobileNumber    = null;
        QueryBuilder       mqClientMessageId = null;

        final QueryBuilder mqClientId        = new TermsQueryBuilder(MiddlewareConstant.MW_CLIENT_ID.getName(), aDlrQueryRequest.getClientId());

        if (aDlrQueryRequest.isFileIdBased())
            mqMessage = new TermsQueryBuilder(MiddlewareConstant.MW_FILE_ID.getName(), aDlrQueryRequest.getFileIdList());

        if (aDlrQueryRequest.isDestBased())
            mqMobileNumber = new TermsQueryBuilder(MiddlewareConstant.MW_MOBILE_NUMBER.getName(), aDlrQueryRequest.getDestList());

        if (aDlrQueryRequest.isCliMsgIdBased())
            mqClientMessageId = new TermsQueryBuilder(MiddlewareConstant.MW_CLIENT_MESSAGE_ID.getName(), aDlrQueryRequest.getCliMsgIdList());

        final BoolQueryBuilder lBoolQueryBuilder = new BoolQueryBuilder();
        lBoolQueryBuilder.must(mqClientId);

        if (mqMessage != null)
            lBoolQueryBuilder.must(mqMessage);

        if (mqMobileNumber != null)
            lBoolQueryBuilder.must(mqMobileNumber);

        if (mqClientMessageId != null)
            lBoolQueryBuilder.must(mqClientMessageId);

        aSearchSourceBuilder.query(lBoolQueryBuilder);

        if (log.isDebugEnabled())
            log.debug("Search Query " + aSearchSourceBuilder);
    }

}