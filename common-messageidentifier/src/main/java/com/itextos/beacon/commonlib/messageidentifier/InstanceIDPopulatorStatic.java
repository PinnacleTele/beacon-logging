package com.itextos.beacon.commonlib.messageidentifier;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class InstanceIDPopulatorStatic
{

    private static final Log     log       = LogFactory.getLog(InstanceIDPopulatorStatic.class);

    private static final int     MIN_VALUE = 100;
    private static final int     MAX_VALUE = 999;

    private final String         mUserName;
    private final String         mIP;
    private final String         mUserInfo;
    private int                  mStartId  = 0;
    private int                  mEndId    = 0;

    InstanceIDPopulatorStatic(
            String aUserName,
            String aIp
           )
    {
        mUserName = aUserName;
        mIP       = aIp;
        mUserInfo = "IP : '" + mIP + "' User : '" + mUserName + "': ";

        if (log.isInfoEnabled())
            log.info(mUserInfo + "Starting instance ID populator.");
    }

    void process()
    {

        try
        {
            getValidStartEndInstanceIDs();
            final PopulateDataStatic populateData = new PopulateDataStatic(mUserName, mIP, mStartId, mEndId);
            populateData.populate();
        }
        catch (final Exception e)
        {
            log.error("Exception while Populating instance IDs", e);
        }
    }

    private void getValidStartEndInstanceIDs()
            throws IOException
    {
        boolean validInputs = false;

        while (!validInputs)
        {
            System.out.println(MessageIdentifierConstants.NEW_LINE + "Hi " + mUserName + ",");
            System.out.print("Please enter start instance id (" + MIN_VALUE + " - " + MAX_VALUE + "):");
            final String startIdx = "101";
            System.out.print(MessageIdentifierConstants.NEW_LINE + "Please enter end instance id (" + MIN_VALUE + " - " + MAX_VALUE + ")  :");
            final String endIdx = "999";

            if (log.isInfoEnabled())
                log.info(mUserInfo + "Provided start index : '" + startIdx + "' end index : '" + endIdx + "'");

            mStartId    = Utility.getInteger(startIdx, 0);
            mEndId      = Utility.getInteger(endIdx, 0);
            validInputs = true;

            if ((mStartId < MIN_VALUE) || (mStartId > MAX_VALUE))
            {
                final String s = "Provided start instance id is not valid";
                log.error(mUserInfo + s);
                System.err.println(s);
                validInputs = false;
            }

            if ((mEndId < MIN_VALUE) || (mEndId > MAX_VALUE))
            {
                final String s = "Provided end instance id is not valid";
                log.error(mUserInfo + s);
                System.err.println(s);
                validInputs = false;
            }

            if (mStartId > mEndId)
            {
                final String s = "Start instance id cannot be greater than End instance id.";
                log.error(mUserInfo + s);
                System.err.println(s);
                validInputs = false;
            }
        }
    }

}