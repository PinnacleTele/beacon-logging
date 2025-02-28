package com.itextos.beacon.platform.templatefinder.utility;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;

public class DltTemplateProperties
{

    private static final Log    log                                                 = LogFactory.getLog(DltTemplateProperties.class);

    private static final String PROP_REPLACE_SPECIAL_CHARS_ASCII                    = "replace.special.chars.ascii";
    private static final String PROP_DATA_LOADER_THREAD_POOL_SIZE                   = "dataloader.threadpool.size";
    private static final String PROP_RECENTLY_USED_TEMPLATE_ID_COUNT                = "recently.used.templateids.count";
    private static final String PROP_NON_MATCHING_MESSAGE_CREATE_TEMPLATE_MAX_COUNT = "non.matching.message.create.template.max.count";
    private static final String PROP_CREATE_TEMPLATE_MIN_WORD_COUNT                 = "create.template.min.word.count";

    private static class SingletonHolder
    {

        static final DltTemplateProperties INSTANCE = new DltTemplateProperties();

    }

    public static DltTemplateProperties getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    PropertiesConfiguration pc = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.DLT_TEMPLATES_CONFIG_PROPERTIES, true);

    private DltTemplateProperties()
    {}

    public int getDataloaderThreadPoolSize()
    {
        return pc.getInt(PROP_DATA_LOADER_THREAD_POOL_SIZE, 25);
    }

    public int getRecentlyUsedTemplateIdCount()
    {
        return pc.getInt(PROP_RECENTLY_USED_TEMPLATE_ID_COUNT, 10);
    }

    public int getNonMatchingMessageCreateTemplateMaxCount()
    {
        return pc.getInt(PROP_NON_MATCHING_MESSAGE_CREATE_TEMPLATE_MAX_COUNT, 10);
    }

    public int getNonMatchingMessageCreateTemplateMinWordCount()
    {
        return pc.getInt(PROP_CREATE_TEMPLATE_MIN_WORD_COUNT, 5);
    }

    public String getReplaceChars(
            boolean aSkipSpace)
    {
        final String[]      asciis = pc.getStringArray(PROP_REPLACE_SPECIAL_CHARS_ASCII);
        int                 count  = 0;
        final StringBuilder sb     = new StringBuilder();

        for (final String s : asciis)
        {
            count++;

            try
            {
                if (aSkipSpace && (s.equals("32")))
                    continue;
                final char c = (char) Integer.parseInt(s);
                sb.append(c);
            }
            catch (final Exception e)
            {
                log.error("Exception while loading the DLT Template replace chars Skipping '" + s + "' in position " + count, e);
            }
        }
        return sb.toString();
    }

}