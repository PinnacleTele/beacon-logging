package com.itextos.beacon.interfaces.generichttpapi.processor.response;

import com.itextos.beacon.http.generichttpapi.common.data.response.ResponseObject;
import com.itextos.beacon.http.generichttpapi.common.utils.Utility;

public class GenerateQueryStringResponse
        extends
        GenerateAbstractResponse
{

    public GenerateQueryStringResponse(
            String aIP)
    {
        super(aIP);
    }

    @Override
    protected String getErrorString()
    {
        return Utility.getJsonErrorResponse(getResponseDateTimeString());
    }

    @Override
    protected String getGeneralReqTypeSepecificResponse(
            ResponseObject aRo)
    {
        return Utility.getGeneralJsonResponse(aRo);
    }

}