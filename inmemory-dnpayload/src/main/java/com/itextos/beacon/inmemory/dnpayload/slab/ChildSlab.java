package com.itextos.beacon.inmemory.dnpayload.slab;

public class ChildSlab
{

    private final String childId;
    private final String childStartInSec;
    private final String childEndInSec;
    private final String percentage;

    public ChildSlab(
            String aChildId,
            String aChildStartInSec,
            String aChildEndInSec,
            String aPercentage)
    {
        childId         = aChildId;
        childStartInSec = aChildStartInSec;
        childEndInSec   = aChildEndInSec;
        percentage      = aPercentage;
    }

    public String getChildId()
    {
        return childId;
    }

    public String getChildStartInSec()
    {
        return childStartInSec;
    }

    public String getChildEndInSec()
    {
        return childEndInSec;
    }

    public String getPercentage()
    {
        return percentage;
    }

    @Override
    public String toString()
    {
        return "ChildSlab [childId=" + childId + ", childStartInSec=" + childStartInSec + ", childEndInSec=" + childEndInSec + ", percentage=" + percentage + "]";
    }

}