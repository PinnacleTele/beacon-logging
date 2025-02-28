package com.itextos.beacon.inmemory.msgutil.cache;

public class CarrierCircle
{

    private final String              mCarrier;
    private final String              mCircle;

    public static final CarrierCircle DEFAULT_CARRIER_CIRCLE = new CarrierCircle("Others", "Others");

    public CarrierCircle(
            String aCarrier,
            String aCircle)
    {
        super();
        mCarrier = aCarrier;
        mCircle  = aCircle;
    }

    public String getCarrier()
    {
        return mCarrier;
    }

    public String getCircle()
    {
        return mCircle;
    }

    @Override
    public String toString()
    {
        return "CarrierCircle [mCarrier=" + mCarrier + ", mCircle=" + mCircle + "]";
    }

}