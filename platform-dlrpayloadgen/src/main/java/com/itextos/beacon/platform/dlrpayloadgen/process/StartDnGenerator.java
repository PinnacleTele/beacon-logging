package com.itextos.beacon.platform.dlrpayloadgen.process;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.platform.dlrpayloadgen.util.DlrPayloadGenUtil;

public class StartDnGenerator
{

    private static final Log log = LogFactory.getLog(StartDnGenerator.class);

    private StartDnGenerator()
    {}

    public static List<GenerateDnsFromPayloadStore> mDlrGenThreadLs = new ArrayList<>();

    public static void startDlrGenerator(
            ClusterType aCluster)
    {
        final List<String> lPayloadRedisInfo = DlrPayloadGenUtil.getPlayloadRedisInfo(aCluster);

        if (log.isDebugEnabled())
            log.debug("PayloadRedisInfo:" + lPayloadRedisInfo);

        boolean isDone            = true;
        boolean lDlrPayloadStatus = false;

        for (final String lPayloadId : lPayloadRedisInfo)
        {
            lDlrPayloadStatus = false;
            isDone            = true;

            while (isDone)
                try
                {
                    lDlrPayloadStatus = DlrPayloadGenUtil.getDlrPayloadStatus("dnexpire_cluster:" + aCluster.getKey().toLowerCase() + ":rid:" + lPayloadId);
                    isDone            = false;
                }
                catch (final Exception e)
                {

                    try
                    {
                        Thread.sleep(5000);
                    }
                    catch (final InterruptedException e1)
                    {
                        e1.printStackTrace();
                    }
                }
            System.out.println("dnPayloadStatus: " + lDlrPayloadStatus + " payloadId:" + "dnexpire_cluster:" + aCluster.getKey().toLowerCase() + ":rid:" + lPayloadId);

            if (lDlrPayloadStatus)
            {
                System.err.println("Going to shoutdown due to payloadid: " + "dnexpire_cluster:" + aCluster.getKey().toLowerCase() + ":rid:" + lPayloadId
                        + " already started in redis as per key \"kannel:dngeneratepayloadid\" in kannelredis and check the values configured in "
                        + "redis_config table - dnpayload ids can't be given in other instances only one instance should be started with one payloadid"
                        + " The server may not stop properly use kill -9 to stop if not stopped");
                log.error("Payloadid: " + "dnexpire_cluster:" + aCluster.getKey().toLowerCase() + ":rid:" + lPayloadId
                        + " already started in redis as per key \"kannel:dngeneratepayloadid\" in kannelredis and check the values configured in "
                        + "redis_config table - dnpayload ids can't be given in other instances only one instance should be started with one payloadid"
                        + " The server may not stop properly use kill -9 to stop if not stopped");
                System.exit(-1);
            }
            else
            {
                final GenerateDnsFromPayloadStore generateDnsFromRedis = new GenerateDnsFromPayloadStore(lPayloadId, aCluster);
                mDlrGenThreadLs.add(generateDnsFromRedis);
            }

            isDone = true;
            while (isDone)
                try
                {
                    DlrPayloadGenUtil.setDlrPayloadStatus("dnexpire_cluster:" + aCluster.getKey().toLowerCase() + ":Payload Rid:" + lPayloadId, true);
                    isDone = false;
                }
                catch (final Exception e)
                {

                    try
                    {
                        Thread.sleep(5000);
                    }
                    catch (final InterruptedException e1)
                    {
                        e1.printStackTrace();
                    }
                }

            if (log.isDebugEnabled())
                log.debug("Dn Generator started for payloadId:" + "dnexpire_cluster:" + aCluster.getKey().toLowerCase() + ":Payload Rid:" + lPayloadId + " dnGenThreadLs:" + mDlrGenThreadLs);
        }
    }

}
