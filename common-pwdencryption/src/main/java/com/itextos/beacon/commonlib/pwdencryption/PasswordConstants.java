package com.itextos.beacon.commonlib.pwdencryption;

class PasswordConstants
{

    private PasswordConstants()
    {}

    static final int            INITIAL_VECTOR_LENGTH = 16;
    static final int            SALT_LEGNTH           = 10;

    static final String         ALGORITHM             = "AES/CBC/PKCS5Padding";
    static final String         SECRET_KEY_ALGORITHM  = "PBKDF2WithHmacSHA256";

    static final int            GUI_PASSWORD_LEGNTH   = 12;
    static final int            API_PASSWORD_LEGNTH   = 12;
    static final int            SMPP_PASSWORD_LEGNTH  = 8;

    static final String         API_PASSWORD_KEY      = "d2lubm92YXR1cmVhcGl0cnVzdGZvcmV2ZXI=";
    static final String         SMPP_PASSWORD_KEY     = "c21wcHRydXN0d2lubm92YXR1cmVmb3JldmVy";
    static final String         DB_PASSWORD_KEY       = "S3VtYXJhcGFuZGlhbmlUZXh0b3NTdW1hdGhp";

    private static final String UPPERCASE             = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE             = UPPERCASE.toLowerCase();
    private static final String NUMBERS               = "0123456789";
    private static final String ALPHABETS             = UPPERCASE + LOWERCASE;
    static final char[]         ALL_CHARS             = (ALPHABETS + NUMBERS).toCharArray();

}