package com.itextos.beacon.dlrquery.queryprocess;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.dlrquery.request.RequestData;
import com.itextos.beacon.dlrquery.request.enums.DlrQueryStatus;
import com.itextos.beacon.platform.elasticsearchutil.EsProcess;
import com.itextos.beacon.platform.elasticsearchutil.types.DlrQueryMulti;

public class QueryProcessor
        extends
        QueryProcessorHelper
{

    private static final Log  log = LogFactory.getLog(QueryProcessor.class);

    private final RequestData mRequestData;

    public QueryProcessor(
            RequestData aRequestData)
    {
        mRequestData = aRequestData;
    }

    public String query()
    {
        final long           startTime       = System.currentTimeMillis();
        DlrQueryStatus       mDlrQueryStatus = DlrQueryStatus.VALID_REQUEST;
        List<MessageRequest> mResultList     = null;

        try
        {
            if (log.isDebugEnabled())
                log.debug("Request Data '" + mRequestData + "'");

            if (mRequestData.getDlrQueryStatus() == DlrQueryStatus.VALID_REQUEST)
            {
                final DlrQueryMulti                         lDlrQueryRequest = mRequestData.getDlrQueryRequest();
                final List<Map<MiddlewareConstant, String>> lDlrQueryInfo    = EsProcess.getDlrQueryInfo(lDlrQueryRequest);
                mResultList = getParsedData(mRequestData, lDlrQueryInfo);
            }
            else
                log.error("Request Data '" + mRequestData + "'");

            mDlrQueryStatus = mRequestData.getDlrQueryStatus();
        }
        catch (final Exception e)
        {
            log.error("Exception while processing Request Data '" + mRequestData + "'", e);
            mDlrQueryStatus = DlrQueryStatus.INTERNAL_ERROR;
        }
        String responseString;

        if (mRequestData.getDlrQueryStatus() == DlrQueryStatus.INVALID_ACCESS_KEY)
            responseString = "{\"status\":{\"code\":\"301\",\"reason\":\"Invalid Accesskey\"},\"records\":[]}";
        else
        {
            final ResponseGenerator rg = new ResponseGenerator(mRequestData, mDlrQueryStatus, mResultList);
            responseString = rg.generate();
        }
        if (log.isDebugEnabled())
            log.debug("Time taken to Query " + (System.currentTimeMillis() - startTime) + " millis.");

        return responseString;
    }

}