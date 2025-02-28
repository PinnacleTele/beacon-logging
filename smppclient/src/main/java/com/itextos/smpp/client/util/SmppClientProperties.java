package com.itextos.smpp.client.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SmppClientProperties
        extends
        PropertyConstants
{

    private static final Log log               = LogFactory.getLog(SmppClientProperties.class);

    private static final int DCS_PLAIN         = 0;
    private static final int DCS_UC2           = 8;
    private static final int DCS_FLASH_PLAIN   = 10;
    private static final int DCS_FLASH_UNICODE = 18;
    private static final int DCS_SP_PLAIN      = 12;
    private static final int DCS_BINARY        = 11;

    private static class SingletonHolder
    {

        static final SmppClientProperties INSTANCE = new SmppClientProperties();

    }

    public static SmppClientProperties getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private SmppClientProperties()
    {
        loadProperties();
    }

    private final Properties prop = new Properties();

    private void loadProperties()
    {
        final String lPropertyPath     = System.getProperty("smpp.client.prop.path");
        final String propFileName      = "smpp-client.properties";
        final String lPropertyFilePath = lPropertyPath + File.separator + propFileName;

        log.fatal("Property File Path : " + lPropertyFilePath);

        try (
                InputStream input = new FileInputStream(lPropertyFilePath))
        {
            prop.load(input);
        }
        catch (final IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private String getStringValue(
            String aKey)
    {
        return prop.getProperty(aKey) == null ? "" : prop.getProperty(aKey);
    }

    private int getIntValue(
            String aKey)
    {
        return SmppClientUtil.getInteger(SmppClientUtil.nullCheck(prop.getProperty(aKey)));
    }

    private int getLongValue(
            String aKey)
    {
        return SmppClientUtil.getInteger(SmppClientUtil.nullCheck(prop.getProperty(aKey)));
    }

    private boolean getBooleanValue(
            String aKey)
    {
        return "1YESTRUE".contains(getStringValue(aKey).toUpperCase());
    }

    public int getBindType()
    {
        return getIntValue(BIND_TYPE);
    }

    public String getHost()
    {
        return getStringValue(HOST);
    }

    public int getPort()
    {
        return getIntValue(PORT);
    }

    public String getSystemId()
    {
        return getStringValue(SYSTEM_ID);
    }

    public String getPassword()
    {
        return getStringValue(PASSWORD);
    }

    public int getWindowSize()
    {
        return getIntValue(WINDOW_SIZE);
    }

    public String getSourceAddress()
    {
        return getStringValue(SOURCE_ADDR);
    }

    public String getDestinationAddress()
    {
        return getStringValue(DEST_ADDR);
    }

    public int getRegisteredDelivery()
    {
        return getIntValue(REGISTERED_DELIVERY);
    }

    public String getValidityPeriod()
    {
        return getStringValue(VALIDITY_PERIOD);
    }

    public String getMessage()
    {
        return getStringValue(MESSAGE);
    }

    public String getScheduleTime()
    {
        return getStringValue(SCHEDULE_DELIVERY_TIME);
    }

    public byte getDcs()
    {
        return getDcsFromInt(getIntValue(DCS));
    }

    private static byte getDcsFromInt(
            int lDcs)
    {

        switch (lDcs)
        {
            case DCS_PLAIN:
                return (byte) 0x00;

            case DCS_UC2:
                return (byte) 0x08;

            case DCS_FLASH_PLAIN:
                return (byte) 0x10;

            case DCS_FLASH_UNICODE:
                return (byte) 0x18;

            case DCS_SP_PLAIN:
                return (byte) 0x12;

            case DCS_BINARY:
                return (byte) 0x11;

            default:
                return (byte) 0x00;
        }
    }

    public String getCharSet()
    {
        return getStringValue(CHARSET_VALUE);
    }

    public String getOptionalParams()
    {
        return getStringValue(OPTIONAL_PARAMS);
    }

    public long getNumberOfMessages()
    {
        return getIntValue(NO_OF_MESSAGES);
    }

    public boolean is16BitUdfRef()
    {
        return getBooleanValue(IS_16_BIT_UDF_REF);
    }

    public boolean isContineousHit()
    {
        return getBooleanValue(IS_CONTINEOUS_HIT);
    }

    public int getSleepInbetweenHitsInMillis()
    {
        return getIntValue(SLEEP_BETWEEN_EVERY_HIT);
    }

    public int getSessionBindCounts()
    {
        return getIntValue(NUMBER_OF_SESSIONS);
    }

}