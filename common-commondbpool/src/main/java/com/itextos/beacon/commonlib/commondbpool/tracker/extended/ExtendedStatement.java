package com.itextos.beacon.commonlib.commondbpool.tracker.extended;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.itextos.beacon.commonlib.commondbpool.tracker.ConnectionsTracker;
import com.itextos.beacon.commonlib.commondbpool.tracker.IDGenerator;
import com.itextos.beacon.commonlib.commondbpool.tracker.SQLTracker;

public class ExtendedStatement
        implements
        Statement
{

    private final String             m_PoolName;
    private final ExtendedConnection m_Connection;
    private final Statement          m_Statement;
    private final Thread             m_CalledBy;
    private final String             m_StatementId;
    private final long               m_CreatedTime;
    private final SQLTracker         m_SQLTracker;

    public ExtendedStatement(
            String aPoolName,
            ExtendedConnection aConnection,
            Statement aStatement,
            Thread aCalledBy)
    {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmssSSS");

        m_PoolName    = aPoolName;
        m_Connection  = aConnection;
        m_Statement   = aStatement;
        m_CalledBy    = aCalledBy;

        m_CreatedTime = System.currentTimeMillis();
        m_SQLTracker  = SQLTracker.getInstance();

        m_StatementId = m_Connection.getId() + ":" + IDGenerator.getInstance().getNextStatementId() + ":" + sdf.format(new Date());

        ConnectionsTracker.getInstance().addStatement(m_PoolName, m_Connection, this, m_CalledBy);
    }

    public String getId()
    {
        return m_StatementId;
    }

    public long getCreatedTime()
    {
        return m_CreatedTime;
    }

    public Thread getCalledBy()
    {
        return m_CalledBy;
    }

    @Override
    public <T> T unwrap(
            Class<T> aIface)
            throws SQLException
    {
        return m_Statement.unwrap(aIface);
    }

    @Override
    public boolean isWrapperFor(
            Class<?> aIface)
            throws SQLException
    {
        return m_Statement.isWrapperFor(aIface);
    }

    @Override
    public ResultSet executeQuery(
            String aSql)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return new ExtendedResultSet(m_PoolName, m_Connection, this, m_Statement.executeQuery(aSql), m_CalledBy);
    }

    @Override
    public int executeUpdate(
            String aSql)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_Statement.executeUpdate(aSql);
    }

    @Override
    public void close()
            throws SQLException
    {
        ConnectionsTracker.getInstance().removeStatement(m_PoolName, m_Connection, this);
        m_Statement.close();
    }

    @Override
    public int getMaxFieldSize()
            throws SQLException
    {
        return m_Statement.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(
            int aMax)
            throws SQLException
    {
        m_Statement.setMaxFieldSize(aMax);
    }

    @Override
    public int getMaxRows()
            throws SQLException
    {
        return m_Statement.getMaxRows();
    }

    @Override
    public void setMaxRows(
            int aMax)
            throws SQLException
    {
        m_Statement.setMaxRows(aMax);
    }

    @Override
    public void setEscapeProcessing(
            boolean aEnable)
            throws SQLException
    {
        m_Statement.setEscapeProcessing(aEnable);
    }

    @Override
    public int getQueryTimeout()
            throws SQLException
    {
        return m_Statement.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(
            int aSeconds)
            throws SQLException
    {
        m_Statement.setQueryTimeout(aSeconds);
    }

    @Override
    public void cancel()
            throws SQLException
    {
        m_Statement.cancel();
    }

    @Override
    public SQLWarning getWarnings()
            throws SQLException
    {
        return m_Statement.getWarnings();
    }

    @Override
    public void clearWarnings()
            throws SQLException
    {
        m_Statement.clearWarnings();
    }

    @Override
    public void setCursorName(
            String aName)
            throws SQLException
    {
        m_Statement.setCursorName(aName);
    }

    @Override
    public boolean execute(
            String aSql)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_Statement.execute(aSql);
    }

    @Override
    public ResultSet getResultSet()
            throws SQLException
    {
        return new ExtendedResultSet(m_PoolName, m_Connection, this, m_Statement.getResultSet(), m_CalledBy);
    }

    @Override
    public int getUpdateCount()
            throws SQLException
    {
        return m_Statement.getUpdateCount();
    }

    @Override
    public boolean getMoreResults()
            throws SQLException
    {
        return m_Statement.getMoreResults();
    }

    @Override
    public void setFetchDirection(
            int aDirection)
            throws SQLException
    {
        m_Statement.setFetchDirection(aDirection);
    }

    @Override
    public int getFetchDirection()
            throws SQLException
    {
        return m_Statement.getFetchDirection();
    }

    @Override
    public void setFetchSize(
            int aRows)
            throws SQLException
    {
        m_Statement.setFetchSize(aRows);
    }

    @Override
    public int getFetchSize()
            throws SQLException
    {
        return m_Statement.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency()
            throws SQLException
    {
        return m_Statement.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType()
            throws SQLException
    {
        return m_Statement.getResultSetType();
    }

    @Override
    public void addBatch(
            String aSql)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        m_Statement.addBatch(aSql);
    }

    @Override
    public void clearBatch()
            throws SQLException
    {
        m_Statement.clearBatch();
    }

    @Override
    public int[] executeBatch()
            throws SQLException
    {
        return m_Statement.executeBatch();
    }

    @Override
    public Connection getConnection()
            throws SQLException
    {
        return m_Statement.getConnection();
    }

    @Override
    public boolean getMoreResults(
            int aCurrent)
            throws SQLException
    {
        return m_Statement.getMoreResults(aCurrent);
    }

    @Override
    public ResultSet getGeneratedKeys()
            throws SQLException
    {
        return new ExtendedResultSet(m_PoolName, m_Connection, this, m_Statement.getGeneratedKeys(), m_CalledBy);
    }

    @Override
    public int executeUpdate(
            String aSql,
            int aAutoGeneratedKeys)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_Statement.executeUpdate(aSql, aAutoGeneratedKeys);
    }

    @Override
    public int executeUpdate(
            String aSql,
            int[] aColumnIndexes)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_Statement.executeUpdate(aSql, aColumnIndexes);
    }

    @Override
    public int executeUpdate(
            String aSql,
            String[] aColumnNames)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_Statement.executeUpdate(aSql, aColumnNames);
    }

    @Override
    public boolean execute(
            String aSql,
            int aAutoGeneratedKeys)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_Statement.execute(aSql, aAutoGeneratedKeys);
    }

    @Override
    public boolean execute(
            String aSql,
            int[] aColumnIndexes)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_Statement.execute(aSql, aColumnIndexes);
    }

    @Override
    public boolean execute(
            String aSql,
            String[] aColumnNames)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_Statement.execute(aSql, aColumnNames);
    }

    @Override
    public int getResultSetHoldability()
            throws SQLException
    {
        return m_Statement.getResultSetHoldability();
    }

    @Override
    public boolean isClosed()
            throws SQLException
    {
        return m_Statement.isClosed();
    }

    @Override
    public void setPoolable(
            boolean aPoolable)
            throws SQLException
    {
        m_Statement.setPoolable(aPoolable);
    }

    @Override
    public boolean isPoolable()
            throws SQLException
    {
        return m_Statement.isPoolable();
    }

    @Override
    public void closeOnCompletion()
            throws SQLException
    {
        m_Statement.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion()
            throws SQLException
    {
        return m_Statement.isCloseOnCompletion();
    }

    @Override
    public String toString()
    {
        return m_StatementId;
    }

}
