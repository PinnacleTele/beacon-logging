package com.itextos.beacon.http.interfaceutil.uiftp;

import com.itextos.beacon.commonlib.constants.MiddlewareConstant;

public enum InterfaceConstant
{

    APP_INSTANCE_ID(MiddlewareConstant.MW_APP_INSTANCE_ID),
    FILE_ID(MiddlewareConstant.MW_FILE_ID),
    BASE_MESSAGE_ID(MiddlewareConstant.MW_BASE_MESSAGE_ID),
    CLIENT_ID(MiddlewareConstant.MW_CLIENT_ID),
    CLIENT_IP(MiddlewareConstant.MW_CLIENT_SOURCE_IP),
    MOBILE_NUMBER(MiddlewareConstant.MW_MOBILE_NUMBER),
    HEADER(MiddlewareConstant.MW_HEADER),
    CLIENT_MESSAGE_ID(MiddlewareConstant.MW_CLIENT_MESSAGE_ID),
    DLT_ENTITY_ID(MiddlewareConstant.MW_DLT_ENTITY_ID),
    DLT_TEMPLATE_ID(MiddlewareConstant.MW_DLT_TEMPLATE_ID),
    DLT_TMA_ID(MiddlewareConstant.MW_DLT_TMA_ID),
    DLR_REQURIED(MiddlewareConstant.MW_DLR_REQ_FROM_CLI),
    MAX_MESSAGE_VALIDITY_SEC(MiddlewareConstant.MW_MAX_VALIDITY_IN_SEC),
    RECEIVED_TIME(MiddlewareConstant.MW_MSG_RECEIVED_TIME),
    RECEIVED_DATE(MiddlewareConstant.MW_MSG_RECEIVED_DATE),
    MESSAGE(MiddlewareConstant.MW_MSG),
    IS_HEX_MSG(MiddlewareConstant.MW_IS_HEX_MSG),
    MESSAGE_TAG(MiddlewareConstant.MW_MSG_TAG),
    MESSAGE_CLASS(MiddlewareConstant.MW_MSG_CLASS),
    UI_DUP_CHECK_ENABLE(MiddlewareConstant.MW_UI_DUP_CHK),
    CAMPAIGN_ID(MiddlewareConstant.MW_CAMP_ID),
    CAMPAIGN_NAME(MiddlewareConstant.MW_CAMP_NAME),
    UI_VL_SHORT_REQ(MiddlewareConstant.MW_UI_VL_SHORTNER_REQ),
    MSG_TAG1(MiddlewareConstant.MW_MSG_TAG1),
    MSG_TAG2(MiddlewareConstant.MW_MSG_TAG2),
    MSG_TAG3(MiddlewareConstant.MW_MSG_TAG3),
    MSG_TAG4(MiddlewareConstant.MW_MSG_TAG4),
    MSG_TAG5(MiddlewareConstant.MW_MSG_TAG5),
    PRIORITY(MiddlewareConstant.MW_SMS_PRIORITY),
    INTL_HEADER(MiddlewareConstant.MW_INTL_HEADER),

    ACCOUNT_INFO(null);

    private final MiddlewareConstant parent;

    InterfaceConstant(
            MiddlewareConstant aMC)
    {
        parent = aMC;
    }

    public MiddlewareConstant getParent()
    {
        return parent;
    }

}