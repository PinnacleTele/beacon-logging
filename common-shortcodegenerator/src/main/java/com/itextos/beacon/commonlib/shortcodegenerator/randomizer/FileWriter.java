package com.itextos.beacon.commonlib.shortcodegenerator.randomizer;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itextos.beacon.commonlib.shortcodegenerator.randomizer.util.PropertyReader;

public class FileWriter
        implements
        Runnable
{

    private static final Logger log = LogManager.getLogger(FileWriter.class);

    private final List<String>  toWrite;
    private final String        fileNameAlone;

    public FileWriter(
            List<String> aWriteData,
            String aFileNameAlone)
    {
        toWrite       = aWriteData;
        fileNameAlone = aFileNameAlone;
    }

    @Override
    public void run()
    {
        final String fullFileName = PropertyReader.getInstance().getOutputFolder() + fileNameAlone;

        try
        {
            // log.debug("Data to write in file : '" + file + "' size : '" + toWrite.size()
            // + "'");

            try (

                    final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(fullFileName))));)
            {
                toWrite.stream().forEach(data -> {

                    try
                    {
                        writer.write(data);
                        writer.newLine();
                    }
                    catch (final IOException e)
                    {
                        log.error("Exception while writing the data into file '" + fullFileName + "' Exiting the application.", e);
                        System.exit(-1);
                    }
                });
                writer.flush();
            }

            log.debug("Completed writing file '" + fileNameAlone + "'");

            InformationHolder.getInstance().updateWriteInfo(fileNameAlone, toWrite.size());
        }
        catch (final IOException e)
        {
            log.error("Exception while writing the data into file '" + fullFileName + "' Exiting the application.", e);
            System.exit(-1);
        }
    }

}