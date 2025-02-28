package com.itextos.beacon.platform.msgflowutil.billing;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.msgflowutil.util.PlatformUtil;

public class BillingDatabaseTableIndentifier
{

    private static final Log    log                = LogFactory.getLog(BillingDatabaseTableIndentifier.class);

    private static final String SCHEMA_DATE_FORMAT = "_yyyyMM";
    private static final String TABLE_DATE_FORMAT  = "_yyyyMMdd";

    private final BaseMessage   mBaseMessage;

    public BillingDatabaseTableIndentifier(
            BaseMessage aBaseMessage)
    {
        mBaseMessage = aBaseMessage;
    }

    public void identifySuffix()
    {
        final String               clientId             = mBaseMessage.getValue(MiddlewareConstant.MW_CLIENT_ID);

        final BillLogMapCollection billLogMapCollection = (BillLogMapCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.BILL_LOG_MAPPING);
        BillLogMap                 lBillLogInfo         = billLogMapCollection.getBillLogInfo(clientId);

        if (lBillLogInfo == null)
            lBillLogInfo = BillLogMap.getDefaultMap();

        appendDatewiseSchemaTable();

        mBaseMessage.putValue(MiddlewareConstant.MW_DB_BILLING_INSERT_CLIENT_SUFFIX, lBillLogInfo.getTableSuffix());
        mBaseMessage.putValue(MiddlewareConstant.MW_DB_BILLING_INSERT_JNDI, lBillLogInfo.getJndiID());
    }

    private void appendDatewiseSchemaTable()
    {
        final boolean isDateWiseBillingEnabled = CommonUtility.isEnabled(PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.BILLING_TABLE_DATEWISE_ENABLED));

        if (log.isDebugEnabled())
            log.debug("Is Datewise Billing Enabled '" + isDateWiseBillingEnabled + "'");

        if (isDateWiseBillingEnabled)
        {
            final String receiveDateString = mBaseMessage.getValue(MiddlewareConstant.MW_MSG_ACTUAL_RECEIVED_TIME);
            final Date   receiveDate       = DateTimeUtility.getDateFromString(receiveDateString, DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);

            String       schemaDateFormat  = CommonUtility.nullCheck(PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.BILLING_SCHEMA_DATE_FORMAT), true);
            String       tableDateFormat   = CommonUtility.nullCheck(PlatformUtil.getAppConfigValueAsString(ConfigParamConstants.BILLING_TABLE_DATE_FORMAT), true);

            if (log.isDebugEnabled())
                log.debug("schemaDateFormat : '" + schemaDateFormat + "' tableDateFormat : '" + tableDateFormat + "'");

            schemaDateFormat = schemaDateFormat.isBlank() ? SCHEMA_DATE_FORMAT : schemaDateFormat;
            tableDateFormat  = tableDateFormat.isBlank() ? TABLE_DATE_FORMAT : tableDateFormat;

            final String schemaString = DateTimeUtility.getFormattedDateTime(receiveDate, schemaDateFormat);
            final String tableString  = DateTimeUtility.getFormattedDateTime(receiveDate, tableDateFormat);

            if (log.isDebugEnabled())
                log.debug("Final schemaDateFormat : '" + schemaDateFormat + "' tableDateFormat : '" + tableDateFormat + "'");

            mBaseMessage.putValue(MiddlewareConstant.MW_DB_BILLING_INSERT_DATABASE_SUFFIX, schemaString);
            mBaseMessage.putValue(MiddlewareConstant.MW_DB_BILLING_INSERT_TABLE_SUFFIX, tableString);
        }
    }

}
