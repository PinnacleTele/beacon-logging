package com.itextos.beacon.platform.walletreminder.quartz.jobs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.walletreminder.data.DataCollector;
import com.itextos.beacon.platform.walletreminder.data.UserInfo;
import com.itextos.beacon.platform.walletreminder.email.CcEmail;
import com.itextos.beacon.platform.walletreminder.email.EmailObject;
import com.itextos.beacon.platform.walletreminder.email.FromEmail;
import com.itextos.beacon.platform.walletreminder.email.RmlEmailSender;
import com.itextos.beacon.platform.walletreminder.email.ToEmail;
import com.itextos.beacon.platform.walletreminder.utils.WalletReminderProperties;

public class ClientBalanceJob
        extends
        WalletBalanceJob
{

    private static final Log log = LogFactory.getLog(ClientBalanceJob.class);

    @Override
    void process(
            JobExecutionContext aContext)
    {

        if (WalletReminderProperties.getInstance().getClientBalanceCheckEnabled())
        {
            final Map<Long, UserInfo> lUserWalletBalance = DataCollector.getInstance().getUserWalletBalance();

            if (lUserWalletBalance != null)
            {
                final List<UserInfo> mailToSend = new ArrayList<>();

                for (final Entry<Long, UserInfo> entry : lUserWalletBalance.entrySet())
                {
                    final Long     cliId    = entry.getKey();
                    final UserInfo userInfo = entry.getValue();

                    if (log.isDebugEnabled())
                        log.debug("Processing for the client '" + cliId + "' User Info '" + userInfo + "'");

                    if (userInfo.isValidBalance() && (userInfo.getWalletBalance().longValue() < WalletReminderProperties.getInstance().getMinimumBalanceToNotify()))
                    {
                        if (log.isInfoEnabled())
                            log.info("Client '" + cliId + "' is having lower Balance : '" + userInfo.getWalletBalance() + "'");

                        mailToSend.add(userInfo);
                    }
                }

                if (!mailToSend.isEmpty())
                    sendMails(mailToSend);
            }
            else
                log.error("No Client Data found to process further.");
        }
        else
            log.error("Client Balance Check disabled.");
    }

    private static void sendMails(
            List<UserInfo> aMailToSend)
    {
        final List<CcEmail> adminMails    = getAdminMails();
        final String        defaultMailId = WalletReminderProperties.getInstance().getDefaultNotificationMailId();

        for (final UserInfo userInfo : aMailToSend)
        {
            ToEmail toEmail = null;

            try
            {
                final boolean isEnoughMailsSent = isEnoughMailsSent(userInfo);

                if (isEnoughMailsSent)
                    continue;

                final String toMailId = defaultMailId.isBlank() ? userInfo.getEmailAddress() : defaultMailId;

                if (log.isInfoEnabled())
                    log.info("Mail processing for '" + userInfo + "' with mail id " + toMailId);

                toEmail = new ToEmail(userInfo.getFullName(), toMailId);
                final FromEmail   fromEmail   = new FromEmail(WalletReminderProperties.getInstance().getFromName(), WalletReminderProperties.getInstance().getFromEmailId());

                final String      subject     = getMailSubject(userInfo);
                final String      message     = getMailContent(userInfo);

                final EmailObject emailObject = new EmailObject(fromEmail, subject, message);
                emailObject.addTo(toEmail);

                for (final CcEmail ccEmail : adminMails)
                    emailObject.addCc(ccEmail);

                final RmlEmailSender rmlEmailSender = new RmlEmailSender(emailObject);
                rmlEmailSender.sendEmail();
            }
            catch (final Exception e)
            {
                log.error("Exception while sending the mail to " + toEmail + " - " + userInfo, e);
            }
        }

        log.fatal("Client mails sent count : " + aMailToSend.size());
    }

    private static boolean isEnoughMailsSent(
            UserInfo aUserInfo)
    {
        // TODO Need to check inmemory and in the log file for the remainder mails for
        // this user.
        return false;
    }

    private static String getMailContent(
            UserInfo aUserInfo)
    {
        return MessageFormat.format(WalletReminderProperties.getInstance().getClientBodyFormat(), //
                aUserInfo.getPrintableWalletBalance(), // {0}
                aUserInfo.getFullName(), // {1}
                DateTimeUtility.getFormattedDateTime(new Date(DataCollector.getInstance().getLastDataReceived()), DateTimeFormat.DEFAULT), // {2}
                aUserInfo.getUser(), // {3}
                aUserInfo.getClientId() + "" // {4}
        );
    }

    private static String getMailSubject(
            UserInfo aUserInfo)
    {
        return MessageFormat.format(WalletReminderProperties.getInstance().getClientSubjectFormat(),
                DateTimeUtility.getFormattedDateTime(new Date(DataCollector.getInstance().getLastDataReceived()), DateTimeFormat.DEFAULT), aUserInfo.getUser());
    }

    private static List<CcEmail> getAdminMails()
    {
        final List<CcEmail> returnValue     = new ArrayList<>();
        final List<String>  lAdminEmailList = WalletReminderProperties.getInstance().getAdminEmailList();
        for (final String ccEmail : lAdminEmailList)
            returnValue.add(new CcEmail(ccEmail, ccEmail));
        return returnValue;
    }

    @Override
    void printNextFireTime(
            JobExecutionContext aContext)
    {
        log.fatal("Next scheduled time: " + DateTimeUtility.getFormattedDateTime(aContext.getNextFireTime(), DateTimeFormat.DEFAULT));
    }

}