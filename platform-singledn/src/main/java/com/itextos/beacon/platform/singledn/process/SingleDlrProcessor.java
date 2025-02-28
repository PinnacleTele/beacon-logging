package com.itextos.beacon.platform.singledn.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.message.DeliveryObject;
import com.itextos.beacon.inmemory.customfeatures.pojo.DNDeliveryMode;
import com.itextos.beacon.inmemory.customfeatures.pojo.DlrTypeInfo;
import com.itextos.beacon.inmemory.customfeatures.pojo.SingleDnProcessType;
import com.itextos.beacon.platform.singledn.ISingleDnProcess;
import com.itextos.beacon.platform.singledn.data.DeliveryInfo;
import com.itextos.beacon.platform.singledn.data.SingleDnInfo;
import com.itextos.beacon.platform.singledn.data.SingleDnRequest;
import com.itextos.beacon.platform.singledn.enums.DnStatus;
import com.itextos.beacon.platform.singledn.impl.SingleDnProcessorAllFailure;
import com.itextos.beacon.platform.singledn.impl.SingleDnProcessorAllSuccess;
import com.itextos.beacon.platform.singledn.impl.SingleDnProcessorAnyFailure;
import com.itextos.beacon.platform.singledn.impl.SingleDnProcessorAtleastOneSuccess;
import com.itextos.beacon.platform.singledn.impl.SingleDnProcessorPartialFailure;
import com.itextos.beacon.platform.singledn.impl.SingleDnProcessorPartialSuccess;

public class SingleDlrProcessor
{

    private static final Log log = LogFactory.getLog(SingleDlrProcessor.class);

    private SingleDlrProcessor()
    {}

    public static SingleDnInfo processSingleDnProcessor(
            SingleDnRequest aSingleDnRequest)
            throws ItextosException
    {
        final DlrTypeInfo lDlrTypeInfo = SingleDNUtil.getDnTypeInfo(aSingleDnRequest.getClientId());

        if (log.isDebugEnabled())
            log.debug("Dlr Type Info :" + lDlrTypeInfo);

        RedisOperation.incrementCountersAndinsertDnData(aSingleDnRequest, lDlrTypeInfo.getExpiryInSec());

        SingleDnInfo returnValue = getProcessor(aSingleDnRequest, lDlrTypeInfo);

        if (returnValue != null)
        {
            final boolean isDuplicate = RedisOperation.addToProcessedDn(aSingleDnRequest, lDlrTypeInfo);

            // final boolean isDuplicate =
            // RedisOperation.isSingleDnAlreadSent(aSingleDnRequest.getClientId(),
            // aSingleDnRequest.getBaseMessageId());

            if (log.isDebugEnabled())
                log.debug("Is Duplicate Record? '" + isDuplicate + "'");

            if (isDuplicate)
                returnValue = null;
        }

        if (log.isDebugEnabled())
            log.debug("Final Single DN value : " + returnValue);

        return returnValue;
    }

    private static SingleDnInfo getProcessor(
            SingleDnRequest aSingleDnRequest,
            DlrTypeInfo aDlrTypeInfo)
            throws ItextosException
    {
        final SingleDnProcessType lSingleDnProcessType = aDlrTypeInfo.getSingleDnProcessType();
        ISingleDnProcess          singleDnProcess      = null;

        switch (lSingleDnProcessType)
        {
            case ALL_FAILURE:
                singleDnProcess = new SingleDnProcessorAllFailure(aSingleDnRequest, aDlrTypeInfo);
                break;

            case ALL_SUCCESS:
                singleDnProcess = new SingleDnProcessorAllSuccess(aSingleDnRequest, aDlrTypeInfo);
                break;

            case PARTIAL_FAILURE:
                singleDnProcess = new SingleDnProcessorPartialFailure(aSingleDnRequest, aDlrTypeInfo);
                break;

            case PARTIAL_SUCCESS:
                singleDnProcess = new SingleDnProcessorPartialSuccess(aSingleDnRequest, aDlrTypeInfo);
                break;

            case ANY_FAILURE:
                singleDnProcess = new SingleDnProcessorAnyFailure(aSingleDnRequest, aDlrTypeInfo);
                break;

            case ATLEAST_ONE_SUCCESS:
                singleDnProcess = new SingleDnProcessorAtleastOneSuccess(aSingleDnRequest, aDlrTypeInfo);
                break;

            default:
                throw new ItextosException("Invalid Single Dn Process Type.");
        }

        return addPreviousOrCurrentDnInfo(aSingleDnRequest, aDlrTypeInfo, singleDnProcess);
    }

