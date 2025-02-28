package com.itextos.beacon.commonlib.message.serialize;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.serialization.Deserializer;

public class ValueDeserializer
        implements
        Deserializer<Object>
{

    private static final Log log = LogFactory.getLog(ValueDeserializer.class);

    @Override
    public Object deserialize(
            String aTopic,
            byte[] aData)
    {
        Object result = null;

        if (aData != null)
            try (
                    ByteArrayInputStream b = new ByteArrayInputStream(aData);
                    ObjectInputStream o = new ObjectInputStream(b);)
            {
                result = o.readObject();
            }
            catch (final Exception exp)
            {
                log.error("Problem deserailzing Key in Topic '" + aTopic + "'", exp);
            }
        return result;
    }

}