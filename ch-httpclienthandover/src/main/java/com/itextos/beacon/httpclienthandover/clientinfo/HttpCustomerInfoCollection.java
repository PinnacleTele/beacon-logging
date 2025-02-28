package com.itextos.beacon.httpclienthandover.clientinfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverData;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverHeaderParams;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverMaster;
import com.itextos.beacon.httpclienthandover.data.ClientHandoverParams;
import com.itextos.beacon.httpclienthandover.data.HttpMethod;
import com.itextos.beacon.httpclienthandover.data.ParamDataType;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class HttpCustomerInfoCollection
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                log                                     = LogFactory.getLog(HttpCustomerInfoCollection.class);
    private static final String             MASTER_QUERY                            = "select * from client_handover_config_detail";
    private static final String             PARAMS_QUERY                            = "select * from client_handover_config_body_params";
    private static final String             HEADER_PARAMS_QUERY                     = "select * from client_handover_config_header_params";

    private static final String             HANDOVER_ID                             = "handover_id";
    private static final String             HANDOVER_SEQ_NO                         = "handover_seq_no";

    // Handover Header Params
    private static final String             HEADER_PARAMS_SEQ_NO                    = "param_seq_no";
    private static final String             HEADER_PARAMS_NAME                      = "header_param_name";
    private static final String             HEADER_PARAMS_VALUE                     = "header_param_value";

    // Handover Params
    private static final String             PARAMS_SEQ_NO                           = "param_seq_no";
    private static final String             PARAMS_MW_CONSTANT_NAME                 = "mw_constant_name";
    private static final String             PARAMS_MW_ALT_CONST_NAME                = "mw_alternative_constant_name";
    private static final String             PARAMS_DEFAULT_VALUE                    = "default_value";
    private static final String             PARAMS_DATA_TYPE                        = "data_type";
    private static final String             PARAMS_DATA_FORMAT                      = "data_format";
    private static final String             PARAMS_DATA_VALIDATION                  = "data_validation";
    private static final String             PARAMS_DATA_DROOLS_VALIDATION_FILE_PATH = "drools_file_path";

    // Client Handover
    private static final String             CLIENT_ID                               = "cli_id";
    private static final String             RETRY_EXPIRY_LOGIC                      = "retry_expiry_logic";
    private static final String             BATCH_SIZE                              = "batch_size";
    private static final String             EXP_TIME_SECONDS                        = "expiry_time_seconds";
    private static final String             MAX_RETRY_COUNT                         = "max_retry_count";
    private static final String             RETRY_SLEEP_TIME_MS                     = "retry_sleep_time_millis";
    private static final String             LOG_RETRY_ATTEMPT                       = "log_retry_attempt";
    private static final String             THREAD_COUNT                            = "thread_count";

    // ClientHandover Master
    private static final String             PRIMARY_URL                             = "primary_url";
    private static final String             SECONDARY_URL                           = "secondary_url";
    private static final String             IS_SECURED                              = "is_secured";
    private static final String             CERTIFICATE_FILE_PATH                   = "certificate_file_path";
    private static final String             CERTIFICATE_PASS_PHARSE                 = "certificate_pass_pharse";
    private static final String             HTTP_METHOD                             = "http_method";
    private static final String             HANDOVER_TEMPLATE                       = "handover_template";
    private static final String             BODY_HEADER                             = "body_header";
    private static final String             BODY_FOOTER                             = "body_footer";
    private static final String             BATCH_BODY_DELIMITER                    = "batch_body_delimiter";
    private static final String             CON_WAIT_TIMEOUT_MILLS                  = "con_wait_timeout_millis";
    private static final String             READ_TIMEOUT_MILLS                      = "read_timeout_millis";

    private Map<String, ClientHandoverData> clientHandoverInfoMap                   = new HashMap<>();
    private boolean                         isFirstTime                             = true;

    public HttpCustomerInfoCollection(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public ClientHandoverData getClientHandoverInfo(
            String clientId)
    {
        return clientHandoverInfoMap.get(clientId);
    }

    @Override
    protected void processResultSet(
            ResultSet mResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        try
        {
            final Map<Long, List<ClientHandoverMaster>>         clientHandoverMasterMap = getClientHandoverMaster();
            final Map<String, List<ClientHandoverHeaderParams>> clientHandoverHeaderMap = getClientHandoverHeaderParams();
            final Map<String, List<ClientHandoverParams>>       clientHandoverparamsMap = getClientHandoverParams();
            final Map<String, ClientHandoverData>               tempClientHandoverMap   = new HashMap<>();

            while (mResultSet.next())
            {
                final long               handoverId        = mResultSet.getLong(HANDOVER_ID);
                final String             clientId          = mResultSet.getString(CLIENT_ID);
                final ClientHandoverData clientHandoverDTO = tempClientHandoverMap.computeIfAbsent(clientId, k -> createClientHandoverDTO(mResultSet));

                if (clientHandoverDTO != null) // In case of Exception
                {
                    final List<ClientHandoverMaster> clientHandoverMasterList = clientHandoverMasterMap.get(handoverId);

                    for (final ClientHandoverMaster clientHandoverMaster : clientHandoverMasterList)
                    {
                        final String                           combinedKey                = CommonUtility.combine(Long.toString(handoverId), Integer.toString(clientHandoverMaster.getSequenceNo()));

                        final List<ClientHandoverHeaderParams> clientHandoverHeaderParams = clientHandoverHeaderMap.get(combinedKey);
                        final List<ClientHandoverParams>       clientHandoverParams       = clientHandoverparamsMap.get(combinedKey);

                        clientHandoverMaster.setClientHandoverHeaderParams(clientHandoverHeaderParams);
                        clientHandoverMaster.setClientHandoverParams(clientHandoverParams);
                        clientHandoverDTO.addClientHandoverMaster(clientHandoverMaster);
                    }
                }
                else
                    log.error("There is a serious problem in getting the Client Handover data for Handover Id : '" + handoverId);
            }

            if (tempClientHandoverMap.size() > 0)
                clientHandoverInfoMap = tempClientHandoverMap;

            isFirstTime = false;
        }
        catch (final Exception e)
        {
            log.error("There is a serious problem in getting the Client Handover data", e);
        }
    }

    private static ClientHandoverMaster createClientHandoverMaster(
            ResultSet aResultSet)
            throws SQLException
    {
        return new ClientHandoverMaster(aResultSet.getLong(HANDOVER_ID), aResultSet.getInt(HANDOVER_SEQ_NO), CommonUtility.nullCheck(aResultSet.getString(PRIMARY_URL), true),
                CommonUtility.nullCheck(aResultSet.getString(SECONDARY_URL), true), CommonUtility.isTrue(CommonUtility.nullCheck(aResultSet.getString(IS_SECURED), true)),
                CommonUtility.nullCheck(aResultSet.getString(CERTIFICATE_FILE_PATH), true), CommonUtility.nullCheck(aResultSet.getString(CERTIFICATE_PASS_PHARSE), true),
                HttpMethod.getHttpMethod(aResultSet.getString(HTTP_METHOD)), CommonUtility.nullCheck(aResultSet.getString(HANDOVER_TEMPLATE), true),
                CommonUtility.nullCheck(aResultSet.getString(BODY_HEADER), true), CommonUtility.nullCheck(aResultSet.getString(BODY_FOOTER), true),
                CommonUtility.nullCheck(aResultSet.getString(BATCH_BODY_DELIMITER), true), aResultSet.getInt(CON_WAIT_TIMEOUT_MILLS), aResultSet.getInt(READ_TIMEOUT_MILLS));
    }

    private static ClientHandoverData createClientHandoverDTO(
            ResultSet aResultSet)
    {

        try
        {
            return new ClientHandoverData(aResultSet.getLong(HANDOVER_ID), aResultSet.getLong(CLIENT_ID), aResultSet.getInt(BATCH_SIZE), aResultSet.getInt(RETRY_EXPIRY_LOGIC),
                    aResultSet.getInt(EXP_TIME_SECONDS), aResultSet.getInt(MAX_RETRY_COUNT), aResultSet.getInt(RETRY_SLEEP_TIME_MS), 1 == aResultSet.getInt(LOG_RETRY_ATTEMPT),
                    aResultSet.getInt(THREAD_COUNT));
        }
        catch (final SQLException e)
        {
            log.error("Exception while getting the Client Handover data", e);
        }
        return null;
    }

    private Map<Long, List<ClientHandoverMaster>> getClientHandoverMaster()
            throws ItextosException
    {
        final Map<Long, List<ClientHandoverMaster>> tempClientHandoverMasterMap = new HashMap<>();
        ResultSet                                   resultSet                   = null;
    	Connection con =null;
    	PreparedStatement pstmt = null;
    
        try 
        {
        	 con = DBDataSourceFactory.getConnectionFromThin(mInmemoryInput.getJNDIInfo());
             pstmt = con.prepareStatement(MASTER_QUERY, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            resultSet = pstmt.executeQuery();

            while (resultSet.next())
            {
                final List<ClientHandoverMaster> tempClientHandoverMasterList = tempClientHandoverMasterMap.computeIfAbsent(resultSet.getLong(HANDOVER_ID), k -> new ArrayList<>());

                final ClientHandoverMaster       clientHandoverHeaderMaster   = createClientHandoverMaster(resultSet);
                tempClientHandoverMasterList.add(clientHandoverHeaderMaster);
            }
        }
        catch (final Exception e)
        {

            if (isFirstTime)
            {
                final String s = "Exception while loading data for Client Handover Header data";
                log.error(s, e);

                throw new ItextosException(s, e);
            }

            log.error("Ignorable Exception. Exception while loading data for Client Handover Header data", e);
        }
        finally
        {
            CommonUtility.closeResultSet(resultSet);
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
     
        }
        return tempClientHandoverMasterMap;
    }

    private Map<String, List<ClientHandoverHeaderParams>> getClientHandoverHeaderParams()
            throws ItextosException
    {
        final Map<String, List<ClientHandoverHeaderParams>> tempClientHandoverHeaderMap = new HashMap<>();

     	Connection con =null;
    	PreparedStatement pstmt = null;
    	ResultSet resultSet=null;
  
        try 
        {
        	
        	 con = DBDataSourceFactory.getConnectionFromThin(mInmemoryInput.getJNDIInfo());
             pstmt = con.prepareStatement(HEADER_PARAMS_QUERY, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            resultSet = pstmt.executeQuery();

            while (resultSet.next())
            {
                final long                             handoverId                   = resultSet.getLong(HANDOVER_ID);
                final int                              handoverSeqNo                = resultSet.getInt(HANDOVER_SEQ_NO);
                final String                           combinedKey                  = CommonUtility.combine(Long.toString(handoverId), Integer.toString(handoverSeqNo));
                final List<ClientHandoverHeaderParams> tempClientHandoverHeaderList = tempClientHandoverHeaderMap.computeIfAbsent(combinedKey, k -> new ArrayList<>());

                final ClientHandoverHeaderParams       clientHandoverHeaderParam    = new ClientHandoverHeaderParams(handoverId, handoverSeqNo, resultSet.getInt(HEADER_PARAMS_SEQ_NO),
                        CommonUtility.nullCheck(resultSet.getString(HEADER_PARAMS_NAME), true), CommonUtility.nullCheck(resultSet.getString(HEADER_PARAMS_VALUE), true));

                tempClientHandoverHeaderList.add(clientHandoverHeaderParam);
            }
        }
        catch (final Exception e)
        {

            if (isFirstTime)
            {
                final String s = "Exception while loading data for Client Handover Header data";
                log.error(s, e);
                throw new ItextosException(s, e);
            }

            log.error("Ignorable Exception. Exception while loading data for Client Handover Header data", e);
        }
        finally
        {
            CommonUtility.closeResultSet(resultSet);
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
   
        }
        return tempClientHandoverHeaderMap;
    }

    private Map<String, List<ClientHandoverParams>> getClientHandoverParams()
            throws ItextosException
    {
        final Map<String, List<ClientHandoverParams>> tempClientHandoverparamsMap = new HashMap<>();
        ResultSet                                     resultSet                   = null;
    	Connection con =null;
    	PreparedStatement pstmt = null;
    	
        try 
        {
        	  con = DBDataSourceFactory.getConnectionFromThin(mInmemoryInput.getJNDIInfo());
              pstmt = con.prepareStatement(PARAMS_QUERY, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            resultSet = pstmt.executeQuery();

            while (resultSet.next())
            {
                final long                       handoverId                   = resultSet.getLong(HANDOVER_ID);
                final int                        handoverSeqNo                = resultSet.getInt(HANDOVER_SEQ_NO);
                final String                     combinedKey                  = CommonUtility.combine(Long.toString(handoverId), Integer.toString(handoverSeqNo));
                final List<ClientHandoverParams> tempClientHandoverParamsList = tempClientHandoverparamsMap.computeIfAbsent(combinedKey, k -> new ArrayList<>());

                final ClientHandoverParams       clientHandoverParam          = new ClientHandoverParams(handoverId, handoverSeqNo, resultSet.getInt(PARAMS_SEQ_NO),
                        CommonUtility.nullCheck(resultSet.getString(PARAMS_MW_CONSTANT_NAME), true), CommonUtility.nullCheck(resultSet.getString(PARAMS_MW_ALT_CONST_NAME), true),
                        CommonUtility.nullCheck(resultSet.getString(PARAMS_DEFAULT_VALUE), false), ParamDataType.getParamDataType(resultSet.getString(PARAMS_DATA_TYPE)),
                        CommonUtility.nullCheck(resultSet.getString(PARAMS_DATA_FORMAT), true), CommonUtility.nullCheck(resultSet.getString(PARAMS_DATA_VALIDATION), true),
                        CommonUtility.nullCheck(resultSet.getString(PARAMS_DATA_DROOLS_VALIDATION_FILE_PATH), true));

                tempClientHandoverParamsList.add(clientHandoverParam);
            }
        }
        catch (final Exception e)
        {

            if (isFirstTime)
            {
                final String s = "Exception while loading data for Client Handover Param data";
                log.error(s, e);
                throw new ItextosException(s, e);
            }

            log.error("Ignorable Exception. Exception while loading data for Client Handover Param data", e);
        }
        finally
        {
            CommonUtility.closeResultSet(resultSet);
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
     
        }
        return tempClientHandoverparamsMap;
    }

}
