package com.itextos.beacon.inmemory.elasticsearchcolumn;

public class ESIndexColMapValue
{

    public String  ColumnName;
    public String  MappedName;
    public String  ColumnType;
    public String  DefaultValue;
    public boolean CIColumnRequired;

    public ESIndexColMapValue(
            String pColName,
            String pMapName,
            String pColType,
            String pDefault,
            boolean pCIReq)
    {
        this.ColumnName       = pColName;
        this.MappedName       = pMapName;
        this.ColumnType       = pColType;
        this.DefaultValue     = pDefault;
        this.CIColumnRequired = pCIReq;
    }

}
