package com.itextos.beacon.commonlib.commondbpool.tracker.extended;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
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
import java.util.Map;

import com.itextos.beacon.commonlib.commondbpool.tracker.ConnectionsTracker;
import com.itextos.beacon.commonlib.commondbpool.tracker.IDGenerator;
import com.itextos.beacon.commonlib.commondbpool.tracker.SQLTracker;

public class ExtendedCallableStatement
        implements
        CallableStatement
{

    private final String             m_PoolName;
    private final ExtendedConnection m_Connection;
    private final CallableStatement  m_CallableStatement;
    private final Thread             m_CalledBy;
    private final String             m_CallableStatementId;
    private final long               m_CreatedTimestamp;
    private final SQLTracker         m_SQLTracker;

    public ExtendedCallableStatement(
            String aPoolName,
            ExtendedConnection aConnection,
            CallableStatement aCallableStatement,
            Thread aCalledBy)
    {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmssSSS");
        m_PoolName            = aPoolName;
        m_Connection          = aConnection;
        m_CallableStatement   = aCallableStatement;
        m_CreatedTimestamp    = System.currentTimeMillis();
        m_CalledBy            = aCalledBy;
        m_SQLTracker          = SQLTracker.getInstance();

        m_CallableStatementId = m_Connection.getId() + ":" + IDGenerator.getInstance().getNextCallableStatementId() + ":" + sdf.format(new Date(m_CreatedTimestamp)) + "'";
        ConnectionsTracker.getInstance().addCallableStatment(m_PoolName, m_Connection, this, m_CalledBy);
    }

    public String getId()
    {
        return m_CallableStatementId;
    }

    public long getCreatedTime()
    {
        return m_CreatedTimestamp;
    }

    public Thread getCalledBy()
    {
        return m_CalledBy;
    }

    @Override
    public ResultSet executeQuery()
            throws SQLException
    {
        return new ExtendedResultSet(m_PoolName, m_Connection, this, m_CallableStatement.executeQuery(), m_CalledBy);
    }

    @Override
    public int executeUpdate()
            throws SQLException
    {
        return m_CallableStatement.executeUpdate();
    }

    @Override
    public void setNull(
            int aParameterIndex,
            int aSqlType)
            throws SQLException
    {
        m_CallableStatement.setNull(aParameterIndex, aSqlType);
    }

    @Override
    public void setBoolean(
            int aParameterIndex,
            boolean aX)
            throws SQLException
    {
        m_CallableStatement.setBoolean(aParameterIndex, aX);
    }

    @Override
    public void setByte(
            int aParameterIndex,
            byte aX)
            throws SQLException
    {
        m_CallableStatement.setByte(aParameterIndex, aX);
    }

    @Override
    public void setShort(
            int aParameterIndex,
            short aX)
            throws SQLException
    {
        m_CallableStatement.setShort(aParameterIndex, aX);
    }

    @Override
    public void setInt(
            int aParameterIndex,
            int aX)
            throws SQLException
    {
        m_CallableStatement.setInt(aParameterIndex, aX);
    }

    @Override
    public void setLong(
            int aParameterIndex,
            long aX)
            throws SQLException
    {
        m_CallableStatement.setLong(aParameterIndex, aX);
    }

    @Override
    public void setFloat(
            int aParameterIndex,
            float aX)
            throws SQLException
    {
        m_CallableStatement.setFloat(aParameterIndex, aX);
    }

    @Override
    public void setDouble(
            int aParameterIndex,
            double aX)
            throws SQLException
    {
        m_CallableStatement.setDouble(aParameterIndex, aX);
    }

    @Override
    public void setBigDecimal(
            int aParameterIndex,
            BigDecimal aX)
            throws SQLException
    {
        m_CallableStatement.setBigDecimal(aParameterIndex, aX);
    }

    @Override
    public void setString(
            int aParameterIndex,
            String aX)
            throws SQLException
    {
        m_CallableStatement.setString(aParameterIndex, aX);
    }

    @Override
    public void setBytes(
            int aParameterIndex,
            byte[] aX)
            throws SQLException
    {
        m_CallableStatement.setBytes(aParameterIndex, aX);
    }

    @Override
    public void setDate(
            int aParameterIndex,
            Date aX)
            throws SQLException
    {
        m_CallableStatement.setDate(aParameterIndex, aX);
    }

    @Override
    public void setTime(
            int aParameterIndex,
            Time aX)
            throws SQLException
    {
        m_CallableStatement.setTime(aParameterIndex, aX);
    }

    @Override
    public void setTimestamp(
            int aParameterIndex,
            Timestamp aX)
            throws SQLException
    {
        m_CallableStatement.setTimestamp(aParameterIndex, aX);
    }

    @Override
    public void setAsciiStream(
            int aParameterIndex,
            InputStream aX,
            int aLength)
            throws SQLException
    {
        m_CallableStatement.setAsciiStream(aParameterIndex, aX, aLength);
    }

    @Deprecated
    @Override
    public void setUnicodeStream(
            int aParameterIndex,
            InputStream aX,
            int aLength)
            throws SQLException
    {
        m_CallableStatement.setUnicodeStream(aParameterIndex, aX, aLength);
    }

    @Override
    public void setBinaryStream(
            int aParameterIndex,
            InputStream aX,
            int aLength)
            throws SQLException
    {
        m_CallableStatement.setBinaryStream(aParameterIndex, aX, aLength);
    }

    @Override
    public void clearParameters()
            throws SQLException
    {
        m_CallableStatement.clearParameters();
    }

    @Override
    public void setObject(
            int aParameterIndex,
            Object aX,
            int aTargetSqlType)
            throws SQLException
    {
        m_CallableStatement.setObject(aParameterIndex, aX, aTargetSqlType);
    }

    @Override
    public void setObject(
            int aParameterIndex,
            Object aX)
            throws SQLException
    {
        m_CallableStatement.setObject(aParameterIndex, aX);
    }

    @Override
    public boolean execute()
            throws SQLException
    {
        return m_CallableStatement.execute();
    }

    @Override
    public void addBatch()
            throws SQLException
    {
        m_CallableStatement.addBatch();
    }

    @Override
    public void setCharacterStream(
            int aParameterIndex,
            Reader aReader,
            int aLength)
            throws SQLException
    {
        m_CallableStatement.setCharacterStream(aParameterIndex, aReader, aLength);
    }

    @Override
    public void setRef(
            int aParameterIndex,
            Ref aX)
            throws SQLException
    {
        m_CallableStatement.setRef(aParameterIndex, aX);
    }

    @Override
    public void setBlob(
            int aParameterIndex,
            Blob aX)
            throws SQLException
    {
        m_CallableStatement.setBlob(aParameterIndex, aX);
    }

    @Override
    public void setClob(
            int aParameterIndex,
            Clob aX)
            throws SQLException
    {
        m_CallableStatement.setClob(aParameterIndex, aX);
    }

    @Override
    public void setArray(
            int aParameterIndex,
            Array aX)
            throws SQLException
    {
        m_CallableStatement.setArray(aParameterIndex, aX);
    }

    @Override
    public ResultSetMetaData getMetaData()
            throws SQLException
    {
        return m_CallableStatement.getMetaData();
    }

    @Override
    public void setDate(
            int aParameterIndex,
            Date aX,
            Calendar aCal)
            throws SQLException
    {
        m_CallableStatement.setDate(aParameterIndex, aX, aCal);
    }

    @Override
    public void setTime(
            int aParameterIndex,
            Time aX,
            Calendar aCal)
            throws SQLException
    {
        m_CallableStatement.setTime(aParameterIndex, aX, aCal);
    }

    @Override
    public void setTimestamp(
            int aParameterIndex,
            Timestamp aX,
            Calendar aCal)
            throws SQLException
    {
        m_CallableStatement.setTimestamp(aParameterIndex, aX, aCal);
    }

    @Override
    public void setNull(
            int aParameterIndex,
            int aSqlType,
            String aTypeName)
            throws SQLException
    {
        m_CallableStatement.setNull(aParameterIndex, aSqlType, aTypeName);
    }

    @Override
    public void setURL(
            int aParameterIndex,
            URL aX)
            throws SQLException
    {
        m_CallableStatement.setURL(aParameterIndex, aX);
    }

    @Override
    public ParameterMetaData getParameterMetaData()
            throws SQLException
    {
        return m_CallableStatement.getParameterMetaData();
    }

    @Override
    public void setRowId(
            int aParameterIndex,
            RowId aX)
            throws SQLException
    {
        m_CallableStatement.setRowId(aParameterIndex, aX);
    }

    @Override
    public void setNString(
            int aParameterIndex,
            String aValue)
            throws SQLException
    {
        m_CallableStatement.setNString(aParameterIndex, aValue);
    }

    @Override
    public void setNCharacterStream(
            int aParameterIndex,
            Reader aValue,
            long aLength)
            throws SQLException
    {
        m_CallableStatement.setNCharacterStream(aParameterIndex, aValue, aLength);
    }

    @Override
    public void setNClob(
            int aParameterIndex,
            NClob aValue)
            throws SQLException
    {
        m_CallableStatement.setNClob(aParameterIndex, aValue);
    }

    @Override
    public void setClob(
            int aParameterIndex,
            Reader aReader,
            long aLength)
            throws SQLException
    {
        m_CallableStatement.setClob(aParameterIndex, aReader, aLength);
    }

    @Override
    public void setBlob(
            int aParameterIndex,
            InputStream aInputStream,
            long aLength)
            throws SQLException
    {
        m_CallableStatement.setBlob(aParameterIndex, aInputStream, aLength);
    }

    @Override
    public void setNClob(
            int aParameterIndex,
            Reader aReader,
            long aLength)
            throws SQLException
    {
        m_CallableStatement.setNClob(aParameterIndex, aReader, aLength);
    }

    @Override
    public void setSQLXML(
            int aParameterIndex,
            SQLXML aXmlObject)
            throws SQLException
    {
        m_CallableStatement.setSQLXML(aParameterIndex, aXmlObject);
    }

    @Override
    public void setObject(
            int aParameterIndex,
            Object aX,
            int aTargetSqlType,
            int aScaleOrLength)
            throws SQLException
    {
        m_CallableStatement.setObject(aParameterIndex, aX, aTargetSqlType, aScaleOrLength);
    }

    @Override
    public void setAsciiStream(
            int aParameterIndex,
            InputStream aX,
            long aLength)
            throws SQLException
    {
        m_CallableStatement.setAsciiStream(aParameterIndex, aX, aLength);
    }

    @Override
    public void setBinaryStream(
            int aParameterIndex,
            InputStream aX,
            long aLength)
            throws SQLException
    {
        m_CallableStatement.setBinaryStream(aParameterIndex, aX, aLength);
    }

    @Override
    public void setCharacterStream(
            int aParameterIndex,
            Reader aReader,
            long aLength)
            throws SQLException
    {
        m_CallableStatement.setCharacterStream(aParameterIndex, aReader, aLength);
    }

    @Override
    public void setAsciiStream(
            int aParameterIndex,
            InputStream aX)
            throws SQLException
    {
        m_CallableStatement.setAsciiStream(aParameterIndex, aX);
    }

    @Override
    public void setBinaryStream(
            int aParameterIndex,
            InputStream aX)
            throws SQLException
    {
        m_CallableStatement.setBinaryStream(aParameterIndex, aX);
    }

    @Override
    public void setCharacterStream(
            int aParameterIndex,
            Reader aReader)
            throws SQLException
    {
        m_CallableStatement.setCharacterStream(aParameterIndex, aReader);
    }

    @Override
    public void setNCharacterStream(
            int aParameterIndex,
            Reader aValue)
            throws SQLException
    {
        m_CallableStatement.setNCharacterStream(aParameterIndex, aValue);
    }

    @Override
    public void setClob(
            int aParameterIndex,
            Reader aReader)
            throws SQLException
    {
        m_CallableStatement.setClob(aParameterIndex, aReader);
    }

    @Override
    public void setBlob(
            int aParameterIndex,
            InputStream aInputStream)
            throws SQLException
    {
        m_CallableStatement.setBlob(aParameterIndex, aInputStream);
    }

    @Override
    public void setNClob(
            int aParameterIndex,
            Reader aReader)
            throws SQLException
    {
        m_CallableStatement.setNClob(aParameterIndex, aReader);
    }

    @Override
    public ResultSet executeQuery(
            String aSql)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return new ExtendedResultSet(m_PoolName, m_Connection, this, m_CallableStatement.executeQuery(aSql), m_CalledBy);
    }

    @Override
    public int executeUpdate(
            String aSql)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_CallableStatement.executeUpdate(aSql);
    }

    @Override
    public void close()
            throws SQLException
    {
        ConnectionsTracker.getInstance().removeCallableStatement(m_PoolName, m_Connection, this);
        m_CallableStatement.close();
    }

    @Override
    public int getMaxFieldSize()
            throws SQLException
    {
        return m_CallableStatement.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(
            int aMax)
            throws SQLException
    {
        m_CallableStatement.setMaxFieldSize(aMax);
    }

    @Override
    public int getMaxRows()
            throws SQLException
    {
        return m_CallableStatement.getMaxRows();
    }

    @Override
    public void setMaxRows(
            int aMax)
            throws SQLException
    {
        m_CallableStatement.setMaxRows(aMax);
    }

    @Override
    public void setEscapeProcessing(
            boolean aEnable)
            throws SQLException
    {
        m_CallableStatement.setEscapeProcessing(aEnable);
    }

    @Override
    public int getQueryTimeout()
            throws SQLException
    {
        return m_CallableStatement.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(
            int aSeconds)
            throws SQLException
    {
        m_CallableStatement.setQueryTimeout(aSeconds);
    }

    @Override
    public void cancel()
            throws SQLException
    {
        m_CallableStatement.cancel();
    }

    @Override
    public SQLWarning getWarnings()
            throws SQLException
    {
        return m_CallableStatement.getWarnings();
    }

    @Override
    public void clearWarnings()
            throws SQLException
    {
        m_CallableStatement.clearWarnings();
    }

    @Override
    public void setCursorName(
            String aName)
            throws SQLException
    {
        m_CallableStatement.setCursorName(aName);
    }

    @Override
    public boolean execute(
            String aSql)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_CallableStatement.execute();
    }

    @Override
    public ResultSet getResultSet()
            throws SQLException
    {
        return new ExtendedResultSet(m_PoolName, m_Connection, this, m_CallableStatement.getResultSet(), m_CalledBy);
    }

    @Override
    public int getUpdateCount()
            throws SQLException
    {
        return m_CallableStatement.getUpdateCount();
    }

    @Override
    public boolean getMoreResults()
            throws SQLException
    {
        return m_CallableStatement.getMoreResults();
    }

    @Override
    public void setFetchDirection(
            int aDirection)
            throws SQLException
    {
        m_CallableStatement.setFetchDirection(aDirection);
    }

    @Override
    public int getFetchDirection()
            throws SQLException
    {
        return m_CallableStatement.getFetchDirection();
    }

    @Override
    public void setFetchSize(
            int aRows)
            throws SQLException
    {
        m_CallableStatement.setFetchSize(aRows);
    }

    @Override
    public int getFetchSize()
            throws SQLException
    {
        return m_CallableStatement.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency()
            throws SQLException
    {
        return m_CallableStatement.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType()
            throws SQLException
    {
        return m_CallableStatement.getResultSetType();
    }

    @Override
    public void addBatch(
            String aSql)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        m_CallableStatement.addBatch(aSql);
    }

    @Override
    public void clearBatch()
            throws SQLException
    {
        m_CallableStatement.clearBatch();
    }

    @Override
    public int[] executeBatch()
            throws SQLException
    {
        return m_CallableStatement.executeBatch();
    }

    @Override
    public Connection getConnection()
            throws SQLException
    {
        return m_CallableStatement.getConnection();
    }

    @Override
    public boolean getMoreResults(
            int aCurrent)
            throws SQLException
    {
        return m_CallableStatement.getMoreResults(aCurrent);
    }

    @Override
    public ResultSet getGeneratedKeys()
            throws SQLException
    {
        return new ExtendedResultSet(m_PoolName, m_Connection, this, m_CallableStatement.getGeneratedKeys(), m_CalledBy);
    }

    @Override
    public int executeUpdate(
            String aSql,
            int aAutoGeneratedKeys)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_CallableStatement.executeUpdate(aSql, aAutoGeneratedKeys);
    }

    @Override
    public int executeUpdate(
            String aSql,
            int[] aColumnIndexes)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_CallableStatement.executeUpdate(aSql, aColumnIndexes);
    }

    @Override
    public int executeUpdate(
            String aSql,
            String[] aColumnNames)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_CallableStatement.executeUpdate(aSql, aColumnNames);
    }

    @Override
    public boolean execute(
            String aSql,
            int aAutoGeneratedKeys)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_CallableStatement.execute(aSql, aAutoGeneratedKeys);
    }

    @Override
    public boolean execute(
            String aSql,
            int[] aColumnIndexes)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_CallableStatement.execute(aSql, aColumnIndexes);
    }

    @Override
    public boolean execute(
            String aSql,
            String[] aColumnNames)
            throws SQLException
    {
        m_SQLTracker.logSQL(aSql);
        return m_CallableStatement.execute(aSql, aColumnNames);
    }

    @Override
    public int getResultSetHoldability()
            throws SQLException
    {
        return m_CallableStatement.getResultSetHoldability();
    }

    @Override
    public boolean isClosed()
            throws SQLException
    {
        return m_CallableStatement.isClosed();
    }

    @Override
    public void setPoolable(
            boolean aPoolable)
            throws SQLException
    {
        m_CallableStatement.setPoolable(aPoolable);
    }

    @Override
    public boolean isPoolable()
            throws SQLException
    {
        return m_CallableStatement.isPoolable();
    }

    @Override
    public void closeOnCompletion()
            throws SQLException
    {
        m_CallableStatement.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion()
            throws SQLException
    {
        return m_CallableStatement.isCloseOnCompletion();
    }

    @Override
    public <T> T unwrap(
            Class<T> aIface)
            throws SQLException
    {
        return m_CallableStatement.unwrap(aIface);
    }

    @Override
    public boolean isWrapperFor(
            Class<?> aIface)
            throws SQLException
    {
        return m_CallableStatement.isWrapperFor(aIface);
    }

    @Override
    public void registerOutParameter(
            int aParameterIndex,
            int aSqlType)
            throws SQLException
    {
        m_CallableStatement.registerOutParameter(aParameterIndex, aSqlType);
    }

    @Override
    public void registerOutParameter(
            int aParameterIndex,
            int aSqlType,
            int aScale)
            throws SQLException
    {
        m_CallableStatement.registerOutParameter(aParameterIndex, aSqlType, aScale);
    }

    @Override
    public boolean wasNull()
            throws SQLException
    {
        return m_CallableStatement.wasNull();
    }

    @Override
    public String getString(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getString(aParameterIndex);
    }

    @Override
    public boolean getBoolean(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getBoolean(aParameterIndex);
    }

    @Override
    public byte getByte(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getByte(aParameterIndex);
    }

    @Override
    public short getShort(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getShort(aParameterIndex);
    }

    @Override
    public int getInt(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getInt(aParameterIndex);
    }

    @Override
    public long getLong(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getLong(aParameterIndex);
    }

    @Override
    public float getFloat(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getFloat(aParameterIndex);
    }

    @Override
    public double getDouble(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getDouble(aParameterIndex);
    }

    @Deprecated
    @Override
    public BigDecimal getBigDecimal(
            int aParameterIndex,
            int aScale)
            throws SQLException
    {
        return m_CallableStatement.getBigDecimal(aParameterIndex, aScale);
    }

    @Override
    public byte[] getBytes(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getBytes(aParameterIndex);
    }

    @Override
    public Date getDate(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getDate(aParameterIndex);
    }

    @Override
    public Time getTime(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getTime(aParameterIndex);
    }

    @Override
    public Timestamp getTimestamp(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getTimestamp(aParameterIndex);
    }

    @Override
    public Object getObject(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getObject(aParameterIndex);
    }

    @Override
    public BigDecimal getBigDecimal(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getBigDecimal(aParameterIndex);
    }

    @Override
    public Object getObject(
            int aParameterIndex,
            Map<String, Class<?>> aMap)
            throws SQLException
    {
        return m_CallableStatement.getObject(aParameterIndex, aMap);
    }

    @Override
    public Ref getRef(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getRef(aParameterIndex);
    }

    @Override
    public Blob getBlob(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getBlob(aParameterIndex);
    }

    @Override
    public Clob getClob(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getClob(aParameterIndex);
    }

    @Override
    public Array getArray(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getArray(aParameterIndex);
    }

    @Override
    public Date getDate(
            int aParameterIndex,
            Calendar aCal)
            throws SQLException
    {
        return m_CallableStatement.getDate(aParameterIndex, aCal);
    }

    @Override
    public Time getTime(
            int aParameterIndex,
            Calendar aCal)
            throws SQLException
    {
        return m_CallableStatement.getTime(aParameterIndex, aCal);
    }

    @Override
    public Timestamp getTimestamp(
            int aParameterIndex,
            Calendar aCal)
            throws SQLException
    {
        return m_CallableStatement.getTimestamp(aParameterIndex, aCal);
    }

    @Override
    public void registerOutParameter(
            int aParameterIndex,
            int aSqlType,
            String aTypeName)
            throws SQLException
    {
        m_CallableStatement.registerOutParameter(aParameterIndex, aSqlType, aTypeName);
    }

    @Override
    public void registerOutParameter(
            String aParameterName,
            int aSqlType)
            throws SQLException
    {
        m_CallableStatement.registerOutParameter(aParameterName, aSqlType);
    }

    @Override
    public void registerOutParameter(
            String aParameterName,
            int aSqlType,
            int aScale)
            throws SQLException
    {
        m_CallableStatement.registerOutParameter(aParameterName, aSqlType, aScale);
    }

    @Override
    public void registerOutParameter(
            String aParameterName,
            int aSqlType,
            String aTypeName)
            throws SQLException
    {
        m_CallableStatement.registerOutParameter(aParameterName, aSqlType, aTypeName);
    }

    @Override
    public URL getURL(
            int aParameterIndex)
            throws SQLException
    {
        return getURL(aParameterIndex);
    }

    @Override
    public void setURL(
            String aParameterName,
            URL aVal)
            throws SQLException
    {
        m_CallableStatement.setURL(aParameterName, aVal);
    }

    @Override
    public void setNull(
            String aParameterName,
            int aSqlType)
            throws SQLException
    {
        m_CallableStatement.setNull(aParameterName, aSqlType);
    }

    @Override
    public void setBoolean(
            String aParameterName,
            boolean aX)
            throws SQLException
    {
        m_CallableStatement.setBoolean(aParameterName, aX);
    }

    @Override
    public void setByte(
            String aParameterName,
            byte aX)
            throws SQLException
    {
        m_CallableStatement.setByte(aParameterName, aX);
    }

    @Override
    public void setShort(
            String aParameterName,
            short aX)
            throws SQLException
    {
        m_CallableStatement.setShort(aParameterName, aX);
    }

    @Override
    public void setInt(
            String aParameterName,
            int aX)
            throws SQLException
    {
        m_CallableStatement.setInt(aParameterName, aX);
    }

    @Override
    public void setLong(
            String aParameterName,
            long aX)
            throws SQLException
    {
        m_CallableStatement.setLong(aParameterName, aX);
    }

    @Override
    public void setFloat(
            String aParameterName,
            float aX)
            throws SQLException
    {
        m_CallableStatement.setFloat(aParameterName, aX);
    }

    @Override
    public void setDouble(
            String aParameterName,
            double aX)
            throws SQLException
    {
        m_CallableStatement.setDouble(aParameterName, aX);
    }

    @Override
    public void setBigDecimal(
            String aParameterName,
            BigDecimal aX)
            throws SQLException
    {
        m_CallableStatement.setBigDecimal(aParameterName, aX);
    }

    @Override
    public void setString(
            String aParameterName,
            String aX)
            throws SQLException
    {
        m_CallableStatement.setString(aParameterName, aX);
    }

    @Override
    public void setBytes(
            String aParameterName,
            byte[] aX)
            throws SQLException
    {
        m_CallableStatement.setBytes(aParameterName, aX);
    }

    @Override
    public void setDate(
            String aParameterName,
            Date aX)
            throws SQLException
    {
        m_CallableStatement.setDate(aParameterName, aX);
    }

    @Override
    public void setTime(
            String aParameterName,
            Time aX)
            throws SQLException
    {
        m_CallableStatement.setTime(aParameterName, aX);
    }

    @Override
    public void setTimestamp(
            String aParameterName,
            Timestamp aX)
            throws SQLException
    {
        m_CallableStatement.setTimestamp(aParameterName, aX);
    }

    @Override
    public void setAsciiStream(
            String aParameterName,
            InputStream aX,
            int aLength)
            throws SQLException
    {
        m_CallableStatement.setAsciiStream(aParameterName, aX, aLength);
    }

    @Override
    public void setBinaryStream(
            String aParameterName,
            InputStream aX,
            int aLength)
            throws SQLException
    {
        m_CallableStatement.setBinaryStream(aParameterName, aX, aLength);
    }

    @Override
    public void setObject(
            String aParameterName,
            Object aX,
            int aTargetSqlType,
            int aScale)
            throws SQLException
    {
        m_CallableStatement.setObject(aParameterName, aX, aTargetSqlType, aScale);
    }

    @Override
    public void setObject(
            String aParameterName,
            Object aX,
            int aTargetSqlType)
            throws SQLException
    {
        m_CallableStatement.setObject(aParameterName, aX, aTargetSqlType);
    }

    @Override
    public void setObject(
            String aParameterName,
            Object aX)
            throws SQLException
    {
        m_CallableStatement.setObject(aParameterName, aX);
    }

    @Override
    public void setCharacterStream(
            String aParameterName,
            Reader aReader,
            int aLength)
            throws SQLException
    {
        m_CallableStatement.setCharacterStream(aParameterName, aReader, aLength);
    }

    @Override
    public void setDate(
            String aParameterName,
            Date aX,
            Calendar aCal)
            throws SQLException
    {
        m_CallableStatement.setDate(aParameterName, aX, aCal);
    }

    @Override
    public void setTime(
            String aParameterName,
            Time aX,
            Calendar aCal)
            throws SQLException
    {
        m_CallableStatement.setTime(aParameterName, aX, aCal);
    }

    @Override
    public void setTimestamp(
            String aParameterName,
            Timestamp aX,
            Calendar aCal)
            throws SQLException
    {
        m_CallableStatement.setTimestamp(aParameterName, aX, aCal);
    }

    @Override
    public void setNull(
            String aParameterName,
            int aSqlType,
            String aTypeName)
            throws SQLException
    {
        m_CallableStatement.setNull(aParameterName, aSqlType, aTypeName);
    }

    @Override
    public String getString(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getString(aParameterName);
    }

    @Override
    public boolean getBoolean(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getBoolean(aParameterName);
    }

    @Override
    public byte getByte(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getByte(aParameterName);
    }

    @Override
    public short getShort(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getShort(aParameterName);
    }

    @Override
    public int getInt(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getInt(aParameterName);
    }

    @Override
    public long getLong(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getLong(aParameterName);
    }

    @Override
    public float getFloat(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getFloat(aParameterName);
    }

    @Override
    public double getDouble(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getDouble(aParameterName);
    }

    @Override
    public byte[] getBytes(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getBytes(aParameterName);
    }

    @Override
    public Date getDate(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getDate(aParameterName);
    }

    @Override
    public Time getTime(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getTime(aParameterName);
    }

    @Override
    public Timestamp getTimestamp(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getTimestamp(aParameterName);
    }

    @Override
    public Object getObject(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getObject(aParameterName);
    }

    @Override
    public BigDecimal getBigDecimal(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getBigDecimal(aParameterName);
    }

    @Override
    public Object getObject(
            String aParameterName,
            Map<String, Class<?>> aMap)
            throws SQLException
    {
        return m_CallableStatement.getObject(aParameterName, aMap);
    }

    @Override
    public Ref getRef(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getRef(aParameterName);
    }

    @Override
    public Blob getBlob(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getBlob(aParameterName);
    }

    @Override
    public Clob getClob(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getClob(aParameterName);
    }

    @Override
    public Array getArray(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getArray(aParameterName);
    }

    @Override
    public Date getDate(
            String aParameterName,
            Calendar aCal)
            throws SQLException
    {
        return m_CallableStatement.getDate(aParameterName, aCal);
    }

    @Override
    public Time getTime(
            String aParameterName,
            Calendar aCal)
            throws SQLException
    {
        return m_CallableStatement.getTime(aParameterName, aCal);
    }

    @Override
    public Timestamp getTimestamp(
            String aParameterName,
            Calendar aCal)
            throws SQLException
    {
        return m_CallableStatement.getTimestamp(aParameterName, aCal);
    }

    @Override
    public URL getURL(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getURL(aParameterName);
    }

    @Override
    public RowId getRowId(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getRowId(aParameterIndex);
    }

    @Override
    public RowId getRowId(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getRowId(aParameterName);
    }

    @Override
    public void setRowId(
            String aParameterName,
            RowId aX)
            throws SQLException
    {
        m_CallableStatement.setRowId(aParameterName, aX);
    }

    @Override
    public void setNString(
            String aParameterName,
            String aValue)
            throws SQLException
    {
        m_CallableStatement.setNString(aParameterName, aValue);
    }

    @Override
    public void setNCharacterStream(
            String aParameterName,
            Reader aValue,
            long aLength)
            throws SQLException
    {
        m_CallableStatement.setNCharacterStream(aParameterName, aValue, aLength);
    }

    @Override
    public void setNClob(
            String aParameterName,
            NClob aValue)
            throws SQLException
    {
        m_CallableStatement.setNClob(aParameterName, aValue);
    }

    @Override
    public void setClob(
            String aParameterName,
            Reader aReader,
            long aLength)
            throws SQLException
    {
        m_CallableStatement.setClob(aParameterName, aReader, aLength);
    }

    @Override
    public void setBlob(
            String aParameterName,
            InputStream aInputStream,
            long aLength)
            throws SQLException
    {
        m_CallableStatement.setBlob(aParameterName, aInputStream, aLength);
    }

    @Override
    public void setNClob(
            String aParameterName,
            Reader aReader,
            long aLength)
            throws SQLException
    {
        m_CallableStatement.setNClob(aParameterName, aReader, aLength);
    }

    @Override
    public NClob getNClob(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getNClob(aParameterIndex);
    }

    @Override
    public NClob getNClob(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getNClob(aParameterName);
    }

    @Override
    public void setSQLXML(
            String aParameterName,
            SQLXML aXmlObject)
            throws SQLException
    {
        m_CallableStatement.setSQLXML(aParameterName, aXmlObject);
    }

    @Override
    public SQLXML getSQLXML(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getSQLXML(aParameterIndex);
    }

    @Override
    public SQLXML getSQLXML(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getSQLXML(aParameterName);
    }

    @Override
    public String getNString(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getNString(aParameterIndex);
    }

    @Override
    public String getNString(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getNString(aParameterName);
    }

    @Override
    public Reader getNCharacterStream(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getNCharacterStream(aParameterIndex);
    }

    @Override
    public Reader getNCharacterStream(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getNCharacterStream(aParameterName);
    }

    @Override
    public Reader getCharacterStream(
            int aParameterIndex)
            throws SQLException
    {
        return m_CallableStatement.getCharacterStream(aParameterIndex);
    }

    @Override
    public Reader getCharacterStream(
            String aParameterName)
            throws SQLException
    {
        return m_CallableStatement.getCharacterStream(aParameterName);
    }

    @Override
    public void setBlob(
            String aParameterName,
            Blob aX)
            throws SQLException
    {
        m_CallableStatement.setBlob(aParameterName, aX);
    }

    @Override
    public void setClob(
            String aParameterName,
            Clob aX)
            throws SQLException
    {
        m_CallableStatement.setClob(aParameterName, aX);
    }

    @Override
    public void setAsciiStream(
            String aParameterName,
            InputStream aX,
            long aLength)
            throws SQLException
    {
        m_CallableStatement.setAsciiStream(aParameterName, aX, aLength);
    }

    @Override
    public void setBinaryStream(
            String aParameterName,
            InputStream aX,
            long aLength)
            throws SQLException
    {
        m_CallableStatement.setBinaryStream(aParameterName, aX, aLength);
    }

    @Override
    public void setCharacterStream(
            String aParameterName,
            Reader aReader,
            long aLength)
            throws SQLException
    {
        m_CallableStatement.setNCharacterStream(aParameterName, aReader, aLength);
    }

    @Override
    public void setAsciiStream(
            String aParameterName,
            InputStream aX)
            throws SQLException
    {
        m_CallableStatement.setAsciiStream(aParameterName, aX);
    }

    @Override
    public void setBinaryStream(
            String aParameterName,
            InputStream aX)
            throws SQLException
    {
        m_CallableStatement.setBinaryStream(aParameterName, aX);
    }

    @Override
    public void setCharacterStream(
            String aParameterName,
            Reader aReader)
            throws SQLException
    {
        m_CallableStatement.setCharacterStream(aParameterName, aReader);
    }

    @Override
    public void setNCharacterStream(
            String aParameterName,
            Reader aValue)
            throws SQLException
    {
        m_CallableStatement.setNCharacterStream(aParameterName, aValue);
    }

    @Override
    public void setClob(
            String aParameterName,
            Reader aReader)
            throws SQLException
    {
        m_CallableStatement.setClob(aParameterName, aReader);
    }

    @Override
    public void setBlob(
            String aParameterName,
            InputStream aInputStream)
            throws SQLException
    {
        m_CallableStatement.setBlob(aParameterName, aInputStream);
    }

    @Override
    public void setNClob(
            String aParameterName,
            Reader aReader)
            throws SQLException
    {
        m_CallableStatement.setNClob(aParameterName, aReader);
    }

    @Override
    public <T> T getObject(
            int aParameterIndex,
            Class<T> aType)
            throws SQLException
    {
        return m_CallableStatement.getObject(aParameterIndex, aType);
    }

    @Override
    public <T> T getObject(
            String aParameterName,
            Class<T> aType)
            throws SQLException
    {
        return m_CallableStatement.getObject(aParameterName, aType);
    }

    @Override
    public String toString()
    {
        return m_CallableStatementId;
    }

}
