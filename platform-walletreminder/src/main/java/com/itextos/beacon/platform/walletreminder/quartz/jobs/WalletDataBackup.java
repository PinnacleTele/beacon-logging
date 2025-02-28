package com.itextos.beacon.platform.walletreminder.quartz.jobs;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.walletreminder.utils.WalletReminderProperties;

import redis.clients.jedis.Jedis;

public class WalletDataBackup
        implements
        Job
{

    private static final Log    log                = LogFactory.getLog(WalletDataBackup.class);

    private static final String REDIS_PREAPID_KEY  = "wallet:amount";
    static final String         FILE_PREFIX        = "prepaidstatus_";
    private static final int    FILE_PREFIX_LENGTH = FILE_PREFIX.length();
    private static final int    OLD_BACKUP_DAYS    = WalletReminderProperties.getInstance().getBackupDataDays();
    private static final String PARENT_FOLDER      = WalletReminderProperties.getInstance().getBackupFolder();

    static
    {
        final File f = new File(PARENT_FOLDER);

        if (!f.exists())
        {
            if (log.isDebugEnabled())
                log.debug(f.getAbsolutePath() + " not exists. Trying to create it.");

            final boolean lMkdirs = f.mkdirs();

            if (log.isDebugEnabled())
                log.debug("Status of mk dirs " + lMkdirs);

            if (!lMkdirs)
                log.error("Exception while making directory ");
        }
        else
            if (log.isDebugEnabled())
                log.debug("Parent Folder already Exists.");
    }

    @Override
    public void execute(
            JobExecutionContext aContext)
            throws JobExecutionException
    {
        if (log.isDebugEnabled())
            log.debug("Job Execution Time : " + DateTimeUtility.getFormattedDateTime(aContext.getFireTime(), DateTimeFormat.DEFAULT));

        final long startTime = System.currentTimeMillis();

        doBackupInFile();

        final long endTime = System.currentTimeMillis();

        if (log.isInfoEnabled())
            log.info("Time taken to complete the job " + (endTime - startTime) + " millis");

        log.fatal("Next scheduled time: " + DateTimeUtility.getFormattedDateTime(aContext.getNextFireTime(), DateTimeFormat.DEFAULT));
    }

    private static void doBackupInFile()
    {
        if (log.isDebugEnabled())
            log.debug("Calling Prepaid Redis Data backup.");

        try (
                Jedis jedis = getRedisConnection();)
        {
            final Map<String, String> lHgetAll = jedis.hgetAll(REDIS_PREAPID_KEY);

            if (lHgetAll != null)
            {
                if (log.isDebugEnabled())
                    log.debug("Prepaid records count " + lHgetAll.size());

                final String fileContent = getFileContent(lHgetAll);

                final String fileName    = getFileName();
                final Path   lWrite      = Files.write(Path.of(fileName), fileContent.getBytes(), StandardOpenOption.CREATE);

                if (log.isInfoEnabled())
                    log.info("Prepaid datat Written in '" + lWrite + "'");

                deleteOldRecords();
            }
            else
                log.fatal("Unable to get the prepaid Redis Data.");
        }
        catch (final Exception e)
        {
            log.error("Exception while getting / writing the prepaid data from Redis into file.", e);
        }
    }

    private static String getFileName()
    {
        return PARENT_FOLDER + File.separator + FILE_PREFIX + new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date()) + ".csv";
    }

    private static void deleteOldRecords()
    {

        try
        {
            final List<File> filteredFiles = getFilteredFiles();

            if (filteredFiles.isEmpty())
            {
                log.error("No old file to delete.");
                return;
            }

            for (final File curFile : filteredFiles)
                deleteFile(curFile);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting old files list", e);
        }
    }

    private static void deleteFile(
            File aCurFile)
    {

        try
        {
            if (log.isInfoEnabled())
                log.info("Deleting old file '" + aCurFile.getAbsolutePath() + "'");

            final boolean lDelete = aCurFile.delete();

            if (log.isDebugEnabled())
                log.debug("Delete file status '" + aCurFile.getAbsolutePath() + "' is " + lDelete);

            if (!lDelete)
                log.error("Some problem in deleting the file. Filename '" + aCurFile.getAbsolutePath() + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while deleting the file '" + aCurFile.getAbsolutePath() + "'", e);
        }
    }

    private static List<File> getFilteredFiles()
            throws ItextosException
    {
        final List<File> returnValue = new ArrayList<>();

        final File       parent      = new File(PARENT_FOLDER);
        final File[]     lListFiles  = parent.listFiles(new MyFileFilter());

        final long       toCompare   = getOldestDate();

        if ((lListFiles != null) && (lListFiles.length > 0))
            for (final File currentFile : lListFiles)
            {
                final String s    = currentFile.getName();
                final long   temp = getCurrentFileDate(s);

                if (temp < toCompare)
                    returnValue.add(currentFile);
            }

        return returnValue;
    }

    private static long getCurrentFileDate(
            String aCurrentFileName)
            throws ItextosException
    {
        final String date = aCurrentFileName.substring(FILE_PREFIX_LENGTH, FILE_PREFIX_LENGTH + 8);
        final long   temp = CommonUtility.getLong(date, -9999);

        if (temp == -9999)
            throw new ItextosException("Invalid Date format. Something is not right here.");

        return temp;
    }

    private static long getOldestDate()
            throws ItextosException
    {
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, -1 * OLD_BACKUP_DAYS);
        final long toCompare = CommonUtility.getLong(DateTimeUtility.getFormattedDateTime(c.getTime(), DateTimeFormat.NO_SEPARATOR_YYYY_MM_DD), -9999);

        if (toCompare == -9999)
            throw new ItextosException("Invalid Date format. Something is not right here.");

        return toCompare;
    }

    private static String getFileContent(
            Map<String, String> aHgetAll)
    {
        final Map<String, String> sorted        = new TreeMap<>(aHgetAll);

        final String              lineSeparator = CommonUtility.getLineSeparator();
        final StringJoiner        sj            = new StringJoiner(lineSeparator);

        for (final Entry<String, String> entry : sorted.entrySet())
            sj.add(entry.getKey() + "," + entry.getValue());

        return sj.toString();
    }

    private static Jedis getRedisConnection()
    {
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.WALLET_CHK, 1);
    }

    public static void main(
            String[] args)
            throws ItextosException
    {
        final String s  = WalletReminderProperties.getInstance().getClientBodyFormat();
        final String s1 = MessageFormat.format(s, "46.875 (INR)", "Saravanan L", "2022-02-07 20:06:05", "pulsesadmin", "4000005900000000");
        System.out.println(s1);
    }

}

class MyFileFilter
        implements
        FileFilter
{

    @Override
    public boolean accept(
            File aPathname)
    {

        if (aPathname.isFile())
        {
            final String s = aPathname.getName().toLowerCase();
            if (s.startsWith(WalletDataBackup.FILE_PREFIX) && s.endsWith(".csv"))
                return true;
        }
        return false;
    }

}