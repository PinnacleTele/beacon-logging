package com.itextos.beacon.platform.walletreminder.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.walletprocess.WalletUpdateProcessor;
import com.itextos.beacon.platform.walletreminder.utils.WalletReminderProperties;

public class DataCollector
{

    private static final Log    log         = LogFactory.getLog(DataCollector.class);
    private static final long   EXPIRY_TIME = 4 * 60 * 1000L;

    // TODO Need to change the Query
    private static final String USER_QUERY  = "select cli_id, pu_id, su_id, firstname, lastname, email, user, billing_currency from accounts.accounts_view av where acc_status = 0 and bill_type = 1 and acc_type not in ('1','2')";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final DataCollector INSTANCE = new DataCollector();

    }

    public static DataCollector getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private long                                  mLastDataReceived     = -1;
    private Map<Long, Map<Long, Set<Long>>>       allClientIdMap        = new TreeMap();
    private Map<String, Map<String, Set<String>>> allUserMap            = new TreeMap(String.CASE_INSENSITIVE_ORDER);
    private Map<Long, Long>                       parentMap             = new TreeMap<>();
    private Map<Long, Long>                       superAdminMap         = new TreeMap<>();
    private Map<Long, UserInfo>                   allUserInfoByClientId = new TreeMap<>();
    private Map<String, Long>                     allUserCliIdMap       = new TreeMap(String.CASE_INSENSITIVE_ORDER);

    private DataCollector()
    {}

    public Map<Long, Map<Long, Set<Long>>> getClientIdBasedMap()
    {
        return allClientIdMap;
    }

    public Map<String, Map<String, Set<String>>> getUserBasedMap()
    {
        return allUserMap;
    }

    public void getData()
            throws Exception
    {

        if (isDataExpired())
        {
            getDbUserData();
            getRedisBalance();
            mLastDataReceived = System.currentTimeMillis();
        }
    }

    public long getLastDataReceived()
    {
        return mLastDataReceived;
    }

    private void getRedisBalance()
    {

        try
        {
            final List<String> cliIds = new ArrayList<>();
            for (final Entry<Long, UserInfo> userInfo : allUserInfoByClientId.entrySet())
                cliIds.add("" + userInfo.getKey());

            final Map<String, Double> lWalletBalance = WalletUpdateProcessor.getWalletBalance(cliIds);

            for (final Entry<String, Double> walBalance : lWalletBalance.entrySet())
            {
                final UserInfo lUserInfo = allUserInfoByClientId.get(CommonUtility.getLong(walBalance.getKey()));
                lUserInfo.setWalletBalance(walBalance.getValue());
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Wallet Balance from Redis", e);
            throw e;
        }
    }

    private void getDbUserData()
            throws Exception
    {
        final String sql = appendNotInCase(USER_QUERY);

        if (log.isDebugEnabled())
            log.debug("Query to execute '" + sql + "'");

        try (
                final Connection con = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfo(2));
                final PreparedStatement pstmt = con.prepareStatement(sql);
                final ResultSet rs = pstmt.executeQuery();)
        {
            final Map<Long, Long>                 tempParentMap             = new TreeMap();
            final Map<Long, Long>                 tempSuperAdminMap         = new TreeMap();
            final Map<Long, Map<Long, Set<Long>>> tempAllClientIdMap        = new TreeMap();
            final Map<Long, UserInfo>             tempAllUserInfoByClientId = new TreeMap();
            final Map<String, Long>               tempUserCliIdMap          = new TreeMap(String.CASE_INSENSITIVE_ORDER);

            while (rs.next())
            {
                final long   cliId        = rs.getLong("cli_id");
                final long   puId         = rs.getLong("pu_id");
                final long   suId         = rs.getLong("su_id");
                final String user         = rs.getString("user");
                final String lastName     = rs.getString("lastname");
                final String firstName    = rs.getString("firstname");
                final String emailAddress = rs.getString("email");
                final String currency     = rs.getString("billing_currency");

                tempParentMap.put(cliId, puId);
                tempSuperAdminMap.put(cliId, suId);

                addTohirarchy(tempAllClientIdMap, cliId, puId, suId);

                tempAllUserInfoByClientId.put(cliId, new UserInfo(cliId, puId, suId, user, firstName, lastName, emailAddress, currency));

                tempUserCliIdMap.put(user, cliId);
            }

            final Map<String, Map<String, Set<String>>> tempAllUserMap = populateUserBased(tempAllClientIdMap, tempAllUserInfoByClientId);

            if (!tempParentMap.isEmpty())
            {
                parentMap             = tempParentMap;
                superAdminMap         = tempSuperAdminMap;
                allClientIdMap        = tempAllClientIdMap;
                allUserMap            = tempAllUserMap;
                allUserInfoByClientId = tempAllUserInfoByClientId;
                allUserCliIdMap       = tempUserCliIdMap;
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the data from database", e);
            // throw e;
        }
    }

    private static Map<String, Map<String, Set<String>>> populateUserBased(
            Map<Long, Map<Long, Set<Long>>> aTempAllClientIdMap,
            Map<Long, UserInfo> aTempAllUserInfoByClientId)
    {
        final Map<String, Map<String, Set<String>>> returnValue = new TreeMap(String.CASE_INSENSITIVE_ORDER);

        for (final Entry<Long, Map<Long, Set<Long>>> suMap : aTempAllClientIdMap.entrySet())
        {
            final Long   suId   = suMap.getKey();
            final String suUser = getUser(suId, aTempAllUserInfoByClientId);

            if (suUser == null)
                continue;

            final Map<String, Set<String>> puUserMap = returnValue.computeIfAbsent(suUser, k -> new TreeMap(String.CASE_INSENSITIVE_ORDER));

            final Map<Long, Set<Long>>     puMap     = suMap.getValue();

            for (final Entry<Long, Set<Long>> puMapEntry : puMap.entrySet())
            {
                final Long   parentCliId = puMapEntry.getKey();
                final String parentUser  = getUser(parentCliId, aTempAllUserInfoByClientId);

                if (parentUser == null)
                    continue;

                final Set<String> userList = puUserMap.computeIfAbsent(parentUser, k -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER));

                for (final Long cliId : puMapEntry.getValue())
                {
                    final String user = getUser(cliId, aTempAllUserInfoByClientId);
                    if (user != null)
                        userList.add(user);
                }
            }
        }

        // for (final String s : returnValue.keySet())
        // {
        // System.out.println(s);
        //
        // if (s.equals("itextosuser1"))
        // {
        // final Map<String, Set<String>> lMap = returnValue.get(s);
        //
        // for (final String sq : lMap.keySet())
        // {
        // System.out.println("\t" + sq);
        // final Set<String> lSet = lMap.get(sq);
        // for (final String user : lSet)
        // System.out.println("\t\t" + user);
        // }
        // }
        // }
        return returnValue;
    }

    private static String getUser(
            Long aCliId,
            Map<Long, UserInfo> aTempAllUserInfoByClientId)
    {

        try
        {
            final UserInfo lUserInfo = aTempAllUserInfoByClientId.get(aCliId);

            if (lUserInfo == null)
                throw new ItextosRuntimeException("No User data found for '" + aCliId + "'");
            return lUserInfo.getUser();
        }
        catch (final Exception e)
        {
            log.error(e);
        }
        return null;
    }

    private static void addTohirarchy(
            Map<Long, Map<Long, Set<Long>>> aTempAllUsersMap,
            long aCliId,
            long aPuId,
            long aSuId)
    {
        final Map<Long, Set<Long>> parentIdMap = aTempAllUsersMap.computeIfAbsent(aSuId, k -> new TreeMap<>());
        final Set<Long>            cliIds      = parentIdMap.computeIfAbsent(aPuId, k -> new TreeSet<>());
        cliIds.add(aCliId);
    }

    private static String appendNotInCase(
            String aBaseQuery)
    {
        final List<String> lClientIdsToIgnore = WalletReminderProperties.getInstance().getClientIdsToIgnore();
        if (lClientIdsToIgnore.isEmpty())
            return aBaseQuery;

        final StringJoiner sj = new StringJoiner(",", " and cli_id not in( ", " )");
        for (final String s : lClientIdsToIgnore)
            sj.add("'" + s + "'");
        return aBaseQuery + sj.toString();
    }

    private boolean isDataExpired()
    {
        return ((System.currentTimeMillis() - mLastDataReceived) > EXPIRY_TIME);
    }

    public Long getClientIdByUser(
            String aUser)
    {
        return allUserCliIdMap.get(aUser);
    }

    public UserInfo getUserInfo(
            Long aCliId)
    {
        return allUserInfoByClientId.get(aCliId);
    }

    public Map<Long, UserInfo> getUserWalletBalance(
            boolean isAscending)
    {
        final List<Map.Entry<Long, UserInfo>> list = new LinkedList<>(allUserInfoByClientId.entrySet());

        if (isAscending)
            Collections.sort(list, (
                    i1,
                    i2) -> i1.getValue().getWalletBalance().compareTo(i2.getValue().getWalletBalance()));
        else
            Collections.sort(list, (
                    i1,
                    i2) -> i2.getValue().getWalletBalance().compareTo(i1.getValue().getWalletBalance()));

        final Map<Long, UserInfo> temp = new LinkedHashMap<>();
        for (final Entry<Long, UserInfo> entry : list)
            temp.put(entry.getKey(), entry.getValue());
        return temp;
    }

    public Map<Long, UserInfo> getUserWalletBalance()
    {
        return allUserInfoByClientId;
    }

    public Set<String> getUserList()
    {
        return allUserCliIdMap.keySet();
    }

}