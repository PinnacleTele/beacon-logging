package com.itextos.beacon.commonlib.utility.messagetypeidentifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MessageTypeIdentifier
{

    private static final Log log = LogFactory.getLog(MessageTypeIdentifier.class);

    private MessageTypeIdentifier()
    {}

    public static void checkForUcMessage(
            MessageTypeIdentifyObject aMessageIdentifyObject)
    {
        aMessageIdentifyObject.doInitial();

        // TODO Add the logic for the checking and update.
        // TODO Add the required Logs here.

        if (log.isDebugEnabled())
            log.debug("Message to validate " + aMessageIdentifyObject);

        aMessageIdentifyObject.setIsUcMessage(false);
        aMessageIdentifyObject.setUpdatedMessage(null);

        if (log.isDebugEnabled())
            log.debug("Affter validation Message " + aMessageIdentifyObject);
    }

}