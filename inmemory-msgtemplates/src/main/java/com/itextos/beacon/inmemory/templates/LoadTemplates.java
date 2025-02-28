package com.itextos.beacon.inmemory.templates;

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

public class LoadTemplates
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log          log              = LogFactory.getLog(LoadTemplates.class);

    private Map<String, List<String>> mMsgTemplatesMap = new HashMap<>();

    public LoadTemplates(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public List<String> getTemplateList(
            String aClientId)
    {
        return mMsgTemplatesMap.get(aClientId);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, List<String>> loadMsgTemplates = new HashMap<>();

        while (aResultSet.next())
        {
            final String lTemplateId  = CommonUtility.nullCheck(aResultSet.getString("template_id"), true);
            final String lClientId    = CommonUtility.nullCheck(aResultSet.getString("cli_id"), true);
            final String lMsgTemplate = CommonUtility.nullCheck(aResultSet.getString("msg_template"), true);
            final String lEncrypt     = CommonUtility.nullCheck(aResultSet.getString("encrypt"), true);

            if ("".equals(lMsgTemplate))
                continue;

            final List<String> lTemplatesLst = loadMsgTemplates.computeIfAbsent(lClientId, k -> new ArrayList<>());
            lTemplatesLst.add(CommonUtility.combine(lTemplateId, lEncrypt, lMsgTemplate));
        }

        if (!loadMsgTemplates.isEmpty())
            mMsgTemplatesMap = loadMsgTemplates;
    }

}