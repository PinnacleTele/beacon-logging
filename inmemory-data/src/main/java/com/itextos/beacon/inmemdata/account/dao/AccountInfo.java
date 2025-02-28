package com.itextos.beacon.inmemdata.account.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.accountsync.AccountLoader;
import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.pwdencryption.Encryptor;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemdata.account.UserInfo;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class AccountInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log      log                     = LogFactory.getLog(AccountInfo.class);
    // select cli_id, user, ui_pass, api_pass, smpp_pass, acc_status from
    // accounts_view
    private static final int      COL_INDEX_CLIENT_ID     = 1;
    private static final int      COL_INDEX_USER_NAME     = 2;
    private static final int      COL_INDEX_UI_PASSWORD   = 3; // Not used.
    private static final int      COL_INDEX_API_PASSWORD  = 4;
    private static final int      COL_INDEX_SMPP_PASSWORD = 5;
    private static final int      COL_INDEX_STATUS        = 6;

    private Map<String, UserInfo> userPassMap             = new HashMap<>();
    private Map<String, UserInfo> accessKeyMap            = new HashMap<>();
    private Map<String, UserInfo> clientIdMap             = new HashMap<>();

    public AccountInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Loading db records into inmemory");

        final Map<String, UserInfo> tempUserPassMap  = new HashMap<>();
        final Map<String, UserInfo> tempAccessKeyMap = new HashMap<>();
        final Map<String, UserInfo> tempClientIdMap  = new HashMap<>();

        while (aResultSet.next())
            try
            {
                final String clientId          = aResultSet.getString(COL_INDEX_CLIENT_ID);

                final String lApiPass          = CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_API_PASSWORD), true);
                final String lSmppPass         = CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_SMPP_PASSWORD), true);

                final String decryptedApiPass  = decryptApiPassword(lApiPass, clientId);
                final String decryptedSmppPass = decryptSmppPassword(lSmppPass, clientId);

                final String cliId             = CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_CLIENT_ID), true);
                final String userName          = CommonUtility.nullCheck(aResultSet.getString(COL_INDEX_USER_NAME), true).toLowerCase();

                if (cliId.isBlank())
                    throw new ItextosException("Invalid client Id specified.");

                if (userName.isBlank())
                    throw new ItextosException("Invalid username specified.");

                final UserInfo userInfo = new UserInfo(cliId, userName, decryptedApiPass, decryptedSmppPass, CommonUtility.getInteger(aResultSet.getString(COL_INDEX_STATUS), -1));

                tempUserPassMap.put(userInfo.getUserName(), userInfo);
                tempClientIdMap.put(userInfo.getClientId(), userInfo);

                if (userInfo.getApiPassword()!=null&&!userInfo.getApiPassword().isBlank())
                    tempAccessKeyMap.put(userInfo.getApiPassword(), userInfo);
            }
            catch (final Exception e)
            {
                log.error("Exception while getting the user information from database. Client id : '" + aResultSet.getString(COL_INDEX_CLIENT_ID) + "'", e);
            }

        if (tempUserPassMap.size() > 0)
        {
            userPassMap  = tempUserPassMap;
            accessKeyMap = tempAccessKeyMap;
            clientIdMap  = tempClientIdMap;
        }

        if (log.isDebugEnabled())
            log.debug("Completed loading db records into inmemory. Total Records Loaded : " + userPassMap.size());
    }

    private static String decryptApiPassword(
            String aApiPass,
            String aClientId)
    {
        String returnValue = null;

        try
        {
            returnValue = aApiPass.isBlank() ? "" : Encryptor.getApiDecryptedPassword(aApiPass);
        }
        catch (final Exception e)
        {
            returnValue = "";
            if (log.isDebugEnabled()) // Specifically used DEBUG
                log.error("Exception while decrypting user API password from database. Client id : '" + aClientId + "'", e);
        }
        return returnValue;
    }

    private static String decryptSmppPassword(
            String aSmppPass,
            String aClientId)
    {
        String returnValue = null;

        try
        {
            returnValue = aSmppPass.isBlank() ? "" : Encryptor.getSmppDecryptedPassword(aSmppPass);
        }
        catch (final Exception e)
        {
            returnValue = "";
            if (log.isDebugEnabled()) // Specifically used DEBUG
                log.error("Exception while decrypting user SMPP password from database. Client id : '" + aClientId + "'", e);
        }
        return returnValue;
    }

    public UserInfo getUserByUser(
            String aUsername)
    {
        return userPassMap.get(aUsername);
    }

    public UserInfo getUserByAccessKey(
            String aAccessKey)
    {
        return accessKeyMap.get(aAccessKey);
    }

    public UserInfo getUserByClientId(
            String aClienntId)
    {
        return clientIdMap.get(aClienntId);
    }

    public static Map<String, String> getAccountInfo(
            String aClientId)
    {
        // TODO Need to replace with the required fields.
        final String              sql            = "select * from accounts_view where cli_id=?";
        ResultSet                 rs             = null;
     	Connection con =null;
    	PreparedStatement pstmt = null;
        Map<String, List<String>> serviceDetails = null;

        try
        {
            serviceDetails = AccountLoader.getServiceInfo();
        }
        catch (final Exception e)
        {
            // TODO: handle exception
        }

        try 
        {
        	  con = DBDataSourceFactory.getConnectionFromThin(JndiInfoHolder.getInstance().getJndiInfoUsingName(DatabaseSchema.ACCOUNTS.getKey()));
              pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, Long.parseLong(aClientId));
            rs = pstmt.executeQuery();

            if (rs.next())
            {
                final ResultSetMetaData   rsmd     = rs.getMetaData();
                final int                 colCount = rsmd.getColumnCount();

                final Map<String, String> results  = new HashMap<>();
                for (int index = 1; index <= colCount; index++)
                    results.put(rsmd.getColumnName(index), CommonUtility.nullCheck(rs.getString(index), true));

                if (serviceDetails != null)
                {
                    final List<String> serviceList = serviceDetails.get(aClientId);
                    if (serviceList != null)
                        for (final String s : serviceList)
                            results.put(s, "1");
                }

                return results;
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the user information from database. Client id : '" + aClientId + "' Exception is : '", e);
        }
        finally
        {
            CommonUtility.closeResultSet(rs);
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
   
        }
        return null;
    }

}