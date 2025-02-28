package com.itextos.beacon.commonlib.stringprocessor.dto;

public class ValidatorParams
{

    private final int           paramSeqNo;
    private final String        mwConstantName;
    private final String        mwAlternativeConstantName;
    private final String        defaultValue;
    private final ParamDataType dataType;
    private final String        dataFormat;
    private final String        dataValidation;
    private final String        droolsValidationFilePath;

    public ValidatorParams(
            int aParamSeqNo,
            String aMwConstantName,
            String aMwAlternativeConstantName,
            String aDefaultValue,
            ParamDataType aParamDataType,
            String aDataFormat,
            String aDataValidation,
            String aDroolsValidationFilePath)
    {
        paramSeqNo                = aParamSeqNo;
        mwConstantName            = aMwConstantName;
        mwAlternativeConstantName = aMwAlternativeConstantName;
        defaultValue              = aDefaultValue;
        dataType                  = aParamDataType;
        dataFormat                = aDataFormat;
        dataValidation            = aDataValidation;
        droolsValidationFilePath  = aDroolsValidationFilePath;
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
        return "ValidatorParams [paramSeqNo=" + paramSeqNo + ", mwConstantName=" + mwConstantName + ", mwAlternativeConstantName=" + mwAlternativeConstantName + ", defaultValue=" + defaultValue
                + ", dataType=" + dataType + ", dataFormat=" + dataFormat + ", dataValidation=" + dataValidation + ", droolsValidationFilePath=" + droolsValidationFilePath + "]";
    }

}