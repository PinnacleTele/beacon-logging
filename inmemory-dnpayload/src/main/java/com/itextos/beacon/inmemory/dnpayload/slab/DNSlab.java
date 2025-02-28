package com.itextos.beacon.inmemory.dnpayload.slab;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.RoundRobin;
import com.itextos.beacon.inmemory.dnpayload.util.DNPUtil;

public class DNSlab
{

    private static final Log    log    = LogFactory.getLog(DNSlab.class);
    private static final String RR_KEY = "DNSLAB-";

    public static DNSlab getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder
    {

        static final DNSlab INSTANCE = new DNSlab();

    }

    private Map<String, Map<String, String>> mSlabHistory                  = new HashMap<>();
    private Map<String, String>              mTotalMessageProcessedHistory = new HashMap<>();

    private DNSlab()
    {}

    public void reset()
    {
        mSlabHistory                  = new HashMap<>();
        mTotalMessageProcessedHistory = new HashMap<>();
    }

    public ChildSlab doSlabAdjustments(
            String aClientId,
            long lDtime,
            long aSts)
            throws Exception
    {
        final List<String> lMasterSlabList = DNPUtil.getMasterSlabList(aClientId);

        if (log.isDebugEnabled())
            log.debug("MasterSlabList :" + lMasterSlabList);

        if (lMasterSlabList != null)
            // value format -
            // dn_adjustment_parent.cli_id~dn_adjustment_parent.start_in_sec~dn_adjustment_parent.end_in_sec~dn_adjustment_parent.parent_id
            for (final String clientParentInfo : lMasterSlabList)
            {
                if (log.isDebugEnabled())
                    log.debug("Master key :" + clientParentInfo);

                final long lStartInsec = CommonUtility.getLong(clientParentInfo.split("~")[1]) * 1000;
                final long lEndInsec   = CommonUtility.getLong(clientParentInfo.split("~")[2]) * 1000;
                final long timeDiff    = (lDtime - aSts);

                if ((timeDiff >= lStartInsec) && (timeDiff <= lEndInsec))
                {
                    if (log.isDebugEnabled())
                        log.debug("Dtime-sts falls in slab for startInsec:" + lStartInsec + " endInsec:" + lEndInsec + " dtime:" + lDtime + " sts:" + aSts + " ClientId:" + aClientId);

                    final List<ChildSlab> lChildSlab = DNPUtil.getChildSlabList(clientParentInfo);

                    if (log.isDebugEnabled())
                        log.debug("childSlabList :" + lChildSlab);

                    if ((lChildSlab != null) && !lChildSlab.isEmpty())
                        return getSlabMap(lChildSlab, aClientId);
                    return null;
                }
                else
                    if (log.isDebugEnabled())
                        log.debug("Not in b/w slab for startInsec:" + lStartInsec + " endInsec:" + lEndInsec + " dtime:" + lDtime + " sts:" + aSts + " going to check if further intervals configured");
            }
        return null;
    }

    public synchronized ChildSlab getSlabMap(
            List<ChildSlab> aSlablist,
            String aClientId)
    {
        final String lRoundRobinKey = RR_KEY + aClientId;
        int          lRRIndex       = RoundRobin.getInstance().getCurrentIndex(lRoundRobinKey, aSlablist.size());

        lRRIndex = (lRRIndex - 1);

        if (log.isDebugEnabled())
            log.debug("slablist size :" + aSlablist.size() + " cursor position " + lRRIndex);

        return getSlabMap(aClientId, aSlablist, lRRIndex);
    }

    private ChildSlab getSlabMap(
            String aClientId,
            List<ChildSlab> aSlablist,
            int aRRIndex)
    {
        ChildSlab       lSlabInfo       = aSlablist.get(aRRIndex);

        final ChildSlab lCheckForFallIn = checkForFallIn(aClientId, lSlabInfo);

        if (lCheckForFallIn != null)
            return lCheckForFallIn;

        lSlabInfo = getSlabMapByRecursiveCheck(aClientId, aSlablist, aRRIndex);

        if (log.isDebugEnabled())
            log.debug("Slab found at RecursiveCheck Attempt for Client Id : " + aClientId + " Slab is " + lSlabInfo);

        return lSlabInfo;
    }

    private ChildSlab getSlabMapByRecursiveCheck(
            String aClientId,
            List<ChildSlab> aSlablist,
            int aCurrentPosition)
    {

        for (int index = aCurrentPosition + 1; index < aSlablist.size(); index++)
        {
            // forward check
            final ChildSlab lCheckForFallIn = checkForFallIn(aClientId, aSlablist.get(index));

            if (lCheckForFallIn != null)
                return lCheckForFallIn;
        }

        for (int index = 0; index < aCurrentPosition; index++)
        {
            // backward check
            final ChildSlab lCheckForFallIn = checkForFallIn(aClientId, aSlablist.get(index));

            if (lCheckForFallIn != null)
                return lCheckForFallIn;
        }

        final ChildSlab slabmap      = aSlablist.get(aCurrentPosition);
        final String    primarykeyid = slabmap.getChildId();
        incrementTheHistory(aClientId, primarykeyid);

        return slabmap;
    }

