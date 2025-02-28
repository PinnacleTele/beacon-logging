package com.itextos.beacon.inmemory.dlrquery;

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
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.dlrquery.objects.DlrBodyParam;
import com.itextos.beacon.inmemory.dlrquery.objects.DlrConfigDetail;
import com.itextos.beacon.inmemory.dlrquery.objects.DlrDataType;
import com.itextos.beacon.inmemory.dlrquery.objects.DlrHeaderParam;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class InmemDlrQuery
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                        log                                            = LogFactory.getLog(InmemDlrQuery.class);

    private static final String                     HEADER_PARAM                                   = "select dchp.response_header_id, dchp.header_param_name, dchp.header_param_value from client_handover.dlrquery_config_header_params dchp where dchp.is_active = '1' order by dchp.param_seq_no";
    private static final String                     CONFIG_DETAIL                                  = "select dcd.response_header_id, dcd.handover_template, dcd.body_header, dcd.body_footer, dcd.batch_body_delimiter from client_handover.dlrquery_config_detail dcd";
    private static final String                     CONFIG_BODY_PARAM                              = "select dcbp.response_header_id, dcbp.param_seq_no, dcbp.mw_constant_name, dcbp.mw_alternative_constant_name, dcbp.default_value, dcbp.data_type, dcbp.data_format, dcbp.data_validation, dcbp.drools_file_path from client_handover.dlrquery_config_body_params dcbp order by param_seq_no ";

    private static final int                        COL_INDEX_CLI_ID                               = 1;
    private static final int                        COL_INDEX_RESPONSE_HEADER_ID                   = 2;

    private static final int                        COL_INDEX_HDR_PARAM_RESPONSE_HDR_ID            = 1;
    private static final int                        COL_INDEX_HDR_PARAM_PARAM_NAME                 = 2;
    private static final int                        COL_INDEX_HDR_PARAM_PARAM_VALUE                = 3;

    private static final int                        COL_INDEX_CONFIG_DETAIL_RESPONSE_HDR_ID        = 1;
    private static final int                        COL_INDEX_CONFIG_DETAIL_HANDOVER_TEMPLATE      = 2;
    private static final int                        COL_INDEX_CONFIG_DETAIL_BODY_HEADER            = 3;
    private static final int                        COL_INDEX_CONFIG_DETAIL_BODY_FOOTER            = 4;
    private static final int                        COL_INDEX_CONFIG_DETAIL_BODY_DELIMITER         = 5;

    private static final int                        COL_INDEX_BODY_PARAM_RESPONSE_HDR_ID           = 1;
    private static final int                        COL_INDEX_BODY_PARAM_SEQ_NO                    = 2;
    private static final int                        COL_INDEX_BODY_PARAM_CONSTANT_NAME             = 3;
    private static final int                        COL_INDEX_BODY_PARAM_ALTERNATIVE_CONSTANT_NAME = 4;
    private static final int                        COL_INDEX_BODY_PARAM_DEFAULT_VALUE             = 5;
    private static final int                        COL_INDEX_BODY_PARAM_DATA_TYPE                 = 6;
    private static final int                        COL_INDEX_BODY_PARAM_DATA_FORMAT               = 7;
    private static final int                        COL_INDEX_BODY_PARAM_DATA_VALIDATION           = 8;
    private static final int                        COL_INDEX_BODY_PARAM_DROOLS_FILE_PATH          = 9;

    private Map<String, String>                     mClientResponseMap                             = new HashMap<>();
    private Map<String, List<DlrHeaderParam>>       mResponseHeaderParams                          = new HashMap<>();
    private Map<String, DlrConfigDetail>            mConfigDetail                                  = new HashMap<>();
    private Map<String, Map<Integer, DlrBodyParam>> mBodyParam                                     = new HashMap<>();

    public InmemDlrQuery(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    @Override
    protected void processResultSet(
            ResultSet aMResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, String> localClientResponseMap = new HashMap<>();

        while (aMResultSet.next())
            localClientResponseMap.put(CommonUtility.nullCheck(aMResultSet.getString(COL_INDEX_CLI_ID), true), CommonUtility.nullCheck(aMResultSet.getString(COL_INDEX_RESPONSE_HEADER_ID), true));

        if (!localClientResponseMap.isEmpty())
            mClientResponseMap = localClientResponseMap;

        loadOtherData();
    }

    private void loadOtherData()
            throws SQLException
    {
        loadHeaderParams();
        loadDlrConfigDetails();
        loadDlrBodyParamDetails();
    }

    private void loadDlrBodyParamDetails()
            throws SQLException
    {
        final Map<String, Map<Integer, DlrBodyParam>> localBodyParam = new HashMap<>();

        JndiInfoHolder.getInstance();
        
      	Connection con =null;
    	PreparedStatement pstmt = null;
    	ResultSet rs=null;
   

        try
        {

        	 con = DBDataSourceFactory.getConnectionFromThin(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.CLIENT_HANDOVER.getKey()));
              pstmt = con.prepareStatement(CONFIG_BODY_PARAM);
             rs = pstmt.executeQuery();
            while (rs.next())
            {
                final Map<Integer, DlrBodyParam> map          = localBodyParam.computeIfAbsent(CommonUtility.nullCheck(rs.getString(COL_INDEX_BODY_PARAM_RESPONSE_HDR_ID), true), k -> new HashMap<>());

                final int                        seqNo        = rs.getInt(COL_INDEX_BODY_PARAM_SEQ_NO);
                final DlrDataType                dlrDataType  = getDlrDataType(rs.getInt(COL_INDEX_BODY_PARAM_DATA_TYPE));

                final DlrBodyParam               dlrBodyParam = new DlrBodyParam(seqNo, CommonUtility.nullCheck(rs.getString(COL_INDEX_BODY_PARAM_CONSTANT_NAME), true),
                        CommonUtility.nullCheck(rs.getString(COL_INDEX_BODY_PARAM_ALTERNATIVE_CONSTANT_NAME), true), CommonUtility.nullCheck(rs.getString(COL_INDEX_BODY_PARAM_DEFAULT_VALUE), true),
                        dlrDataType, CommonUtility.nullCheck(rs.getString(COL_INDEX_BODY_PARAM_DATA_FORMAT), true), CommonUtility.nullCheck(rs.getString(COL_INDEX_BODY_PARAM_DATA_VALIDATION), true),
                        CommonUtility.nullCheck(rs.getString(COL_INDEX_BODY_PARAM_DROOLS_FILE_PATH), true));

                map.put(seqNo, dlrBodyParam);
            }
        }
        catch (final SQLException e)
        {
            log.error("Exception while getting the DLR header param details", e);
            throw e;
        }
        catch (final Exception e1)
        {
            log.error("Exception while getting the DLR header param details", e1);
        }finally {
            CommonUtility.closeResultSet(rs);
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
        }
        if (!localBodyParam.isEmpty())
            mBodyParam = localBodyParam;
    }

    private static DlrDataType getDlrDataType(
            int aInt)
    {

        switch (aInt)
        {
            case 1:
                return DlrDataType.STRING;

            case 2:
                return DlrDataType.NUMBER;

            case 3:
                return DlrDataType.DATE_TIME;

            default:
                return DlrDataType.STRING;
        }
    }

    private void loadDlrConfigDetails()
            throws SQLException
    {
        final Map<String, DlrConfigDetail> localConfigDetail = new HashMap<>();

        Connection con =null;
    	PreparedStatement pstmt = null;
    	ResultSet rs=null;
       
        try 
        {
             con = DBDataSourceFactory.getConnectionFromThin(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.CLIENT_HANDOVER.getKey()));
             pstmt = con.prepareStatement(CONFIG_DETAIL);
             rs = pstmt.executeQuery();
            while (rs.next())
                localConfigDetail.put(CommonUtility.nullCheck(rs.getString(COL_INDEX_CONFIG_DETAIL_RESPONSE_HDR_ID), true),
                        new DlrConfigDetail(CommonUtility.nullCheck(rs.getString(COL_INDEX_CONFIG_DETAIL_HANDOVER_TEMPLATE), true),
                                CommonUtility.nullCheck(rs.getString(COL_INDEX_CONFIG_DETAIL_BODY_HEADER), true), CommonUtility.nullCheck(rs.getString(COL_INDEX_CONFIG_DETAIL_BODY_FOOTER), true),
                                CommonUtility.nullCheck(rs.getString(COL_INDEX_CONFIG_DETAIL_BODY_DELIMITER), true)));
        }
        catch (final SQLException e)
        {
            log.error("Exception while getting the DLR header param details", e);
            throw e;
        }
        catch (final Exception e1)
        {
            log.error("Exception while getting the DLR header param details", e1);
        }finally {
            CommonUtility.closeResultSet(rs);
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
     
        }
        if (!localConfigDetail.isEmpty())
            mConfigDetail = localConfigDetail;
    }

    private void loadHeaderParams()
            throws SQLException
    {
        final Map<String, List<DlrHeaderParam>> localResponseHeaderParams = new HashMap<>();

        Connection con =null;
    	PreparedStatement pstmt = null;
    	ResultSet rs=null;
   
        try 
        {
        	 con = DBDataSourceFactory.getConnectionFromThin(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.CLIENT_HANDOVER.getKey()));
             pstmt = con.prepareStatement(HEADER_PARAM);
             rs = pstmt.executeQuery();

            while (rs.next())
            {
                final List<DlrHeaderParam> list = localResponseHeaderParams.computeIfAbsent(CommonUtility.nullCheck(rs.getString(COL_INDEX_HDR_PARAM_RESPONSE_HDR_ID), true), k -> new ArrayList<>());
                list.add(new DlrHeaderParam(CommonUtility.nullCheck(rs.getString(COL_INDEX_HDR_PARAM_PARAM_NAME), true), CommonUtility.nullCheck(rs.getString(COL_INDEX_HDR_PARAM_PARAM_VALUE), true)));
            }
        }
        catch (final SQLException e)
        {
            log.error("Exception while getting the DLR header param details", e);
            throw e;
        }
        catch (final Exception e1)
        {
            log.error("Exception while getting the DLR header param details", e1);
        }finally {
            CommonUtility.closeResultSet(rs);
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
     
        }
        if (!localResponseHeaderParams.isEmpty())
            mResponseHeaderParams = localResponseHeaderParams;
    }

    public String getResponseHeaderId(
            String aClientId)
    {
        final ItextosClient itextosClient    = new ItextosClient(aClientId);
        String              responseHeaderId = mClientResponseMap.get(itextosClient.getClientId());

        if (responseHeaderId != null)
            return responseHeaderId;

        responseHeaderId = mClientResponseMap.get(itextosClient.getAdmin());

        if (responseHeaderId != null)
            return responseHeaderId;

        return mClientResponseMap.get(itextosClient.getSuperAdmin());
    }

    public List<DlrHeaderParam> getDlrHeaderParam(
            String aResponseHeaderId)
    {
        return mResponseHeaderParams.computeIfAbsent(aResponseHeaderId, k -> new ArrayList<>());
    }

    public DlrConfigDetail getDlrConfigDetail(
            String aResponseHeaderId)
    {
        return mConfigDetail.get(aResponseHeaderId);
    }

    public Map<Integer, DlrBodyParam> getDlrConfigBodyParam(
            String aResponseHeaderId)
    {
        return mBodyParam.computeIfAbsent(aResponseHeaderId, k -> new HashMap<>());
    }

}