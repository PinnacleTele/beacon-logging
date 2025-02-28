package com.itextos.beacon.platform.templatefinder.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.itextos.beacon.commonlib.constants.Constants;
import com.itextos.beacon.platform.templatefinder.Result;
import com.itextos.beacon.platform.templatefinder.TemplateResult;
import com.itextos.beacon.platform.templatefinder.utility.TemplateUtility;

public class TemplateDataHolder
{

    private static final double ONE_KB = 1000d;
    private static final double ONE_MB = ONE_KB * 1024;
    private static final double ONE_GB = ONE_MB * 1024;

    private static class SingletonHolder
    {

        static final TemplateDataHolder INSTANCE = new TemplateDataHolder();

    }

    public static TemplateDataHolder getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    /**
     * <ul>
     * <li>Key - EntityId+Header
     * <li>Value - List of TemplateInfo
     * </ul>
     */
    private Map<String, TemplateInfo> allTemplates = new ConcurrentHashMap<>();

    private TemplateDataHolder()
    {}

    public void addTemplateInfo(
            Map<String, List<TemplateContents>> aTempAllTemplates)
    {
        final Map<String, TemplateInfo> tempMap = new HashMap<>();

        for (final Entry<String, List<TemplateContents>> entry : aTempAllTemplates.entrySet())
        {
            final String                 key    = entry.getKey();
            final String[]               list   = key.split("" + Constants.DEFAULT_CONCATENATE_CHAR);

            final List<TemplateContents> lValue = entry.getValue();

            for (final TemplateContents tci : lValue)
            {
                final String       tKey = TemplateUtility.getTemplateKey(list[0], list[1]);
                final TemplateInfo ti   = tempMap.computeIfAbsent(tKey, k -> new TemplateInfo(list[0], list[1]));

                ti.clearTemporaryTemplates();
                ti.addTemplateContentInfo(tci);
            }
        }
        if (tempMap.size() > 0)
            this.allTemplates = tempMap;
    }

    public TemplateResult getTemplates(
            String aTemplateGroupId,
            String aHeader,
            String aMessage)
    {
        final TemplateResult lTemplateResult = new TemplateResult();

        if (aTemplateGroupId.isEmpty() || aHeader.isEmpty() || aMessage.isEmpty())
            lTemplateResult.setResult(Result.TEMPLATE_INVALID_INPUTS);
        else
        {
            final String       key = TemplateUtility.getTemplateKey(aTemplateGroupId, aHeader);
            final TemplateInfo ti  = allTemplates.get(key);

            if (ti == null)
            {
                lTemplateResult.setResult(Result.TEMPLATE_NOT_FOUND);
                return lTemplateResult;
            }

            ti.checkTemplates(aMessage, lTemplateResult);
        }
        return lTemplateResult;
    }

    public double calculateSize()
    {

        try
        {

            try (
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    final ObjectOutputStream oos = new ObjectOutputStream(baos))
            {
                oos.writeObject(allTemplates);

                return baos.size() / ONE_MB;
            }
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

}