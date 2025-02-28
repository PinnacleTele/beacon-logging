package com.itextos.beacon.commonlib.commondbpool.tracker.extended;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.itextos.beacon.commonlib.commondbpool.tracker.ConnectionsTracker;
import com.itextos.beacon.commonlib.commondbpool.tracker.IDGenerator;
import com.itextos.beacon.commonlib.commondbpool.tracker.SQLTracker;

public class ExtendedPreparedStatement
        implements
        PreparedStatement
{

    private final String             m_PoolName;
    private final ExtendedConnection m_Connection;
    private final PreparedStatement  m_PreparedStatement;
    private final Thread             m_CalledBy;
    private final String             m_PreparedStatementId;
    private final long               m_CreatedTime;
    private final SQLTracker         m_SQLTracker;

    public ExtendedPreparedStatement(
            String aPoolName,
            ExtendedConnection aConnection,
            PreparedStatement aPreparedStatement,
            Thread aCalledBy)
    {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmssSSS");
        m_PoolName            = aPoolName;
        m_Connection          = aConnection;
        m_PreparedStatement   = aPreparedStatement;
        m_CalledBy            = aCalledBy;

        m_CreatedTime         = System.currentTimeMillis();
        m_SQLTracker          = SQLTracker.getInstance();

        m_PreparedStatementId = m_Connection.getId() + ":" + IDGenerator.getInstance().getNextPreparedStatementId() + ":" + sdf.format(new java.util.Date());

        ConnectionsTracker.getInstance().addPreparedStatment(m_PoolName, m_Connection, this, m_CalledBy);
    }

    public String getId()
    {
        return m_PreparedStatementId;
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
    public ResultSet executeQuery(
            String aSql)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return new ExtendedResultSet(m_PoolName, m_Connection, this, m_PreparedStatement.executeQuery(aSql), m_CalledBy);
    }

    @Override
    public int executeUpdate(
            String aSql)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_PreparedStatement.executeUpdate(aSql);
    }

    @Override
    public void close()
            throws SQLException
    {
        ConnectionsTracker.getInstance().removePreparedStatement(m_PoolName, m_Connection, this);
        m_PreparedStatement.close();
    }

    @Override
    public int getMaxFieldSize()
            throws SQLException
    {
        return m_PreparedStatement.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(
            int aMax)
            throws SQLException
    {
        m_PreparedStatement.setMaxFieldSize(aMax);
    }

    @Override
    public int getMaxRows()
            throws SQLException
    {
        return m_PreparedStatement.getMaxRows();
    }

    @Override
    public void setMaxRows(
            int aMax)
            throws SQLException
    {
        m_PreparedStatement.setMaxRows(aMax);
    }

    @Override
    public void setEscapeProcessing(
            boolean aEnable)
            throws SQLException
    {
        m_PreparedStatement.setEscapeProcessing(aEnable);
    }

    @Override
    public int getQueryTimeout()
            throws SQLException
    {
        return m_PreparedStatement.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(
            int aSeconds)
            throws SQLException
    {
        m_PreparedStatement.setQueryTimeout(aSeconds);
    }

    @Override
    public void cancel()
            throws SQLException
    {
        m_PreparedStatement.cancel();
    }

    @Override
    public SQLWarning getWarnings()
            throws SQLException
    {
        return m_PreparedStatement.getWarnings();
    }

    @Override
    public void clearWarnings()
            throws SQLException
    {
        m_PreparedStatement.clearWarnings();
    }

    @Override
    public void setCursorName(
            String aName)
            throws SQLException
    {
        m_PreparedStatement.setCursorName(aName);
    }

    @Override
    public boolean execute(
            String aSql)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_PreparedStatement.execute(aSql);
    }

    @Override
    public ResultSet getResultSet()
            throws SQLException
    {
        return new ExtendedResultSet(m_PoolName, m_Connection, this, m_PreparedStatement.getResultSet(), m_CalledBy);
    }

    @Override
    public int getUpdateCount()
            throws SQLException
    {
        return m_PreparedStatement.getUpdateCount();
    }

    @Override
    public boolean getMoreResults()
            throws SQLException
    {
        return m_PreparedStatement.getMoreResults();
    }

    @Override
    public void setFetchDirection(
            int aDirection)
            throws SQLException
    {
        m_PreparedStatement.setFetchDirection(aDirection);
    }

    @Override
    public int getFetchDirection()
            throws SQLException
    {
        return m_PreparedStatement.getFetchDirection();
    }

    @Override
    public void setFetchSize(
            int aRows)
            throws SQLException
    {
        m_PreparedStatement.setFetchSize(aRows);
    }

    @Override
    public int getFetchSize()
            throws SQLException
    {
        return m_PreparedStatement.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency()
            throws SQLException
    {
        return m_PreparedStatement.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType()
            throws SQLException
    {
        return m_PreparedStatement.getResultSetType();
    }

    @Override
    public void addBatch(
            String aSql)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        m_PreparedStatement.addBatch(aSql);
    }

    @Override
    public void clearBatch()
            throws SQLException
    {
        m_PreparedStatement.clearBatch();
    }

    @Override
    public int[] executeBatch()
            throws SQLException
    {
        return m_PreparedStatement.executeBatch();
    }

    @Override
    public Connection getConnection()
            throws SQLException
    {
        return m_PreparedStatement.getConnection();
    }

    @Override
    public boolean getMoreResults(
            int aCurrent)
            throws SQLException
    {
        return m_PreparedStatement.getMoreResults(aCurrent);
    }

    @Override
    public ResultSet getGeneratedKeys()
            throws SQLException
    {
        return new ExtendedResultSet(m_PoolName, m_Connection, this, m_PreparedStatement.getGeneratedKeys(), m_CalledBy);
    }

    @Override
    public int executeUpdate(
            String aSql,
            int aAutoGeneratedKeys)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_PreparedStatement.executeUpdate(aSql, aAutoGeneratedKeys);
    }

    @Override
    public int executeUpdate(
            String aSql,
            int[] aColumnIndexes)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_PreparedStatement.executeUpdate(aSql, aColumnIndexes);
    }

    @Override
    public int executeUpdate(
            String aSql,
            String[] aColumnNames)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_PreparedStatement.executeUpdate(aSql, aColumnNames);
    }

    @Override
    public boolean execute(
            String aSql,
            int aAutoGeneratedKeys)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_PreparedStatement.execute(aSql, aAutoGeneratedKeys);
    }

    @Override
    public boolean execute(
            String aSql,
            int[] aColumnIndexes)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_PreparedStatement.execute(aSql, aColumnIndexes);
    }

    @Override
    public boolean execute(
            String aSql,
            String[] aColumnNames)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_PreparedStatement.execute(aSql, aColumnNames);
    }

    @Override
    public int getResultSetHoldability()
            throws SQLException
    {
        return m_PreparedStatement.getResultSetHoldability();
    }

    @Override
    public boolean isClosed()
            throws SQLException
    {
        return m_PreparedStatement.isClosed();
    }

    @Override
    public void setPoolable(
            boolean aPoolable)
            throws SQLException
    {
        m_PreparedStatement.setPoolable(aPoolable);
    }

    @Override
    public boolean isPoolable()
            throws SQLException
    {
        return m_PreparedStatement.isPoolable();
    }

    @Override
    public void closeOnCompletion()
            throws SQLException
    {
        m_PreparedStatement.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion()
            throws SQLException
    {
        return m_PreparedStatement.isCloseOnCompletion();
    }

    @Override
    public <T> T unwrap(
            Class<T> aIface)
            throws SQLException
    {
        return m_PreparedStatement.unwrap(aIface);
    }

    @Override
    public boolean isWrapperFor(
            Class<?> aIface)
            throws SQLException
    {
        return m_PreparedStatement.isWrapperFor(aIface);
    }

    @Override
    public ResultSet executeQuery()
            throws SQLException
    {
        return new ExtendedResultSet(m_PoolName, m_Connection, this, m_PreparedStatement.executeQuery(), m_CalledBy);
    }

    @Override
    public int executeUpdate()
            throws SQLException
    {
        return m_PreparedStatement.executeUpdate();
    }

    @Override
    public void setNull(
            int aParameterIndex,
            int aSqlType)
            throws SQLException
    {
        m_PreparedStatement.setNull(aParameterIndex, aSqlType);
    }

    @Override
    public void setBoolean(
            int aParameterIndex,
            boolean aX)
            throws SQLException
    {
        m_PreparedStatement.setBoolean(aParameterIndex, aX);
    }

    @Override
    public void setByte(
            int aParameterIndex,
            byte aX)
            throws SQLException
    {
        m_PreparedStatement.setByte(aParameterIndex, aX);
    }

    @Override
    public void setShort(
            int aParameterIndex,
            short aX)
            throws SQLException
    {
        m_PreparedStatement.setShort(aParameterIndex, aX);
    }

    @Override
    public void setInt(
            int aParameterIndex,
            int aX)
            throws SQLException
    {
        m_PreparedStatement.setInt(aParameterIndex, aX);
    }

    @Override
    public void setLong(
            int aParameterIndex,
            long aX)
            throws SQLException
    {
        m_PreparedStatement.setLong(aParameterIndex, aX);
    }

    @Override
    public void setFloat(
            int aParameterIndex,
            float aX)
            throws SQLException
    {
        m_PreparedStatement.setFloat(aParameterIndex, aX);
    }

    @Override
    public void setDouble(
            int aParameterIndex,
            double aX)
            throws SQLException
    {
        m_PreparedStatement.setDouble(aParameterIndex, aX);
    }

    @Override
    public void setBigDecimal(
            int aParameterIndex,
            BigDecimal aX)
            throws SQLException
    {
        m_PreparedStatement.setBigDecimal(aParameterIndex, aX);
    }

    @Override
    public void setString(
            int aParameterIndex,
            String aX)
            throws SQLException
    {
        m_PreparedStatement.setString(aParameterIndex, aX);
    }

    @Override
    public void setBytes(
            int aParameterIndex,
            byte[] aX)
            throws SQLException
    {
        m_PreparedStatement.setBytes(aParameterIndex, aX);
    }

    @Override
    public void setDate(
            int aParameterIndex,
            Date aX)
            throws SQLException
    {
        m_PreparedStatement.setDate(aParameterIndex, aX);
    }

    @Override
    public void setTime(
            int aParameterIndex,
            Time aX)
            throws SQLException
    {
        m_PreparedStatement.setTime(aParameterIndex, aX);
    }

    @Override
    public void setTimestamp(
            int aParameterIndex,
            Timestamp aX)
            throws SQLException
    {
        m_PreparedStatement.setTimestamp(aParameterIndex, aX);
    }

    @Override
    public void setAsciiStream(
            int aParameterIndex,
            InputStream aX,
            int aLength)
            throws SQLException
    {
        m_PreparedStatement.setAsciiStream(aParameterIndex, aX, aLength);
    }

    @Deprecated
    @Override
    public void setUnicodeStream(
            int aParameterIndex,
            InputStream aX,
            int aLength)
            throws SQLException
    {
        m_PreparedStatement.setUnicodeStream(aParameterIndex, aX, aLength);
    }

    @Override
    public void setBinaryStream(
            int aParameterIndex,
            InputStream aX,
            int aLength)
            throws SQLException
    {
        m_PreparedStatement.setBinaryStream(aParameterIndex, aX, aLength);
    }

    @Override
    public void clearParameters()
            throws SQLException
    {
        m_PreparedStatement.clearParameters();
    }

    @Override
    public void setObject(
            int aParameterIndex,
            Object aX,
            int aTargetSqlType)
            throws SQLException
    {
        m_PreparedStatement.setObject(aParameterIndex, aX, aTargetSqlType);
    }

    @Override
    public void setObject(
            int aParameterIndex,
            Object aX)
            throws SQLException
    {
        m_PreparedStatement.setObject(aParameterIndex, aX);
    }

    @Override
    public boolean execute()
            throws SQLException
    {
        return m_PreparedStatement.execute();
    }

    @Override
    public void addBatch()
            throws SQLException
    {
        m_PreparedStatement.addBatch();
    }

    @Override
    public void setCharacterStream(
            int aParameterIndex,
            Reader aReader,
            int aLength)
            throws SQLException
    {
        m_PreparedStatement.setCharacterStream(aParameterIndex, aReader, aLength);
    }

    @Override
    public void setRef(
            int aParameterIndex,
            Ref aX)
            throws SQLException
    {
        m_PreparedStatement.setRef(aParameterIndex, aX);
    }

    @Override
    public void setBlob(
            int aParameterIndex,
            Blob aX)
            throws SQLException
    {
        m_PreparedStatement.setBlob(aParameterIndex, aX);
    }

    @Override
    public void setClob(
            int aParameterIndex,
            Clob aX)
            throws SQLException
    {
        m_PreparedStatement.setClob(aParameterIndex, aX);
    }

    @Override
    public void setArray(
            int aParameterIndex,
            Array aX)
            throws SQLException
    {
        m_PreparedStatement.setArray(aParameterIndex, aX);
    }

    @Override
    public ResultSetMetaData getMetaData()
            throws SQLException
    {
        return m_PreparedStatement.getMetaData();
    }

    @Override
    public void setDate(
            int aParameterIndex,
            Date aX,
            Calendar aCal)
            throws SQLException
    {
        m_PreparedStatement.setDate(aParameterIndex, aX, aCal);
    }

    @Override
    public void setTime(
            int aParameterIndex,
            Time aX,
            Calendar aCal)
            throws SQLException
    {
        m_PreparedStatement.setTime(aParameterIndex, aX, aCal);
    }

    @Override
    public void setTimestamp(
            int aParameterIndex,
            Timestamp aX,
            Calendar aCal)
            throws SQLException
    {
        m_PreparedStatement.setTimestamp(aParameterIndex, null, aCal);
    }

    @Override
    public void setNull(
            int aParameterIndex,
            int aSqlType,
            String aTypeName)
            throws SQLException
    {
        m_PreparedStatement.setNull(aParameterIndex, aSqlType, aTypeName);
    }

    @Override
    public void setURL(
            int aParameterIndex,
            URL aX)
            throws SQLException
    {
        m_PreparedStatement.setURL(aParameterIndex, aX);
    }

    @Override
    public ParameterMetaData getParameterMetaData()
            throws SQLException
    {
        return m_PreparedStatement.getParameterMetaData();
    }

    @Override
    public void setRowId(
            int aParameterIndex,
            RowId aX)
            throws SQLException
    {
        m_PreparedStatement.setRowId(aParameterIndex, aX);
    }

    @Override
    public void setNString(
            int aParameterIndex,
            String aValue)
            throws SQLException
    {
        m_PreparedStatement.setNString(aParameterIndex, aValue);
    }

    @Override
    public void setNCharacterStream(
            int aParameterIndex,
            Reader aValue,
            long aLength)
            throws SQLException
    {
        m_PreparedStatement.setNCharacterStream(aParameterIndex, aValue, aLength);
    }

    @Override
    public void setNClob(
            int aParameterIndex,
            NClob aValue)
            throws SQLException
    {
        m_PreparedStatement.setNClob(aParameterIndex, aValue);
    }

    @Override
    public void setClob(
            int aParameterIndex,
            Reader aReader,
            long aLength)
            throws SQLException
    {
        m_PreparedStatement.setClob(aParameterIndex, aReader, aLength);
    }

    @Override
    public void setBlob(
            int aParameterIndex,
            InputStream aInputStream,
            long aLength)
            throws SQLException
    {
        m_PreparedStatement.setBlob(aParameterIndex, aInputStream, aLength);
    }

    @Override
    public void setNClob(
            int aParameterIndex,
            Reader aReader,
            long aLength)
            throws SQLException
    {
        m_PreparedStatement.setNClob(aParameterIndex, aReader, aLength);
    }

    @Override
    public void setSQLXML(
            int aParameterIndex,
            SQLXML aXmlObject)
            throws SQLException
    {
        m_PreparedStatement.setSQLXML(aParameterIndex, aXmlObject);
    }

    @Override
    public void setObject(
            int aParameterIndex,
            Object aX,
            int aTargetSqlType,
            int aScaleOrLength)
            throws SQLException
    {
        m_PreparedStatement.setObject(aParameterIndex, aX, aTargetSqlType, aScaleOrLength);
    }

    @Override
    public void setAsciiStream(
            int aParameterIndex,
            InputStream aX,
            long aLength)
            throws SQLException
    {
        m_PreparedStatement.setAsciiStream(aParameterIndex, aX, aLength);
    }

    @Override
    public void setBinaryStream(
            int aParameterIndex,
            InputStream aX,
            long aLength)
            throws SQLException
    {
        m_PreparedStatement.setBinaryStream(aParameterIndex, aX, aLength);
    }

    @Override
    public void setCharacterStream(
            int aParameterIndex,
            Reader aReader,
            long aLength)
            throws SQLException
    {
        m_PreparedStatement.setCharacterStream(aParameterIndex, aReader, aLength);
    }

    @Override
    public void setAsciiStream(
            int aParameterIndex,
            InputStream aX)
            throws SQLException
    {
        m_PreparedStatement.setAsciiStream(aParameterIndex, aX, aParameterIndex);
    }

    @Override
    public void setBinaryStream(
            int aParameterIndex,
            InputStream aX)
            throws SQLException
    {
        m_PreparedStatement.setBinaryStream(aParameterIndex, aX);
    }

    @Override
    public void setCharacterStream(
            int aParameterIndex,
            Reader aReader)
            throws SQLException
    {
        m_PreparedStatement.setCharacterStream(aParameterIndex, aReader);
    }

    @Override
    public void setNCharacterStream(
            int aParameterIndex,
            Reader aValue)
            throws SQLException
    {
        m_PreparedStatement.setNCharacterStream(aParameterIndex, aValue);
    }

    @Override
    public void setClob(
            int aParameterIndex,
            Reader aReader)
            throws SQLException
    {
        m_PreparedStatement.setClob(aParameterIndex, aReader);
    }

    @Override
    public void setBlob(
            int aParameterIndex,
            InputStream aInputStream)
            throws SQLException
    {
        m_PreparedStatement.setBlob(aParameterIndex, aInputStream);
    }

    @Override
    public void setNClob(
            int aParameterIndex,
            Reader aReader)
            throws SQLException
    {
        m_PreparedStatement.setNClob(aParameterIndex, aReader);
    }

    @Override
    public String toString()
    {
        return m_PreparedStatementId;
    }

}
