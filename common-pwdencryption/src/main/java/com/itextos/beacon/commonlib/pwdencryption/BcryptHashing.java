package com.itextos.beacon.commonlib.pwdencryption;

import org.mindrot.jbcrypt.BCrypt;

class BcryptHashing
{

    private static final int DEFAULT_HASH_ROUNDS = 10;

    private BcryptHashing()
    {}

    static EncryptedObject hash(
            String aPassword)
    {
        return hash(aPassword, DEFAULT_HASH_ROUNDS);
    }

    static EncryptedObject hash(
            String aStringToHash,
            int aDefaultHashRounds)
    {
        final String hashed = BCrypt.hashpw(aStringToHash, BCrypt.gensalt(aDefaultHashRounds));
        return new EncryptedObject(aStringToHash, hashed);
    }

    static boolean isValidHash(
            String aUserPassword,
            String aDbHashValue)
    {
        return BCrypt.checkpw(aUserPassword, aDbHashValue);
    }

}