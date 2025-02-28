package com.itextos.beacon.platform.faillistutil.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.platform.faillistutil.process.FileStats;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * Utility class to have the common methods.
 */
public final class FaillistUtil
{

    private static Log log = LogFactory.getLog(FaillistUtil.class);

    /**
     * A method to read the CSV files from the folder specified in
     * {@link FaillistConfig}.
     *
     * @param aBlockListConfig
     *                         To represent the configuration of the process type,
     *                         which read from the properties file.
     *
     * @return {@link List} of {@link File} from the folder specified in the
     *         <code>aBlockListConfig</code> object.
     */
    public static List<File> getFiles(
            FaillistConfig aBlockListConfig)
    {
        final List<File> list = new ArrayList<>();

        try
        {
            final File f = new File(aBlockListConfig.getFilePath());

            if (log.isDebugEnabled())
                log.debug("File path to look CSV files : '" + f.getAbsolutePath() + "'");

            final File[] listFiles = f.listFiles(new CSVFileFilter());
            list.addAll(Arrays.asList(listFiles));

            if (log.isDebugEnabled())
                log.debug("List of files to be processed : '" + list + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the CSV files.", e);
        }
        return list;
    }

    /**
     * Build the Redis Keyword String based on the parameters passed.
     *
     * @param aEsme
     * @param aNumber
     * @param aSplitLength
     * @param aRedisKeyPrefix
     *
     * @return
     */
    public static String[] getRedisKeywords(
            String aEsme,
            String aNumber,
            int aSplitLength,
            String aRedisKeyPrefix)
    {
        final String[]     splitted = splitNumber(aNumber, aSplitLength);

        final StringBuffer sb       = new StringBuffer();
        sb.append(aRedisKeyPrefix);
        sb.append(FaillistConstants.REDIS_KEY_SEPARATOR);
        sb.append(aEsme);
        sb.append(FaillistConstants.REDIS_KEY_SEPARATOR);
        sb.append(splitted[0]);
        return new String[]
        { sb.toString(), splitted[1] };
    }

    /**
     * Split the number passed by the length specified.
     *
     * @param aNumber
     *                     - A <code>String</code> representing the number to be
     *                     split.
     * @param aSplitLength
     *                     - An <code>int</code> representing the number of
     *                     characters to be used for the outer key.
     *
     * @return A <code>String</code> array returns the split strings. First element
     *         will be the outer key, second element will be the member of the list
     *         for the Outer key.
     */
    private static String[] splitNumber(
            String aNumber,
            int aSplitLength)
    {
        return new String[]
        { aNumber.substring(0, aSplitLength), aNumber.substring(aSplitLength) };
    }

    private static boolean checkAndAddNumbersToProcess(
            FaillistRecord aRecord,
            int aRedisPoolSize,
            Map<Integer, List<FaillistRecord>> aToBeProcessed)
    {

        try
        {
            final long           lNumber  = Long.parseLong(aRecord.getNumber());
            final int            modIndex = (int) ((lNumber % aRedisPoolSize) + 1);

            List<FaillistRecord> list     = aToBeProcessed.get(modIndex);

            if (list == null)
            {
                list = new ArrayList<>();
                aToBeProcessed.put(modIndex, list);
            }
            list.add(aRecord);
            return true;
        }
        catch (final Exception e)
        {
            log.error("Unable parse the number passed. Number : '" + aRecord.getNumber() + "'", e);
            return false;
        }
    }

    private static FaillistRecord validateRecord(
            String aRecordString,
            int aSplitLength,
            String aRedisKeyPrefix)
    {
        final String[] splittedString = aRecordString.split(",");
        FaillistRecord record;
        if (splittedString.length == 2)
            record = new FaillistRecord(splittedString[0], splittedString[1], FaillistConstants.ACTION_ADD, aSplitLength, aRedisKeyPrefix);
        else
            if (splittedString.length == 3)
                record = new FaillistRecord(splittedString[0], splittedString[1], splittedString[2], aSplitLength, aRedisKeyPrefix);
            else
                record = new FaillistRecord(null, null, null, -1, null);
        return record;
    }

    /**
     * A method to read the file content passed <code>aCurrentFile</code>, specific
     * to the configuration passed <code>aBlockListConfig</code> and the populate
     * the statistics in the passed
     * <code>aCurrentStats</code>
     * <p>
     * <b>Note:</b> Need to handle the exceptional cases.
     *
     * @param aCurrentFile
     *                         - A {@link File} object which needs to be processed.
     * @param aBlockListConfig
     *                         - A (@link BlockListConfig} object which represents
     *                         the configurations of the process.
     * @param aCurrentStats
     *                         - A {@link FileStats} object which holds the
     *                         statistical data of the file processing.
     *
     * @return An <code>int</code> representing the total number of records has been
     *         read from the file.
     */
    public static int readFile(
            File aCurrentFile,
            FaillistConfig aBlockListConfig,
            FileStats aCurrentStats)
    {
        Scanner sc           = null;
        int     totalRecords = 0;
        aCurrentStats.setProcessStartTime(System.currentTimeMillis());

        try
        {
            sc = new Scanner(aCurrentFile);
            final List<String> currentList = new ArrayList<>();
            String             tempLine;

            while (sc.hasNextLine())
            {
                tempLine = sc.nextLine();

                if (log.isDebugEnabled())
                    log.debug("Line read from file : '" + tempLine + "'");

                currentList.add(tempLine.toUpperCase());
                ++totalRecords;

                if ((currentList.size() % FaillistPropertyLoader.getInstance().getBatchSize()) == 0)
                {
                    processRecords(currentList, aBlockListConfig, aCurrentStats);
                    currentList.clear();
                }
            }

            if ((currentList.size() % FaillistPropertyLoader.getInstance().getBatchSize()) != 0)
            {
                processRecords(currentList, aBlockListConfig, aCurrentStats);
                currentList.clear();
            }

            aCurrentStats.addTotalRecords(totalRecords);
            aCurrentStats.setProcessEndTime(System.currentTimeMillis());
        }
        catch (final FileNotFoundException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (sc != null)
                sc.close();
        }
        return totalRecords;
    }

    private static void processRecords(
            List<String> aCurrentList,
            FaillistConfig aBlockListConfig,
            FileStats aCurrentStats)
    {
        final Map<Integer, List<FaillistRecord>> toBeProcessed = new HashMap<>();
        FaillistRecord                           record;
        final int                                redisPoolSize = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.FAILLIST);

        if (log.isDebugEnabled())
            log.debug("Number of records to be processed now is : '" + aCurrentList.size() + "'");

        long validRecords = 0, invalidRecords = 0;

        for (final String currentLine : aCurrentList)
        {
            record = validateRecord(currentLine, aBlockListConfig.getNumberSplitLength(), aBlockListConfig.getRedisPrefixKey());

            if (log.isDebugEnabled())
                log.debug("Record Object : '" + record + "'");

            if (record.isValid())
            {
                final boolean checkAndAddNumbersToProcess = checkAndAddNumbersToProcess(record, redisPoolSize, toBeProcessed);

                if (checkAndAddNumbersToProcess)
                    validRecords++;
                else
                    invalidRecords++;
            }
            else
                invalidRecords++;
        }

        if (log.isDebugEnabled())
            log.debug("Valid Records : '" + validRecords + "', Invalid Records : '" + invalidRecords + "'");

        aCurrentStats.addValidRecords(validRecords);
        aCurrentStats.addInvalidRecords(invalidRecords);

        doRedisOperations(aBlockListConfig, toBeProcessed, aCurrentStats);
    }

