package com.itextos.beacon.platform.topic2table.inserter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfo;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.topic2table.utils.ConnectionAndStatement;
import com.itextos.beacon.platform.topic2table.utils.T2TUtility;

public class DynamicFullMessageTableInserter
        extends
        AbstractFullMessageTableInserter
{

    private static final Log                          log                     = LogFactory.getLog(DynamicFullMessageTableInserter.class);
    private final Map<String, ConnectionAndStatement> mConnectionStatementMap = new HashMap<>();

    public DynamicFullMessageTableInserter(
            Component aActualComponent,
            Table2DBInserterId aTableInserterId,
            List<BaseMessage> aMessagesToInsert)
    {
        super(aActualComponent, aTableInserterId, aMessagesToInsert, true);
    }

    @Override
    public ConnectionAndStatement getConnectionAndStatement(
            BaseMessage aCurrentMessage)
            throws Exception
    {
        final Map<String, String> map = T2TUtility.getKeyValueMapFromMessage(mFullmsgTableInserterInfo.getTableNameFinderKeys(), aCurrentMessage);

        mTableNameFinder.setInputValues(mFullmsgTableInserterInfo.getJndiInfo(), mFullmsgTableInserterInfo.getDatabaseName(), mFullmsgTableInserterInfo.getTableName(), map);
        mTableNameFinder.process();

        final JndiInfo curJndiInfoID = mTableNameFinder.getJndiInfo();
        final String   curTableName  = mTableNameFinder.getTableName();
        final String   tempQuery     = T2TUtility.replaceTableName(mFullMessageInsertQuery, curTableName);
        final String   tempID        = CommonUtility.combine(curJndiInfoID.getId() + "", curTableName);

        if (log.isInfoEnabled())
            log.info("Query for the Table ID : '" + tempID + "' is : '" + tempQuery + "'");

        ConnectionAndStatement tempConStatement = mConnectionStatementMap.get(tempID);

        if (tempConStatement == null)
        {
            tempConStatement = createConnectionStatement(curJndiInfoID, tempID, tempQuery);
            mConnectionStatementMap.put(tempID, tempConStatement);
        }
        return tempConStatement;
    }

    private static ConnectionAndStatement createConnectionStatement(
            JndiInfo aJndiInfoID,
            String aTempID,
            String aTempQuery)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("Creating connection for the table id : '" + aTempID + "'");

        final Connection tempCon = DBDataSourceFactory.getConnection(aJndiInfoID);
        tempCon.setAutoCommit(false);

        if (log.isDebugEnabled())
            log.debug("Creating statement for the table id : '" + aTempID + "'");

        final PreparedStatement pstmt = tempCon.prepareStatement(aTempQuery);
        return new ConnectionAndStatement(aTempID, tempCon, pstmt);
    }

    @Override
    public void processRemaingDataAndCommit(
            Map<String, AtomicInteger> aRecordCounter,
            int aBatchSize)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Process Remaining data for all the tables.");

        for (final String id : aRecordCounter.keySet())
        {
            final AtomicInteger integer = aRecordCounter.get(id);

            if (log.isDebugEnabled())
                log.debug("Counter for the table id : '" + id + "' is : '" + integer + "'");

            if ((integer.intValue() % aBatchSize) != 0)
                try
                {
                    final ConnectionAndStatement connectionAndStatement = mConnectionStatementMap.get(id);
                    connectionAndStatement.getStatement().executeBatch();

                    if (log.isDebugEnabled())
                        log.debug("Successfully executed the remaining data for table id : '" + id + "'");
                }
                catch (final SQLException e)
                {
                    // log.error("Exception while executing the remaing data", e);
                    throw e;
                }
        }

        final List<String> commited = new ArrayList<>();

        for (final Entry<String, ConnectionAndStatement> entry : mConnectionStatementMap.entrySet())
        {
            if (log.isDebugEnabled())
                log.debug("Committing the transaction for the table id : '" + entry.getKey() + "'");

            try
            {
                entry.getValue().getConnection().commit();
                commited.add(entry.getKey());
            }
            catch (final SQLException e)
            {
                log.error("Exception while commiting after executing the remaing data." + " In this case we may get some duplicate entries in the table"
                        + " as commit has happened for set of connections." + " Already Committed connections : '" + commited + "'", e);
                throw e;
            }
        }

        if (log.isDebugEnabled())
            log.debug("All connections committed");
    }

    @Override
    public void closeConnectionAndStatement()
    {
        if (log.isInfoEnabled())
            log.info("Closing the connections and statements.");

        for (final String id : mConnectionStatementMap.keySet())
        {
            if (log.isDebugEnabled())
                log.debug("Closing Connection and prepared statement for the table id : '" + id + "'");

            final ConnectionAndStatement remove = mConnectionStatementMap.get(id);

            if (remove != null)
            {
                CommonUtility.closeStatement(remove.getStatement());
                CommonUtility.closeConnection(remove.getConnection());
            }
        }
        mConnectionStatementMap.clear();
    }

    @Override
    public void rollBackTransactions()
    {
        if (log.isInfoEnabled())
            log.info("Rollbacking the transactions.");

        for (final String id : mConnectionStatementMap.keySet())
        {
            if (log.isDebugEnabled())
                log.debug("Clearing the parameters of the statment for the table id : '" + id + "'");

            try
            {
                final ConnectionAndStatement connectionAndStatement = mConnectionStatementMap.get(id);

                if (connectionAndStatement != null)
                {
                    connectionAndStatement.getStatement().clearBatch();
                    CommonUtility.rollbackConnection(connectionAndStatement.getConnection());
                }
            }
            catch (final SQLException e)
            {
                log.error("Exception while clearing the batch and / or rollbacking the transaction for the table id : '" + id + "'", e);
            }
        }
    }

}