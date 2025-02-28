package com.itextos.beacon.commonlib.dnddataloader.enums;

public enum DndAction
{

    APPEND_OR_UPDATE("A"),
    DELETE("D"),
    INVALID("I"),
    INVALID_NUMBER("IN");

    private final String operation;

    DndAction(
            String aAction)
    {
        operation = aAction;
    }

    public String getOperation()
    {
        return operation;
    }

}