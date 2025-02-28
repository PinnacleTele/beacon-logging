package com.itextos.beacon.smpp.interfaces.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.smpp.interfaces.sessionhandlers.ItextosSessionManager;

public class ItextosServerSocketHandler
        implements
        Runnable
{

    private static final Log log          = LogFactory.getLog(ItextosServerSocketHandler.class);

    private final Socket     mSocket;
    private boolean          mCanContinue = true;

    public ItextosServerSocketHandler(
            Socket aSocket)
    {
        this.mSocket = aSocket;
    }

    @Override
    public void run()
    {
        final String clientIP = getClientIp();

        try (
                BufferedReader br = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                PrintWriter out = new PrintWriter(mSocket.getOutputStream(), true);)
        {
            out.println("Welcome to iTexTos SMPP Interface Control Center. You are connected from '" + clientIP + "'");

            log.error("Client connected from '" + clientIP + "'");

            while (mCanContinue)
            {
                final int optionSelected = getOption(br, out);
                handleSelectedOption(optionSelected, br, out);
            }// End of while

            if (log.isDebugEnabled())
                log.debug("Server signals stop");

            out.println("Server disconnecting. Bye.");
            out.flush();
        }
        catch (final Exception e)
        {
            log.error("Exception while running the admin server", e);
        }
    }

    private String getClientIp()
    {

        try
        {
            return mSocket.getInetAddress().getHostAddress();
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the client IP", e);
        }
        return "<UNKNOWN HOST>";
    }

    private void handleSelectedOption(
            int aOptionSelected,
            BufferedReader aBr,
            PrintWriter aOut)
    {

        switch (aOptionSelected)
        {
            case 1:
                clientsList(aBr, aOut);
                break;

            case 2:
                exitAdminConsole();
                break;

            default:
                aOut.println("Invalid option. exiting the application.");
                mCanContinue = false;
                exitAdminConsole();
                break;
        }
    }

    private int getOption(
            BufferedReader aBr,
            PrintWriter aOut)
            throws IOException
    {
        int optionSelected = -1;

        try
        {
            aOut.println("1 - List Clients");
            aOut.println("2 - Exit SMPP Interface Control Center");
            aOut.println(">>>>");
            aOut.flush();

            final String option = aBr.readLine();

            if (log.isDebugEnabled())
                log.debug("Selected Option: " + option);

            optionSelected = CommonUtility.getInteger(option, 2);
        }
        catch (final
                java.net.SocketTimeoutException
                | java.net.SocketException se)
        {
            log.error("Socket Exception. Error: ", se);
            exitAdminConsole();
            throw se;
        }
        catch (final Exception e)
        {
            log.error("Exception while reading the option. Error: ", e);
            optionSelected = -1;
        }
        return optionSelected;
    }

    private void exitAdminConsole()
    {
        log.fatal("Stopping the client connection from " + getClientIp());

        try
        {
            if (mSocket != null)
                mSocket.close();
        }
        catch (final Exception ee)
        {
            log.error("Exception while closing the mSocket", ee);
        }
    }

    public void stopMe()
    {
        mCanContinue = false;
    }

    private static void clientsList(
            BufferedReader br,
            PrintWriter out)
    {
        ItextosSessionManager.getInstance().listClients(br, out);
        out.println("list completed ****");
    }

}