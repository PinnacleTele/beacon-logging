package com.itextos.beacon.inmemory.userheader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class DomesticUserHeaderInfo
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log         log                               = LogFactory.getLog(DomesticUserHeaderInfo.class);

    private Map<String, String>      mTemplateGroupHeaderEntiryIdMap   = new HashMap<>();
    private Map<String, Set<String>> mTemplateGroupHeaderTemplateIdMap = new HashMap<>();

    public DomesticUserHeaderInfo(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public boolean isHeaderMatches(
            String aTemplateGroupId,
            String aHeader)
    {
        return mTemplateGroupHeaderEntiryIdMap.containsKey(CommonUtility.combine(aTemplateGroupId, aHeader));
    }

    public String getEntityId(
            String aTemplateGroupId,
            String aHeader)
    {
        return mTemplateGroupHeaderEntiryIdMap.get(CommonUtility.combine(aTemplateGroupId, aHeader));
    }

    public Set<String> getTemplateIds(
            String aTemplateGroupId,
            String aHeader)
    {
        return mTemplateGroupHeaderTemplateIdMap.get(CommonUtility.combine(aTemplateGroupId, aHeader));
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select template_group_id, header, template_id, entity_id from
        // dlt_template_group_header_entity_map

        final Map<String, String>      lTemplateGroupHeaderEntiryIdMap   = new HashMap<>();
        final Map<String, Set<String>> lTemplateGroupHeaderTemplateIdMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String lTemplateGroupId = CommonUtility.nullCheck(aResultSet.getString("template_group_id"), true);
            final String lHeader          = CommonUtility.nullCheck(aResultSet.getString("header"), true);
            final String lEntityId        = CommonUtility.nullCheck(aResultSet.getString("entity_id"), true);
            final String lTemplateId      = CommonUtility.nullCheck(aResultSet.getString("template_id"), true);

            if ("".equals(lTemplateGroupId) && "".equals(lHeader))
                continue;

            final String temp = CommonUtility.combine(lTemplateGroupId, lHeader.toLowerCase());
            lTemplateGroupHeaderEntiryIdMap.put(temp, lEntityId);

            final Set<String> tempSet = lTemplateGroupHeaderTemplateIdMap.computeIfAbsent(temp, k -> new HashSet<>());
            tempSet.add(lTemplateId);
        }

        if (!lTemplateGroupHeaderEntiryIdMap.isEmpty())
        {
            mTemplateGroupHeaderEntiryIdMap   = lTemplateGroupHeaderEntiryIdMap;
            mTemplateGroupHeaderTemplateIdMap = lTemplateGroupHeaderTemplateIdMap;
        }
    }

}