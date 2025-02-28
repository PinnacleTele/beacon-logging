package com.itextos.beacon.queryprocessor.paramvalidator;

public class ValidatorResponse
{

    public String StatusMessage;
    public int    StatusCode;

    public ValidatorResponse(
            String p_message,
            int badRequest400)
    {
        StatusMessage = p_message;
        StatusCode    = badRequest400;
    }

}