    private ChildSlab checkForFallIn(
            String aClientId,
            ChildSlab aSlabInfo)
    {
        final String lPrimarykeyId      = aSlabInfo.getChildId();
        final String lAllowedpercentage = aSlabInfo.getPercentage();

        if (isValidSlab(aClientId, lPrimarykeyId, lAllowedpercentage))
        {
            if (log.isDebugEnabled())
                log.debug("Slab found at first Attempt for Client Id : " + aClientId + " Slab is " + aSlabInfo);

            return aSlabInfo;
        }
        return null;
    }

    private boolean isValidSlab(
            String aClientId,
            String aPrimarykeyId,
            String aAllowedPercentage)
    {
        final int lAllowedpercentageforthisslab = CommonUtility.getInteger(aAllowedPercentage);
        final int lSendCountForThisSlab         = CommonUtility.getInteger(getProcessedCount(aClientId, aPrimarykeyId));
        final int lTotalmsgcount                = getTotalProcessedMessageCount(aClientId) + 1;
        final int lProcessedpercentage          = (int) Math.floor((lSendCountForThisSlab * 100D) / lTotalmsgcount);

        if (log.isDebugEnabled())
        {
            log.debug("sendCountForThisSlab : " + lSendCountForThisSlab + ", totalmsgcount : " + lTotalmsgcount);
            log.debug("isValidSlabmap() primarykeyid = " + aPrimarykeyId //
                    + " allowedpercentageforthisSlab = " + lAllowedpercentageforthisslab //
                    + " processedpercentage = " + lProcessedpercentage//
                    + " sendCountForThisSlab = " + lSendCountForThisSlab //
                    + " totalmsgcount = " + lTotalmsgcount);
        }

        if (lAllowedpercentageforthisslab > lProcessedpercentage)
        {
            incrementTheHistory(aClientId, aPrimarykeyId);
            return true;
        }

        if (log.isDebugEnabled())
            log.debug("slab exceeded : primarykeyid = " + aPrimarykeyId //
                    + " allowedpercentageforthisSlab = " + lAllowedpercentageforthisslab //
                    + " processedpercentage = " + lProcessedpercentage //
                    + " sendCountForThisSlab = " + lSendCountForThisSlab //
                    + " totalmsgcount = " + lTotalmsgcount);

        return false;
    }

    private String getProcessedCount(
            String aClientId,
            String aPrimarykeyId)
    {
        if (log.isDebugEnabled())
            log.debug("ClientId :" + aClientId + " primarykeyid:" + aPrimarykeyId);

        final Map<String, String> lSlabHistoryforClient = mSlabHistory.computeIfAbsent(aClientId, k -> new HashMap<>());

        if (log.isDebugEnabled())
            log.debug("SlabHistoryforClient" + lSlabHistoryforClient);

        return lSlabHistoryforClient.computeIfAbsent(aPrimarykeyId, k -> "0");
    }

    private void incrementTheHistory(
            String aClientId,
            String aPrimarykeyId)
    {
        if (log.isDebugEnabled())
            log.debug("incrementTheHistory : ");

        try
        {
            final Map<String, String> lSlabProcessedMap = mSlabHistory.get(aClientId);

            if (lSlabProcessedMap != null)
            {
                final String count = lSlabProcessedMap.get(aPrimarykeyId);

                if (count != null)
                {
                    int lProcessedcount = CommonUtility.getInteger(count);
                    lProcessedcount++;
                    lSlabProcessedMap.put(aPrimarykeyId, String.valueOf(lProcessedcount));

                    if (log.isDebugEnabled())
                        log.debug("incrementTheHistory : primarykeyid :  " + aPrimarykeyId + " count : " + lProcessedcount);
                }
            }

            if (log.isDebugEnabled())
                log.debug("incrementTheHistory : slabHistory:" + mSlabHistory);

            final String count          = mTotalMessageProcessedHistory.computeIfAbsent(aClientId, k -> "0");
            int          processedcount = CommonUtility.getInteger(count);
            processedcount++;
            mTotalMessageProcessedHistory.put(aClientId, String.valueOf(processedcount));

            if (log.isDebugEnabled())
                log.debug("ClientId :  " + aClientId + " count : " + processedcount);
        }
        catch (final Exception ignore)
        {
            log.error("incrementTheHistory ", ignore);
        }
    }

    private int getTotalProcessedMessageCount(
            String aClientId)
    {
        if (log.isDebugEnabled())
            log.debug("getTotalProcessedMessageCount ...................");

        return CommonUtility.getInteger(mTotalMessageProcessedHistory.computeIfAbsent(aClientId, k -> "0"));
    }

}
