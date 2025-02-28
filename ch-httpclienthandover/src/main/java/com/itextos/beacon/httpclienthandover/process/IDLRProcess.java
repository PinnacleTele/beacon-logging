package com.itextos.beacon.httpclienthandover.process;

import java.util.List;

import com.itextos.beacon.commonlib.message.BaseMessage;

public interface IDLRProcess
{

    void processDLR(
            List<BaseMessage> messageList);

}
