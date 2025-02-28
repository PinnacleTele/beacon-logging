package com.itextos.beacon.platform.walletprepaidmigration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.walletbase.data.WalletUpdateInput;
import com.itextos.beacon.platform.walletprepaidmigration.util.PrepaidMigrationProperties;

import redis.clients.jedis.Jedis;

public class Process
{

    private static final Log            log                           = LogFactory.getLog(Process.class);

    private static final int            CSV_FILE_INDEX_USER           = 0;
    private static final int            CSV_FILE_INDEX_BALANCE_CREDIT = 1;
    private static final int            INVALID                       = -999;

    private final Map<String, Integer>  mUserBalanceCredit            = new HashMap<>();
    private final Map<String, UserInfo> mUserInfoMap                  = new HashMap<>();
    private final List<String>          mClientList                   = new ArrayList<>();
    private String                      approvalFileName;
    private final BufferedReader        reader                        = new BufferedReader(new InputStreamReader(System.in));

    public void readBalanceCreditFromCsvFile() throws ItextosRuntimeException
    {
        if (log.isDebugEnabled())
            log.debug("Reading CSV File from '" + PrepaidMigrationProperties.getInstance().getCsvFilePath() + "'");

        try (
                Reader reader = Files.newBufferedReader(Path.of(PrepaidMigrationProperties.getInstance().getCsvFilePath()));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());)
        {
            final int rowCount = 1;

            for (final CSVRecord csvRecord : csvParser)
            {
                final String user    = csvRecord.get(CSV_FILE_INDEX_USER);
                final String balance = csvRecord.get(CSV_FILE_INDEX_BALANCE_CREDIT);
                final int    credits = CommonUtility.getInteger(balance, INVALID);

                if (credits < 0)
                {
                    log.error("Cannot process for user '" + user + "' as the balance credit comes as '" + balance + "'");
                    System.err.println("Cannot process for user '" + user + "' as the balance credit comes as '" + balance + "'");
                    final boolean lShallContinue = shallContinue();

                    if (lShallContinue)
                        continue;

                    {
                        log.error("Invalid input in file. Terminating application.");
                        System.out.println("Process terminated.");
                        System.exit(0);
                    }
                }

                mUserBalanceCredit.put(user, credits);
            }

            if (log.isInfoEnabled())
                log.info("Total number of records to be processed '" + mUserBalanceCredit.size() + "'s");
        }
        catch (final IOException e)
        {
            log.error("Exception while reading the old Balance Credit", e);
            throw new ItextosRuntimeException("Exception while reading the old Balance Credit", e);
        }
    }

    public void getDataFromRedis()
    {
        if (log.isDebugEnabled())
            log.debug("Getting Current Wallet amounts from Redis");

        // final Map<String, Double> lWalletBalance =
        // WalletUpdateProcessor.getWalletBalance(mClientList);
        //
        // for (final Entry<String, Double> entry : lWalletBalance.entrySet())
        // {
        // final String cliId = entry.getKey();
        // final UserInfo lUserInfo = mUserInfoMap.get(cliId);
        // lUserInfo.setValueFromRedis(entry.getValue());
        // }
    }

    public void createApprovalFile() throws ItextosRuntimeException
    {
        final Map<String, UserInfo> tempUserInfoMap = new TreeMap<>();
        for (final Entry<String, UserInfo> entry : mUserInfoMap.entrySet())
            tempUserInfoMap.put(entry.getValue().getUsername(), entry.getValue());

        approvalFileName = PrepaidMigrationProperties.getInstance().getApporvalFilePath().getAbsolutePath() + File.separator + "approval-file-"
                + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv";

        if (log.isDebugEnabled())
            log.debug("Creating the approval file for the users. Filename '" + approvalFileName + "'");

        try (
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(approvalFileName));
                CSVPrinter csvPrinter = new CSVPrinter(writer,
                        CSVFormat.DEFAULT.withHeader("User", "Client Id", "SMS Rate", "DLT Rate", "Unitia Credits", "Equivalent Wallet Balance", "Old Wallet Balance", "New Wallet Balance"));)
        {
            for (final Entry<String, UserInfo> entry : tempUserInfoMap.entrySet())
                csvPrinter.printRecord(entry.getValue().getPrintString());
            csvPrinter.flush();

            if (log.isDebugEnabled())
                log.debug("Approval file created successfully. Filename '" + approvalFileName + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while creating the approval file. Filename '" + approvalFileName + "'", e);
            throw new ItextosRuntimeException("Exception while creating the approval file. Filename '" + approvalFileName + "'", e);
        }
    }

    public void updateRedisWalletAmount()
    {
        final List<WalletUpdateInput> updateList = new ArrayList<>();

        for (final Entry<String, UserInfo> entry : mUserInfoMap.entrySet())
        {
            final UserInfo          lValue       = entry.getValue();
            final WalletUpdateInput lUpdateInput = new WalletUpdateInput(lValue.getClientId(), lValue.getWalletEquivalent());
            updateList.add(lUpdateInput);
        }
        // WalletUpdateProcessor.addWalletAmount(updateList);
    }

    public void getAccountInfo() throws ItextosRuntimeException
    {
        String             sql = "select user, cli_id, sms_rate, dlt_rate from accounts.accounts_view where user in ";

        final StringJoiner sj  = new StringJoiner(",", "(", ")");

        for (final Entry<String, Integer> entry : mUserBalanceCredit.entrySet())
            sj.add("'" + entry.getKey() + "'");

        final String userToFilter = sj.toString();

        if (log.isDebugEnabled())
            log.debug("Getting User information for the users '" + userToFilter + "'");

        ResultSet lExecuteQuery = null;
        sql = sql + sj.toString() + " order by user";

        try (
                Connection con = getDbConnection();
                PreparedStatement pstmt = con.prepareStatement(sql);)
        {
            lExecuteQuery = pstmt.executeQuery();

            while (lExecuteQuery.next())
            {
                final String   user             = lExecuteQuery.getString("user");
                final String   cliId            = lExecuteQuery.getString("cli_id");
                final double   smsRate          = lExecuteQuery.getDouble("sms_rate");
                final double   dltRate          = lExecuteQuery.getDouble("dlt_rate");

                final int      oldBalanceCredit = mUserBalanceCredit.get(user);
                final UserInfo userInfo         = new UserInfo(user, cliId, smsRate, dltRate, oldBalanceCredit);

                if (userInfo.isIsValid())
                {
                    mClientList.add(cliId);
                    mUserInfoMap.put(cliId, userInfo);
                }
            }

            if (log.isInfoEnabled())
                log.info("Total number of users matching with the database users '" + mUserInfoMap.size() + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while getting user data from the database.", e);
            throw new ItextosRuntimeException("Exception while getting user data from the database.", e);
        }
        finally
        {
            CommonUtility.closeResultSet(lExecuteQuery);
        }
    }

    public boolean checkForCountMismatch()
    {

        if (mUserInfoMap.size() != mUserBalanceCredit.size())
        {
            System.out.println("\n\n\n\nThere is a count mismatch between Users from File and Users from new Database.");
            System.out.println("Users Count from Input File :'" + mUserBalanceCredit.size() + "'");
            System.out.println("Users Count from Database   :'" + mUserInfoMap.size() + "'");

            return shallContinue();
        }
        return true;
    }

    public static void check() throws ItextosRuntimeException
    {
        if (log.isDebugEnabled())
            log.debug("Checking for the input values.");

        checkFileExists();
        checkRedisConnection();
        checkDbConnection();
    }

    private static void checkFileExists() throws ItextosRuntimeException
    {
        final String fileName = PrepaidMigrationProperties.getInstance().getCsvFilePath();
        final File   file     = new File(fileName);

        if (!file.exists())
            throw new ItextosRuntimeException("CSV file not found. Filepath provided : '" + fileName + "'");
    }

    private static void checkDbConnection() throws ItextosRuntimeException
    {

        try (
                Connection con = getDbConnection();)
        {}
        catch (final Exception e)
        {
            throw new ItextosRuntimeException("Unable to get the database connection.", e);
        }
    }

    private static void checkRedisConnection() throws ItextosRuntimeException
    {

        try (
                Jedis jedis = getConnection())
        {}
        catch (final Exception e)
        {
            throw new ItextosRuntimeException("Unable to get the Redis Connection. Please check the configuration.", e);
        }
    }

    private static Connection getDbConnection()
            throws Exception
    {
        JndiInfoHolder.getInstance();
        return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfo(PrepaidMigrationProperties.getInstance().getAccountsJndiInfoId()));
    }

    private static Jedis getConnection()
    {
        return RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.WALLET_CHK, 1);
    }

    public boolean checkForApproval()
    {
        System.out.println("\n\nApproval file created in '" + approvalFileName + "'");
        System.out.println("Validate the data before updating the same in Redis");
        return shallContinue();
    }

    private boolean shallContinue()
    {
        System.out.println("\n\nDo you want to continue (Y / N) ?");

        try
        {
            final String input = reader.readLine();
            return CommonUtility.isTrue(input);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public void createFinalFile()
    {
        // final Map<String, Double> lWalletBalance =
        // WalletUpdateProcessor.getWalletBalance(mClientList);
        // final Map<String, String> finalValues = new TreeMap<>();
        //
        // for (final Entry<String, Double> entry : lWalletBalance.entrySet())
        // {
        // final UserInfo lUserInfo = mUserInfoMap.get(entry.getKey());
        // finalValues.put(lUserInfo.getUsername(), entry.getKey() + "|~|" +
        // entry.getValue());
        // }
        //
        // writeToFile(finalValues);
    }

    private void writeToFile(
            Map<String, String> aFinalValues) throws ItextosRuntimeException
    {
        final String finalFileName = PrepaidMigrationProperties.getInstance().getFinalFilePath().getAbsolutePath() + File.separator + "Final-file-"
                + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv";

        if (log.isDebugEnabled())
            log.debug("Creating the Final file for the users. Filename '" + finalFileName + "'");

        try (
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(finalFileName));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("User", "Client Id", "New Wallet Balance"));)
        {

            for (final Entry<String, String> entry : aFinalValues.entrySet())
            {
                final String[] values = StringUtils.split(entry.getValue(), "|~|");
                csvPrinter.printRecord(entry.getKey(), values[0], values[1]);
            }
            csvPrinter.flush();

            if (log.isDebugEnabled())
                log.debug("Final file created successfully. Filename '" + finalFileName + "'");
            System.out.println("Final file created successfully. Filename '" + finalFileName + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while creating the approval file. Filename '" + approvalFileName + "'", e);
            throw new ItextosRuntimeException("Exception while creating the approval file. Filename '" + approvalFileName + "'", e);
        }
    }

}

