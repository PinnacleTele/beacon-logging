package com.itextos.beacon.smpp.dboperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.platform.smpputil.ISmppInfo;
import com.itextos.beacon.smpp.objects.bind.BindInfoValid;
import com.itextos.beacon.smpp.objects.bind.UnbindInfo;
import com.itextos.beacon.smpp.utils.ItextosSmppUtil;

public class DbBindOperation
{

    private static final Log    log                       = LogFactory.getLog(DbBindOperation.class);

    private static final String INSERT_BIND_INFO          = "insert into smpp_bind_info (" //
            + " bind_id, system_id, cli_id," //
            + " bind_mode, bind_date, bind_time," //
            + " server_ip, server_port, source_ip," //
            + " server_instance, thread_name" //
            + ") values (" //
            + "?, ?, ?," //
            + " ?, ?, ?," //
            + " ?, ?, ?," //
            + " ?, ? )";

    private static final String INSERT_UNBIND_INFO        = "insert into smpp_bind_info_log (" //
            + " bind_id, system_id, cli_id," //
            + " bind_mode, bind_date, bind_time," //
            + " server_ip, server_port, source_ip," //
            + " server_instance, thread_name, unbind_time, error_code, reason" //
            + ") values (" //
            + "?, ?, ?," //
            + " ?, ?, ?," //
            + " ?, ?, ?," //
            + " ?, ? ,  now(), ?, ?)";

    private static final String DELETE_BIND_INFO          = "delete from smpp_bind_info where bind_id=? and bind_mode=? and server_instance=? and cli_id=?";
    private static final String INSTANCE_DELETE_BIND_INFO = "delete from smpp_bind_info where server_instance=?";
    private static final String SELECT_AND_INSERT         = "insert into smpp_bind_info_log "
            + "(bind_id, system_id, cli_id, bind_mode, bind_date, bind_time, server_ip, server_port, source_ip, server_instance, thread_name, error_code, reason, unbind_time) "
            + "select bind_id, system_id, cli_id, bind_mode, bind_date, bind_time, server_ip, server_port, source_ip, server_instance, thread_name, '0001','OnStartupMovingToLog', now() from smpp_bind_info where server_instance=?";

    private DbBindOperation()
    {}

    public static void insertBindInfo(
            List<ISmppInfo> aBindIngoList)
            throws Exception
    {
        Connection        lSqlConn      = null;
        PreparedStatement lPreepareStmt = null;

        try
        {
            lSqlConn      = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.LOGGING.getKey()));
            lPreepareStmt = lSqlConn.prepareStatement(INSERT_BIND_INFO);

