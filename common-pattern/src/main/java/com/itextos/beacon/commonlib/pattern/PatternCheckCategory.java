package com.itextos.beacon.commonlib.pattern;

public enum PatternCheckCategory
{

    DLT_TEMPLATE_CHECK(10000),
    HEADER_CHECK(100),
    INTL_SPAM_CHECK(1000),
    SPAM_CHECK(1000),
    TEMPLATE_CHECK(1000),

    ;

    private final int mMaxInMemoryCount;

    PatternCheckCategory(
            int aMaxInmemoryCount)
    {
        mMaxInMemoryCount = aMaxInmemoryCount;
    }

    public int getMaxInmemoryCount()
    {
        return mMaxInMemoryCount;
    }

}