package com.itextos.beacon.queryprocessor.commonutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogBackWrapper
{

    private static final Logger logger = LoggerFactory.getLogger(LogBackWrapper.class);

    public static void LogInfo(
            String message)
    {
        System.out.println(message);
        logger.info(message);
    }

    public static void LogError(
            String message)
    {
        System.out.println(message);
        logger.error(message);
    }

    public static void LogDebug(
            String message)
    {
        System.out.println(message);
        logger.debug(message);
    }

}
