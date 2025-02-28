package com.itextos.beacon.inmemory.inmemdata.mccmnc;

import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;

public class MCCMNCFinder
{

    private MCCMNCFinder()
    {}

 

    public static MccMncInfo getMccMnc(
            String aMNumberSeries)
    {
        final MccMncCollection lMccMncCollection = (MccMncCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.MCC_MNC);
        return lMccMncCollection==null?null:lMccMncCollection.getMccMncData(aMNumberSeries);
    }

  

}