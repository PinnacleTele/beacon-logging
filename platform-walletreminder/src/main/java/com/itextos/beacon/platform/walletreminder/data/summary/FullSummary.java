package com.itextos.beacon.platform.walletreminder.data.summary;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.walletreminder.data.DataCollector;
import com.itextos.beacon.platform.walletreminder.data.UserInfo;
import com.itextos.beacon.platform.walletreminder.utils.SortBy;

public abstract class FullSummary
        extends
        SummaryConstants
{

    abstract boolean getGroupBy();

    abstract SortBy getSortBy();

    public abstract String getSubject();

    public String getSummary()
    {
        final StringBuilder sb = new StringBuilder();

        sb.append(getTop());
        sb.append(getSubject());
        sb.append(getHeaders());

        sb.append(getBalanceData());
        sb.append(getGeneratedTime());
        sb.append(getBottom());
        return sb.toString();
    }

    private static String getGeneratedTime()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("<tr><td colspan='6'>Mail generated on ").append(DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT)).append("</td></tr>");
        return sb.toString();
    }

    private String getBalanceData()
    {

        switch (getSortBy())
        {
            case BALANCE_ASCENDING:
                return getWalletBalance(true);

            case BALANCE_DESCENDING:
                return getWalletBalance(false);

            default:
            case CLIENT_ID_ASCENDING:
                return getClientIdBasedData();

            case USER_NAME_ASCENDING:
                return getUserBasedData();
        }
    }

    private String getClientIdBasedData()
    {
        if (getGroupBy())
            return getClientGroupBy();
        return getDetailData(DataCollector.getInstance().getUserWalletBalance());
    }

    private String getHeaders()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(HTML_HEADER);

        String[] headers = CLIID_HEADERS;
        if (getSortBy() == SortBy.USER_NAME_ASCENDING)
            headers = USER_HEADERS;

        for (final String head : headers)
            sb.append("<th align='center'>").append(head).append("</th>");
        sb.append("</tr>");
        return sb.toString();
    }

    private static String getBottom()
    {
        return HTML_BOTTOM;
    }

    private static String getClientGroupBy()
    {
        final Map<Long, Map<Long, Set<Long>>> lUserBasedMap = DataCollector.getInstance().getClientIdBasedMap();

        final StringBuilder                   sb            = new StringBuilder();
        int                                   sNo           = 0;
        final boolean                         userNameFirst = false;

        for (final Entry<Long, Map<Long, Set<Long>>> suEntry : lUserBasedMap.entrySet())
        {
            int            level     = 1;

            final Long     sCliId    = suEntry.getKey();
            final UserInfo sUserInfo = DataCollector.getInstance().getUserInfo(sCliId);

            if (!sUserInfo.isValidBalance())
                continue;

            sNo++;
            sb.append(getUserInfo(sNo, sUserInfo, level, userNameFirst));

            final Map<Long, Set<Long>> puMap = suEntry.getValue();

            for (final Entry<Long, Set<Long>> puEntry : puMap.entrySet())
            {
                final Long pCliId = puEntry.getKey();

                level = 2;

                final UserInfo pUserInfo = DataCollector.getInstance().getUserInfo(pCliId);

                if (!pUserInfo.isValidBalance())
                    continue;

                sNo++;
                sb.append(getUserInfo(sNo, pUserInfo, level, userNameFirst));

                final Set<Long> users = puEntry.getValue();

                for (final Long cliId : users)
                {
                    level = 3;

                    final UserInfo userInfo = DataCollector.getInstance().getUserInfo(cliId);

                    if (!userInfo.isValidBalance())
                        continue;

                    sNo++;
                    sb.append(getUserInfo(sNo, userInfo, level, userNameFirst));
                }
            }
        }

        return sb.toString();
    }

    private static String getDetailData(
            Set<String> aUserList)
    {
        final StringBuilder sb  = new StringBuilder();
        int                 sNo = 0;

        for (final String user : aUserList)
        {
            final Long     cliId     = DataCollector.getInstance().getClientIdByUser(user);
            final UserInfo sUserInfo = DataCollector.getInstance().getUserInfo(cliId);

            if (!sUserInfo.isValidBalance())
                continue;

            sNo++;
            final String temp = getUserInfo(sNo, sUserInfo, 0, true);
            sb.append(temp);
        }
        return sb.toString();
    }

    private static String getDetailData(
            Map<Long, UserInfo> aMap)
    {
        final StringBuilder sb  = new StringBuilder();
        int                 sNo = 0;

        for (final Entry<Long, UserInfo> entry : aMap.entrySet())
        {
            final Long     cliId     = entry.getKey();
            final UserInfo sUserInfo = DataCollector.getInstance().getUserInfo(cliId);

            if (!sUserInfo.isValidBalance())
                continue;

            sNo++;
            final String temp = getUserInfo(sNo, sUserInfo, 0, false);
            sb.append(temp);
        }
        return sb.toString();
    }

    protected static String getSpace(
            int aLevel)
    {
        final StringBuilder sb = new StringBuilder();

        for (int index = 0; index < (aLevel - 1); index++)
            for (int spaceIndex = 0; spaceIndex < 5; spaceIndex++)
                sb.append("&nbsp;");

        return sb.toString();
    }

    private static String getTop()
    {
        return HTML_TOP;
    }

    private String getUserBasedData()
    {
        if (getGroupBy())
            return getUserBasedGroupData();
        return getDetailData(DataCollector.getInstance().getUserList());
    }

    private static String getUserBasedGroupData()
    {
        final Map<String, Map<String, Set<String>>> lUserBasedMap = DataCollector.getInstance().getUserBasedMap();

        final StringBuilder                         sb            = new StringBuilder();
        int                                         sNo           = 0;
        final boolean                               userNameFirst = true;

        for (final Entry<String, Map<String, Set<String>>> suEntry : lUserBasedMap.entrySet())
        {
            int            level     = 1;

            final String   sUser     = suEntry.getKey();
            final Long     sCliId    = DataCollector.getInstance().getClientIdByUser(sUser);
            final UserInfo sUserInfo = DataCollector.getInstance().getUserInfo(sCliId);

            if (!sUserInfo.isValidBalance())
                continue;

            sNo++;
            sb.append(getUserInfo(sNo, sUserInfo, level, userNameFirst));

            final Map<String, Set<String>> puMap = suEntry.getValue();

            for (final Entry<String, Set<String>> puEntry : puMap.entrySet())
            {
                final String pUser  = puEntry.getKey();
                final Long   pCliId = DataCollector.getInstance().getClientIdByUser(pUser);

                level = 2;

                final UserInfo pUserInfo = DataCollector.getInstance().getUserInfo(pCliId);

                if (!pUserInfo.isValidBalance())
                    continue;

                sNo++;
                sb.append(getUserInfo(sNo, pUserInfo, level, userNameFirst));

                final Set<String> users = puEntry.getValue();

                for (final String user : users)
                {
                    level = 3;

                    final Long     cliId    = DataCollector.getInstance().getClientIdByUser(user);
                    final UserInfo userInfo = DataCollector.getInstance().getUserInfo(cliId);

                    if (!userInfo.isValidBalance())
                        continue;

                    sNo++;
                    sb.append(getUserInfo(sNo, userInfo, level, userNameFirst));
                }
            }
        }

        return sb.toString();
    }

    private static String getUserInfo(
            int aSNo,
            UserInfo aUserInfo,
            int aLevel,
            boolean aUserNameFirst)
    {
        final StringBuilder sb = new StringBuilder(TR_OPEN);
        sb.append(TD_ALIGN_LEFT).append(aSNo).append(TD_CLOSE);

        final String prefixSpace = getSpace(aLevel);

        if (aUserNameFirst)
        {
            sb.append(TD_ALIGN_LEFT).append(prefixSpace).append(aUserInfo.getUser()).append(TD_CLOSE);
            sb.append(TD_ALIGN_LEFT).append(prefixSpace).append(aUserInfo.getClientId()).append(TD_CLOSE);
        }
        else
        {
            sb.append(TD_ALIGN_LEFT).append(prefixSpace).append(aUserInfo.getClientId()).append(TD_CLOSE);
            sb.append(TD_ALIGN_LEFT).append(prefixSpace).append(aUserInfo.getUser()).append(TD_CLOSE);
        }
        sb.append(TD_ALIGN_RIGHT).append(aUserInfo.getPrintableWalletBalance()).append(TD_CLOSE);
        sb.append(TD_ALIGN_LEFT).append(aUserInfo.getFirstName()).append(" ").append(aUserInfo.getLastName()).append(TD_CLOSE);
        sb.append(TD_ALIGN_LEFT).append(aUserInfo.getEmailAddress()).append(TD_CLOSE);
        return sb.toString();
    }

    private static String getWalletBalance(
            boolean aIsAscending)
    {
        final Map<Long, UserInfo> lUserWalletBalance = DataCollector.getInstance().getUserWalletBalance(aIsAscending);
        return getDetailData(lUserWalletBalance);
    }

}