            lSqlConn.setAutoCommit(false);
            insertBindRecords(lPreepareStmt, aBindIngoList);
            lSqlConn.commit();
        }
        catch (final Exception e)
        {
            log.error("Excception while inserting the data into 'smpp_bind_info'", e);
            CommonUtility.rollbackConnection(lSqlConn);

            final String s = "Excception while inserting the data into 'smpp_bind_info'";
            log.error(s, e);
            throw new ItextosException(s, e);
        }
        finally
        {
            CommonUtility.closeStatement(lPreepareStmt);
            CommonUtility.closeConnection(lSqlConn);
        }
    }

    public static void clearSmppBindInfo(
            String aInstanceId)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("clearSmppBindInfo() - ServerInstanceId -" + aInstanceId + "'");

        Connection        lSqlConn      = null;
        PreparedStatement lPreepareStmt = null;

        try
        {
            lSqlConn = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.LOGGING.getKey()));
            lSqlConn.setAutoCommit(false);

            lPreepareStmt = lSqlConn.prepareStatement(SELECT_AND_INSERT);
            lPreepareStmt.setString(1, aInstanceId);
            final int count = lPreepareStmt.executeUpdate();

            if (count > 0)
            {
                if (log.isDebugEnabled())
                    log.debug(aInstanceId + " clearSmppBindInfo() record inserted successfully");

                deleteBindAbortRequest(lSqlConn, aInstanceId);
            }
            else
                log.info(aInstanceId + " insertBindAbortRequest() No records to be inserting in smpp_bind_info_log table." + count);

            lSqlConn.commit();
        }
        catch (final Exception e)
        {
            log.error("Excception while inserting the data into 'smpp_bind_info_log'", e);
            CommonUtility.rollbackConnection(lSqlConn);

            final String s = "Excception while inserting the data into 'smpp_bind_info_log'";
            log.error(s, e);
            throw new ItextosException(s, e);
        }
        finally
        {
            CommonUtility.closeStatement(lPreepareStmt);
            CommonUtility.closeConnection(lSqlConn);
        }
    }

    public static void insertUnBindInfo(
            List<ISmppInfo> aUnBindInfoDbList)
            throws Exception
    {
        Connection        lSqlConn      = null;
        PreparedStatement lPreepareStmt = null;

        try
        {
            lSqlConn      = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.LOGGING.getKey()));
            lPreepareStmt = lSqlConn.prepareStatement(INSERT_UNBIND_INFO);

            lSqlConn.setAutoCommit(false);
            insertUnBindRecords(lPreepareStmt, aUnBindInfoDbList);

            lSqlConn.commit();

            deleteBindInfo(aUnBindInfoDbList);
        }
        catch (final Exception e)
        {
            log.error("Excception while inserting the data into 'smpp_bind_info_log'", e);
            CommonUtility.rollbackConnection(lSqlConn);

            final String s = "Excception while inserting the data into 'smpp_bind_info_log'";
            log.error(s, e);
            throw new ItextosException(s, e);
        }
        finally
        {
            CommonUtility.closeStatement(lPreepareStmt);
            CommonUtility.closeConnection(lSqlConn);
        }
    }

    public static void insertInvalidUnBindInfo(
            List<ISmppInfo> aUnBindInfoDbList)
    {
        // TODO
    }

    public static void deleteBindInfo(
            List<ISmppInfo> aUnBindInfoDbList)
            throws Exception

    {
        Connection        lSqlConn      = null;
        PreparedStatement lPreepareStmt = null;

        try
        {
            lSqlConn      = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.LOGGING.getKey()));
            lPreepareStmt = lSqlConn.prepareStatement(DELETE_BIND_INFO);

            lSqlConn.setAutoCommit(false);
            deleteBindRecords(lPreepareStmt, aUnBindInfoDbList);
            lSqlConn.commit();
        }
        catch (final Exception e)
        {
            log.error("Excception while deleteing the data into 'smpp_bind_info'", e);
            CommonUtility.rollbackConnection(lSqlConn);

            final String s = "Excception while deleteing the data into 'smpp_bind_info'";
            log.error(s, e);
            throw new ItextosException(s, e);
        }
        finally
        {
            CommonUtility.closeStatement(lPreepareStmt);
            CommonUtility.closeConnection(lSqlConn);
        }
    }

    private static void insertBindRecords(
            PreparedStatement aPstmt,
            List<ISmppInfo> aISmppInfo)
            throws SQLException
    {

        for (final ISmppInfo aSmppInfo : aISmppInfo)
        {
            final BindInfoValid lBindInfo = (BindInfoValid) aSmppInfo;

            aPstmt.setString(1, CommonUtility.nullCheck(lBindInfo.getBindId(), true));
            aPstmt.setString(2, lBindInfo.getSystemId());
            aPstmt.setString(3, lBindInfo.getClientId());
            aPstmt.setString(4, ItextosSmppUtil.getBindName(lBindInfo.getBindType()));
            aPstmt.setString(5, lBindInfo.getBindDate());
            aPstmt.setString(6, lBindInfo.getBindTime());
            aPstmt.setString(7, lBindInfo.getServerIp());
            aPstmt.setInt(8, lBindInfo.getServerPort());
            aPstmt.setString(9, lBindInfo.getSourceIp());
            aPstmt.setString(10, lBindInfo.getInstanceId());
            aPstmt.setString(11, lBindInfo.getThreadName());

            aPstmt.addBatch();
        }

        aPstmt.executeBatch();
    }

    private static void insertUnBindRecords(
            PreparedStatement aPstmt,
            List<ISmppInfo> aISmppInfo)
            throws SQLException
    {

        for (final ISmppInfo aSmppInfo : aISmppInfo)
        {
            final UnbindInfo lUnBindInfo = (UnbindInfo) aSmppInfo;

            aPstmt.setString(1, CommonUtility.nullCheck(lUnBindInfo.getBindId(), true));
            aPstmt.setString(2, lUnBindInfo.getSystemId());
            aPstmt.setString(3, lUnBindInfo.getClientId());
            aPstmt.setString(4, ItextosSmppUtil.getBindName(lUnBindInfo.getBindType()));
            aPstmt.setString(5, lUnBindInfo.getBindDate());
            aPstmt.setString(6, lUnBindInfo.getBindTime());
            aPstmt.setString(7, lUnBindInfo.getServerIp());
            aPstmt.setInt(8, lUnBindInfo.getServerPort());
            aPstmt.setString(9, lUnBindInfo.getSourceIp());
            aPstmt.setString(10, lUnBindInfo.getInstanceId());
            aPstmt.setString(11, lUnBindInfo.getThreadName());
            aPstmt.setString(12, CommonUtility.nullCheck(lUnBindInfo.getErrorcode()));
            aPstmt.setString(13, CommonUtility.nullCheck(lUnBindInfo.getReason()));

            aPstmt.addBatch();
        }

        aPstmt.executeBatch();
    }

    private static void deleteBindRecords(
            PreparedStatement aPstmt,
            List<ISmppInfo> aISmppInfo)
            throws SQLException
    {

        for (final ISmppInfo aSmppInfo : aISmppInfo)
        {
            final UnbindInfo lUnBindInfo = (UnbindInfo) aSmppInfo;

            aPstmt.setString(1, CommonUtility.nullCheck(lUnBindInfo.getBindId(), true));
            aPstmt.setString(2, ItextosSmppUtil.getBindName(lUnBindInfo.getBindType()));
            aPstmt.setString(3, lUnBindInfo.getInstanceId());
            aPstmt.setString(4, lUnBindInfo.getClientId());

            aPstmt.addBatch();
        }

        aPstmt.executeBatch();
    }

    private static void deleteBindAbortRequest(
            Connection aSqlConn,
            String aServerInstanceId)
            throws Exception
    {
        PreparedStatement   lPreepareStmt = null;
        final StringBuilder sqlQuery      = new StringBuilder();

        try
        {
            lPreepareStmt = aSqlConn.prepareStatement(INSTANCE_DELETE_BIND_INFO);
            lPreepareStmt.setString(1, aServerInstanceId);

            lPreepareStmt.executeUpdate();

            if (log.isDebugEnabled())
                log.debug(aServerInstanceId + " deleteBindAbortRequest() record deleted successfully from SMPP_BIND_INFO");
        }
        catch (final Exception e)
        {
            log.error(aServerInstanceId + " deleteBindAbortRequest() Exception while inserting unbind request" + "for tracing", e);
            throw e;
        }
        finally
        {
            CommonUtility.closeStatement(lPreepareStmt);
        }
    }

}