package com.itextos.beacon.platform.vcprocess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.BillType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.Name;
import com.itextos.beacon.platform.prc.process.RejectionProcess;
import com.itextos.beacon.platform.vcprocess.util.VCProducer;
import com.itextos.beacon.platform.vcprocess.util.VCUtil;

public class DltMessageVerifyProcessor
        extends
        CommonProcess
{

    private static final Log log = LogFactory.getLog(DltMessageVerifyProcessor.class);

    public DltMessageVerifyProcessor(
            Component aSourceComponent,
            MessageRequest aMessageRequest)
    {
        super(aSourceComponent, aMessageRequest);
    }

    public void process()
    {

        try
        {
        	mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" : Message Received :  " );

            if (!doDuplicateCheck())
                return;

            if (!doTimeBoundCheck())
                return;

            doDomesticMessageProcess(mMessageRequest);
        }
        catch (final Exception e)
        {
            log.error("Exception occer while processing the message in Verify Consumer ....", e);

            mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" : Exception occer while processing the message in Verify Consumer :  " +ErrorMessage.getStackTraceAsString(e));

            VCProducer.sendToErrorLog(mSourceComponent, mMessageRequest, e);
        }
    }

    private void doDomesticMessageProcess(
            MessageRequest aMessageRequest)
            throws Exception
    {
    	aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" : Message Received :  " );

     
        dltMsgProcess();
    }

    public void dltMsgProcess()
            throws Exception
    {
        final String statusDLT = VCUtil.validateDLTReq(mMessageRequest);

        if (statusDLT != null)
        {
            mMessageRequest.setSubOriginalStatusCode(statusDLT);
    //        VCProducer.sendToNextComponent(mSourceComponent, Component.PRC, mMessageRequest);
            mMessageRequest.setFromComponent(mSourceComponent.getKey());
            mMessageRequest.setNextComponent(Component.PRC.getKey());
            RejectionProcess.forPRC(mMessageRequest);

            return;
        }

        final boolean isCreditCheckEnabled = CommonUtility.isEnabled(mMessageRequest.getValue(MiddlewareConstant.MW_CREDIT_CHECK));


        mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+"Credit Check Enabled : " + isCreditCheckEnabled);

        final BillType lBillType = BillType.getBillType(Integer.toString(mMessageRequest.getBillType()));

        if ((BillType.PRE_PAID == lBillType) || isCreditCheckEnabled)
        {
            VCProducer.sendToNextComponent(mSourceComponent, Component.WC, mMessageRequest);

            mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" mSourceComponent "+mSourceComponent +" DEST " +Component.WC+"  Message sendToPrepaidComponent: Successfully : ");

        }
        else
        {
            VCProducer.sendToNextComponent(mSourceComponent, Component.RC, mMessageRequest);

            mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" mSourceComponent "+mSourceComponent +" DEST " +Component.RC+" Message  sendToRouterComponent: Successfully : ");

           
        }
    }

}
