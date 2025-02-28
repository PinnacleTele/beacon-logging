package com.itextos.beacon.platform.walletreminder.email;

import java.util.ArrayList;
import java.util.List;

public class EmailObject
{

    private final EmailAddress       mFromAddress;
    private final String             mSubject;
    private final String             mMessage;
    private final List<EmailAddress> mToEmails = new ArrayList<>();
    private final List<EmailAddress> mCcEmails = new ArrayList<>();

    public EmailObject(
            EmailAddress aFromAddress,
            String aSubject,
            String aMessage)
    {
        mFromAddress = aFromAddress;
        mSubject     = aSubject;
        mMessage     = aMessage;
    }

    public void addTo(
            ToEmail aToEmail)
    {
        addToList(aToEmail, mToEmails);
    }

    public void addCc(
            CcEmail aCcEmail)
    {
        addToList(aCcEmail, mCcEmails);
    }

    public EmailAddress getFromAddress()
    {
        return mFromAddress;
    }

    public String getSubject()
    {
        return mSubject;
    }

    public String getMessage()
    {
        return mMessage;
    }

    public List<EmailAddress> getToEmails()
    {
        return mToEmails;
    }

    public List<EmailAddress> getCcEmails()
    {
        return mCcEmails;
    }

    private static void addToList(
            EmailAddress aEmailAddress,
            List<EmailAddress> aEmailList)
    {
        if (aEmailAddress != null)
            aEmailList.add(aEmailAddress);
    }

    @Override
    public String toString()
    {
        return "EmailObject [mFromAddress=" + mFromAddress + ", mSubject=" + mSubject + ", mMessage=" + mMessage + ", mToEmails=" + mToEmails + ", mCcEmails=" + mCcEmails + "]";
    }

}