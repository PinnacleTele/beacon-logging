package com.itextos.beacon.commonlib.shortcodegenerator.filecounter;

import java.util.Comparator;

public class FilenameComparator
        implements
        Comparator<String>
{

    @Override
    public int compare(
            String aO1,
            String aO2)
    {
        if (aO1 == null)
            return 1;
        if (aO2 == null)
            return -1;

        return getFileNameIndex(aO1) - getFileNameIndex(aO2);
    }

    private static int getFileNameIndex(
            String fileName)
    {
        final int indexStart = fileName.lastIndexOf('_');
        final int indexEnd   = fileName.lastIndexOf('.');
        return Integer.parseInt(fileName.substring(indexStart + 1, indexEnd));
    }

}