package com.itextos.beacon.platform.elasticsearchutil.utility;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import com.itextos.beacon.platform.elasticsearchutil.EsProcess;
import com.itextos.beacon.platform.elasticsearchutil.types.EsOperation;

public class EsBulkProcessor
{

    private static final Log               log = LogFactory.getLog(EsBulkProcessor.class);

    private final EsOperation              mEsTypeInsert;
    private final List<DocWriteRequest<?>> mRequestList;

    public EsBulkProcessor(
            EsOperation aEsTypeInsert,
            List<DocWriteRequest<?>> aRequestList)
    {
        mEsTypeInsert = aEsTypeInsert;
        mRequestList  = aRequestList;
    }

    public void process()
    {
        final BulkRequest bulkRequest = new BulkRequest();

        for (final DocWriteRequest<?> request : mRequestList)
            bulkRequest.add(request);

        if (log.isDebugEnabled())
            log.debug("Doing Bulk Async process for '" + mEsTypeInsert + "' of size '" + bulkRequest.numberOfActions() + "'");

        execute(bulkRequest);

        if (log.isDebugEnabled())
            log.debug("Bulk Async process called '" + mEsTypeInsert + "' of size '" + bulkRequest.numberOfActions() + "'. Response will be updated Asynch.");
    }

    private void execute(
            BulkRequest bulkRequest)
    {
        final RestHighLevelClient tempEsClient = EsProcess.getInstance().getEsConnection();
        tempEsClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, new EsAsyncBulkCallback(mEsTypeInsert, bulkRequest));
        EsProcess.getInstance().updateLastUsed();
    }

}