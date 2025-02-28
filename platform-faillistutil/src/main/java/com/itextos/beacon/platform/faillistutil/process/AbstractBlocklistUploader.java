package com.itextos.beacon.platform.faillistutil.process;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.platform.faillistutil.util.FaillistConfig;
import com.itextos.beacon.platform.faillistutil.util.FaillistUtil;

/**
 * An abstract class to have the common methods to load list of blocked mobile
 * numbers for the International and Domestic
 */
public abstract class AbstractBlocklistUploader
        implements
        IFaillistUploader
{

    private static Log       log = LogFactory.getLog(AbstractBlocklistUploader.class);

    protected FaillistConfig blockListConfig;
    private List<File>       filesToProcess;

    public AbstractBlocklistUploader(
            FaillistConfig aBlockListConfig)
    {
        blockListConfig = aBlockListConfig;
    }

    @Override
    public void process()
    {
        getFiles();
        final List<FileStats> processResults = readFiles();
        FaillistUtil.printStatistics(processResults, getProcessType());
    }

    /**
     * Method to read the list of files from the folder specific to the
     * International / Domestic process. Folder Name to be specified in the
     * <code>intl.file.path</code> /
     * <code>domestic.file.path</code> in the <code>blacklist.properties</code> file
     */
    private void getFiles()
    {
        filesToProcess = FaillistUtil.getFiles(blockListConfig);
    }

    /**
     * Method to read the file content for all the files available in the folder and
     * return the Process Statistics of those files.
     * <p>
     * <b>Note:</b> Need to handle if there is any exception or error occurs.
     *
     * @return - A {@link List} of {@link FileStats} of the processed files.
     */
    private List<FileStats> readFiles()
    {
        if (filesToProcess.size() == 0)
            log.warn("No Files to process now for '" + getProcessType() + "'.");

        final List<FileStats> statsList = new ArrayList<>();
        FileStats             currentStats;

        for (final File currentFile : filesToProcess)
        {
            if (log.isDebugEnabled())
                log.debug("Start processing file : '" + currentFile.getAbsolutePath() + "'");

            currentStats = new FileStats(currentFile.getAbsolutePath());
            statsList.add(currentStats);

            FaillistUtil.readFile(currentFile, blockListConfig, currentStats);
            FaillistUtil.moveToProcessedFolder(currentFile);
        }

        return statsList;
    }

}