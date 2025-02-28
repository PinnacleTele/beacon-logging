package com.itextos.beacon.smpp.interfaces.event.handlers;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.commons.util.HexUtil;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerHandler;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.SmppSessionHandler;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.type.SmppProcessingException;
import com.itextos.beacon.smpp.interfaces.event.ItextosBindUnbindInterface;
import com.itextos.beacon.smpp.interfaces.util.Communicator;
import com.itextos.beacon.smpp.interfaces.validation.ValidateRequest;
import com.itextos.beacon.smpp.objects.SessionDetail;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;
import com.itextos.beacon.smslog.BindLog;

abstract class ItextosSmppServerHelper
        implements
        SmppServerHandler
{

    private static final Log                 log               = LogFactory.getLog(ItextosSmppServerHelper.class);
    private final ItextosBindUnbindInterface mItextosBindUnbindHandler;
    protected boolean                        shutdownInitiated = false;

    public ItextosSmppServerHelper(
            ItextosBindUnbindInterface aItextosSmppSessionBindUnbindHandler)
    {
        mItextosBindUnbindHandler = aItextosSmppSessionBindUnbindHandler;
    }

    public ItextosBindUnbindInterface getSmppSessionHandlerInterface()
    {
        return mItextosBindUnbindHandler;
    }

    void handleSessionBindRequest(
            Long aSessionId,
            SmppSessionConfiguration aSessionConfiguration,
            BaseBind aBindRequest)
            throws SmppProcessingException
    {
        if (log.isInfoEnabled())
            log.info("Inside sessionBindRequested ......");

        if (shutdownInitiated)
        {    // dont allow to bind.
            log.fatal("Shutdown intiated... Cannot bind now");
            // Add to memory
            throw new SmppProcessingException(SmppConstants.STATUS_MISSINGOPTPARAM, "Instance Shutting Down");
        }
        final String lSystemId = aSessionConfiguration.getSystemId();

  
        BindLog.log("Bind Request for : " + lSystemId);
        
        Communicator.sendBindReqLog(aSessionConfiguration, aBindRequest);

        final SessionDetail sessionDetail     = new SessionDetail(aSessionId, aSessionConfiguration, aBindRequest);

        final int           sessionWindowSize = SmppProperties.getInstance().getApiSessionWindowSize();

        
        	 BindLog.log("Binding with System ID : '" + sessionDetail.getSystemId() + "'");
        	 BindLog.log("SMPP Session window size :" + sessionWindowSize);
        
        sessionDetail.setConnectionTimeout(30000);
        sessionDetail.setBindTimeout(30000);
        sessionDetail.setWindowSize(sessionWindowSize);


        ValidateRequest.validateRequestOnBind(aBindRequest, sessionDetail);
    }

  

    void handleSessionCreated(
            Long aSessionId,
            SmppServerSession aServerSession,
            BaseBindResp aBindResponse)
            throws SmppProcessingException
    {
        if (log.isDebugEnabled())
            log.debug("Session created: '" + aServerSession.getConfiguration().getSystemId() + "'");

        if (!log.isDebugEnabled())
        {
            aServerSession.getConfiguration().getLoggingOptions().setLogBytes(true);
            aServerSession.getConfiguration().getLoggingOptions().setLogPdu(true);
        }

        aServerSession.getConfiguration().setName(aServerSession.getConfiguration().getSystemId());
        final SmppSessionHandler smppSessionHandler = mItextosBindUnbindHandler.sessionCreated(aSessionId, aServerSession, aBindResponse);
        aServerSession.serverReady(smppSessionHandler);
    }

    void handleSessionDestroyed(
            Long aSessionId,
            SmppServerSession aServerSession)
    {
        if (log.isInfoEnabled())
            log.info("Smpp Server Session destroyed: '" + aServerSession.getConfiguration().getSystemId() + "'");

        mItextosBindUnbindHandler.sessionDestroyed(aSessionId, aServerSession);

        Communicator.sendUnbindLog(aServerSession, "Request By User");

        if (aServerSession.hasCounters() && log.isInfoEnabled())
        {
            log.info("Final session rx-submitSM:    " + aServerSession.getCounters().getRxSubmitSM());
            log.info("Final session Tx-deliverSM:   " + aServerSession.getCounters().getTxDeliverSM());
            log.info("Final session Tx-EnquireLink: " + aServerSession.getCounters().getRxEnquireLink());
        }

        aServerSession.destroy();

        Communicator.sendUnBindResponseLog(aServerSession);
    }

}
