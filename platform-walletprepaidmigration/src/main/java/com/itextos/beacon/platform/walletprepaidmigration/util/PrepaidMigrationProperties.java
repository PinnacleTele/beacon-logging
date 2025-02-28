package com.itextos.beacon.platform.walletprepaidmigration.util;

import java.io.File;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class PrepaidMigrationProperties
{

    private static final String PROP_KEY_CSV_FILE_PATH         = "csv.file.path";
    private static final String PROP_KEY_ACCOUNTS_JNDI_INFO_ID = "accounts.jndi.info.id";
    private static final String PROP_KEY_APPROVAL_FILE_PATH    = "approval.file.path";
    private static final String PROP_KEY_FINAL_FILE_PATH       = "final.file.path";
    private static final int    INVALID                        = -999;

    private static class SingletonHolder
    {

        static final PrepaidMigrationProperties INSTANCE = new PrepaidMigrationProperties();

    }

    public static PrepaidMigrationProperties getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private String mCsvFilePath;
    private int    mAccountsJndiInfoId;
    private String mApprovalFilePath;
    private String mFinalFilePath;
    private File   apporvalFilePath;
    private File   finalFilePath;

    private PrepaidMigrationProperties()
    {
        loadProperties();
    }

    private void loadProperties()
    {
        final PropertiesConfiguration pc = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.PREPAID_MIGRATION_PROPERTIES, true);
        mCsvFilePath        = CommonUtility.nullCheck(pc.getString(PROP_KEY_CSV_FILE_PATH), true);
        mAccountsJndiInfoId = CommonUtility.getInteger(pc.getString(PROP_KEY_ACCOUNTS_JNDI_INFO_ID), INVALID);
        mApprovalFilePath   = CommonUtility.nullCheck(pc.getString(PROP_KEY_APPROVAL_FILE_PATH), true);
        mFinalFilePath      = CommonUtility.nullCheck(pc.getString(PROP_KEY_FINAL_FILE_PATH), true);

        try {
			validate();
		} catch (ItextosRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
    }

    private void validate() throws ItextosRuntimeException
    {
        if (mCsvFilePath.equals(""))
            throw new ItextosRuntimeException("CSV File path is empty");

        if (mAccountsJndiInfoId == INVALID)
            throw new ItextosRuntimeException("Invalid Accounts Jndi ID provided.");

        if (mApprovalFilePath.equals(""))
            throw new ItextosRuntimeException("Approval File path is empty");

        apporvalFilePath = new File(mApprovalFilePath);

        if (!apporvalFilePath.exists())
            throw new ItextosRuntimeException("Approval File path not exists");

        if (!apporvalFilePath.canWrite())
            throw new ItextosRuntimeException("Approval File path not writable");

        if (mFinalFilePath.equals(""))
            throw new ItextosRuntimeException("Final File path is empty");

        finalFilePath = new File(mFinalFilePath);

        if (!finalFilePath.exists())
            throw new ItextosRuntimeException("Final File path not exists");

        if (!finalFilePath.canWrite())
            throw new ItextosRuntimeException("Final File path not writable");
    }

    public String getCsvFilePath()
    {
        return mCsvFilePath;
    }

    public int getAccountsJndiInfoId()
    {
        return mAccountsJndiInfoId;
    }

    public File getApporvalFilePath()
    {
        return apporvalFilePath;
    }

    public File getFinalFilePath()
    {
        return finalFilePath;
    }

}