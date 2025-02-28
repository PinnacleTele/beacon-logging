package com.itextos.beacon.platform.walletreminder.email;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.httpclient.HTTPRequestUtility;
import com.itextos.beacon.commonlib.httpclient.HttpHeader;
import com.itextos.beacon.commonlib.httpclient.HttpResult;
import com.itextos.beacon.platform.walletreminder.utils.WalletReminderProperties;

public class RmlEmailSender
{

    private static final Log  log = LogFactory.getLog(RmlEmailSender.class);

    private final EmailObject mEmailObject;
    private final String      mEmailString;

    public RmlEmailSender(
            EmailObject aEmailObject)
    {
        mEmailObject = aEmailObject;
        mEmailString = "Email From '" + aEmailObject.getFromAddress() + "' To '" + aEmailObject.getToEmails() + "' with Subject '" + aEmailObject.getSubject() + "'";
    }

    int count = 0;

    public void sendEmail()
            throws ItextosException
    {
        final String     lMailContentJson = getMailContentJson();
        final HttpHeader header           = new HttpHeader<>();
        header.put(HttpHeaders.CONTENT_TYPE, "application/json");

        // System.out.println(lMailContentJson);

        final HttpResult lProcessGetRequest = HTTPRequestUtility.doPostRequest(WalletReminderProperties.getInstance().getEmailApiUrl(), header, lMailContentJson);
        validateReponse(lProcessGetRequest);
    }

    private void validateReponse(
            HttpResult aProcessGetRequest)
    {

        if (aProcessGetRequest != null)
        {
            if (log.isDebugEnabled())
                log.debug("Mail sent to the Email provider. " + getEmailInfo());

            if (aProcessGetRequest.isSuccess())
            {
                final String lResponseString = aProcessGetRequest.getResponseString();
                final String lErrorString    = aProcessGetRequest.getErrorString();

                if (log.isDebugEnabled())
                {
                    log.debug("Response String : '" + lResponseString + "'");
                    log.debug("Error String : '" + lErrorString + "'");
                }

                int mailSentStatus = parseResponse(lResponseString);

                if (mailSentStatus == PARSE_EXCEPTION)
                    mailSentStatus = parseResponse(lErrorString);

                switch (mailSentStatus)
                {
                    case SUCCESS_RESPONSE:
                        if (log.isDebugEnabled())
                            log.debug("Response status 'Success' " + getEmailInfo());
                        break;

                    case PARSE_EXCEPTION:
                        log.error("Response status 'Parse Exception' " + getEmailInfo());
                        break;

                    case ERROR_RESPONSE:
                        log.error("Response status 'Error from Email Provider' " + getEmailInfo());
                        break;

                    default:
                        log.error("Response status UNKNOWN " + getEmailInfo());
                        break;
                }
            }
            else
            {
                final Throwable lException   = aProcessGetRequest.getException();
                final String    lErrorString = aProcessGetRequest.getErrorString();
                if (lException != null)
                    log.error("Problem while connecting to the Email provider URL. " + getEmailInfo(), lException);
                if ((lErrorString != null) && !lErrorString.isBlank())
                    log.error("Problem while connecting to the Email provider URL. Error Message '" + lErrorString + "' " + getEmailInfo());
            }
        }
        else
            log.error("Something went wrong in getting call the HTTP Method. HTTP Result came as null. " + getEmailInfo());
    }

    private String getEmailInfo()
    {
        return mEmailString;
    }

    private static final int PARSE_EXCEPTION  = -1;
    private static final int ERROR_RESPONSE   = 0;
    private static final int SUCCESS_RESPONSE = 1;

    private static int parseResponse(
            String aResponseString)
    {
        int returnValue = PARSE_EXCEPTION;

        try
        {
            final Gson   gson      = new GsonBuilder().create();
            final Map    lFromJson = gson.fromJson(aResponseString, Map.class);
            final String lStatus   = (String) lFromJson.get("status");
            if (lStatus != null)
                if (lStatus.equalsIgnoreCase("error") || !lStatus.equalsIgnoreCase("queued"))
                    returnValue = ERROR_RESPONSE;
                else
                    returnValue = SUCCESS_RESPONSE;
        }
        catch (final Exception e)
        {
            log.error("Parsing response failed.", e);
        }
        return returnValue;
    }

    private String getMailContentJson()
            throws ItextosException
    {
        final JsonObject accessJson  = getMailAccessInfo();
        final JsonObject messageJson = getMessageJson();
        accessJson.add("message", messageJson);
        return accessJson.toString();
    }

    private JsonObject getMessageJson()
            throws ItextosException
    {
        final JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("html", mEmailObject.getMessage());
        jsonObject.addProperty("subject", mEmailObject.getSubject());
        jsonObject.addProperty("from_email", mEmailObject.getFromAddress().getEmailId());
        jsonObject.addProperty("from_name", mEmailObject.getFromAddress().getName());

        final JsonArray toList = getToList();
        jsonObject.add("to", toList);

        final JsonObject mailHeaders = getMailHeader();
        jsonObject.add("headers", mailHeaders);

        return jsonObject;
    }

    private JsonArray getToList()
            throws ItextosException
    {
        final List<EmailAddress> lToEmails = mEmailObject.getToEmails();

        if ((lToEmails == null) || lToEmails.isEmpty())
            throw new ItextosException("To email ids are empty. Cannot send mail.");

        final JsonArray toCcArray = new JsonArray();

        for (final EmailAddress to : lToEmails)
        {
            final JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("email", to.getEmailId());
            jsonObject.addProperty("name", to.getName());
            jsonObject.addProperty("type", "to");

            toCcArray.add(jsonObject);
        }

        final List<EmailAddress> lCcEmails = mEmailObject.getCcEmails();

        if (lCcEmails != null)
            for (final EmailAddress cc : lCcEmails)
            {
                final JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("email", cc.getEmailId());
                jsonObject.addProperty("name", cc.getName());
                jsonObject.addProperty("type", "cc");

                toCcArray.add(jsonObject);
            }
        return toCcArray;
    }

    private static JsonObject getMailHeader()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Reply-To", WalletReminderProperties.getInstance().getReplyToEmailId());
        jsonObject.addProperty("X-Unique-Id", "fastify.nanoid()");

        return jsonObject;
    }

    private static JsonObject getMailAccessInfo()
    {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("owner_id", WalletReminderProperties.getInstance().getEmailApiOwnerId());
        jsonObject.addProperty("token", WalletReminderProperties.getInstance().getEmailApiToken());
        jsonObject.addProperty("smtp_user_name", WalletReminderProperties.getInstance().getEmailSmtpUserName());
        return jsonObject;
    }

    public static void main(
            String[] args)
    {
        final FromEmail    fromEmailAddress = new FromEmail(WalletReminderProperties.getInstance().getFromName(), WalletReminderProperties.getInstance().getFromEmailId());
        final EmailObject  lEmailObject     = new EmailObject(fromEmailAddress, "Test Subject with different Reply-To", "<html><body>Test message 1</body></html>");
        final List<String> lAdminEmailList  = WalletReminderProperties.getInstance().getAdminEmailList();

        for (final String lString : lAdminEmailList)
        {
            lEmailObject.addTo(new ToEmail("To " + lString, lString));
            lEmailObject.addCc(new CcEmail("cc " + lString, lString));
        }

        final RmlEmailSender rmlEmailSender = new RmlEmailSender(lEmailObject);

        try
        {
            rmlEmailSender.sendEmail();
        }
        catch (final ItextosException e)
        {
            e.printStackTrace();
        }
    }

}