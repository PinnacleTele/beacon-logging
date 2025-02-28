package com.itextos.beacon.httpclienthandover.data;

import java.util.List;

public class ClientHandoverMaster
{

    private final long                       handoverId;
    private final int                        sequenceNo;
    private final String                     primaryUrl;
    private final String                     secondaryUrl;
    private final boolean                    isSecured;
    private final String                     certificateFilePath;
    private final String                     certificatePasspharse;
    private final HttpMethod                 httpMethod;
    private final String                     handoverTemplate;
    private final String                     bodyHeader;
    private final String                     bodyFooter;
    private final String                     batchBodyDelimiter;
    private final int                        conWaitTimeoutMills;
    private final int                        readTimeoutMills;
    private List<ClientHandoverHeaderParams> clientHandoverHeaderParams;
    private List<ClientHandoverParams>       clientHandoverParams;

    public ClientHandoverMaster(
            long aHandoverId,
            int aSequenceNo,
            String aPrimaryUrl,
            String aSecondaryUrl,
            boolean aIsSecured,
            String aCertificateFilePath,
            String aCertificatePasspharse,
            HttpMethod aHttpMethod,
            String aHandoverTemplate,
            String aBodyHeader,
            String aBodyFooter,
            String aBatchBodyDelimiter,
            int aConWaitTimeoutMills,
            int aReadTimeoutMills)
    {
        super();
        handoverId            = aHandoverId;
        sequenceNo            = aSequenceNo;
        primaryUrl            = aPrimaryUrl;
        secondaryUrl          = aSecondaryUrl;
        isSecured             = aIsSecured;
        certificateFilePath   = aCertificateFilePath;
        certificatePasspharse = aCertificatePasspharse;
        httpMethod            = aHttpMethod;
        handoverTemplate      = aHandoverTemplate;
        bodyHeader            = aBodyHeader;
        bodyFooter            = aBodyFooter;
        batchBodyDelimiter    = aBatchBodyDelimiter;
        conWaitTimeoutMills   = aConWaitTimeoutMills;
        readTimeoutMills      = aReadTimeoutMills;
    }

    public List<ClientHandoverHeaderParams> getClientHandoverHeaderParams()
    {
        return clientHandoverHeaderParams;
    }

    public void setClientHandoverHeaderParams(
            List<ClientHandoverHeaderParams> aClientHandoverHeaderParams)
    {
        clientHandoverHeaderParams = aClientHandoverHeaderParams;
    }

    public List<ClientHandoverParams> getClientHandoverParams()
    {
        return clientHandoverParams;
    }

    public void setClientHandoverParams(
            List<ClientHandoverParams> aClientHandoverParams)
    {
        clientHandoverParams = aClientHandoverParams;
    }

    public long getHandoverId()
    {
        return handoverId;
    }

    public int getSequenceNo()
    {
        return sequenceNo;
    }

    public String getPrimaryUrl()
    {
        return primaryUrl;
    }

    public String getSecondaryUrl()
    {
        return secondaryUrl;
    }

    public boolean isSecured()
    {
        return isSecured;
    }

    public String getCertificateFilePath()
    {
        return certificateFilePath;
    }

    public String getCertificatePasspharse()
    {
        return certificatePasspharse;
    }

    public HttpMethod getHttpMethod()
    {
        return httpMethod;
    }

    public String getHandoverTemplate()
    {
        return handoverTemplate;
    }

    public String getBodyHeader()
    {
        return bodyHeader;
    }

    public String getBodyFooter()
    {
        return bodyFooter;
    }

    public String getBatchBodyDelimiter()
    {
        return batchBodyDelimiter;
    }

    public int getConWaitTimeoutMills()
    {
        return conWaitTimeoutMills;
    }

    public int getReadTimeoutMills()
    {
        return readTimeoutMills;
    }

    @Override
    public String toString()
    {
        return "ClientHandoverMaster [handoverId=" + handoverId + ", sequenceNo=" + sequenceNo + ", primaryUrl=" + primaryUrl + ", secondaryUrl=" + secondaryUrl + ", isSecured=" + isSecured
                + ", certificateFilePath=" + certificateFilePath + ", certificatePasspharse=" + certificatePasspharse + ", httpMethod=" + httpMethod + ", handoverTemplate=" + handoverTemplate
                + ", bodyHeader=" + bodyHeader + ", bodyFooter=" + bodyFooter + ", batchBodyDelimiter=" + batchBodyDelimiter + ", conWaitTimeoutMills=" + conWaitTimeoutMills + ", readTimeoutMills="
                + readTimeoutMills + ", clientHandoverHeaderParams=" + clientHandoverHeaderParams + ", clientHandoverParams=" + clientHandoverParams + "]";
    }

}