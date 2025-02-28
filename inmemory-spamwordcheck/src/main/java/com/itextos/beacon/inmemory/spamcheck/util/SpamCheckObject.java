package com.itextos.beacon.inmemory.spamcheck.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.MessageType;

public class SpamCheckObject
{

    private static final Log  log                  = LogFactory.getLog(SpamCheckObject.class);

    private final String      mClientId;
    private final String      mMid;
    private final String      mMessage;
    private final MessageType mMsgType;
    private final String      mSpamFilterType;

    private SpamAction        mSpamAction;
    private boolean           mSpamExceptionStatus = false;

    public SpamCheckObject(
            String aClientId,
            String aMid,
            String aMessage,
            String aSpamFilterType,
            MessageType aMsgType)
    {
        super();
        mClientId       = aClientId;
        mMid            = aMid;
        mMessage        = aMessage;
        mSpamFilterType = aSpamFilterType;
        mMsgType        = aMsgType;
    }

    public SpamAction getSpamAction()
    {
        return mSpamAction;
    }

    public void setSpamAction(
            SpamAction aSpamAction)
    {
        mSpamAction = aSpamAction;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public String getMid()
    {
        return mMid;
    }

    public String getMessage()
    {
        return mMessage;
    }

    public MessageType getMsgType()
    {
        return mMsgType;
    }

    public String getSpamFilterType()
    {
        return mSpamFilterType;
    }

    public boolean checkSpam()
    {
        final SpamCheckType lSpamCheckType = SpamCheckType.getSpamCheckType(mSpamFilterType);

        if (log.isDebugEnabled())
            log.debug("Spam Check Type :" + lSpamCheckType);

        switch (lSpamCheckType)
        {
            case ALL:
                checkForAll();
                break;

            case CLIENT_LEVEL:
                checkForClientSpam();
                break;

            case DISABLED:
                break;

            case GLOBAL_LEVEL:
                checkForGlobalSpam();
                break;

            case MSGTYPE_LEVEL:
                checkForMessageTypeSpam();
                break;

            default:
                break;
        }

        if (mSpamExceptionStatus)
            return processSpamAction();

        if (log.isDebugEnabled())
            log.debug("Spam Check Status : " + mSpamExceptionStatus);

        return mSpamExceptionStatus;
    }

    private void checkForGlobalSpam()
    {
        mSpamAction = SpamCheckUtility.spamCheckGlobal(mMessage);

        if (log.isDebugEnabled())
            log.debug("Spam Action : " + mSpamAction);

        mSpamExceptionStatus = mSpamAction != null;
    }

    private void checkForMessageTypeSpam()
    {
        mSpamAction = SpamCheckUtility.spamCheckBasedonMessageType(mMsgType, mMessage);

        if (log.isDebugEnabled())
            log.debug("Spam Action : " + mSpamAction);

        mSpamExceptionStatus = mSpamAction != null;
    }

    private void checkForClientSpam()
    {
        mSpamAction = SpamCheckUtility.spamCheckBasedonClient(mClientId, mMessage);

        if (log.isDebugEnabled())
            log.debug("Spam Action : " + mSpamAction);

        mSpamExceptionStatus = mSpamAction != null;
    }

    private void checkForAll()
    {
        mSpamAction = SpamCheckUtility.spamCheckBasedonClient(mClientId, mMessage);

        if (mSpamAction == null)
            mSpamAction = SpamCheckUtility.spamCheckBasedonMessageType(mMsgType, mMessage);

        if (mSpamAction == null)
            mSpamAction = SpamCheckUtility.spamCheckGlobal(mMessage);

        if (log.isDebugEnabled())
            log.debug("Spam Action : " + mSpamAction);

        mSpamExceptionStatus = mSpamAction != null;
    }

    private boolean processSpamAction()
    {
        final int    action   = mSpamAction == null ? 0 : mSpamAction.getAction();

        final Action lAction  = Action.getSpamAction(String.valueOf(action));

        boolean      isStatus = false;

        switch (lAction)
        {
            case BLOCK_MSG:
                isStatus = true;
                break;

            case NONE:
                break;

            case SPAM_LOG:
                SpamLogger.getInstance().addSpamObject(this);
                break;

            default:
                break;
        }

        return isStatus;
    }

    @Override
    public String toString()
    {
        return "SpamCheckObject [mClientId=" + mClientId + ", mMid=" + mMid + ", mMessage=" + mMessage + ", mMsgType=" + mMsgType + ", mSpamFilterType=" + mSpamFilterType + ", mSpamAction="
                + mSpamAction + "]";
    }

}