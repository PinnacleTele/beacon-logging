package com.itextos.beacon.platform.kannelstatusupdater.xmlparser;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(
        name = "sent")
public class SmsSent
{

    @XmlElement(
            name = "total")
    public String total;

    @XmlElement(
            name = "queued")
    public String queued;

}