    private static SingleDnInfo addPreviousOrCurrentDnInfo(
            SingleDnRequest aSingleDnRequest,
            DlrTypeInfo aDlrTypeInfo,
            ISingleDnProcess aSingleDnProcess)
            throws ItextosException
    {
        SingleDnInfo             returnValue    = null;
        final List<SingleDnInfo> previousDnData = getPreviousDnData(aSingleDnRequest, aDlrTypeInfo);

        for (final SingleDnInfo sdi : previousDnData)
        {
            final boolean isResultFound = aSingleDnProcess.addSingleDnInfo(sdi);

            if (log.isDebugEnabled())
                log.debug("Result :" + isResultFound);

            if (isResultFound)
            {
                returnValue = aSingleDnProcess.getResult();
                break;
            }
        }
        if (log.isDebugEnabled())
            log.debug("DN Result Status :" + returnValue);

        return returnValue;
    }

    private static List<SingleDnInfo> getPreviousDnData(
            SingleDnRequest aSingleDnRequest,
            DlrTypeInfo aDlrTypeInfo)
    {
        final SingleDnProcessType lSingleDnProcessType = aDlrTypeInfo.getSingleDnProcessType();
        boolean                   isOldDnsRequired     = false;

        if (log.isDebugEnabled())
            log.debug("SingleDN Processor Type :" + lSingleDnProcessType);

        switch (lSingleDnProcessType)
        {
            case ALL_FAILURE:
            case ALL_SUCCESS:
            case PARTIAL_FAILURE:
            case PARTIAL_SUCCESS:
                isOldDnsRequired = true;
                break;

            case ANY_FAILURE:
                if (aSingleDnRequest.getDeliveryInfo().isSuccess() && ((aDlrTypeInfo.getAltHandoverMode() == DNDeliveryMode.AVAILABLE_FIRST_SUCCESS_PART)
                        || (aDlrTypeInfo.getAltHandoverMode() == DNDeliveryMode.AVAILABLE_LAST_SUCCESS_PART) || (aDlrTypeInfo.getAltHandoverMode() == DNDeliveryMode.LATEST_SUCCESS_DELIVERED)))
                    isOldDnsRequired = true;
                break;

            case ATLEAST_ONE_SUCCESS:
                if (!aSingleDnRequest.getDeliveryInfo().isSuccess() && ((aDlrTypeInfo.getAltHandoverMode() == DNDeliveryMode.AVAILABLE_FIRST_FAILURE_PART)
                        || (aDlrTypeInfo.getAltHandoverMode() == DNDeliveryMode.AVAILABLE_LAST_FAILURE_PART) || (aDlrTypeInfo.getAltHandoverMode() == DNDeliveryMode.LATEST_FAILURE_DELIVERED)))
                    isOldDnsRequired = true;
                break;

            default:
                break;
        }

        final List<SingleDnInfo> returnValue = new ArrayList<>();

        if (log.isDebugEnabled())
            log.debug("Old Dn's Required Status : " + isOldDnsRequired);

        if (isOldDnsRequired)
        {
            final List<DeliveryInfo> deliveryInfoList = RedisOperation.getPreviousDns(aSingleDnRequest);

            if (log.isDebugEnabled())
                log.debug("Delivery Info List : " + deliveryInfoList);

            for (final DeliveryInfo di : deliveryInfoList)
                returnValue.add(getSingleDnInfo(di));
        }
        else
            returnValue.add(getSingleDnInfo(aSingleDnRequest.getDeliveryInfo()));

        if (log.isDebugEnabled())
            log.debug("Prevous DN's Data : " + returnValue);

        return returnValue;
    }

    private static SingleDnInfo getSingleDnInfo(
            DeliveryInfo aDeliveryInfo)
    {

        try
        {
            final DeliveryObject lDeliveryObject = new DeliveryObject(aDeliveryInfo.getDnJson());

            final Date           lRecvTime       = lDeliveryObject.getMessageReceivedTime();
            final Date           lDelvTime       = lDeliveryObject.getDeliveryTime();

            final DnStatus       lDnStatus       = lDeliveryObject.getDnOriStatusDesc().equalsIgnoreCase("DELIVRD") ? DnStatus.SUCCESS : DnStatus.FAILURE;

            final SingleDnInfo   lSingleDnInfo   = new SingleDnInfo(lDeliveryObject.getMessageId(), aDeliveryInfo.getPartNo(), lDeliveryObject.getMessageTotalParts(), lRecvTime.getTime(),
                    lDelvTime.getTime(), lDnStatus);

            lSingleDnInfo.setDNObject(aDeliveryInfo.getDnJson());

            return lSingleDnInfo;
        }
        catch (

        final Exception e)
        {
            log.debug("Exception occer while process SingleDnInfo ...", e);
        }

        return null;
    }

}