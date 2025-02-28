package com.itextos.beacon.commonlib.shortcodeprovider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ShortcodeLength;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.shortcodeprovider.operation.DbOperation;
import com.itextos.beacon.commonlib.shortcodeprovider.operation.FileInfo;
import com.itextos.beacon.commonlib.shortcodeprovider.operation.RedisOperation;
import com.itextos.beacon.commonlib.shortcodeprovider.operation.ShortCodeProperties;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;

public class RedisShortCodeChecker
        implements
        ITimedProcess
{

    private static final Log     log         = LogFactory.getLog(RedisShortCodeChecker.class);

    private final TimedProcessor mTimedProcessor;
    private boolean              canContinue = true;

    public RedisShortCodeChecker()
    {
    	
        mTimedProcessor = new TimedProcessor("RedisShortCodeChecker", this, TimerIntervalConstant.SHORT_CODE_COUNT_CHECER);
    	
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "RedisShortCodeChecker");

        if (log.isDebugEnabled())
            log.debug("Starting the TimedProcess for the Redis Short Code Checker.");
    }

    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    @Override
    public boolean processNow()
    {
        checkRedisCount();
        return false;
    }

    private static void checkRedisCount()
    {

        try
        {
            if (log.isDebugEnabled())
                log.debug("Checking for the length for " + ShortcodeLength.LENGTH_5);

            checkForLength(ShortcodeLength.LENGTH_5);

            if (log.isDebugEnabled())
                log.debug("Checking for the length for " + ShortcodeLength.LENGTH_6);

            checkForLength(ShortcodeLength.LENGTH_6);
        }
        catch (final Exception e)
        {
            log.error("Exception while checking the counts of the shortcodes", e);
        }
    }

    private static void checkForLength(
            ShortcodeLength aShortcodeType)
            throws Exception
    {
        long redisCount = -1;

        switch (aShortcodeType)
        {
            case LENGTH_5:
                redisCount = RedisOperation.checkForLength5();
                break;

            case LENGTH_6:
                redisCount = RedisOperation.checkForLength6();
                break;

            default:
                break;
        }

        if (log.isDebugEnabled())
            log.debug("Count in Redis for " + aShortcodeType + " is " + redisCount);

        if (redisCount < ShortCodeProperties.getInstance().getMinimumRedisCount())
            loadFromFile(aShortcodeType);
    }

    private static void loadFromFile(
            ShortcodeLength aShortcodeType)
            throws Exception
    {
        final FileInfo lFileInfo = DbOperation.loadFromFile(aShortcodeType);

        if (log.isInfoEnabled())
            log.info("Next file information to be used load into redis " + lFileInfo);

        if (lFileInfo != null)
        {
            final long lLoadDataIntoRedis = RedisOperation.loadDataIntoRedis(aShortcodeType, lFileInfo.getFileName());

            if (log.isDebugEnabled())
                log.debug("Loaded records count from '" + lFileInfo.getFileName() + "' is '" + lLoadDataIntoRedis + "'");

            if (lLoadDataIntoRedis == 0)
                throw new ItextosRuntimeException("Some problem in loading the data from file " + lFileInfo);

            DbOperation.updateStatusToDb(lFileInfo.getId());
        }
        else
            log.error("WE HAVE SOME ISSUE TO GET THE FILE TO LOAD INTO REDIS.");
    }

    @Override
    public void stopMe()
    {
        canContinue = true;
    }

}