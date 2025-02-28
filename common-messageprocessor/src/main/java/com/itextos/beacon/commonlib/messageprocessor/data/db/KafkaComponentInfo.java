package com.itextos.beacon.commonlib.messageprocessor.data.db;

import com.itextos.beacon.commonlib.constants.Component;

public class KafkaComponentInfo
{

    private final Component mComponent;
    private final String    mComponentProcessClass;

    public KafkaComponentInfo(
            Component aComponent,
            String aComponentProcessClass)
    {
        super();
        mComponent             = aComponent;
        mComponentProcessClass = aComponentProcessClass;
    }

    public String getKey()
    {
        return mComponent.getKey();
    }

    public Component getComponent()
    {
        return mComponent;
    }

    public String getComponentProcessClass()
    {
        return mComponentProcessClass;
    }

    @Override
    public String toString()
    {
        return "KafkaComponentInfo [mComponent=" + mComponent + ", mComponentProcessClass=" + mComponentProcessClass + "]";
    }

}