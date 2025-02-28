package com.itextos.beacon.platform.promokannelrediscleaner.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.platform.promokannelrediscleaner.process.redisprocess.RedisCleanerProcessor;

public class StartApplication
{

    private static final Log log = LogFactory.getLog(StartApplication.class);

    public static void main(
            String[] args)
    {

        try
        {
            final RedisCleanerProcessor redisCleanerProcessor = new RedisCleanerProcessor();
            redisCleanerProcessor.start();
        }
        catch (final ItextosException e)
        {
            log.error("Exception while starting the Promo Kannel Redis Cleander.", e);
            System.exit(0);
        }
    }

}