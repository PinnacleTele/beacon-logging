package com.itextos.beacon.smpp.utils;

import java.util.EnumMap;
import java.util.Map;

import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;

public abstract class UdhExtractor
{

    private UdhExtractor()
    {}

    public static Map<MiddlewareConstant, String> extractParams(
            String aUdh,
            boolean aCnExtractUdh)
    {
        final Map<MiddlewareConstant, String> lUdhExtractMap    = new EnumMap<>(MiddlewareConstant.class);
        String                                currentIde        = null;
        String                                currentDataLength = null;

        try
        {

            if (aCnExtractUdh)
            {
                currentIde        = aUdh.substring(2, 4);
                currentDataLength = aUdh.substring(4, 6);
            }
            else
            {
                currentIde        = aUdh.substring(0, 2);
                currentDataLength = aUdh.substring(2, 4);
            }

            switch (currentIde)
            {
                case "00":
                    processFor_00(aUdh, aCnExtractUdh, lUdhExtractMap);
                    break;

                case "05":
                    processFor_05(aUdh, aCnExtractUdh, lUdhExtractMap);
                    break;

                case "04":
                    processFor_04(aUdh, aCnExtractUdh, lUdhExtractMap);
                    break;

                case "08":
                    processFor_08(aUdh, aCnExtractUdh, lUdhExtractMap);
                    break;

                default:
                    // TODO What about other cases?
                    break;
            }

            int length = Integer.parseInt(currentDataLength, 16) * 2;

            if (aCnExtractUdh)
                length += 6;
            else
                length += 4;

            if (length <= aUdh.length())
                aUdh = aUdh.substring(length, aUdh.length());
            else // no concat udh
                throw new ItextosException("can't parse udh problem with data length");

            if (aUdh.length() > 0)
                lUdhExtractMap.putAll(extractParams(aUdh, false));
        }
        catch (final Exception exp)
        {
            lUdhExtractMap.put(MiddlewareConstant.MW_CONCAT_REF_NUM, "0");
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_TOTAL_PARTS, "0");
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_PART_NUMBER, "0");
            lUdhExtractMap.put(MiddlewareConstant.MW_DESTINATION_PORT, "");
        }

        return lUdhExtractMap;
    }

    private static void processFor_00(
            String aUdh,
            boolean aCnExtractUdh,
            Map<MiddlewareConstant, String> lUdhExtractMap)
    {

        if (aCnExtractUdh)
        {
            lUdhExtractMap.put(MiddlewareConstant.MW_CONCAT_REF_NUM, Integer.toString(Integer.parseInt(aUdh.substring(6, 8), 16)));
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_TOTAL_PARTS, Integer.toString(Integer.parseInt(aUdh.substring(8, 10), 16)));
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_PART_NUMBER, Integer.toString(Integer.parseInt(aUdh.substring(10, 12), 16)));
        }
        else
        {
            lUdhExtractMap.put(MiddlewareConstant.MW_CONCAT_REF_NUM, Integer.toString(Integer.parseInt(aUdh.substring(4, 6), 16)));
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_TOTAL_PARTS, Integer.toString(Integer.parseInt(aUdh.substring(6, 8), 16)));
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_PART_NUMBER, Integer.toString(Integer.parseInt(aUdh.substring(8, 10), 16)));
        }
    }

    private static void processFor_05(
            String aUdh,
            boolean aCnExtractUdh,
            Map<MiddlewareConstant, String> lUdhExtractMap)
    {

        if (aCnExtractUdh)
        {
            lUdhExtractMap.put(MiddlewareConstant.MW_CONCAT_REF_NUM, "0");
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_TOTAL_PARTS, "0");
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_PART_NUMBER, "0");
            lUdhExtractMap.put(MiddlewareConstant.MW_DESTINATION_PORT, Integer.toString(Integer.parseInt(aUdh.substring(6, 10), 16)));
        }
        else
        {
            lUdhExtractMap.put(MiddlewareConstant.MW_CONCAT_REF_NUM, "0");
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_TOTAL_PARTS, "0");
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_PART_NUMBER, "0");
            lUdhExtractMap.put(MiddlewareConstant.MW_DESTINATION_PORT, Integer.toString(Integer.parseInt(aUdh.substring(4, 8), 16)));
        }
    }

    private static void processFor_04(
            String aUdh,
            boolean aCnExtractUdh,
            Map<MiddlewareConstant, String> lUdhExtractMap)
    {

        if (aCnExtractUdh)
        {
            lUdhExtractMap.put(MiddlewareConstant.MW_CONCAT_REF_NUM, "0");
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_TOTAL_PARTS, "0");
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_PART_NUMBER, "0");
            lUdhExtractMap.put(MiddlewareConstant.MW_DESTINATION_PORT, Integer.toString(Integer.parseInt(aUdh.substring(6, 8), 16)));
        }
        else
        {
            lUdhExtractMap.put(MiddlewareConstant.MW_CONCAT_REF_NUM, "0");
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_TOTAL_PARTS, "0");
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_PART_NUMBER, "0");
            lUdhExtractMap.put(MiddlewareConstant.MW_DESTINATION_PORT, Integer.toString(Integer.parseInt(aUdh.substring(4, 6), 16)));
        }
    }

    private static void processFor_08(
            String aUdh,
            boolean aCnExtractUdh,
            Map<MiddlewareConstant, String> lUdhExtractMap)
    {

        if (aCnExtractUdh)
        {
            lUdhExtractMap.put(MiddlewareConstant.MW_CONCAT_REF_NUM, Integer.toString(Integer.parseInt(aUdh.substring(6, 10), 16)));
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_TOTAL_PARTS, Integer.toString(Integer.parseInt(aUdh.substring(10, 12), 16)));
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_PART_NUMBER, Integer.toString(Integer.parseInt(aUdh.substring(12, 14), 16)));
        }
        else
        {
            lUdhExtractMap.put(MiddlewareConstant.MW_CONCAT_REF_NUM, Integer.toString(Integer.parseInt(aUdh.substring(4, 8), 16)));
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_TOTAL_PARTS, Integer.toString(Integer.parseInt(aUdh.substring(8, 10), 16)));
            lUdhExtractMap.put(MiddlewareConstant.MW_MSG_PART_NUMBER, Integer.toString(Integer.parseInt(aUdh.substring(10, 12), 16)));
        }
    }

}