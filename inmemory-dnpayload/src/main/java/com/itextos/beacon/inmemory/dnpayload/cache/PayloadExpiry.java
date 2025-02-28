package com.itextos.beacon.inmemory.dnpayload.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.dnpayload.util.DNPUtil;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class PayloadExpiry
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log    log                  = LogFactory.getLog(PayloadExpiry.class);
    private static final String DEFAULT_EXPIRY_HOURS = "6";

    private Map<String, String> mPayloadExpiry       = new HashMap<>();

    public PayloadExpiry(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public int getExpiry(
            String lClientId)
    {
        final ItextosClient lClient        = new ItextosClient(lClientId);
        String              lPayloadExpiry = mPayloadExpiry.get(lClient.getClientId());

        if (lPayloadExpiry == null)
        {
            lPayloadExpiry = mPayloadExpiry.get(lClient.getAdmin());

            if (lPayloadExpiry == null)
            {
                lPayloadExpiry = mPayloadExpiry.get(lClient.getSuperAdmin());

                if (lPayloadExpiry == null)
                {
                    lPayloadExpiry = DNPUtil.getAppConfigValueAsString(ConfigParamConstants.DEFAULT_PAYLOAD_EXPIRY_IN_HR);

                    if (lPayloadExpiry == null)
                        lPayloadExpiry = DEFAULT_EXPIRY_HOURS;
                }
            }
        }
        return CommonUtility.getInteger(lPayloadExpiry, 6) + 1;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from acc_ignore_from_dn_generation where is_active=1 and
        // ignore_or_generate =1

        final Map<String, String> lResult = new HashMap<>();

        while (aResultSet.next())
            lResult.put(aResultSet.getString("cli_id"), aResultSet.getString("payload_expiry_in_hr"));

        if (!lResult.isEmpty())
            mPayloadExpiry = lResult;
    }

}