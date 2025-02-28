package com.itextos.beacon.smpp.interfaces.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.DeliverSmResp;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.EnquireLinkResp;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.pdu.Unbind;
import com.cloudhopper.smpp.pdu.UnbindResp;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.commonlib.prometheusmetricsutil.smpp.SmppPrometheusInfo;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.smpputil.ISmppInfo;
import com.itextos.beacon.smpp.dboperations.DbBindOperation;
import com.itextos.beacon.smpp.objects.SessionDetail;
import com.itextos.beacon.smpp.objects.SmppObjectType;
import com.itextos.beacon.smpp.objects.SmppRequestType;
import com.itextos.beacon.smpp.objects.bind.BindInfoValid;
import com.itextos.beacon.smpp.objects.bind.UnbindInfo;
import com.itextos.beacon.smpp.objects.inmem.InfoCollection;
import com.itextos.beacon.smpp.utils.ItextosSmppUtil;
import com.itextos.beacon.smpp.utils.properties.SmppProperties;

public class Communicator
{

    private static final Log log = LogFactory.getLog(Communicator.class);

    private Communicator()
    {}

    public static final void sendBindReqLog(
            SmppSessionConfiguration aSessionConfiguration,
            BaseBind aBindRequest)
    {
        PrometheusMetrics.smppIncBindRequest(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), SmppProperties.getInstance().getInstanceId(),
                nullCheck(aSessionConfiguration.getSystemId()), aSessionConfiguration.getHost(), ItextosSmppUtil.getBindName(aBindRequest.getCommandId())));
    }

    public static final void sendBindActiveLog(
            SessionDetail aSessionDetail)
    {
        PrometheusMetrics.smppIncBindActiveCounts(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), SmppProperties.getInstance().getInstanceId(),
                nullCheck(aSessionDetail.getSystemId()), aSessionDetail.getHost(), aSessionDetail.getBindName()));

        sendBindResposeLog(aSessionDetail);
    }

    public static final void sendSubmitSmReqLog(
            SessionDetail aSessionDetail,
            SubmitSm aSubmitSmRequest)
    {
        PrometheusMetrics.smppIncSubmitSmRequest(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), aSessionDetail.getInstanceId(), nullCheck(aSessionDetail.getSystemId()),
                aSessionDetail.getHost(), aSessionDetail.getBindName()));
    }

    public static void sendSubmitSmResLog(
            SessionDetail aSessionDetail,
            SubmitSmResp aSubmitSmResponse)
    {
        PrometheusMetrics.smppIncSubmitSmResponse(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), aSessionDetail.getInstanceId(), nullCheck(aSessionDetail.getSystemId()),
                aSessionDetail.getHost(), aSessionDetail.getBindName()));
    }

    public static void sendUnbindRequestLog(
            SessionDetail aSessionDetail,
            Unbind aUnbindRequest)
    {
        PrometheusMetrics.smppIncUnbindRequest(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), aSessionDetail.getInstanceId(), nullCheck(aSessionDetail.getSystemId()),
                aSessionDetail.getHost(), aSessionDetail.getBindName()));

        PrometheusMetrics.smppDecBindActiveCounts(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), aSessionDetail.getInstanceId(), nullCheck(aSessionDetail.getSystemId()),
                aSessionDetail.getHost(), aSessionDetail.getBindName()));
    }

    public static void sendUnbindResponsetLog(
            SessionDetail aSessionDetail,
            UnbindResp aUnbindResponse)
    {
        PrometheusMetrics.smppIncUnbindResponse(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), aSessionDetail.getInstanceId(), nullCheck(aSessionDetail.getSystemId()),
                aSessionDetail.getHost(), aSessionDetail.getBindName()));
    }

    public static void sendEnquireLinkRequestLog(
            SessionDetail aSessionDetail,
            EnquireLink aEnquireLink)
    {
        PrometheusMetrics.smppIncEnquiryLinkRequest(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), aSessionDetail.getInstanceId(), nullCheck(aSessionDetail.getSystemId()),
                aSessionDetail.getHost(), aSessionDetail.getBindName()));
    }

    public static void sendEnquireLinkResponseLog(
            SessionDetail aSessionDetail,
            EnquireLinkResp aEnquireLinkResponse)
    {
        PrometheusMetrics.smppIncEnquiryLinkResponse(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), aSessionDetail.getInstanceId(), nullCheck(aSessionDetail.getSystemId()),
                aSessionDetail.getHost(), aSessionDetail.getBindName()));
    }

    public static void sendBindLog(
            SessionDetail aSessionDetail,
            BindInfoValid aBindinfolog)
    {
        InfoCollection.getInstance().addInfoObject(SmppObjectType.BIND_INFO_VALID, aBindinfolog);
    }

    public static void sendUnBindLog(
            SessionDetail aSessionDetail,
            UnbindInfo aUnBindinfolog)
    {
        InfoCollection.getInstance().addInfoObject(SmppObjectType.UNBIND_INFO_DB, aUnBindinfolog);
        PrometheusMetrics.smppIncUnbindCounts(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), SmppProperties.getInstance().getInstanceId(),
                nullCheck(aSessionDetail.getSystemId()), aSessionDetail.getHost(), aSessionDetail.getBindName()), "Network Unbind");
    }

    public static void sendDeliverSMResponseLog(
            SessionDetail aSessionDetail,
            DeliverSmResp aResponse)
    {
        PrometheusMetrics.smppIncDeliverSmResponse(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), SmppProperties.getInstance().getInstanceId(),
                nullCheck(aSessionDetail.getSystemId()), aSessionDetail.getHost(), aSessionDetail.getBindName()));
    }

    public static void sendBindResposeLog(
            SessionDetail aSessionDetail)
    {
        PrometheusMetrics.smppIncBindResponse(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), SmppProperties.getInstance().getInstanceId(),
                nullCheck(aSessionDetail.getSystemId()), aSessionDetail.getHost(), aSessionDetail.getBindName()));
    }

    public static void sendBindFailureLog(
            SessionDetail aSessionDetail,
            String aErrorCode,
            String aReason)
    {
        sendBindResposeLog(aSessionDetail);

        PrometheusMetrics.smppIncFailureCounts(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), SmppProperties.getInstance().getInstanceId(),
                nullCheck(aSessionDetail.getSystemId()), aSessionDetail.getHost(), aSessionDetail.getBindName()), aErrorCode, aReason);
    }

    public static void sendUnbindLog(
            SmppServerSession aServerSession,
            String aReason)
    {
        PrometheusMetrics.smppIncUnbindCounts(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), SmppProperties.getInstance().getInstanceId(),
                nullCheck(aServerSession.getConfiguration().getSystemId()), aServerSession.getConfiguration().getHost(), ItextosSmppUtil.getBindName(aServerSession.getBindType())), aReason);

        PrometheusMetrics.smppIncUnbindRequest(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), SmppProperties.getInstance().getInstanceId(),
                nullCheck(aServerSession.getConfiguration().getSystemId()), aServerSession.getConfiguration().getHost(), ItextosSmppUtil.getBindName(aServerSession.getBindType())));
        PrometheusMetrics.smppDecBindActiveCounts(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), SmppProperties.getInstance().getInstanceId(),
                nullCheck(aServerSession.getConfiguration().getSystemId()), aServerSession.getConfiguration().getHost(), ItextosSmppUtil.getBindName(aServerSession.getBindType())));
    }

    public static void sendUnBindResponseLog(
            SmppServerSession aServerSession)
    {
        PrometheusMetrics.smppIncUnbindResponse(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), SmppProperties.getInstance().getInstanceId(),
                nullCheck(aServerSession.getConfiguration().getSystemId()), aServerSession.getConfiguration().getHost(), ItextosSmppUtil.getBindName(aServerSession.getBindType())));
    }

    public static void sendDeliverSMRequestLog(
            SessionDetail aSessionDetail)
    {
        PrometheusMetrics.smppIncDeliverSmRequest(new SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(), SmppProperties.getInstance().getInstanceId(),
                nullCheck(aSessionDetail.getSystemId()), aSessionDetail.getHost(), aSessionDetail.getBindName()));
    }

    public static String nullCheck(
            String aSystemId)
    {
        return CommonUtility.nullCheck(aSystemId, true).toLowerCase();
    }

    public static void sendUnbindInfoToDb(
            SessionDetail aSessionDetail,
            int aFailureCode,
            String aReason)
    {
        final boolean isDBInsertRequired = SmppProperties.getInstance().isDbInsertRequired();

        if (log.isDebugEnabled())
            log.debug("DB Insert Required '" + isDBInsertRequired + "', Hence data not insert into DB");

        if (isDBInsertRequired)
        {
            final String lInstanceId  = SmppProperties.getInstance().getInstanceId();
            final int    instancePort = SmppProperties.getInstance().getApiListenPort();
            String       lClient      = "0";
            if (aSessionDetail.getSmppUserInfo() != null)
                lClient = aSessionDetail.getClientId();

            final SmppBindType lBindType     = aSessionDetail.getBindType();
            final String       lBindId       = aSessionDetail.getBindId();
            final String       lSystemId     = aSessionDetail.getSystemId();
            final String       sourceIp      = aSessionDetail.getHost();
            final String       lThreadName   = aSessionDetail.getThreadName();

            final UnbindInfo   unBindinfoLog = new UnbindInfo(lInstanceId, lClient, SmppRequestType.UNBIND, lBindType, lBindId, CommonUtility.getApplicationServerIp(), instancePort, lSystemId,
                    sourceIp, lThreadName);
            unBindinfoLog.setErrorcode(aFailureCode);
            unBindinfoLog.setReason(CommonUtility.nullCheck(aReason, true));
            unBindinfoLog.setBindTime(DateTimeUtility.getFormattedDateTime(aSessionDetail.getBindTime(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS));
            unBindinfoLog.setBindDate(DateTimeUtility.getFormattedDateTime(aSessionDetail.getBindDate(), DateTimeFormat.DEFAULT_DATE_ONLY));

            boolean inserted = false;

            try
            {
                inserted = InfoCollection.getInstance().addInfoObject(SmppObjectType.UNBIND_INFO_DB, unBindinfoLog);
            }
            catch (final Exception e)
            {
                log.error("", e);
            }

            if (!inserted)
            {
                final List<ISmppInfo> bindLogList = new ArrayList<>();
                bindLogList.add(unBindinfoLog);

                try
                {
                    DbBindOperation.insertUnBindInfo(bindLogList);
                }
                catch (final Exception e1)
                {
                    log.error("Exception while inserting Unbind Request...", e1);
                }
            }
        }
    }

}