package com.itextos.beacon.platform.elasticsearchutil.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;

import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.SubmissionObject;
import com.itextos.beacon.platform.elasticsearchutil.types.EsConstant;
import com.itextos.beacon.platform.elasticsearchutil.types.EsCreateTimeStamp;
import com.itextos.beacon.platform.elasticsearchutil.types.EsOperation;
import com.itextos.beacon.platform.elasticsearchutil.utility.ElasticsearchProperties;
import com.itextos.beacon.platform.elasticsearchutil.utility.EsUtility;

public class DlrQuerySubBulk
        extends
        AbstractEsInmemoryCollection
{

    public DlrQuerySubBulk()
    {
        super(EsOperation.DLR_QUERY_SUB_INSERT);
    }

    private static final Log log = LogFactory.getLog(DlrQuerySubBulk.class);

    @Override
    DocWriteRequest<?> getInsertUpdateRequest(
            String aEsIndexName,
            BaseMessage aMessage)
    {
        final SubmissionObject submissionObject = (SubmissionObject) aMessage;

        try
        {
            final String        messageId     = submissionObject.getValue(MiddlewareConstant.MW_MESSAGE_ID);

            final UpdateRequest updateRequest = new UpdateRequest(aEsIndexName, messageId).docAsUpsert(true);
            updateRequest.retryOnConflict(ElasticsearchProperties.getInstance().getRetryConflictResolveCount());

            final String subJson = EsUtility.getJsonContent(submissionObject, EsConstant.DNQUERY_SUBMISSION_FIELDS, EsCreateTimeStamp.DLR_QUERY_SUB_CTIME);
            updateRequest.doc(subJson, XContentType.JSON);

            if (log.isDebugEnabled())
                log.debug("Upsert request for submission " + messageId + " JSON " + subJson);

            return updateRequest;
        }
        catch (final Exception e)
        {
            final String                  errorMessage = "Unable to create the index " + aEsIndexName;
            final ItextosRuntimeException nre          = new ItextosRuntimeException(errorMessage, e);
            log.error(errorMessage, nre);
           // throw nre;
        }
        return null;
    }

}
