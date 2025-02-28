package com.itextos.beacon.platform.msgtool.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.apperrorhandler.servlets.BasicServlet;
import com.itextos.beacon.platform.msgtool.util.JsonRequestReader;
import com.itextos.beacon.platform.msgtool.util.MsgProcessUtil;

public class MessageInfo
        extends
        BasicServlet
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    @Override
    public void init() {
    	
    	App.createfolder();
    }

    @Override
    protected void doGet(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {
        final PrintWriter lWriter = aResponse.getWriter();

        try
        {
            aResponse.setContentType("text/plain");
            final String lResponse = MsgProcessUtil.requestProcess(aRequest);
            lWriter.append(lResponse);
            lWriter.flush();
        }
        catch (final Exception e)
        {
            // ignore
        }
        finally
        {
            lWriter.close();
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
            throws ServletException,
            IOException
    {
        final PrintWriter lWriter = aResponse.getWriter();

        try
        {
            aResponse.setContentType("text/plain");
            final String lJsonReqString = JsonRequestReader.getRequestFromBody(aRequest);
            String       lResponse      = "";

            if (lJsonReqString != null)
            {
                final JSONObject lJsonObj = JsonRequestReader.parseJSON(lJsonReqString);
                lResponse = MsgProcessUtil.requestProcess(lJsonObj);
            }

            lWriter.append(lResponse);
            lWriter.flush();
        }
        catch (final Exception e)
        {
            // ignore
        }
        finally
        {
            lWriter.close();
        }
    }

}
