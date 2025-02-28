package com.itextos.beacon.commonlib.datarefresher.dataobjects;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractDataRefresher
        implements
        IDataRefresher
{

    private static final Log                   log          = LogFactory.getLog(AbstractDataRefresher.class);

    protected Map<DataOperation, List<String>> mModifiedData;
    protected List<String>                     mToBeupdated = new ArrayList<>();

    @Override
    public void setData(
            Map<DataOperation, List<String>> aModifiedData)
    {
        mModifiedData = aModifiedData;
    }

    @Override
    public Map<DataOperation, Integer> process()
    {
        final Map<DataOperation, Integer> returnValue = new EnumMap<>(DataOperation.class);

        for (final Entry<DataOperation, List<String>> entry : mModifiedData.entrySet())
            switch (entry.getKey())
            {
                case DELETE:
                    final int lDeleteData = deleteData(entry.getValue());
                    returnValue.put(DataOperation.DELETE, lDeleteData);
                    break;

                case INSERT:
                    final int lInsertData = insertData(entry.getValue());
                    returnValue.put(DataOperation.INSERT, lInsertData);
                    break;

                case UPDATE:
                    final int lUpdateData = updateData(entry.getValue());
                    returnValue.put(DataOperation.UPDATE, lUpdateData);
                    break;

                default:
                    log.error("Invalid operation specified.");
                    break;
            }
        return returnValue;
    }

}