package com.itextos.beacon.platform.faillistutil.util;

import java.io.File;
import java.io.FileFilter;

/**
 * A class to filter the CSV files alone. It will not fall through the sub
 * folder.
 */
public class CSVFileFilter
        implements
        FileFilter
{

    @Override
    public boolean accept(
            File aFile)
    {
        if (aFile.isFile() && aFile.getName().toLowerCase().endsWith(".csv"))
            return true;
        return false;
    }

}
