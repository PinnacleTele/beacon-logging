package com.itextos.beacon.smpp.interfaces.admin;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.smpp.utils.properties.SmppProperties;

public class ItextosAdminServer
        implements
        Runnable
{

    private static final Log                log            = LogFactory.getLog(ItextosAdminServer.class);
    private ServerSocket                    mServerSocket  = null;
    private final List<ItextosServerSocketHandler> mClientSockets = new ArrayList<>();

    public ItextosAdminServer()
    {

        try
        {
            mServerSocket = new ServerSocket(SmppProperties.getInstance().getAdminPort());
            /*
            final Thread adminServerThread = new Thread(this, "AdminServerr");
            adminServerThread.start();
            */
            Thread virtualThread = Thread.ofVirtual().start(this);

            virtualThread.setName( "AdminServerr");
            
            log.info(" SMPP Interface Admin Server started on port " + mServerSocket.getLocalPort());
        }
        catch (final Exception e)
        {
            log.error("Error while starting admin server. Aborting the startup. Error: ", e);
            System.exit(-1);
        }
    }

    @Override
    public void run()
    {
        while (mServerSocket != null)
            try
            {
                final Socket mClientSocket = mServerSocket.accept();
                mClientSocket.setSoTimeout(SmppProperties.getInstance().getClientSocketTimeout());

                startClientSocketThread(mClientSocket);
            }
            catch (final IOException ioe)
            {
                log.error("Unable to create client socket; Exception: ", ioe);
            }
    }

    private void startClientSocketThread(
            Socket aClientSocket)
    {
        final ItextosServerSocketHandler lServerSocketHandler = new ItextosServerSocketHandler(aClientSocket);
        mClientSockets.add(lServerSocketHandler);
        /*
        final Thread t = new Thread(lServerSocketHandler, "ClientSocketThread");
        t.start();
        */
        Thread virtualThread = Thread.ofVirtual().start(lServerSocketHandler);

        virtualThread.setName( "ClientSocketThread");
    }

    public void stop()
    {

        try
        {
            for (final ItextosServerSocketHandler ssh : mClientSockets)
                ssh.stopMe();

            if (mServerSocket != null)
                mServerSocket.close();
        }
        catch (final Exception e)
        {
            log.error("Exception while stopping the Admin Server", e);
        }
    }

}