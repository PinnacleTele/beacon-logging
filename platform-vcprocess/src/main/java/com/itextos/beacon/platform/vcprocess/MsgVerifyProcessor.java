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
import com.itextos.beacon.platform.vcprocess.util.VCProducer;

public class MsgVerifyProcessor
        extends
        CommonProcess
{

    private static final Log log = LogFactory.getLog(MsgVerifyProcessor.class);

    public MsgVerifyProcessor(
            Component aSourceComponent,
            MessageRequest aMessageRequest)
    {
        super(aSourceComponent, aMessageRequest);
    }

    public void process()
    {

        try
        {
        	mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" : Message Received ");

            if (!doDuplicateCheck())
                return;

            if (!doTimeBoundCheck())
                return;

            if (mMessageRequest.isIsIntl())
            {
                final IntlMsgVerifyProcessor intlMsgProcess = new IntlMsgVerifyProcessor(mSourceComponent, mMessageRequest);
                intlMsgProcess.messageProcess();
            }
            else
                sendToNextComponent(mSourceComponent, mMessageRequest);
        }
        catch (final Exception e)
        {
            log.error("Exception occer while processing the message in Verify Consumer ....", e);
            mMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(mMessageRequest.getFileId()+" : Exception occer while processing the message in Verify Consumer "+ErrorMessage.getStackTraceAsString(e));

            VCProducer.sendToErrorLog(mSourceComponent, mMessageRequest, e);
        }
    }

    private static void sendToNextComponent(
            Component aSourceComponent,
            MessageRequest aMessageRequest)
    {
        final boolean isCreditCheckEnabled = CommonUtility.isEnabled(aMessageRequest.getValue(MiddlewareConstant.MW_CREDIT_CHECK));

     
        aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+" : Credit Check Enabled : " + isCreditCheckEnabled);

        final BillType lBillType = BillType.getBillType(Integer.toString(aMessageRequest.getBillType()));

        if ((BillType.PRE_PAID == lBillType) || isCreditCheckEnabled)
        {
            VCProducer.sendToNextComponent(aSourceComponent, Component.WC, aMessageRequest);

            aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+"Message sendToPrepaidComponent: Successfully" );

        
        }
        else
        {
            VCProducer.sendToNextComponent(aSourceComponent, Component.RC, aMessageRequest);

            aMessageRequest.getLogBuffer().append("\n").append(Name.getLineNumber()).append("\t").append(Name.getClassName()).append("\t").append(Name.getCurrentMethodName()).append("\t").append(aMessageRequest.getFileId()+"Message sendToRouterComponent: Successfully" );

         }
    }

}
