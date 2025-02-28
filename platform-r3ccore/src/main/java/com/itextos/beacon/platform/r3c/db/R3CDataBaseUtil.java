package com.itextos.beacon.platform.r3c.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.platform.r3c.util.R3CUtil;
import com.itextos.beacon.platform.r3c.util.VLRepository;

public class R3CDataBaseUtil
{

    private static final Log    log                        = LogFactory.getLog(R3CDataBaseUtil.class);

    private static final String INSERT_MYSQL               = "M";
    private static final String INSERT_POSTGRES            = "P";
    private static final String INSERT_POSTGRES_BINARY     = "PB";

    private static final String POSTGRES_INSERT_SQL        = "insert into r3c (cli_id, file_id, base_msg_id, dest, shortner_id, additional_info, expiry_date) values (?,?,?,?,?,?::jsonb,?)";
    private static final String POSTGRES_INSERT_SQL_BINARY = "insert into r3c_jsonb ( shortner_id, json_data, expiry_date) values (?,?::jsonb,?)";

    private R3CDataBaseUtil()
    {}

    private static final String INSERT_SQL = "insert into {0} (cli_id, file_id, base_msg_id, dest, shortner_id, additional_info, expiry_date) values (?,?,?,?,?,?,?)";

    public static boolean insertR3CIntoDB(
            JSONObject aAddInfoObj,
            VLRepository aVlRepository,
            String aTableName)
            throws Exception
    {
        boolean           insertStatus = false;

        Connection        lConn        = null;
        PreparedStatement lPstmt       = null;

        try
        {
            final String lSql = MessageFormat.format(INSERT_SQL, aTableName);

            if (log.isDebugEnabled())
                log.debug("Sql Query : " + lSql);

            final String R3C_JNDI_INFO = R3CUtil.getAppConfigValueAsString(ConfigParamConstants.R3C_JNDI_INFO);

            lConn = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfo(CommonUtility.getInteger(R3C_JNDI_INFO, 9)));
            lConn.setAutoCommit(false);

            lPstmt = lConn.prepareStatement(lSql);
            lPstmt.setString(1, aVlRepository.getClientId());
            lPstmt.setString(2, aVlRepository.getFileId());
            lPstmt.setString(3, aVlRepository.getMid());
            lPstmt.setString(4, aVlRepository.getMobileNumber());
            lPstmt.setString(5, aVlRepository.getShortCode());
            lPstmt.setString(6, aAddInfoObj.toJSONString());
            lPstmt.setTimestamp(7, new Timestamp(aVlRepository.getExpiryDate().getTime()));

            lPstmt.execute();
            lConn.commit();

            insertStatus = true;
        }
        catch (final SQLException exp)
        {
            CommonUtility.rollbackConnection(lConn);

            if (exp.getErrorCode() == 2601)
            {
                log.error("Duplicate Short code '" + aVlRepository.getShortCode() + "' present in table '" + aTableName + "'", exp);
                insertStatus = true;
            }
            else
            {
                log.error("Problem inserting payload to mysql...", exp);
                throw exp;
            }
        }
        catch (final Exception e)
        {
            log.error("Problem inserting payload to mysql...", e);
            CommonUtility.rollbackConnection(lConn);

            throw e;
        }
        finally
        {
            CommonUtility.closeStatement(lPstmt);
            CommonUtility.closeConnection(lConn);
        }
        return insertStatus;
    }

    public static boolean insertIntoDb(
            VLRepository aVLRepository,
            JSONObject aAddInfo)
    {
        final String insertType = R3CUtil.getAppConfigValueAsString(ConfigParamConstants.R3C_INSERT_TYPE);

        switch (insertType)
        {
            case INSERT_POSTGRES:
                return insertIntoPostgres(aVLRepository, aAddInfo);

            case INSERT_POSTGRES_BINARY:
                return insertIntoPostgresBinary(aVLRepository, aAddInfo);

            case INSERT_MYSQL:
            default:
                return insertIntoMySql(aVLRepository, aAddInfo);
        }
    }

    private static boolean insertIntoPostgresBinary(
            VLRepository aVlRepository,
            JSONObject aAddInfo)
    {
        boolean   isInsertSuccess   = false;
        int       lRetryCount       = 0;
        final int lConfigRetryCount = CommonUtility.getInteger(R3CUtil.getAppConfigValueAsString(ConfigParamConstants.R3C_MAX_ELASTIC_RETRY_COUNT), 3);

        while (!isInsertSuccess)
        {
            if (lRetryCount > lConfigRetryCount)
                break;

            log.info("Calling DB insertion. Retry count " + lRetryCount);

            Connection        con   = null;
            PreparedStatement pstmt = null;

            try
            {
                if (log.isDebugEnabled())
                    log.debug("Sql Query : " + POSTGRES_INSERT_SQL_BINARY);

                final String R3C_JNDI_INFO = R3CUtil.getAppConfigValueAsString(ConfigParamConstants.R3C_JNDI_INFO);

                con = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfo(CommonUtility.getInteger(R3C_JNDI_INFO, 9)));
                con.setAutoCommit(false);

                pstmt = con.prepareStatement(POSTGRES_INSERT_SQL_BINARY);

                aAddInfo.put("cli_id", aVlRepository.getClientId());
                aAddInfo.put("file_id", aVlRepository.getFileId());
                aAddInfo.put("base_msg_id", aVlRepository.getMid());
                aAddInfo.put("dest", aVlRepository.getMobileNumber());
                aAddInfo.put("id", aVlRepository.getId());
                aAddInfo.put("short_code", aVlRepository.getShortCode());

                pstmt.setString(1, aVlRepository.getShortCode());
                pstmt.setString(2, aAddInfo.toJSONString());
                pstmt.setTimestamp(3, new Timestamp(aVlRepository.getExpiryDate().getTime()));

                pstmt.execute();
                con.commit();

                isInsertSuccess = true;
                log.info("Database insertion status - " + isInsertSuccess);
            }
            catch (final Exception e)
            {
                if (con != null)
                    CommonUtility.rollbackConnection(con);
                log.error("Exception while inserting into DB .", e);
            }
            finally
            {
                CommonUtility.closeStatement(pstmt);
                CommonUtility.closeConnection(con);
            }

            if (!isInsertSuccess)
                CommonUtility.sleepForAWhile();

            lRetryCount++;
        }

        if (!isInsertSuccess)
            log.fatal("Unable to insert record into datatbase. VL info " + aVlRepository + " Additional Info " + aAddInfo);

        return isInsertSuccess;
    }

    private static boolean insertIntoPostgres(
            VLRepository aVlRepository,
            JSONObject aAddInfo)
    {
        boolean   isInsertSuccess   = false;
        int       lRetryCount       = 0;
        final int lConfigRetryCount = CommonUtility.getInteger(R3CUtil.getAppConfigValueAsString(ConfigParamConstants.R3C_MAX_ELASTIC_RETRY_COUNT), 3);

        while (!isInsertSuccess)
        {
            if (lRetryCount > lConfigRetryCount)
                break;

            log.info("Calling DB insertion. Retry count " + lRetryCount);

            Connection        con   = null;
            PreparedStatement pstmt = null;

            try
            {
                if (log.isDebugEnabled())
                    log.debug("Sql Query : " + POSTGRES_INSERT_SQL);

                final String R3C_JNDI_INFO = R3CUtil.getAppConfigValueAsString(ConfigParamConstants.R3C_JNDI_INFO);

                con = DBDataSourceFactory.getConnection(JndiInfoHolder.getInstance().getJndiInfo(CommonUtility.getInteger(R3C_JNDI_INFO, 9)));
                con.setAutoCommit(false);

                pstmt = con.prepareStatement(POSTGRES_INSERT_SQL);
                pstmt.setLong(1, CommonUtility.getLong(aVlRepository.getClientId()));
                pstmt.setString(2, aVlRepository.getFileId());
                pstmt.setString(3, aVlRepository.getMid());
                pstmt.setLong(4, CommonUtility.getLong(aVlRepository.getMobileNumber()));
                pstmt.setString(5, aVlRepository.getShortCode());
                pstmt.setString(6, aAddInfo.toJSONString());
                pstmt.setTimestamp(7, new Timestamp(aVlRepository.getExpiryDate().getTime()));

                pstmt.execute();
                con.commit();

                isInsertSuccess = true;
                log.info("Database insertion status - " + isInsertSuccess);
            }
            catch (final Exception e)
            {
                if (con != null)
                    CommonUtility.rollbackConnection(con);
                log.error("Exception while inserting into DB .", e);
            }
            finally
            {
                CommonUtility.closeStatement(pstmt);
                CommonUtility.closeConnection(con);
            }

            if (!isInsertSuccess)
                CommonUtility.sleepForAWhile();

            lRetryCount++;
        }

        if (!isInsertSuccess)
            log.fatal("Unable to insert record into datatbase. VL info " + aVlRepository + " Additional Info " + aAddInfo);

        return isInsertSuccess;
    }

    private static boolean insertIntoMySql(
            VLRepository aVlRepository,
            JSONObject aAddInfo)
    {
        boolean      isInsertSuccess   = false;
        int          lRetryCount       = 0;
        final int    lConfigRetryCount = CommonUtility.getInteger(R3CUtil.getAppConfigValueAsString(ConfigParamConstants.R3C_MAX_ELASTIC_RETRY_COUNT), 3);

        final String lTableName        = CommonUtility.findR3CTableName(aVlRepository.getShortCode());

        while (!isInsertSuccess)
        {
            if (lRetryCount > lConfigRetryCount)
                break;

            log.info("Calling DB insertion. Retry count " + lRetryCount);

            try
            {
                isInsertSuccess = insertR3CIntoDB(aAddInfo, aVlRepository, lTableName);
                isInsertSuccess = true;

                log.info("Database insertion status - " + isInsertSuccess);
            }
            catch (final Exception e)
            {
                log.error("Exception while inserting into DB .", e);
            }

            if (!isInsertSuccess)
                CommonUtility.sleepForAWhile();

            lRetryCount++;
        }

        if (!isInsertSuccess)
            log.fatal("Unable to insert record into datatbase. VL info " + aVlRepository + " Additional Info " + aAddInfo);

        return isInsertSuccess;
    }

    public static void main(
            String[] args)
    {
        final VLRepository vlObj = new VLRepository();
        vlObj.setClientId("1234567890");
        vlObj.setFileId("fileid");
        vlObj.setId("123456");
        vlObj.setShortCode(vlObj.getId());
        vlObj.setMobileNumber("9884227203");
        vlObj.setMid("MessageId");
        System.out.println(vlObj.getShortCode());
        vlObj.setExpiryDate(DateTimeUtility.getDateFromString("2021-10-10", DateTimeFormat.DEFAULT_DATE_ONLY));

        final JSONObject addInfo = new JSONObject();
        addInfo.put("addInfo_1", "addInfo_1");

        insertIntoDb(vlObj, addInfo);
    }

}
