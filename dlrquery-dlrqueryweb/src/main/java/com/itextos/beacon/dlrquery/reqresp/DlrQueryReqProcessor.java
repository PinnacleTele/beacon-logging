package com.itextos.beacon.dlrquery.reqresp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.dlrquery.queryprocess.QueryProcessor;
import com.itextos.beacon.dlrquery.request.RequestData;
import com.itextos.beacon.dlrquery.request.enums.DlrQueryStatus;
import com.itextos.beacon.inmemory.dlrquery.InmemDlrQuery;
import com.itextos.beacon.inmemory.dlrquery.objects.DlrHeaderParam;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class DlrQueryReqProcessor
{

    private static final Log log = LogFactory.getLog(DlrQueryReqProcessor.class);

    public static void processRequest(
            HttpServletRequest aRequest,
            HttpServletResponse aResponse)
    {
        final String lAccessKey    = aRequest.getParameter("accesskey");
        final String lCustMid      = aRequest.getParameter("custmid");
        final String lMobileNumber = aRequest.getParameter("mnumber");
        final String lFileId       = aRequest.getParameter("fileid");
        final String lSourceIP     = aRequest.getRemoteAddr();
        if (log.isDebugEnabled())
            log.debug("File_Id :'" + lFileId + "' - CustMid :'" + lCustMid + "' - Dest :'" + lMobileNumber + "' - SourceIP :'" + lSourceIP + "'");

        final RequestData    lReqData = new RequestData(lAccessKey, lSourceIP, lFileId, lCustMid, lMobileNumber);

        final DlrQueryStatus lDlrQReq = lReqData.getDlrQueryStatus();

        if (log.isDebugEnabled())
            log.debug("Dlr Query Status :" + lDlrQReq);

        final QueryProcessor lQueryProcess = new QueryProcessor(lReqData);
        final String         lResponse     = lQueryProcess.query();

        sendResponse(aResponse, lResponse, lDlrQReq, lReqData.getClientId());
    }

    private static void sendResponse(
            HttpServletResponse aResponse,
            String aResponseMessage,
            DlrQueryStatus aDlrQueryStatus,
            String aClientId)
    {
        PrintWriter writer = null;

        try
        {

            if (aDlrQueryStatus == DlrQueryStatus.VALID_REQUEST)
            {
                final List<DlrHeaderParam> lDlrHeaderParams = getHeaderParams(aClientId);

                if (lDlrHeaderParams != null)
                {
                    if (log.isDebugEnabled())
                        log.debug("Header Params : " + lDlrHeaderParams.toString());

                    for (final DlrHeaderParam lDlrParam : lDlrHeaderParams)
                        aResponse.addHeader(lDlrParam.getParamName(), lDlrParam.getParamValue());
                }
            }
            else
                aResponse.setContentType("application/json");

            writer = aResponse.getWriter();
            writer.println(aResponseMessage);
            writer.flush();
        }
        catch (final IOException e)
        {
            log.error("Exception while sending the Response.", e);
        }
        finally
        {
            if (writer != null)
                writer.close();
        }
    }

    private static List<DlrHeaderParam> getHeaderParams(
            String aClientId)
    {
        final InmemDlrQuery        inmemDlrQuery     = (InmemDlrQuery) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.DLR_QUERY);
        final String               lResponseHeaderId = inmemDlrQuery.getResponseHeaderId(aClientId);
        final List<DlrHeaderParam> lDlrHeaderParams  = inmemDlrQuery.getDlrHeaderParam(lResponseHeaderId);

        return lDlrHeaderParams;
    }

    public static void main(
            String[] args)
    {
        final String lAccessKey    = "4XAn$Yr8lr8U";
        final String lCustMid      = "";
        final String lMobileNumber = "";
        final String lFileId       = "1162203061751160002500";
        final String lSourceIP     = "192.168.1.6";

        if (log.isDebugEnabled())
            log.debug("File_Id :'" + lFileId + "' - CustMid :'" + lCustMid + "' - Dest :'" + lMobileNumber + "' - SourceIP :'" + lSourceIP + "'");

        final RequestData    lReqData = new RequestData(lAccessKey, lSourceIP, lFileId, lCustMid, lMobileNumber);

        final DlrQueryStatus lDlrQReq = lReqData.getDlrQueryStatus();

        if (log.isDebugEnabled())
            log.debug("DlrQReq Status :" + lDlrQReq);

        if (lDlrQReq == DlrQueryStatus.VALID_REQUEST)
        {
            final QueryProcessor lQueryProcess = new QueryProcessor(lReqData);
            final String         lResponse     = lQueryProcess.query();

            if (log.isDebugEnabled())
                log.debug("Response : " + lResponse);
        }
    }

}
