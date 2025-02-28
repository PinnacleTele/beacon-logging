package com.itextos.beacon.http.cloudacceptor.common.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FailuerLogger
{

    private final static Log log = LogFactory.getLog(FailuerLogger.class);

    public static void log(
            Class<?> aClass,
            LogLevel aLogLevel,
            String aMessage)
    {
        log(aClass, aLogLevel, aMessage, null);
    }

    public static void log(
            Class<?> aClass,
            LogLevel aLogLevel,
            String aMessage,
            Throwable aThrowable)
    {
        final StringBuffer sb = new StringBuffer("[");
        sb.append(aClass.getSimpleName()).append("] ").append(aMessage);

        switch (aLogLevel)
        {
            case TRACE:
                if (log.isTraceEnabled())
                    log.trace(sb, aThrowable);
                break;

            case DEBUG:
                if (log.isDebugEnabled())
                    log.debug(sb, aThrowable);
                break;

            case INFO:
                if (log.isInfoEnabled())
                    log.info(sb, aThrowable);
                break;

            case WARN:
                if (log.isWarnEnabled())
                    log.warn(sb, aThrowable);
                break;

            case ERROR:
                if (log.isErrorEnabled())
                    log.error(sb, aThrowable);
                break;

            case FATAL:
                if (log.isFatalEnabled())
                    log.fatal(sb, aThrowable);
                break;

            case ALL:
                log.trace(sb, aThrowable);
            case OFF:
                // ignore it
            default:
                log.debug(sb, aThrowable);
                break;
        }
    }

}
