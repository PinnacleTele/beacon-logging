package com.itextos.beacon.platform.topic2table.impl.billing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.JndiInfo;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.configvalues.ApplicationConfiguration;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.topic2table.tablename.AbstractTablenameFinder;

public class BillingTablenameFinder
        extends
        AbstractTablenameFinder
{

    private static final Log log = LogFactory.getLog(BillingTablenameFinder.class);

    @Override
    public void process()
    {

        if (!mInputValues.isEmpty())
        {
            setJndiId();
            setTableName();
        }
    }

    private void setTableName()
    {
        final boolean isDateWiseBillingEnabled = CommonUtility.isEnabled(getAppConfigValueAsString(ConfigParamConstants.BILLING_TABLE_DATEWISE_ENABLED));

        if (isDateWiseBillingEnabled)
        {
            final String lDatabaseSuffix  = CommonUtility.nullCheck(mInputValues.get(MiddlewareConstant.MW_DB_BILLING_INSERT_DATABASE_SUFFIX.getKey()), true);
            final String lTableNameSuffix = CommonUtility.nullCheck(mInputValues.get(MiddlewareConstant.MW_DB_BILLING_INSERT_TABLE_SUFFIX.getKey()), true);

            String       tempName         = "";
            if (!lDatabaseSuffix.isEmpty() && !mDbName.isBlank())
                tempName = mDbName + lDatabaseSuffix + ".";
            else
                tempName = mDbName + ".";

            if (!lTableNameSuffix.isEmpty())
                tempName = tempName + mTableName + lTableNameSuffix;
            else
                tempName = tempName + mTableName;

            mTableName = tempName;
        }

        final String lClientSuffix = CommonUtility.nullCheck(mInputValues.get(MiddlewareConstant.MW_DB_BILLING_INSERT_CLIENT_SUFFIX.getKey()), true);
        if (!lClientSuffix.isEmpty())
            mTableName = mTableName + "_" + lClientSuffix;

        if (log.isDebugEnabled())
            log.debug("Final Table name to use '" + mTableName + "'");
    }

    public static String getAppConfigValueAsString(
            ConfigParamConstants aKey)
    {
        if (aKey == null)
            return null;
        final ApplicationConfiguration lAppConfiguration = (ApplicationConfiguration) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.APPLICATION_CONFIG);
        return lAppConfiguration.getConfigValue(aKey.getKey());
    }

    private void setJndiId()
    {
        final String jndiInfoId = CommonUtility.nullCheck(mInputValues.get(MiddlewareConstant.MW_DB_BILLING_INSERT_JNDI.getKey()), true);

        if (!jndiInfoId.isEmpty())
        {
            final int jndiId = CommonUtility.getInteger(jndiInfoId, -1);

            if (jndiId != -1)
            {
                final JndiInfo lJndiInfo = JndiInfoHolder.getInstance().getJndiInfo(jndiId);
                if (mJndiInfo != null)
                    mJndiInfo = lJndiInfo;
                else
                    log.error("Invalid jndi id specified in bill_log_map " + jndiInfoId);
            }
            else
                log.error("Invalid jndi id specified in bill_log_map " + jndiInfoId);
        }
    }

}