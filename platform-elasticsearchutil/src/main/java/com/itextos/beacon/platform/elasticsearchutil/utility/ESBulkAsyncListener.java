package com.itextos.beacon.platform.elasticsearchutil.utility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;

public class ESBulkAsyncListener
        implements
        ActionListener<BulkResponse>
{

    private static final Log log       = LogFactory.getLog(ESBulkAsyncListener.class);
    protected BulkRequest    bRequest;
    protected boolean        isFullMsg = false;

    public ESBulkAsyncListener(
            BulkRequest aRequest,
            boolean aIsFullMsg)
    {
        bRequest  = aRequest;
        isFullMsg = aIsFullMsg;
    }

    @Override
    public void onResponse(
            BulkResponse aResponse)
    {

        if (aResponse.hasFailures())
        {
            final BulkItemResponse biResponses[] = aResponse.getItems();

            for (int rIdx = 0; rIdx < biResponses.length; rIdx++)
            {
                final BulkItemResponse response = biResponses[rIdx];

                if (response.isFailed())
                {
                    final BulkItemResponse.Failure failure = response.getFailure();
                    final DocWriteRequest          req     = bRequest.requests().get(rIdx);
                    final String                   reqId   = req.id();

                    if (isFullMsg)
                    {
                        log.error("FullMsg, Failed to Index[" + reqId + "]: " + req.toString());
                        log.error("FullMsg, Failure Message[" + reqId + "]: " + failure.getMessage());
                        log.error("FullMsg, Exception[" + reqId + "]: ", failure.getCause());
                    }
                    else
                    {
                        log.error("Failed to Index[" + reqId + "]: " + req.toString());
                        log.error("Failure Message[" + reqId + "]: " + failure.getMessage());
                        log.error("Exception[" + reqId + "]: ", failure.getCause());
                    }
                }
            }
        }
    }

    @Override
    public void onFailure(
            Exception aException)
    {
        log.error(aException.getMessage(), aException);
    }

}
