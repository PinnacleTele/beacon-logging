package com.itextos.smpp.client.util;

import org.apache.commons.lang3.StringUtils;

public class GenerateUDHRefNumber
{

    private static final int DEFAULT_16_BIT_REF_NO = 255;
    private static final int MAX_16_BIT_REF_NO     = 65535;

    private static final int DEFAULT_8_BIT_REF_NO  = 1;
    private static final int MAX_8_BIT_REF_NO      = 255;

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final GenerateUDHRefNumber INSTANCE = new GenerateUDHRefNumber();

    }

    public static GenerateUDHRefNumber getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final int udhRefNumberStart_8    = DEFAULT_8_BIT_REF_NO;
    private int       udhRefNumberCurrent_8  = udhRefNumberStart_8;
    private final int udhRefNumberStart_16   = DEFAULT_16_BIT_REF_NO;
    private int       udhRefNumberCurrent_16 = udhRefNumberStart_16;

    private GenerateUDHRefNumber()
    {}

    public int getNextRefNumber()
    {
        if (SmppClientProperties.getInstance().is16BitUdfRef())
            return get16BitRefNumber();
        return get8BitRefNumber();
    }

    private synchronized int get16BitRefNumber()
    {
        udhRefNumberCurrent_16 = (udhRefNumberCurrent_16 < MAX_16_BIT_REF_NO) ? ++udhRefNumberCurrent_16 : udhRefNumberStart_16;
        return udhRefNumberCurrent_16;
    }

    private synchronized int get8BitRefNumber()
    {
        udhRefNumberCurrent_8 = (udhRefNumberCurrent_8 < MAX_8_BIT_REF_NO) ? ++udhRefNumberCurrent_8 : udhRefNumberStart_8;
        return udhRefNumberCurrent_8;
    }

    public static void main(
            String[] args)
    {

        for (int i = 1; i < 150; i++)
        {
            System.out.println("i->" + i);
            final int a = GenerateUDHRefNumber.getInstance().get16BitRefNumber();
            System.out.println("-> " + a);

            final String h = Integer.toHexString(a);
            System.out.println("h>" + h);

            if (h.length() != 4)
            {
                final String ss = StringUtils.leftPad(h, 4, "0");
                final String s  = ss.substring(0, 2);
                System.out.println("SU > " + s);
                final String as = ss.substring(2);
                System.out.println("AS >" + as);
                System.out.println("SS >" + ss);
                final byte b = Byte.parseByte(as);

                System.out.println("Byte b :" + b);

                System.out.println("Hes :" + String.format("%02x", b));
            }
            else
            {
                final byte b = Byte.parseByte(h);

                System.out.println("P Byte b :" + b);

                System.out.println("P Hes :" + String.format("%02x", b));
            }
            // final String s = HexUtil.toHexString(a);
            // System.out.println(s);

            // System.out.println(Character.forDigit((s.getBytes()[0] >> 4) & 0xF, 16));
            // System.out.println(HexUtil.toHexString(refNu));
            // System.out.println(Byte.parseByte(HexUtil.toHexString(GenerateUDHRefNumber.getInstance().getNextRefNumber()),
            // 16));
        }
    }

}