package com.itextos.beacon.smppsimulator.interfaces.event.handlers;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.PduAsyncResponse;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionHandler;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.DeliverSmResp;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.EnquireLinkResp;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.pdu.Unbind;
import com.cloudhopper.smpp.pdu.UnbindResp;
import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.prometheusmetricsutil.smpp.SmppPrometheusInfo;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.smppsimulator.util.DeliverSmInfo;
import com.itextos.beacon.smppsimulator.interfaces.sessionhandlers.ItextosSessionManager;
import com.itextos.beacon.smppsimulator.interfaces.util.Communicator;
import com.itextos.beacon.smppsimulator.interfaces.validation.ValidateRequest;
import com.itextos.beacon.smppsimulator.objects.SessionCounterStats;
import com.itextos.beacon.smppsimulator.objects.SessionDetail;
import com.itextos.beacon.smppsimulator.objects.SmppRequestType;
import com.itextos.beacon.smppsimulator.objects.SmppUserInfo;
import com.itextos.beacon.smppsimulator.objects.bind.UnbindInfo;
import com.itextos.beacon.smppsimulator.utils.properties.SmppProperties;
import com.itextos.beacon.smslog.DnMissedLog;
import com.itextos.beacon.smslog.EntryLog;
import com.itextos.beacon.smslog.SmppInvalidBindReceiverLog;
import com.itextos.beacon.smslog.SubmitsmLog;

import io.prometheus.client.Histogram.Timer;

