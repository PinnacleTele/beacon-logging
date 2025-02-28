package com.itextos.beacon.inmemory.interfaces.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.interfaces.bean.InterfaceResponse;
import com.itextos.beacon.inmemory.interfaces.bean.InterfaceResponseCodeMapping;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class GenericResponse
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log               log                = LogFactory.getLog(GenericResponse.class);

    private Map<String, InterfaceResponse> mClientResponseMap = new HashMap<>();

    public GenericResponse(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        // select cli_id, api_req_type, response_template, res_content_type,
        // date_time_format, intf_status_Code, cli_status_code, cli_reason,
        // cli_status_info, cli_http_status_code from api_custom_response acr,
        // api_custom_response_error_code_mapping acrecm WHERE acr.seq_no =
        // acrecm.parent_seq_no

        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, InterfaceResponse> lClientResponseMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String lClientId             = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lApiReqType           = CommonUtility.nullCheck(aResultSet.getString("api_req_type"), true);
            final String lResposeTemplate      = CommonUtility.nullCheck(aResultSet.getString("response_template"), true);
            final String lResponseContentType  = CommonUtility.nullCheck(aResultSet.getString("res_content_type"), true);
            final String lDateTimeFormat       = CommonUtility.nullCheck(aResultSet.getString("date_time_format"), true);
            final String lIntfStatusCode       = CommonUtility.nullCheck(aResultSet.getString("intf_status_Code"), true);
            final String lClientStatusCode     = CommonUtility.nullCheck(aResultSet.getString("cli_status_code"), true);
            final String lClientReason         = CommonUtility.nullCheck(aResultSet.getString("cli_reason"), true);
            final String lClientStatusInfo     = CommonUtility.nullCheck(aResultSet.getString("cli_status_info"), true);
            final String lClientHttpStatusCode = CommonUtility.nullCheck(aResultSet.getString("cli_http_status_code"), true);

            if (lClientId.isBlank() || (lApiReqType.isBlank()) || lResposeTemplate.isBlank() || lIntfStatusCode.isBlank() || lClientStatusCode.isBlank())
            {
                log.error("Unable to add the record to Client Specific Response. Client Id '" + lClientId + "' API Type '" + lApiReqType + "' Response Template '" + lResposeTemplate
                        + "' Response Content Type '" + lResponseContentType + "' DateTimeFormat '" + lDateTimeFormat + "' Intf Status Code '" + lIntfStatusCode + "' Client Status Code '"
                        + lClientStatusCode + "' Client Reason '" + lClientReason + "' Client Status Info '" + lClientStatusInfo + "' Client HTTP Status Code '" + lClientHttpStatusCode + "'");
                continue;
            }

            final String                       lKey                          = CommonUtility.combine(lClientId, lApiReqType);
            final InterfaceResponse            interfaceResponse             = lClientResponseMap.computeIfAbsent(lKey,
                    k -> new InterfaceResponse(lClientId, lApiReqType, lResposeTemplate, lResponseContentType, lDateTimeFormat));

            final InterfaceResponseCodeMapping lInterfaceResponseCodeMapping = new InterfaceResponseCodeMapping(lIntfStatusCode, lClientStatusCode, lClientReason, lClientStatusInfo,
                    lClientHttpStatusCode);
            interfaceResponse.addResponseCodeMapping(lInterfaceResponseCodeMapping);
        }

        if (!lClientResponseMap.isEmpty())
            mClientResponseMap = lClientResponseMap;
    }

    public InterfaceResponse getInterfaceResponse(
            String aClient,
            String aReqType)
    {
        final String cliId    = CommonUtility.nullCheck(aClient, true);
        final String lReqType = CommonUtility.nullCheck(aReqType, true);

        if (cliId.isBlank() || (lReqType.isBlank()))
            return null;

        final ItextosClient lClient            = new ItextosClient(aClient);
        String              key                = CommonUtility.combine(lClient.getClientId(), lReqType);
        InterfaceResponse   lInterfaceResponse = mClientResponseMap.get(key);

        if (lInterfaceResponse != null)
            return lInterfaceResponse;

        key                = CommonUtility.combine(lClient.getAdmin(), lReqType);
        lInterfaceResponse = mClientResponseMap.get(key);

        if (lInterfaceResponse != null)
            return lInterfaceResponse;

        key = CommonUtility.combine(lClient.getSuperAdmin(), lReqType);
        return mClientResponseMap.get(key);
    }

}
