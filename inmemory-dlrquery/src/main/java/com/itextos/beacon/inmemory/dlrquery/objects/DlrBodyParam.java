package com.itextos.beacon.inmemory.dlrquery.objects;

public class DlrBodyParam
{

    private final int         mParamSeqNo;
    private final String      mConstantName;
    private final String      mAlternativeConstantName;
    private final String      mDefaultValue;
    private final DlrDataType mDlrDataType;
    private final String      mDataFormat;
    private final String      mDataValidation;
    private final String      mDroolsDataPath;

    public DlrBodyParam(
            int aParamSeqNo,
            String aConstantName,
            String aAlternativeConstantName,
            String aDefaultValue,
            DlrDataType aDlrDataType,
            String aDataFormat,
            String aDataValidation,
            String aDroolsDataPath)
    {
        super();
        mParamSeqNo              = aParamSeqNo;
        mConstantName            = aConstantName;
        mAlternativeConstantName = aAlternativeConstantName;
        mDefaultValue            = aDefaultValue;
        mDlrDataType             = aDlrDataType;
        mDataFormat              = aDataFormat;
        mDataValidation          = aDataValidation;
        mDroolsDataPath          = aDroolsDataPath;
    }

    public String getConstantName()
    {
        return mConstantName;
    }

    public String getAlternativeConstantName()
    {
        return mAlternativeConstantName;
    }

    public String getDefaultValue()
    {
        return mDefaultValue;
    }

    public DlrDataType getDlrDataType()
    {
        return mDlrDataType;
    }

    public String getDataFormat()
    {
        return mDataFormat;
    }

    public String getDataValidation()
    {
        return mDataValidation;
    }

    public String getDroolsDataPath()
    {
        return mDroolsDataPath;
    }

    public int getParamSeqNo()
    {
        return mParamSeqNo;
    }

    @Override
    public String toString()
    {
        return "DlrBodyParam [mParamSeqNo=" + mParamSeqNo + ", mConstantName=" + mConstantName + ", mAlternativeConstantName=" + mAlternativeConstantName + ", mDefaultValue=" + mDefaultValue
                + ", mDlrDataType=" + mDlrDataType + ", mDataFormat=" + mDataFormat + ", mDataValidation=" + mDataValidation + ", mDroolsDataPath=" + mDroolsDataPath + "]";
    }

}