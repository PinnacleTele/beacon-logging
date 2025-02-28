package com.itextos.beacon.commonlib.pattern;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class PatternCache
{

    private static final boolean PATTTERN_CACHE_REQ           = true;
    private static final String  CARRIAGE_RETURN_AND_NEW_LINE = "\r\n";
    private static final String  CARRIAGE_RETURN              = "\r";
    private static final String  NEW_LINE                     = "\n";

    private static class SingletonHolder
    {

        static final PatternCache INSTANCE = new PatternCache();

    }

    public static PatternCache getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<PatternCheckCategory, Map<String, Pattern>>     patternCategoryMap = new ConcurrentHashMap<>();
    private final Map<PatternCheckCategory, List<PatternInfo>>        patternCountMap    = new EnumMap<>(PatternCheckCategory.class);
    private final Map<PatternCheckCategory, Map<String, PatternInfo>> patternCacheMap    = new EnumMap<>(PatternCheckCategory.class);

    private PatternCache()
    {}

    public boolean isPatternMatch(
            PatternCheckCategory aCategory,
            String aPattern,
            String aMessage)
    {
        return checkForRegularExpression(aCategory, aPattern, aMessage);
    }

    private boolean checkForRegularExpression(
            PatternCheckCategory aCategory,
            String aPattern,
            String aMessage)
    {
        if (PATTTERN_CACHE_REQ)
            synchronized (patternCategoryMap)
            {
                // May be we need to create a new Pattern object if its not working, by setting
                // PATTTERN_CACHE_REQ = false.
                final Map<String, Pattern>     patternMap        = patternCategoryMap.computeIfAbsent(aCategory, k -> new HashMap<>());
                final Pattern                  pattern           = patternMap.computeIfAbsent(aPattern, k -> getNewPattern(aCategory, aPattern));

                final Map<String, PatternInfo> patterCache       = patternCacheMap.get(aCategory);
                final List<PatternInfo>        patternCollection = patternCountMap.get(aCategory);

                if (patternCollection.size() > aCategory.getMaxInmemoryCount())
                {
                    final PatternInfo lPollFirst = patternCollection.remove(0);
                    patterCache.remove(lPollFirst.getPattern());
                    patternCategoryMap.get(aCategory).remove(lPollFirst.getPattern());
                }

                patternCacheMap.get(aCategory).get(aPattern).increaseUsedCount();

                return pattern.matcher(aMessage).matches();
            }

        return Pattern.compile(aPattern, Pattern.CASE_INSENSITIVE).matcher(aMessage).matches();
    }

    private Pattern getNewPattern(
            PatternCheckCategory aCategory,
            String aPattern)
    {
        final Pattern                  tempPattern       = Pattern.compile(aPattern, Pattern.CASE_INSENSITIVE);
        final List<PatternInfo>        patternCollection = patternCountMap.computeIfAbsent(aCategory, k -> new ArrayList<>());
        final Map<String, PatternInfo> patterCache       = patternCacheMap.computeIfAbsent(aCategory, k -> new HashMap<>());
        final PatternInfo              pi                = patterCache.computeIfAbsent(aPattern, k -> new PatternInfo(aCategory, aPattern));
        if (patternCollection.contains(pi))
            patternCollection.remove(pi);
        patternCollection.add(pi);

        return tempPattern;
    }

}