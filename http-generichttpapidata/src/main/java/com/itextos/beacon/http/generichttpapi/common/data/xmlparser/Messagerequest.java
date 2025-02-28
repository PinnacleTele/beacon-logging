package com.itextos.beacon.http.generichttpapi.common.data.xmlparser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="accesskey" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="encrypt" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="batchno" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="schedule_time" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="messages" maxOccurs="unbounded"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="msg" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="destination"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="dest" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="header" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="msgtype" minOccurs="0"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                         &lt;enumeration value="FL"/&gt;
 *                         &lt;enumeration value="PM"/&gt;
 *                         &lt;enumeration value="UC"/&gt;
 *                         &lt;enumeration value="BM"/&gt;
 *                         &lt;enumeration value="AD"/&gt;
 *                         &lt;enumeration value="FU"/&gt;
 *                         &lt;enumeration value="SP"/&gt;
 *                       &lt;/restriction&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="dltentityid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="dlttemplateid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="dcs" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="udhi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="dlr_req" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="expiry" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="app_country" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="country_cd" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="template_id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="template_values" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="values" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="urltrack" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="cust_ref" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="url_shortner" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="msgtag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="param1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="param2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="param3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="param4" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="param5" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="param6" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="param7" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="param8" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="param9" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="param10" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder =
        { "accesskey", "encrypt", "batchno", "scheduleTime", "messages" })
@XmlRootElement(
        name = "messagerequest")
