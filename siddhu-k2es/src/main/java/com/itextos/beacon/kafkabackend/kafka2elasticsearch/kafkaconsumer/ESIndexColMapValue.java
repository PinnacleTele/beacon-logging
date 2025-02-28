package com.itextos.beacon.kafkabackend.kafka2elasticsearch.kafkaconsumer;

public class ESIndexColMapValue
{

    String  ColumnName;
    String  MappedName;
    String  ColumnType;
    String  DefaultValue;
    boolean CIColumnRequired;

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
