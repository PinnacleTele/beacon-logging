package com.itextos.beacon.commonlib.dnddataloader.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.dnddataloader.enums.DndAction;
import com.itextos.beacon.commonlib.dnddataloader.redis.RedisOperations;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class DndInfo
{

    private static final Log   log        = LogFactory.getLog(DndInfo.class);

    public static final String KEY_PREFIX = "dnd:";

    private String             dest;
    private final String       originalDest;
    private final String       preferences;
    private final String       operation;

    private DndAction          dndAction;
    private String             hashKey    = null;
    private String             key        = null;
    private int                redisIndex = -1;

    public DndInfo(
            String aDest,
            String aPreferences,
            String aOperation)
    {
        originalDest = aDest;
        dest         = CommonUtility.nullCheck(aDest, true);
        preferences  = CommonUtility.nullCheck(aPreferences, true);
        operation    = aOperation;
        dndAction    = validateData(aOperation);

        if (DndAction.INVALID != this.dndAction)
            processDest();
    }

    private DndAction validateData(
            String aOperation)
    {
        DndAction returnValue = DndAction.INVALID;

        try
        {
            dest = checkForMaxDigit(dest);

            if (dndAction != DndAction.INVALID_NUMBER)
            {
                aOperation = CommonUtility.nullCheck(aOperation, true);

                if (DndAction.APPEND_OR_UPDATE.getOperation().equals(aOperation))
                {
                    if ("".equals(preferences) || "null".equals(preferences))
                        returnValue = DndAction.INVALID;
                    else
                        returnValue = DndAction.APPEND_OR_UPDATE;
                }
                else
                    if (DndAction.DELETE.getOperation().equals(aOperation))
                        returnValue = DndAction.DELETE;
            }
        }
        catch (final Exception e)
        {
            log.error("Setting the operation as INVALID as encounted exception while validating the inputs '" + toString() + "'", e);
            returnValue = DndAction.INVALID;
        }
        return returnValue;
    }

    private void processDest()
    {
        hashKey    = KEY_PREFIX + dest.substring(0, 5);
        key        = dest.substring(5);

        // As Redis index starts from 1 and not with 0
        redisIndex = 1 + (int) (Long.parseLong(dest) % RedisOperations.getDndMasterRedisCount());
    }

    /**
     * Check the length of the number:
     * <ul>
     * <li>If it is more than 10 then get the last 10 digits.
     * <li>If it is less then 7 then throw an exception.
     * <li>Else return the number as it is.
     * </ul>
     * Meanwhile need to validate the passed string is number or not.
     *
     * @param aDest
     *
     * @return
     */
    private String checkForMaxDigit(
            String aDest)
    {
        String    returnValue = aDest;
        final int len         = aDest.length();

        if (log.isDebugEnabled())
            log.debug("dest : '" + aDest + "' - Length : '" + len + "'");

        // get the last 10 digits only.
        if (len > 10)
            returnValue = aDest.substring(len - 10);
        else
            if (len < 7)
            {
                log.error("Invalid Number specified. Number : '" + aDest + "'");
                dndAction = DndAction.INVALID_NUMBER;
            }

        try
        {
            // To check the validity of the numeric value.
            Long.parseLong(returnValue);

            if (log.isDebugEnabled())
                log.debug("Final dest : '" + returnValue + "'");
        }
        catch (final Exception e)
        {
            log.error("Number specified is not proper. " + aDest, e);
            dndAction = DndAction.INVALID_NUMBER;
        }
        return returnValue;
    }

    public String getDest()
    {
        return dest;
    }

    public String getPreferences()
    {
        return preferences;
    }

    public String getHashKey()
    {
        return hashKey;
    }

    public String getKey()
    {
        return key;
    }

    public int getRedisIndex()
    {
        return redisIndex;
    }

    public DndAction getDndAction()
    {
        return dndAction;
    }

    public String getOriginal()
    {
        return originalDest;
    }

    @Override
    public String toString()
    {
        return "DndInfo [dest=" + dest + ", originalDest=" + originalDest + ", preferences=" + preferences + ", operation=" + operation + ", dndAction=" + dndAction + ", hashKey=" + hashKey + ", key="
                + key + ", redisIndex=" + redisIndex + "]";
    }

}