package com.itextos.beacon.httpclienthandover.data;

public class ClientHandoverHeaderParams
{

    private final long   handoverId;
    private final int    sequenceNo;
    private final int    paramSeqNo;
    private final String headerParamName;
    private final String headerParamValue;

    public ClientHandoverHeaderParams(
            long aHandoverId,
            int aSequenceNo,
            int aParamSeqNo,
            String aHeaderParamName,
            String aHeaderParamValue)
    {
        super();
        handoverId       = aHandoverId;
        sequenceNo       = aSequenceNo;
        paramSeqNo       = aParamSeqNo;
        headerParamName  = aHeaderParamName;
        headerParamValue = aHeaderParamValue;
    }

    public long getHandoverId()
    {
        return handoverId;
    }

    public int getSequenceNo()
    {
        return sequenceNo;
    }

    public int getParamSeqNo()
    {
        return paramSeqNo;
    }

    public String getHeaderParamName()
    {
        return headerParamName;
    }

    public String getHeaderParamValue()
    {
        return headerParamValue;
    }

    @Override
    public String toString()
    {
        return "ClientHandoverHeaderParams [handoverId=" + handoverId + ", sequenceNo=" + sequenceNo + ", paramSeqNo=" + paramSeqNo + ", headerParamName=" + headerParamName + ", headerParamValue="
                + headerParamValue + "]";
    }

}