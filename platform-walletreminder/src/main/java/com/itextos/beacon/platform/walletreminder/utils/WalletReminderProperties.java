package com.itextos.beacon.platform.walletreminder.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class WalletReminderProperties
        extends
        WalletReminderPropertiesConstants
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final WalletReminderProperties INSTANCE = new WalletReminderProperties();

    }

    public static WalletReminderProperties getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final PropertiesConfiguration mProps = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.WALLET_BALANCE_REMINDER_PROPERTIES, true);

    private final String                  mEmailApiType;
    private final String                  mEmailApiUrl;
    private final String                  mEmailApiOwnerId;
    private final String                  mEmailApiToken;
    private final String                  mEmailSmtpUserName;
    private final String                  mOverallTrigger;
    private final String                  mDailyTrigger;
    private final String                  mClientTrigger;
    private final String                  mWalletBackupTrigger;

    private WalletReminderProperties()
    {
        mEmailApiType        = mProps.getString(PROP_KEY_EMAIL_API_TYPE, DEFAULT_EMAIL_API_TYPE);
        mEmailApiUrl         = CommonUtility.nullCheck(mProps.getString(PROP_KEY_EMAIL_API_URL), true);
        mEmailApiOwnerId     = CommonUtility.nullCheck(mProps.getString(PROP_KEY_EMAIL_API_OWNER_ID), true);
        mEmailApiToken       = CommonUtility.nullCheck(mProps.getString(PROP_KEY_EMAIL_API_TOKEN), true);
        mEmailSmtpUserName   = CommonUtility.nullCheck(mProps.getString(PROP_KEY_EMAIL_API_SMTP_USER_NAME), true);
        mOverallTrigger      = CommonUtility.nullCheck(mProps.getString(PROP_KEY_OVERALL_BALANCE_CHECK_TRIGGER), true);
        mDailyTrigger        = CommonUtility.nullCheck(mProps.getString(PROP_KEY_DAILY_BALANCE_CHECK_TRIGGER), true);
        mClientTrigger       = CommonUtility.nullCheck(mProps.getString(PROP_KEY_CLIENT_BALANCE_CHECK_TRIGGER), true);
        mWalletBackupTrigger = CommonUtility.nullCheck(mProps.getString(PROP_KEY_WALLET_BACKUP_TRIGGER), true);

        try {
			validate();
		} catch (ItextosRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
    }

    private void validate() throws ItextosRuntimeException
    {
        validate(mEmailApiType, "Email API Type");
        validate(mEmailApiUrl, "Email API URL");
        validate(mEmailApiOwnerId, "Email API Owner Id");
        validate(mEmailApiToken, "Email API Token");
        validate(mEmailSmtpUserName, "Email API SMTP User Name");
        validate(mOverallTrigger, "Overall Trigger");
        validate(mDailyTrigger, "Daily Trigger");
        validate(mClientTrigger, "Client Trigger");
        validate(mWalletBackupTrigger, "Wallet Backup Trigger");
    }

    private static void validate(
            String aValue,
            String aErrorString) throws ItextosRuntimeException
    {
        if ("".equals(aValue))
            throw new ItextosRuntimeException("Invalid " + aErrorString);

        if ("Email API Type".equalsIgnoreCase(aErrorString) && !DEFAULT_EMAIL_API_TYPE.equalsIgnoreCase(aValue))
            throw new ItextosRuntimeException("Non implemented " + aErrorString + " specified. Email API Type: '" + aValue + "'");
    }

    public String getEmailApiType()
    {
        return mEmailApiType;
    }

    public String getEmailApiUrl()
    {
        return mEmailApiUrl;
    }

    public String getEmailApiOwnerId()
    {
        return mEmailApiOwnerId;
    }

    public String getEmailApiToken()
    {
        return mEmailApiToken;
    }

    public String getEmailSmtpUserName()
    {
        return mEmailSmtpUserName;
    }

    public String getOverallTrigger()
    {
        return mOverallTrigger;
    }

    public String getDailyTrigger()
    {
        return mDailyTrigger;
    }

    public String getClientTrigger()
    {
        return mClientTrigger;
    }

    public String getWalletBackupTrigger()
    {
        return mWalletBackupTrigger;
    }

    public String getFromName()
    {
        return mProps.getString(PROP_KEY_FROM_NAME, DEFAULT_FROM_NAME);
    }

    public String getFromEmailId()
    {
        return mProps.getString(PROP_KEY_FROM_EMAIL, DEFAULT_FROM_EMAIL_ID);
    }

    public List<String> getAdminEmailList()
    {
        return getSplitProperties(mProps.getString(PROP_KEY_ADMIN_EMAIL_LIST, DEFAULT_ADMIN_EMAIL_ID_LIST));
    }

    public String getReplyToEmailId()
    {
        return mProps.getString(PROP_KEY_REPLY_TO_EMAIL, DEFAULT_REPLY_TO_EMAIL_ID);
    }

    public int getClientBalanceRemainderPerDay()
    {
        return mProps.getInt(PROP_KEY_CLIENT_BALANCE_REMINDER_MAIL_COUNT_PER_DAY, DEFAULT_REMINDER_PER_DAY);
    }

    public String getClientBalanceRemainderDataLogPath()
    {
        return mProps.getString(PROP_KEY_CLIENT_BALANCE_REMINDER_DATA_LOG_PATH, DEFAULT_LOG_PATH);
    }

    public String getOverallBalanceRemainderSubjectFormat()
    {
        return mProps.getString(PROP_KEY_OVERALL_BALANCE_REMINDER_SUBJECT_FORMAT, DEFAULT_OVERALL_BALANCE_REMINDER_SUBJECT_FORMAT);
    }

    public String getDailyBalanceRemainderSubjectFormat()
    {
        return mProps.getString(PROP_KEY_DAILY_BALANCE_REMINDER_SUBJECT_FORMAT, DEFAULT_DAILY_BALANCE_REMINDER_SUBJECT_FORMAT);
    }

    public SortBy getOverallSortBy()
    {
        return SortBy.getSortBy(mProps.getInt(PROP_KEY_OVERALL_SORT_BY, 0));
    }

    public SortBy getDailySortBy()
    {
        return SortBy.getSortBy(mProps.getInt(PROP_KEY_DAILY_SORT_BY, 0));
    }

    public boolean getOverallGroupBy()
    {
        return CommonUtility.isTrue(CommonUtility.nullCheck(mProps.getString(PROP_KEY_OVERALL_GROUPBY_ENABLED), true));
    }

    public boolean getDailyGroupBy()
    {
        return CommonUtility.isTrue(CommonUtility.nullCheck(mProps.getString(PROP_KEY_DAILY_GROUPBY_ENABLED), true));
    }

    public boolean getClientBalanceCheckEnabled()
    {
        return CommonUtility.isTrue(CommonUtility.nullCheck(mProps.getString(PROP_KEY_CLIENT_BALANCE_CHECK_ENABLED), false));
    }

    public List<String> getClientIdsToIgnore()
    {
        return getSplitProperties(CommonUtility.nullCheck(mProps.getString(PROP_KEY_CLIENT_IDS_TO_IGNORE), true));
    }

    public String getClientSubjectFormat()
    {
        return CommonUtility.nullCheck(mProps.getString(PROP_KEY_CLIENT_BALANCE_REMINDER_SUBJECT_FORMAT, DEFAULT_CLIENT_BALANCE_REMINDER_SUBJECT_FORMAT), true);
    }

    public String getClientBodyFormat()
    {
        return CommonUtility.nullCheck(mProps.getString(PROP_KEY_CLIENT_BALANCE_REMINDER_BODY_FORMAT, DEFAULT_CLIENT_BALANCE_REMINDER_BODY_FORMAT), true);
    }

    public int getMinimumBalanceToNotify()
    {
        return CommonUtility.getInteger(mProps.getString(PROP_KEY_MINIMUM_BALANCE), DEFAULT_MINIMUM_BALANCE);
    }

    private static List<String> getSplitProperties(
            String aFullProperties)
    {
        final List<String> returnValue = new ArrayList<>();
        if ("".equals(CommonUtility.nullCheck(aFullProperties, true)))
            return returnValue;

        for (final String lString : aFullProperties.split(","))
            returnValue.add(lString);

        return returnValue;
    }

    public int getBackupDataDays()
    {
        return CommonUtility.getInteger(mProps.getString(PROP_KEY_FILE_BACKUP_DAYS), DEFAULT_BACKUP_DAYS);
    }

    public String getBackupFolder()
    {
        return CommonUtility.nullCheck(mProps.getString(PROP_KEY_FILE_BACKUP_PATH, "/"), true);
    }

    public boolean filterInvalid()
    {
        return CommonUtility.isTrue(CommonUtility.nullCheck(mProps.getString(PROP_KEY_FILTER_INVALID_RECORDS_ENABLED), true));
    }

    public String getDefaultNotificationMailId()
    {
        return CommonUtility.nullCheck(mProps.getString(PROP_KEY_DEFAULT_NOTIFICATION_MAIL_ID), true);
    }

}