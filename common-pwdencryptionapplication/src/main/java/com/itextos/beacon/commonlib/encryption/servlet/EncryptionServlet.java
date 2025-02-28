package com.itextos.beacon.commonlib.encryption.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.commonlib.encryption.process.Processor;
import com.itextos.beacon.commonlib.pwdencryption.EncryptedObject;

/**
 * Servlet implementation class EncryptionServlet
 */
@WebServlet("/encryption")
public class EncryptionServlet
        extends
        BasicServlet
{

    private static final long   serialVersionUID = 1L;
    private static final Log    log              = LogFactory.getLog(EncryptionServlet.class);
    private static final String DESTINATION      = "jsp/encryption.jsp";
    private static final String ENCRYPT          = "encrypt";
    private static final String DECRYPT          = "decrypt";
    private static final String ENCODE           = "encode";
    private static final String DECODE           = "decode";

    /**
     * @see BasicServlet#BasicServlet()
     */
    public EncryptionServlet()
    {
        super();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
            IOException
    {
        String          lEncodedText     = null;
        String          lDecodedText     = null;
        String          lEncryptedText   = null;
        String          lDecryptedText   = null;
        String          cryptoType       = null;
        String          textToEncode     = null;
        String          textToDecode     = null;
        String          textToEncrypt    = null;
        String          textToDecrypt    = null;
        String          encryptKey       = null;
        String          decryptKey       = null;
        EncryptedObject lEncryptedObject = null;

        try
        {
            cryptoType    = request.getParameter("cryptotype");
            textToEncode  = request.getParameter("encodetext");
            textToDecode  = request.getParameter("decodetext");
            textToEncrypt = request.getParameter("encrypttext");
            textToDecrypt = request.getParameter("decrypttext");
            encryptKey    = request.getParameter("ekey");
            decryptKey    = request.getParameter("dkey");

            if (cryptoType != null)
                if (cryptoType.equals(ENCRYPT) && (encryptKey != null))
                {
                    lEncryptedObject = Processor.encryptProcess("aes256", textToEncrypt, encryptKey);
                    lEncryptedText   = lEncryptedObject.getEncryptedWithIvAndSalt();
                }
                else
                    if (cryptoType.equals(DECRYPT))
                        lDecryptedText = Processor.decryptProcess("aes256", textToDecrypt, decryptKey);
                    else
                        if (cryptoType.equals(ENCODE))
                        {
                            lEncryptedObject = Processor.encodeProcess(ENCODE, textToEncode, null);
                            lEncodedText     = lEncryptedObject.getEncryptedWithIvAndSalt();
                        }
                        else
                            if (cryptoType.equals(DECODE))
                                lDecodedText = Processor.decodeProcess(ENCODE, textToDecode, null);
        }
        catch (final Exception e)
        {
            log.error("Exception while process the request ", e);
        }
        finally
        {
            final HttpSession lSession = request.getSession(true);
            lSession.setAttribute("encodeText", textToEncode);
            lSession.setAttribute("encodedText", lEncodedText);
            lSession.setAttribute("decodeText", textToDecode);
            lSession.setAttribute("decodedText", lDecodedText);
            lSession.setAttribute("enryptedText", lEncryptedText);
            lSession.setAttribute("decryptedText", lDecryptedText);
            lSession.setAttribute("cryptotype", cryptoType);
            lSession.setAttribute("etext", textToEncrypt);
            lSession.setAttribute("dtext", textToDecrypt);
            lSession.setAttribute("ekey", encryptKey);
            lSession.setAttribute("dkey", decryptKey);

            final RequestDispatcher dispatcher = request.getRequestDispatcher(DESTINATION);
            dispatcher.forward(request, response);
        }
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
