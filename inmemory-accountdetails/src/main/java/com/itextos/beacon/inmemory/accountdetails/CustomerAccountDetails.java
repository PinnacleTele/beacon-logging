package com.itextos.beacon.inmemory.accountdetails;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.AccountStatus;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class CustomerAccountDetails
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log                 log                    = LogFactory.getLog(CustomerAccountDetails.class);

    private Map<String, String>        mAccountInfoByUser     = new HashMap<>();
    private Map<String, AccountStatus> mAccountStatusByClient = new HashMap<>();

    public CustomerAccountDetails(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public String getClientIdByUser(
            String aUser)
    {
        final String temp = CommonUtility.nullCheck(aUser, true).toLowerCase();
        return mAccountInfoByUser.get(temp);
    }

    public AccountStatus getAccountStatusByUser(
            String aUser)
    {
        return getAccountStatusByClientId(getClientIdByUser(aUser));
    }

    public AccountStatus getAccountStatusByClientId(
            String aClientId)
    {
        final String temp = CommonUtility.nullCheck(aClientId, true);
        return mAccountStatusByClient.get(temp);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, String>        lAccountInfoByUser     = new HashMap<>();
        final Map<String, AccountStatus> lAccountStatusByClient = new HashMap<>();

        while (aResultSet.next())
        {
            final String user     = aResultSet.getString("user").toLowerCase();
            final String clientId = aResultSet.getString("cli_id");

            try
            {
                final AccountStatus lAccountStatus = AccountStatus.getAccountStatus(aResultSet.getInt("acc_status"));

                lAccountInfoByUser.put(user, clientId);
                lAccountStatusByClient.put(clientId, lAccountStatus);
            }
            catch (final Exception e)
            {
                log.error("Exception while getting the account information from the database for the client User '" + user + "' Client Id '" + clientId + "'", e);
            }
        }
        mAccountInfoByUser     = lAccountInfoByUser;
        mAccountStatusByClient = lAccountStatusByClient;
    }

}