package com.itextos.beacon.http.clouddataprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.http.clouddataprocessor.process.HttpCall;

public class DataFromRedisHolder
{

    private final Map<String, List<String>> clientWiseDataMap = new HashMap<>();

    public void addRequest(
            String aClientId,
            String aRequestData)
    {
        final List<String> clientWiseList = clientWiseDataMap.computeIfAbsent(aClientId, k -> new ArrayList<>());
        clientWiseList.add(aRequestData);
    }

    public void process() throws ItextosRuntimeException
    {
        final JsonObject jsonObj             = new JsonObject();
        final JsonArray  messageRequestArray = new JsonArray();
        jsonObj.add("message_request", messageRequestArray);

        for (final Entry<String, List<String>> entry : clientWiseDataMap.entrySet())
        {
            final JsonObject clientData = new JsonObject();
            clientData.addProperty("client_id", entry.getKey());
            messageRequestArray.add(clientData);

            final JsonArray messageArray = new JsonArray(entry.getValue().size());
            clientData.add("messages", messageArray);

            final List<String> requestList = entry.getValue();

            for (final String s : requestList)
            {
                final IncomingMessageParser imp = new IncomingMessageParser(s);
                messageArray.add(imp.getJsonObject());
            }
        }

        sendToPlatform(jsonObj.toString());
    }

    private void sendToPlatform(
            String aRequest)
    {
        HttpCall.hitTheDataToThePlatformPostUrl(aRequest);
    }

}