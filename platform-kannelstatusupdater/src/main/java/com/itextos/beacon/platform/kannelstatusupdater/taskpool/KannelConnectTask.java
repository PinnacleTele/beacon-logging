package com.itextos.beacon.platform.kannelstatusupdater.taskpool;

import java.util.concurrent.Callable;

import com.itextos.beacon.platform.kannelstatusupdater.beans.KannelStatusInfo;
import com.itextos.beacon.platform.kannelstatusupdater.process.KannelConnector;

public class KannelConnectTask
        implements
        Callable<KannelStatusInfo>
{

    private final String kannelid;
    private final String kannelurl;

    public KannelConnectTask(
            String aKannelid,
            String aKannelURL)
    {
        this.kannelid  = aKannelid;
        this.kannelurl = aKannelURL;
    }

    @Override
    public KannelStatusInfo call()
    {
       // return KannelConnector.getKannelStatus(kannelid, kannelurl);
    	return null;
    }

}