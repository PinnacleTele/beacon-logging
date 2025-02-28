package com.itextos.beacon.platform.topic2table.inserter;

import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.platform.topic2table.utils.ExceptionHandlerType;

public interface ITableInserter
{

    void process();

    void createAlert(
            Exception aException);

    void returnToSameTopic();

    void returnToSameTopic(
            BaseMessage aBaseMessage);

    ExceptionHandlerType handleException(
            Exception aException,
            boolean aIsBatchInsert);

    void processIndividualMessages(
            boolean aTrimData);

    void dropMessage(
            Exception aException,
            BaseMessage aCurrentMessage);

}
