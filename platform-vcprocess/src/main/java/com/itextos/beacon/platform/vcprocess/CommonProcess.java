package com.itextos.beacon.platform.vcprocess;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.PlatformStatusCode;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.platform.vcprocess.util.VCProducer;
import com.itextos.beacon.platform.vcprocess.util.VCUtil;

public abstract class CommonProcess
{

 //   private static final Log       log = LogFactory.getLog(CommonProcess.class);

    protected final MessageRequest mMessageRequest;
    protected final Component      mSourceComponent;

    protected CommonProcess(
            Component aSourceComponent,
            MessageRequest aMessageRequest)
    {
        this.mMessageRequest  = aMessageRequest;
        this.mSourceComponent = aSourceComponent;
    }

    protected boolean doDuplicateCheck()
    {

        if (((mMessageRequest.getVlShortner() == 0) && (mMessageRequest.getUrlSmartlinkEnable() == 0)) && VCUtil.doDuplicateChk(mMessageRequest))
        {
        	mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" : Message Rejected Duplicate Check Failed ..  ");

             mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.DUPLICATE_CHECK_FAILED.getStatusCode());
            VCProducer.sendToPlatformRejection(mSourceComponent, mMessageRequest);
            return false;
        }
        return true;
    }

    protected boolean doTimeBoundCheck()
    {

        if (!VCUtil.doTimeboundChk(mMessageRequest))
        {
        	
        	mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" : Message Rejected Time Bound Check Failed  ..  ");

            mMessageRequest.setSubOriginalStatusCode(PlatformStatusCode.REJECT_TIMEBOUND_CHECK.getStatusCode());
            VCProducer.sendToPlatformRejection(mSourceComponent, mMessageRequest);
            return false;
        }

        return true;
    }

}
