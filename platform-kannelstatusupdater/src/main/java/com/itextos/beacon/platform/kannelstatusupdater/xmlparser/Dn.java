package com.itextos.beacon.platform.kannelstatusupdater.xmlparser;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(
        name = "dlr")
public class Dn
{

    @XmlElementRef
    DnReceived    dnreceived;

    @XmlElement(
            name = "inbound")
    public String inbound;

    @XmlElement(
            name = "queued")
    public String queued;

}