package com.itextos.beacon.platform.elasticsearchutil;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;

import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.elasticsearchutil.types.EsConstant;
import com.itextos.beacon.platform.elasticsearchutil.types.EsSortOrder;
import com.itextos.beacon.platform.elasticsearchutil.types.EsTypeParent;
import com.itextos.beacon.platform.elasticsearchutil.utility.EsUtility;

class SingleDnProcess
{

    private static final Log log = LogFactory.getLog(SingleDnProcess.class);

    private SingleDnProcess()
    {}

    static boolean insert(
            DeliveryObject aDeliveryObject)
    {
        boolean isSuccess = true;

        try
        {
            final String indexName = EsUtility.getEsIndexName(EsTypeParent.SINGLE_DN);

            if (log.isDebugEnabled())
                log.debug("Index Name : '" + indexName + "'");

            final IndexRequest        lInsertRequest = EsUtility.getSingleDnInsertRequest(indexName, aDeliveryObject);
            final RestHighLevelClient tempEsClient   = EsProcess.getInstance().getEsConnection();
            final IndexResponse       lIndex         = tempEsClient.index(lInsertRequest, RequestOptions.DEFAULT);

            if (log.isDebugEnabled())
                log.debug("Index '" + lIndex.getIndex() + "' Id '" + lIndex.getId() + "' Version '" + lIndex.getVersion() + "'");

            EsProcess.getInstance().updateLastUsed();
        }
        catch (final Exception e)
        {
            isSuccess = false;
            log.error("Exception while inserting Single Dn into Elasticsearch. " + aDeliveryObject, e);
        }
        return isSuccess;
    }

    static boolean delete(
            String aClientId,
            String aMessageId)
    {
        final boolean returnValue = false;

        try
        {
            final String indexName = EsUtility.getEsIndexName(EsTypeParent.SINGLE_DN);

            if (log.isDebugEnabled())
                log.debug("Index Name : '" + indexName + "'");

            final DeleteRequest deleteRequest = new DeleteRequest(indexName);
            deleteRequest.id(aMessageId);

            final RestHighLevelClient tempEsClient   = EsProcess.getInstance().getEsConnection();
            final DeleteResponse      deleteResponse = tempEsClient.delete(deleteRequest, RequestOptions.DEFAULT);

            if (deleteResponse.getResult() == DocWriteResponse.Result.DELETED)
            {
                if (log.isDebugEnabled())
                    log.debug("Record deleted for Client Id '" + aClientId + "' IMessage Id '" + aMessageId + "'");
            }
            else
                if (log.isDebugEnabled())
                    log.debug("Record not deleted for Client Id '" + aClientId + "' IMessage Id '" + aMessageId + "'");

            EsProcess.getInstance().updateLastUsed();
        }
        catch (final Exception e)
        {
            log.error("Exception while deleting the SingleDn record. ClientId : '" + aClientId + "' MessageId : '" + aMessageId + "'", e);
        }
        return returnValue;
    }

