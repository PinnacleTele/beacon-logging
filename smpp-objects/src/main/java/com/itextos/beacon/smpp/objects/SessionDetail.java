package com.itextos.beacon.smpp.objects;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppSession;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.itextos.beacon.commonlib.constants.AccountStatus;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.MessagePriority;
import com.itextos.beacon.commonlib.constants.MessageType;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.itextos.beacon.platform.smpputil.AbstractSmppInfo;
import com.itextos.beacon.smpp.objects.counters.ServerRequestCounter;
import com.itextos.beacon.smpp.objects.request.EnquireLinkRequest;
import com.itextos.beacon.smpp.utils.AccountDetails;
import com.itextos.beacon.smpp.utils.ItextosSmppUtil;
import com.itextos.beacon.smpp.utils.generator.BindIdGenerator;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;

public class SessionDetail
        extends
        AbstractSmppInfo
{

    private static final Log               log             = LogFactory.getLog(SessionDetail.class);
    private static final String            SESSION_CLOSED  = "CLOSED";
    private static final int               INVALID_BIND_ID = -1;

    private final Long                     mSessionId;
    private final SmppServerSession        mSession;
    private final SmppSessionConfiguration mSmppSessionConfiguration;
    private final String                   mInstanceId;
    private final String                   mSystemId;
    private final String                   mBindId;
    private final SmppBindType             mBindType;
    private final String                   mBindName;
    private final String                   mHost;
    private final int                      mPort;
    private final int                      mWindowSize;
    private final String                   mSystemType;
    private final int                      mCommandId;
    private SmppUserInfo                   mSmppUserInfo;
    private final String                   mThreadName;
    private final Date                     mBindDate;
    private final long                     mBindTime;

    public SessionDetail(
            Long aSessionId,
            SmppServerSession aSession)
    {
        mSessionId                = aSessionId;
        mSession                  = aSession;
        mBindType                 = mSession.getBindType();
        mInstanceId               = SmppProperties.getInstance().getInstanceId();

        mSmppSessionConfiguration = mSession.getConfiguration();
        mHost                     = mSmppSessionConfiguration.getHost();
        mPort                     = mSmppSessionConfiguration.getPort();
        mSystemId                 = mSmppSessionConfiguration.getSystemId().toLowerCase();
        mWindowSize               = mSmppSessionConfiguration.getWindowSize();
        mSystemType               = mSmppSessionConfiguration.getSystemType();
        mBindName                 = ItextosSmppUtil.getBindName(mBindType);
        mBindId                   = BindIdGenerator.getInstance().getNextBindId();
        mCommandId                = INVALID_BIND_ID;
        mThreadName               = Thread.currentThread().getName();
        mBindDate                 = new Date();
        mBindTime                 = System.currentTimeMillis();
    }

    public SessionDetail(
            Long aSessionId,
            SmppSessionConfiguration aSessionConfiguration,
            BaseBind aBindRequest)
    {
        mSessionId                = aSessionId;
        mSession                  = null;
        mSmppSessionConfiguration = aSessionConfiguration;
        mInstanceId               = SmppProperties.getInstance().getInstanceId();
        mHost                     = mSmppSessionConfiguration.getHost();
        mPort                     = mSmppSessionConfiguration.getPort();
        mSystemId                 = mSmppSessionConfiguration.getSystemId().toLowerCase();
        mWindowSize               = mSmppSessionConfiguration.getWindowSize();
        mSystemType               = mSmppSessionConfiguration.getSystemType();
        mCommandId                = aBindRequest.getCommandId();
        mBindType                 = ItextosSmppUtil.getBindType(mCommandId);
        mBindName                 = ItextosSmppUtil.getBindName(mCommandId);
        mBindId                   = BindIdGenerator.getInstance().getNextBindId();
        mThreadName               = Thread.currentThread().getName();
        mBindDate                 = new Date();
        mBindTime                 = System.currentTimeMillis();
    }

    public void updateUserInfo()
            throws ItextosException
    {
        final UserInfo userInfo = AccountDetails.getUserInfo(mSystemId);

        if (userInfo != null) {
            mSmppUserInfo = new SmppUserInfo(userInfo);
        }
        else {
            throw new ItextosException("Unable to find the user details for the user '" + mSystemId + "'");
        }
    }

    public void close()
    {

        try
        {
            mSession.close();
        }
        catch (final Exception exp)
        {
            log.error("Exception in closing session. May be already closed.", exp);
        }
    }

    public void destroy()
    {

        try
        {
            mSession.destroy();
        }
        catch (final Exception exp)
        {
            log.error("Exception in destroying session. May be already closed.", exp);
        }
    }

    public String getInstanceId()
    {
        return mInstanceId;
    }

    public String getSystemType()
    {
        return mSystemType;
    }

    public String getBindId()
    {
        return mBindId;
    }

    public SmppBindType getBindType()
    {
        return mBindType;
    }

    public String getBindName()
    {
        return mBindName;
    }

    public String getHost()
    {
        return mHost;
    }

    public int getSendWindowSize()
    {
        return mSession.getSendWindow().getSize();
    }

    public SmppServerSession getSession()
    {
        return mSession;
    }

    public Long getSessionId()
    {
        return mSessionId;
    }

    public String getSystemId()
    {
        return mSystemId;
    }

    public int getWindowSize()
    {
        return mWindowSize;
    }

    public boolean isClosed()
    {
        return SESSION_CLOSED.equals(mSession.getStateName());
    }

    public void resetCounters()
    {
        ((DefaultSmppSession) mSession).resetCounters();
    }

    public void updateAndResetCounters()
    {

        try
        {
            final ServerRequestCounter counter = ServerRequestCounter.getInstance();
            counter.setDeliverSm(counter.getDeliverSm() + mSession.getCounters().getTxDeliverSM().getRequest());
            counter.setDeliverSmResp(counter.getDeliverSmResp() + mSession.getCounters().getTxDeliverSM().getResponse());
            counter.setEnquireLink(counter.getEnquireLink() + mSession.getCounters().getRxEnquireLink().getResponse());
            counter.setSubmitSm(counter.getSubmitSm() + mSession.getCounters().getRxSubmitSM().getRequest());
            counter.setSubmitSmResp(counter.getSubmitSmResp() + mSession.getCounters().getRxSubmitSM().getResponse());
            resetCounters();
        }
        catch (final Exception exp)
        {
            log.error("problem resetting counters during idle session removal...", exp);
        }
    }

    public int getCommandId()
    {
        return mCommandId;
    }

    public boolean isTransmitterBind()
    {
        return mBindType == SmppBindType.TRANSMITTER;
    }

    public boolean isSessionBound()
    {
        return mSession.isBound();
    }

    public int getPort()
    {
        return mPort;
    }

    public WindowFuture<Integer, PduRequest, PduResponse> sendRequestPdu(
            DeliverSm aRequest,
            int aRequestTimeout,
            boolean aIsSync)
            throws Exception
    {
        return mSession.sendRequestPdu(aRequest, aRequestTimeout, aIsSync);
    }

    public WindowFuture<Integer, PduRequest, PduResponse> sendRequestPdu(
            EnquireLink aRequest,
            int aRequestTimeout,
            boolean aIsSync)
            throws Exception
    {
        return mSession.sendRequestPdu(aRequest, aRequestTimeout, aIsSync);
    }
    
    public String getClientId()
    {
        return mSmppUserInfo.getClientId();
    }

    public SessionCounterStats getSessionCounterStatistics()
    {
        return new SessionCounterStats(mSession.getCounters());
    }

    public int getMaxBindAllowed()
    {
        return mSmppUserInfo.getMaxBindAllowed();
    }

    public ClusterType getClusterType()
    {
        return mSmppUserInfo.getClusterType();
    }

    public MessageType getMessageType()
    {
        return mSmppUserInfo.getMessageType();
    }

    public MessagePriority getMessagePriority()
    {
        return mSmppUserInfo.getMessagePriority();
    }

    @Deprecated
    public String getAccountJson()
    {
        return mSmppUserInfo.getAccountJson();
    }

    public boolean isDomesticTraBlockoutReject()
    {
        return mSmppUserInfo.isDomesticTraBlockoutReject();
    }

    public void setConnectionTimeout(
            int aConnectionTimeout)
    {
        mSmppSessionConfiguration.setConnectTimeout(aConnectionTimeout);
    }

    public void setBindTimeout(
            int aBindTimeout)
    {
        mSmppSessionConfiguration.setBindTimeout(aBindTimeout);
    }

    public void setWindowSize(
            int aSessionWindowSize)
    {
        mSmppSessionConfiguration.setWindowSize(aSessionWindowSize);
    }

    public AccountStatus getAccountStatus()
    {
        return mSmppUserInfo.getAccountStatus();
    }

    public boolean isAllowedIp()
    {
        return mSmppUserInfo.isAllowedIp(mHost);
    }

    public boolean isSmppServiceEnabled()
    {
        return mSmppUserInfo.isSmppServiceEnabled();
    }

    public boolean isValidPassword(
            String aPassword)
    {
        return mSmppUserInfo.getSmppPassword().equals(aPassword);
    }

    public boolean isAllowedBindType()
    {
        return mSmppUserInfo.isAllowedBindType(mBindName);
    }

    public int getMaxSpeedAllowded()
    {
        return mSmppUserInfo.getMaxSpeedAllowed();
    }

    public boolean considerDefaultLengthAsDomesitic()
    {
        return mSmppUserInfo.considerDefaultLengthAsDomesitic();
    }

    public int getDndPreferences()
    {
        return mSmppUserInfo.getDndPreferences();
    }

    public boolean isDndRejectYN()
    {
        return mSmppUserInfo.isDndRejectYN();
    }

    public boolean isIntlServiceAllowed()
    {
        return mSmppUserInfo.isIntlServiceAllowed();
    }

    public ClusterType getPlatformCluster()
    {
        return mSmppUserInfo.getClusterType();
    }

    public String getDltEntityId()
    {
        return mSmppUserInfo.getDltEntityId();
    }

    public String getDltTemplateId()
    {
        return mSmppUserInfo.getDltTemplateId();
    }

    public String getAccountTimeZone()
    {
        return mSmppUserInfo.getAccountTimeZone();
    }

    public String getSmppCharSet()
    {
        return mSmppUserInfo.getSmppCharSet();
    }

    public SmppUserInfo getSmppUserInfo()
    {
        return mSmppUserInfo;
    }

    public boolean isDomesticSpecialSeriesAllow()
    {
        return mSmppUserInfo.isDomesticSpecialSeriesAllow();
    }

    public String getThreadName()
    {
        return mThreadName;
    }

    public Date getBindDate()
    {
        return mBindDate;
    }

    public long getBindTime()
    {
        return mBindTime;
    }

    public String getClientMidTag()
    {
        return mSmppUserInfo.getClientMidTag();
    }

}