package com.itextos.beacon.inmemory.customfeatures;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class InmemSmppParentChildAccountMap
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log          log                = LogFactory.getLog(InmemSmppParentChildAccountMap.class);

    private Map<String, String>       mParentToChildInfo = new HashMap<>();
    private Map<String, List<String>> mParentToChildList = new HashMap<>();
    private Map<String, List<String>> mChildToParentList = new HashMap<>();

    public InmemSmppParentChildAccountMap(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public String getMsgAccInfo(
            String aClient,
            String aOrgId)
    {
        final String key = CommonUtility.combine(aClient, aOrgId.toLowerCase());
        return mParentToChildInfo.get(key);
    }

    public List<String> getParentAccInfo(
            String aMsgClientId)
    {
        return mParentToChildList.get(aMsgClientId);
    }

    public List<String> getParentClientIdByChildClientId(
            String aMsgClient)
    {
        return mChildToParentList.get(aMsgClient);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + getClass());

        // SELECT * FROM interface_to_message_acc_mapping order by message_esme,
        // interface_esme

        final Map<String, String>       lInterfaceToMsgInfo = new HashMap<>();
        final Map<String, List<String>> lInterfaceToMsgList = new HashMap<>();
        final Map<String, List<String>> lMsgToInterfaceList = new HashMap<>();

        while (aResultSet.next())
        {
            final String lParentClientId         = CommonUtility.nullCheck(aResultSet.getString("parent_cli_id"), true);
            final String lOrgId                  = CommonUtility.nullCheck(aResultSet.getString("org_id"), true);
            final String lChildClientId          = CommonUtility.nullCheck(aResultSet.getString("child_cli_id"), true);
            final String lMsgType                = CommonUtility.nullCheck(aResultSet.getString("msg_type"), true);
            final String lParentClientIdAllowDlr = CommonUtility.nullCheck(aResultSet.getString("parent_cli_id_allow_dlr"), true);

            if (!"".equals(lParentClientId) && !"".equals(lOrgId) && !"".equals(lChildClientId))
                lInterfaceToMsgInfo.put(CommonUtility.combine(lParentClientId, lOrgId), lChildClientId);

            // This is required for MT flow - since MT flow knows Parent ClientId
            if ("1".equals(lParentClientIdAllowDlr))
            {
                final List<String> lTempLs = lInterfaceToMsgList.computeIfAbsent(lChildClientId, k -> new ArrayList<>());
                lTempLs.add(lParentClientId);
            }

            // This is required for MO flow - since MO flow doesn't know interfaceesme
            final List<String> lTempLs = lMsgToInterfaceList.computeIfAbsent(lChildClientId, k -> new ArrayList<>());
            lTempLs.add(lParentClientId);
        }

        if (!lInterfaceToMsgInfo.isEmpty())
        {
            mParentToChildInfo = lInterfaceToMsgInfo;
            mParentToChildList = lInterfaceToMsgList;
            mChildToParentList = lMsgToInterfaceList;
        }
    }

}
