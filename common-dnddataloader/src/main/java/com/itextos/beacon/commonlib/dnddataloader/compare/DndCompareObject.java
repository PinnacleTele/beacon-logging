package com.itextos.beacon.commonlib.dnddataloader.compare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.dnddataloader.common.CountHolder;
import com.itextos.beacon.commonlib.dnddataloader.redis.RedisOperations;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

class DndCompareObject
{

    private static final Log                log        = LogFactory.getLog(DndCompareObject.class);

    private final int                       modIndex;
    private final String                    mRange;
    private final Map<String, List<String>> dbRecords;
    private static final String             KEY_PREFIX = "dnd:";

    DndCompareObject(
            int aModIndex,
            String aRange,
            Map<String, List<String>> aDbRecords)
    {
        modIndex  = aModIndex;
        mRange    = aRange;
        dbRecords = aDbRecords;
    }

    void process()
    {

        try (
                Jedis jedis = RedisOperations.getDndMasterRedis(modIndex + 1);
                Pipeline pipe = jedis.pipelined();)
        {
            final List<CommonDndObject>                          differences  = new ArrayList<>();
            final HashMap<String, Response<Map<String, String>>> redisResults = new HashMap<>();

            long                                                 count        = 0;

            for (final String key : dbRecords.keySet())
            {
                count++;

                final Response<Map<String, String>> hgetAll = pipe.hgetAll(KEY_PREFIX + key);
                redisResults.put(key, hgetAll);

                if ((count % 1000) == 0)
                    pipe.sync();
            }

            if ((count % 1000) != 0)
                pipe.sync();

            if (log.isDebugEnabled())
                log.debug("Range '" + mRange + "' DB Records Size : '" + dbRecords.size() + "' Redis Results Size : '" + redisResults.size() + "'");

            List<String>                  dbPreferences;
            Map<String, String>           redisPreferences;
            Response<Map<String, String>> redisRes;

            for (final String key : dbRecords.keySet())
            {
                dbPreferences    = dbRecords.get(key);
                redisRes         = redisResults.get(key);
                redisPreferences = redisRes != null ? redisRes.get() : null;

                if (redisPreferences != null)
                    comparePreferences(differences, key, dbPreferences, redisPreferences);
            }

            for (final CommonDndObject obj : differences)
                log.fatal("Range '" + mRange + "' Mis matched Objects : Mod Value '" + modIndex + "' Object '" + obj + "'");
        }
    }

    private void comparePreferences(
            List<CommonDndObject> aDifferences,
            String aOuterKey,
            List<String> aDbResults,
            Map<String, String> aRedisResults)
    {
        final List<String> toRemove = new ArrayList<>();

        for (final String dbRecord : aDbResults)
        {
            final String[] innerAndPref = dbRecord.split("~");
            final String   dbInner      = innerAndPref[0];
            final String   dbPreference = innerAndPref[1];
            toRemove.add(dbInner);

            final String redisPref = aRedisResults.get(dbInner);

            if (log.isDebugEnabled())
                log.debug("Range '" + mRange + "' Preferences : outerKey : '" + aOuterKey + "' innerKey : '" + dbInner + "' DB Pref :'" + dbPreference + "' Redis Pref : '" + redisPref + "'");

            if (redisPref == null)
            {
                // No Redis info available
                aDifferences.add(new CommonDndObject(null, new DbDndObject(aOuterKey, dbInner, dbPreference)));
                CountHolder.getInstance().incrementNotAvailableInRedis();
            }
            else
            {
                final RedisDndObject redisDndObject = new RedisDndObject(modIndex, aOuterKey, dbInner, redisPref);

                if (redisPref.equals(dbPreference))
                    // No Worries here. All matched.
                    CountHolder.getInstance().incrementNoMismatch();
                else
                {
                    // Differences between the Redis and DB
                    aDifferences.add(new CommonDndObject(redisDndObject, new DbDndObject(aOuterKey, dbInner, dbPreference)));
                    CountHolder.getInstance().incrementPreferencesMismatch();
                }
            }
        } // for (String dbRecord : aDbResults)

        for (final String s : toRemove)
            aRedisResults.remove(s);

        if (aRedisResults.size() > 0)
            for (final Entry<String, String> entry : aRedisResults.entrySet())
            {
                final RedisDndObject redisDndObject = new RedisDndObject(modIndex, aOuterKey, entry.getKey(), entry.getValue());
                aDifferences.add(new CommonDndObject(redisDndObject, null));
                CountHolder.getInstance().incrementNotAvailableinDatabase();
            }
    }

}