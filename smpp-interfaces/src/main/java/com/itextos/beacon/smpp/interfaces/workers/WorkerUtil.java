package com.itextos.beacon.smpp.interfaces.workers;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cloudhopper.commons.charset.Charset;
import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.SmppInvalidArgumentException;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.timezoneutility.TimeZoneUtility;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.smpp.account.SmppAccInfo;
import com.itextos.beacon.inmemory.smpp.account.util.SmppAccUtil;
import com.itextos.beacon.platform.smpputil.DeliverSmInfo;
import com.itextos.beacon.smpp.objects.SmppUserInfo;
import com.itextos.beacon.smpp.utils.AccountDetails;
import com.itextos.beacon.smpp.utils.enums.SmppCharset;
import com.itextos.beacon.smpp.utils.generator.SequenceNumber;

public class WorkerUtil
{

    private static final Log      log                   = LogFactory.getLog(WorkerUtil.class);

    private static final String[] EXPECTED_TIME_FORMATS =
    { "yyMMddHHmm", "yyMMddHHmmss" };

    private static final String[] TIME_LABLES           =
    { "submit date", "done date" };

    private WorkerUtil()
    {}

    static DeliverSm getDeliverSmRequest(
            String serviceType,
            byte esm,
            String datacoding,
            String dnMsg,
            String srcAddress,
            String destAddress,
            DeliverSmInfo aDeliveySmInfo)
            throws SmppInvalidArgumentException
    {
        final DeliverSm request = new DeliverSm();

        request.setServiceType(serviceType);
        request.setEsmClass(esm);
        request.setProtocolId((byte) 0x00);
        request.setPriority((byte) 0x00);
        request.setRegisteredDelivery((byte) 0x00);

        if ((datacoding != null) && datacoding.trim().equals("8"))
            request.setDataCoding((byte) 0x08);
        else
            request.setDataCoding((byte) 0x00);

        byte sourceton = 0;
        byte sourcenpi = 0;
        byte destton   = 0;
        byte destnpi   = 0;

        // Google Requirement
        if (aDeliveySmInfo.getMsgId() == null)
        {
            sourceton = 0x00;
            sourcenpi = 0x01;
            destton   = 0x00;
            destnpi   = 0x01;
        }
        else
        {
            sourceton = 0x01;
            sourcenpi = 0x01;
            destton   = 0x01;
            destnpi   = 0x01;
        }

        final Address sourceAddress      = new Address(sourceton, sourcenpi, srcAddress);
        final Address destinationAddress = new Address(destton, destnpi, destAddress);
        request.setDestAddress(sourceAddress);
        request.setSourceAddress(destinationAddress);
        request.setShortMessage(dnMsg.getBytes());

        return request;
    }

    public static DeliverSm getDeliverSmRequest(
            String aServiceType,
            byte aEsmClass,
            int aDcs,
            String dnMsg,
            String aSourceAddress,
            String aDestAddress,
            DeliverSmInfo aDnPostLogInfo,
            SmppUserInfo aUserInfo)
            throws SmppInvalidArgumentException
    {
        final DeliverSm request = new DeliverSm();
        request.setSequenceNumber(SequenceNumber.getInstance().getNextId());
        request.setServiceType(aServiceType);
        request.setEsmClass(aEsmClass);
        request.setProtocolId((byte) 0x00);
        request.setPriority((byte) 0x00);
        request.setRegisteredDelivery((byte) 0x00);

        // For MO message the dcs sending as received
        // from operator if the message is Unicode
        if ((aDnPostLogInfo.getMsgId() == null) && (aDcs == 8))
            request.setDataCoding((byte) 0x08);
        else
            request.setDataCoding((byte) 0x00);

        byte sourceton = 0;
        byte sourcenpi = 0;
        byte destton   = 0;
        byte destnpi   = 0;

        // Google Requirement
        if (aDnPostLogInfo.getMsgId() == null)
        {
            sourceton = 0x00;
            sourcenpi = 0x01;
            destton   = 0x00;
            destnpi   = 0x01;
        }
        else
        {
            sourceton = 0x01;
            sourcenpi = 0x01;
            destton   = 0x01;
            destnpi   = 0x01;
        }

        if (log.isDebugEnabled())
            log.debug("Source Address : '" + aSourceAddress + "', Dest Adddress: '" + aDestAddress + "'");

        Address sourceAddress      = new Address(sourceton, sourcenpi, aSourceAddress);
        Address destinationAddress = new Address(destton, destnpi, aDestAddress);

        String  lStandaredTonNpi   = getStandaredTonNpi(aDnPostLogInfo.getClientId());
        if (log.isDebugEnabled())
            log.debug("Enable Global Standared TON/NPI - " + lStandaredTonNpi);

        if ((lStandaredTonNpi == null) || "0".equals(lStandaredTonNpi))
        {
            lStandaredTonNpi = AccountDetails.getAccountCustomeFeature(aDnPostLogInfo.getClientId(), CustomFeatures.STANDARD_TON_NPI_ENABLE);
            if (log.isDebugEnabled())
                log.debug("Enable Account Custom Standared TON/NPI - " + lStandaredTonNpi);
        }

        if ((lStandaredTonNpi != null) && "1".equals(lStandaredTonNpi))
        {
            // SMPP standard TON/NPI setting enabled.

            sourceAddress = setTON_NPI(aSourceAddress);

            if (log.isDebugEnabled())
                log.debug("SourceAddress -[" + aSourceAddress + "] - TON/NPI Value - [" + sourceAddress.getTon() + "/" + sourceAddress.getNpi() + "]");

            destinationAddress = setTON_NPI(aDestAddress);

            if (log.isDebugEnabled())
                log.debug("DestinationAddress -[" + aDestAddress + "] - TON/NPI Value - [" + destinationAddress.getTon() + "/" + destinationAddress.getNpi() + "]");
        }

        request.setDestAddress(sourceAddress);
        request.setSourceAddress(destinationAddress);

        final SmppAccInfo lSmppAccInfo = SmppAccUtil.getSmppAccountInfo(aDnPostLogInfo.getClientId());

        String            lCharSet     = "";
        if (lSmppAccInfo != null)
            lCharSet = lSmppAccInfo.getCharSet();

        if ((dnMsg != null) && !dnMsg.isEmpty())
        {
            if (log.isDebugEnabled())
                log.debug("DN String :" + dnMsg);

            /*
            try
            {
                convertMessageFromMessage(aDcs, dnMsg.getBytes(), request, lCharSet);
            }
            catch (final Exception e)
            {
                request.setShortMessage(dnMsg.getBytes());
            }
            */
            request.setShortMessage(dnMsg.getBytes());

        }
        else
        {
            if (log.isDebugEnabled())
                log.debug("DN String for Blank/Null : " + dnMsg);
            request.setShortMessage("".getBytes());
        }

        /*
         * final String carrier = "";
         * final String circle = "";
         * if ((carrier != null) && (carrier.trim().length() > 0) && (circle != null) &&
         * (circle.trim().length() != 0))
         * {
         * final Tlv tlv = new Tlv((short) 0x1401, (carrier + "," + circle).getBytes());
         * request.setOptionalParameter(tlv);
         * }
         */

        return request;
    }

