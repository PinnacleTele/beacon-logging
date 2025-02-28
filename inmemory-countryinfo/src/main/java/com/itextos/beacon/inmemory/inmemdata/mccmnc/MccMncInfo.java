package com.itextos.beacon.inmemory.inmemdata.mccmnc;


public class MccMncInfo
{

 
    private final String    mcc;
    private final String    mnc;
    private final String    prefix;


    public MccMncInfo(
        
    		String mcc,
    		String mnc,
    		String prefix)
    {
        super();
        this.mcc       = mcc;
        this.mnc       = mnc;
        this.prefix    = prefix;
    }

   

   
    public String getMcc()
    {
        return mcc;
    }

    public String getMnc()
    {
        return mnc;
    }

   
    public String getPrefix()
    {
        return prefix;
    }
    
	@Override
    public String toString()
    {
        return "MccMncInfo [mcc=" + mcc + ", mnc=" + mnc + ", prefix=" + prefix+"]";
    }

}