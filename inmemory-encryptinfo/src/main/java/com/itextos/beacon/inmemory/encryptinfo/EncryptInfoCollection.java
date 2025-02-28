package com.itextos.beacon.inmemory.encryptinfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class EncryptInfoCollection
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log         log                                 = LogFactory.getLog(EncryptInfoCollection.class);
    private static final int         COL_INDEX_CLI_ID                    = 1;
    private static final int         COL_INDEX_INCOMING_CRYPTO_TYPE      = 2;
    private static final int         COL_INDEX_INCOMING_CRYPTO_ALOGRITHM = 3;
    private static final int         COL_INDEX_BILLING_CRYPTO_TYPE       = 4;
    private static final int         COL_INDEX_BILLING_CRYPTO_ALGORITHM  = 5;
    private static final int         COL_INDEX_BILLING_CRYPTO_COLUMNS    = 6;
    private static final int         COL_INDEX_HANDOVER_CRYPTO_TYPE      = 7;
    private static final int         COL_INDEX_HANDOVER_CRYPTO_ALGORITHM = 8;
    private static final int         COL_INDEX_INCOMING_CRYPTO_PARAM_1   = 9;
    private static final int         COL_INDEX_INCOMING_CRYPTO_PARAM_2   = 10;
    private static final int         COL_INDEX_INCOMING_CRYPTO_PARAM_3   = 11;
    private static final int         COL_INDEX_INCOMING_CRYPTO_PARAM_4   = 12;
    private static final int         COL_INDEX_INCOMING_CRYPTO_PARAM_5   = 13;
    private static final int         COL_INDEX_BILLING_CRYPTO_PARAM_1    = 14;
    private static final int         COL_INDEX_BILLING_CRYPTO_PARAM_2    = 15;
    private static final int         COL_INDEX_BILLING_CRYPTO_PARAM_3    = 16;
    private static final int         COL_INDEX_BILLING_CRYPTO_PARAM_4    = 17;
    private static final int         COL_INDEX_BILLING_CRYPTO_PARAM_5    = 18;
    private static final int         COL_INDEX_HANDOVER_CRYPTO_PARAM_1   = 19;
    private static final int         COL_INDEX_HANDOVER_CRYPTO_PARAM_2   = 20;
    private static final int         COL_INDEX_HANDOVER_CRYPTO_PARAM_3   = 21;
    private static final int         COL_INDEX_HANDOVER_CRYPTO_PARAM_4   = 22;
    private static final int         COL_INDEX_HANDOVER_CRYPTO_PARAM_5   = 23;

    private Map<String, EncryptInfo> mEncryptInfoMap                     = new HashMap<>();

    public EncryptInfoCollection(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public EncryptInfo getEncryptInfo(
            String aClientId)
    {
        return mEncryptInfoMap.get(aClientId);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        final Map<String, EncryptInfo> tempEncryptInfoMap = new HashMap<>();

        while (aResultSet.next())
        {
            final EncryptInfo eInfo = new EncryptInfo(aResultSet.getString(COL_INDEX_CLI_ID), CommonUtility.getInteger(aResultSet.getString(COL_INDEX_INCOMING_CRYPTO_TYPE), 0),
                    CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_INCOMING_CRYPTO_ALOGRITHM), true), CommonUtility.getInteger(aResultSet.getString(COL_INDEX_BILLING_CRYPTO_TYPE), 0),
                    CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_BILLING_CRYPTO_ALGORITHM), true), CommonUtility.getInteger(aResultSet.getString(COL_INDEX_BILLING_CRYPTO_COLUMNS), 0),
                    CommonUtility.getInteger(aResultSet.getString(COL_INDEX_HANDOVER_CRYPTO_TYPE), 0), CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_HANDOVER_CRYPTO_ALGORITHM), true));

            eInfo.setIncomingCryptoParam1(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_INCOMING_CRYPTO_PARAM_1), true));
            eInfo.setIncomingCryptoParam2(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_INCOMING_CRYPTO_PARAM_2), true));
            eInfo.setIncomingCryptoParam3(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_INCOMING_CRYPTO_PARAM_3), true));
            eInfo.setIncomingCryptoParam4(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_INCOMING_CRYPTO_PARAM_4), true));
            eInfo.setIncomingCryptoParam5(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_INCOMING_CRYPTO_PARAM_5), true));

            eInfo.setBillingCryptoParam1(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_BILLING_CRYPTO_PARAM_1), true));
            eInfo.setBillingCryptoParam2(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_BILLING_CRYPTO_PARAM_2), true));
            eInfo.setBillingCryptoParam3(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_BILLING_CRYPTO_PARAM_3), true));
            eInfo.setBillingCryptoParam4(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_BILLING_CRYPTO_PARAM_4), true));
            eInfo.setBillingCryptoParam5(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_BILLING_CRYPTO_PARAM_5), true));

            eInfo.setHandoverCryptoParam1(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_HANDOVER_CRYPTO_PARAM_1), true));
            eInfo.setHandoverCryptoParam2(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_HANDOVER_CRYPTO_PARAM_2), true));
            eInfo.setHandoverCryptoParam3(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_HANDOVER_CRYPTO_PARAM_3), true));
            eInfo.setHandoverCryptoParam4(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_HANDOVER_CRYPTO_PARAM_4), true));
            eInfo.setHandoverCryptoParam5(CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_HANDOVER_CRYPTO_PARAM_5), true));

            tempEncryptInfoMap.put(eInfo.getClientId(), eInfo);
        }
        if (!tempEncryptInfoMap.isEmpty())
            mEncryptInfoMap = tempEncryptInfoMap;
    }

}