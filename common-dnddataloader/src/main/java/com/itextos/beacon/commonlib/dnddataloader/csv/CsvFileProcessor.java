package com.itextos.beacon.commonlib.dnddataloader.csv;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.dnddataloader.common.CountHolder;
import com.itextos.beacon.commonlib.dnddataloader.common.DndInfo;
import com.itextos.beacon.commonlib.dnddataloader.common.InMemoryDataHolder;
import com.itextos.beacon.commonlib.dnddataloader.enums.DndAction;
import com.itextos.beacon.commonlib.dnddataloader.util.DndPropertyProvider;

class CsvFileProcessor
{

    private static final Log log                  = LogFactory.getLog(CsvFileProcessor.class);

    private static final int CSV_INDEX_DEST       = 0;
    private static final int CSV_INDEX_PREFERENCE = 1;
    private static final int CSV_INDEX_ACTION     = 2;

    private final File       csvFile;
    private int              recordsCount;

    CsvFileProcessor(
            File aCSVFile)
    {
        csvFile = aCSVFile;
    }

    void process()
    {
        if (log.isInfoEnabled())
            log.info("Process started for file : " + csvFile.getAbsolutePath());

        boolean isSuccess = true;
        CountHolder.getInstance().setStartTime(csvFile);

        try (
                // Skip the heading
                CSVParser parser = new CSVParser(new FileReader(csvFile), CSVFormat.DEFAULT.withFirstRecordAsHeader().withSkipHeaderRecord()))
        {
            final Iterator<CSVRecord> lIterator = parser.iterator();
            while (lIterator.hasNext())
                processRecord(lIterator.next());
        }
        catch (final Exception e)
        {
            isSuccess = false;
            log.error("Exception while parsing the CSV file.", e);
        }
        finally
        {
            if (log.isInfoEnabled())
                log.info("Process completed for file : " + csvFile.getAbsolutePath());

            CountHolder.getInstance().setEndTime(csvFile, isSuccess, recordsCount);
            moveFile(csvFile, isSuccess);
        }
    }

    private void processRecord(
            CSVRecord aCurRecord)
            throws Exception
    {
        final String dest   = aCurRecord.get(CSV_INDEX_DEST);
        final String pref   = aCurRecord.get(CSV_INDEX_PREFERENCE);
        final String action = aCurRecord.get(CSV_INDEX_ACTION);

        recordsCount++;

        final DndInfo dndInfo = new DndInfo(dest, pref, action);

        if ((DndAction.INVALID == dndInfo.getDndAction()) || (DndAction.INVALID_NUMBER == dndInfo.getDndAction()))
            log.error(csvFile.getName() + " : " + recordsCount + " : Invalid Info " + dndInfo);

        InMemoryDataHolder.getInstance().addData(dndInfo);
    }

    private static void moveFile(
            File aCSVFile,
            boolean aIsSuccess)
    {
        if (log.isDebugEnabled())
            log.debug("Moving the processed file.");

        File    destFolder       = new File(DndPropertyProvider.getInstance().getCsvDestinationFilePath());
        boolean destFolderExists = destFolder.exists();

        if (log.isDebugEnabled())
            log.debug("Destination path to move : " + destFolder.getAbsolutePath() + ". Is Exists " + destFolderExists);

        if (!destFolderExists)
        {
            destFolderExists = destFolder.mkdirs();
            log.info("Destination folder creation status : " + destFolderExists);

            if (!destFolderExists)
            {
                if (log.isInfoEnabled())
                    log.info("As unable to create destination folder using source folder as destination folder");
                destFolder = aCSVFile.getParentFile();
            }
        }

        final String newFileName  = new SimpleDateFormat("yyyyMMddHHmmss_").format(new Date()) + aCSVFile.getName() + (aIsSuccess ? ".done" : ".error");
        final File   destFile     = new File(destFolder.getAbsoluteFile() + File.separator + newFileName);
        boolean      renameResult = true;

        try
        {
            renameResult = aCSVFile.renameTo(destFile);
        }
        catch (final Exception e)
        {
            renameResult = false;
            log.error("Error while renaming the file ", e);
        }
        if (log.isInfoEnabled())
            log.info("'" + aCSVFile.getAbsoluteFile() + "' renamed to '" + destFile.getAbsolutePath() + "'. Status : " + renameResult);
    }

}