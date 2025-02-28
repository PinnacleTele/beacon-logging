package com.itextos.beacon.platform.kannelstatusupdater.xmlparser;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(
        name = "sms")
public class Sms
{

    @XmlElement(
            name = "storesize")
    public String storesize;

    @XmlElement(
            name = "inbound")
    public String inbound;

    @XmlElement(
            name = "outbound")
    public String outbound;

    @XmlElementRef
    SmsSent       smssent;

}