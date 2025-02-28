package com.itextos.beacon.platform.topic2table.inserter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.Table2DBInserterId;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.topic2table.utils.ConnectionAndStatement;

public class StaticTableInserter
        extends
        AbstractTableInserter
{

    private static final Log       log                   = LogFactory.getLog(StaticTableInserter.class);

    private ConnectionAndStatement connectionAndStatment = null;

    public StaticTableInserter(
            Component aActualComponent,
            Table2DBInserterId aTableInserterId,
            List<BaseMessage> aMessagesToInsert)
    {
        super(aActualComponent, aTableInserterId, aMessagesToInsert, false);
    }

    @Override
    public ConnectionAndStatement getConnectionAndStatement(
            BaseMessage aCurrentMessage)
            throws Exception
    {
        if (connectionAndStatment == null)
            connectionAndStatment = createConnectionStatement();
        return connectionAndStatment;
    }

    private ConnectionAndStatement createConnectionStatement()
            throws Exception
    {
         String tableID = CommonUtility.combine(mTableInserterInfo.getJndiInfo().getId() + "", mTableInserterInfo.getTableName());


         
        if (log.isDebugEnabled())
            log.debug("Creating connection for the JNDIInfoID :'" + tableID + "'");

        final Connection con = DBDataSourceFactory.getConnection(mTableInserterInfo.getJndiInfo());
        con.setAutoCommit(false);

        if (log.isDebugEnabled())
            log.debug("Creating prepared statment for the Query : '" + mInsertQuery + "'");

        final PreparedStatement pstmt = con.prepareStatement(mInsertQuery);
        return new ConnectionAndStatement(tableID, con, pstmt);
    }

    @Override
    public void processRemaingDataAndCommit(
            Map<String, AtomicInteger> aRecordCounter,
            int aBatchSize)
            throws Exception
    {
        final String        id      = connectionAndStatment.getTableID();
        final AtomicInteger counter = aRecordCounter.get(id);

        if (log.isDebugEnabled())
            log.debug("Counter for the table id : '" + id + "' is : '" + counter + "'");

        // Obviously it should have only one entry.
        if ((counter.intValue() % aBatchSize) != 0)
            try
            {
                connectionAndStatment.getStatement().executeBatch();
                if (log.isDebugEnabled())
                    log.debug("Successfully executed the remaining data for table id : '" + id + "'");
            }
            catch (final Exception e)
            {
                throw e;
            }

        try
        {
            if (log.isDebugEnabled())
                log.debug("Committing the transaction for the table id : '" + id + "'");

            connectionAndStatment.getConnection().commit();
        }
        catch (final Exception e)
        {
            log.error("Exception while commiting after executing the remaing data", e);
            throw e;
        }
    }

    @Override
    public void closeConnectionAndStatement()
    {

        if (connectionAndStatment != null)
        {
            if (log.isDebugEnabled())
                log.debug("Closing the statement and connection for the table id : '" + connectionAndStatment.getTableID() + "'");

            connectionAndStatment.close();
            connectionAndStatment = null;
        }
        else
            log.info("Connections might have been closed already");
    }

    @Override
    public void rollBackTransactions()
            throws Exception
    {

        if (connectionAndStatment != null)
        {
            if (log.isDebugEnabled())
                log.debug("Rollback transactions for the table id : '" + connectionAndStatment.getTableID() + "'");

            connectionAndStatment.rollback();
        }
        else
            log.info("Connections might have been closed already");
    }

}