    private static String getStandaredTonNpi(
            String aClientId)
    {
        String isStandaredTonNpi = CommonUtility.nullCheck(AccountDetails.getAccountCustomeFeature("*", CustomFeatures.STANDARD_TON_NPI_ENABLE), true);
        if (log.isDebugEnabled())
            log.debug("Enable Global Standared TON/NPI - " + isStandaredTonNpi);

        if (isStandaredTonNpi.isBlank() || "0".equals(isStandaredTonNpi))
        {
            isStandaredTonNpi = CommonUtility.nullCheck(AccountDetails.getAccountCustomeFeature(aClientId, CustomFeatures.STANDARD_TON_NPI_ENABLE), true);
            if (log.isDebugEnabled())
                log.debug("Enable Account Custom Standared TON/NPI - " + isStandaredTonNpi);
        }
        return isStandaredTonNpi;
    }

    /**
     * Use this method to set the SMPP-3.4 standard TON/NPI values.
     *
     * @param adress
     *
     * @return
     */
    private static Address setTON_NPI(
            String address)
    {
        byte valueTON = 0x01;
        byte valueNPI = 0x01;

        try
        {

            if ((address != null) && (address.length() > 0))
            {

                // Alphanumeric source/destination address
                if (!address.matches("\\d+") && !address.startsWith("+"))
                {
                    valueTON = 0x05;
                    valueNPI = 0x00;
                }

                // Numeric short code [length of 3 to 8 digit]
                if (address.matches("\\d+"))
                    if (address.matches("^[0-9]{3,8}$"))
                    {
                        valueTON = 0x03;
                        valueNPI = 0x00;
                    }
                    else
                        // Domestic / International source/destination address.
                        if ((address.length() == 12) && address.startsWith("91"))
                        {
                            valueTON = 0x02;
                            valueNPI = 0x01;
                        }
                        else
                            if (address.length() > 1)
                            {
                                valueTON = 0x01;
                                valueNPI = 0x01;
                            }
                            else
                            {
                                // Sender/Destination is empty.
                                valueTON = 0x00;
                                valueNPI = 0x00;
                            }
            }
            else
            {
                // Sender/Destination is empty.
                valueTON = 0x00;
                valueNPI = 0x00;
            }
        }
        catch (final Exception e)
        {
            log.error(" Exception occer while setting SMPP standard TON/NPI values ., Hence setting default values..", e);
        }

        if (log.isDebugEnabled())
            log.debug("setTON_NPI Address - [" + address + "] - TON Value - [" + valueTON + " ] - NPI Value - [" + valueNPI + "]");

        final Address _address = new Address(valueTON, valueNPI, address);

        return _address;
    }

    static String getMsgId(
            String aShortMsg)
    {

        try
        {
            final String[] strArray = aShortMsg.split(" ");

            for (final String data : strArray)
            {
                final String[] str = data.split(":");

                if (str[0].equalsIgnoreCase("id"))
                    return str[1];
            }
        }
        catch (final Exception exp)
        {
            log.error("no mid found..." + aShortMsg, exp);
        }
        return null;
    }

