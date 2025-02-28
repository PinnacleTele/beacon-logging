package com.itextos.beacon.platform.walletreminder.data.summary;

import java.text.MessageFormat;
import java.util.Date;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.walletreminder.data.DataCollector;
import com.itextos.beacon.platform.walletreminder.utils.SortBy;
import com.itextos.beacon.platform.walletreminder.utils.WalletReminderProperties;

public class DailySummary
        extends
        FullSummary
{

    @Override
    public String getSubject()
    {
        final String lDailyBalanceRemainderSubjectFormat = WalletReminderProperties.getInstance().getDailyBalanceRemainderSubjectFormat();
        return MessageFormat.format(lDailyBalanceRemainderSubjectFormat,
                DateTimeUtility.getFormattedDateTime(new Date(DataCollector.getInstance().getLastDataReceived()), DateTimeFormat.DEFAULT_DATE_ONLY));
    }

    @Override
    SortBy getSortBy()
    {
        return WalletReminderProperties.getInstance().getDailySortBy();
    }

    @Override
    boolean getGroupBy()
    {
        return WalletReminderProperties.getInstance().getDailyGroupBy();
    }

}
