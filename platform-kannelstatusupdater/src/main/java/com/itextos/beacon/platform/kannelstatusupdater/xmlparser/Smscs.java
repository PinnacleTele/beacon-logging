package com.itextos.beacon.platform.kannelstatusupdater.xmlparser;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(
        name = "smscs")
public class Smscs
{

    @XmlElement(
            name = "count")
    public String count;

    @XmlElementRef
    List<Smsc>    smsclist;

}