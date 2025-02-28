package com.itextos.beacon.commonlib.messageidentifier;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

class MessageIdentifierProperties
{

    private static final Log    log                                              = LogFactory.getLog(MessageIdentifierProperties.class);

    private static final int    DEFAULT_REDIS_UPDATE_INTERVAL_SECONDS            = 30;
    private static final int    DEFAULT_REDIS_EXPIRY_INTERVAL_MINUTES            = 30;
    private static final long   ONE_SEC                                          = 1000;
    private static final long   ONE_MINUTE                                       = 60 * ONE_SEC;
    private static final String MESSAGEIDENTIFIER_INTERFACE_TYPES                = "messageidentifier.interface.types";
    private static final String MESSAGEIDENTIFIER_STATUS_UPDATE_INTERVAL_SECONDS = "messageidentifier.status.update.interval.seconds";
    private static final String MESSAGEIDENTIFIER_EXPIRY_INTERVAL_MINUTES        = "messageidentifier.expiry.interval.minutes";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final MessageIdentifierProperties INSTANCE = new MessageIdentifierProperties();

    }

    static MessageIdentifierProperties getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final List<String> interfaceTypes;
    private long               statusUpdateSecs;
    private long               expiryMinutes;

    private MessageIdentifierProperties()
    {
        final PropertiesConfiguration pc   = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.MESSAGE_ID_GENERATOR_PROPERTIES, true);

        final List<Object>            list = pc.getList(MESSAGEIDENTIFIER_INTERFACE_TYPES);

        if ((list == null) || (list.isEmpty())) {
            //throw new ItextosRuntimeException("Interface types not defined.");
        }
        interfaceTypes = new ArrayList<>(list.size());

        for (final Object s : list)
            interfaceTypes.add(((String) s).toLowerCase());

        String temp = pc.getString(MESSAGEIDENTIFIER_STATUS_UPDATE_INTERVAL_SECONDS);

        try
        {
            statusUpdateSecs = Integer.parseInt(temp) * ONE_SEC;
        }
        catch (final Exception e)
        {
            log.error("Invalid update interval time has specified. Value in Property file : '" + temp + "'. Using default value " + DEFAULT_REDIS_UPDATE_INTERVAL_SECONDS + " for it. ");
            statusUpdateSecs = DEFAULT_REDIS_UPDATE_INTERVAL_SECONDS * ONE_SEC;
        }

        temp = pc.getString(MESSAGEIDENTIFIER_EXPIRY_INTERVAL_MINUTES);

        try
        {
            expiryMinutes = Integer.parseInt(temp) * ONE_MINUTE;
        }
        catch (final Exception e)
        {
            log.error("Invalid expiry interval time has specified. Value in Property file : '" + temp + "'. Using default value " + DEFAULT_REDIS_UPDATE_INTERVAL_SECONDS + " for it. ");
            expiryMinutes = DEFAULT_REDIS_EXPIRY_INTERVAL_MINUTES * ONE_MINUTE;
        }

        if (log.isDebugEnabled())
        {
            log.debug("Defined Interface Types                    : '" + interfaceTypes + "'");
            log.debug("Message Identifier status update seconds   : '" + statusUpdateSecs + "'");
            log.debug("Message Identifier Expiry interval minutes : '" + expiryMinutes + "'");
        }
    }

    boolean isValidInterfaceType(
            final InterfaceType aInterfaceType)
    {
        final boolean isValid = interfaceTypes.contains(aInterfaceType.getKey());

        if (log.isDebugEnabled())
            log.debug("Interface Type : '" + aInterfaceType + "' Is Valid Type : '" + isValid + "'");

        return isValid;
    }

    long getStatusUpdateSeconds()
    {
        return statusUpdateSecs;
    }

    long getExpiryMinutesInMillis()
    {
        return expiryMinutes;
    }

}