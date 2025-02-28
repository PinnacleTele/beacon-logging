package com.itextos.beacon.smpp.utils.enums;

import com.itextos.beacon.commonlib.constants.ItextosEnum;

public enum SmppEventType
        implements
        ItextosEnum
{

    SUBMIT_SM_REQ("submitsm_req"),
    SUBMIT_SM_RES("submitsm_res"),
    BIND_REQ("bind_req"),
    BIND_RES("nbind_res"),
    UNBIND_REQ("unbind_req"),
    UNBIND_RES("unbind_res"),
    ENQUIRY_REQ("enquiry_req"),
    ENQUIRY_RES("enquiry_res"),
    DELIVERY_SM_REQ("deliversm_req"),
    DELIVERY_SM_RES("deliversm_res"),

    ;

    private String mType;

    SmppEventType(
            String aType)
    {
        mType = aType;
    }

    @Override
    public String getKey()
    {
        return mType;
    }

}