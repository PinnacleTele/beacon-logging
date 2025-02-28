package com.itextos.beacon.commonlib.stringprocessor.dto;

import java.util.List;

public class ValidatorMaster
{

    // private final long id;
    private final boolean               encodeRequired;
    private final String                bodyTemplate;
    private final String                bodyHeader;
    private final String                bodyFooter;
    private final String                batchBodyDelimiter;

    private final List<ValidatorParams> validatorParams;

    public ValidatorMaster(
            boolean aEncodeRequired,
            String aBodyTemplate,
            String aBodyHeader,
            String aBodyFooter,
            String aBatchBodyDelimiter,
            List<ValidatorParams> aValidatorParams)
    {
        super();
        encodeRequired     = aEncodeRequired;
        bodyTemplate       = aBodyTemplate;
        bodyHeader         = aBodyHeader;
        bodyFooter         = aBodyFooter;
        batchBodyDelimiter = aBatchBodyDelimiter;
        validatorParams    = aValidatorParams;
    }

    public boolean isEncodeRequired()
    {
        return encodeRequired;
    }

    public String getBodyTemplate()
    {
        return bodyTemplate;
    }

    public String getBodyHeader()
    {
        return bodyHeader;
    }

    public String getBodyFooter()
    {
        return bodyFooter;
    }

    public String getBatchBodyDelimiter()
    {
        return batchBodyDelimiter;
    }

    public List<ValidatorParams> getValidatorParams()
    {
        return validatorParams;
    }

    @Override
    public String toString()
    {
        return "ValidatorMaster [encodeRequired=" + encodeRequired + ", bodyTemplate=" + bodyTemplate + ", bodyHeader=" + bodyHeader + ", bodyFooter=" + bodyFooter + ", batchBodyDelimiter="
                + batchBodyDelimiter + ", validatorParams=" + validatorParams + "]";
    }

}