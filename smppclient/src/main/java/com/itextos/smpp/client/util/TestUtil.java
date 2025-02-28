package com.itextos.smpp.client.util;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.util.HexUtil;

public class TestUtil
{

    public static void main(
            String[] args)
    {
        verifyUnicode();
    }

    private static void verifyUnicode()
    {

        try
        {
            final String msg        = "1234 புரட்சி தலைவி அம்மாவின்";

            final byte[] ad         = msg.getBytes("UTF-8");

            final byte[] encodedMsg = CharsetUtil.encode(msg, CharsetUtil.CHARSET_UCS_2);

            final String decodedMsg = CharsetUtil.decode(encodedMsg, CharsetUtil.CHARSET_GSM);

            System.out.println("Message : " + decodedMsg);

            System.out.println("Hex :" + HexUtil.toHexString(encodedMsg));
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private void getHexContent()
    {
        final byte[] c = HexUtil.toByteArray(
                "4869201b2823766172231b292c2054686973206973201b2823766172231b292066726f6d2045786f74656c3b2049207472696564207265616368696e6720796f75206f6e20796f7572206e756d626572206275742077617320756e61626c6520746f20636f6e6e6563743b20506c65617365206665656c206672656520746f207265616368206d65206f6e201b2823766172231b29206f72");

        System.out.println(new String(c));
        System.out.println(HexUtil.toHexString(c));
        System.out.println(CharsetUtil.decode(HexUtil.toHexString(c).getBytes(), CharsetUtil.CHARSET_GSM));

        final byte[] ls = HexUtil.toByteArray(
                "4869201b2823766172231b292c2054686973206973201b2823766172231b292066726f6d2045786f74656c3b2049207472696564207265616368696e6720796f75206f6e20796f7572206e756d626572206275742077617320756e61626c6520746f20636f6e6e6563743b20506c65617365206665656c206672656520746f207265616368206d65206f6e201b2823766172231b29206f72");

        System.out.println(CharsetUtil.decode(ls, CharsetUtil.CHARSET_GSM));
    }

}
