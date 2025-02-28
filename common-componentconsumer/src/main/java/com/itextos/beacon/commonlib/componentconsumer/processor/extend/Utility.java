package com.itextos.beacon.commonlib.componentconsumer.processor.extend;

import java.util.HashMap;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.kafkaservice.consumer.ConsumerInMemCollection;

public class Utility
{

    private static Map<String, String> messageTypeClassMap = new HashMap<>();

    static
    {
        messageTypeClassMap.put("AsyncRequestObject", "com.itextos.beacon.commonlib.message.AsyncRequestObject");
        messageTypeClassMap.put("AbstractMessage", "com.itextos.beacon.commonlib.message.AbstractMessage"); // May not be useful.
        messageTypeClassMap.put("BaseMessage", "com.itextos.beacon.commonlib.message.BaseMessage");// May not be useful.
        messageTypeClassMap.put("ClientHandoverObject", "com.itextos.beacon.commonlib.message.ClientHandoverObject");
        messageTypeClassMap.put("DeliveryObject", "com.itextos.beacon.commonlib.message.DeliveryObject");
        messageTypeClassMap.put("ErrorObject", "com.itextos.beacon.commonlib.message.ErrorObject");
        messageTypeClassMap.put("MessagePart", "com.itextos.beacon.commonlib.message.MessagePart");// May not be useful.
        messageTypeClassMap.put("MessageRequest", "com.itextos.beacon.commonlib.message.MessageRequest");
        messageTypeClassMap.put("SubmissionObject", "com.itextos.beacon.commonlib.message.SubmissionObject");// May not be useful.
    }

    private Utility()
    {}

    public static Class<Object>[] getDeclaredConstrutorArgumentTypes()
    {
        // protected final String mThreadName;
        // protected final Component mComponent;
        // protected final ClusterType mPlatformCluster;
        // protected final String mTopicName;
        // protected int mSleepInMillis;

        final Class[] constrcutorArgumentTypes = new Class[6];
        constrcutorArgumentTypes[0] = String.class;
        constrcutorArgumentTypes[1] = Component.class;
        constrcutorArgumentTypes[2] = ClusterType.class;
        constrcutorArgumentTypes[3] = String.class;
        constrcutorArgumentTypes[4] = ConsumerInMemCollection.class;
        constrcutorArgumentTypes[5] = Integer.TYPE;
        return constrcutorArgumentTypes;
    }

    public static String getClassName(
            String aProgramMessageType)
    {
        if ((aProgramMessageType == null) || aProgramMessageType.isBlank())
            return null;
        return messageTypeClassMap.get(aProgramMessageType);
    }

}