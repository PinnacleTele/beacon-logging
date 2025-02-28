package com.itextos.beacon.platform.messagetool;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.utility.CommonUtility;

abstract class CharacterAsciiMapping
{

    private CharacterAsciiMapping()
    {}

    private static final int               INVALID_ENTRY = -1;
    private static PropertiesConfiguration gsmproperties;
    private static PropertiesConfiguration isoproperties;

    static
    {
        gsmproperties = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.GSM_REPLACE_PROPERTIES, true);
        isoproperties = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.ISO_REPLACE_PROPERTIES, true);
    }

    static int getMappedByteGsm(
            int aValue)
    {
        final int returnValue = CommonUtility.getInteger(gsmproperties.getString(Integer.toString(aValue)), INVALID_ENTRY);
        return returnValue == INVALID_ENTRY ? aValue : returnValue;
    }

    static int getMappedByteIso(
            int aValue)
    {
        final int returnValue = CommonUtility.getInteger(isoproperties.getString(Integer.toString(aValue)), INVALID_ENTRY);
        return returnValue == INVALID_ENTRY ? aValue : returnValue;
    }

}