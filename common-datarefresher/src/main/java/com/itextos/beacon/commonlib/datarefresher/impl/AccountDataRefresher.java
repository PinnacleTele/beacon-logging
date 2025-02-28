package com.itextos.beacon.commonlib.datarefresher.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.accountsync.AccountLoader;
import com.itextos.beacon.commonlib.datarefresher.dataobjects.AbstractDataRefresher;

public class AccountDataRefresher
        extends
        AbstractDataRefresher
{

    private static final Log log = LogFactory.getLog(AccountDataRefresher.class);

    @Override
    public int insertData(
            List<String> aList)
    {

        try
        {
            AccountLoader.loadAccountData(aList);
            return aList.size();
        }
        catch (final Exception e)
        {
            log.error("Exception while synching account details into redis", e);
            return -1;
        }
    }

    @Override
    public int updateData(
            List<String> aList)
    {

        try
        {
            AccountLoader.loadAccountData(aList);
            return aList.size();
        }
        catch (final Exception e)
        {
            log.error("Exception while synching account details into redis", e);
            return -1;
        }
    }

    @Override
    public int deleteData(
            List<String> aList)
    {

        try
        {
            if (!aList.isEmpty())
                AccountLoader.deleteAccount(aList);
            return aList.size();
        }
        catch (final Exception e)
        {
            log.error("Exception while removing the account data from Redis.", e);
            return -1;
        }
    }

}