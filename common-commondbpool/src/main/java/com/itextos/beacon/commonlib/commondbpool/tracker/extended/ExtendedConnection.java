package com.itextos.beacon.commonlib.commondbpool.tracker.extended;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import com.itextos.beacon.commonlib.commondbpool.tracker.ConnectionsTracker;
import com.itextos.beacon.commonlib.commondbpool.tracker.IDGenerator;
import com.itextos.beacon.commonlib.commondbpool.tracker.SQLTracker;

public class ExtendedConnection
        implements
        Connection
{

    private final String     m_PoolName;
    private final Connection m_Connection;
    private final Thread     m_CalledBy;
    private final String     m_ConnectionId;
    private final long       m_CreatedTime;
    private final SQLTracker m_SQLTracker;

    public ExtendedConnection(
            String aPoolName,
            Connection aConnection,
            Thread aCalledBy)
    {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmssSSS");
        m_PoolName     = aPoolName;
        m_Connection   = aConnection;
        m_CalledBy     = aCalledBy;
        m_CreatedTime  = System.currentTimeMillis();

        m_ConnectionId = m_PoolName + ":" + IDGenerator.getInstance().getNextConnectionId() + ":" + sdf.format(new Date());

        m_SQLTracker   = SQLTracker.getInstance();
        ConnectionsTracker.getInstance().addConnection(m_PoolName, this, aCalledBy);
    }

    public String getId()
    {
        return m_ConnectionId;
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
        return m_Connection.unwrap(aIface);
    }

    @Override
    public boolean isWrapperFor(
            Class<?> aIface)
            throws SQLException
    {
        return m_Connection.isWrapperFor(aIface);
    }

    @Override
    public Statement createStatement()
            throws SQLException
    {
        return new ExtendedStatement(m_PoolName, this, m_Connection.createStatement(), m_CalledBy);
    }

    @Override
    public PreparedStatement prepareStatement(
            String aSql)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return new ExtendedPreparedStatement(m_PoolName, this, m_Connection.prepareStatement(aSql), m_CalledBy);
    }

    @Override
    public CallableStatement prepareCall(
            String aSql)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return new ExtendedCallableStatement(m_PoolName, this, m_Connection.prepareCall(aSql), m_CalledBy);
    }

    @Override
    public String nativeSQL(
            String aSql)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_Connection.nativeSQL(aSql);
    }

    @Override
    public void setAutoCommit(
            boolean aAutoCommit)
            throws SQLException
    {
        m_Connection.setAutoCommit(aAutoCommit);
    }

    @Override
    public boolean getAutoCommit()
            throws SQLException
    {
        return m_Connection.getAutoCommit();
    }

    @Override
    public void commit()
            throws SQLException
    {
        m_Connection.commit();
    }

    @Override
    public void rollback()
            throws SQLException
    {
        m_Connection.rollback();
    }

    @Override
    public void close()
            throws SQLException
    {
        ConnectionsTracker.getInstance().removeConnection(m_PoolName, this);
        m_Connection.close();
    }

    @Override
    public boolean isClosed()
            throws SQLException
    {
        return m_Connection.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData()
            throws SQLException
    {
        return m_Connection.getMetaData();
    }

    @Override
    public void setReadOnly(
            boolean aReadOnly)
            throws SQLException
    {
        m_Connection.setReadOnly(aReadOnly);
    }

    @Override
    public boolean isReadOnly()
            throws SQLException
    {
        return m_Connection.isReadOnly();
    }

    @Override
    public void setCatalog(
            String aCatalog)
            throws SQLException
    {
        m_Connection.setCatalog(aCatalog);
    }

    @Override
    public String getCatalog()
            throws SQLException
    {
        return m_Connection.getCatalog();
    }

    @Override
    public void setTransactionIsolation(
            int aLevel)
            throws SQLException
    {
        m_Connection.setTransactionIsolation(aLevel);
    }

    @Override
    public int getTransactionIsolation()
            throws SQLException
    {
        return m_Connection.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings()
            throws SQLException
    {
        return m_Connection.getWarnings();
    }

    @Override
    public void clearWarnings()
            throws SQLException
    {
        m_Connection.clearWarnings();
    }

    @Override
    public Statement createStatement(
            int aResultSetType,
            int aResultSetConcurrency)
            throws SQLException
    {
        return new ExtendedStatement(m_PoolName, this, m_Connection.createStatement(aResultSetType, aResultSetConcurrency), m_CalledBy);
    }

    @Override
    public PreparedStatement prepareStatement(
            String aSql,
            int aResultSetType,
            int aResultSetConcurrency)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return new ExtendedPreparedStatement(m_PoolName, this, m_Connection.prepareStatement(aSql, aResultSetType, aResultSetConcurrency), m_CalledBy);
    }

    @Override
    public CallableStatement prepareCall(
            String aSql,
            int aResultSetType,
            int aResultSetConcurrency)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return new ExtendedCallableStatement(m_PoolName, this, m_Connection.prepareCall(aSql, aResultSetType, aResultSetConcurrency), m_CalledBy);
    }

    @Override
    public Map<String, Class<?>> getTypeMap()
            throws SQLException
    {
        return m_Connection.getTypeMap();
    }

    @Override
    public void setTypeMap(
            Map<String, Class<?>> aMap)
            throws SQLException
    {
        m_Connection.setTypeMap(aMap);
    }

    @Override
    public void setHoldability(
            int aHoldability)
            throws SQLException
    {
        m_Connection.setHoldability(aHoldability);
    }

    @Override
    public int getHoldability()
            throws SQLException
    {
        return m_Connection.getHoldability();
    }

    @Override
    public Savepoint setSavepoint()
            throws SQLException
    {
        return m_Connection.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(
            String aName)
            throws SQLException
    {
        return m_Connection.setSavepoint(aName);
    }

    @Override
    public void rollback(
            Savepoint aSavepoint)
            throws SQLException
    {
        m_Connection.rollback(aSavepoint);
    }

    @Override
    public void releaseSavepoint(
            Savepoint aSavepoint)
            throws SQLException
    {
        m_Connection.releaseSavepoint(aSavepoint);
    }

    @Override
    public Statement createStatement(
            int aResultSetType,
            int aResultSetConcurrency,
            int aResultSetHoldability)
            throws SQLException
    {
        return new ExtendedStatement(m_PoolName, this, m_Connection.createStatement(aResultSetType, aResultSetConcurrency, aResultSetHoldability), m_CalledBy);
    }

    @Override
    public PreparedStatement prepareStatement(
            String aSql,
            int aResultSetType,
            int aResultSetConcurrency,
            int aResultSetHoldability)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return new ExtendedPreparedStatement(m_PoolName, this, m_Connection.prepareStatement(aSql, aResultSetType, aResultSetConcurrency, aResultSetHoldability), m_CalledBy);
    }

    @Override
    public CallableStatement prepareCall(
            String aSql,
            int aResultSetType,
            int aResultSetConcurrency,
            int aResultSetHoldability)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return new ExtendedCallableStatement(m_PoolName, this, m_Connection.prepareCall(aSql, aResultSetType, aResultSetConcurrency, aResultSetHoldability), m_CalledBy);
    }

    @Override
    public PreparedStatement prepareStatement(
            String aSql,
            int aAutoGeneratedKeys)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return new ExtendedPreparedStatement(m_PoolName, this, m_Connection.prepareStatement(aSql, aAutoGeneratedKeys), m_CalledBy);
    }

    @Override
    public PreparedStatement prepareStatement(
            String aSql,
            int[] aColumnIndexes)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return new ExtendedPreparedStatement(m_PoolName, this, m_Connection.prepareStatement(aSql, aColumnIndexes), m_CalledBy);
    }

    @Override
    public PreparedStatement prepareStatement(
            String aSql,
            String[] aColumnNames)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return new ExtendedPreparedStatement(m_PoolName, this, m_Connection.prepareStatement(aSql, aColumnNames), m_CalledBy);
    }

    @Override
    public Clob createClob()
            throws SQLException
    {
        return m_Connection.createClob();
    }

    @Override
    public Blob createBlob()
            throws SQLException
    {
        return m_Connection.createBlob();
    }

    @Override
    public NClob createNClob()
            throws SQLException
    {
        return m_Connection.createNClob();
    }

    @Override
    public SQLXML createSQLXML()
            throws SQLException
    {
        return m_Connection.createSQLXML();
    }

    @Override
    public boolean isValid(
            int aTimeout)
            throws SQLException
    {
        return m_Connection.isValid(aTimeout);
    }

    @Override
    public void setClientInfo(
            String aName,
            String aValue)
            throws SQLClientInfoException
    {
        m_Connection.setClientInfo(aName, aValue);
    }

    @Override
    public void setClientInfo(
            Properties aProperties)
            throws SQLClientInfoException
    {
        m_Connection.setClientInfo(aProperties);
    }

    @Override
    public String getClientInfo(
            String aName)
            throws SQLException
    {
        return m_Connection.getClientInfo(aName);
    }

    @Override
    public Properties getClientInfo()
            throws SQLException
    {
        return m_Connection.getClientInfo();
    }

    @Override
    public Array createArrayOf(
            String aTypeName,
            Object[] aElements)
            throws SQLException
    {
        return m_Connection.createArrayOf(aTypeName, aElements);
    }

    @Override
    public Struct createStruct(
            String aTypeName,
            Object[] aAttributes)
            throws SQLException
    {
        return m_Connection.createStruct(aTypeName, aAttributes);
    }

    @Override
    public void setSchema(
            String aSchema)
            throws SQLException
    {
        m_Connection.setSchema(aSchema);
    }

    @Override
    public String getSchema()
            throws SQLException
    {
        return m_Connection.getSchema();
    }

    @Override
    public void abort(
            Executor aExecutor)
            throws SQLException
    {
        m_Connection.abort(aExecutor);
    }

    @Override
    public void setNetworkTimeout(
            Executor aExecutor,
            int aMilliseconds)
            throws SQLException
    {
        m_Connection.setNetworkTimeout(aExecutor, aMilliseconds);
    }

    @Override
    public int getNetworkTimeout()
            throws SQLException
    {
        return m_Connection.getNetworkTimeout();
    }

    @Override
    public String toString()
    {
        return m_ConnectionId;
    }

}