public class Messagerequest
        implements
        Serializable
{

    private static final long               serialVersionUID = -6609107787028682352L;

    @XmlElement(
            required = true)
    protected String                        accesskey;
    protected String                        encrypt;
    protected String                        batchno;
    @XmlElement(
            name = "schedule_time")
    protected String                        scheduleTime;
    @XmlElement(
            required = true)
    protected List<Messagerequest.Messages> messages;
    @XmlAttribute(
            name = "version")
    protected String                        version;

    /**
     * Gets the value of the accesskey property.
     *
     * @return
     *         possible object is
     *         {@link String }
     */
    public String getAccesskey()
    {
        return accesskey;
    }

    /**
     * Sets the value of the accesskey property.
     *
     * @param value
     *              allowed object is
     *              {@link String }
     */
    public void setAccesskey(
            String value)
    {
        this.accesskey = value;
    }

    /**
     * Gets the value of the encrypt property.
     *
     * @return
     *         possible object is
     *         {@link String }
     */
    public String getEncrypt()
    {
        return encrypt;
    }

    /**
     * Sets the value of the encrypt property.
     *
     * @param value
     *              allowed object is
     *              {@link String }
     */
    public void setEncrypt(
            String value)
    {
        this.encrypt = value;
    }

    /**
     * Gets the value of the batchno property.
     *
     * @return
     *         possible object is
     *         {@link String }
     */
    public String getBatchno()
    {
        return batchno;
    }

    /**
     * Sets the value of the batchno property.
     *
     * @param value
     *              allowed object is
     *              {@link String }
     */
    public void setBatchno(
            String value)
    {
        this.batchno = value;
    }

    /**
     * Gets the value of the scheduleTime property.
     *
     * @return
     *         possible object is
     *         {@link String }
     */
    public String getScheduleTime()
    {
        return scheduleTime;
    }

    /**
     * Sets the value of the scheduleTime property.
     *
     * @param value
     *              allowed object is
     *              {@link String }
     */
    public void setScheduleTime(
            String value)
    {
        this.scheduleTime = value;
    }

    /**
     * Gets the value of the messages property.
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messages property.
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getMessages().add(newItem);
     * </pre>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Messagerequest.Messages }
     */
    public List<Messagerequest.Messages> getMessages()
    {
        if (messages == null)
            messages = new ArrayList<>();
        return this.messages;
    }

    /**
     * Gets the value of the version property.
     *
     * @return
     *         possible object is
     *         {@link String }
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value
     *              allowed object is
     *              {@link String }
     */
    public void setVersion(
            String value)
    {
        this.version = value;
    }

    /**
     * <p>
     * Java class for anonymous complex type.
     * <p>
     * The following schema fragment specifies the expected content contained within
     * this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="msg" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="destination"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="dest" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="header" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="msgtype" minOccurs="0"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *               &lt;enumeration value="FL"/&gt;
     *               &lt;enumeration value="PM"/&gt;
     *               &lt;enumeration value="UC"/&gt;
     *               &lt;enumeration value="BM"/&gt;
     *               &lt;enumeration value="AD"/&gt;
     *               &lt;enumeration value="FU"/&gt;
     *               &lt;enumeration value="SP"/&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="dltentityid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="dlttemplateid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="dcs" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="udhi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="dlr_req" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="expiry" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="app_country" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="country_cd" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="template_id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="template_values" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="values" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="urltrack" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="cust_ref" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="url_shortner" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="msgtag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="param1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="param2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="param3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="param4" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="param5" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="param6" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="param7" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="param8" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="param9" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="param10" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(
            name = "",
            propOrder =
            { "msg", "destination", "header", "msgtype", "dltentityid", "dlttemplateid", "dcs", "udhi", "port", "dlrReq", "expiry", "appCountry", "countryCd", "templateId", "templateValues",
                    "urltrack", "custRef", "urlShortner", "msgtag", "param1", "param2", "param3", "param4", "param5", "param6", "param7", "param8", "param9", "param10" })
    public static class Messages
            implements
            Serializable
    {

        private static final long                        serialVersionUID = 2333602026116331948L;

        @XmlElement(
                required = true)
        protected String                                 msg;
        @XmlElement(
                required = true)
        protected Messagerequest.Messages.Destination    destination;
        @XmlElement(
                required = true)
        protected String                                 header;
        protected String                                 msgtype;
        protected String                                 dltentityid;
        protected String                                 dlttemplateid;
        protected String                                 dlttelemarketerid;

        protected String                                 dcs;
        protected String                                 udhi;
        protected String                                 port;
        @XmlElement(
                name = "dlr_req")
        protected String                                 dlrReq;
        protected String                                 expiry;
        @XmlElement(
                name = "app_country")
        protected String                                 appCountry;
        @XmlElement(
                name = "country_cd")
        protected String                                 countryCd;
        @XmlElement(
                name = "template_id")
        protected String                                 templateId;
        @XmlElement(
                name = "template_values")
        protected Messagerequest.Messages.TemplateValues templateValues;
        protected String                                 urltrack;
        @XmlElement(
                name = "cust_ref")
        protected String                                 custRef;
        @XmlElement(
                name = "url_shortner")
        protected String                                 urlShortner;
        protected String                                 msgtag;
        protected String                                 param1;
        protected String                                 param2;
        protected String                                 param3;
        protected String                                 param4;
        protected String                                 param5;
        protected String                                 param6;
        protected String                                 param7;
        protected String                                 param8;
        protected String                                 param9;
        protected String                                 param10;

        /**
         * Gets the value of the msg property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getMsg()
        {
            return msg;
        }

        /**
         * Sets the value of the msg property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setMsg(
                String value)
        {
            this.msg = value;
        }

        /**
         * Gets the value of the destination property.
         *
         * @return
         *         possible object is
         *         {@link Messagerequest.Messages.Destination }
         */
        public Messagerequest.Messages.Destination getDestination()
        {
            return destination;
        }

        /**
         * Sets the value of the destination property.
         *
         * @param value
         *              allowed object is
         *              {@link Messagerequest.Messages.Destination }
         */
        public void setDestination(
                Messagerequest.Messages.Destination value)
        {
            this.destination = value;
        }

        /**
         * Gets the value of the header property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getHeader()
        {
            return header;
        }

        /**
         * Sets the value of the header property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setHeader(
                String value)
        {
            this.header = value;
        }

        /**
         * Gets the value of the msgtype property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getMsgtype()
        {
            return msgtype;
        }

        /**
         * Sets the value of the msgtype property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setMsgtype(
                String value)
        {
            this.msgtype = value;
        }

        /**
         * Gets the value of the dltentityid property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getDltentityid()
        {
            return dltentityid;
        }

        /**
         * Sets the value of the dltentityid property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setDltentityid(
                String value)
        {
            this.dltentityid = value;
        }

        /**
         * Gets the value of the dlttemplateid property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getDlttemplateid()
        {
            return dlttemplateid;
        }
        
        
        /**
         * Sets the value of the dlttemplateid property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setDlttemplateid(
                String value)
        {
            this.dlttemplateid = value;
        }
        
        

        /**
         * Gets the value of the dlttemplateid property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getDlttelemarketerid()
        {
            return dlttelemarketerid;
        }

        
        /**
         * Sets the value of the dlttemplateid property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setDlttelemarketerid(
                String value)
        {
            this.dlttelemarketerid = value;
        }
       

   
       
        /**
         * Gets the value of the dcs property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getDcs()
        {
            return dcs;
        }

        /**
         * Sets the value of the dcs property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setDcs(
                String value)
        {
            this.dcs = value;
        }

        /**
         * Gets the value of the udhi property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getUdhi()
        {
            return udhi;
        }

        /**
         * Sets the value of the udhi property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setUdhi(
                String value)
        {
            this.udhi = value;
        }

        /**
         * Gets the value of the port property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getPort()
        {
            return port;
        }

        /**
         * Sets the value of the port property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setPort(
                String value)
        {
            this.port = value;
        }

        /**
         * Gets the value of the dlrReq property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getDlrReq()
        {
            return dlrReq;
        }

        /**
         * Sets the value of the dlrReq property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setDlrReq(
                String value)
        {
            this.dlrReq = value;
        }

        /**
         * Gets the value of the expiry property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getExpiry()
        {
            return expiry;
        }

        /**
         * Sets the value of the expiry property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setExpiry(
                String value)
        {
            this.expiry = value;
        }

        /**
         * Gets the value of the appCountry property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getAppCountry()
        {
            return appCountry;
        }

        /**
         * Sets the value of the appCountry property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setAppCountry(
                String value)
        {
            this.appCountry = value;
        }

        /**
         * Gets the value of the countryCd property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getCountryCd()
        {
            return countryCd;
        }

        /**
         * Sets the value of the countryCd property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setCountryCd(
                String value)
        {
            this.countryCd = value;
        }

        /**
         * Gets the value of the templateId property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getTemplateId()
        {
            return templateId;
        }

        /**
         * Sets the value of the templateId property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setTemplateId(
                String value)
        {
            this.templateId = value;
        }

        /**
         * Gets the value of the templateValues property.
         *
         * @return
         *         possible object is
         *         {@link Messagerequest.Messages.TemplateValues }
         */
        public Messagerequest.Messages.TemplateValues getTemplateValues()
        {
            return templateValues;
        }

        /**
         * Sets the value of the templateValues property.
         *
         * @param value
         *              allowed object is
         *              {@link Messagerequest.Messages.TemplateValues }
         */
        public void setTemplateValues(
                Messagerequest.Messages.TemplateValues value)
        {
            this.templateValues = value;
        }

        /**
         * Gets the value of the urltrack property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getUrltrack()
        {
            return urltrack;
        }

        /**
         * Sets the value of the urltrack property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setUrltrack(
                String value)
        {
            this.urltrack = value;
        }

        /**
         * Gets the value of the custRef property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getCustRef()
        {
            return custRef;
        }

        /**
         * Sets the value of the custRef property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setCustRef(
                String value)
        {
            this.custRef = value;
        }

        /**
         * Gets the value of the urlShortner property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getUrlShortner()
        {
            return urlShortner;
        }

        /**
         * Sets the value of the urlShortner property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setUrlShortner(
                String value)
        {
            this.urlShortner = value;
        }

        /**
         * Gets the value of the msgtag property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getMsgtag()
        {
            return msgtag;
        }

        /**
         * Sets the value of the msgtag property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setMsgtag(
                String value)
        {
            this.msgtag = value;
        }

        /**
         * Gets the value of the param1 property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getParam1()
        {
            return param1;
        }

        /**
         * Sets the value of the param1 property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setParam1(
                String value)
        {
            this.param1 = value;
        }

        /**
         * Gets the value of the param2 property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getParam2()
        {
            return param2;
        }

        /**
         * Sets the value of the param2 property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setParam2(
                String value)
        {
            this.param2 = value;
        }

        /**
         * Gets the value of the param3 property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getParam3()
        {
            return param3;
        }

        /**
         * Sets the value of the param3 property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setParam3(
                String value)
        {
            this.param3 = value;
        }

        /**
         * Gets the value of the param4 property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getParam4()
        {
            return param4;
        }

        /**
         * Sets the value of the param4 property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setParam4(
                String value)
        {
            this.param4 = value;
        }

        /**
         * Gets the value of the param5 property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getParam5()
        {
            return param5;
        }

        /**
         * Sets the value of the param5 property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setParam5(
                String value)
        {
            this.param5 = value;
        }

        /**
         * Gets the value of the param6 property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getParam6()
        {
            return param6;
        }

        /**
         * Sets the value of the param6 property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setParam6(
                String value)
        {
            this.param6 = value;
        }

        /**
         * Gets the value of the param7 property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getParam7()
        {
            return param7;
        }

        /**
         * Sets the value of the param7 property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setParam7(
                String value)
        {
            this.param7 = value;
        }

        /**
         * Gets the value of the param8 property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getParam8()
        {
            return param8;
        }

        /**
         * Sets the value of the param8 property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setParam8(
                String value)
        {
            this.param8 = value;
        }

        /**
         * Gets the value of the param9 property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getParam9()
        {
            return param9;
        }

        /**
         * Sets the value of the param9 property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setParam9(
                String value)
        {
            this.param9 = value;
        }

        /**
         * Gets the value of the param10 property.
         *
         * @return
         *         possible object is
         *         {@link String }
         */
        public String getParam10()
        {
            return param10;
        }

        /**
         * Sets the value of the param10 property.
         *
         * @param value
         *              allowed object is
         *              {@link String }
         */
        public void setParam10(
                String value)
        {
            this.param10 = value;
        }

        /**
         * <p>
         * Java class for anonymous complex type.
         * <p>
         * The following schema fragment specifies the expected content contained within
         * this class.
         *
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="dest" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(
                name = "",
                propOrder =
                { "dest" })
        public static class Destination
                implements
                Serializable
        {

            private static final long serialVersionUID = 7408071373374836355L;

            @XmlElement(
                    required = true)
            protected List<String>    dest;

            /**
             * Gets the value of the dest property.
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the dest property.
             * <p>
             * For example, to add a new item, do as follows:
             *
             * <pre>
             * getDest().add(newItem);
             * </pre>
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link String }
             */
            public List<String> getDest()
            {
                if (dest == null)
                    dest = new ArrayList<>();
                return this.dest;
            }

        }

        /**
         * <p>
         * Java class for anonymous complex type.
         * <p>
         * The following schema fragment specifies the expected content contained within
         * this class.
         *
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="values" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
         *       &lt;/sequence&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(
                name = "",
                propOrder =
                { "values" })
        public static class TemplateValues
                implements
                Serializable
        {

            private static final long serialVersionUID = -7609679355372487510L;

            @XmlElement(
                    required = true)
            protected List<String>    values;

            /**
             * Gets the value of the values property.
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the values property.
             * <p>
             * For example, to add a new item, do as follows:
             *
             * <pre>
             * getValues().add(newItem);
             * </pre>
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link String }
             */
            public List<String> getValues()
            {
                if (values == null)
                    values = new ArrayList<>();
                return this.values;
            }

        }

    }

}
