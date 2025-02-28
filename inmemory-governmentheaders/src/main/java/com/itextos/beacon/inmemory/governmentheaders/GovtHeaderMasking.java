package com.itextos.beacon.inmemory.governmentheaders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class GovtHeaderMasking
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log                 log                 = LogFactory.getLog(GovtHeaderMasking.class);

    private Map<String, Map<String, String>> mGovtMaskingHeaders = new HashMap<>();

    public GovtHeaderMasking(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public Map<String, String> getGovrMaskingDetails(
            String aKey)
    {
        return mGovtMaskingHeaders.get(aKey);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, Map<String, String>> loadMaskingHeaders = new HashMap<>();

        while (aResultSet.next())
        {
            final Map<String, String> lMaskHeaderIdInfo = new HashMap<>();

            String                    lClientId         = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            String                    lCircle           = CommonUtility.nullCheck(aResultSet.getString("circle"), true);
            String                    lOriginalHeader   = CommonUtility.nullCheck(aResultSet.getString("original_header"), true);
            String                    lMaskingHeader    = CommonUtility.nullCheck(aResultSet.getString("masking_header"), true);
            String                    lRouteId          = CommonUtility.nullCheck(aResultSet.getString("route_id"), true);
            final String              lRotateHeader     = CommonUtility.nullCheck(aResultSet.getString("rotate_header"), true);
            final String              lEntityId         = CommonUtility.nullCheck(aResultSet.getString("entity_id"), true);

            if ("".equals(lMaskingHeader) && "".equals(lRouteId))
                continue;

            lClientId       = ("".equals(lClientId) ? Constants.NULL_STRING : lClientId);
            lCircle         = ("".equals(lCircle) ? Constants.NULL_STRING : lCircle.toUpperCase());
            lOriginalHeader = ("".equals(lOriginalHeader) ? Constants.NULL_STRING : lOriginalHeader.toUpperCase());
            lMaskingHeader  = ("".equals(lMaskingHeader) ? lMaskingHeader : lMaskingHeader.toUpperCase());
            lRouteId        = ("".equals(lRouteId) ? lRouteId : lRouteId.toUpperCase());

            lMaskHeaderIdInfo.put("masked_header", lMaskingHeader);
            lMaskHeaderIdInfo.put("route_id", lRouteId);
            lMaskHeaderIdInfo.put("entity_id", lEntityId);
            lMaskHeaderIdInfo.put("rotate_header", lRotateHeader);
            loadMaskingHeaders.put(CommonUtility.combine(lClientId, lCircle, lOriginalHeader), lMaskHeaderIdInfo);
        }

        if (!loadMaskingHeaders.isEmpty())
            mGovtMaskingHeaders = loadMaskingHeaders;
    }

}
