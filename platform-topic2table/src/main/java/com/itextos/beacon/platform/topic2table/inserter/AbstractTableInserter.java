package com.itextos.beacon.platform.topic2table.inserter;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.message.ErrorObject;
import com.itextos.beacon.commonlib.messageprocessor.process.MessageProcessor;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.errorlog.ErrorLog;
import com.itextos.beacon.inmemory.loader.InmemoryLoaderCollection;
import com.itextos.beacon.inmemory.loader.process.InmemoryId;
import com.itextos.beacon.platform.topic2table.dbinfo.ColumnInfo;
import com.itextos.beacon.platform.topic2table.dbinfo.ColumnInfoHandler;
import com.itextos.beacon.platform.topic2table.dbinfo.ReplaceIgnoreColumn;
import com.itextos.beacon.platform.topic2table.dbinfo.TableInserterInfo;
import com.itextos.beacon.platform.topic2table.dbinfo.TableInserterInfoCollection;
import com.itextos.beacon.platform.topic2table.impl.billing.SubmissionTableInserter;
import com.itextos.beacon.platform.topic2table.tablename.ITablenameFinder;
import com.itextos.beacon.platform.topic2table.utils.ConnectionAndStatement;
import com.itextos.beacon.platform.topic2table.utils.ExceptionHandlerType;
import com.itextos.beacon.platform.topic2table.utils.T2TUtility;

