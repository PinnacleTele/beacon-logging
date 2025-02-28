package com.itextos.beacon.platform.topic2table.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;

public abstract class ExceptionMessageList
{

    private static final Log               log                  = LogFactory.getLog(ExceptionMessageList.class);

    private static List<String>            individualInsertList = new ArrayList<>();
    private static List<String>            alertAndPushBackList = new ArrayList<>();
    private static List<String>            trimAndInsertList    = new ArrayList<>();
    private static List<String>            pushBackList         = new ArrayList<>();
    private static List<String>            alretAndDropMessage  = new ArrayList<>();
    private static PropertiesConfiguration pc                   = null;

    private static final String            ALERT_AND_PUSH_BACK  = "alert.pushback.list";
    private static final String            TRIM_AND_INSERT      = "trim.insert.list";
    private static final String            INDIVIDUAL_INSERT    = "individual.insert.list";
    private static final String            PUSH_BACK            = "pushback.list";
    private static final String            ALERT_AND_DROP       = "alert.and.drop";

    static
    {
        pc = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.EXCEPTION_CASE_PROPERTIES, true);

        addIndividuallist();
        addAlertAndPushBackMessageList();
        addTrimAndInsertList();
        addPushBackList();
        addAlertAndDropList();
    }

    private ExceptionMessageList()
    {}

    private static void addPushBackList()
    {
        pushBackList = readMessageProperty(pc, PUSH_BACK);
    }

    private static void addTrimAndInsertList()
    {
        trimAndInsertList = readMessageProperty(pc, TRIM_AND_INSERT);
    }

    private static void addAlertAndPushBackMessageList()
    {
        alertAndPushBackList = readMessageProperty(pc, ALERT_AND_PUSH_BACK);
    }

    private static void addIndividuallist()
    {
        individualInsertList = readMessageProperty(pc, INDIVIDUAL_INSERT);
    }

    private static void addAlertAndDropList()
    {
        alretAndDropMessage = readMessageProperty(pc, ALERT_AND_DROP);
    }

    public static List<String> getIndividualInsertList()
    {
        return individualInsertList;
    }

    public static List<String> getAlertAndPushbackList()
    {
        return alertAndPushBackList;
    }

    public static List<String> getTrimAndInsertList()
    {
        return trimAndInsertList;
    }

    public static List<String> getPushBackList()
    {
        return pushBackList;
    }

    public static List<String> getAlertAndDropMessage()
    {
        return alretAndDropMessage;
    }

    private static List<String> readMessageProperty(
            PropertiesConfiguration aPc,
            String aPropKey)
    {
        final String temp = aPc.getString(aPropKey);

        if (log.isDebugEnabled())
            log.debug(new java.util.Date() + " - ReadMessageProperty temp : '" + temp + "' Length : '" + (temp == null ? "null" : temp.length()) + "'");

        if ((temp == null) || (temp.trim().length() == 0))
            return new ArrayList<>(0);

        final List<Object> lList       = aPc.getList(aPropKey, null);

        final List<String> returnValue = new ArrayList<>(lList.size());
        for (final Object o : lList)
            returnValue.add(((String) o).toLowerCase());
        return returnValue;
    }

}