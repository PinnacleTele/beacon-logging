package com.itextos.beacon.interfaces.generichttpapi.processor.reader;

import org.json.simple.JSONObject;

import com.itextos.beacon.http.generichttpapi.common.interfaces.IRequestProcessor;

public interface RequestReader
{

    void processGetRequest();

    void processPostRequest()
            throws Exception;

    void doProcess(
            String aParsedString);

    void doProcess(
            JSONObject aJsonObj);

    void sendResponse(
            IRequestProcessor aRequestProcessor);

    void setContentType();

    void setContentLength(
            String aResponse);

}
