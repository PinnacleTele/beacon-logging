package com.itextos.beacon.platform.elasticsearchutil.utility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkItemResponse.Failure;

import com.itextos.beacon.platform.elasticsearchutil.types.EsOperation;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;

public class EsAsyncBulkCallback
        implements
        ActionListener<BulkResponse>
{

    private static final Log  log       = LogFactory.getLog(EsAsyncBulkCallback.class);

    private final EsOperation mElasticSearchType;
    private final BulkRequest mBulkRequest;
    private final long        startTime = System.currentTimeMillis();

    public EsAsyncBulkCallback(
            EsOperation aElasticSearchType,
            BulkRequest aBulkRequest)
    {
        mElasticSearchType = aElasticSearchType;
        mBulkRequest       = aBulkRequest;
    }

    @Override
    public void onResponse(
            BulkResponse aResponse)
    {
        final long endTime      = System.currentTimeMillis();
        int        successCount = 0;
        int        failureCount = 0;

        if (aResponse.hasFailures())
        {
            final BulkItemResponse[] bulkResponseItems = aResponse.getItems();

            for (int index = 0; index < bulkResponseItems.length; index++)
            {
                final BulkItemResponse response = bulkResponseItems[index];

                if (response.isFailed())
                {
                    final BulkItemResponse.Failure failure = response.getFailure();
                    final DocWriteRequest<?>       req     = mBulkRequest.requests().get(index);
                    processRequest(index, req, failure);
                    failureCount++;
                }
                else
                    successCount++;
            }
            if (log.isDebugEnabled())
                log.debug("Time taken : '" + (endTime - startTime) + "' Success Records " + successCount + " Failed Records " + failureCount);
        }
        else
            if (log.isDebugEnabled())
                log.debug("Time taken : '" + (endTime - startTime) + "'");
    }

    private void processRequest(
            int aIndex,
            DocWriteRequest<?> aReq,
            Failure aFailure)
    {
        log.error("Failed to Index [" + aIndex + "]: " + aReq.toString());
        log.error("Failure Message [" + aIndex + "]: " + aFailure.getMessage());
        log.error("Exception       [" + aIndex + "]: ", aFailure.getCause());

        switch (mElasticSearchType)
        {
            case AGING_INSERT:
                log.error("Need to reporcess the message. Aging Insert");
                break;

            case AGING_DELETE:
                log.error("Need to reporcess the message. Aging Delete");
                break;

            case AGING_UPDATE:
                log.error("Need to reporcess the message. Aging Update");
                break;

            case DLR_QUERY_DN_INSERT:
                log.error("Need to reporcess the message. DN Query Dn Insert");
                break;

            case DLR_QUERY_SUB_INSERT:
                log.error("Need to reporcess the message.Dn Query Sub Insert");
                break;

            case R3_INSERT:
            case SINGLE_DN_DELETE:
            case SINGLE_DN_INSERT:
            case SINGLE_DN_QUERY:
            default:
                break;
        }
    }

    @Override
    public void onFailure(
            Exception aException)
    {
        log.error("Failed Async Bulk process. Need to reprocess the entire request set. Total request : " + mBulkRequest.numberOfActions(), aException);
        for (final DocWriteRequest<?> requestObject : mBulkRequest.requests())
            log.error("Need to reporcess the message. " + requestObject);
    }

}