package com.itextos.beacon.commonlib.constants;

public class MessageClassLength
{

    private MessageClassLength()
    {}

    public static final short MAX_LENGTH_PLAIN_MESSAGE            = 160;
    public static final short MAX_LENGTH_SP_PLAIN_MESSAGE         = 152;
    public static final short MAX_LENGTH_UNICODE_MESSAGE          = 280;
    public static final short MAX_LENGTH_SP_UNICODE_MESSAGE       = 264;
    public static final short MAX_LENGTH_BINARY_MESSAGE           = 280;
    public static final short MAX_LENGTH_UDH_MESSAGE              = 280;
    public static final short SPLIT_LENGTH_PLAIN_MESSAGE_8_BIT    = 153;
    public static final short SPLIT_LENGTH_PLAIN_MESSAGE_16_BIT   = 152;
    public static final short SPLIT_LENGTH_SP_PLAIN_MESSAGE       = 146;
    public static final short SPLIT_LENGTH_UNICODE_MESSAGE        = 268;
    public static final short SPLIT_LENGTH_SP_UNICODE_MESSAGE     = 256;
    public static final short SPLIT_LENGTH_BINARY_MESSAGE         = 256;
    public static final short SPLIT_LENGTH_UDH_MESSAGE            = 280;
    public static final short SPLIT_LENGTH_UNICODE_MESSAGE_16_BIT = 264;

}
