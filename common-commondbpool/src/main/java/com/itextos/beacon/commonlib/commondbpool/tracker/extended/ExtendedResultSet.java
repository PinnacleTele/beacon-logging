package com.itextos.beacon.commonlib.commondbpool.tracker.extended;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import com.itextos.beacon.commonlib.commondbpool.tracker.ConnectionsTracker;

public class ExtendedResultSet
        implements
        ResultSet
{

    private final String             m_PoolName;
    private final ExtendedConnection m_Connection;
    private final Statement          m_Statement;
    private final ResultSet          m_ResultSet;
    private final Thread             m_CalledBy;
    private final long               m_CreatedTime;
    private final String             m_ResultSetId;

    public ExtendedResultSet(
            String aPoolName,
            ExtendedConnection aConnection,
            Statement aStatement,
            ResultSet aResultSet,
            Thread aCallBy)
    {
        m_PoolName    = aPoolName;
        m_Connection  = aConnection;
        m_ResultSet   = aResultSet;
        m_Statement   = aStatement;
        m_CalledBy    = aCallBy;
        m_CreatedTime = System.currentTimeMillis();

        if (m_Statement instanceof ExtendedStatement)
            m_ResultSetId = ((ExtendedStatement) aStatement).getId();
        else
            if (m_Statement instanceof ExtendedPreparedStatement)
                m_ResultSetId = ((ExtendedPreparedStatement) aStatement).getId();
            else
                if (m_Statement instanceof ExtendedCallableStatement)
                    m_ResultSetId = ((ExtendedCallableStatement) aStatement).getId();
                else
                    m_ResultSetId = "INVALID";

        ConnectionsTracker.getInstance().addResultset(m_PoolName, m_Connection, m_Statement, this, m_CreatedTime, m_CalledBy);
    }

    @Override
    public <T> T unwrap(
            Class<T> aIface)
            throws SQLException
    {
        return m_ResultSet.unwrap(aIface);
    }

    @Override
    public boolean isWrapperFor(
            Class<?> aIface)
            throws SQLException
    {
        return m_ResultSet.isWrapperFor(aIface);
    }

    @Override
    public boolean next()
            throws SQLException
    {
        return m_ResultSet.next();
    }

    @Override
    public void close()
            throws SQLException
    {
        ConnectionsTracker.getInstance().removeResultset(m_PoolName, m_Connection, m_Statement, this);
        m_ResultSet.close();
    }

    @Override
    public boolean wasNull()
            throws SQLException
    {
        return m_ResultSet.wasNull();
    }

    @Override
    public String getString(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getString(aColumnIndex);
    }

    @Override
    public boolean getBoolean(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getBoolean(aColumnIndex);
    }

    @Override
    public byte getByte(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getByte(aColumnIndex);
    }

    @Override
    public short getShort(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getShort(aColumnIndex);
    }

    @Override
    public int getInt(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getInt(aColumnIndex);
    }

    @Override
    public long getLong(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getLong(aColumnIndex);
    }

    @Override
    public float getFloat(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getFloat(aColumnIndex);
    }

    @Override
    public double getDouble(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getDouble(aColumnIndex);
    }

    @Deprecated
    @Override
    public BigDecimal getBigDecimal(
            int aColumnIndex,
            int aScale)
            throws SQLException
    {
        return m_ResultSet.getBigDecimal(aColumnIndex, aScale);
    }

    @Override
    public byte[] getBytes(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getBytes(aColumnIndex);
    }

    @Override
    public Date getDate(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getDate(aColumnIndex);
    }

    @Override
    public Time getTime(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getTime(aColumnIndex);
    }

    @Override
    public Timestamp getTimestamp(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getTimestamp(aColumnIndex);
    }

    @Override
    public InputStream getAsciiStream(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getAsciiStream(aColumnIndex);
    }

    @Deprecated
    @Override
    public InputStream getUnicodeStream(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getUnicodeStream(aColumnIndex);
    }

    @Override
    public InputStream getBinaryStream(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getBinaryStream(aColumnIndex);
    }

    @Override
    public String getString(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getString(aColumnLabel);
    }

    @Override
    public boolean getBoolean(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getBoolean(aColumnLabel);
    }

    @Override
    public byte getByte(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getByte(aColumnLabel);
    }

    @Override
    public short getShort(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getShort(aColumnLabel);
    }

    @Override
    public int getInt(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getInt(aColumnLabel);
    }

    @Override
    public long getLong(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getLong(aColumnLabel);
    }

    @Override
    public float getFloat(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getFloat(aColumnLabel);
    }

    @Override
    public double getDouble(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getDouble(aColumnLabel);
    }

    @Deprecated
    @Override
    public BigDecimal getBigDecimal(
            String aColumnLabel,
            int aScale)
            throws SQLException
    {
        return m_ResultSet.getBigDecimal(aColumnLabel, aScale);
    }

    @Override
    public byte[] getBytes(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getBytes(aColumnLabel);
    }

    @Override
    public Date getDate(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getDate(aColumnLabel);
    }

    @Override
    public Time getTime(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getTime(aColumnLabel);
    }

    @Override
    public Timestamp getTimestamp(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getTimestamp(aColumnLabel);
    }

    @Override
    public InputStream getAsciiStream(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getAsciiStream(aColumnLabel);
    }

    @Deprecated
    @Override
    public InputStream getUnicodeStream(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getUnicodeStream(aColumnLabel);
    }

    @Override
    public InputStream getBinaryStream(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getBinaryStream(aColumnLabel);
    }

    @Override
    public SQLWarning getWarnings()
            throws SQLException
    {
        return m_ResultSet.getWarnings();
    }

    @Override
    public void clearWarnings()
            throws SQLException
    {
        m_ResultSet.clearWarnings();
    }

    @Override
    public String getCursorName()
            throws SQLException
    {
        return m_ResultSet.getCursorName();
    }

    @Override
    public ResultSetMetaData getMetaData()
            throws SQLException
    {
        return m_ResultSet.getMetaData();
    }

    @Override
    public Object getObject(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getObject(aColumnIndex);
    }

    @Override
    public Object getObject(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getObject(aColumnLabel);
    }

    @Override
    public int findColumn(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.findColumn(aColumnLabel);
    }

    @Override
    public Reader getCharacterStream(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getCharacterStream(aColumnIndex);
    }

    @Override
    public Reader getCharacterStream(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getCharacterStream(aColumnLabel);
    }

    @Override
    public BigDecimal getBigDecimal(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getBigDecimal(aColumnIndex);
    }

    @Override
    public BigDecimal getBigDecimal(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getBigDecimal(aColumnLabel);
    }

    @Override
    public boolean isBeforeFirst()
            throws SQLException
    {
        return m_ResultSet.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast()
            throws SQLException
    {
        return m_ResultSet.isAfterLast();
    }

    @Override
    public boolean isFirst()
            throws SQLException
    {
        return m_ResultSet.isFirst();
    }

    @Override
    public boolean isLast()
            throws SQLException
    {
        return m_ResultSet.isLast();
    }

    @Override
    public void beforeFirst()
            throws SQLException
    {
        m_ResultSet.beforeFirst();
    }

    @Override
    public void afterLast()
            throws SQLException
    {
        m_ResultSet.afterLast();
    }

    @Override
    public boolean first()
            throws SQLException
    {
        return m_ResultSet.first();
    }

    @Override
    public boolean last()
            throws SQLException
    {
        return m_ResultSet.last();
    }

    @Override
    public int getRow()
            throws SQLException
    {
        return m_ResultSet.getRow();
    }

    @Override
    public boolean absolute(
            int aRow)
            throws SQLException
    {
        return m_ResultSet.absolute(aRow);
    }

    @Override
    public boolean relative(
            int aRows)
            throws SQLException
    {
        return m_ResultSet.relative(aRows);
    }

    @Override
    public boolean previous()
            throws SQLException
    {
        return m_ResultSet.previous();
    }

    @Override
    public void setFetchDirection(
            int aDirection)
            throws SQLException
    {
        m_ResultSet.setFetchDirection(aDirection);
    }

    @Override
    public int getFetchDirection()
            throws SQLException
    {
        return m_ResultSet.getFetchDirection();
    }

    @Override
    public void setFetchSize(
            int aRows)
            throws SQLException
    {
        m_ResultSet.setFetchSize(aRows);
    }

    @Override
    public int getFetchSize()
            throws SQLException
    {
        return m_ResultSet.getFetchSize();
    }

    @Override
    public int getType()
            throws SQLException
    {
        return m_ResultSet.getType();
    }

    @Override
    public int getConcurrency()
            throws SQLException
    {
        return m_ResultSet.getConcurrency();
    }

    @Override
    public boolean rowUpdated()
            throws SQLException
    {
        return m_ResultSet.rowUpdated();
    }

    @Override
    public boolean rowInserted()
            throws SQLException
    {
        return m_ResultSet.rowInserted();
    }

    @Override
    public boolean rowDeleted()
            throws SQLException
    {
        return m_ResultSet.rowDeleted();
    }

    @Override
    public void updateNull(
            int aColumnIndex)
            throws SQLException
    {
        m_ResultSet.updateNull(aColumnIndex);
    }

    @Override
    public void updateBoolean(
            int aColumnIndex,
            boolean aX)
            throws SQLException
    {
        m_ResultSet.updateBoolean(aColumnIndex, aX);
    }

    @Override
    public void updateByte(
            int aColumnIndex,
            byte aX)
            throws SQLException
    {
        m_ResultSet.updateByte(aColumnIndex, aX);
    }

    @Override
    public void updateShort(
            int aColumnIndex,
            short aX)
            throws SQLException
    {
        m_ResultSet.updateShort(aColumnIndex, aX);
    }

    @Override
    public void updateInt(
            int aColumnIndex,
            int aX)
            throws SQLException
    {
        m_ResultSet.updateInt(aColumnIndex, aX);
    }

    @Override
    public void updateLong(
            int aColumnIndex,
            long aX)
            throws SQLException
    {
        m_ResultSet.updateLong(aColumnIndex, aX);
    }

    @Override
    public void updateFloat(
            int aColumnIndex,
            float aX)
            throws SQLException
    {
        m_ResultSet.updateFloat(aColumnIndex, aX);
    }

    @Override
    public void updateDouble(
            int aColumnIndex,
            double aX)
            throws SQLException
    {
        m_ResultSet.updateDouble(aColumnIndex, aX);
    }

    @Override
    public void updateBigDecimal(
            int aColumnIndex,
            BigDecimal aX)
            throws SQLException
    {
        m_ResultSet.updateBigDecimal(aColumnIndex, aX);
    }

    @Override
    public void updateString(
            int aColumnIndex,
            String aX)
            throws SQLException
    {
        m_ResultSet.updateString(aColumnIndex, aX);
    }

    @Override
    public void updateBytes(
            int aColumnIndex,
            byte[] aX)
            throws SQLException
    {
        m_ResultSet.updateBytes(aColumnIndex, aX);
    }

    @Override
    public void updateDate(
            int aColumnIndex,
            Date aX)
            throws SQLException
    {
        m_ResultSet.updateDate(aColumnIndex, aX);
    }

    @Override
    public void updateTime(
            int aColumnIndex,
            Time aX)
            throws SQLException
    {
        m_ResultSet.updateTime(aColumnIndex, aX);
    }

    @Override
    public void updateTimestamp(
            int aColumnIndex,
            Timestamp aX)
            throws SQLException
    {
        m_ResultSet.updateTimestamp(aColumnIndex, aX);
    }

    @Override
    public void updateAsciiStream(
            int aColumnIndex,
            InputStream aX,
            int aLength)
            throws SQLException
    {
        m_ResultSet.updateAsciiStream(aColumnIndex, aX, aLength);
    }

    @Override
    public void updateBinaryStream(
            int aColumnIndex,
            InputStream aX,
            int aLength)
            throws SQLException
    {
        m_ResultSet.updateBinaryStream(aColumnIndex, aX, aLength);
    }

    @Override
    public void updateCharacterStream(
            int aColumnIndex,
            Reader aX,
            int aLength)
            throws SQLException
    {
        m_ResultSet.updateCharacterStream(aColumnIndex, aX, aLength);
    }

    @Override
    public void updateObject(
            int aColumnIndex,
            Object aX,
            int aScaleOrLength)
            throws SQLException
    {
        m_ResultSet.updateObject(aColumnIndex, aX, aScaleOrLength);
    }

    @Override
    public void updateObject(
            int aColumnIndex,
            Object aX)
            throws SQLException
    {
        m_ResultSet.updateObject(aColumnIndex, aX);
    }

    @Override
    public void updateNull(
            String aColumnLabel)
            throws SQLException
    {
        m_ResultSet.updateNull(aColumnLabel);
    }

    @Override
    public void updateBoolean(
            String aColumnLabel,
            boolean aX)
            throws SQLException
    {
        m_ResultSet.updateBoolean(aColumnLabel, aX);
    }

    @Override
    public void updateByte(
            String aColumnLabel,
            byte aX)
            throws SQLException
    {
        m_ResultSet.updateByte(aColumnLabel, aX);
    }

    @Override
    public void updateShort(
            String aColumnLabel,
            short aX)
            throws SQLException
    {
        m_ResultSet.updateShort(aColumnLabel, aX);
    }

    @Override
    public void updateInt(
            String aColumnLabel,
            int aX)
            throws SQLException
    {
        m_ResultSet.updateInt(aColumnLabel, aX);
    }

    @Override
    public void updateLong(
            String aColumnLabel,
            long aX)
            throws SQLException
    {
        m_ResultSet.updateLong(aColumnLabel, aX);
    }

    @Override
    public void updateFloat(
            String aColumnLabel,
            float aX)
            throws SQLException
    {
        m_ResultSet.updateFloat(aColumnLabel, aX);
    }

    @Override
    public void updateDouble(
            String aColumnLabel,
            double aX)
            throws SQLException
    {
        m_ResultSet.updateDouble(aColumnLabel, aX);
    }

    @Override
    public void updateBigDecimal(
            String aColumnLabel,
            BigDecimal aX)
            throws SQLException
    {
        m_ResultSet.updateBigDecimal(aColumnLabel, aX);
    }

    @Override
    public void updateString(
            String aColumnLabel,
            String aX)
            throws SQLException
    {
        m_ResultSet.updateString(aColumnLabel, aX);
    }

    @Override
    public void updateBytes(
            String aColumnLabel,
            byte[] aX)
            throws SQLException
    {
        m_ResultSet.updateBytes(aColumnLabel, aX);
    }

    @Override
    public void updateDate(
            String aColumnLabel,
            Date aX)
            throws SQLException
    {
        m_ResultSet.updateDate(aColumnLabel, aX);
    }

    @Override
    public void updateTime(
            String aColumnLabel,
            Time aX)
            throws SQLException
    {
        m_ResultSet.updateTime(aColumnLabel, aX);
    }

    @Override
    public void updateTimestamp(
            String aColumnLabel,
            Timestamp aX)
            throws SQLException
    {
        m_ResultSet.updateTimestamp(aColumnLabel, aX);
    }

    @Override
    public void updateAsciiStream(
            String aColumnLabel,
            InputStream aX,
            int aLength)
            throws SQLException
    {
        m_ResultSet.updateAsciiStream(aColumnLabel, aX, aLength);
    }

    @Override
    public void updateBinaryStream(
            String aColumnLabel,
            InputStream aX,
            int aLength)
            throws SQLException
    {
        m_ResultSet.updateBinaryStream(aColumnLabel, aX, aLength);
    }

    @Override
    public void updateCharacterStream(
            String aColumnLabel,
            Reader aReader,
            int aLength)
            throws SQLException
    {
        m_ResultSet.updateCharacterStream(aColumnLabel, aReader, aLength);
    }

    @Override
    public void updateObject(
            String aColumnLabel,
            Object aX,
            int aScaleOrLength)
            throws SQLException
    {
        m_ResultSet.updateObject(aColumnLabel, aX, aScaleOrLength);
    }

    @Override
    public void updateObject(
            String aColumnLabel,
            Object aX)
            throws SQLException
    {
        m_ResultSet.updateObject(aColumnLabel, aX);
    }

    @Override
    public void insertRow()
            throws SQLException
    {
        m_ResultSet.insertRow();
    }

    @Override
    public void updateRow()
            throws SQLException
    {
        m_ResultSet.updateRow();
    }

    @Override
    public void deleteRow()
            throws SQLException
    {
        m_ResultSet.deleteRow();
    }

    @Override
    public void refreshRow()
            throws SQLException
    {
        m_ResultSet.refreshRow();
    }

    @Override
    public void cancelRowUpdates()
            throws SQLException
    {
        m_ResultSet.cancelRowUpdates();
    }

    @Override
    public void moveToInsertRow()
            throws SQLException
    {
        m_ResultSet.moveToInsertRow();
    }

    @Override
    public void moveToCurrentRow()
            throws SQLException
    {
        m_ResultSet.moveToCurrentRow();
    }

    @Override
    public Statement getStatement()
            throws SQLException
    {
        return m_ResultSet.getStatement();
    }

    @Override
    public Object getObject(
            int aColumnIndex,
            Map<String, Class<?>> aMap)
            throws SQLException
    {
        return m_ResultSet.getObject(aColumnIndex, aMap);
    }

    @Override
    public Ref getRef(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getRef(aColumnIndex);
    }

    @Override
    public Blob getBlob(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getBlob(aColumnIndex);
    }

    @Override
    public Clob getClob(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getClob(aColumnIndex);
    }

    @Override
    public Array getArray(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getArray(aColumnIndex);
    }

    @Override
    public Object getObject(
            String aColumnLabel,
            Map<String, Class<?>> aMap)
            throws SQLException
    {
        return m_ResultSet.getObject(aColumnLabel, aMap);
    }

    @Override
    public Ref getRef(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getRef(aColumnLabel);
    }

    @Override
    public Blob getBlob(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getBlob(aColumnLabel);
    }

    @Override
    public Clob getClob(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getClob(aColumnLabel);
    }

    @Override
    public Array getArray(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getArray(aColumnLabel);
    }

    @Override
    public Date getDate(
            int aColumnIndex,
            Calendar aCal)
            throws SQLException
    {
        return m_ResultSet.getDate(aColumnIndex, aCal);
    }

    @Override
    public Date getDate(
            String aColumnLabel,
            Calendar aCal)
            throws SQLException
    {
        return m_ResultSet.getDate(aColumnLabel, aCal);
    }

    @Override
    public Time getTime(
            int aColumnIndex,
            Calendar aCal)
            throws SQLException
    {
        return m_ResultSet.getTime(aColumnIndex, aCal);
    }

    @Override
    public Time getTime(
            String aColumnLabel,
            Calendar aCal)
            throws SQLException
    {
        return m_ResultSet.getTime(aColumnLabel, aCal);
    }

    @Override
    public Timestamp getTimestamp(
            int aColumnIndex,
            Calendar aCal)
            throws SQLException
    {
        return m_ResultSet.getTimestamp(aColumnIndex, aCal);
    }

    @Override
    public Timestamp getTimestamp(
            String aColumnLabel,
            Calendar aCal)
            throws SQLException
    {
        return m_ResultSet.getTimestamp(aColumnLabel, aCal);
    }

    @Override
    public URL getURL(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getURL(aColumnIndex);
    }

    @Override
    public URL getURL(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getURL(aColumnLabel);
    }

    @Override
    public void updateRef(
            int aColumnIndex,
            Ref aX)
            throws SQLException
    {
        m_ResultSet.updateRef(aColumnIndex, aX);
    }

    @Override
    public void updateRef(
            String aColumnLabel,
            Ref aX)
            throws SQLException
    {
        m_ResultSet.updateRef(aColumnLabel, aX);
    }

    @Override
    public void updateBlob(
            int aColumnIndex,
            Blob aX)
            throws SQLException
    {
        m_ResultSet.updateBlob(aColumnIndex, aX);
    }

    @Override
    public void updateBlob(
            String aColumnLabel,
            Blob aX)
            throws SQLException
    {
        m_ResultSet.updateBlob(aColumnLabel, aX);
    }

    @Override
    public void updateClob(
            int aColumnIndex,
            Clob aX)
            throws SQLException
    {
        m_ResultSet.updateClob(aColumnIndex, aX);
    }

    @Override
    public void updateClob(
            String aColumnLabel,
            Clob aX)
            throws SQLException
    {
        m_ResultSet.updateClob(aColumnLabel, aX);
    }

    @Override
    public void updateArray(
            int aColumnIndex,
            Array aX)
            throws SQLException
    {
        m_ResultSet.updateArray(aColumnIndex, aX);
    }

    @Override
    public void updateArray(
            String aColumnLabel,
            Array aX)
            throws SQLException
    {
        m_ResultSet.updateArray(aColumnLabel, aX);
    }

    @Override
    public RowId getRowId(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getRowId(aColumnIndex);
    }

    @Override
    public RowId getRowId(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getRowId(aColumnLabel);
    }

    @Override
    public void updateRowId(
            int aColumnIndex,
            RowId aX)
            throws SQLException
    {
        m_ResultSet.updateRowId(aColumnIndex, aX);
    }

    @Override
    public void updateRowId(
            String aColumnLabel,
            RowId aX)
            throws SQLException
    {
        m_ResultSet.updateRowId(aColumnLabel, aX);
    }

    @Override
    public int getHoldability()
            throws SQLException
    {
        return m_ResultSet.getHoldability();
    }

    @Override
    public boolean isClosed()
            throws SQLException
    {
        return m_ResultSet.isClosed();
    }

    @Override
    public void updateNString(
            int aColumnIndex,
            String aNString)
            throws SQLException
    {
        m_ResultSet.updateNString(aColumnIndex, aNString);
    }

    @Override
    public void updateNString(
            String aColumnLabel,
            String aNString)
            throws SQLException
    {
        m_ResultSet.updateNString(aColumnLabel, aNString);
    }

    @Override
    public void updateNClob(
            int aColumnIndex,
            NClob aNClob)
            throws SQLException
    {
        m_ResultSet.updateNClob(aColumnIndex, aNClob);
    }

    @Override
    public void updateNClob(
            String aColumnLabel,
            NClob aNClob)
            throws SQLException
    {
        m_ResultSet.updateNClob(aColumnLabel, aNClob);
    }

    @Override
    public NClob getNClob(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getNClob(aColumnIndex);
    }

    @Override
    public NClob getNClob(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getNClob(aColumnLabel);
    }

    @Override
    public SQLXML getSQLXML(
            int aColumnIndex)
            throws SQLException
    {
        return getSQLXML(aColumnIndex);
    }

    @Override
    public SQLXML getSQLXML(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getSQLXML(aColumnLabel);
    }

    @Override
    public void updateSQLXML(
            int aColumnIndex,
            SQLXML aXmlObject)
            throws SQLException
    {
        m_ResultSet.updateSQLXML(aColumnIndex, aXmlObject);
    }

    @Override
    public void updateSQLXML(
            String aColumnLabel,
            SQLXML aXmlObject)
            throws SQLException
    {
        m_ResultSet.updateSQLXML(aColumnLabel, aXmlObject);
    }

    @Override
    public String getNString(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getNString(aColumnIndex);
    }

    @Override
    public String getNString(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getNString(aColumnLabel);
    }

    @Override
    public Reader getNCharacterStream(
            int aColumnIndex)
            throws SQLException
    {
        return m_ResultSet.getNCharacterStream(aColumnIndex);
    }

    @Override
    public Reader getNCharacterStream(
            String aColumnLabel)
            throws SQLException
    {
        return m_ResultSet.getNCharacterStream(aColumnLabel);
    }

    @Override
    public void updateNCharacterStream(
            int aColumnIndex,
            Reader aX,
            long aLength)
            throws SQLException
    {
        m_ResultSet.updateNCharacterStream(aColumnIndex, aX, aLength);
    }

    @Override
    public void updateNCharacterStream(
            String aColumnLabel,
            Reader aReader,
            long aLength)
            throws SQLException
    {
        m_ResultSet.updateNCharacterStream(aColumnLabel, aReader, aLength);
    }

    @Override
    public void updateAsciiStream(
            int aColumnIndex,
            InputStream aX,
            long aLength)
            throws SQLException
    {
        m_ResultSet.updateAsciiStream(aColumnIndex, aX, aLength);
    }

    @Override
    public void updateBinaryStream(
            int aColumnIndex,
            InputStream aX,
            long aLength)
            throws SQLException
    {
        m_ResultSet.updateBinaryStream(aColumnIndex, aX, aLength);
    }

    @Override
    public void updateCharacterStream(
            int aColumnIndex,
            Reader aX,
            long aLength)
            throws SQLException
    {
        m_ResultSet.updateCharacterStream(aColumnIndex, aX, aLength);
    }

    @Override
    public void updateAsciiStream(
            String aColumnLabel,
            InputStream aX,
            long aLength)
            throws SQLException
    {
        m_ResultSet.updateAsciiStream(aColumnLabel, aX, aLength);
    }

    @Override
    public void updateBinaryStream(
            String aColumnLabel,
            InputStream aX,
            long aLength)
            throws SQLException
    {
        m_ResultSet.updateBinaryStream(aColumnLabel, aX, aLength);
    }

    @Override
    public void updateCharacterStream(
            String aColumnLabel,
            Reader aReader,
            long aLength)
            throws SQLException
    {
        m_ResultSet.updateCharacterStream(aColumnLabel, aReader, aLength);
    }

    @Override
    public void updateBlob(
            int aColumnIndex,
            InputStream aInputStream,
            long aLength)
            throws SQLException
    {
        m_ResultSet.updateBlob(aColumnIndex, aInputStream, aLength);
    }

    @Override
    public void updateBlob(
            String aColumnLabel,
            InputStream aInputStream,
            long aLength)
            throws SQLException
    {
        m_ResultSet.updateBlob(aColumnLabel, aInputStream, aLength);
    }

    @Override
    public void updateClob(
            int aColumnIndex,
            Reader aReader,
            long aLength)
            throws SQLException
    {
        m_ResultSet.updateClob(aColumnIndex, aReader, aLength);
    }

    @Override
    public void updateClob(
            String aColumnLabel,
            Reader aReader,
            long aLength)
            throws SQLException
    {
        m_ResultSet.updateClob(aColumnLabel, aReader, aLength);
    }

    @Override
    public void updateNClob(
            int aColumnIndex,
            Reader aReader,
            long aLength)
            throws SQLException
    {
        m_ResultSet.updateNClob(aColumnIndex, aReader, aLength);
    }

    @Override
    public void updateNClob(
            String aColumnLabel,
            Reader aReader,
            long aLength)
            throws SQLException
    {
        m_ResultSet.updateNClob(aColumnLabel, aReader, aLength);
    }

    @Override
    public void updateNCharacterStream(
            int aColumnIndex,
            Reader aX)
            throws SQLException
    {
        m_ResultSet.updateNCharacterStream(aColumnIndex, aX);
    }

    @Override
    public void updateNCharacterStream(
            String aColumnLabel,
            Reader aReader)
            throws SQLException
    {
        m_ResultSet.updateNCharacterStream(aColumnLabel, aReader);
    }

    @Override
    public void updateAsciiStream(
            int aColumnIndex,
            InputStream aX)
            throws SQLException
    {
        m_ResultSet.updateAsciiStream(aColumnIndex, aX);
    }

    @Override
    public void updateBinaryStream(
            int aColumnIndex,
            InputStream aX)
            throws SQLException
    {
        m_ResultSet.updateBinaryStream(aColumnIndex, aX);
    }

    @Override
    public void updateCharacterStream(
            int aColumnIndex,
            Reader aX)
            throws SQLException
    {
        m_ResultSet.updateCharacterStream(aColumnIndex, aX);
    }

    @Override
    public void updateAsciiStream(
            String aColumnLabel,
            InputStream aX)
            throws SQLException
    {
        m_ResultSet.updateAsciiStream(aColumnLabel, aX);
    }

    @Override
    public void updateBinaryStream(
            String aColumnLabel,
            InputStream aX)
            throws SQLException
    {
        m_ResultSet.updateBinaryStream(aColumnLabel, aX);
    }

    @Override
    public void updateCharacterStream(
            String aColumnLabel,
            Reader aReader)
            throws SQLException
    {
        m_ResultSet.updateCharacterStream(aColumnLabel, aReader);
    }

    @Override
    public void updateBlob(
            int aColumnIndex,
            InputStream aInputStream)
            throws SQLException
    {
        m_ResultSet.updateBlob(aColumnIndex, aInputStream);
    }

    @Override
    public void updateBlob(
            String aColumnLabel,
            InputStream aInputStream)
            throws SQLException
    {
        m_ResultSet.updateBlob(aColumnLabel, aInputStream);
    }

    @Override
    public void updateClob(
            int aColumnIndex,
            Reader aReader)
            throws SQLException
    {
        m_ResultSet.updateClob(aColumnIndex, aReader);
    }

    @Override
    public void updateClob(
            String aColumnLabel,
            Reader aReader)
            throws SQLException
    {
        m_ResultSet.updateClob(aColumnLabel, aReader);
    }

    @Override
    public void updateNClob(
            int aColumnIndex,
            Reader aReader)
            throws SQLException
    {
        m_ResultSet.updateNClob(aColumnIndex, aReader);
    }

    @Override
    public void updateNClob(
            String aColumnLabel,
            Reader aReader)
            throws SQLException
    {
        m_ResultSet.updateNClob(aColumnLabel, aReader);
    }

    @Override
    public <T> T getObject(
            int aColumnIndex,
            Class<T> aType)
            throws SQLException
    {
        return m_ResultSet.getObject(aColumnIndex, aType);
    }

    @Override
    public <T> T getObject(
            String aColumnLabel,
            Class<T> aType)
            throws SQLException
    {
        return m_ResultSet.getObject(aColumnLabel, aType);
    }

}
