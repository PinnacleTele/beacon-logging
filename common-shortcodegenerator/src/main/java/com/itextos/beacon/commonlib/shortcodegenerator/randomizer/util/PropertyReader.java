package com.itextos.beacon.commonlib.shortcodegenerator.randomizer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertyReader
{

    private static final Logger log = LogManager.getLogger(PropertyReader.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final PropertyReader INSTANCE = new PropertyReader();

    }

    public static PropertyReader getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private int     mNoOfFileReader;
    private int     mNoOfRedisPusher;
    private int     mNoOfRedisReader;
    private int     mMaxInMemorySize;
    private int     mMaxDatabaseInRedis;
    private int     mMaxRecordsPerFile;

    private String  mInputFolder;
    private String  mOutputFolder;
    private String  mRedisConfigPath;
    private String  mFileNamePrefix;
    private boolean mFlushRedisStartup;
    private boolean mDeletePartialCreatedFiles;

    private PropertyReader()
    {
        final String propertyFileName = System.getProperty("randomizer.properties.filepath");

        if (log.isDebugEnabled())
            log.debug("Properties File Name : '" + propertyFileName + "'");

        final Properties lProperties = new Properties();

        try (
                FileInputStream lFileInputStream = new FileInputStream(new File(propertyFileName));)
        {
            lProperties.load(lFileInputStream);
            lProperties.list(System.out);

            mNoOfFileReader            = Integer.parseInt(lProperties.getProperty("number.file.reader"));
            mNoOfRedisPusher           = Integer.parseInt(lProperties.getProperty("number.redis.pusher"));
            mNoOfRedisReader           = Integer.parseInt(lProperties.getProperty("number.redis.reader"));
            mMaxInMemorySize           = Integer.parseInt(lProperties.getProperty("max.inmemory.size"));
            mMaxDatabaseInRedis        = Integer.parseInt(lProperties.getProperty("max.database.in.redis"));
            mMaxRecordsPerFile         = Integer.parseInt(lProperties.getProperty("max.records.per.file"));

            mInputFolder               = lProperties.getProperty("input.folder");
            mOutputFolder              = lProperties.getProperty("output.folder");
            mRedisConfigPath           = lProperties.getProperty("redis.config.path");
            mFileNamePrefix            = lProperties.getProperty("filename.prefix");
            mFlushRedisStartup         = "1YESTRUE".contains(lProperties.getProperty("flush.redis.onstartup", "0").toUpperCase());
            mDeletePartialCreatedFiles = "1YESTRUE".contains(lProperties.getProperty("delete.old.created.files", "0").toUpperCase());

            if (!mInputFolder.endsWith(File.separator))
                mInputFolder = mInputFolder + File.separator;

            if (!mOutputFolder.endsWith(File.separator))
                mOutputFolder = mOutputFolder + File.separator;

            if (log.isDebugEnabled())
            {
                log.debug("mNoOfFileReader            : '" + mNoOfFileReader + "'");
                log.debug("mNoOfRedisPusher           : '" + mNoOfRedisPusher + "'");
                log.debug("mNoOfRedisReader           : '" + mNoOfRedisReader + "'");
                log.debug("mMaxInMemorySize           : '" + mMaxInMemorySize + "'");
                log.debug("mMaxDatabaseInRedis        : '" + mMaxDatabaseInRedis + "'");
                log.debug("mMaxRecordsPerFile         : '" + mMaxRecordsPerFile + "'");
                log.debug("mInputFolder               : '" + mInputFolder + "'");
                log.debug("mOutputFolder              : '" + mOutputFolder + "'");
                log.debug("mRedisConfigPath           : '" + mRedisConfigPath + "'");
                log.debug("mFlushRedisStartup         : '" + mFlushRedisStartup + "'");
                log.debug("mDeletePartialCreatedFiles : '" + mDeletePartialCreatedFiles + "'");
            }
        }
        catch (final IOException e)
        {
            log.error("Exception while reading the properties file. Cannot proceed further.", e);
            System.exit(-1);
        }
    }

    public int getNoOfFileReader()
    {
        return mNoOfFileReader;
    }

    public int getNoOfRedisPusher()
    {
        return mNoOfRedisPusher;
    }

    public int getNoOfRedisReader()
    {
        return mNoOfRedisReader;
    }

    public int getMaxInMemorySize()
    {
        return mMaxInMemorySize;
    }

    public int getMaxDatabaseInRedis()
    {
        return mMaxDatabaseInRedis;
    }

    public String getInputFolder()
    {
        return mInputFolder;
    }

    public String getOutputFolder()
    {
        return mOutputFolder;
    }

    public boolean isFlushRedisStartup()
    {
        return mFlushRedisStartup;
    }

    public boolean isDeletePartialCreatedFiles()
    {
        return mDeletePartialCreatedFiles;
    }

    public String getRedisConfigPath()
    {
        return mRedisConfigPath;
    }

    public int getMaxRecordsPerFile()
    {
        return mMaxRecordsPerFile;
    }

    public String getFileNamePrefix()
    {
        return mFileNamePrefix;
    }

}