package com.itextos.beacon.commonlib.utility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClientLevel;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

public class ItextosClient
{

    private static final Log  log                               = LogFactory.getLog(ItextosClient.class);
    public static final int   CLIENT_ID_USER_LEVEL_LENGTH       = 16;
    public static final int   CLIENT_ID_ADMIN_USER_LEVEL_LENGTH = 12;
    public static final int   CLIENT_ID_SUPER_USER_LEVEL_LENGTH = 8;

    private final String      mClientId;
    private final String      mAdmin;
    private final String      mSuperAdmin;
    private final ClientLevel mClientLevel;

    public ItextosClient(
            String aClientId)
    {
        final int len = CommonUtility.nullCheck(aClientId, true).length();

        if (log.isDebugEnabled())
            log.debug("Client id passed '" + aClientId + "' Length : " + len);

        switch (len)
        {
            case CLIENT_ID_USER_LEVEL_LENGTH:
            {
                mClientId    = aClientId;
                mAdmin       = mClientId.substring(0, 12);
                mSuperAdmin  = mClientId.substring(0, 8);
                mClientLevel = ClientLevel.USER;
                break;
            }

            case CLIENT_ID_ADMIN_USER_LEVEL_LENGTH:
            {
                final String errorMsg = "Passed clientid is at Admin User Level. Please check the calling application .... Client Id '" + aClientId + "'";
      //          log.error(">>>>>>>>>>>>>>>>>>>>>>>>> " + errorMsg, new ItextosException(errorMsg));
                log.error(">>>>>>>>>>>>>>>>>>>>>>>>> " + errorMsg);
                mClientId    = aClientId;
                mAdmin       = aClientId;
                mSuperAdmin  = mClientId.substring(0, 8);
                mClientLevel = ClientLevel.ADMIN_USER;

                break;
            }

            case CLIENT_ID_SUPER_USER_LEVEL_LENGTH:
            {
                final String errMsg = "Passed clientid is at Super User Level. Please check the calling application .... Client Id '" + aClientId + "'";
        //        log.error(">>>>>>>>>>>>>>>>>>>>>>>>> " + errMsg, new ItextosException(errMsg));
               log.error(">>>>>>>>>>>>>>>>>>>>>>>>> " + errMsg);
                
                mClientId    = aClientId;
                mAdmin       = aClientId;
                mSuperAdmin  = aClientId;
                mClientLevel = ClientLevel.SUPER_USER;
                break;
            }

            default:
            {
                final String errMsg = "Passed clientid is not in valid length. Please check the calling application .... Client Id '" + aClientId + "'";
         //       log.error(">>>>>>>>>>>>>>>>>>>>>>>>> " + errMsg, new ItextosRuntimeException(errMsg));
                log.error(">>>>>>>>>>>>>>>>>>>>>>>>> " + errMsg);
                
                mClientId    = null;
                mAdmin       = null;
                mSuperAdmin  = null;
                mClientLevel = null;
            }
        }
        

    }

    public ClientLevel getClientLevel()
    {
        return mClientLevel;
    }

    public String getClientId()
    {
        return mClientId;
    }

    public String getAdmin()
    {
        return mAdmin;
    }

    public String getSuperAdmin()
    {
        return mSuperAdmin;
    }

    @Override
    public String toString()
    {
        return "ItextosClient [mClientId=" + mClientId + ", mAdmin=" + mAdmin + ", mSuperAdmin=" + mSuperAdmin + "]";
    }

}