package com.itextos.beacon.commonlib.password.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.commonlib.pwdencryption.EncryptedObject;
import com.itextos.beacon.commonlib.pwdencryption.Encryptor;
import com.itextos.beacon.commonlib.utility.CommonUtility;

/**
 * Servlet implementation class PasswordResetServlet
 */
@WebServlet("/reset")
public class PasswordResetServlet
        extends
        BasicServlet
{

    private static final long   serialVersionUID = 1L;
    private static final Log    log              = LogFactory.getLog(PasswordResetServlet.class);
    private static final String PARAM_CLIENT_ID  = "cli_id";
    private static final String PARAM_API_KEY    = "reset_api";
    private static final String PARAM_SMPP_KEY   = "reset_smpp";

    /**
     * @see BasicServlet#BasicServlet()
     */
    public PasswordResetServlet()
    {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException
    {
        final long   startTime      = System.currentTimeMillis();
        String       clientId       = request.getParameter(PARAM_CLIENT_ID);
        String       apiRequest     = request.getParameter(PARAM_API_KEY);
        String       smppRequest    = request.getParameter(PARAM_SMPP_KEY);
        final String ip             = request.getRemoteAddr();
        boolean      status         = false;
        int          httpStatus     = HttpServletResponse.SC_ACCEPTED;
        String       responseString = "";

        try
        {
            if (log.isDebugEnabled())
                log.debug("Requested from : '" + ip + "' Client Id: '" + clientId + "' Api Request : '" + apiRequest + "' Smpp Request : '" + smppRequest + "'");

            clientId    = CommonUtility.nullCheck(clientId, true);
            apiRequest  = CommonUtility.nullCheck(apiRequest, true);
            smppRequest = CommonUtility.nullCheck(smppRequest, true);

            // if (true)
            // throw new Exception("For testing purpose");

            if (clientId.isBlank())
            {
                responseString = ("Invalid Client Id Specified.");
                httpStatus     = HttpServletResponse.SC_BAD_REQUEST;
                return;
            }

            if (!CommonUtility.isEnabled(apiRequest) && !CommonUtility.isEnabled(smppRequest))
            {
                responseString = ("Invalid reset option specified. Api Request : '" + apiRequest + "' Smpp Request : '" + smppRequest + "'");
                httpStatus     = HttpServletResponse.SC_BAD_REQUEST;
                return;
            }

            EncryptedObject apiObject  = null;
            EncryptedObject smppObject = null;

            if (CommonUtility.isEnabled(apiRequest))
                apiObject = Encryptor.getApiPassword();

            if (CommonUtility.isEnabled(smppRequest))
                smppObject = Encryptor.getSmppPassword();

            final String        apiResponse  = getResponse("api", apiObject);
            final String        smppResponse = getResponse("smpp", smppObject);

            final StringBuilder sb           = new StringBuilder();

            sb.append("\"passwords\":[");
            if (!apiResponse.isEmpty())
                sb.append(apiResponse);

            if (CommonUtility.isEnabled(apiRequest) && CommonUtility.isEnabled(smppRequest))
                sb.append(",");

            if (!smppResponse.isEmpty())
                sb.append(smppResponse);

            sb.append("]");
            responseString = sb.toString();

            if (log.isDebugEnabled())
                log.debug("Succsfully geenrated passwords for the request. Requested from : '" + ip + "' Client Id: '" + clientId + "' Api Request : '" + apiRequest + "' Smpp Request : '"
                        + smppRequest + "'");

            httpStatus = HttpServletResponse.SC_OK;
            status     = true;
        }
        catch (final Exception e)
        {
            httpStatus     = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            responseString = "Some Error has happened. " + e.getMessage() + ". Check logs for more details";
            log.error("Exception while trying to reset the password from : '" + ip + "' Client Id: '" + clientId + "' Api Request : '" + apiRequest + "' Smpp Request : '" + smppRequest + "'", e);
        }
        finally
        {
            final long          endTime = System.currentTimeMillis();

            final StringBuilder sb      = new StringBuilder("{");

            if (!status)
            {
                sb.append("\"status\":\"failed\",");
                sb.append("\"timetaken\":\"").append(endTime - startTime).append("\",");
                sb.append("\"reason\":\"").append(responseString).append("\"");
            }
            else
            {
                sb.append("\"status\":\"success\",");
                sb.append("\"timetaken\":\"").append(endTime - startTime).append("\",");
                sb.append("\"reason\":\"\",");
                sb.append(responseString);
            }
            sb.append("}");

            response.setStatus(httpStatus);
            response.setContentType("text/plain");

            try (
                    PrintWriter lWriter = response.getWriter())
            {
                lWriter.write(sb.toString());
                lWriter.flush();
            }

            if (log.isDebugEnabled())
                log.debug("Time taken = " + (endTime - startTime) + " Client id " + clientId + " Password generated status " + status);
        }
    }

    private static String getResponse(
            String aString,
            EncryptedObject aEncryptedObject)
    {
        if (aEncryptedObject == null)
            return "";

        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"type\":\"").append(aString).append("\",");
        sb.append("\"customer_password\":\"").append(aEncryptedObject.getActualString()).append("\",");
        sb.append("\"dbinsert_password\":\"").append(aEncryptedObject.getEncryptedWithIvAndSalt()).append("\"");
        sb.append("}");
        return sb.toString();
    }

    @Override
    protected void doPost(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {
        doGet(aRequest, aResponse);
    }

}
