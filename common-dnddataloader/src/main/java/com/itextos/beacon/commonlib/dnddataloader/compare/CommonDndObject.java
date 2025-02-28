package com.itextos.beacon.commonlib.dnddataloader.compare;

import com.itextos.beacon.commonlib.dnddataloader.enums.RedisDbCompareResults;

class CommonDndObject
{

    private final RedisDndObject  redisDndObject;
    private final DbDndObject     databaseDndObject;
    private RedisDbCompareResults result = RedisDbCompareResults.NO_MISMATCH;

    CommonDndObject(
            RedisDndObject aRedisDndObject,
            DbDndObject aDatabaseDndObject)
    {
        redisDndObject    = aRedisDndObject;
        databaseDndObject = aDatabaseDndObject;

        compare();
    }

    void compare()
    {
        if (redisDndObject == null)
            result = RedisDbCompareResults.NOT_AVAILABLE_IN_REDIS;
        else
            if (databaseDndObject == null)
                result = RedisDbCompareResults.NOT_AVAILABLE_IN_DATABASE;
            else
                if (redisDndObject.getDest() == databaseDndObject.getDest())
                {
                    if (!redisDndObject.getRedisPreference().equals(databaseDndObject.getPreferences()))
                        result = RedisDbCompareResults.PREFERENCES_MISMATCH;
                    else
                        // this may not come here
                        result = RedisDbCompareResults.NO_MISMATCH;
                }
                else
                    // this may not come here
                    result = RedisDbCompareResults.DEST_MISMATCH;
    }

    RedisDbCompareResults getResult()
    {
        return result;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(result).append("\t");

        if (redisDndObject == null)
            sb.append("\t\t\t\t\t");
        else
        {
            sb.append(redisDndObject.getRedisIndex()).append("\t");
            sb.append(redisDndObject.getDest()).append("\t");
            sb.append(redisDndObject.getRedisOuterKey()).append("\t");
            sb.append(redisDndObject.getRedisInnerKey()).append("\t");
            sb.append(redisDndObject.getRedisPreference()).append("\t");
        }

        if (databaseDndObject == null)
            sb.append("\t\t");
        else
        {
            sb.append(databaseDndObject.getDest()).append("\t");
            sb.append(databaseDndObject.getPreferences()).append("\t");
        }
        return sb.toString();
    }

}