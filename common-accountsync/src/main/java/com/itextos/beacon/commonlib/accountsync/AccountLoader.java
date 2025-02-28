package com.itextos.beacon.commonlib.accountsync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiIdProperties;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.RedisKeys;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class AccountLoader
{

    private static final Log    log                      = LogFactory.getLog(AccountLoader.class);
    private static final int    FETCH_BATCH_SIZE         = 2000;

    private static final String SQL_SELECT_ALL_CUSTOMERS = "select * from accounts_view";
    private static final String SQL_SEPECIFIC_CUSTOMERS  = "select * from accounts_view where cli_id = ?";
    private static final String SQL_USER_SERVICE_MAP     = "select" //
            + " usm.cli_id, usm.service, usm.sub_service" //
            + " from" //
            + " accounts.user_service_map usm," //
            + " accounts.sub_service ss," //
            + " accounts.service s" //
            + " WHERE" //
            + " usm.sub_service = ss.sub_service" //
            + " and usm.service = s.service" //
            + " and ss.service = s.service" //
            + " and ss.is_active = '1' ";

    private AccountLoader()
    {}

    public static void loadAllAccountData()
            throws ItextosException
    {
        loadEntireData();
    }

    public static void loadAccountData(
            List<String> aData)
            throws Exception
    {
        loadSelectedData(aData);
    }

    public static void deleteAccount(
            List<String> aAccountToRemove)
            throws Exception
    {
        RedisOperations.deleteAccount(aAccountToRemove);
    }

    private static void loadSelectedData(
            List<String> aList)
            throws Exception
    {

        if ((aList != null) && !aList.isEmpty())
        {
            final Map<String, List<String>> serviceDetails = getServiceInfo();

           	Connection con =null;
        	PreparedStatement pstmt = null;
     
            try 
            {
                 con = DBDataSourceFactory.getConnectionFromThin(JndiInfoHolder.getInstance().getJndiInfo(JndiIdProperties.getInstance().getJndiProperty(DatabaseSchema.ACCOUNTS.getKey())));
                 pstmt = con.prepareStatement(SQL_SEPECIFIC_CUSTOMERS);
                final Map<String, Map<String, String>> dbAccDataMap = new HashMap<>();

                for (final String s : aList)
                {
                    pstmt.setString(1, s);

                	ResultSet resultSet=null;

                    try 
                    {
                    	resultSet = pstmt.executeQuery();

                        if (resultSet.next())
                        {
                            final String              cliId   = resultSet.getString("cli_id");
                            final Map<String, String> dataMap = accumulateData(resultSet, serviceDetails.get(cliId));
                            dbAccDataMap.put(RedisKeys.CLIENTINFO_BY_CLID.getKey() + cliId, dataMap);
                        }
                        else
                            log.warn("No data found for the query '" + SQL_SEPECIFIC_CUSTOMERS + "' for the key value '" + s + "'");
                    }catch(Exception e) {
                    	
                    }finally {
                    	
                        CommonUtility.closeResultSet(resultSet);

                    }
                }

                if (!dbAccDataMap.isEmpty())
                    RedisOperations.updateAccount(dbAccDataMap);
            }
            catch (final Exception e)
            {
                final String s = "Exception while update the account data into Redis";
                log.error(s, e);
                throw new ItextosException(s, e);
            }finally {
            	
                CommonUtility.closeStatement(pstmt);
                CommonUtility.closeConnection(con);
         
            }
        }
    }

    public static Map<String, List<String>> getServiceInfo()
            throws ItextosException
    {
        final Map<String, List<String>> returnValue = new HashMap<>();

      	Connection con =null;
    	PreparedStatement pstmt = null;
    	ResultSet userServiceMap=null;
   
        try 
        {
        	 con = DBDataSourceFactory.getConnectionFromThin(JndiInfoHolder.getInstance().getJndiInfo(JndiIdProperties.getInstance().getJndiProperty(DatabaseSchema.ACCOUNTS.getKey())));
             pstmt = con.prepareStatement(SQL_USER_SERVICE_MAP);
             userServiceMap = pstmt.executeQuery();

            while (userServiceMap.next())
            {
                final String       cliId       = userServiceMap.getString("cli_id");
                final List<String> serviceList = returnValue.computeIfAbsent(cliId, k -> new ArrayList<>());
                serviceList.add(CommonUtility.combine(userServiceMap.getString("service"), userServiceMap.getString("sub_service")));
            }
        }
        catch (final Exception e)
        {
            final String s = "Exception while getting the User Service Map Info";
            log.error(s, e);
            throw new ItextosException(s, e);
        }finally {
            CommonUtility.closeResultSet(userServiceMap);
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
     
        }

        return returnValue;
    }

    private static Map<String, String> accumulateData(
            ResultSet aResultSet,
            List<String> aList)
    {
        final Map<String, String> dataMap = CommonUtility.getAccountDataMap(aResultSet);

        if (aList != null)
            for (final String s : aList)
                dataMap.put(s, "1");

        return dataMap;
    }

    private static void loadEntireData()
            throws ItextosException
    {

        if (log.isInfoEnabled())
        {
            log.info("Redis population started at : " + new Date());
            log.info("SQL_SELECT_ALL_CUSTOMERS to get the starting set of records : " + SQL_SELECT_ALL_CUSTOMERS);
        }

        boolean                         isSuccess      = true;
        ResultSet                       resultSet      = null;
      	Connection con =null;
    	PreparedStatement pstmt = null;
 
        int                             recordsCount   = 0;

        final Map<String, List<String>> serviceDetails = getServiceInfo();

        try 
        {
             con = DBDataSourceFactory.getConnectionFromThin(JndiInfoHolder.getInstance().getJndiInfo(JndiIdProperties.getInstance().getJndiProperty(DatabaseSchema.ACCOUNTS.getKey())));
             pstmt = con.prepareStatement(SQL_SELECT_ALL_CUSTOMERS, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);


            resultSet = pstmt.executeQuery();
            final Map<String, Map<String, String>> dbAccDataMap = new HashMap<>();

            while (resultSet.next())
            {
                recordsCount++;

                final String              cliId   = resultSet.getString("cli_id");
                final Map<String, String> dataMap = accumulateData(resultSet, serviceDetails.get(cliId));

                dbAccDataMap.put(RedisKeys.CLIENTINFO_BY_CLID.getKey() + cliId, dataMap);

                // Push the account information for every 1000 records
                if ((recordsCount % 1000) == 0)
                {
                    RedisOperations.updateAccount(dbAccDataMap);
                    dbAccDataMap.clear();
                }
            }

            if ((recordsCount % 1000) != 0)
                RedisOperations.updateAccount(dbAccDataMap);
        }
        catch (final Exception e)
        {
            isSuccess = false;
            final String s = "Exception while update the account data into Redis";
            log.error(s, e);
            throw new ItextosException(s, e);
        }
        finally
        {
            CommonUtility.closeResultSet(resultSet);
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
    
        }

        if (log.isInfoEnabled())
            log.info("Final status : " + isSuccess);

        if (isSuccess)
        {
            final DecimalFormat dcf = new DecimalFormat("###,###,###,###");
            dcf.setMinimumFractionDigits(0);
            dcf.setMaximumFractionDigits(0);

            if (log.isInfoEnabled())
                log.info("Records pushed to Redis : " + dcf.format(recordsCount));
        }

        if (log.isInfoEnabled())
            log.info("***** Accounts sycn done and exit");
    }

}