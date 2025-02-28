package com.itextos.beacon.inmemory.errorinfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.errorinfo.data.CarrierErrorInfo;
import com.itextos.beacon.inmemory.errorinfo.data.CarrierRouteMap;
import com.itextos.beacon.inmemory.errorinfo.data.FailureType;
import com.itextos.beacon.inmemory.errorinfo.db.CarrierErrorInfoInmemReaper;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class CarrierErrorInfoCollection
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log              log                         = LogFactory.getLog(CarrierErrorInfoCollection.class);
    private static final String           DEFAULT_ERROR_STATUS        = "FAILED";
    private static final String           DEFAULT_OPERATOR_ERROR_CODE = "699";
    private static final String           SQL_CARRIER_ROUTE_MAP       = "select" + //
            " crm.route_id, cm.carrier_id, cm.carrier_name, cm.circle_wise_error_code"//
            + " from "//
            + " carrier_master cm ," //
            + " carrier_route_map crm" //
            + " where" //
            + " crm.carrier_id = cm.carrier_id ";

    private Map<String, CarrierErrorInfo> mErrorCodesMap              = new ConcurrentHashMap<>();
    private Map<String, CarrierRouteMap>  mCarrierRouteMap            = new ConcurrentHashMap<>();

    public CarrierErrorInfoCollection(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    private CarrierErrorInfo getDefaultCarrierErrorInfo(
            String aCarrierRouteMap,
            String aErrorCode,
            String aErrorStatus)
    {
        String errorStatus = CommonUtility.nullCheck(aErrorStatus, true);
        errorStatus = errorStatus.isEmpty() ? DEFAULT_ERROR_STATUS : errorStatus;

        final CarrierErrorInfo lCarrierErrorInfo = new CarrierErrorInfo(aCarrierRouteMap, aErrorCode, aErrorStatus, errorStatus, FailureType.PERMANENT.getKey(), DEFAULT_OPERATOR_ERROR_CODE);

        /**
         * No need to add an entry into mCarrierRouteMap.
         * mCarrierRouteMap will not have any records unless there is an problem in
         * loading the data into memory.
         */

        // Add it for the current collection.
        mErrorCodesMap.put(lCarrierErrorInfo.getKey(), lCarrierErrorInfo);

        // Add it for DB insert.
        CarrierErrorInfoInmemReaper.getInstance().addErrorInfo(lCarrierErrorInfo);

        return lCarrierErrorInfo;
    }

    protected CarrierErrorInfo getErrorInfo(
            String aRouteId,
            String aErrorCode,
            String aErrorStatus)
    {
        final CarrierRouteMap lCarrierRouteMap = mCarrierRouteMap.get(CommonUtility.nullCheck(aRouteId, true).toUpperCase());

        if (lCarrierRouteMap == null)
        {
            log.error(new ItextosRuntimeException("Invalid router id passed. Router id '" + aRouteId + "'"));

            // Check for the previous same error code.
            final String           key               = CommonUtility.combine(aRouteId, aErrorCode);
            final CarrierErrorInfo lCarrierErrorInfo = mErrorCodesMap.get(key);

            if (lCarrierErrorInfo != null)
                return lCarrierErrorInfo;

            return getDefaultCarrierErrorInfo(aRouteId, aErrorCode, aErrorStatus);
        }

        String errorCodeKey = lCarrierRouteMap.getRouteId();
        if (!lCarrierRouteMap.isCircleBasedErrorCode())
            errorCodeKey = lCarrierRouteMap.getCarrierId();

        final String     key               = CommonUtility.combine(errorCodeKey, aErrorCode);
        CarrierErrorInfo lCarrierErrorInfo = mErrorCodesMap.get(key);

        if (lCarrierErrorInfo == null)
            lCarrierErrorInfo = getDefaultCarrierErrorInfo(errorCodeKey, aErrorCode, aErrorStatus);

        return lCarrierErrorInfo;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        final Map<String, CarrierErrorInfo> lTempErrorCodeMap = new HashMap<>();

        while (aResultSet.next())
        {
            final CarrierErrorInfo lErrorCode = new CarrierErrorInfo(CommonUtility.nullCheck(aResultSet.getString("route_id"), true).toUpperCase(),
                    CommonUtility.nullCheck(aResultSet.getString("error_code"), true), CommonUtility.nullCheck(aResultSet.getString("error_desc"), true),
                    CommonUtility.nullCheck(aResultSet.getString("error_status"), true), CommonUtility.nullCheck(aResultSet.getString("failure_type"), true),
                    CommonUtility.nullCheck(aResultSet.getString("platform_error_code"), true));

            lTempErrorCodeMap.put(lErrorCode.getKey(), lErrorCode);
        }

        getCarrierRouteMap();

        if (!lTempErrorCodeMap.isEmpty())
            mErrorCodesMap = lTempErrorCodeMap;
    }

    private void getCarrierRouteMap()
    {
        final Map<String, CarrierRouteMap> tempCarrierRouteMap = new ConcurrentHashMap<>();

        ResultSet                 rs             = null;
     	Connection con =null;
    	PreparedStatement pstmt = null;
   
        try 
        {
        	  con = DBDataSourceFactory.getConnectionFromThin(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.CARRIER_HANDOVER.getKey()));
              pstmt = con.prepareStatement(SQL_CARRIER_ROUTE_MAP);
              rs = pstmt.executeQuery();

            while (rs.next())
            {
                final CarrierRouteMap crm = new CarrierRouteMap(CommonUtility.nullCheck(rs.getString("route_id"), true).toUpperCase(),
                        CommonUtility.nullCheck(rs.getString("carrier_id"), true).toUpperCase(), CommonUtility.nullCheck(rs.getString("carrier_name"), true).toUpperCase(),
                        CommonUtility.isEnabled(CommonUtility.nullCheck(rs.getString("circle_wise_error_code"), true)));
                tempCarrierRouteMap.put(crm.getRouteId(), crm);
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while loading the carrier and route map information.", e);
        }finally
        {
            CommonUtility.closeResultSet(rs);
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
   
        }

        if (!tempCarrierRouteMap.isEmpty())
            mCarrierRouteMap = tempCarrierRouteMap;
    }

    public static void main(
            String[] args)
    {
        final CarrierErrorInfoCollection lCarrierErrorInfoCollection = (CarrierErrorInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CARRIER_ERROR_INFO);
        final CarrierErrorInfo           lErrorInfo                  = lCarrierErrorInfoCollection.getErrorInfo("ABCD", "210", "FAILED");
        System.out.println(lErrorInfo);
    }

}