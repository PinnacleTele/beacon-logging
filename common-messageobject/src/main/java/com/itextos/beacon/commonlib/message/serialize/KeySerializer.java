package com.itextos.beacon.commonlib.message.serialize;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.serialization.Serializer;

public class KeySerializer
        implements
        Serializer<Object>
{

    private static final Log log = LogFactory.getLog(KeySerializer.class);

    @Override
    public byte[] serialize(
            String aTopic,
            Object aData)
    {
        byte[] result = null;

        if (aData != null)
            try (
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    ObjectOutputStream o = new ObjectOutputStream(b);)
            {
                o.writeObject(aData);
                result = b.toByteArray();
            }
            catch (final Exception exp)
            {
                log.error("Problem serializing key '" + aTopic + "' in Topic '" + aTopic + "' due to...", exp);
            }
        return result;
    }

}