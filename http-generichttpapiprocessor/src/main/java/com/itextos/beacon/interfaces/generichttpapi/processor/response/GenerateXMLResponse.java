package com.itextos.beacon.interfaces.generichttpapi.processor.response;

import javax.xml.bind.JAXBException;

import com.itextos.beacon.http.generichttpapi.common.data.response.ResponseObject;
import com.itextos.beacon.http.generichttpapi.common.utils.Utility;

public class GenerateXMLResponse
        extends
        GenerateAbstractResponse
{

    public GenerateXMLResponse(
            String aIP)
    {
        super(aIP);
    }

    @Override
    protected String getErrorString()
    {
        return Utility.getXmlErrorResponse(getResponseDateTimeString());
    }

    @Override
    protected String getGeneralReqTypeSepecificResponse(
            ResponseObject aRo)
            throws JAXBException
    {
        return Utility.getGeneralXmlResponse(aRo);
    }

}