abstract class ItextosSmppSessionHelper
        implements
        SmppSessionHandler
{

    private static final Log                            log                 = LogFactory.getLog(ItextosSmppSessionHelper.class);
    private static final String                         BIND_TYPE_UNKNOWN   = "UNKNOWN";
    private static final String                         USER_UNKNOWN        = "UNKNOWN";

    protected static final String                       STR_NOT_IMPLEMENTED = "Not Implemented. ";

    private SessionDetail                               mSessionDetail;
    private Date                                        mLastUsedTime       = new Date();
    private Date                                        mPrevUsedTime;
    private final ConcurrentMap<Integer, DeliverSmInfo> mDeliverySmInfoMap  = new ConcurrentHashMap<>();

    private boolean                                     mInUse;
    private boolean                                     mIsExpired          = false;
    private int                                         mBindFailuerError;

    ItextosSmppSessionHelper(
            SessionDetail aSessionDetail)
    {
        mSessionDetail = aSessionDetail;
    }

    void handleChannelUnExpectedClose()
    {
        log.error("Handle Channel Unexpectedly Closed");

        final boolean isDBInsertRequired = SmppProperties.getInstance().isDbInsertRequired();

        if (log.isDebugEnabled())
            log.debug("DB Insert enabled ==" + isDBInsertRequired);

        if (isDBInsertRequired)
        {
            final String       sourceIp      = mSessionDetail.getHost();

            final String       lInstanceId   = mSessionDetail.getInstanceId();
            final String       lSystemId     = mSessionDetail.getSystemId();

            final String       lClientId     = mSessionDetail.getClientId();
            final String       lBindId       = mSessionDetail.getBindId();
            final SmppBindType lBindType     = mSessionDetail.getBindType();
            final int          lServerPort   = SmppProperties.getInstance().getApiListenPort();

            final UnbindInfo   unBindinfolog = new UnbindInfo(lInstanceId, lClientId, SmppRequestType.BIND, lBindType, lBindId, mSessionDetail.getHost(), lServerPort, lSystemId, sourceIp, "");
            Communicator.sendUnBindLog(mSessionDetail, unBindinfolog);
        }
    }

    PduResponse handlePduRequestReceived(
            PduRequest<?> aPduRequest)
    {
        final PduResponse pduResponse = aPduRequest.createResponse();
        final int         pduCommand  = aPduRequest.getCommandId();

        if (log.isDebugEnabled())
            log.debug("Command Received '" + pduCommand + "'");

        switch (pduCommand)
        {
            case SmppConstants.CMD_ID_SUBMIT_SM:
                handleSubmitSm((SubmitSm) aPduRequest, (SubmitSmResp) pduResponse);
                break;

            case SmppConstants.CMD_ID_SUBMIT_MULTI:
                log.error(STR_NOT_IMPLEMENTED + "CMD_ID_SUBMIT_MULTI (" + SmppConstants.CMD_ID_SUBMIT_MULTI + ")");
                pduResponse.setCommandStatus(SmppConstants.STATUS_INVCMDID);
                break;

            case SmppConstants.CMD_ID_DELIVER_SM:
                log.error(STR_NOT_IMPLEMENTED + "CMD_ID_DELIVER_SM (" + SmppConstants.CMD_ID_DELIVER_SM + ")");
                pduResponse.setCommandStatus(SmppConstants.STATUS_INVCMDID);
                break;

            case SmppConstants.CMD_ID_DATA_SM:
                log.error(STR_NOT_IMPLEMENTED + "CMD_ID_DATA_SM (" + SmppConstants.CMD_ID_DATA_SM + ")");
                pduResponse.setCommandStatus(SmppConstants.STATUS_INVCMDID);
                break;

            case SmppConstants.CMD_ID_QUERY_SM:
                log.error(STR_NOT_IMPLEMENTED + "CMD_ID_QUERY_SM (" + SmppConstants.CMD_ID_QUERY_SM + ")");
                pduResponse.setCommandStatus(SmppConstants.STATUS_INVCMDID);
                break;

            case SmppConstants.CMD_ID_CANCEL_SM:
                log.error(STR_NOT_IMPLEMENTED + "CMD_ID_CANCEL_SM (" + SmppConstants.CMD_ID_CANCEL_SM + ")");
                pduResponse.setCommandStatus(SmppConstants.STATUS_INVCMDID);
                break;

            case SmppConstants.CMD_ID_REPLACE_SM:
                log.error(STR_NOT_IMPLEMENTED + "CMD_ID_REPLACE_SM (" + SmppConstants.CMD_ID_REPLACE_SM + ")");
                pduResponse.setCommandStatus(SmppConstants.STATUS_INVCMDID);
                break;

            case SmppConstants.CMD_ID_ENQUIRE_LINK:
                handleEnquireLink((EnquireLink) aPduRequest, (EnquireLinkResp) pduResponse);
                break;

            case SmppConstants.CMD_ID_UNBIND:
                handleUnbind((Unbind) aPduRequest, (UnbindResp) pduResponse);
                break;

            default:
                break;
        }
        mLastUsedTime = new Date();
        return pduResponse;
    }

    void handlePduRequestExpired(
            PduRequest aPduRequest)
    {
        final int lSequenceNumber = aPduRequest.getSequenceNumber();

        if (log.isDebugEnabled())
            log.debug("Handle PduRequestExpired Sequence Number '" + lSequenceNumber + "'");

        mLastUsedTime = new Date();

        if (mDeliverySmInfoMap.get(lSequenceNumber) != null)
        {
            final DeliverSmInfo lDeliverSmInfo = mDeliverySmInfoMap.get(lSequenceNumber);
            lDeliverSmInfo
                    .setReason("DN Expired, due to DN Response not received before '" + SmppProperties.getInstance().getApiDnReqTimeout() + "' time , DN:'" + lDeliverSmInfo.getShortMessage() + "'");
            mDeliverySmInfoMap.remove(lSequenceNumber);
            writeResponse(lDeliverSmInfo, -2, lSequenceNumber);
        }
        else
        {
            log.info("firePduRequestExpired - Map Obj -" + mDeliverySmInfoMap);
            log.error("firePduRequestExpired - Missing Seq Number in map -" + lSequenceNumber);
        }
    }

    public void addDeliverySmInfo(
            Integer aSequenceNo,
            DeliverSmInfo aDnInfo)
    {
        if (!mDeliverySmInfoMap.containsKey(aSequenceNo))
            mDeliverySmInfoMap.put(aSequenceNo, aDnInfo);
        else
            log.error("Duplicate Sequence number try to be adding... " + aSequenceNo);
    }

    public void removeDeliverySmInfo(
            Integer aSequenceNo)
    {
        mDeliverySmInfoMap.remove(aSequenceNo);
    }

    private void handleSubmitSm(
            SubmitSm aSubmitSmRequest,
            SubmitSmResp aSubmitSmResponse)
    {
    	

        int    seqNum   = -1;
        String bindType = BIND_TYPE_UNKNOWN;
        String userName = USER_UNKNOWN;
        Timer  lTimer   = null;

        StringBuffer sb=new StringBuffer();
        sb.append("\n#######################################################################\n");
        
        try
        {
            seqNum   = aSubmitSmRequest.getSequenceNumber();
            bindType = mSessionDetail.getBindName();
            userName = mSessionDetail.getSystemId();
            sb.append(" smpp : "+userName+ " :  "+bindType+ " : " +aSubmitSmResponse.getMessageId()+ " : "+ aSubmitSmResponse.getResultMessage()).append("\n");

            
            lTimer   = PrometheusMetrics
                    .smppSubmitSmStartTimer(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), mSessionDetail.getInstanceId(), userName, mSessionDetail.getHost(), bindType));

            if (mSessionDetail.getBindType() == SmppBindType.RECEIVER)
            {
                aSubmitSmResponse.setCommandStatus(SmppConstants.STATUS_SYSERR);
                SmppInvalidBindReceiverLog.log(userName+ " :  "+bindType+" SmppBindType.RECEIVER : "+ SmppBindType.RECEIVER + " : " +aSubmitSmResponse.getMessageId()+ " : "+ aSubmitSmResponse.getResultMessage() );
                return;
            }


            Communicator.sendSubmitSmReqLog(mSessionDetail, aSubmitSmRequest);
            ValidateRequest.validateSubmitSm(aSubmitSmRequest, aSubmitSmResponse, mSessionDetail,sb);

            if (log.isDebugEnabled())
            {
                log.debug("Submit SM Response : " + aSubmitSmResponse);
                log.debug("Submit SM Response Reult Message: " + aSubmitSmResponse.getResultMessage());
            }
          
            EntryLog.log(" smpp : "+userName+ " :  "+bindType+ " : " +aSubmitSmResponse.getMessageId()+ " : "+ aSubmitSmResponse.getResultMessage());
            sb.append(" smpp : "+userName+ " :  "+bindType+ " : " +aSubmitSmResponse.getMessageId()+ " : "+ aSubmitSmResponse.getResultMessage()).append("\n");


        }
        catch (final Exception e)
        {
            log.error("Exception while handling SubmitSm Request for user '" + userName + "'", e);
            aSubmitSmResponse.setCommandStatus(SmppConstants.STATUS_SYSERR);
            EntryLog.log(" smpp : "+userName+ " :  "+bindType+ " : " +aSubmitSmResponse.getMessageId()+ " : "+ aSubmitSmResponse.getResultMessage()+" error : "+ErrorMessage.getStackTraceAsString(e));
        }
        finally
        {
            PrometheusMetrics.smppSubmitSmEndTimer(
                    new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), mSessionDetail.getInstanceId(), userName, mSessionDetail.getHost(), bindType), lTimer);
            Communicator.sendSubmitSmResLog(mSessionDetail, aSubmitSmResponse);
        }
        
        sb.append("\n#######################################################################\n");

        SubmitsmLog.log(sb.toString());
    }

    private void handleUnbind(
            Unbind aUnbindRequest,
            UnbindResp aUnbindResponse)
    {
        String bindType = BIND_TYPE_UNKNOWN;
        String userName = USER_UNKNOWN;
        Timer  lTimer   = null;

        try
        {
            bindType = mSessionDetail.getBindName();
            userName = mSessionDetail.getSystemId();

            if (log.isDebugEnabled())
                log.debug("Unbind Request rised ......" + userName);

            Communicator.sendUnbindRequestLog(mSessionDetail, aUnbindRequest);
            lTimer = PrometheusMetrics
                    .smppUnbindStartTimer(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), mSessionDetail.getInstanceId(), userName, mSessionDetail.getHost(), bindType));
        }
        catch (final Exception e)
        {
            log.error("Exception while handling Unbind for user '" + userName + "'", e);
            aUnbindResponse.setCommandStatus(SmppConstants.STATUS_SYSERR);
        }
        finally
        {
            PrometheusMetrics.smppUnbindEndTimer(
                    new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), mSessionDetail.getInstanceId(), userName, mSessionDetail.getHost(), bindType), lTimer);

            Communicator.sendUnbindResponsetLog(mSessionDetail, aUnbindResponse);
        }
    }

    private void handleEnquireLink(
            EnquireLink aEnquireLink,
            EnquireLinkResp aEnquireLinkResponse)
    {
        String bindType = BIND_TYPE_UNKNOWN;
        String userName = USER_UNKNOWN;
        Timer  lTimer   = null;

        try
        {
            bindType = mSessionDetail.getBindName();
            userName = mSessionDetail.getSystemId();

            Communicator.sendEnquireLinkRequestLog(mSessionDetail, aEnquireLink);
            lTimer = PrometheusMetrics
                    .smppEnquiryLinkStartTimer(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), mSessionDetail.getInstanceId(), userName, mSessionDetail.getHost(), bindType));
            aEnquireLinkResponse.setCommandStatus(SmppConstants.STATUS_OK);
        }
        catch (final Exception e)
        {
            log.error("Exception while handling EnquireLink for user '" + userName + "'", e);
            aEnquireLinkResponse.setCommandStatus(SmppConstants.STATUS_SYSERR);
        }
        finally
        {
            PrometheusMetrics.smppEnquiryLinkEndTimer(
                    new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), mSessionDetail.getInstanceId(), userName, mSessionDetail.getHost(), bindType), lTimer);
            Communicator.sendEnquireLinkResponseLog(mSessionDetail, aEnquireLinkResponse);
        }
    }

    void handleExpectedPduResponseReceived(
            PduAsyncResponse aPduAsyncResponse)
    {
        mLastUsedTime = new Date();

        final PduResponse lResponse       = aPduAsyncResponse.getResponse();
        final int         lSequenceNumber = lResponse.getSequenceNumber();

        if (log.isDebugEnabled())
            log.debug("Handle ExpectedPduResponseReceived  - Sequence No :" + lSequenceNumber);

        final DeliverSmInfo dnInfo = mDeliverySmInfoMap.remove(lSequenceNumber);

        if (dnInfo != null)
        {
            dnInfo.setReason("DN Handover Success , DN:'" + dnInfo.getShortMessage() + "'");

            writeResponse(dnInfo, lResponse.getCommandStatus(), lSequenceNumber);

            Communicator.sendDeliverSMResponseLog(mSessionDetail, (DeliverSmResp) lResponse);
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("Sequence and DnInfo Map " + mDeliverySmInfoMap);
            log.error("Missing Seq Number in map. Seq num '" + lSequenceNumber + "'");
        }
    }

    void handleUnexpectedPduResponseReceived(
            PduResponse aPduResponse)
    {
        mLastUsedTime = new Date();
        
        StringBuffer sb=new StringBuffer();
        
        if (log.isDebugEnabled())
            log.debug("handle UnexpectedPduResponseReceived possible unhandled response " + aPduResponse);

        final DeliverSmResp deliverSmResp   = (DeliverSmResp) aPduResponse;
        final int           lSequenceNumber = deliverSmResp.getSequenceNumber();

        sb.append("Missing DN Error").append("\n");
        

        if (log.isInfoEnabled())
            log.info("deliver_sm_resp: commandStatus for new [" + deliverSmResp.getCommandStatus() + "=" + deliverSmResp.getResultMessage() + "][Sequence Number -]" + lSequenceNumber);

        sb.append("deliver_sm_resp: commandStatus for new [").append(deliverSmResp.getCommandStatus() + "=" + deliverSmResp.getResultMessage() + "][Sequence Number -]" + lSequenceNumber).append("\n");

        final DeliverSmInfo dnInfo = mDeliverySmInfoMap.remove(lSequenceNumber);

        if (dnInfo != null)
        {
            writeResponse(dnInfo, deliverSmResp.getCommandStatus(), lSequenceNumber);
            Communicator.sendDeliverSMResponseLog(mSessionDetail, deliverSmResp);
        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("Sequence and DnInfo Map " + mDeliverySmInfoMap);
            
            sb.append("Sequence and DnInfo Map"+ mDeliverySmInfoMap).append("\n");

            log.error("fireUnexpectedPduResponseReceived - Missing Seq Number in map -" + lSequenceNumber);
       
            sb.append("fireUnexpectedPduResponseReceived - Missing Seq Number in map -" + lSequenceNumber).append("\n");

        }
        
        DnMissedLog.log(sb.toString());
    }

    void handleUnknownThrowable(
            Throwable aThrowable)
    {
        log.error("handle UnknownThrowable... closing the session", aThrowable);

        try
        {
            mSessionDetail.close();
            mSessionDetail.destroy();
        }
        catch (final Exception error)
        {
            log.error("session destroy failed session handler removed>>>", error);
        }
        finally
        {

            try
            {
                ItextosSessionManager.getInstance().removeSession(true, mSessionDetail.getSessionId(), mSessionDetail.getSession());
            }
            catch (final Exception exp)
            {
                log.error("session counter removal issue...sessionId=" + mSessionDetail.getSessionId(), exp);
            }
        }
    }

    public void clearWaitingDn()
    {
        final Map<Integer, DeliverSmInfo> tempMap = new HashMap<>();

        synchronized (mDeliverySmInfoMap)
        {
            tempMap.putAll(mDeliverySmInfoMap);
            mDeliverySmInfoMap.clear();
        }

        for (final DeliverSmInfo dnInfo : tempMap.values())
        {
            writeResponse(dnInfo, null, -1);
            dnInfo.setReason("API-FAILE- Remove all DN Response waiting.. DN:'" + dnInfo.getShortMessage() + "'");
        }
    }

    public void updateLastUsedTime()
    {
        mPrevUsedTime = mLastUsedTime;
        mLastUsedTime = new Date();
    }

    public void resetLastUsedTime()
    {
        mLastUsedTime = mPrevUsedTime;
    }

    private void writeResponse(
            DeliverSmInfo aDnInfo,
            Integer status,
            int seqNum)
    {

        try
        {
            aDnInfo.updateRetryInfo(status);

            if (seqNum != -1)
                Communicator.sendDeliverSMResponseLog(mSessionDetail, null);

            if (log.isInfoEnabled())
                log.info("DNInfo CommandStatus '" + CommonUtility.nullCheck(status, true) + "'");

            boolean postLog = aDnInfo.updateDnStatus(status);

            if (!postLog)
            {
                postLog = aDnInfo.isExpired(SmppProperties.getInstance().getDnWaitingTime());
                aDnInfo.setReason("DN-Expired, due to API-DN_MAX-Wait time reached, WaitTime:'" + SmppProperties.getInstance().getDnWaitingTime() + "'");
            }

            final long index = -1;

          
        }
        catch (final Exception exp)
        {
            log.error("problem writing responses....", exp);
        }
    }

    public String getSystemId()
    {
        return mSessionDetail.getSystemId();
    }

    public int getWindowSize()
    {
        return mSessionDetail.getWindowSize();
    }

    public int getSendWindowSize()
    {
        return mSessionDetail.getSendWindowSize();
    }

    public boolean isInUse()
    {
        return mInUse;
    }

    public void setInUse(
            boolean aInUse)
    {
        mInUse = aInUse;
    }

    public boolean isSameSession(
            SmppServerSession aSession)
    {
        return mSessionDetail.getSession().equals(aSession);
    }

    public Date getLastUsedTime()
    {
        return mLastUsedTime;
    }

    public String getBindId()
    {
        return mSessionDetail.getBindId();
    }

    public boolean isExpired()
    {
        return mIsExpired;
    }

    public void setExpired()
    {
        mIsExpired = true;
    }

    public boolean isSessionClosed()
    {
        return mSessionDetail.isClosed();
    }

    public void closeSession()
    {
        mSessionDetail.close();
    }

    public void destroySession()
    {
        mSessionDetail.destroy();
    }

    public void updateAndResetCounters()
    {
        mSessionDetail.updateAndResetCounters();
    }

    public void resetCounters()
    {
        mSessionDetail.resetCounters();
    }

    public Long getSessionId()
    {
        return mSessionDetail.getSessionId();
    }

    public SmppBindType getBindType()
    {
        return mSessionDetail.getBindType();
    }

    public String getBindTypeString()
    {
        return mSessionDetail.getBindName();
    }

    public void forceUnbind()
    {
        mSessionDetail.getSession().unbind(0);
    }

    public boolean isTransmitterBind()
    {
        return mSessionDetail.isTransmitterBind();
    }

    public boolean isSessionBound()
    {
        return mSessionDetail.isSessionBound();
    }

    public String getHost()
    {
        return mSessionDetail.getHost();
    }

    public int getPort()
    {
        return mSessionDetail.getPort();
    }

    public String getClientId()
    {
        return mSessionDetail.getClientId();
    }

    public WindowFuture<Integer, PduRequest, PduResponse> sendRequestPdu(
            DeliverSm aRequest,
            int aDnRequestTimeout,
            boolean aIsSync)
            throws Exception
    {
        return mSessionDetail.sendRequestPdu(aRequest, aDnRequestTimeout, aIsSync);
    }

    public int getBindFailureError()
    {
        return mBindFailuerError;
    }

    public void setBindFailuerError(
            int aStatusSyserr)
    {
        mBindFailuerError = aStatusSyserr;
    }

    public SessionCounterStats getSessionCounterStatistics()
    {
        return mSessionDetail.getSessionCounterStatistics();
    }

    public SmppServerSession getSession()
    {
        return mSessionDetail.getSession();
    }

    public SmppUserInfo getSmppUserInfo()
    {
        return mSessionDetail.getSmppUserInfo();
    }

    public SessionDetail getSessionDetail()
    {
        return mSessionDetail;
    }

    public void setSessionDetail(
            SessionDetail aSessionDetail)
    {
        mSessionDetail = aSessionDetail;
    }

}