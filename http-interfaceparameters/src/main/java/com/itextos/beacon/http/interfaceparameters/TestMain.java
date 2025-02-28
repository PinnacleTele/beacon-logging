package com.itextos.beacon.http.interfaceparameters;

import com.itextos.beacon.commonlib.constants.InterfaceType;

public class TestMain
{

    public static void main(
            String[] args)
    {
        final String                   clientId     = "6000000200000000";
        final InterfaceType            type         = InterfaceType.HTTP_JAPI;
        final InterfaceParameter       parameter    = InterfaceParameter.MOBILE_NUMBER;
        final InterfaceParameterLoader ints         = InterfaceParameterLoader.getInstance();
        final String                   lParamterKey = ints.getParamterKey(clientId, type, parameter);
        System.out.println(lParamterKey);
    }

}