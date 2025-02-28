package com.itextos.beacon.inmemory.templates.pojo;

public class DLTMsgTemplateObj
{

    private final String mTemplateId;
    private final String mEntityId;
    private final String mTemplateType;
    private final String mTemplate;

    public DLTMsgTemplateObj(
            String aEntityId,
            String aTemplateId,
            String aTemplateType,
            String aTemplate)
    {
        mTemplateId   = aTemplateId;
        mEntityId     = aEntityId;
        mTemplateType = aTemplateType;
        mTemplate     = aTemplate;
    }

    public String getTemplateId()
    {
        return mTemplateId;
    }

    public String getTemplate()
    {
        return mTemplate;
    }

    public String getTemplateType()
    {
        return mTemplateType;
    }

    public String getEntityId()
    {
        return mEntityId;
    }

    @Override
    public String toString()
    {
        return "DLTMsgTemplateObj [mTemplateId=" + mTemplateId + ", mEntityId=" + mEntityId + ", mTemplateType=" + mTemplateType + ", mTemplate=" + mTemplate + "]";
    }

}