    private static void doRedisOperations(
            FaillistConfig aBlockListConfig,
            Map<Integer, List<FaillistRecord>> aToBeProcessed,
            FileStats aCurrentStats)
    {

        for (final Integer redisIndex : aToBeProcessed.keySet())
        {
            final List<FaillistRecord> list     = aToBeProcessed.get(redisIndex);
            Jedis                      con      = null;
            Pipeline                   pipeline = null;

            try
            {
                if (log.isDebugEnabled())
                    log.debug("Getting Redis connection for the redis index : '" + redisIndex + "'");

                con      = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.FAILLIST, redisIndex);
                pipeline = con.pipelined();

                final List<Response<Long>> addResults    = new ArrayList<>();
                final List<Response<Long>> removeResults = new ArrayList<>();

                for (final FaillistRecord r : list)
                {
                    if (log.isDebugEnabled())
                        log.debug("Records to be processed : '" + r + "'");

                    if (FaillistConstants.ACTION_ADD.equalsIgnoreCase(r.getActionFlag()))
                    {
                        final Response<Long> sadd = pipeline.sadd(r.getOuterKey(), r.getInnerKey());
                        addResults.add(sadd);
                    }
                    else
                    {
                        final Response<Long> srem = pipeline.srem(r.getOuterKey(), r.getInnerKey());
                        removeResults.add(srem);
                    }
                }

                pipeline.sync();

                if (log.isDebugEnabled())
                    log.debug("All records processed for this batch.");

                final long[] updateResultCounts = updateResults(addResults, removeResults);

                aCurrentStats.addInsertCount(updateResultCounts[0]);
                aCurrentStats.addUpdateCount(updateResultCounts[1]);
                aCurrentStats.addDeleteCount(updateResultCounts[2]);
                aCurrentStats.addDeleteFailCount(updateResultCounts[3]);
                aCurrentStats.addInvalidOperationCount(updateResultCounts[4]);
            }
            finally
            {

                try
                {
                    if (pipeline != null)
                        pipeline.close();
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
                if (con != null)
                    con.close();
            }
        }
    }

