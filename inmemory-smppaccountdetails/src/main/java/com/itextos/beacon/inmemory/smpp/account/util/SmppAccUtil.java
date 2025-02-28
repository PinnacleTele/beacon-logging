package com.itextos.beacon.inmemory.smpp.account.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.smpp.account.SmppAccInfo;
import com.itextos.beacon.inmemory.smpp.account.SmppAccountInfo;

public class SmppAccUtil
{

    private static final Log log = LogFactory.getLog(SmppAccUtil.class);

    private SmppAccUtil()
    {}

    public static SmppAccInfo getSmppAccountInfo(
            String aClientId)
    {
        final SmppAccountInfo lSmppAccountInfo = (SmppAccountInfo) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.SMPP_ACCOUNT_INFO);
        return lSmppAccountInfo.getSmppAccountInfo(aClientId);
    }

}
