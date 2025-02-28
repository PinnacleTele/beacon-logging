package com.itextos.beacon.commonlib.dnddataloader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.dnddataloader.common.CountHolder;
import com.itextos.beacon.commonlib.dnddataloader.common.InmemoryDataPrinter;
import com.itextos.beacon.commonlib.dnddataloader.compare.DndRedisDatabaseComparision;
import com.itextos.beacon.commonlib.dnddataloader.csv.Csv2RedisThreadBased;
import com.itextos.beacon.commonlib.dnddataloader.db.Db2RedisThreadBased;
import com.itextos.beacon.commonlib.dnddataloader.redis.DndDataOperation;
import com.itextos.beacon.commonlib.dnddataloader.redis.DndRedisCountChecker;
import com.itextos.beacon.commonlib.dnddataloader.redis.RedisDataDeleter;
import com.itextos.beacon.commonlib.dnddataloader.redis.RedisDataPusher;
import com.itextos.beacon.commonlib.dnddataloader.util.DndPropertyProvider;

public class DndOperation
{

    private static final Log log = LogFactory.getLog(DndOperation.class);

    public static void main(
            String[] args)
    {
        int option = -1;

        try
        {
            option = Integer.parseInt(args[0]);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            log.error("Exception in DndOperation.", e);
            printUsage();
        }

        String                 operationType = null;
        List<DndDataOperation> loperations   = null;
        final long             startTime     = System.currentTimeMillis();

        switch (option)
        {
            case 1:
                log.info("Process to be taken care : Db 2 Redis");
                operationType = "DB 2 Redis";
                Db2RedisThreadBased.processDBRecords();
                loperations = startRedisPusher();
                break;

            case 2:
                log.info("Process to be taken care : CSV 2 Redis");
                operationType = "CSV File 2 Redis";
                final boolean lProcess = Csv2RedisThreadBased.process();
                if (lProcess)
                    loperations = startRedisPusher();
                break;

            case 3:
                log.info("Process to be taken care : DND Data print statistics from Redis");
                operationType = "Redis Count Check";
                DndRedisCountChecker.checkCountAndPrint();
                break;

            case 4:
                log.info("Process to be taken care : DND Data compare between DB and Redis");
                operationType = "DB vs Redis Compare";
                DndRedisDatabaseComparision.compare();
                break;

            default:
                log.info("Process to be taken care : Invalid option selected");
                operationType = "Invalid";
                printUsage();
        }

        if (loperations != null)
        {
            waitForCompletion(loperations);
            printStats(startTime, operationType);
        }
    }

    private static void waitForCompletion(
            List<DndDataOperation> aLoperations)
    {

        while (true)
        {
            boolean isCompleted = true;

            for (final DndDataOperation ddo : aLoperations)
            {
                isCompleted = isCompleted && ddo.isStopped();

                if (log.isDebugEnabled())
                    log.debug(ddo.getThreadName() + " " + ddo.isStopped());

                if (!isCompleted)
                    break;
            }

            if (isCompleted)
                break;

            try
            {
                Thread.sleep(1 * 1000L);
            }
            catch (final InterruptedException e)
            {}
        }
    }

    private static void printStats(
            long aStartTime,
            String aOperationType)
    {
        final long endTime = System.currentTimeMillis();
        log.fatal("Redis Operation results.");
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm:ss.SSS");

        log.fatal("************************");
        log.fatal("Opertion Type            : '" + aOperationType + "'");
        log.fatal("Start Time               : '" + sdf.format(new Date(aStartTime)) + "'");
        log.fatal("End Time                 : '" + sdf.format(new Date(endTime)) + "'");
        log.fatal("Time Taken (Secs)        : '" + ((endTime - aStartTime) / 1000.0));
        log.fatal("Opertion Type            : '" + aOperationType + "'");
        log.fatal("Total Request            : " + CountHolder.getInstance().getTotalReq());
        log.fatal("Add Update Request       : " + CountHolder.getInstance().getAddUpdateReq());
        log.fatal("Delete Request           : " + CountHolder.getInstance().getDeleteReq());
        log.fatal("Invalid Mobile No        : " + CountHolder.getInstance().getInvalidMobileNumberReq());
        log.fatal("Invalid Operation        : " + CountHolder.getInstance().getInvalidOperationReq());
        log.fatal("Redis Added              : " + CountHolder.getInstance().getRedisOperationAdd());
        log.fatal("Redis Updated            : " + CountHolder.getInstance().getRedisOperationUpdate());
        log.fatal("Redis Deleted            : " + CountHolder.getInstance().getRedisOperationDelete());
        log.fatal("Redis Data Not Available : " + CountHolder.getInstance().getRedisOperationNotAvailable());
    }

    private static List<DndDataOperation> startRedisPusher()
    {
        final InmemoryDataPrinter idPrinter   = new InmemoryDataPrinter();
        final Thread              printThread = new Thread(idPrinter, "idPrinter");
        printThread.start();

        final int                    lRedisWriterThreadCount = DndPropertyProvider.getInstance().getRedisWriterThreadCount();

        final List<DndDataOperation> operationsList          = new ArrayList<>();

        for (int index = 0; index < lRedisWriterThreadCount; index++)
        {
            final DndDataOperation pushOperations = new RedisDataPusher("DataPusher-" + (index + 1));
            operationsList.add(pushOperations);

            final Thread th = new Thread(pushOperations, "PushOperations-" + (index + 1));
            th.start();
        }

        int deleteThreadCount = lRedisWriterThreadCount / 2;
        deleteThreadCount = deleteThreadCount <= 0 ? 1 : deleteThreadCount;

        for (int index = 0; index < deleteThreadCount; index++)
        {
            final DndDataOperation pushOperations = new RedisDataDeleter("DataDeleter-" + (index + 1));
            operationsList.add(pushOperations);

            final Thread th = new Thread(pushOperations, "DeleteOperations-" + (index + 1));
            th.start();
        }
        return operationsList;
    }

    private static void printUsage()
    {
        log.info("Usage:");
        log.info("<JAVA_HOME>\\java -classpath <all_req_jars>;dndoperations.jar com.itextos.beacon.commonlib.dnd.DndOperation < 1 | 2 | 3 | 4 >");
        log.info("Note:");
        log.info("1 --> DB2Redis Process");
        log.info("2 --> CSV2Redis Process");
        log.info("3 --> Print Redis statistics");
        log.info("4 --> Compare Data between Database and Redis");

        System.out.println("Usage:");
        System.out.println("<JAVA_HOME>\\java -classpath <all_req_jars>;dndoperations.jar com.itextos.beacon.commonlib.dnd.DndOperation < 1 | 2 | 3 | 4 >");
        System.out.println("Note:");
        System.out.println("1 --> DB2Redis Process");
        System.out.println("2 --> CSV2Redis Process");
        System.out.println("3 --> Print Redis statistics");
        System.out.println("4 --> Compare Data between Database and Redis");
    }

}
