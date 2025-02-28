package com.itextos.beacon.http.clouddataprocessor;

import org.apache.commons.lang.StringUtils;

import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.http.clouddatautil.common.RequestType;

public class IncomingMessageParser
        extends
        MessageParserConstants
{

    private final String   mIncomingString;
    private DataToPlatform mDataToPlatformRequest;

    public IncomingMessageParser(
            String aString) throws ItextosRuntimeException
    {
        mIncomingString = aString;
        parse();
    }

    private void parse() throws ItextosRuntimeException
    {
        final String[] lSplit = StringUtils.split(mIncomingString, CONCATE_STRING);

        if (lSplit.length != EXPECTED_LENGTH)
            throw new ItextosRuntimeException("Invalid incoming message string. '" + mIncomingString + "'");

        final String                 fileId                 = lSplit[POS_FILE_ID];
        final String                 clientId               = lSplit[POS_CLIENT_ID];
        final String                 clientIp               = lSplit[POS_CLIENT_IP];
        final String                 receivedTime           = lSplit[POS_RECEIVED_TIME];
        final String                 reqType                = lSplit[POS_REQUEST_TYPE];
        final String                 actualReq              = lSplit[POS_ACTUAL_REQUEST];

        // final String actualReq = StringUtils.split(fileIdString, CONCATE_VALUES)[1];
        // final String clientId = StringUtils.split(clientIdString, CONCATE_VALUES)[1];
        // final String clientIp = StringUtils.split(clientIpString, CONCATE_VALUES)[1];
        // final String receivedTime = StringUtils.split(receivedTimeString,
        // CONCATE_VALUES)[1];
        // final String reqType = StringUtils.split(reqTypeString, CONCATE_VALUES)[1];
        // final String actualReq = StringUtils.split(actualReqString,
        // CONCATE_VALUES)[1];

        final RequestType            lRequestType           = RequestType.getRequestType(reqType);
        final IDataToPlatformRequest lDataToPlatformRequest = getDataToPlatform(lRequestType, actualReq);

        if (lDataToPlatformRequest != null)
            mDataToPlatformRequest = new DataToPlatform(fileId, clientId, clientIp, receivedTime, lRequestType, lDataToPlatformRequest);
    }

    private static IDataToPlatformRequest getDataToPlatform(
            RequestType aRequestType,
            String aActualReq)
    {
        IDataToPlatformRequest lDataToPlatformRequest = null;

        switch (aRequestType)
        {
            case JSON:
                lDataToPlatformRequest = new JsonDataToPlatformRequest(aActualReq);

                break;

            case QS:
                lDataToPlatformRequest = getQsDataToPlatform(aActualReq);
                break;

            default:
                break;
        }
        return lDataToPlatformRequest;
    }

    private static IDataToPlatformRequest getQsDataToPlatform(
            String aActualReq)
    {
        final String[]                allQueryParams         = StringUtils.split(aActualReq, "&");
        final QsDataToPlatformRequest lDataToPlatformRequest = new QsDataToPlatformRequest();

        for (final String s : allQueryParams)
        {
            final String[] lSplit = s.split(CONCATE_VALUES);

            switch (lSplit[0])
            {
                case APP_COUNTRY:
                    lDataToPlatformRequest.setAppendCountry(lSplit[1]);
                    break;

                case COUNTRY_CD:
                    lDataToPlatformRequest.setAppendCountryCode(lSplit[1]);
                    break;

                case CUST_REF:
                    lDataToPlatformRequest.setCustomerReference(lSplit[1]);
                    break;

                case DCS:
                    lDataToPlatformRequest.setDcs(lSplit[1]);
                    break;

                case DEST:
                    lDataToPlatformRequest.setDestination(lSplit[1]);
                    break;

                case DLR_REQ:
                    lDataToPlatformRequest.setDlrRequired(lSplit[1]);
                    break;

                case DLT_ENTITY_ID:
                    lDataToPlatformRequest.setDltEntityId(lSplit[1]);
                    break;

                case DLT_TEMPLATE_ID:
                    lDataToPlatformRequest.setDltTemplateId(lSplit[1]);
                    break;

                case EXPIRY:
                    lDataToPlatformRequest.setExpiry(lSplit[1]);
                    break;

                case HEADER:
                    lDataToPlatformRequest.setHeader(lSplit[1]);
                    break;

                case MAX_SPLIT:
                    lDataToPlatformRequest.setMaxSplit(lSplit[1]);
                    break;

                case MSG:
                    lDataToPlatformRequest.setMessage(lSplit[1]);
                    break;

                case MSG_TAG:
                    lDataToPlatformRequest.setMessageTag(lSplit[1]);
                    break;

                case PARAM1:
                    lDataToPlatformRequest.setParam1(lSplit[1]);
                    break;

                case PARAM10:
                    lDataToPlatformRequest.setParam10(lSplit[1]);
                    break;

                case PARAM2:
                    lDataToPlatformRequest.setParam2(lSplit[1]);
                    break;

                case PARAM3:
                    lDataToPlatformRequest.setParam3(lSplit[1]);
                    break;

                case PARAM4:
                    lDataToPlatformRequest.setParam4(lSplit[1]);
                    break;

                case PARAM5:
                    lDataToPlatformRequest.setParam5(lSplit[1]);
                    break;

                case PARAM6:
                    lDataToPlatformRequest.setParam6(lSplit[1]);
                    break;

                case PARAM7:
                    lDataToPlatformRequest.setParam7(lSplit[1]);
                    break;

                case PARAM8:
                    lDataToPlatformRequest.setParam8(lSplit[1]);
                    break;

                case PARAM9:
                    lDataToPlatformRequest.setParam9(lSplit[1]);
                    break;

                case PORT:
                    lDataToPlatformRequest.setPort(lSplit[1]);
                    break;

                case SCHEDULE_TIME:
                    lDataToPlatformRequest.setScheduleTime(lSplit[1]);
                    break;

                case TEMPLATE_ID:
                    lDataToPlatformRequest.setTemplateId(lSplit[1]);
                    break;

                case TEMPLATE_VALUES:
                    lDataToPlatformRequest.setTemplateValues(lSplit[1]);
                    break;

                case TYPE:
                    lDataToPlatformRequest.setMessageType(lSplit[1]);
                    break;

                case UDH:
                    lDataToPlatformRequest.setUdh(lSplit[1]);
                    break;

                case UDHI:
                    lDataToPlatformRequest.setUdhi(lSplit[1]);
                    break;

                case URLTRACK:
                    lDataToPlatformRequest.setUrltrack(lSplit[1]);
                    break;

                default:
                    break;
            }
        }

        return lDataToPlatformRequest;
    }

    public String getJsonObject()
    {
        if (mDataToPlatformRequest != null)
            return mDataToPlatformRequest.getJsonObject().toString();
        return null;
    }

}