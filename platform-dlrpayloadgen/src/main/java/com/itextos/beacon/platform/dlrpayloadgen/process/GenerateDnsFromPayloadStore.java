package com.itextos.beacon.platform.dlrpayloadgen.process;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.platform.dlrpayloadgen.util.DlrPayloadGenUtil;
import com.itextos.beacon.platform.dlrpayloadgen.util.PushToDlrProcessor;
import com.itextos.beacon.smslog.DNgenerationFromPayloadLog;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

public class GenerateDnsFromPayloadStore
        implements
        ITimedProcess
{

    private final String         mPayloadId;
    private final ClusterType    mCluster;
    private final TimedProcessor mTimedProcessor;
    private boolean              canContinue = true;

    private static Log           log         = LogFactory.getLog(GenerateDnsFromPayloadStore.class);

    public GenerateDnsFromPayloadStore(
            String aPayloadId,
            ClusterType aCluster)
    {
        super();

        
        mTimedProcessor = new TimedProcessor("GenerateDnsFromPayloadStore-" + aPayloadId, this, TimerIntervalConstant.DLR_PAYLOAD_GEN_REFRESH);
   
        this.mPayloadId = aPayloadId;
        this.mCluster   = aCluster;
        
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "GenerateDnsFromPayloadStore-" + aPayloadId);
    }

    private void doProcess()
    {
        if (log.isDebugEnabled())
            log.debug("payLoadId:" + mPayloadId + " thread started its job canContinue:" + canContinue);

        DNgenerationFromPayloadLog.log("payLoadId:" + mPayloadId + " thread started its job canContinue:" + canContinue);
        try
        {
            if (mPayloadId.equals("mysql"))
                generateMysqlDns();
            else
                generateDlrFromRedis();
        }
        catch (final Exception e)
        {
            log.error("payLoadId:" + mPayloadId + " exception", e);
        }
    }

    private void generateMysqlDns()
    {
        Connection        lSqlConn   = null;
        PreparedStatement lPStmt     = null;
        PreparedStatement lDelPstmt  = null;
        ResultSet         lResultSet = null;

        try
        {
            if (log.isDebugEnabled())
                log.debug("payLoadId: " + mPayloadId + " starting");

            int lDlrGenPayloadSize = 200;

            try
            {
                lDlrGenPayloadSize = CommonUtility.getInteger(DlrPayloadGenUtil.getAppConfigValueAsString(ConfigParamConstants.DLR_GEN_MYSQL_PAYLOAD_SIZE));
            }
            catch (final Exception ignore)
            {
                log.error(ignore);
            }
            final Date               lTimeToGenerateDlr = DlrPayloadGenUtil.getTimeToGenerateDn();
            final java.sql.Timestamp sqlTimestamp       = new java.sql.Timestamp(lTimeToGenerateDlr.getTime());

             
            DNgenerationFromPayloadLog.log("TimeToGenerateDlr: " + lTimeToGenerateDlr + "sqlTimestamp: " + sqlTimestamp);

            lSqlConn = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.PAYLOAD.getKey()));

            final String lSelectQuery = "select * from dn_payload_map WHERE cluster=? and payload_expiry<=now() and processed=? limit " + lDlrGenPayloadSize;
            final String lDeleteQuery = "delete from dn_payload_map where message_id=? and retry_attempt=?";

            lPStmt    = lSqlConn.prepareStatement(lSelectQuery);
            lDelPstmt = lSqlConn.prepareStatement(lDeleteQuery);

            final String cluster = mCluster.getKey();
            lPStmt.setString(1, ((cluster != null) && (cluster.trim().length() != 0)) ? cluster.trim().toLowerCase() : null);
            lPStmt.setInt(2, 0);

            lResultSet = lPStmt.executeQuery();
            List<String>              payloadDtLs = null;
            List<Map<String, String>> payloadMap  = null;

            while (lResultSet.next())
            {
                final Map<String, String> lTempMap       = new HashMap<>();
                final String              lMessageId     = lResultSet.getString("message_id");
                final int                 lRetryAttempt  = lResultSet.getInt("retry_attempt");
                final String              lPayloadExpiry = lResultSet.getString("payload_expiry");
                final Date                lDateParse     = DateTimeUtility.getDateFromString(lPayloadExpiry, DateTimeFormat.DEFAULT);
                final String              lPayloadJson   = lResultSet.getString("payload");
                final String              lPayloadDt     = "dnpayload-expire:" + DateTimeUtility.getFormattedDateTime(lDateParse, DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH);

                lDelPstmt.setString(1, lMessageId);
                lDelPstmt.setInt(2, lRetryAttempt);
                lDelPstmt.addBatch();

                if (payloadDtLs == null)
                {
                    payloadDtLs = new ArrayList<>();
                    payloadMap  = new ArrayList<>();
                }

                lTempMap.put("payloadDt", lPayloadDt);
                lTempMap.put("payloadJson", lPayloadJson);
                payloadMap.add(lTempMap);

                if (!payloadDtLs.contains(lPayloadDt))
                    payloadDtLs.add(lPayloadDt);
            }

            if (log.isDebugEnabled())
                log.debug("generateMysqlDns payloadDtLs: " + payloadDtLs);
            
            DNgenerationFromPayloadLog.log("generateMysqlDns payloadDtLs: " + payloadDtLs);

            if ((payloadDtLs != null) && (payloadMap != null))
            {
                final int[] executeBatch = lDelPstmt.executeBatch();

                if (log.isDebugEnabled())
                    log.debug("generateMysqlDns executeBatch result:" + executeBatch);

                final Iterator<Map<String, String>> iterator = payloadMap.iterator();

                while (iterator.hasNext())
                {
                    final Map<String, String> next = iterator.next();
                    PushToDlrProcessor.handoverToEngine(mPayloadId, next.get("payloadDt"), next.get("payloadJson"));
                }
                DlrPayloadGenUtil.updateInMemCountToDB(mPayloadId, payloadDtLs);
            }
        }
        catch (final Exception e)
        {
            log.error("generateMysqlDns exception", e);
        }
        finally
        {
            CommonUtility.closeResultSet(lResultSet);
            CommonUtility.closeStatement(lDelPstmt);
            CommonUtility.closeConnection(lSqlConn);
        }
    }

    private void generateDlrFromRedis()
    {
        Jedis lJedisCon = null;

        try
        {
            if (log.isDebugEnabled())
                log.debug("payLoadId: " + mPayloadId + " starting");

            lJedisCon = RedisConnectionProvider.getInstance().getConnection(mCluster, Component.DN_PAYLOAD, CommonUtility.getInteger(mPayloadId));

            final Set<String> lRedisKeys = lJedisCon.keys("dnpayload-expire:*");
            if (log.isDebugEnabled())
                log.debug("redis keys:" + lRedisKeys + " for the rid:" + mPayloadId);
            
            
            DNgenerationFromPayloadLog.log("redis keys:" + lRedisKeys + " for the rid:" + mPayloadId);

            for (final String redisKey : lRedisKeys)
                if (DlrPayloadGenUtil.isKeyToGenerateDNs(redisKey))
                {
                    if (log.isDebugEnabled())
                        log.debug("redis key: " + redisKey + " payLoadId: " + mPayloadId);
                    
                    DNgenerationFromPayloadLog.log("redis key: " + redisKey + " payLoadId: " + mPayloadId);


                    try
                    {
                        scanAndProcess(lJedisCon, redisKey);
                    }
                    catch (final Exception e)
                    {
                        log.error("Exception", e);
                    }
                    DlrPayloadGenUtil.updateInMemCountToDB(mPayloadId, redisKey);
                }
                else
                    if (log.isDebugEnabled())
                        log.debug("Not generating dn - ignored redis key:" + redisKey);
        }
        catch (final Exception e)
        {
            log.error("generateDns exception", e);
        }
        finally
        {

            try
            {
                if (lJedisCon != null)
                    lJedisCon.close();
            }
            catch (final Exception e)
            {}
        }
    }

    private void scanAndProcess(
            Jedis aJedisCon,
            String aRedisKey)
    {
        boolean          lToStop            = true;
        int              lDlrGenPayloadSize = 200;

        final ScanParams scanParams         = new ScanParams();

        try
        {
            lDlrGenPayloadSize = CommonUtility.getInteger(DlrPayloadGenUtil.getAppConfigValueAsString(ConfigParamConstants.DLR_GEN_PAYLOAD_SIZE));
        }
        catch (final Exception ignore)
        {
            log.error(ignore);
        }
        scanParams.count(lDlrGenPayloadSize);

        String cursor = ScanParams.SCAN_POINTER_START;

        while (lToStop && canContinue)
        {
            final ScanResult<Entry<String, String>> scanResult = aJedisCon.hscan(aRedisKey, String.valueOf(cursor), scanParams);
            final List<Entry<String, String>>       result     = scanResult.getResult();
            cursor = scanResult.getCursor();

            if (log.isDebugEnabled())
                log.debug("result: " + result.size() + " redisKey:" + aRedisKey + " cursor:" + cursor);

            if ((result.size() == 0) && cursor.equals("0"))
                lToStop = false;
            else
                for (final Entry<String, String> entry : result)
                    try
                    {
                        final String key   = entry.getKey();
                        final String value = entry.getValue();
                        aJedisCon.hdel(aRedisKey, key);
                        PushToDlrProcessor.handoverToEngine(mPayloadId, aRedisKey, value);
                    }
                    catch (final Exception exp)
                    {
                        exp.printStackTrace();
                    }
        }
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        doProcess();
        return false;
    }

    @Override
    public void stopMe()
    {
        canContinue = false;
    }

}
