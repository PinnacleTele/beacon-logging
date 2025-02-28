package com.itextos.beacon.http.generichttpapi.common.data.xmlparser;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com.java.files package.
 * <p>
 * An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups. Factory methods for each of these are
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory
{

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema
     * derived classes for package: com.java.files
     */
    public ObjectFactory()
    {}

    /**
     * Create an instance of {@link Messagerequest }
     */
    public Messagerequest createMessagerequest()
    {
        return new Messagerequest();
    }

    /**
     * Create an instance of {@link Messagerequest.Messages }
     */
    public Messagerequest.Messages createMessagerequestMessages()
    {
        return new Messagerequest.Messages();
    }

    /**
     * Create an instance of {@link Messagerequest.Messages.Destination }
     */
    public Messagerequest.Messages.Destination createMessagerequestMessagesDestination()
    {
        return new Messagerequest.Messages.Destination();
    }

    /**
     * Create an instance of {@link Messagerequest.Messages.TemplateValues }
     */
    public Messagerequest.Messages.TemplateValues createMessagerequestMessagesTemplateValues()
    {
        return new Messagerequest.Messages.TemplateValues();
    }

}
