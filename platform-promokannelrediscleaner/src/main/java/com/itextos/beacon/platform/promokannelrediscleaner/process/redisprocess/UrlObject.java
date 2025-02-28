package com.itextos.beacon.platform.promokannelrediscleaner.process.redisprocess;

class UrlObject
{

    private String  dest;
    private String  header;
    private String  operatorMsgId;

    private String  c_id;
    private String  car_ts_format;
    private String  intf_grp_type;
    private String  intf_type;
    private String  intl_msg;
    private String  m_id;
    private String  msg_create_ts;
    private String  msg_type;
    private String  pl_exp;
    private String  pl_rds_id;
    private String  platform_cluster;
    private String  recv_ts;
    private String  rty_atmpt;
    private String  rute_id;
    private String  sms_priority;
    private boolean isValid = false;

    public boolean isValid()
    {
        return isValid;
    }

    public void setValid(
            boolean aIsValid)
    {
        isValid = aIsValid;
    }

    public String getDest()
    {
        return dest;
    }

    public void setDest(
            String aDest)
    {
        dest = aDest;
    }

    public String getHeader()
    {
        return header;
    }

    public void setHeader(
            String aHeader)
    {
        header = aHeader;
    }

    public String getOperatorMsgId()
    {
        return operatorMsgId;
    }

    public void setOperatorMsgId(
            String aOperatorMsgId)
    {
        operatorMsgId = aOperatorMsgId;
    }

    public String getC_id()
    {
        return c_id;
    }

    public void setC_id(
            String aC_id)
    {
        c_id = aC_id;
    }

    public String getCar_ts_format()
    {
        return car_ts_format;
    }

    public void setCar_ts_format(
            String aCar_ts_format)
    {
        car_ts_format = aCar_ts_format;
    }

    public String getIntf_grp_type()
    {
        return intf_grp_type;
    }

    public void setIntf_grp_type(
            String aIntf_grp_type)
    {
        intf_grp_type = aIntf_grp_type;
    }

    public String getIntf_type()
    {
        return intf_type;
    }

    public void setIntf_type(
            String aIntf_type)
    {
        intf_type = aIntf_type;
    }

    public String getIntl_msg()
    {
        return intl_msg;
    }

    public void setIntl_msg(
            String aIntl_msg)
    {
        intl_msg = aIntl_msg;
    }

    public String getM_id()
    {
        return m_id;
    }

    public void setM_id(
            String aM_id)
    {
        m_id = aM_id;
    }

    public String getMsg_create_ts()
    {
        return msg_create_ts;
    }

    public void setMsg_create_ts(
            String aMsg_create_ts)
    {
        msg_create_ts = aMsg_create_ts;
    }

    public String getMsg_type()
    {
        return msg_type;
    }

    public void setMsg_type(
            String aMsg_type)
    {
        msg_type = aMsg_type;
    }

    public String getPl_exp()
    {
        return pl_exp;
    }

    public void setPl_exp(
            String aPl_exp)
    {
        pl_exp = aPl_exp;
    }

    public String getPl_rds_id()
    {
        return pl_rds_id;
    }

    public void setPl_rds_id(
            String aPl_rds_id)
    {
        pl_rds_id = aPl_rds_id;
    }

    public String getPlatform_cluster()
    {
        return platform_cluster;
    }

    public void setPlatform_cluster(
            String aPlatform_cluster)
    {
        platform_cluster = aPlatform_cluster;
    }

    public String getRecv_ts()
    {
        return recv_ts;
    }

    public void setRecv_ts(
            String aRecv_ts)
    {
        recv_ts = aRecv_ts;
    }

    public String getRty_atmpt()
    {
        return rty_atmpt;
    }

    public void setRty_atmpt(
            String aRty_atmpt)
    {
        rty_atmpt = aRty_atmpt;
    }

    public String getRute_id()
    {
        return rute_id;
    }

    public void setRute_id(
            String aRute_id)
    {
        rute_id = aRute_id;
    }

    public String getSms_priority()
    {
        return sms_priority;
    }

    public void setSms_priority(
            String aSms_priority)
    {
        sms_priority = aSms_priority;
    }

    @Override
    public String toString()
    {
        return "UrlObject [dest=" + dest + ", header=" + header + ", operatorMsgId=" + operatorMsgId + ", c_id=" + c_id + ", car_ts_format=" + car_ts_format + ", intf_grp_type=" + intf_grp_type
                + ", intf_type=" + intf_type + ", intl_msg=" + intl_msg + ", m_id=" + m_id + ", msg_create_ts=" + msg_create_ts + ", msg_type=" + msg_type + ", pl_exp=" + pl_exp + ", pl_rds_id="
                + pl_rds_id + ", platform_cluster=" + platform_cluster + ", recv_ts=" + recv_ts + ", rty_atmpt=" + rty_atmpt + ", rute_id=" + rute_id + ", sms_priority=" + sms_priority + ", isValid="
                + isValid + "]";
    }

}