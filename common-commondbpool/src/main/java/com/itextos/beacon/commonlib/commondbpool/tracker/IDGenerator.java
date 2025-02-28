package com.itextos.beacon.commonlib.commondbpool.tracker;

import java.util.HashMap;

public class IDGenerator
{

    public static final String CONNECTION_PREFIX         = "CONN";
    public static final String STATEMENT_PREFIX          = "STMT";
    public static final String PREPARED_STATEMENT_PREFIX = "PSTT";
    public static final String CALLABLE_STATEMENT_PREFIX = "CSTT";
    public static final String RESULT_SET_PREFIX         = "RSET";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final IDGenerator INSTANCE = new IDGenerator();

    }

    public static IDGenerator getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final HashMap<String, Long> idMap = new HashMap<>();

    private IDGenerator()
    {
        idMap.put(CONNECTION_PREFIX, 0L);
        idMap.put(STATEMENT_PREFIX, 0L);
        idMap.put(PREPARED_STATEMENT_PREFIX, 0L);
        idMap.put(CALLABLE_STATEMENT_PREFIX, 0L);
        idMap.put(RESULT_SET_PREFIX, 0L);
    }

    private String getNextID(
            String aIDType)
    {
        String returnValue = null;
        long   tempLastID  = idMap.get(aIDType);

        if (tempLastID == Long.MAX_VALUE)
            tempLastID = 0;

        returnValue = aIDType + (++tempLastID);
        idMap.put(aIDType, tempLastID);
        return returnValue;
    }

    public String getNextStatementId()
    {
        return getNextID(STATEMENT_PREFIX);
    }

    public String getNextPreparedStatementId()
    {
        return getNextID(PREPARED_STATEMENT_PREFIX);
    }

    public String getNextCallableStatementId()
    {
        return getNextID(CALLABLE_STATEMENT_PREFIX);
    }

    public String getNextConnectionId()
    {
        return getNextID(CONNECTION_PREFIX);
    }

    public String getNextResultSetId()
    {
        return getNextID(RESULT_SET_PREFIX);
    }

}