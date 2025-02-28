package com.itextos.beacon.http.generichttpapi.common.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.messageidentifier.MessageIdentifier;
import com.itextos.beacon.http.generichttpapi.common.data.QueueObject;

public class FileGenUtil
{

    private static final Log log = LogFactory.getLog(FileGenUtil.class);

    private FileGenUtil()
    {}

    public static boolean storeInFile(
            QueueObject aQueueObject)
    {
        final String lReqFilePath = APIConstants.REQUEST_FILE_PATH + APIConstants.CLUSTER_INSTANCE + File.separator;

        if (log.isDebugEnabled())
            log.debug("Kafka is not reachable, stored in file " + lReqFilePath);

        final File lFileFolder = new File(lReqFilePath);

        if (!lFileFolder.exists())
        {
            final boolean lMkdirs = lFileFolder.mkdirs();

            if (!lMkdirs)
            {
                log.error("Unable to create directory " + lFileFolder.getAbsolutePath() + ". Please check the file permission.");
                return false;
            }
        }

        try
        {
            final String lAbsPath = lReqFilePath + MessageIdentifier.getInstance().getAppInstanceId() + "-" + aQueueObject.getMid() + ".txt";

            if (log.isDebugEnabled())
                log.debug("request is stored in file as " + lAbsPath);

            final List<String> lines = Arrays.asList(//
                    setStxEtx(aQueueObject.getReqType()),//
                    setStxEtx(aQueueObject.getCustIp()), //
                    setStxEtx(aQueueObject.getMid()), //
                    setStxEtx(aQueueObject.getClientId()), //
                    setStxEtx(aQueueObject.getCluster()), //
                    setStxEtx(aQueueObject.getMsgType()), //
                    setStxEtx(Long.toString(aQueueObject.getRequestedTime())), //
                    setStxEtx(aQueueObject.getRequestMag()));

            final Path         lFile = Paths.get(lAbsPath);

            Files.write(lFile, lines, StandardCharsets.UTF_8);
        }
        catch (final IOException e)
        {
            log.error("Error storing in file", e);
        }
        return true;
    }

    public static Set<String> listFiles(
            String dir)
            throws IOException
    {
        final Set<String> lFileList       = new HashSet<>();
        final File        folder          = new File(dir);
        final boolean     createDirectory = true;

        if (!folder.exists())
        {
            if (log.isDebugEnabled())
                log.debug(folder.getAbsolutePath() + " does not exists.");

            if (createDirectory)
            {
                final boolean lMkdirs = folder.mkdirs();

                if (!lMkdirs)
                {
                    log.error("Unable to create directory " + folder.getAbsolutePath() + ". Please check the file permission.");
                    return lFileList;
                }
            }
            else
            {
                log.error("Unable to find directory " + folder.getAbsolutePath() + ". Please check for it.");
                return lFileList;
            }
        }

        try (

                DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir)))
        {
            for (final Path lPath : stream)
                if (!Files.isDirectory(lPath))
                    lFileList.add(lPath.getFileName().toString());
        }
        return lFileList;
    }

    public static String readFileContent(
            String aFilePath)
    {

        try
        {
            final byte[] lReadAllBytes = Files.readAllBytes(Paths.get(aFilePath));
            return new String(lReadAllBytes, StandardCharsets.UTF_8);
        }
        catch (final IOException exc)
        {
            log.error("Exception in reading file " + aFilePath, exc);
        }
        return null;
    }

    private static String setStxEtx(
            String aValue)
    {
        return APIConstants.SUFFIX + aValue + APIConstants.PREFIX;
    }

    public static void fileMove(
            String aSourceFolder,
            String aTargetFolder,
            String aFilename)
    {

        try
        {
            final File lTargetfolder = new File(aTargetFolder);

            if (!lTargetfolder.exists())
                lTargetfolder.mkdirs();

            final Path lSourcePath = Paths.get(aSourceFolder);
            final Path lTargetPath = Paths.get(aTargetFolder + aFilename);

            Files.move(lSourcePath, lTargetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (final IOException e)
        {
            log.error("Exception occer while file moved to " + aTargetFolder + aFilename, e);
        }
    }

}
