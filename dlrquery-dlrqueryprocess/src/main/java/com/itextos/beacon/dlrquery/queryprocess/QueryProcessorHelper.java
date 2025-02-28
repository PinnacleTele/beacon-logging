package com.itextos.beacon.dlrquery.queryprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.dlrquery.request.RequestData;

class QueryProcessorHelper
{

    private static final Log log         = LogFactory.getLog(QueryProcessorHelper.class);

    private static final int DEST        = 1;
    private static final int FILE_ID     = 2;
    private static final int CUST_REF_ID = 3;

    QueryProcessorHelper()
    {}

    static List<MessageRequest> getParsedData(
            RequestData aRequestData,
            List<Map<MiddlewareConstant, String>> aDlrQueryInfo) throws ItextosRuntimeException
    {
        final List<MessageRequest> returnValue = new ArrayList<>();

        if (log.isDebugEnabled())
            log.debug("Total records fetch " + aDlrQueryInfo.size());

        if (aRequestData.isMultiple())
        {
            if (aRequestData.isDestListBased())
                validateForList(returnValue, aDlrQueryInfo, aRequestData.getDestList(), DEST);
            else
                if (aRequestData.isFileIdListBased())
                    validateForList(returnValue, aDlrQueryInfo, aRequestData.getFileIdList(), FILE_ID);
                else
                    if (aRequestData.isCustRefIdListBased())
                        validateForList(returnValue, aDlrQueryInfo, aRequestData.getCusrRefIdList(), CUST_REF_ID);
        }
        else
            if (aDlrQueryInfo.isEmpty())
                returnValue.add(getBlankMessageRequest(aRequestData.getDest(), aRequestData.getFileId(), aRequestData.getCustRefId()));
            else
                for (final Map<MiddlewareConstant, String> temp : aDlrQueryInfo)
                    returnValue.add(createMessageRequest(temp));
        return returnValue;
    }

    static void validateForList(
            List<MessageRequest> aReturnValue,
            List<Map<MiddlewareConstant, String>> aDlrQueryInfo,
            List<String> aSearchList,
            int aSearchType) throws ItextosRuntimeException
    {
        final List<String> addedList = new ArrayList<>();

        switch (aSearchType)
        {
            case DEST:
                for (final Map<MiddlewareConstant, String> temp : aDlrQueryInfo)
                {
                    final String dest = temp.getOrDefault(MiddlewareConstant.MW_MOBILE_NUMBER, "");

                    if (aSearchList.contains(dest))
                    {
                        final MessageRequest lMessageRequest = createMessageRequest(temp);
                        aReturnValue.add(lMessageRequest);
                        addedList.add(dest);
                    }
                }
                break;

            case FILE_ID:
                for (final Map<MiddlewareConstant, String> temp : aDlrQueryInfo)
                {
                    final String fileId = temp.getOrDefault(MiddlewareConstant.MW_FILE_ID, "");

                    if (aSearchList.contains(fileId))
                    {
                        final MessageRequest lMessageRequest = createMessageRequest(temp);
                        aReturnValue.add(lMessageRequest);
                        addedList.add(fileId);
                    }
                }
                break;

            case CUST_REF_ID:
                for (final Map<MiddlewareConstant, String> temp : aDlrQueryInfo)
                {
                    final String custRefId = temp.getOrDefault(MiddlewareConstant.MW_CLIENT_MESSAGE_ID, "");

                    if (aSearchList.contains(custRefId))
                    {
                        final MessageRequest lMessageRequest = createMessageRequest(temp);
                        aReturnValue.add(lMessageRequest);
                        addedList.add(custRefId);
                    }
                }
                break;

            default:
        }

        final List<String> duplicate = new ArrayList<>(aSearchList);
        duplicate.removeAll(addedList);

        if (!duplicate.isEmpty())
        {
            final List<MessageRequest> missingRecords = getMissedRecords(aSearchType, duplicate);
            aReturnValue.addAll(missingRecords);
        }
    }

    static List<MessageRequest> getMissedRecords(
            int aSearchType,
            List<String> aDuplicate) throws ItextosRuntimeException
    {
        List<MessageRequest> retrunValue = new ArrayList<>();

        switch (aSearchType)
        {
            case DEST:
                retrunValue = getDestRecords(aDuplicate);
                break;

            case FILE_ID:
                retrunValue = getFileIdRecords(aDuplicate);
                break;

            case CUST_REF_ID:
                retrunValue = getCustRefIdRecords(aDuplicate);
                break;

            default:
        }
        return retrunValue;
    }

    static List<MessageRequest> getFileIdRecords(
            List<String> aDuplicate) throws ItextosRuntimeException
    {
        final List<MessageRequest> returnValue = new ArrayList<>();

        for (final String fileId : aDuplicate)
            returnValue.add(getBlankMessageRequest(null, fileId, null));

        return returnValue;
    }

    static List<MessageRequest> getCustRefIdRecords(
            List<String> aDuplicate) throws ItextosRuntimeException
    {
        final List<MessageRequest> returnValue = new ArrayList<>();

        for (final String custRefId : aDuplicate)
            returnValue.add(getBlankMessageRequest(null, null, custRefId));

        return returnValue;
    }

