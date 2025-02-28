package com.itextos.beacon.smpp.utils;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.CustomFeatures;
import com.itextos.beacon.inmemdata.account.ClientAccountDetails;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.itextos.beacon.inmemdata.account.dao.AccountInfo;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.customfeatures.InmemCustomFeatures;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class AccountDetails
{

    private static final Log log = LogFactory.getLog(AccountDetails.class);

    private AccountDetails()
    {}

    public static void loadAccounts()
    {
        final long startTime = System.currentTimeMillis();

        if (log.isDebugEnabled())
            log.debug("Account loading start time : " + new Date(startTime));

        InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ACCOUNT_INFO);
        InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.SMPP_ACCOUNT_INFO);

        final long timeTaken = System.currentTimeMillis() - startTime;

        if (log.isDebugEnabled())
            log.debug("Account loading Time taken : " + timeTaken);
    }

    public static UserInfo getUserInfo(
            String aUsername)
    {
        final AccountInfo accountInfo = (AccountInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.ACCOUNT_INFO);
        final UserInfo    lUserIfo    = accountInfo.getUserByUser(aUsername);
        return ClientAccountDetails.getUserDetailsByClientId(lUserIfo.getClientId());
    }

    public static UserInfo getUserInfoByClientId(
            String aClientId)
    {
        return ClientAccountDetails.getUserDetailsByClientId(aClientId);
    }

    public static String getAccountCustomeFeature(
            String aClientId,
            CustomFeatures aCustomFeature)
    {
        final InmemCustomFeatures lCustomFeatures = (InmemCustomFeatures) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CUSTOM_FEATURES);
        return lCustomFeatures.getValueOfCustomFeature(aClientId, aCustomFeature.getKey());
    }

    public static String getConfigParamsValueAsString(
            ConfigParamConstants aKey)
    {
        final ApplicationConfiguration lAppConfigValues = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfigValues.getConfigValue(aKey.getKey());
    }

}