    static boolean deleteSingleDn(
            String aClientId,
            String aBaseMessageId,
            String aMessageId)
    {
        boolean returnValue = false;

        try
        {
            final String indexName = EsUtility.getEsIndexName(EsTypeParent.SINGLE_DN);

            if (log.isDebugEnabled())
                log.debug("Index Name : '" + indexName + "'");

            final DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);

            request.setConflicts("proceed");
            request.setQuery(getFilterCondition(aBaseMessageId, aMessageId));
            request.setMaxDocs(100); // TODO Need to check this value.
            request.setBatchSize(100);
            request.setSlices(2);

            final RestHighLevelClient  tempEsClient = EsProcess.getInstance().getEsConnection();
            final BulkByScrollResponse bulkResponse = tempEsClient.deleteByQuery(request, RequestOptions.DEFAULT);
            EsProcess.getInstance().updateLastUsed();

            final long totalDocs   = bulkResponse.getTotal();
            final long deletedDocs = bulkResponse.getDeleted();

            if (log.isDebugEnabled())
                log.debug("Client Id '" + aClientId + "' Base IMessage Id '" + aBaseMessageId + "' Total Docs processed '" + totalDocs + "' Total Deleted Docs '" + deletedDocs + "'");
            returnValue = true;
        }
        catch (final Exception e)
        {
            log.error("Exception while deleting the records. Client Id '" + aClientId + "' Base IMessage Id '" + aBaseMessageId + "'", e);
        }
        return returnValue;
    }

    private static QueryBuilder getFilterCondition(
            String aBaseMessageId,
            String aMessageId)
    {
        final QueryBuilder     lBaseMessageIdFilter = new MatchQueryBuilder(MiddlewareConstant.MW_BASE_MESSAGE_ID.getName(), aBaseMessageId);
        final BoolQueryBuilder lBoolQueryBuilder    = new BoolQueryBuilder();
        lBoolQueryBuilder.must(lBaseMessageIdFilter);

        final String msgId = CommonUtility.nullCheck(aMessageId, true);

        if (!msgId.isEmpty())
        {
            final QueryBuilder lMessageIdFilter = new MatchQueryBuilder(MiddlewareConstant.MW_MESSAGE_ID.getName(), msgId);
            lBoolQueryBuilder.must(lMessageIdFilter);
        }
        return lBoolQueryBuilder;
    }

    static Map<MiddlewareConstant, String> get(
            String aClientId,
            String aBaseId,
            MiddlewareConstant aSortbasedOn,
            EsSortOrder aSearchOrder)
    {
        Map<MiddlewareConstant, String> returnValue = new EnumMap<>(MiddlewareConstant.class);

        try
        {
            final String indexName = EsUtility.getEsIndexName(EsTypeParent.SINGLE_DN);

            if (log.isDebugEnabled())
                log.debug("Index Name : '" + indexName + "'");

            final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            buildMatchQuery(searchSourceBuilder, aBaseId);

            searchSourceBuilder.from(0); // Filter from ZERO-th record
            searchSourceBuilder.size(1); // Give at the maximum of one record.

            addSortOptions(searchSourceBuilder, aSortbasedOn, aSearchOrder);

            final SearchRequest searchRequest = new SearchRequest(indexName);
            searchRequest.source(searchSourceBuilder);

            if (log.isDebugEnabled())
                log.debug("Search Criteria : '" + searchRequest.toString() + "'");

            final RestHighLevelClient tempEsClient   = EsProcess.getInstance().getEsConnection();
            final SearchResponse      searchResponse = tempEsClient.search(searchRequest, RequestOptions.DEFAULT);
            EsProcess.getInstance().updateLastUsed();

            final SearchHits  hits       = searchResponse.getHits();
            final TotalHits   totalHits  = hits.getTotalHits();
            final SearchHit[] searchHits = hits.getHits();

            if (log.isDebugEnabled())
                log.debug("Total Records : '" + totalHits + "' Search results : " + (searchHits != null ? searchHits.length : "-1"));

            if ((searchHits != null) && (searchHits.length > 0))
            {
                if ((searchHits.length > 1) && log.isDebugEnabled())
                    log.debug("Using the first record only as result received " + searchHits.length + " records.");

                final SearchHit           hit         = searchHits[0];
                final Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                returnValue = EsUtility.getGenericResponseReader(EsConstant.SINGLE_DN_INSERT_FIELDS, sourceAsMap);
            }
            else
                if (log.isDebugEnabled())
                    log.debug("No Records found for Single Dn from Elastic search ClientId : '" + aClientId + "' BaseMessageId : '" + aBaseId + "' SearchOrder : '" + aSearchOrder + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Single Dn record from Elastic search ClientId : '" + aClientId + "' BaseMessageId : '" + aBaseId + "' SearchOrder : '" + aSearchOrder + "'", e);
        }
        return returnValue;
    }

    private static void addSortOptions(
            SearchSourceBuilder aSearchSourceBuilder,
            MiddlewareConstant aSortbasedOn,
            EsSortOrder aSearchOrder)
    {
        aSearchSourceBuilder.sort(new FieldSortBuilder(aSortbasedOn.getName()).order(aSearchOrder.getSortOrder()));
    }

    private static void buildMatchQuery(
            SearchSourceBuilder aSearchSourceBuilder,
            String aBaseId)
    {
        final MatchQueryBuilder mqMessageId       = new MatchQueryBuilder(MiddlewareConstant.MW_BASE_MESSAGE_ID.getName(), aBaseId);
        final BoolQueryBuilder  lBoolQueryBuilder = new BoolQueryBuilder();
        lBoolQueryBuilder.must(mqMessageId);
        aSearchSourceBuilder.query(lBoolQueryBuilder);
    }

}