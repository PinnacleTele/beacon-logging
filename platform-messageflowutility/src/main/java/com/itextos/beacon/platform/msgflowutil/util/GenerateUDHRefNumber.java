package com.itextos.beacon.platform.msgflowutil.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;

public class GenerateUDHRefNumber
{

    private static final Log    log                   = LogFactory.getLog(GenerateUDHRefNumber.class);

    private static final String KEY_16_BIT_REF_NO     = "16.bit.udh.ref";
    private static final String KEY_8_BIT_REF_NO      = "8.bit.udh.ref";

    private static final int    INVALID_REF_NO        = -999;
    private static final int    DEFAULT_16_BIT_REF_NO = 255;
    private static final int    DEFAULT_8_BIT_REF_NO  = 1;

    private static final int    MAX_16_BIT_REF_NO     = 65535;
    private static final int    MAX_8_BIT_REF_NO      = 255;

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final GenerateUDHRefNumber INSTANCE = new GenerateUDHRefNumber();

    }

    public static GenerateUDHRefNumber getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private int _16_BitStartNo;
    private int _8_BitStartNo;

    private int _8_BitCurrentNo;
    private int _16_BitCurrentNo;

    private GenerateUDHRefNumber()
    {
        _16_BitStartNo = CommonUtility.getInteger(System.getProperty(KEY_16_BIT_REF_NO), INVALID_REF_NO);
        _8_BitStartNo  = CommonUtility.getInteger(System.getProperty(KEY_8_BIT_REF_NO), INVALID_REF_NO);

        if (_16_BitStartNo == INVALID_REF_NO)
        {
            _16_BitStartNo = DEFAULT_16_BIT_REF_NO;
            log.error("Proper value for the 16 bit ref number specified. '" + System.getProperty(KEY_16_BIT_REF_NO) + "'. Using default value = " + DEFAULT_16_BIT_REF_NO);
        }

        if (_8_BitStartNo == INVALID_REF_NO)
        {
            _8_BitStartNo = DEFAULT_8_BIT_REF_NO;
            log.error("Proper value for the 16 bit ref number specified. '" + System.getProperty(KEY_8_BIT_REF_NO) + "'. Using default value = " + DEFAULT_8_BIT_REF_NO);
        }
        _16_BitCurrentNo = _16_BitStartNo;
        _8_BitCurrentNo  = _8_BitStartNo;
    }

    public synchronized int get16BitRefNumber()
    {
        _16_BitCurrentNo = (_16_BitCurrentNo < MAX_16_BIT_REF_NO) ? ++_16_BitCurrentNo : _16_BitStartNo;
        return _16_BitCurrentNo;
    }

    public synchronized int get8BitRefNumber()
    {
        _8_BitCurrentNo = (_8_BitCurrentNo < MAX_8_BIT_REF_NO) ? ++_8_BitCurrentNo : _8_BitStartNo;
        return _8_BitCurrentNo;
    }

}