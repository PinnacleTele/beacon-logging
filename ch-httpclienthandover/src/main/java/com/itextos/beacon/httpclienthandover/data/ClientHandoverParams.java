package com.itextos.beacon.httpclienthandover.data;

public class ClientHandoverParams
{

    private final long          handoverId;
    private final int           sequenceNo;
    private final int           paramSeqNo;
    private final String        mwConstantName;
    private final String        mwAlternativeConstantName;
    private final String        defaultValue;
    private final ParamDataType dataType;
    private final String        dataFormat;
    private final String        dataValidation;
    private final String        droolsValidationFilePath;

    public ClientHandoverParams(
            long aHandoverId,
            int aSequenceNo,
            int aParamSeqNo,
            String aMwConstantName,
            String aMwAlternativeConstantName,
            String aDefaultValue,
            ParamDataType aParamDataType,
            String aDataFormat,
            String aDataValidation,
            String aDroolsValidationFilePath)
    {
        super();
        handoverId                = aHandoverId;
        sequenceNo                = aSequenceNo;
        paramSeqNo                = aParamSeqNo;
        mwConstantName            = aMwConstantName;
        mwAlternativeConstantName = aMwAlternativeConstantName;
        defaultValue              = aDefaultValue;
        dataType                  = aParamDataType;
        dataFormat                = aDataFormat;
        dataValidation            = aDataValidation;
        droolsValidationFilePath  = aDroolsValidationFilePath;
    }

    public long getHandoverId()
    {
        return handoverId;
    }

    public int getSequenceNo()
    {
        return sequenceNo;
    }

    public int getParamSeqNo()
    {
        return paramSeqNo;
    }

    public String getMwConstantName()
    {
        return mwConstantName;
    }

    public String getMwAlternativeConstantName()
    {
        return mwAlternativeConstantName;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public ParamDataType getDataType()
    {
        return dataType;
    }

    public String getDataFormat()
    {
        return dataFormat;
    }

    public String getDataValidation()
    {
        return dataValidation;
    }

    public String getDroolsValidationFilePath()
    {
        return droolsValidationFilePath;
    }

    @Override
    public String toString()
    {
        return "ClientHandoverParams [handoverId=" + handoverId + ", sequenceNo=" + sequenceNo + ", paramSeqNo=" + paramSeqNo + ", mwConstantName=" + mwConstantName + ", mwAlternativeConstantName="
                + mwAlternativeConstantName + ", defaultValue=" + defaultValue + ", dataType=" + dataType + ", dataFormat=" + dataFormat + ", dataValidation=" + dataValidation
                + ", droolsValidationFilePath=" + droolsValidationFilePath + "]";
    }

}