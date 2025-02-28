package com.itextos.beacon.platform.smppdlrutil.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;

import redis.clients.jedis.Jedis;

public class SmppDlrRedis
        implements
        ITimedProcess
{

    private static final Log                              log              = LogFactory.getLog(SmppDlrRedis.class);

    static final String                                   DN_SESSION_INFO  = "dn:session:info:*";
    private static Map<String, List<Map<String, String>>> mLiveSessionInfo = new HashMap<>();

    private final int                                     mRedisPoolIndex;
    private final TimedProcessor                          mTimedProcessor;
    private boolean                                       mCanContinue     = true;

    public SmppDlrRedis(
            int aRedisPoolIndex)
    {
        super();
        mRedisPoolIndex = aRedisPoolIndex;
      
        mTimedProcessor = new TimedProcessor("SmppRedisOperation:" + mRedisPoolIndex, this, TimerIntervalConstant.SMPP_CONCAT_MESSAGE_CHECKER_INTERVAL);
        
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "SmppRedisOperation:" + mRedisPoolIndex);
        
        if (log.isDebugEnabled())
            log.debug("SmppDlrRedisPoller started successfully ........." + aRedisPoolIndex);
    }

    @Override
    public boolean processNow()
    {
        getSessionInfo();
        return false;
    }

    public static Map<String, List<Map<String, String>>> getLiveSessionInfo()
    {
        return mLiveSessionInfo;
    }

    public static void getSessionInfo()
    {
        final Map<String, List<Map<String, String>>> result = new HashMap<>();

        try (
                Jedis lJedis = SmppRedisConnectionProvider.getRedis();)
        {
            final Set<String>      keys              = lJedis.keys(DN_SESSION_INFO);
            final Iterator<String> sessionInfoKeyitr = keys.iterator();

            while (sessionInfoKeyitr.hasNext())
                try
                {
                    final String              lKey           = sessionInfoKeyitr.next().toString();
                    final String              lClientId      = lKey.substring(lKey.lastIndexOf(":") + 1, lKey.length());

                    final Map<String, String> data           = lJedis.hgetAll(lKey);
                    final Iterator<String>    instanceIdItr  = data.keySet().iterator();
                    List<Map<String, String>> instanceidlist = null;

                    if (result.containsKey(lClientId))
                        instanceidlist = result.get(lClientId);
                    else
                    {
                        instanceidlist = new ArrayList<>();
                        result.put(lClientId, instanceidlist);
                    }

                    while (instanceIdItr.hasNext())
                    {
                        final String              instanceid = instanceIdItr.next().toString();
                        final Map<String, String> record     = new HashMap<>();
                        record.put("INSTANCEID", instanceid);
                        record.put("VERSION", "4");

                        final int sessioncnt = Integer.parseInt(data.get(instanceid).toString());
                        for (int i = 0; i < sessioncnt; i++)
                            instanceidlist.add(record);
                    }
                }
                catch (final Exception e)
                {
                    log.error("getSessionInfo()", e);
                }
        }

        if (log.isDebugEnabled())
            log.debug("Live TRX/RX Sessions list :" + result);

        mLiveSessionInfo = result;
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
    }

}
