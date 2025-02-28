package com.itextos.beacon.dlrquery.queryprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.stringprocessor.dto.ParamDataType;
import com.itextos.beacon.commonlib.stringprocessor.dto.ValidatorMaster;
import com.itextos.beacon.commonlib.stringprocessor.dto.ValidatorParams;
import com.itextos.beacon.commonlib.stringprocessor.process.ValidatorProcess;
import com.itextos.beacon.dlrquery.request.RequestData;
import com.itextos.beacon.dlrquery.request.enums.DlrQueryStatus;
import com.itextos.beacon.inmemory.dlrquery.InmemDlrQuery;
import com.itextos.beacon.inmemory.dlrquery.objects.DlrBodyParam;
import com.itextos.beacon.inmemory.dlrquery.objects.DlrConfigDetail;
import com.itextos.beacon.inmemory.dlrquery.objects.DlrDataType;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class ResponseGenerator
{

    private static final Log           log        = LogFactory.getLog(QueryProcessorHelper.class);

    private static final String        HDR_CODE   = "{code}";
    private static final String        HDR_REASON = "{reason}";

    private final RequestData          mRequestData;
    private final DlrQueryStatus       mDlrQueryStatus;
    private final List<MessageRequest> mResultList;

    public ResponseGenerator(
            RequestData aRequestData,
            DlrQueryStatus aDlrQueryStatus,
            List<MessageRequest> aResultList)
    {
        mRequestData    = aRequestData;
        mDlrQueryStatus = aDlrQueryStatus;
        mResultList     = aResultList;
    }

    public String generate()
    {
        return getRecordString();
    }

    private String getRecordString()
    {

        try
        {
            final InmemDlrQuery              inmemDlrQuery       = (InmemDlrQuery) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.DLR_QUERY);

            final String                     lResponseHeaderId   = inmemDlrQuery.getResponseHeaderId(mRequestData.getClientId());
            final DlrConfigDetail            lDlrConfigDetail    = inmemDlrQuery.getDlrConfigDetail(lResponseHeaderId);
            final Map<Integer, DlrBodyParam> lDlrConfigBodyParam = inmemDlrQuery.getDlrConfigBodyParam(lResponseHeaderId);

            final ValidatorMaster            master              = getValidatorMaster(lDlrConfigDetail, lDlrConfigBodyParam);
            final String                     header              = populateHeader(master.getBodyHeader());

            final StringJoiner               sj                  = new StringJoiner(lDlrConfigDetail.getBatchBodyDelimiter(), header, master.getBodyFooter());

            if ((mResultList != null) && !mResultList.isEmpty())
                for (final MessageRequest msgRequest : mResultList)
                {
                    final String prcoessedMessage = ValidatorProcess.processTemplate(master, msgRequest);
                    sj.add(prcoessedMessage);
                }

            return sj.toString();
        }
        catch (final Exception e)
        {
            log.error("Exception occer while generate response :", e);
            return "{\"status\":{\"code\":\"500\",\"reason\":\"Internal Error\"},\"records\":[]}";
        }
    }

    public String populateHeader(
            String aBodyHeader)
    {
        final String returnValue = aBodyHeader.replace(HDR_CODE, mDlrQueryStatus.getStatusCode());
        return returnValue.replace(HDR_REASON, mDlrQueryStatus.getStatusDesc());
    }

    private static ValidatorMaster getValidatorMaster(
            DlrConfigDetail aDlrConfigDetail,
            Map<Integer, DlrBodyParam> aDlrConfigBodyParam)
    {
        final List<ValidatorParams> paramList = new ArrayList<>();

        for (final Entry<Integer, DlrBodyParam> entry : aDlrConfigBodyParam.entrySet())
        {
            final int             index     = entry.getKey();
            final DlrBodyParam    bodyParam = entry.getValue();
            final ValidatorParams params    = new ValidatorParams(index, bodyParam.getConstantName(), bodyParam.getAlternativeConstantName(), bodyParam.getDefaultValue(),
                    getParamType(bodyParam.getDlrDataType()), bodyParam.getDataFormat(), bodyParam.getDataValidation(), bodyParam.getDroolsDataPath());
            paramList.add(params);
        }

        if (log.isDebugEnabled())
            log.debug("DlrConfig Details : " + aDlrConfigDetail);

        return new ValidatorMaster(false, aDlrConfigDetail.getBodyTemplate(), aDlrConfigDetail.getBodyHeader(), aDlrConfigDetail.getBodyFooter(), aDlrConfigDetail.getBatchBodyDelimiter(), paramList);
    }

    private static ParamDataType getParamType(
            DlrDataType aDlrDataType)
    {

        switch (aDlrDataType)
        {
            case DATE_TIME:
                return ParamDataType.DATE_TIME;

            case NUMBER:
                return ParamDataType.NUMBER;

            case STRING:
            default:
                return ParamDataType.STRING;
        }
    }

}