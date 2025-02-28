package com.itextos.beacon.smpp.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.inmemory.customfeatures.InmemCustomFeatures;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.messagetool.FeatureCodeFinder;
import com.itextos.beacon.platform.messagetool.MessageSplitRequest;

public class ItextosSmppUtil
{

    private static final Log log = LogFactory.getLog(ItextosSmppUtil.class);

    private ItextosSmppUtil()
    {}

    public static String getBindName(
            SmppBindType aBindType)
    {
        String lBindType = "";

        switch (aBindType)
        {
            case TRANSMITTER:
                lBindType = ItextosSmppConstants.TRANSMITTER;
                break;

            case TRANSCEIVER:
                lBindType = ItextosSmppConstants.TRANSCEIVER;
                break;

            case RECEIVER:
                lBindType = ItextosSmppConstants.RECEIVER;
                break;

            default:
                break;
        }
        return lBindType;
    }

    public static SmppBindType getBindType(
            int aCommandId)
    {

        switch (aCommandId)
        {
            case SmppConstants.CMD_ID_BIND_TRANSMITTER:
                return SmppBindType.TRANSMITTER;

            case SmppConstants.CMD_ID_BIND_TRANSCEIVER:
                return SmppBindType.TRANSCEIVER;

            case SmppConstants.CMD_ID_BIND_RECEIVER:
                return SmppBindType.RECEIVER;

            default:
                break;
        }
        return null;
    }

    public static String getBindName(
            int aCommandId)
    {
        String lBindName = "";

        switch (aCommandId)
        {
            case SmppConstants.CMD_ID_BIND_TRANSMITTER:
                lBindName = ItextosSmppConstants.TRANSMITTER;
                break;

            case SmppConstants.CMD_ID_BIND_TRANSCEIVER:
                lBindName = ItextosSmppConstants.TRANSCEIVER;
                break;

            case SmppConstants.CMD_ID_BIND_RECEIVER:
                lBindName = ItextosSmppConstants.RECEIVER;
                break;

            default:
                break;
        }
        return lBindName;
    }

    /*
     * public static void logAndInsertError(
     * String aErrorString,
     * String aPrometheusString,
     * int aSmppStatusCode,
     * String aSystemId,
     * String aBindName,
     * String aInstanceId)
     * throws SmppProcessingException
     * {
     * if (log.isInfoEnabled())
     * log.info(aErrorString);
     * PrometheusMetrics.smppIncFailureCounts(new
     * SmppPrometheusInfo(ClusterType.BULK, aInstanceId, aSystemId, aBindName), "0x"
     * + HexUtil.toHexString(aSmppStatusCode), aPrometheusString);
     * logBindResponse(aSystemId, aBindName, aInstanceId);
     * throw new SmppProcessingException(aSmppStatusCode);
     * }
     * private static void logBindResponse(
     * String aSystemId,
     * String aBindName,
     * String aInstanceId)
     * {
     * PrometheusMetrics.smppIncBindResponse(new
     * SmppPrometheusInfo(SmppProperties.getInstance().getInstanceCluster(),
     * aInstanceId, aSystemId, aBindName));
     * }
     */

    public static JSONObject parseJSON(
            String aJsonString)
            throws ParseException
    {
        return (JSONObject) new JSONParser().parse(aJsonString);
    }

    public static void setFeatureCode(
            MessageRequest aMessageRequest)
    {
        final MessageSplitRequest lMsgSplitReq = papulateMessageSplitRequest(aMessageRequest);
        final FeatureCodeFinder   lFCFinder    = new FeatureCodeFinder(lMsgSplitReq);
        lFCFinder.getFeatureCode();

        if (log.isDebugEnabled())
            log.debug("Identifying Feature Code : " + lMsgSplitReq.getFeatureCode());

        aMessageRequest.setFeatureCode(lMsgSplitReq.getFeatureCode());
        aMessageRequest.setMessageClass(lMsgSplitReq.getMessageClass());
    }

    private static MessageSplitRequest papulateMessageSplitRequest(
            MessageRequest aMessageRequest)
    {
        final MessageSplitRequest msr = new MessageSplitRequest(aMessageRequest.getClientId(), aMessageRequest.getBaseMessageId(), aMessageRequest.getLongMessage(), aMessageRequest.getMessageClass(),
                aMessageRequest.isHexMessage());

        msr.setClientMaxSplit(aMessageRequest.getClientMaxSplit());
        msr.setCountry(aMessageRequest.getCountry());
        msr.setDcs(aMessageRequest.getDcs());
        msr.setDestinationPort(aMessageRequest.getDestinationPort());
        msr.setDltEnabled(aMessageRequest.isDltCheckEnabled());
        msr.setDltTemplateType(aMessageRequest.getDltTemplateType());
        msr.setHeader(aMessageRequest.getHeader());
        msr.setIs16BitUdh(aMessageRequest.is16BitUdh());
        msr.setUdh(null);
        // msr.setUdhi(aMessageRequest.getUDH);

        return msr;
    }

    public static String getHexString(
            String aStringValue)
    {
        String lHex = null;

        if (aStringValue != null)
        {
            final int decval = Integer.parseInt(aStringValue.trim());
            lHex = Integer.toHexString(decval);
        }

        return lHex;
    }

    public static boolean isNumaric(
            String aValue)
    {

        try
        {
            Long.parseLong(aValue);
        }
        catch (final NumberFormatException e)
        {
            return false;
        }
        return true;
    }

    public static String getCutomFeatureValue(
            String aClientId,
            CustomFeatures aCustomFeature)
    {
        final InmemCustomFeatures lCustomFeatures = (InmemCustomFeatures) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_FEATURES);
        return lCustomFeatures.getValueOfCustomFeature(aClientId, aCustomFeature.getKey());
    }

}