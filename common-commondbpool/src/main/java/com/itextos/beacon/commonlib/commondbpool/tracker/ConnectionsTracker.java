package com.itextos.beacon.commonlib.commondbpool.tracker;

import java.sql.Statement;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.tracker.extended.ExtendedCallableStatement;
import com.itextos.beacon.commonlib.commondbpool.tracker.extended.ExtendedConnection;
import com.itextos.beacon.commonlib.commondbpool.tracker.extended.ExtendedPreparedStatement;
import com.itextos.beacon.commonlib.commondbpool.tracker.extended.ExtendedResultSet;
import com.itextos.beacon.commonlib.commondbpool.tracker.extended.ExtendedStatement;

public class ConnectionsTracker
{

    private static final Log  log               = LogFactory.getLog(ConnectionsTracker.class);
    static final List<String> IGNORE_CLASS_LIST = ConnectionTrackerIgnoreList.getAllIgnoreList();
    static final String       INIT              = "<init>";
    static final String       INIT_CONSTRUCTOR  = "in-constrcutor";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final ConnectionsTracker INSTANCE = new ConnectionsTracker();

    }

    private Map<String, Map<ExtendedConnection, StackTraceCollection>>                                         connPoolList            = null;
    private Map<String, Map<ExtendedConnection, Map<ExtendedStatement, StackTraceCollection>>>                 statementCol            = null;
    private Map<String, Map<ExtendedConnection, Map<ExtendedPreparedStatement, StackTraceCollection>>>         prepareStatementCol     = null;
    private Map<String, Map<ExtendedConnection, Map<ExtendedCallableStatement, StackTraceCollection>>>         callableStatementCol    = null;
    private Map<String, Map<ExtendedConnection, Map<Statement, Map<ExtendedResultSet, StackTraceCollection>>>> resltSetCol             = null;
    private Map<String, Map<String, Long>>                                                                     connPoolTimeDetailsList = null;

    private ConnectionsTracker()
    {
        connPoolList            = new Hashtable<>();
        connPoolTimeDetailsList = new Hashtable<>();
        statementCol            = new Hashtable<>();
        prepareStatementCol     = new Hashtable<>();
        callableStatementCol    = new Hashtable<>();
        resltSetCol             = new Hashtable<>();
    }

    public static ConnectionsTracker getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public Object clone()
            throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("ConnectionsTracker can not be cloned !");
    }

    public void addConnection(
            String aPoolName,
            ExtendedConnection aConnection,
            Thread aThread)
    {
        final String s = "Connection Pool '" + aPoolName + "' Connection '" + aConnection + "'";

        try
        {
            if (log.isDebugEnabled())
                log.debug("Adding connection. " + s);

            Map<ExtendedConnection, StackTraceCollection> connInfo = connPoolList.get(aPoolName);

            if (connInfo == null)
            {
                connInfo = new Hashtable<>();

                synchronized (connPoolList)
                {
                    connPoolList.put(aPoolName, connInfo);
                }
            }

            final StackTraceCollection stc = connInfo.get(aConnection);

            if (stc != null)
            {
                log.error("", new RuntimeException("Connection Already in Use ! " + s));
                return;
            }

            synchronized (connInfo)
            {
                connInfo.put(aConnection, new StackTraceCollection(aThread));
            }

            addCreatedTime(aPoolName, aConnection, aConnection.getCreatedTime(), IDGenerator.CONNECTION_PREFIX);
        }
        catch (final Throwable t)
        {
            log.error("Exception in Adding Connection. " + s, t);
        }
    }

    public void removeConnection(
            String aPoolName,
            ExtendedConnection aConnection)
    {
        final String s = "Connection Pool '" + aPoolName + "' Connection '" + aConnection + "'";

        try
        {
            if (log.isDebugEnabled())
                log.debug("Removing Connection " + s);

            final Map<ExtendedConnection, StackTraceCollection> connInfo = connPoolList.get(aPoolName);

            if (connInfo != null)
            {

                synchronized (connInfo)
                {
                    connInfo.remove(aConnection);
                }

                if (connInfo.size() == 0)
                    synchronized (connPoolList)
                    {
                        connPoolList.remove(aPoolName);
                    }

                removeCreatedTime(aPoolName, aConnection, IDGenerator.CONNECTION_PREFIX);
            }
        }
        catch (final Throwable t)
        {
            log.error("Exception in remove Connection " + s, t);
        }
    }

    public void addStatement(
            String aPoolName,
            ExtendedConnection aConnection,
            ExtendedStatement aEXStatement,
            Thread aThread)
    {
        final String s = "Connection Pool '" + aPoolName + "' Connection '" + aConnection + "' Statement '" + aEXStatement + "'";

        try
        {
            if (log.isDebugEnabled())
                log.debug("Adding Statement " + s);

            Map<ExtendedConnection, Map<ExtendedStatement, StackTraceCollection>> connInfoMap = statementCol.get(aPoolName);

            if (connInfoMap == null)
            {
                connInfoMap = new Hashtable<>();

                synchronized (statementCol)
                {
                    statementCol.put(aPoolName, connInfoMap);
                }
            }

            Map<ExtendedStatement, StackTraceCollection> statementInfoMap = connInfoMap.get(aConnection);

            if (statementInfoMap == null)
            {
                statementInfoMap = new Hashtable<>();

                synchronized (connInfoMap)
                {
                    connInfoMap.put(aConnection, statementInfoMap);
                }
            }

            final StackTraceCollection stc = statementInfoMap.get(aEXStatement);

            if (stc != null)
                throw new RuntimeException("Statement already in use! Statement = " + s);

            synchronized (statementInfoMap)
            {
                statementInfoMap.put(aEXStatement, new StackTraceCollection(aThread));
            }

            // Here definitely this would be a ExtendedConnection
            addCreatedTime(aPoolName, aConnection, aEXStatement.getCreatedTime(), IDGenerator.STATEMENT_PREFIX);
        }
        catch (final Throwable t)
        {
            log.error("Exception in adding Statment " + s, t);
        }
    }

    public void removeStatement(
            String aPoolName,
            ExtendedConnection aConnection,
            ExtendedStatement aEXStatement)
    {
        final String s = "Connection Pool '" + aPoolName + "' Connection '" + aConnection + "' Statement '" + aEXStatement + "'";

        if (log.isDebugEnabled())
            log.debug("Removing Statement " + s);

        try
        {
            final Map<ExtendedConnection, Map<ExtendedStatement, StackTraceCollection>> connInfo = statementCol.get(aPoolName);

            if (connInfo != null)
            {
                final Map<ExtendedStatement, StackTraceCollection> stmtInfo = connInfo.get(aConnection);

                if (stmtInfo != null)
                {

                    synchronized (stmtInfo)
                    {
                        stmtInfo.remove(aEXStatement);
                    }

                    removeCreatedTime(aPoolName, aEXStatement, IDGenerator.STATEMENT_PREFIX);

                    if (stmtInfo.size() == 0)
                    {

                        synchronized (connInfo)
                        {
                            connInfo.remove(aConnection);
                        }

                        if (connInfo.size() == 0)
                            synchronized (statementCol)
                            {
                                statementCol.remove(aPoolName);
                            }
                    }
                }
            }
        }
        catch (final Throwable t)
        {
            log.error("Exception in removing statement " + s, t);
        }
    }

    public void addPreparedStatment(
            String aPoolName,
            ExtendedConnection aConnection,
            ExtendedPreparedStatement aEXPreparedStatement,
            Thread aThread)
    {
        final String s = "Connection Pool '" + aPoolName + "' Connection '" + aConnection + "' Statement '" + aEXPreparedStatement + "'";

        if (log.isDebugEnabled())
            log.debug("Adding Prepared Statement " + s);

        try
        {
            Map<ExtendedConnection, Map<ExtendedPreparedStatement, StackTraceCollection>> connInfoMap = prepareStatementCol.get(aPoolName);

            if (connInfoMap == null)
            {
                connInfoMap = new Hashtable<>();

                synchronized (prepareStatementCol)
                {
                    prepareStatementCol.put(aPoolName, connInfoMap);
                }
            }

            Map<ExtendedPreparedStatement, StackTraceCollection> statementInfoMap = connInfoMap.get(aConnection);

            if (statementInfoMap == null)
            {
                statementInfoMap = new Hashtable<>();

                synchronized (connInfoMap)
                {
                    connInfoMap.put(aConnection, statementInfoMap);
                }
            }

            final StackTraceCollection stc = statementInfoMap.get(aEXPreparedStatement);

            if (stc != null)
                throw new RuntimeException("PreparedStatement already in use! Statement = " + aEXPreparedStatement);

            synchronized (statementInfoMap)
            {
                statementInfoMap.put(aEXPreparedStatement, new StackTraceCollection(aThread));
            }
            addCreatedTime(aPoolName, aConnection, aEXPreparedStatement.getCreatedTime(), IDGenerator.PREPARED_STATEMENT_PREFIX);
        }
        catch (final Throwable t)
        {
            log.error("Exception in adding PreparedStatment " + s, t);
        }
    }

    public void removePreparedStatement(
            String aPoolName,
            ExtendedConnection aConnection,
            ExtendedPreparedStatement aEXPreparedStatement)
    {
        final String s = "Connection Pool '" + aPoolName + "' Connection '" + aConnection + "' Statement '" + aEXPreparedStatement + "'";

        if (log.isDebugEnabled())
            log.debug("Removing Prepared Statement " + s);

        try
        {
            final Map<ExtendedConnection, Map<ExtendedPreparedStatement, StackTraceCollection>> connInfo = prepareStatementCol.get(aPoolName);

            if (connInfo != null)
            {
                final Map<ExtendedPreparedStatement, StackTraceCollection> stmtInfo = connInfo.get(aConnection);

                if (stmtInfo != null)
                {

                    synchronized (stmtInfo)
                    {
                        stmtInfo.remove(aEXPreparedStatement);
                    }

                    removeCreatedTime(aPoolName, aEXPreparedStatement, IDGenerator.PREPARED_STATEMENT_PREFIX);

                    if (stmtInfo.size() == 0)
                    {

                        synchronized (connInfo)
                        {
                            connInfo.remove(aConnection);
                        }

                        if (connInfo.size() == 0)
                            synchronized (prepareStatementCol)
                            {
                                prepareStatementCol.remove(aPoolName);
                            }
                    }
                }
            }
        }
        catch (final Throwable t)
        {
            log.error("Exception in removing PreparedStatement " + s, t);
        }
    }

    public void addCallableStatment(
            String aPoolName,
            ExtendedConnection aConnection,
            ExtendedCallableStatement aEXCallableStatement,
            Thread aThread)
    {
        final String s = "Connection Pool '" + aPoolName + "' Connection '" + aConnection + "' Statement '" + aEXCallableStatement + "'";

        if (log.isDebugEnabled())
            log.debug("Adding Callable Statement " + s);

        try
        {
            Map<ExtendedConnection, Map<ExtendedCallableStatement, StackTraceCollection>> connInfoMap = callableStatementCol.get(aPoolName);

            if (connInfoMap == null)
            {
                connInfoMap = new Hashtable<>();

                synchronized (callableStatementCol)
                {
                    callableStatementCol.put(aPoolName, connInfoMap);
                }
            }

            Map<ExtendedCallableStatement, StackTraceCollection> statementInfoMap = connInfoMap.get(aConnection);

            if (statementInfoMap == null)
            {
                statementInfoMap = new Hashtable<>();

                synchronized (connInfoMap)
                {
                    connInfoMap.put(aConnection, statementInfoMap);
                }
            }

            final StackTraceCollection stc = statementInfoMap.get(aEXCallableStatement);

            if (stc == null)
                throw new RuntimeException("CallableStatement already in use! Statement = " + aEXCallableStatement);

            synchronized (statementInfoMap)
            {
                statementInfoMap.put(aEXCallableStatement, new StackTraceCollection(aThread));
            }

            addCreatedTime(aPoolName, aConnection, aEXCallableStatement.getCreatedTime(), IDGenerator.CALLABLE_STATEMENT_PREFIX);
        }
        catch (final Throwable t)
        {
            log.error("Exception in adding CallableStatment " + s, t);
        }
    }

    public void removeCallableStatement(
            String aPoolName,
            ExtendedConnection aConnection,
            ExtendedCallableStatement aEXCallableStatement)
    {
        final String s = "Connection Pool '" + aPoolName + "' Connection '" + aConnection + "' Statement '" + aEXCallableStatement + "'";

        if (log.isDebugEnabled())
            log.debug("Removing Callable Statement " + s);

        try
        {
            final Map<ExtendedConnection, Map<ExtendedCallableStatement, StackTraceCollection>> connInfo = callableStatementCol.get(aPoolName);

            if (connInfo != null)
            {
                final Map<ExtendedCallableStatement, StackTraceCollection> stmtInfo = connInfo.get(aConnection);

                if (stmtInfo != null)
                {

                    synchronized (stmtInfo)
                    {
                        stmtInfo.remove(aEXCallableStatement);
                    }
                    removeCreatedTime(aPoolName, aEXCallableStatement, IDGenerator.CALLABLE_STATEMENT_PREFIX);

                    if (stmtInfo.size() == 0)
                    {

                        synchronized (connInfo)
                        {
                            connInfo.remove(aConnection);
                        }

                        if (connInfo.size() == 0)
                            synchronized (callableStatementCol)
                            {
                                callableStatementCol.remove(aPoolName);
                            }
                    }
                }
            }
        }
        catch (final Throwable t)
        {
            log.error("Exception in removing CallableStatement " + s, t);
        }
    }

    public String getCallableStatementTrace(
            int aHoldTimeInMinutes)
    {
        final StringBuilder sb = new StringBuilder();

        try
        {
            sb.append("<CallableStatementTracker>");

            if (callableStatementCol.size() > 0)
            {
                final long offSet = System.currentTimeMillis() - getOffSetTime(aHoldTimeInMinutes);

                for (final String connPoolName : callableStatementCol.keySet())
                {
                    sb.append("<ConnectionPool PoolName=\'" + connPoolName + "\' >");

                    final Map<ExtendedConnection, Map<ExtendedCallableStatement, StackTraceCollection>> connInfo = callableStatementCol.get(connPoolName);

                    for (final ExtendedConnection conn : connInfo.keySet())
                    {
                        sb.append("<Connection>");
                        sb.append("<ID ").append(conn.getId()).append(" />");

                        final Map<ExtendedCallableStatement, StackTraceCollection> stmtInfo = connInfo.get(conn);

                        for (final ExtendedCallableStatement statement : stmtInfo.keySet())
                            if (statement.getCreatedTime() <= offSet)
                            {
                                sb.append("<Statement>");
                                sb.append("<ID ").append(statement.getId()).append(" />");
                                sb.append("<StackInfo>").append(stmtInfo.get(statement).getXML()).append("</StackInfo>");
                                sb.append("</Statement>");
                            }
                        sb.append("</Connection>");
                    }
                    sb.append("</ConnectionPool>");
                }
            }

            sb.append("</CallableStatementTracker>");
        }
        catch (final Throwable t)
        {
            log.error("Exception in getCallableStatementTrace", t);
        }
        return sb.toString();
    }

    public String getPreparedStatementTrace(
            int aHoldTimeInMinutes)
    {
        final StringBuilder sb = new StringBuilder();

        try
        {
            sb.append("<PreparedStatementTracker>");

            if (prepareStatementCol.size() > 0)
            {
                final long offSet = System.currentTimeMillis() - getOffSetTime(aHoldTimeInMinutes);

                for (final String connPoolName : prepareStatementCol.keySet())
                {
                    sb.append("<ConnectionPool PoolName=\'" + connPoolName + "\' >");

                    final Map<ExtendedConnection, Map<ExtendedPreparedStatement, StackTraceCollection>> connInfo = prepareStatementCol.get(connPoolName);

                    for (final ExtendedConnection conn : connInfo.keySet())
                    {
                        sb.append("<Connection>");
                        sb.append("<ID ").append(conn.getId()).append(" />");
                        final Map<ExtendedPreparedStatement, StackTraceCollection> stmtInfo = connInfo.get(conn);

                        for (final ExtendedPreparedStatement statement : stmtInfo.keySet())
                            if (statement.getCreatedTime() <= offSet)
                            {
                                sb.append("<Statement>");
                                sb.append("<ID ").append(statement.getId()).append(" />");
                                sb.append("<StackInfo>").append(stmtInfo.get(statement).getXML()).append("</StackInfo>");
                                sb.append("</Statement>");
                            }
                        sb.append("</Connection>");
                    }
                    sb.append("</ConnectionPool>");
                }
            }

            sb.append("</PreparedStatementTracker>");
        }
        catch (final Exception e)
        {
            log.error("Exception in getPreparedStatementTrace", e);
        }
        catch (final Throwable t)
        {
            log.error("Exception in getPreparedStatementTrace", t);
        }
        return sb.toString();
    }

    public String getStatementTrace(
            int aHoldTimeInMinutes)
    {
        final StringBuilder sb = new StringBuilder();

        try
        {
            sb.append("<StatementTracker>");

            if (statementCol.size() > 0)
            {
                final long offSet = System.currentTimeMillis() - getOffSetTime(aHoldTimeInMinutes);

                for (final String connPoolName : statementCol.keySet())
                {
                    sb.append("<ConnectionPool PoolName=\'" + connPoolName + "\' >");

                    final Map<ExtendedConnection, Map<ExtendedStatement, StackTraceCollection>> connInfo = statementCol.get(connPoolName);

                    for (final Entry<ExtendedConnection, Map<ExtendedStatement, StackTraceCollection>> entry : connInfo.entrySet())
                    {
                        final ExtendedConnection conn = entry.getKey();
                        sb.append("<Connection>");
                        sb.append("<ID ").append(conn.getId()).append(" />");

                        final Map<ExtendedStatement, StackTraceCollection> stmtInfo = entry.getValue();

                        for (final ExtendedStatement statement : stmtInfo.keySet())
                            if (statement.getCreatedTime() <= offSet)
                            {
                                sb.append("<Statement>");
                                sb.append("<ID ").append(statement.getId()).append(" />");
                                sb.append("<StackInfo>").append(stmtInfo.get(statement).getXML()).append("</StackInfo>");
                                sb.append("</Statement>");
                            }
                        sb.append("</Connection>");
                    }
                    sb.append("</ConnectionPool>");
                }
            }

            sb.append("</StatementTracker>");
        }
        catch (final Exception e)
        {
            log.error("Exception in getStatementTrace", e);
        }
        catch (final Throwable t)
        {
            log.error("Exception in getStatementTrace", t);
        }

        return sb.toString();
    }

    public void addResultset(
            String aPoolName,
            ExtendedConnection aConnection,
            Statement aStatement,
            ExtendedResultSet aResultSet,
            long aCreatedTime,
            Thread aCalledBy)
    {
        Map<ExtendedConnection, Map<Statement, Map<ExtendedResultSet, StackTraceCollection>>> connectionMap = resltSetCol.get(aPoolName);

        if (connectionMap == null)
        {
            connectionMap = new Hashtable<>();
            resltSetCol.put(aPoolName, connectionMap);
        }
        Map<Statement, Map<ExtendedResultSet, StackTraceCollection>> statementMap = connectionMap.get(aConnection);

        if (statementMap == null)
        {
            statementMap = new Hashtable<>();
            connectionMap.put(aConnection, statementMap);
        }

        Map<ExtendedResultSet, StackTraceCollection> resultSetMap = statementMap.get(aStatement);

        if (resultSetMap == null)
        {
            resultSetMap = new Hashtable<>();
            statementMap.put(aStatement, resultSetMap);
        }

        final StackTraceCollection stackTraceCollection = resultSetMap.get(aResultSet);

        if (stackTraceCollection == null)
            resultSetMap.put(aResultSet, new StackTraceCollection(aCalledBy));
        else
            log.error("");
    }

    public void removeResultset(
            String aPoolName,
            ExtendedConnection aConnection,
            Statement aStatement,
            ExtendedResultSet aResultSet)
    {
        final Map<ExtendedConnection, Map<Statement, Map<ExtendedResultSet, StackTraceCollection>>> connectionMap = resltSetCol.get(aPoolName);

        if (connectionMap != null)
        {
            final Map<Statement, Map<ExtendedResultSet, StackTraceCollection>> statementMap = connectionMap.get(aConnection);

            if (statementMap != null)
            {
                final Map<ExtendedResultSet, StackTraceCollection> resultSetMap = statementMap.get(aStatement);

                if (resultSetMap != null)
                    resultSetMap.remove(aResultSet);
            }
        }
    }

    public String getAllTrace()
    {
        return getAllTrace(0);
    }

    public String getAllTrace(
            int aHoldTimeInMinutes)
    {
        final StringBuilder sb = new StringBuilder();

        try
        {
            sb.append("<AllTrace>");
            sb.append(getConnectionTrace(aHoldTimeInMinutes));
            sb.append(getStatementTrace(aHoldTimeInMinutes));
            sb.append(getPreparedStatementTrace(aHoldTimeInMinutes));
            sb.append(getCallableStatementTrace(aHoldTimeInMinutes));
            sb.append("</AllTrace>");
        }
        catch (final Throwable t)
        {
            if (log.isDebugEnabled())
                log.debug("Exception in getAllTrace", t);
        }
        return sb.toString();
    }

    public String getConnectionTrace()
    {
        return getConnectionTrace(0);
    }

    public String getConnectionTrace(
            int aHoldTimeInMinutes)
    {
        final StringBuilder sb = new StringBuilder();

        try
        {
            sb.append("<ConnectionTracker>");

            final long offSet = System.currentTimeMillis() - getOffSetTime(aHoldTimeInMinutes);

            for (final String connPoolName : connPoolList.keySet())
            {
                sb.append("<ConnectionPool PoolName=\'" + connPoolName + "\' CurrentCount=\'");

                final Map<ExtendedConnection, StackTraceCollection> connInfo = connPoolList.get(connPoolName);
                sb.append(connInfo.size()).append("\'>");

                final Map<String, Long> connTimeInfo = connPoolTimeDetailsList.get(connPoolName);

                for (final ExtendedConnection con : connInfo.keySet())
                {
                    final Long lastAccessed = connTimeInfo.get(IDGenerator.CONNECTION_PREFIX + con.toString());

                    if (lastAccessed.longValue() <= offSet)
                    {
                        sb.append("<Connection>");
                        sb.append("<ID ").append(con.getId()).append("/>");
                        sb.append("<StackInfo>").append(connInfo.get(con).getXML()).append("</StackInfo>");
                        sb.append("</Connection>");
                    }
                }
                sb.append("</ConnectionPool>");
            }
            sb.append("</ConnectionTracker>");
        }
        catch (final Throwable t)
        {
            log.error("Exception in getConnectionTrace", t);
        }
        return sb.toString();
    }

    private static long getOffSetTime(
            int aHoldTimeInMinutes)
    {
        return (aHoldTimeInMinutes * 60 * 1000);
    }

    private void addCreatedTime(
            String aPoolName,
            Object aObject,
            long aTimestamp,
            String aObjectType)
    {
        Map<String, Long> connTimeInfo = connPoolTimeDetailsList.get(aPoolName);

        if (connTimeInfo == null)
        {
            connTimeInfo = new Hashtable<>();
            connPoolTimeDetailsList.put(aPoolName, connTimeInfo);
        }

        final String temp = aObjectType + aObject.toString();
        connTimeInfo.compute(temp, (
                k,
                v) -> v = aTimestamp);
    }

    private void removeCreatedTime(
            String aPoolName,
            Object aObject,
            String aObjectType)
    {
        final Map<String, Long> connTimeInfo = connPoolTimeDetailsList.get(aPoolName);

        if (connTimeInfo != null)
        {

            synchronized (connTimeInfo)
            {
                connTimeInfo.remove(aObjectType + aObject.toString());
            }

            if (connTimeInfo.size() == 0)
                synchronized (connPoolTimeDetailsList)
                {
                    connPoolTimeDetailsList.remove(aPoolName);
                }
        }
    }

}