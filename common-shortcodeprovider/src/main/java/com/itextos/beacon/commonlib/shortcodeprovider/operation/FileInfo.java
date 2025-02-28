package com.itextos.beacon.commonlib.shortcodeprovider.operation;

public class FileInfo
{

    private final String id;
    private final String fileName;

    FileInfo(
            String aId,
            String aFileName)
    {
        id       = aId;
        fileName = aFileName;
    }

    public String getId()
    {
        return id;
    }

    public String getFileName()
    {
        return fileName;
    }

    @Override
    public String toString()
    {
        return "FileInfo [id=" + id + ", fileName=" + fileName + "]";
    }

}