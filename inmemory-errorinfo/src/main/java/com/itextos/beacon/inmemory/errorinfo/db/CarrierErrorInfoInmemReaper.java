package com.itextos.beacon.inmemory.errorinfo.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfo;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;
import com.itextos.beacon.inmemory.errorinfo.data.CarrierErrorInfo;

public class CarrierErrorInfoInmemReaper
        implements
        ITimedProcess
{

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final CarrierErrorInfoInmemReaper INSTANCE = new CarrierErrorInfoInmemReaper();

    }

    private static final Log    log                            = LogFactory.getLog(CarrierErrorInfoInmemReaper.class);

    private static final String SQL_OPERATOR_ERROR_CODE_INSERT = "insert into carrier_error_code(route_id,error_code,error_desc,error_status,failure_type,platform_error_code,created_ts,updated_ts) values (?, ?, ?, ?, ?, ?, now(), now())";

    public static CarrierErrorInfoInmemReaper getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final BlockingQueue<CarrierErrorInfo> toInsert    = new LinkedBlockingQueue<>();

    private boolean                               canContinue = true;
    private final TimedProcessor                  mTimedProcessor;

    private CarrierErrorInfoInmemReaper()
    {
    	
        mTimedProcessor = new TimedProcessor("CarrierErrorInfoInmemReaper", this, TimerIntervalConstant.CARRIER_ERROR_INFO_REFRESH);
       
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "CarrierErrorInfoInmemReaper");
    }

    public void addErrorInfo(
            CarrierErrorInfo aCarrierErrorInfo)
    {

        try
        {
            if (!toInsert.contains(aCarrierErrorInfo))
                toInsert.put(aCarrierErrorInfo);
        }
        catch (final InterruptedException e)
        {
            log.error("Unable to add to the inmemory queue. " + aCarrierErrorInfo, e);
        }
    }

 
  
    @Override
    public boolean canContinue()
    {
        return canContinue;
    }

    private boolean processInMemObjects()
    {
        final int lSize = toInsert.size();

        if (lSize > 0)
        {
            final int                    maxSize = lSize > 1000 ? 1000 : lSize;
            final List<CarrierErrorInfo> insert  = new ArrayList<>(maxSize);
            toInsert.drainTo(insert);
            insertInToDb(insert);

            return lSize > 1000;
        }
        return false;
    }

    @Override
    public boolean processNow()
    {
        return processInMemObjects();
    }

    @Override
    public void stopMe()
    {
        canContinue = false;

 
        if (mTimedProcessor != null)
            mTimedProcessor.stopReaper();
 
    }

    private static void insertInToDb(
            List<CarrierErrorInfo> aInsert)
    {
        Connection        lCon             = null;
        PreparedStatement lPStmt           = null;
        boolean           statementCreated = false;

        try
        {
            lCon = DBDataSourceFactory.getConnectionFromThin(JndiInfo.CONFIGURARION_DB);
            lCon.setAutoCommit(false);

            lPStmt           = lCon.prepareStatement(SQL_OPERATOR_ERROR_CODE_INSERT);
            statementCreated = true;

            if (log.isInfoEnabled())
                log.info("Inserting OperatorErrorInfo : SQL_OPERATOR_ERROR_CODE_INSERT '" + SQL_OPERATOR_ERROR_CODE_INSERT + "'");

            for (final CarrierErrorInfo operatorErrorInfo : aInsert)
            {
                if (log.isInfoEnabled())
                    log.info("Inserting OperatorErrorInfo : '" + operatorErrorInfo + "'");

                lPStmt.setString(1, operatorErrorInfo.getRouteId());
                lPStmt.setString(2, operatorErrorInfo.getErrorCode());
                lPStmt.setString(3, operatorErrorInfo.getErrorDesc());
                lPStmt.setString(4, operatorErrorInfo.getErrorStatus());
                lPStmt.setString(5, operatorErrorInfo.getFailureType().getKey());
                lPStmt.setString(6, operatorErrorInfo.getPlatformErrorCode());
                lPStmt.addBatch();
            }

            lPStmt.executeBatch();
            lCon.commit();

            if (log.isInfoEnabled())
                log.info("Records inserted successfully");
        }
        catch (final Exception e)
        {
            log.error("Exception while inserting into the Error Codes Table", e);

            try
            {
                CommonUtility.rollbackConnection(lCon);
                if (statementCreated)
                    lPStmt.clearBatch();
            }
            catch (final SQLException e1)
            {}

            if (statementCreated)
            {
                log.error("Insert failed as Batch. Will try independently.");
                doInsertIndependent(aInsert, lCon, lPStmt);
            }
            else
            {
                log.error("Returning the error info to the inmemory");
                for (final CarrierErrorInfo operatorErrorInfo : aInsert)
                    getInstance().addErrorInfo(operatorErrorInfo);
            }
        }
        finally
        {
            CommonUtility.closeStatement(lPStmt);
            CommonUtility.closeConnection(lCon);
        }
    }

    private static void doInsertIndependent(
            List<CarrierErrorInfo> aInsert,
            Connection aCon,
            PreparedStatement aPStmt)
    {

        for (final CarrierErrorInfo operatorErrorInfo : aInsert)
        {
            if (log.isInfoEnabled())
                log.info("Inserting OperatorErrorInfo : '" + operatorErrorInfo + "'");

            try
            {
                aCon.setAutoCommit(false);

                aPStmt.setString(1, operatorErrorInfo.getRouteId());
                aPStmt.setString(2, operatorErrorInfo.getErrorCode());
                aPStmt.setString(3, operatorErrorInfo.getErrorDesc());
                aPStmt.setString(4, operatorErrorInfo.getErrorStatus());
                aPStmt.setString(5, operatorErrorInfo.getFailureType().getKey());
                aPStmt.setString(6, operatorErrorInfo.getPlatformErrorCode());

                aPStmt.execute();
                aCon.commit();
            }
            catch (final Exception e)
            {
                CommonUtility.rollbackConnection(aCon);
                log.error("SQL Exception while inserting into the Error Codes Table. " + operatorErrorInfo, e);
            }
        }
    }

}