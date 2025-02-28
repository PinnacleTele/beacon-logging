package com.itextos.beacon.platform.promokannelrediscleaner.process.redisprocess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class InsertAllRecordsProcess
        extends
        AbstractRedisProcess
{

    private static final Log    log                          = LogFactory.getLog(InsertAllRecordsProcess.class);
    private static final String INSERT_PROMO_KANNEL_DATA_LOG = "insert into promo_kannel_data_log(cli_id,msg_id,operator_msg_id,dest,header,intf_type,platform_cluster,recv_ts,route_id,pl_rds_id) values (?,?,?,?,?,?,?,?,?,?)";

    public InsertAllRecordsProcess(
            ClusterType aClusterType,
            int aRedisIndex)
    {
        super(aClusterType, aRedisIndex);
    }

    @Override
    public boolean process()
    {
        final List<String> keys = getRedisKeys();

        if (keys.isEmpty())
            return false;

        extractAndInsert(keys);

        deleteKeys(keys);

        return false;
    }

    private void extractAndInsert(
            List<String> aKeys)
    {
        final Map<String, DlrRedisData> lRedisInformation = getRedisInformation(aKeys);
        final List<UrlObject>           toInsert          = new ArrayList<>();

        for (final Entry<String, DlrRedisData> entry : lRedisInformation.entrySet())
        {
            final UrlObject lUrlObject = entry.getValue().getUrlObject();

            if (log.isDebugEnabled())
                log.debug("ULR Object : '" + lUrlObject + "'");

            if ((lUrlObject == null) || !lUrlObject.isValid())
            {
                log.error("Problem in retriving the AdditionalInfo object for the key '" + entry.getKey() + "'");
                continue;
            }

            if (canAdd(lUrlObject.getC_id()))
                toInsert.add(lUrlObject);
        }

        insertIntoDb(toInsert);
    }

    private static void insertIntoDb(
            List<UrlObject> aToInsert)
    {
        Connection        lSqlCon = null;
        PreparedStatement lPstmt  = null;

        if (log.isDebugEnabled())
            log.debug("Data size to insert into DB " + aToInsert.size());
        if (log.isDebugEnabled())
            log.debug("Record inserting table name : " + "promo_kannel_data_log");

        try
        {
            final String lSql = INSERT_PROMO_KANNEL_DATA_LOG;
            lSqlCon = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.LOGGING.getKey()));
            lPstmt  = lSqlCon.prepareStatement(lSql);
            lSqlCon.setAutoCommit(false);

            insertRecords(lPstmt, aToInsert);
            lSqlCon.commit();
        }
        catch (final Exception e)
        {
            log.error("Exception while inserting to table promo_kannel_data_log ", e);

            try
            {
                if (lSqlCon != null)
                    lSqlCon.rollback();
            }
            catch (final SQLException e1)
            {
                // Ignore
            }
        }
        finally
        {
            CommonUtility.closeStatement(lPstmt);
            CommonUtility.closeConnection(lSqlCon);
        }
    }

    private static void insertRecords(
            PreparedStatement aPstmt,
            List<UrlObject> aUrlObjectsList)
            throws SQLException
    {

        for (final UrlObject aUrlObject : aUrlObjectsList)
        {
            aPstmt.setString(1, aUrlObject.getC_id());
            aPstmt.setString(2, aUrlObject.getM_id());
            aPstmt.setString(3, aUrlObject.getOperatorMsgId());
            aPstmt.setString(4, aUrlObject.getDest());
            aPstmt.setString(5, aUrlObject.getHeader());
            aPstmt.setString(6, aUrlObject.getIntf_type());
            aPstmt.setString(7, aUrlObject.getPlatform_cluster());
            final Date parsedDate = DateTimeUtility.getDateFromString(aUrlObject.getRecv_ts(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
            aPstmt.setTimestamp(8, new java.sql.Timestamp(parsedDate.getTime()));
            aPstmt.setString(9, aUrlObject.getRute_id());
            aPstmt.setString(10, aUrlObject.getPl_rds_id());
            aPstmt.addBatch();
        }

        final int[] linsertCount = aPstmt.executeBatch();

        if (log.isDebugEnabled())
            log.debug("Record inserted to the table promo_kannel_data_log is " + linsertCount.length);
    }

    boolean canAdd(
            String aClientId)
    {
        return true;
    }

    private Map<String, DlrRedisData> getRedisInformation(
            List<String> aKeys)
    {
        Map<String, DlrRedisData> returnValue = new HashMap<>();

        try (
                Jedis jedis = getRedisConnection();
                Pipeline pipe = jedis.pipelined();)
        {
            final Map<String, Response<Map<String, String>>> allRedisEntries = new HashMap<>();

            for (final String key : aKeys)
            {
                final Response<Map<String, String>> lHgetAll = pipe.hgetAll(key);
                allRedisEntries.put(key, lHgetAll);
            }
            pipe.sync();

            returnValue = getDlrRedisObjects(allRedisEntries);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the DLR information from Redis.", e);
        }
        return returnValue;
    }

    private static Map<String, DlrRedisData> getDlrRedisObjects(
            Map<String, Response<Map<String, String>>> aAllRedisEntries)
    {
        final Map<String, DlrRedisData> returnValue = new HashMap<>();

        if ((aAllRedisEntries == null) || aAllRedisEntries.isEmpty())
            return returnValue;

        for (final Entry<String, Response<Map<String, String>>> entry : aAllRedisEntries.entrySet())
        {
            final Map<String, String> lMap = entry.getValue().get();
            if (lMap != null)
                returnValue.put(entry.getKey(), new DlrRedisData(lMap));
        }

        return returnValue;
    }

}