class UserInfo
{

    private static final Log log = LogFactory.getLog(UserInfo.class);

    private final String     mUsername;
    private final String     mClientId;
    private final double     mSmsRate;
    private final double     mDltRate;
    private final int        mOldBalanceCredit;
    private final double     mWalletEquivalent;
    private final boolean    mIsValid;
    private double           mValueFromRedis;
    private double           mValueToUpdateInRedis;

    /**
     * @param aUsername
     * @param aClientId
     * @param aSmsRate
     * @param aDltRate
     * @param aOldBalanceCredit
     * @param aWalletEquivalent
     */
    UserInfo(
            String aUsername,
            String aClientId,
            double aSmsRate,
            double aDltRate,
            int aOldBalanceCredit)
    {
        super();
        mUsername = aUsername;
        mClientId = aClientId;
        mSmsRate  = aSmsRate;
        mDltRate  = aDltRate;

        if ((mSmsRate <= 0) || (mDltRate <= 0))
        {
            log.error("Invalid SMS Rate or DLT Rate. SMS Rate :'" + mSmsRate + "', DLT Rate '" + mDltRate + "'");
            mIsValid = false;
        }
        else
            mIsValid = true;

        mOldBalanceCredit = aOldBalanceCredit;
        mWalletEquivalent = aOldBalanceCredit * (mSmsRate + mDltRate);
        // System.out.println(aSmsRate + " > " + aDltRate + " > " + aOldBalanceCredit +
        // " > " + mWalletEquivalent);
    }

