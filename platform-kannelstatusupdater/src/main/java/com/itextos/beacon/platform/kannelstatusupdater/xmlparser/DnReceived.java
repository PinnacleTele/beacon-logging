package com.itextos.beacon.platform.kannelstatusupdater.xmlparser;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(
        name = "received")
public class DnReceived
{

    @XmlElement(
            name = "total")
    public String total;

}
