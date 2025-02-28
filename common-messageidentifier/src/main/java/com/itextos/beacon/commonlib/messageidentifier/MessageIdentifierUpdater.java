package com.itextos.beacon.commonlib.messageidentifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class MessageIdentifierUpdater
        implements
        Runnable
{

    private static final Log             log          = LogFactory.getLog(MessageIdentifierUpdater.class);

    private final RedisAppInstnaceIDPool mAppInstanceSource;
    private final long                   mUpdateSleepTimeinMillis;
    private boolean                      mCanContinue = true;

    MessageIdentifierUpdater(
            final RedisAppInstnaceIDPool aAppInstanceSource)
    {
        mAppInstanceSource       = aAppInstanceSource;
        mUpdateSleepTimeinMillis = MessageIdentifierProperties.getInstance().getStatusUpdateSeconds();
    }

    @Override
    public void run()
    {

        while (mCanContinue)
        {
            updateStatus();

            if (log.isDebugEnabled())
                log.debug("Going to sleep for the " + mUpdateSleepTimeinMillis + " seconds.");

      
            try
            {
                Thread.sleep(mUpdateSleepTimeinMillis);
            }
            catch (final Exception e)
            {
                // ignore
            }
          
        }
    }

    private void updateStatus()
    {

        if (mAppInstanceSource.getCurrentAppInstanceID() == null)
        {
            log.error("No need to update the as the Instance ID is null");
            return;
        }

        if (log.isDebugEnabled())
            log.debug("Update the last used time for the interface type '" + mAppInstanceSource.getInterfaceType() + "' and AppInstanceID : '" + mAppInstanceSource.getCurrentAppInstanceID() + "'");

        mAppInstanceSource.updateLastUsedTime();
    }

    void stopMe()
    {
        mCanContinue = false;
        Thread.currentThread().interrupt();
    }

}
