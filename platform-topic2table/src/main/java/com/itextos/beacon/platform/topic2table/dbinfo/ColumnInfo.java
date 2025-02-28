package com.itextos.beacon.platform.topic2table.dbinfo;

public class ColumnInfo
{

    private final int      columnIndex;
    private final DataType columnDataType;
    private final String   columnName;
    private final int      columnLength;

    public ColumnInfo(
            int aColumnIndex,
            DataType aColumnDataType,
            String aColumnName,
            int aColumnLength)
    {
        columnIndex    = aColumnIndex;
        columnDataType = aColumnDataType;
        columnName     = aColumnName;
        columnLength   = aColumnLength;
    }

    public int getColumnIndex()
    {
        return columnIndex;
    }

    public DataType getColumnDataType()
    {
        return columnDataType;
    }

    public String getColumnName()
    {
        return columnName;
    }

    public int getColumnLength()
    {
        return columnLength;
    }

    @Override
    public String toString()
    {
        return "ColumnInfo [columnIndex=" + columnIndex + ", columnDataType=" + columnDataType + ", columnName=" + columnName + ", columnLength=" + columnLength + "]";
    }

}