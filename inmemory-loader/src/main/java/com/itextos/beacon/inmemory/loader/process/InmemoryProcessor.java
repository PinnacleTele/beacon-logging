package com.itextos.beacon.inmemory.loader.process;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;

public abstract class InmemoryProcessor
        implements
        IInmemoryProcess
{

    private static final Log log         = LogFactory.getLog(InmemoryProcessor.class);

    protected InmemoryInput  mInmemoryInput;
    private boolean          isFirstTime = true;

    protected InmemoryProcessor(
            InmemoryInput aInmemoryInputDetail)
    {
        mInmemoryInput = aInmemoryInputDetail;
    }

    @Override
    public void getDataFromDB()
    {

        try (
                Connection con = DBDataSourceFactory.getConnection(mInmemoryInput.getJNDIInfo());
                PreparedStatement pstmt = con.prepareStatement(mInmemoryInput.getSQL(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                ResultSet mResultSet = pstmt.executeQuery();)
        {
        //    pstmt.setFetchSize(1000);
            processResultSet(mResultSet);
            isFirstTime = false;
        }
        catch (final Exception e)
        {
            log.error("ignorable Exception. Exception while doinng inmemory load of '" + mInmemoryInput.getInmemoryId() + "'", e);

            if (isFirstTime)
            {
                log.error("Since the initial load has failed, stopping the application for " + mInmemoryInput, e);
                System.exit(-9);
            }
        }
    }

    
    @Override
    public void getDataFromEJBServer() {
    	
    }
    
    @Override
    public void refreshInmemoryData()
    {
    	String module=System.getenv("module");
    	
    	if(module.equals("dbgwejb")) {
    		
    		getDataFromDB();
    		
    	}else {
    		
    		
        	String dbgw=System.getenv("dbgw");

    		if(dbgw!=null&&dbgw.equals("ejb")) {
    			
    			getDataFromEJBServer();

    		}else {
    			
    			getDataFromDB();

    		}    	}
        
    }

    protected abstract void processResultSet(
            ResultSet mResultSet)
            throws SQLException;

}