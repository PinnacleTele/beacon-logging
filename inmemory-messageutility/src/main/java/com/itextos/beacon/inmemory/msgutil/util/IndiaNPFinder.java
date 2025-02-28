package com.itextos.beacon.inmemory.msgutil.util;

import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.inmemory.msgutil.cache.CarrierCircle;
import com.itextos.beacon.inmemory.msgutil.cache.CarrierCircles;

public class IndiaNPFinder
{

    private IndiaNPFinder()
    {}

 

    public static CarrierCircle getCarrierCircle(
            String aMNumberSeries)
    {
        final CarrierCircles lCarrierCircle = (CarrierCircles) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CARRIER_CIRCLE);
        return lCarrierCircle.getCarrierCircle(aMNumberSeries);
    }

    public static CarrierCircle getCarrierCircle(
            String aMNumberSeries,
            boolean aReturnDefault)
    {
        final CarrierCircles lCarrierCircle = (CarrierCircles) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.CARRIER_CIRCLE);
        return lCarrierCircle.getCarrierCircle(aMNumberSeries, aReturnDefault);
    }

}