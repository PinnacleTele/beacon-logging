package com.itextos.beacon.platform.faillistutil.util;

public class FaillistConstants
{

    /**
     * To retain the same <code>String</code> in all the places of this project.
     */
    public static final String CLIENT_ADDRESS_GLOBAL        = "GLOBAL";
    /**
     * A <code>String</code> to represent the Key Separator in Redis.
     */
    public static final String REDIS_KEY_SEPARATOR          = ":";
    /**
     * The possible operation for the Record. <code>'A'</code> to represent the
     * <code>add / update</code> operation.
     */
    public static final String ACTION_ADD                   = "A";
    /**
     * The possible operation for the Record. <code>'D'</code> to represent the
     * <code>Delete / Remove</code> operation.
     */
    public static final String ACTION_DELETE                = "D";

    public static final String INTL_FILE_PATH               = "intl.file.path";
    public static final String INTL_NUMBER_SPLIT_LENGTH     = "intl.num.split.len";
    public static final String INTL_REDIS_POOL_KEY          = "intl.redis.pool.key";
    public static final String INTL_REDIS_PREFIX_KEY        = "intl.redis.prefix.key";

    public static final String DOMESTIC_FILE_PATH           = "domestic.file.path";
    public static final String DOMESTIC_NUMBER_SPLIT_LENGTH = "domestic.num.split.len";
    public static final String DOMESTIC_REDIS_POOL_KEY      = "domestic.redis.pool.key";
    public static final String DOMESTIC_REDIS_PREFIX_KEY    = "domestic.redis.prefix.key";

    public static final String BATCH_PROCESS_SIZE           = "batch.process.size";

}
