package com.itextos.beacon.platform.kannelstatusupdater.xmlparser;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(
        name = "sms")
public class SmsBindWise
{

    @XmlElement(
            name = "sent")
    public String sms;

}