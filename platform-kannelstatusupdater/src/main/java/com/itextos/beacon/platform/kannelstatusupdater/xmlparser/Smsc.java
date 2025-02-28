package com.itextos.beacon.platform.kannelstatusupdater.xmlparser;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(
        name = "smsc")
public class Smsc
{

    @XmlElement(
            name = "id")
    public String id;

    @XmlElement(
            name = "status")
    public String status;

    @XmlElement(
            name = "failed")
    public String failed;

    @XmlElement(
            name = "queued")
    public String queued;

    @XmlElement(
            name = "name")
    public String name;

    @XmlElementRef
    DnBindWise    dn;

    @XmlElementRef
    SmsBindWise   sms;

}