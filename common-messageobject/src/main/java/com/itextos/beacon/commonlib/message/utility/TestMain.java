package com.itextos.beacon.commonlib.message.utility;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.RouteType;
import com.itextos.beacon.commonlib.message.MessagePart;
import com.itextos.beacon.commonlib.message.MessageRequest;

public class TestMain
{

    public static void main(
            String[] args)
            throws Exception
    {
        final MessageRequest msgReq = new MessageRequest(ClusterType.BULK, InterfaceType.HTTP_JAPI, InterfaceGroup.API, MessageType.PROMOTIONAL, MessagePriority.PRIORITY_0, RouteType.DOMESTIC);

        msgReq.setBaseMessageId("1234567890");
        msgReq.setClientId("kp");

        final MessagePart msgObj1 = new MessagePart("12345");
        msgObj1.setMessage("Test 1");

        final MessagePart msgObj2 = new MessagePart("67890");
        msgObj2.setMessage("Test 2");

        msgReq.addMessagePart(msgObj1);
        msgReq.addMessagePart(msgObj2);

        final String jsonString = msgReq.getJsonString();

        System.out.println(msgReq);
        System.out.println(jsonString);

        final MessageRequest msgReq2 = new MessageRequest(jsonString);
        System.out.println(msgReq2);
        System.out.println(msgReq2.getJsonString());
    }

}