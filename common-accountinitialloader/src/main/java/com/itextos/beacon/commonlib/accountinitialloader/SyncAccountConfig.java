package com.itextos.beacon.commonlib.accountinitialloader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.accountsync.AccountLoader;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;

public class SyncAccountConfig
{

    private static final Log log = LogFactory.getLog(SyncAccountConfig.class);

    public static void main(
            String[] args)
    {
        if (log.isInfoEnabled())
            log.info("Account sync starting");

        try
        {
            AccountLoader.loadAllAccountData();

            if (log.isInfoEnabled())
                log.info("Account Data loading completed.");
        }
        catch (final ItextosException e)
        {
            log.error("Exception while loading the account data.", e);
        }
        if (log.isInfoEnabled())
            log.info("Process Completed.");
    }

}
