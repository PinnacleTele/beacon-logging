package com.itextos.beacon.inmemory.rr.cache;

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

public class LoadVoiceMessageTemplate
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log          log               = LogFactory.getLog(LoadVoiceMessageTemplate.class);
    private Map<String, List<Object>> mVoiceTemplateMap = new HashMap<>();

    public LoadVoiceMessageTemplate(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public List<Object> getVoiceTemplateInfo(
            String aKey)
    {
        return mVoiceTemplateMap.get(aKey);
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // select * from OTP_VOICE_TEMPLATE_MAPPING

        // Table Name: otp_voice_template_map

        final Map<String, List<Object>> lVoiceTemplateInfo = new HashMap<>();

        while (aResultSet.next())
        {
            final String lClientId     = aResultSet.getString("cli_id");
            final String lVoiceCfgId   = aResultSet.getString("voice_config_id");

            final String lKey          = CommonUtility.combine(lClientId, lVoiceCfgId);

            List<Object> lTemplateList = lVoiceTemplateInfo.get(lKey);
            if (lTemplateList == null)
                lTemplateList = new ArrayList<>();

            final Object map = CommonUtility.getMapFromResultSet(aResultSet, aResultSet.getMetaData());
            lTemplateList.add(map);
            lVoiceTemplateInfo.put(lKey, lTemplateList);
        }

        mVoiceTemplateMap = lVoiceTemplateInfo;
    }

}
