package com.itextos.beacon.http.generichttpapi.common.interfaces;

import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.http.generichttpapi.common.data.BasicInfo;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceMessage;
import com.itextos.beacon.http.generichttpapi.common.data.InterfaceRequestStatus;
import com.itextos.beacon.http.generichttpapi.common.data.QueueObject;

public interface IRequestProcessor
{

    void parseBasicInfo(
            String aAuthorization)
            throws ItextosException;

    InterfaceRequestStatus validateBasicInfo();

    int getMessagesCount();

    int getNumbersCount(
            int aIndex);

    InterfaceMessage getSingleMessage(StringBuffer sb);

    void setRequestStatus(
            InterfaceRequestStatus aRequestStatus);

    String generateResponse();

    InterfaceRequestStatus getMultipleMessages(
            boolean aStatus);

    /*
     * String appendCountryCode(
     * InterfaceMessage aMessage,
     * String aMobileNumber);
     */
    void pushKafkaTopic(
            String aReqType);

    boolean pushRRQueue(
            QueueObject aQueueObj,
            String aType);

    BasicInfo getBasicInfo();

    void setRequestString(
            String aRequestString);

    void resetRequestJson(
            JSONObject aRequestJson);

    int getHttpStatus();

}