    double getValueFromRedis()
    {
        return mValueFromRedis;
    }

    void setValueFromRedis(
            double aValueFromRedis)
    {
        mValueFromRedis       = aValueFromRedis;
        mValueToUpdateInRedis = mValueFromRedis + mWalletEquivalent;
    }

    double getValueToUpdateInRedis()
    {
        return mValueToUpdateInRedis;
    }

    String getUsername()
    {
        return mUsername;
    }

    String getClientId()
    {
        return mClientId;
    }

    double getSmsRate()
    {
        return mSmsRate;
    }

    double getDltRate()
    {
        return mDltRate;
    }

    int getOldBalanceCredit()
    {
        return mOldBalanceCredit;
    }

    double getWalletEquivalent()
    {
        return mWalletEquivalent;
    }

    boolean isIsValid()
    {
        return mIsValid;
    }

    String[] getPrintString()
    {
        return new String[]
        { mUsername, mClientId, "" + mSmsRate, "" + mDltRate, "" + mOldBalanceCredit, "" + mWalletEquivalent, "" + mValueFromRedis, "" + mValueToUpdateInRedis };
    }

    @Override
    public String toString()
    {
        return "UserInfo [mUsername=" + mUsername + ", mClientId=" + mClientId + ", mSmsRate=" + mSmsRate + ", mDltRate=" + mDltRate + ", mOldBalanceCredit=" + mOldBalanceCredit
                + ", mWalletEquivalent=" + mWalletEquivalent + ", mIsValid=" + mIsValid + ", mValueFromRedis=" + mValueFromRedis + ", mValueToUpdateInRedis=" + mValueToUpdateInRedis + "]";
    }

}