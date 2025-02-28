package com.itextos.beacon.platform.kannelstatusupdater.process;

import java.io.StringReader;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ErrorMessage;
import com.itextos.beacon.commonlib.httpclient.BasicHttpConnector;
import com.itextos.beacon.commonlib.httpclient.HttpResult;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.errorlog.ErrorLog;
import com.itextos.beacon.platform.kannelstatusupdater.beans.KannelStatusInfo;
import com.itextos.beacon.platform.kannelstatusupdater.xmlparser.Gateway;
import com.itextos.beacon.smslog.KannelStatusLog;

public class KannelConnector
{

    private static Log log = LogFactory.getLog(KannelConnector.class);

    private KannelConnector()
    {}

    public static String getKannelStatus(
            String aKannelID,
            String aKannelURL)
    {
        final KannelStatusInfo kannelStatusInfo = new KannelStatusInfo(aKannelID);

        try
        {
            final URL url = new URL(aKannelURL);
            kannelStatusInfo.setKannelIp(url.getHost());
            kannelStatusInfo.setKannelPort(url.getPort());
            final HttpResult httpResult = BasicHttpConnector.connect(aKannelURL, true);

            if (log.isDebugEnabled())
                log.debug("Kannel Http Result : " + httpResult);

            if (httpResult.isSuccess())
            {
                final String xml = httpResult.getResponseString();

                

                return xml;

            }
           
        }
        catch (final Exception e)
        {
            log.error("Error while getting the Kannel Status", e);
            KannelStatusLog.log("Kannel Status for Url:'" + aKannelURL + "', " +ErrorMessage.getStackTraceAsString(e));

            kannelStatusInfo.setKannelAvailable(false);
        }
        return null;
    }

    public static void setKannelStatus(
            String xml,
            KannelStatusInfo kannelStatusInfo)
    {

        KannelStatusLog.log("setKannelStatus entered");

        try
        {
            final JAXBContext  context = JAXBContext.newInstance(Gateway.class);
            final Unmarshaller um      = context.createUnmarshaller();
            KannelStatusLog.log("setKannelStatus get  Unmarshaller um");

            final Gateway      gateway = (Gateway) um.unmarshal(new StringReader(xml));

            KannelStatusLog.log("setKannelStatus get  Gateway ");

            if (log.isDebugEnabled())
                log.debug("Parsed the XML successfully");

            kannelStatusInfo.setUpTime(gateway.getUptime());
            kannelStatusInfo.setSMS(gateway.getSMSInfo());
            kannelStatusInfo.setDN(gateway.getDNMap());
            kannelStatusInfo.setSmscList(gateway.getSMSCS());
            kannelStatusInfo.generateSummary();

            final long queueSize = gateway.getSMSBoxQueued();

            if (queueSize == -1)
            {
                kannelStatusInfo.setKannelAvailable(false);
                kannelStatusInfo.setSmsBoxQueue(0);
            }
            else
            {
                kannelStatusInfo.setKannelAvailable(true);
                kannelStatusInfo.setSmsBoxQueue(queueSize);
            }
        }
        catch (final Exception e)
        {
            ErrorLog.log("Exception while parsing and getting the details from XML \n"+ErrorMessage.getStackTraceAsString(e));
            kannelStatusInfo.setKannelAvailable(false);
        }
        
        KannelStatusLog.log("setKannelStatus complete");
    }

}