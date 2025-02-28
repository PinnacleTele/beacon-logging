package com.itextos.beacon.smpp.redisoperations;

public class RedisKeyConstants
{

    private RedisKeyConstants()
    {}

    static final String SESSION_BIND_INFO = "session:bind:info:";
    static final String DN_SESSION_INFO   = "dn:session:info:";
    static final String TX_SESSION_INFO   = "tx:session:info:";
    static final String SMPP_DN_QUEUE     = "smpp:dn:q:";
    static final String THROTTLER_KEY     = "smpp:tps:counter:";

}
