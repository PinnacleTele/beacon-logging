package com.itextos.beacon.inmemory.dnpayload.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.ItextosClient;
import com.itextos.beacon.inmemory.dnpayload.slab.ChildSlab;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class CustomSlabs
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log             log             = LogFactory.getLog(CustomSlabs.class);

    private Map<String, List<String>>    mMasterSlabInfo = new HashMap<>();
    private Map<String, List<ChildSlab>> mChildSlabInfo  = new HashMap<>();

    public CustomSlabs(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public List<String> getMasterSlabList(
            String aClientId)
    {
        if (log.isDebugEnabled())
            log.debug(" Master Map : " + mMasterSlabInfo);

        final ItextosClient lClient     = new ItextosClient(aClientId);

        List<String>        returnValue = mMasterSlabInfo.get(lClient.getClientId());
        if (returnValue != null)
            return returnValue;

        returnValue = mMasterSlabInfo.get(lClient.getAdmin());
        if (returnValue != null)
            return returnValue;

        return mMasterSlabInfo.get(lClient.getSuperAdmin());
    }

    // key -
    // dn_adjustment_parent.cli_id~dn_adjustment_parent.start_in_sec~dn_adjustment_parent.end_in_sec~dn_adjustment_parent.parent_id
    public List<ChildSlab> getChildSlabList(
            String aClientId)
    {
        if (log.isDebugEnabled())
            log.debug(" Child Map : " + mChildSlabInfo);

        final ItextosClient lClient     = new ItextosClient(aClientId);

        List<ChildSlab>     returnValue = mChildSlabInfo.get(lClient.getClientId());
        if (returnValue != null)
            return returnValue;

        returnValue = mChildSlabInfo.get(lClient.getAdmin());
        if (returnValue != null)
            return returnValue;

        return mChildSlabInfo.get(lClient.getSuperAdmin());
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // SELECT a.master_id,a.esmeaddr,a.start_in_sec AS masterstartinsec,a.end_in_sec
        // AS masterendinsec,b.id childid,b.start_in_sec AS childstartinsec,b.end_in_sec
        // AS childendinsec,b.percentage
        // FROM acc_dn_master_slabs a,acc_dn_child_slabs b WHERE a.master_id=b.master_id

        // Tables : dn_adjustment_parent, dn_adjustment_child

        final Map<String, List<String>>    lMsaterSlabMap = new HashMap<>();
        final Map<String, List<ChildSlab>> lChildSlabMap  = new HashMap<>();

        while (aResultSet.next())
        {
            final String lClientId         = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lParentId         = CommonUtility.nullCheck(aResultSet.getString("parent_id"), true);
            final String lParentStartInSec = CommonUtility.nullCheck(aResultSet.getString("parent_start_insec"), true);
            String       lParentEndInSec   = CommonUtility.nullCheck(aResultSet.getString("parent_end_insec"), true);

            // setting max integer value for infinite
            if (lParentEndInSec.equals("-1"))
                lParentEndInSec = Integer.toString(Integer.MAX_VALUE);

            final String lChildId         = CommonUtility.nullCheck(aResultSet.getString("child_id"), true);
            final String lChildStartInSec = CommonUtility.nullCheck(aResultSet.getString("child_start_insec"), true);
            final String lChildEndInSec   = CommonUtility.nullCheck(aResultSet.getString("child_end_insec"), true);
            final String lPercentage      = CommonUtility.nullCheck(aResultSet.getString("percentage"), true);

            if (StringUtils.isNumeric(lParentId) && StringUtils.isNumeric(lClientId) //
                    && StringUtils.isNumeric(lParentStartInSec) && StringUtils.isNumeric(lParentEndInSec)//
                    && StringUtils.isNumeric(lChildId) && StringUtils.isNumeric(lChildStartInSec) //
                    && StringUtils.isNumeric(lChildEndInSec) && StringUtils.isNumeric(lPercentage))
            {
                final String       masterStartPlusEnd = CommonUtility.combine(lClientId, lParentStartInSec, lParentEndInSec, lParentId);
                final List<String> list               = lMsaterSlabMap.computeIfAbsent(lClientId, k -> new ArrayList<>());

                if (!list.contains(masterStartPlusEnd))
                    list.add(masterStartPlusEnd);

                final Map<String, String> record = new HashMap<>();
                record.put("childid", lChildId);
                record.put("childstartinsec", lChildStartInSec);
                record.put("childendinsec", lChildEndInSec);
                record.put("percentage", lPercentage);

                final ChildSlab       childSlab  = new ChildSlab(lChildId, lChildStartInSec, lChildEndInSec, lPercentage);
                final List<ChildSlab> childLlist = lChildSlabMap.computeIfAbsent(masterStartPlusEnd, k -> new ArrayList<>());
                childLlist.add(childSlab);
            }
        }

        if (!lMsaterSlabMap.isEmpty())
            mMasterSlabInfo = lMsaterSlabMap;

        if (!lChildSlabMap.isEmpty())
            mChildSlabInfo = lChildSlabMap;

        if (log.isDebugEnabled())
        {
            log.debug("MasterMap:" + mMasterSlabInfo);
            log.debug("ChildMap:" + mChildSlabInfo);
        }
    }

}