    private static long[] updateResults(
            List<Response<Long>> aAddResults,
            List<Response<Long>> aRemoveResults)
    {
        int insertCount = 0, updateCount = 0, deleteCount = 0, deleteFailCount = 0, invalidOperationCount = 0;
        for (final Response<Long> longValue : aAddResults)
            if (longValue.get().longValue() == 1)
                insertCount++;
            else
                if (longValue.get().longValue() == 0)
                    updateCount++;
                else
                    invalidOperationCount++;

        for (final Response<Long> longValue : aRemoveResults)
            if (longValue.get().longValue() == 1)
                deleteCount++;
            else
                if (longValue.get().longValue() == 0)
                    deleteFailCount++;
                else
                    invalidOperationCount++;

        return new long[]
        { insertCount, updateCount, deleteCount, deleteFailCount, invalidOperationCount };
    }

    /**
     * Print the statistics of the files processed.
     *
     * @param aProcessResults
     *                        - A {@link List} of {@link FileStats} to be
     *                        printed.
     * @param aProcessType
     *                        - A <code>String</code> representing process Type.
     *                        <code>International</code> / <code>Domestic</code>.
     */
    public static void printStatistics(
            List<FileStats> aProcessResults,
            String aProcessType)
    {
        for (final FileStats stats : aProcessResults)
            printDetails(stats, aProcessType);
    }

    private static void printDetails(
            FileStats aStats,
            String aProcessType)
    {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
        log.info("File Name              : '" + aStats.getFileName() + "'");
        log.info("Process Type           : '" + aProcessType + "'");
        log.info("Process Start Time     : '" + sdf.format(new Date(aStats.getProcessStartTime())) + "'");
        log.info("Process End Time       : '" + sdf.format(new Date(aStats.getProcessEndTime())) + "'");
        log.info("Total Count            : '" + aStats.getTotalRecords() + "'");
        log.info("Valid Count            : '" + aStats.getValidRecords() + "'");
        log.info("Invalid Count          : '" + aStats.getInvalidRecords() + "'");
        log.info("Inserted Count         : '" + aStats.getInsertCount() + "'");
        log.info("Updated Count          : '" + aStats.getUpdateCount() + "'");
        log.info("Deleted Count          : '" + aStats.getDeleteCount() + "'");
        log.info("Undeleted Count        : '" + aStats.getDeleteFailCount() + "'");
        log.info("Failed Operation Count : '" + aStats.getInvalidOperationCount() + "'");
        log.info("****************************************************************************************************");
    }

    public static void moveToProcessedFolder(
            File aCurrentFile)
    {
        final File parent          = aCurrentFile.getParentFile();
        final File processedFolder = new File(parent.getAbsoluteFile() + File.separator + "processed");

        if (log.isDebugEnabled())
            log.debug("Current File : '" + aCurrentFile.getAbsolutePath() + "', Parent Folder : '" + parent.getAbsolutePath() + "', Process Folder : '" + processedFolder + "'");

        if (!processedFolder.exists())
        {
            final boolean mkdirs = processedFolder.mkdirs();

            if (!mkdirs)
            {
                log.error("Unable to create the processed folder. Cannot move the file to : '" + processedFolder.getAbsolutePath() + "'");
                return;
            }
        }

        if (!processedFolder.canWrite())
        {
            log.error("Processed folder is write protected. Cannot move the file to : '" + processedFolder.getAbsolutePath() + "'");
            return;
        }
        final SimpleDateFormat sdf         = new SimpleDateFormat("_yyyyMMddHHmmssSSS_");
        final String           newFileName = processedFolder + File.separator + aCurrentFile.getName() + sdf.format(new Date()) + "done";
        final boolean          renameTo    = aCurrentFile.renameTo(new File(newFileName));

        if (log.isDebugEnabled())
            log.debug("Current File : '" + aCurrentFile.getAbsolutePath() + " is renamed to : '" + newFileName + "'. Rename status : '" + renameTo + "'");

        if (!renameTo)
            log.warn("Unable to move the file to the processed folder. Filename : '" + aCurrentFile.getAbsolutePath() + "'. new filename : '" + newFileName + "'");
    }

}
