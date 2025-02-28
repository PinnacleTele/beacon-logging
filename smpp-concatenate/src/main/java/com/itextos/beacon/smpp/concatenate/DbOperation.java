package com.itextos.beacon.smpp.concatenate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.smpp.objects.request.SmppMessageRequest;

class DbOperation
{

    private static final Log    log        = LogFactory.getLog(DbOperation.class);
    private static final String MOD_VALUE  = System.getProperty("modvalue"); // valid values are 0,1,2,3

    private static final String INSERT_SQL = "insert into smpp_concat_all (cluster,message_payload) values (?,?)";
    private static final String DELETE_SQL = "delete from smpp_concat_all where sno=?";
    private static final String SELECT_SQL = "select sno, message_payload from smpp_concat_all where cluster=? and mod(sno,4) in (" + MOD_VALUE + ") limit 500";

    private DbOperation()
    {
        if ((MOD_VALUE == null) || MOD_VALUE.isBlank()) {
        	log.error("Invalid Modvalue set in Runtime...' System going down" + MOD_VALUE + "'");
        	System.exit(-1);
         //   throw new ItextosRuntimeException("Invalid Modvalue set in Runtime...'" + MOD_VALUE + "'");
        }
        {
            if (log.isDebugEnabled())
                log.debug("Modvalues passed '" + MOD_VALUE + "'");

            final String[] mods = MOD_VALUE.split(",");

            for (final String s : mods)
            {
                final int mod = CommonUtility.getInteger(s, -999);
                if ((mod == -999) || (mod >= 4)) {
                   // throw new ItextosRuntimeException("Invalid Modvalue set in Runtime...'" + MOD_VALUE + "'");
                	log.error("Invalid Modvalue set in Runtime... System going to down" + MOD_VALUE + "'");
                	System.exit(-1);
                }
            }
        }
    }

    static void dbInsert(
            List<SmppMessageRequest> aList)
            throws Exception
    {
        Connection        con   = null;
        PreparedStatement pstmt = null;

        try
        {
            con   = getConnection();
            pstmt = con.prepareStatement(INSERT_SQL);

            con.setAutoCommit(false);

            for (final SmppMessageRequest smr : aList)
            {
                pstmt.setString(1, smr.getCluster());
                pstmt.setString(2, smr.getJsonString());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            con.commit();
        }
        catch (final Exception e)
        {
            log.error("", e);

            CommonUtility.rollbackConnection(con);

            throw e;
        }
        finally
        {
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
        }
    }

    static Map<Long, String> getMessageFromDb(
            String aCluster)
    {
        final Map<Long, String> messsageFromDb = new HashMap<>();

        Connection              con            = null;
        PreparedStatement       pstmt          = null;
        ResultSet               rs             = null;

        try

        {
            con   = getConnection();
            pstmt = con.prepareStatement(SELECT_SQL);
            pstmt.setString(1, aCluster);

            rs = pstmt.executeQuery();

            while (rs.next())
                messsageFromDb.put(rs.getLong(1), rs.getString(2));
        }
        catch (final Exception e)
        {
            log.error("", e);
        }
        finally
        {
            CommonUtility.closeResultSet(rs);
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
        }

        return messsageFromDb;
    }

    static void deleteFromDb(
            List<Long> aAddedMessageSeqNo)
    {
        Connection        con   = null;
        PreparedStatement pstmt = null;

        try
        {
            con   = getConnection();
            pstmt = con.prepareStatement(DELETE_SQL);

            con.setAutoCommit(false);

            for (final Long seqNo : aAddedMessageSeqNo)
            {
                pstmt.setLong(1, seqNo);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            con.commit();
        }
        catch (final Exception e)
        {
            log.error("", e);

            CommonUtility.rollbackConnection(con);
        }
        finally
        {
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
        }
    }

    private static Connection getConnection()
            throws Exception
    {
        JndiInfoHolder.getInstance();
        return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.MESSAGING.getKey()));
    }

}