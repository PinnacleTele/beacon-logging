package com.itextos.beacon.platform.templatefinder.data;

import java.util.Comparator;

public class ListComparator
        implements
        Comparator<TemplateContents>
{

    @Override
    public int compare(
            TemplateContents aO1,
            TemplateContents aO2)
    {
        if ((aO1 != null) && (aO2 != null))
            return aO2.getStaticWordsCount() - aO1.getStaticWordsCount();

        if (aO1 != null)
            return aO1.getStaticWordsCount();

        if (aO2 != null)
            return aO2.getStaticWordsCount();

        return 0;
    }

}
