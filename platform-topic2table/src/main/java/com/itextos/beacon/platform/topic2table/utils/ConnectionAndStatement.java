package com.itextos.beacon.platform.topic2table.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;

public class ConnectionAndStatement
{

    private static final Log        log = LogFactory.getLog(ConnectionAndStatement.class);

    private final String            tableID;
    private final Connection        connection;
    private final PreparedStatement statement;

    public ConnectionAndStatement(
            String aTableID,
            Connection aConnection,
            PreparedStatement aStatement)
    {
        tableID    = aTableID;
        connection = aConnection;
        statement  = aStatement;
    }

    public String getTableID()
    {
        return tableID;
    }

    public Connection getConnection()
    {
        return connection;
    }

    public PreparedStatement getStatement()
    {
        return statement;
    }

    public void commit()
            throws SQLException
    {
        connection.commit();
    }

    public void rollback()
    {
        if (log.isDebugEnabled())
            log.debug("Rollbacking the transaction");

        try
        {
            statement.clearBatch();
            statement.clearParameters();

            connection.rollback();
        }
        catch (final Exception e)
        {
            log.error("Exception while rollbacking the transactions.", e);
        }
    }

    public void close()
    {
        if (log.isDebugEnabled())
            log.debug("Closing the Statement and Connections.");

        CommonUtility.closeStatement(statement);
        CommonUtility.closeConnection(connection);
    }

    @Override
    public String toString()
    {
        return "ConnectionAndStatement [tableID=" + tableID + "]";
    }

}