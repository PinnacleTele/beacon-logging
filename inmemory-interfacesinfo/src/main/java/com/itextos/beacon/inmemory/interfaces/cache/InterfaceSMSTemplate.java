package com.itextos.beacon.inmemory.interfaces.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class InterfaceSMSTemplate
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log    log           = LogFactory.getLog(InterfaceSMSTemplate.class);

    private Map<String, String> mSmstemplates = new HashMap<>();

    public InterfaceSMSTemplate(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        final Map<String, String> lSmsTemplateMap = new HashMap<>();

        while (aResultSet.next())
        {
            final String key = CommonUtility.combine(CommonUtility.nullCheck(aResultSet.getString("cli_id"), true), CommonUtility.nullCheck(aResultSet.getString("template_id"), true));
            lSmsTemplateMap.put(key, CommonUtility.nullCheck(aResultSet.getString("template"), true));
        }

        if (lSmsTemplateMap.size() > 0)
            mSmstemplates = lSmsTemplateMap;
    }

    public String getInterfaceMsgTemplate(
            String aClientId,
            String aMsgTemplateId)
    {
        return mSmstemplates.get(CommonUtility.combine(aClientId, aMsgTemplateId));
    }

}