package com.itextos.beacon.platform.promokannelrediscleaner.process.redisprocess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.ClusterType;

public class InsertClientSpecificRecords
        extends
        InsertAllRecordsProcess
{

    private static final Log    log       = LogFactory.getLog(InsertClientSpecificRecords.class);
    private static final String SQL       = "select cli_id from promo_kannel_dlr_entry_req_clients where is_active=1";

    private List<String>        mClientId = new ArrayList<>();

    public InsertClientSpecificRecords(
            ClusterType aClusterType,
            int aRedisIndex)
    {
        super(aClusterType, aRedisIndex);
    }

    @Override
    public boolean process()
    {
        mClientId = getClientIds();
        return super.process();
    }

    @Override
    boolean canAdd(
            String aClientId)
    {
        return mClientId.contains(aClientId);
    }

    private static List<String> getClientIds()
    {
        final List<String> returnValue = new ArrayList<>();

        try (
                Connection con = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.ACCOUNTS.getKey()));
                PreparedStatement pstmt = con.prepareStatement(SQL);
                ResultSet rs = pstmt.executeQuery();)
        {
            while (rs.next())
                returnValue.add(rs.getString(1));
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the client id for the promo redis deletion", e);
        }
        return returnValue;
    }

}