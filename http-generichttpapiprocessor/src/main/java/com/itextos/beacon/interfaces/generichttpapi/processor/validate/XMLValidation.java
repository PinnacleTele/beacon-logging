package com.itextos.beacon.interfaces.generichttpapi.processor.validate;

import java.io.File;
import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.http.generichttpapi.common.utils.APIConstants;

public class XMLValidation
{

    private static final Log log = LogFactory.getLog(XMLValidation.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final XMLValidation INSTANCE = new XMLValidation();

    }

    public static XMLValidation getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private File          mSchemaFile = null;
    private SchemaFactory factory     = null;
    private Schema        schema      = null;

    private XMLValidation()
    {

        try
        {
            mSchemaFile = new File(APIConstants.XML_SCHEMA_PATH);
            factory     = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

            // factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            // factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            if (log.isDebugEnabled())
                log.debug("Schema file path  '" + mSchemaFile + "'");
            schema = factory.newSchema(mSchemaFile);
        }
        catch (final Exception e)
        {
            log.error("Exception while Loading XML Schema", e);
        }
    }

    public void isXMLValid(
            String aRequestXml)
            throws Exception
    {
        final Validator lValidator = schema.newValidator();
        // lValidator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        // lValidator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        final Source    lSource    = new StreamSource(new StringReader(aRequestXml));
        lValidator.validate(lSource);
    }

}