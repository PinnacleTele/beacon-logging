package com.itextos.beacon.platform.topic2table.tablename;

import java.util.Map;

import com.itextos.beacon.commonlib.commondbpool.JndiInfo;

public interface ITablenameFinder
{

    void setInputValues(
            JndiInfo aJndiInfo,
            String aDatabaseName,
            String aTableName,
            Map<String, String> aKeyValuesFromMap);

    void process();

    JndiInfo getJndiInfo();

    String getTableName();

}