package com.itextos.beacon.platform.msgflowutil.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DCS;
import com.itextos.beacon.commonlib.constants.FeatureCode;
import com.itextos.beacon.commonlib.constants.UdhHeaderInfo;
import com.itextos.beacon.commonlib.message.MessagePart;
import com.itextos.beacon.commonlib.message.MessageRequest;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class UdhProcessor
{

    private static final Log     log = LogFactory.getLog(UdhProcessor.class);

    private final MessageRequest mMessageRequest;

    public UdhProcessor(
            MessageRequest aMessageRequest)
    {
        this.mMessageRequest = aMessageRequest;
    }

    public void generateUDH()
    {
        final FeatureCode lFeatureCode = FeatureCode.getFeatureCode(CommonUtility.nullCheck(mMessageRequest.getFeatureCode()));
        if (lFeatureCode == null)
            log.fatal("mMessageRequest.getFeatureCode() " + mMessageRequest.getFeatureCode() + " nullcheck " + CommonUtility.nullCheck(mMessageRequest.getFeatureCode()) + " lFeatureCode "
                    + lFeatureCode + " Message " + mMessageRequest);

        final boolean lIsHexMsg = mMessageRequest.isHexMessage();

        if (log.isDebugEnabled())
            log.debug("Is Hex Message : " + lIsHexMsg);

        final List<MessagePart> lMessageObjectLst = mMessageRequest.getMessageParts();

        for (final MessagePart aMsgObj : lMessageObjectLst)
        {
            final String lMessage = aMsgObj.getMessage();

            if (lIsHexMsg && !lMessage.isEmpty() && (aMsgObj.getUdhi() == 1))
            {
                // This is applicable for Non English Message

                final String udhlengthHex = lMessage.substring(0, 2);
                final int    i            = Integer.parseInt(udhlengthHex, 16);
                final int    udhlengthInt = (i * 2) + 2;
                final String lUdh         = lMessage.substring(0, udhlengthInt);
                aMsgObj.setUdh(lUdh);
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("");

                switch (lFeatureCode)
                {
                    case BINARY_MSG:
                    case PLAIN_MESSAGE_MULTI:
                    case FLASH_PLAIN_MESSAGE_MULTI:
                    case FLASH_UNICODE_MULTI:
                    case UNICODE_MULTI:
                        handleNonPortBasedMessage(aMsgObj, lFeatureCode, lMessage);
                        break;

                    case SPECIAL_PORT_PLAIN_MESSAGE_MULTI:
                    case SPECIAL_PORT_UNICODE_MULTI:
                        handleSpecialPortMulti(aMsgObj, lFeatureCode, lMessage);
                        break;

                    case SPECIAL_PORT_PLAIN_MESSAGE_SINGLE:
                    case SPECIAL_PORT_UNICODE_SINGLE:
                        handleSpecialPortSingle(aMsgObj, lFeatureCode, lMessage);
                        break;

                    case FLASH_PLAIN_MESSAGE_SINGLE:
                    case FLASH_UNICODE_SINGLE:
                    case PLAIN_MESSAGE_SINGLE:
                    case UNICODE_SINGLE:
                    default:
                        break;
                }
            }
        }
    }

    private void handleNonPortBasedMessage(
            MessagePart aMessageObj,
            FeatureCode aFeatureCode,
            String aHexMessage)
    {
        final int lDcs = mMessageRequest.getDcs();

        if (CommonUtility.getInteger(DCS.PDU_MSG.getKey()) == lDcs)
            // Send message as it is to carrier don't add udh before the longMsg, adding
            // udhi=1 since tested SMSC with udhi=1 and it's working so just adding udhi=1
            aMessageObj.setUdhi(1);
        else
        {
            String lUdh = CommonUtility.nullCheck(aMessageObj.getUdh(), true);
            if ((lUdh.isBlank()) || (lUdh.length() < 1))
                lUdh = getConcatenateUDH(aMessageObj);

            aMessageObj.setUdh(lUdh);
            aMessageObj.setUdhi(1);

            if ((FeatureCode.BINARY_MSG == aFeatureCode) || (FeatureCode.FLASH_UNICODE_MULTI == aFeatureCode) || (FeatureCode.UNICODE_MULTI == aFeatureCode))
            {
                aMessageObj.setMessage(lUdh + aHexMessage);
                mMessageRequest.setIsHexMessage(true);
            }
        }
    }

    private void handleSpecialPortSingle(
            MessagePart aMessageObj,
            FeatureCode aFeatureCode,
            String aMessage)
    {
        String lUdh = CommonUtility.nullCheck(aMessageObj.getUdh(), true);
        if ((lUdh.isBlank()) || (lUdh.length() < 1))
            lUdh = getPortBasedSinglepartUDH();

        aMessageObj.setUdh(lUdh);

        if (FeatureCode.SPECIAL_PORT_UNICODE_SINGLE == aFeatureCode)
        {
            aMessageObj.setMessage(lUdh + aMessage);
            aMessageObj.setUdhi(1);

            mMessageRequest.setIsHexMessage(true);
        }
    }

    private void handleSpecialPortMulti(
            MessagePart aMessageObj,
            FeatureCode aFeatureCode,
            String aMessage)
    {
        String lUdh = CommonUtility.nullCheck(aMessageObj.getUdh(), true);
        if ((lUdh.isBlank()) || (lUdh.length() < 1))
            lUdh = getPortBasedMultipartUDH(aMessageObj);

        aMessageObj.setUdh(lUdh);
        aMessageObj.setUdhi(1);

        if (FeatureCode.SPECIAL_PORT_UNICODE_MULTI == aFeatureCode)
        {
            aMessageObj.setMessage(lUdh + aMessage);
            mMessageRequest.setIsHexMessage(true);
        }
    }

    private String getPortBasedMultipartUDH(
            MessagePart aMessageObj)
    {
        final int    lSmsRefNumber      = aMessageObj.getConcatnateReferenceNumber();
        final String uniqueConcatId     = StringUtils.leftPad(Integer.toHexString(lSmsRefNumber), 2, "0");
        final int    lTotalMessageParts = mMessageRequest.getMessageTotalParts();
        final String lTotalMsg          = StringUtils.leftPad(Integer.toHexString(lTotalMessageParts), 2, "0");
        final int    lMessagePartNumber = aMessageObj.getMessagePartNumber();
        final String lPartNum           = StringUtils.leftPad(Integer.toHexString(lMessagePartNumber), 2, "0");
        return UdhHeaderInfo.CONCAT_PORT_MULTI_HEADER_PREFIX.getKey() + StringUtils.leftPad(Integer.toHexString(mMessageRequest.getDestinationPort()).toUpperCase(), 4, "0")
                + UdhHeaderInfo.CONCAT_PORT_MULTI_HEADER_SUFFIX.getKey() + uniqueConcatId + lTotalMsg + lPartNum;
    }

    private String getPortBasedSinglepartUDH()
    {
        return UdhHeaderInfo.CONCAT_PORT_HEADER_PREFIX.getKey() + StringUtils.leftPad(Integer.toHexString(mMessageRequest.getDestinationPort()).toUpperCase(), 4, "0")
                + UdhHeaderInfo.CONCAT_PORT_HEADER_SUFFIX.getKey();
    }

    private String getConcatenateUDH(
            MessagePart aMessageObj)
    {
        final int    lTotalMessageParts = mMessageRequest.getMessageTotalParts();
        final int    lSmsRefNumber      = aMessageObj.getConcatnateReferenceNumber();
        final int    lMessagePartNumber = aMessageObj.getMessagePartNumber();
        final String lTotalMsg          = StringUtils.leftPad(Integer.toHexString(lTotalMessageParts), 2, "0");
        final String lPartNum           = StringUtils.leftPad(Integer.toHexString(lMessagePartNumber), 2, "0");

        String       lConcatId          = "";

        if (lSmsRefNumber < 256)
        {
            lConcatId = StringUtils.leftPad(Integer.toHexString(lSmsRefNumber), 2, "0");
            return UdhHeaderInfo.CONCAT_8BIT_HEADER.getKey() + lConcatId + lTotalMsg + lPartNum;
        }

        lConcatId = StringUtils.leftPad(Integer.toHexString(lSmsRefNumber), 4, "0");
        return UdhHeaderInfo.CONCAT_16BIT_HEADER.getKey() + lConcatId + lTotalMsg + lPartNum;
    }

}