package com.itextos.beacon.inmemory.elasticsearchcolumn;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class ElasticSearchColumnDetail
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static Log                 log                    = LogFactory.getLog(ElasticSearchColumnDetail.class);

    private Map<String,List<ESIndexColMapValue>>        mListESColMap     = new HashMap<String,List<ESIndexColMapValue>>();

    public ElasticSearchColumnDetail(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

  
    public List<ESIndexColMapValue> getElasticSearchColumnDetail(String indextype)
    {
        return mListESColMap.get(indextype);
    }


    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());
        
         Map<String,List<ESIndexColMapValue> >       aListESColMap     = new HashMap<String,List<ESIndexColMapValue>>();

        while (aResultSet.next())
        {
       
        	
            final String column_name   = CommonUtility.nullCheck(aResultSet.getString("column_name"), true);
            final String map_name      = CommonUtility.nullCheck(aResultSet.getString("mapped_name"), true);
            final String column_type   = CommonUtility.nullCheck(aResultSet.getString("column_type"), true);
            final String default_value = CommonUtility.nullCheck(aResultSet.getString("default_value"), true);
            final String index_type = CommonUtility.nullCheck(aResultSet.getString("index_type"), true);

            
            final int    ci_required   = aResultSet.getInt("ci_column_required");

            if ("".equals(column_name) || "".equals(map_name) || "".equals(column_type))
            {
                 break;
            }

            boolean ci_req_flag = false;
            if (ci_required != 0)
                ci_req_flag = true;
            
            List<ESIndexColMapValue> list=aListESColMap.get(index_type);
            
            if(list==null) {
            	
            	list=new ArrayList<ESIndexColMapValue>();
            	aListESColMap.put(index_type, list);
            }

            list.add(new ESIndexColMapValue(column_name, map_name, column_type, default_value, ci_req_flag));

          
        }
        mListESColMap     = aListESColMap;
    }

}