    private static String getKeyValueFromShortMsg(
            String dnMsg,
            String key)
    {

        try
        {
            // Getting the starting index of key
            int startIndex = dnMsg.indexOf(key);

            if (startIndex > 0)
            {
                startIndex += key.length();
                final int endIndex = dnMsg.indexOf(" ", startIndex);
                return dnMsg.substring(startIndex + 1, endIndex);
            }
            else
                log.error("no key " + key + " found..." + dnMsg);
        }
        catch (final Exception exp)
        {
            log.error("Exception @ getKeyValueFromShortMsg-" + dnMsg, exp);
        }
        return null;
    }

    static String convertTimeFromIstToIntlTimeZone(
            String dnMsg,
            String aTimeOffSet,
            String msgid,
            StringBuffer sb)
    {
        String time = null;

        sb.append(msgid+" aTimeOffSet : "+aTimeOffSet).append("\n");

        try
        {

            for (final String timeLable : TIME_LABLES)
            {
                time = getKeyValueFromShortMsg(dnMsg, timeLable);

                sb.append(msgid+" timeLable : "+time).append("\n");

                if (time != null)
                {
                    final String timeInTz = convertTime(time, aTimeOffSet,msgid,sb);

                    sb.append(msgid+" timeInTz : "+timeInTz).append("\n");

                    if (timeInTz != null)
                        dnMsg = dnMsg.replace(timeLable + ":" + time, timeLable + ":" + timeInTz);
               
                
                    sb.append(msgid+" dnMsg : "+dnMsg).append("\n");

                }
            }
        }
        catch (final Exception e)
        {
            log.error("Exception @ convertTimeFromIstToIntlTimeZone " + dnMsg);
        }

        return dnMsg;
    }

    private static String convertTime(
            String time,
            String aTimeOffSet,
            String msgid,
            StringBuffer sb)
    {
            try
            {
                DateTimeFormat lTimeForMat = DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM_SS;
                
                sb.append(msgid+"  aTime length "+time.length()+" aTimeFormat length"+DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM_SS.getKey().length()).append("\n");

                if (time.length()!=DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM_SS.getKey().length())
                    lTimeForMat = DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM;

                sb.append(msgid+"Choosen Time And Format aTime "+time+" aTimeFormat "+lTimeForMat).append("\n");

                return changeTimeToGivenOffset(aTimeOffSet, time, lTimeForMat,msgid,sb);
            }
            catch (final Exception ignore)
            {}

        return null;
    }

    public static String changeTimeToGivenOffset(
            String aTimeZone,
            String aTime,
            DateTimeFormat aTimeFormat,
            String msgid,
            StringBuffer sb)
    {
        final Date lDate = TimeZoneUtility.getDateBasedOnTimeZone(aTime, aTimeFormat, aTimeZone);

        sb.append(msgid+" aTime "+aTime+" aTimeFormat "+aTimeFormat+" aTimeZone : "+aTimeZone+" After converting to IST lDate "+lDate).append("\n");

        if (log.isDebugEnabled())
            log.debug("After converting to IST : '" + lDate + "'");


        return DateTimeUtility.getFormattedDateTime(lDate, aTimeFormat);
    }

    private static void convertMessageFromMessage(
            int sDcs,
            byte[] aMessageInBytes,
            DeliverSm request,
            String aCharSet)
            throws Exception
    {
        if ((sDcs == 3) || (sDcs == 1))
            aCharSet = SmppCharset.ISO_8859_1.getKey();

        if (log.isInfoEnabled())
            log.info("current charset=" + aCharSet);

        Charset           toUserCharSet = CharsetUtil.CHARSET_ISO_8859_1;

        final SmppCharset lCharset      = SmppCharset.getCharset(aCharSet);

        switch (lCharset)
        {
            case GSM:
                toUserCharSet = CharsetUtil.CHARSET_GSM;
                break;

            case ISO_8859_1:
                toUserCharSet = CharsetUtil.CHARSET_ISO_8859_1;
                break;

            case ISO_8859_15:
                toUserCharSet = CharsetUtil.CHARSET_ISO_8859_15;
                break;

            case UTF_8:
                toUserCharSet = CharsetUtil.CHARSET_UTF_8;
                break;

            case UCS_2:
                toUserCharSet = CharsetUtil.CHARSET_UCS_2;
                break;

            case GSM8:
                toUserCharSet = CharsetUtil.CHARSET_GSM8;
                break;

            case GSM7:
                toUserCharSet = CharsetUtil.CHARSET_GSM7;
                break;

            default:
                toUserCharSet = CharsetUtil.CHARSET_ISO_8859_1;
                break;
        }

        final String mMessage = CharsetUtil.decode(aMessageInBytes, toUserCharSet);

        if (log.isDebugEnabled())
            log.debug("Message after conversion--->" + mMessage);

        request.setShortMessage(mMessage.getBytes());
    }

    public static void main(String args[]) {
    	
    int l=	DateTimeFormat.NO_SEPARATOR_YY_MM_DD_HH_MM_SS.getKey().length();
    
    System.out.println(l);
    }
}