    static List<MessageRequest> getDestRecords(
            List<String> aDuplicate) throws ItextosRuntimeException
    {
        final List<MessageRequest> returnValue = new ArrayList<>();

        for (final String dest : aDuplicate)
            returnValue.add(getBlankMessageRequest(dest, null, null));

        return returnValue;
    }

    static MessageRequest getBlankMessageRequest(
            String aDest,
            String aFileId,
            String aCustRefId) throws ItextosRuntimeException
    {
        final MessageRequest lMessageRequest = getDefaultMessageRequest();

        if ((aDest != null) && !aDest.isBlank())
            lMessageRequest.putValue(MiddlewareConstant.MW_MOBILE_NUMBER, aDest);

        if ((aFileId != null) && !aFileId.isBlank())
            lMessageRequest.putValue(MiddlewareConstant.MW_FILE_ID, aFileId);

        if ((aCustRefId != null) && !aCustRefId.isBlank())
            lMessageRequest.putValue(MiddlewareConstant.MW_CLIENT_MESSAGE_ID, aCustRefId);

        return lMessageRequest;
    }

    static MessageRequest createMessageRequest(
            Map<MiddlewareConstant, String> aRecord) throws ItextosRuntimeException
    {
        final MessageRequest lMessageRequest = getDefaultMessageRequest();

        lMessageRequest.putValue(MiddlewareConstant.MW_CLIENT_ID, aRecord.getOrDefault(MiddlewareConstant.MW_CLIENT_ID, ""));
        lMessageRequest.putValue(MiddlewareConstant.MW_HEADER, aRecord.getOrDefault(MiddlewareConstant.MW_HEADER, ""));
        lMessageRequest.putValue(MiddlewareConstant.MW_MOBILE_NUMBER, aRecord.getOrDefault(MiddlewareConstant.MW_MOBILE_NUMBER, ""));
        lMessageRequest.putValue(MiddlewareConstant.MW_MESSAGE_ID, aRecord.getOrDefault(MiddlewareConstant.MW_MESSAGE_ID, ""));
        lMessageRequest.putValue(MiddlewareConstant.MW_FILE_ID, aRecord.getOrDefault(MiddlewareConstant.MW_FILE_ID, ""));
        lMessageRequest.putValue(MiddlewareConstant.MW_BASE_MESSAGE_ID, aRecord.getOrDefault(MiddlewareConstant.MW_BASE_MESSAGE_ID, ""));
        lMessageRequest.putValue(MiddlewareConstant.MW_CLIENT_MESSAGE_ID, aRecord.getOrDefault(MiddlewareConstant.MW_CLIENT_MESSAGE_ID, ""));
        lMessageRequest.putValue(MiddlewareConstant.MW_MSG_RECEIVED_TIME, aRecord.getOrDefault(MiddlewareConstant.MW_MSG_RECEIVED_TIME, null));
        lMessageRequest.putValue(MiddlewareConstant.MW_MSG_RECEIVED_DATE, aRecord.getOrDefault(MiddlewareConstant.MW_MSG_RECEIVED_DATE, null));
        lMessageRequest.putValue(MiddlewareConstant.MW_MSG_TOTAL_PARTS, aRecord.getOrDefault(MiddlewareConstant.MW_MSG_TOTAL_PARTS, "0"));
        lMessageRequest.putValue(MiddlewareConstant.MW_CARRIER_SUBMIT_TIME, aRecord.getOrDefault(MiddlewareConstant.MW_CARRIER_SUBMIT_TIME, null));
        lMessageRequest.putValue(MiddlewareConstant.MW_CARRIER_RECEIVED_TIME, aRecord.getOrDefault(MiddlewareConstant.MW_CARRIER_RECEIVED_TIME, null));
        lMessageRequest.putValue(MiddlewareConstant.MW_SUB_CLI_STATUS_CODE, aRecord.getOrDefault(MiddlewareConstant.MW_SUB_CLI_STATUS_CODE, ""));
        lMessageRequest.putValue(MiddlewareConstant.MW_SUB_CLI_STATUS_DESC, aRecord.getOrDefault(MiddlewareConstant.MW_SUB_CLI_STATUS_DESC, ""));
        lMessageRequest.putValue(MiddlewareConstant.MW_DN_CLI_STATUS_CODE, aRecord.getOrDefault(MiddlewareConstant.MW_DN_CLI_STATUS_CODE, ""));
        lMessageRequest.putValue(MiddlewareConstant.MW_DN_CLI_STATUS_DESC, aRecord.getOrDefault(MiddlewareConstant.MW_DN_CLI_STATUS_DESC, ""));
        lMessageRequest.putValue(MiddlewareConstant.MW_DELIVERY_HEADER, aRecord.getOrDefault(MiddlewareConstant.MW_DELIVERY_HEADER, ""));
        lMessageRequest.putValue(MiddlewareConstant.MW_DELIVERY_TIME, aRecord.getOrDefault(MiddlewareConstant.MW_DELIVERY_TIME, ""));

        return lMessageRequest;
    }

    static MessageRequest getDefaultMessageRequest() throws ItextosRuntimeException
    {
        return new MessageRequest(ClusterType.COMMON, InterfaceType.HTTP_JAPI, InterfaceGroup.API, MessageType.TRANSACTIONAL, MessagePriority.PRIORITY_5, RouteType.DOMESTIC);
    }

}