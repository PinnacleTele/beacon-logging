package com.itextos.beacon.commonlib.message;

import java.io.Serializable;

public interface IMessage
        extends
        Serializable,
        Cloneable
{

    String getJsonString();

    String getClientId();

    String getNextComponent();

    void setNextComponent(
            String aNextComponentKey);
    

    String getFromComponent();

    void setFromComponent(
            String aFromComponentKey);
    
    
    String getProcessorComponent();

    void setProcessorComponent(
            String aFromComponentKey);
    
    
    
}