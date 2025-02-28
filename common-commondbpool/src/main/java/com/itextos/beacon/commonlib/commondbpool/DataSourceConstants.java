package com.itextos.beacon.commonlib.commondbpool;

class DataSourceConstants
{

    DataSourceConstants()
    {}

    static final int     DEFAULT_INITIAL_SIZE                      = 1;
    static final int     DEFAULT_MAX_ACTIVE                        = 3;
    static final int     DEFAULT_MAX_TOTAL                         = 10;
    static final int     DEFAULT_MAX_IDLE                          = 2;
    static final int     DEFAULT_MIN_IDLE                          = 5;
    static final int     DEFAULT_MAX_WAIT                          = 10000;
    static final int     DEFAULT_MAX_WAIT_IN_MILLIS                = 10000;
    static final int     DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS = -1;
    static final int     DEFAULT_NUM_TESTS_PER_EVICTION_RUN        = 3;
    static final int     DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS    = 1800000;
    static final int     DEFAULT_REMOVE_ABANDONED_TIMEOUT          = 60;
    static final String  DEFAULT_VALIDATION_QUERY                  = "select 1";
    static final boolean DEFAULT_TEST_ON_BORROW                    = false;
    static final boolean DEFAULT_REMOVE_ABANDONED                  = true;
    static final boolean DEFAULT_REMOVE_ABANDONED_ON_MAINTENANCE   = true;
    static final boolean DEFAULT_REMOVE_ABANDONED_ON_BORROW        = true;
    static final boolean DEFAULT_LOG_ABANDONED                     = true;
    static final boolean DEFAULT_ABANDONED_USAGE_TRACKING          = false;
    static final String  BOOLEAN_DB_EQUIVALENT_TRUE                = "1";
    static final String  BOOLEAN_DB_EQUIVALENT_FALSE               = "0";

}
