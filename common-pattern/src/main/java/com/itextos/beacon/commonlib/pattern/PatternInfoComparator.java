package com.itextos.beacon.commonlib.pattern;

import java.util.Comparator;

class PatternInfoComparator
        implements
        Comparator<PatternInfo>
{

    @Override
    public int compare(
            PatternInfo aO1,
            PatternInfo aO2)
    {

        if ((aO1 != null) && (aO2 != null))
        {
            final int diff = aO1.getUsedCount() - aO2.getUsedCount();
            if (diff == 0)
                return (int) (aO1.getLastUsedTime() - aO2.getLastUsedTime());
            return diff;
        }
        return 0;
    }

}