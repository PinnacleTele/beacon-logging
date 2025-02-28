package com.itextos.beacon.commonlib.datarefresher.dataobjects;

import java.util.List;
import java.util.Map;

public interface IDataRefresher
{

    void setData(
            Map<DataOperation, List<String>> aValue);

    Map<DataOperation, Integer> process();

    int insertData(
            List<String> aList);

    int updateData(
            List<String> aList);

    int deleteData(
            List<String> aList);

}