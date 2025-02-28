package com.itextos.beacon.platform.walletprepaidmigration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

public class Migrate
{

    private static final Log log = LogFactory.getLog(Migrate.class);

    public static void main(
            String[] args) throws ItextosRuntimeException
    {
        Process.check();
        final Process process = new Process();
        process.readBalanceCreditFromCsvFile();
        process.getAccountInfo();

        if (process.checkForCountMismatch())
        {
            process.getDataFromRedis();
            process.createApprovalFile();

            final boolean checkApproval = process.checkForApproval();

            if (checkApproval)
            {
                process.updateRedisWalletAmount();
                process.createFinalFile();
                System.out.println("Process Completed");
            }
            else
            {
                log.error("User didn't approve for the wallet update. Exiting application");
                System.out.println("Process terminated.");
            }
        }
        else
        {
            log.error("Due to the count mismatch user dont want to continue the process. Exiting application");
            System.out.println("Process terminated.");
        }
    }

}