abstract class AbstractTableInserter
        implements
        ITableInserter
{

    private static final Log          log                         = LogFactory.getLog(AbstractTableInserter.class);
    protected static final String     TABLE_NAME                  = "{0}";
    protected static final int        BATCH_SIZE                  = 1000;

    private final Component           mActualComponent;
    private final Table2DBInserterId  mTableInsereterId;

    private final boolean             toCheckTableNameFinder;
    protected final List<BaseMessage> mMessagesToInsert;
    protected final List<String>      mAllColumnNames             = new ArrayList<>();

    protected final List<String>      mAllMiddlewareConstantNames = new ArrayList<>();
    protected final List<Integer>     mAllColumnIndices           = new ArrayList<>();

    protected TableInserterInfo       mTableInserterInfo;

    protected String                  mInsertQuery;

    protected ITablenameFinder        mTableNameFinder;

    protected AbstractTableInserter(
            Component aActualComponent,
            Table2DBInserterId aTableInserterId,
            List<BaseMessage> aMessagesToInsert,
            boolean aToCheckTableNameFinder)
    {
        mActualComponent       = aActualComponent;
        mTableInsereterId      = aTableInserterId;
        mMessagesToInsert      = aMessagesToInsert;
        toCheckTableNameFinder = aToCheckTableNameFinder;
        
        initialize();
    }

    private void initialize()
    {
        getTableInsertDetails();
        getTableName();
        buildInsertQuery();
        
       
    }

    private void buildInsertQuery()
    {
        final Map<Integer, ColumnInfo>         allColumnInfo           = ColumnInfoHandler.getInstance().getColumnInfo(mTableInserterInfo.getDatabaseName(), mTableInserterInfo.getTableName());
        final Map<String, ReplaceIgnoreColumn> lReplaceorIgnoreColumns = mTableInserterInfo.getReplaceorIgnoreColumns();

        if (lReplaceorIgnoreColumns.isEmpty())
            loadStaticColumnInfo(allColumnInfo);
        else
            loadDynamicColumnInfo(allColumnInfo, lReplaceorIgnoreColumns);

        mInsertQuery = buildInsertSQL();
    }

  
    private void loadStaticColumnInfo(
            Map<Integer, ColumnInfo> aAllColumnInfo)
    {

        for (int index = 0, size = aAllColumnInfo.size(); index < size; index++)
        {
            mAllColumnNames.add(aAllColumnInfo.get(index + 1).getColumnName().toLowerCase());
            mAllMiddlewareConstantNames.add(aAllColumnInfo.get(index + 1).getColumnName().toLowerCase());
            mAllColumnIndices.add(index + 1);
        }
    }

    private void loadDynamicColumnInfo(
            Map<Integer, ColumnInfo> aAllColumnInfo,
            Map<String, ReplaceIgnoreColumn> aReplaceorIgnoreColumns)
    {

        for (int index = 0, size = aAllColumnInfo.size(); index < size; index++)
        {
            final ColumnInfo          dbci    = aAllColumnInfo.get(index + 1);
            final String              colName = dbci.getColumnName().toLowerCase();
            final ReplaceIgnoreColumn temp    = aReplaceorIgnoreColumns.get(colName);

            if (temp != null)
            {
                // if property value is -1 then skip this column.
                if (temp.isIgnore())
                    continue;
                mAllMiddlewareConstantNames.add(temp.getReplaceColumn());
            }
            else
                // Add the column name in the insert columns list.
                mAllMiddlewareConstantNames.add(colName);

            mAllColumnNames.add(colName);

            // add the column index to the indices
            mAllColumnIndices.add(dbci.getColumnIndex());
        }
    }

    private String buildInsertSQL()
    {
        final String        tableName = getTableName();
        final StringBuilder sbQuery   = new StringBuilder("insert into ");
        sbQuery.append(tableName);

        final StringBuffer sbColumns = new StringBuffer(" (");
        final StringBuffer sbCommos  = new StringBuffer(" values (");

        for (int index = 0, size = mAllColumnNames.size(); index < size; index++)
            if ((index + 1) == size)
            {
                sbColumns.append(mAllColumnNames.get(index)).append(") ");
                sbCommos.append("?)");
            }
            else
            {
                sbColumns.append(mAllColumnNames.get(index)).append(", ");
                sbCommos.append("?,");
            }

        sbQuery.append(sbColumns).append(sbCommos);
        return sbQuery.toString();
    }

    
    
    private String getTableName()
    {
        final String lTableNameFinderClassName = mTableInserterInfo.getTableNameFinderClass();

        if (!toCheckTableNameFinder || lTableNameFinderClassName.equals(""))
            return mTableInserterInfo.getTableName();

        mTableNameFinder = T2TUtility.getTableNameFinder(mTableInserterInfo.getTableNameFinderClass());
        return TABLE_NAME;
    }
    
   
    private void getTableInsertDetails()
    {
        final TableInserterInfoCollection tiic = (TableInserterInfoCollection) InmemoryLoaderCollection.getInstance().getInmemoryCollection(InmemoryId.TABLE_INSERTER_INFO);

        mTableInserterInfo = tiic.getTableInserterInfo(mTableInsereterId);
        if (mTableInserterInfo == null) {
         //   throw new ItextosRuntimeException("Cannot proceed with '" + mTableInsereterId + "' as not data found for it.");
           log.error("Cannot proceed with '" + mTableInsereterId + "' as not data found for it."); 
        }
    }
    
    

    @Override
    public ExceptionHandlerType handleException(
            Exception aException,
            boolean aIsBatchInsert)
    {
        return T2TUtility.checkExceptionType(aException, aIsBatchInsert);
    }

    @Override
    public void process()
    {
        if (log.isDebugEnabled())
            log.debug("Process Started. Messages Count : '" + mMessagesToInsert.size() + "'");

        Exception exception = null;

        try
        {
            final Map<String, AtomicInteger> allBatchRecordCounts = new HashMap<>();
            final Map<Integer, ColumnInfo>   allColInfo           = ColumnInfoHandler.getInstance().getColumnInfo(mTableInserterInfo.getDatabaseName(), mTableInserterInfo.getTableName());

            for (final BaseMessage currentMessage : mMessagesToInsert)
            {
                if (log.isDebugEnabled())
                    log.debug("Processing current message : '" + currentMessage + "'");

                final ConnectionAndStatement connectionAndStatment = getConnectionAndStatement(currentMessage);
                final PreparedStatement      pstmt                 = connectionAndStatment.getStatement();
                final AtomicInteger          recCounterObj         = allBatchRecordCounts.computeIfAbsent(connectionAndStatment.getTableID(), k -> new AtomicInteger());

                if (log.isInfoEnabled())
                    log.info("Records added into batch for the table : '" + connectionAndStatment.getTableID() + "' is : '" + recCounterObj.get() + "'");

                final int recCount = recCounterObj.incrementAndGet();
                T2TUtility.populateColumnDataValues(pstmt, currentMessage, mAllMiddlewareConstantNames, allColInfo, mAllColumnIndices, false);

                pstmt.addBatch();
                pstmt.clearParameters();

                if ((recCount % BATCH_SIZE) == 0)
                {
                    if (log.isDebugEnabled())
                        log.debug("Batch Executing the records for the table : '" + connectionAndStatment.getTableID() + "'");

                    pstmt.executeBatch();
                    pstmt.clearBatch();
                }
            } // end of for

            if (log.isDebugEnabled())
            {
                log.debug("Completed adding messages to the Statement");
                log.info("Executing the remaing records into the table");
            }

            processRemaingDataAndCommit(allBatchRecordCounts, BATCH_SIZE);

            if (log.isInfoEnabled())
                log.info("Completed processing all the messages. Total Records processed : '" + mMessagesToInsert.size() + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while processing the messsages in Batch", e);

            if (log.isInfoEnabled())
                log.info("Rollbacking the transactions and inserting the records individually");

            try
            {
                rollBackTransactions();
            }
            catch (final Exception e1)
            {
                log.error("Exception while rollbacking the transaction. However the messages will be processed individually. These transactions will cleared.", e1);
            }

            exception = e;
        }
        finally
        {
            if (log.isDebugEnabled())
                log.debug("Close the statements and connections");

            // Close the already opened Connections and Statements in all success flow.
            closeConnectionAndStatement();
        }

        if (exception != null)
        {
            final ExceptionHandlerType exceptionType = T2TUtility.checkExceptionType(exception, true);
            T2TUtility.handleException(this, exceptionType, exception, false, null);
        }

        if (log.isDebugEnabled())
            log.debug("Completed the process method.");
    }

    @Override
    public void processIndividualMessages(
            boolean aTrimIt)
    {
        if (log.isDebugEnabled())
            log.debug("Started processing individaul messages");

        Exception               exception   = null;

        final List<BaseMessage> toBeRemoved = new ArrayList<>();

        try
        {
            final Map<Integer, ColumnInfo> allColInfo = ColumnInfoHandler.getInstance().getColumnInfo(mTableInserterInfo.getDatabaseName(), mTableInserterInfo.getTableName());

            for (final BaseMessage currentMessage : mMessagesToInsert)
            {
                if (log.isDebugEnabled())
                    log.debug("Message to be processed is '" + currentMessage + "'");

                final ConnectionAndStatement connectionAndStatment = getConnectionAndStatement(currentMessage);
                final PreparedStatement      pstmt                 = connectionAndStatment.getStatement();

                try
                {
                    T2TUtility.populateColumnDataValues(pstmt, currentMessage, mAllMiddlewareConstantNames, allColInfo, mAllColumnIndices, aTrimIt);

                    if (log.isDebugEnabled())
                        log.debug("PrepareStatement :" + pstmt);

                    // Execute the query and commit it immediately.
                    pstmt.execute();
                    connectionAndStatment.commit();
                    pstmt.clearParameters();

                    // Add the processed message in the to be removed list.
                    toBeRemoved.add(currentMessage);
                }
                catch (final Exception e)
                {
                    if (log.isInfoEnabled())
                        log.info("Insertion failed. Message :'" + currentMessage + "'", e);

                    try
                    {
                        connectionAndStatment.rollback();
                    }
                    catch (final Exception e1)
                    {
                        log.error("Exception while rollbacking the transaction for the message, '" + currentMessage + "'", e1);
                    }

                    final ExceptionHandlerType exceptionType = T2TUtility.checkExceptionType(e, false);
                    T2TUtility.handleException(this, exceptionType, e, true, currentMessage);
                }
            }
        }
        catch (final Exception e)
        {
            log.error("Exception occured while getting the connection and statments. Sending back all the mssages to the original queue after rollback all the transactions.", e);

            if (log.isDebugEnabled())
                log.debug("Rollback the transactions.");

            try
            {
                rollBackTransactions();

                if (log.isDebugEnabled())
                    log.debug("Rollback successfull. Now pushing back to the original queue.");
            }
            catch (final Exception e1)
            {
                log.error("Exception while rollbacking the transaction. However the messages will be pushed back to the original queue.", e1);
            }

            exception = e;
        }
        finally
        {
            if (log.isDebugEnabled())
                log.debug("Close the statements and connections");

            closeConnectionAndStatement();
        }

        if (exception != null)
        {
            final ExceptionHandlerType exceptionType = T2TUtility.checkExceptionType(exception, false);
            T2TUtility.handleException(this, exceptionType, exception, true, null);
        }
    }

    @Override
    public void createAlert(
            Exception aException)
    {
        log.error("An alert has to be created.", aException);
    }

    @Override
    public void returnToSameTopic()
    {
        for (final BaseMessage message : mMessagesToInsert)
            returnToSameTopic(message);
    }

    @Override
    public void returnToSameTopic(
            BaseMessage aBaseMessage)
    {

        try
        {
            MessageProcessor.writeMessage(mActualComponent, mActualComponent, aBaseMessage);
        }
        catch (final ItextosException e)
        {
            log.error("Exception while sending back the messsage " + aBaseMessage, e);
        }
    }

    protected abstract ConnectionAndStatement getConnectionAndStatement(
            BaseMessage aCurrentMessage)
            throws Exception;

    protected abstract void processRemaingDataAndCommit(
            Map<String, AtomicInteger> aAllBatchRecordCounts,
            int aBatchSize)
            throws Exception;

    protected abstract void rollBackTransactions()
            throws Exception;

    protected abstract void closeConnectionAndStatement();

    @Override
    public void dropMessage(
            Exception aException,
            BaseMessage aCurrentMessage)
    {
        log.fatal("Due to unrecoverable error with the message unable to insert into database table. DROPPING THE MESSAGE '" + aCurrentMessage + "'", aException);
        sendToErrorLog(mActualComponent, aCurrentMessage, aException);
    }

    public static void sendToErrorLog(
            Component aComponent,
            BaseMessage aBaseMessage,
            Exception aException)
    {

        try
        {
            final ErrorObject errorObject = aBaseMessage.getErrorObject(aComponent, aException);
            MessageProcessor.writeMessage(aComponent, Component.T2DB_ERROR_LOG, errorObject);
        } catch (final ItextosRuntimeException e)
        {
            log.error("Exception while sending the message to error log topic for the component '" + aComponent + "' Message '" + aBaseMessage + "' Error [[[" + CommonUtility.getStackTrace(aException)
                    + "]]]", e);
        }
        catch (final ItextosException e)
        {
            log.error("Exception while sending the message to error log topic for the component '" + aComponent + "' Message '" + aBaseMessage + "' Error [[[" + CommonUtility.getStackTrace(aException)
                    + "]]]", e);
        }
    }

}