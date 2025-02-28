package com.itextos.beacon.r3r.dbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.constants.ConfigParamConstants;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.r3r.data.R3RObject;
import com.itextos.beacon.r3r.utils.R3RConstants;
import com.itextos.beacon.r3r.utils.R3RUtils;

public class DBHandler
{

    private DBHandler()
    {}

    private static final Log    log                      = LogFactory.getLog(DBHandler.class);
    private static final String SELECT_MYSQL             = "M";
    private static final String SELECT_POSTGRES          = "P";
    private static final String SELECT_POSTGRES_BINARY   = "PB";

    private static final String INSERT_URL_TRACK_LOG_SQL = "insert into url_redirect_log(client_id, dest, msg_id, campaign_id,campaign_name,msg_recv_time,cli_msg_id," + "ipaddress,countrycode,"//
            + "country,region,city,longitute,latitude,osname,osgroup,browsername,browserversion,devicename,requesttime,smartlinkid,"
            + "shortenurl,shortcode,redirecturl,useragent,customparams,status) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SELECT_MYSQL_DATA        = "select cli_id,base_msg_id,dest,additional_info from {0} where shortner_id=?";
    private static final String SELECT_SQL_PB            = "select expiry_date,json_data from r3c_jsonb where shortner_id=?";
    private static final String SELECT_SQL_P             = "select cli_id,base_msg_id,dest,additional_info from r3c where shortner_id =?";

    public static void insertRecords(
            List<R3RObject> aR3rObjectsList)
            throws ItextosException
    {
        Connection        lSqlCon = null;
        PreparedStatement lPstmt  = null;

        if (log.isDebugEnabled())
            log.debug("Record inserting table name : " + "url_redirect_log");

        try
        {
            lSqlCon = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.R3C.getKey()));
            lPstmt  = lSqlCon.prepareStatement(INSERT_URL_TRACK_LOG_SQL);

            lSqlCon.setAutoCommit(false);
            insertRecords(lPstmt, aR3rObjectsList);
            lSqlCon.commit();
        }
        catch (final Exception e)
        {
            CommonUtility.rollbackConnection(lSqlCon);

            final String s = "Excception while inserting the data into '" + "url_track_log" + "'";
            log.error(s, e);

            try
            {
                insertIndividualRecords(aR3rObjectsList);
            }
            catch (final Exception e1)
            {
                final String s1 = "Excception while inserting the data into '" + "url_track_log" + "'";
                log.error(s1, e1);
                throw new ItextosException(s1, e1);
            }
        }
        finally
        {
            CommonUtility.closeStatement(lPstmt);
            CommonUtility.closeConnection(lSqlCon);
        }
    }

    private static void insertIndividualRecords(
            List<R3RObject> aR3rObjectsList)
            throws ItextosException
    {
        Connection        lSqlCon = null;
        PreparedStatement lPstmt  = null;

        if (log.isDebugEnabled())
            log.debug("Record inserting table name : " + "url_redirect_log");

        try
        {
            lSqlCon = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.R3C.getKey()));
            lPstmt  = lSqlCon.prepareStatement(INSERT_URL_TRACK_LOG_SQL);

            for (final R3RObject lR3rObject : aR3rObjectsList)
            {
                lSqlCon.setAutoCommit(false);
                insertRecords(lSqlCon, lPstmt, lR3rObject);
                lSqlCon.commit();
            }
        }
        catch (final Exception e)
        {
            CommonUtility.rollbackConnection(lSqlCon);
            final String s = "Excception while inserting the data into '" + "url_track_log" + "'";
            log.error(s, e);

            throw new ItextosException(s, e);
        }
        finally
        {
            CommonUtility.closeStatement(lPstmt);
            CommonUtility.closeConnection(lSqlCon);
        }
    }

    private static void insertRecords(
            Connection aSqlCon,
            PreparedStatement aPstmt,
            R3RObject aR3rObject)
    {

        try
        {
            aPstmt = prepareStatement(aR3rObject, aPstmt);

            aPstmt.execute();
            final int linsertCount = aPstmt.getUpdateCount();

            if (log.isDebugEnabled())
                log.debug("Record inserted to the table url_track_log is " + linsertCount);
        }
        catch (final Exception e)
        {
            CommonUtility.rollbackConnection(aSqlCon);
            log.error("Error while inseerting " + aR3rObject, e);
        }
    }

    private static void insertRecords(
            PreparedStatement aPstmt,
            List<R3RObject> aR3rObjectsList)
            throws SQLException
    {

        for (final R3RObject lR3rObject : aR3rObjectsList)
        {
            aPstmt = prepareStatement(lR3rObject, aPstmt);
            aPstmt.addBatch();
        }

        final int[] linsertCount = aPstmt.executeBatch();

        if (log.isDebugEnabled())
            log.debug("Record inserted to the table url_track_log is " + linsertCount.length);
    }

    private static PreparedStatement prepareStatement(
            R3RObject aR3rObject,
            PreparedStatement aPstmt)
    {

        try
        {
            if (aR3rObject.getClientId() != null)
                aPstmt.setString(1, aR3rObject.getClientId());
            else
                aPstmt.setString(1, R3RConstants.DEFAULT_CLIENT_ID);
            if (aR3rObject.getDest() != null)
                aPstmt.setString(2, aR3rObject.getDest());
            else
                aPstmt.setString(2, R3RConstants.DEFAULT_DEST);
            if (aR3rObject.getDest() != null)
                aPstmt.setString(3, aR3rObject.getMsgId());
            else
                aPstmt.setString(3, R3RConstants.DEFAULT_MID);
            aPstmt.setString(4, aR3rObject.getCamapignId());
            aPstmt.setString(5, aR3rObject.getCampaignName());

            if (aR3rObject.getMsgRecvTime() > 0)
            {
                final String    date        = DateTimeUtility.getFormattedDateTime(aR3rObject.getMsgRecvTime(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
                final Timestamp msgRecvTime = Timestamp.valueOf(date);
                aPstmt.setTimestamp(6, msgRecvTime);
            }
            else
                aPstmt.setTimestamp(6, null);
            aPstmt.setString(7, aR3rObject.getCustMsgId());
            aPstmt.setString(8, aR3rObject.getIpAddress());
            aPstmt.setString(9, aR3rObject.getCountryCode());
            aPstmt.setString(10, aR3rObject.getCountryName());
            aPstmt.setString(11, aR3rObject.getRegion());
            aPstmt.setString(12, aR3rObject.getCity());
            aPstmt.setString(13, aR3rObject.getLongitude());
            aPstmt.setString(14, aR3rObject.getLatitude());
            aPstmt.setString(15, aR3rObject.getOsName());
            aPstmt.setString(16, aR3rObject.getOsGroup());
            aPstmt.setString(17, aR3rObject.getBrowserName());
            aPstmt.setString(18, aR3rObject.getBrowserVersion());
            aPstmt.setString(19, aR3rObject.getDeviceName());
            final String    date                   = DateTimeUtility.getFormattedDateTime(aR3rObject.getRequestTime(), DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS);
            final Timestamp urlRequestRedirectTime = Timestamp.valueOf(date);
            aPstmt.setTimestamp(20, urlRequestRedirectTime);
            aPstmt.setString(21, aR3rObject.getSmartLinkId());
            aPstmt.setString(22, aR3rObject.getShortenUrl());
            aPstmt.setString(23, aR3rObject.getShortCode());
            aPstmt.setString(24, aR3rObject.getRedirectUrl());
            aPstmt.setString(25, aR3rObject.getUserAgent());
            aPstmt.setString(26, aR3rObject.getCustomParams());
            aPstmt.setString(27, aR3rObject.getReason());
        }
        catch (final Exception e)
        {
            log.error("Exception while prepare the statement", e);
        }
        return aPstmt;
    }

    public static Map<String, String> getShortCodeData(
            String aShortCode)
    {
        final String insertType = R3RUtils.getAppConfigValueAsString(ConfigParamConstants.R3C_INSERT_TYPE);

        switch (insertType)
        {
            case SELECT_POSTGRES:
                return getRedirectUrlDataFromPostgres(aShortCode);

            case SELECT_POSTGRES_BINARY:
                return getRedirectUrlDataFromPostgresBinary(aShortCode);

            case SELECT_MYSQL:
            default:
                final String lShortCodeDataTableName = CommonUtility.findR3CTableName(aShortCode);
                return getRedirectUrlDataFromMySQL(aShortCode, lShortCodeDataTableName);
        }
    }

    public static Map<String, String> getRedirectUrlDataFromMySQL(
            String aShortCode,
            String aShortCodeDataTableName)
    {
        ResultSet           lResultSet        = null;
        Map<String, String> lShortCodeDataMap = new HashMap<>();

        if (log.isDebugEnabled())
            log.debug("Get the Record for the Shortcode : " + aShortCode);

        final String lSql          = MessageFormat.format(SELECT_MYSQL_DATA, aShortCodeDataTableName);
        final String R3C_JNDI_INFO = R3RUtils.getAppConfigValueAsString(ConfigParamConstants.R3C_JNDI_INFO);

        if (log.isDebugEnabled())
            log.debug("Sql Query : " + lSql);

        try (
                Connection lSqlCon = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfo(CommonUtility.getInteger(R3C_JNDI_INFO, 9)));
                PreparedStatement lPstmt = lSqlCon.prepareStatement(lSql);)
        {
            lPstmt.setString(1, aShortCode);
            lResultSet = lPstmt.executeQuery();

            if (lResultSet.next())
                lShortCodeDataMap = getSmartLinkData(lResultSet);
        }
        catch (final Exception e)
        {
            log.error("Exception while get the shortcode data ", e);
        }
        finally
        {
            CommonUtility.closeResultSet(lResultSet);
        }
        return lShortCodeDataMap;
    }

    public static Map<String, String> getRedirectUrlDataFromPostgres(
            String aShortCode)
    {
        ResultSet           lResultSet        = null;
        Map<String, String> lShortCodeDataMap = new HashMap<>();

        if (log.isDebugEnabled())
            log.debug("Sql Query : " + SELECT_SQL_P);
        final String R3C_JNDI_INFO = R3RUtils.getAppConfigValueAsString(ConfigParamConstants.R3C_JNDI_INFO);

        if (log.isDebugEnabled())
            log.debug("Get the Record for the Shortcode : " + aShortCode);

        try (
                Connection lSqlCon = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfo(CommonUtility.getInteger(R3C_JNDI_INFO, 9)));
                PreparedStatement lPstmt = lSqlCon.prepareStatement(SELECT_SQL_P);)
        {
            lPstmt.setString(1, aShortCode);
            lResultSet = lPstmt.executeQuery();

            if (lResultSet.next())
                lShortCodeDataMap = getSmartLinkData(lResultSet);
        }
        catch (final Exception e)
        {
            log.error("Exception while get the shortcode data from Postgres ", e);
        }
        finally
        {
            CommonUtility.closeResultSet(lResultSet);
        }
        return lShortCodeDataMap;
    }

    public static Map<String, String> getRedirectUrlDataFromPostgresBinary(
            String aShortCode)
    {
        ResultSet           lResultSet        = null;
        String              jsonResultData    = null;
        Date                lExpiryDate       = null;
        Map<String, String> lShortCodeDataMap = new HashMap<>();

        if (log.isDebugEnabled())
            log.debug("Get the Record for the Shortcode : " + aShortCode);

        if (log.isDebugEnabled())
            log.debug("Sql Query : " + SELECT_SQL_PB);

        final String R3C_JNDI_INFO = R3RUtils.getAppConfigValueAsString(ConfigParamConstants.R3C_JNDI_INFO);

        try (
                Connection lSqlCon = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfo(CommonUtility.getInteger(R3C_JNDI_INFO, 9)));
                PreparedStatement lPstmt = lSqlCon.prepareStatement(SELECT_SQL_PB);)
        {
            lPstmt.setString(1, aShortCode);
            lResultSet = lPstmt.executeQuery();

            if (lResultSet.next())
            {
                lExpiryDate    = lResultSet.getDate("expiry_date");
                jsonResultData = lResultSet.getString("json_data");
            }

            if (jsonResultData != null)
                lShortCodeDataMap = getSmartLinkDataFromPB(jsonResultData, lExpiryDate);
        }
        catch (final Exception e)
        {
            log.error("Exception while get the shortcode data from Postgres Binary ", e);
        }
        finally
        {
            CommonUtility.closeResultSet(lResultSet);
        }

        return lShortCodeDataMap;
    }

    public static Map<String, String> getSmartLinkData(
            ResultSet aResultSet)
    {
        final Map<String, String> lSmartLinkDataMap = new HashMap<>();

        try
        {
            lSmartLinkDataMap.put("client_id", aResultSet.getString("cli_id"));
            lSmartLinkDataMap.put("msg_id", aResultSet.getString("base_msg_id"));
            lSmartLinkDataMap.put("dest", aResultSet.getString("dest"));

            final Map<String, String> lSmartLinkInfo = R3RUtils.getSmartLinkData(aResultSet.getString("additional_info"));
            lSmartLinkDataMap.put("url", lSmartLinkInfo.get("url"));
            lSmartLinkDataMap.put("smartlink_id", lSmartLinkInfo.get("smartlink_id"));
            lSmartLinkDataMap.put("shortner_url", lSmartLinkInfo.get("shortner_url"));
        }
        catch (final Exception lE)
        {
            log.error("Exception while process the ResultSet Data ", lE);
        }
        return lSmartLinkDataMap;
    }

    public static Map<String, String> getSmartLinkDataFromPB(
            String aPBJsonData,
            Date aExpiryDate)
    {
        final Map<String, String> lSmartLinkDataMap = new HashMap<>();

        try
        {
            final JsonObject shortCodeJson = JsonParser.parseString(aPBJsonData).getAsJsonObject();
            lSmartLinkDataMap.put("client_id", CommonUtility.nullCheck(shortCodeJson.get("cli_id").getAsString(), true));
            lSmartLinkDataMap.put("msg_id", CommonUtility.nullCheck(shortCodeJson.get("base_msg_id").getAsString(), true));
            lSmartLinkDataMap.put("dest", CommonUtility.nullCheck(shortCodeJson.get("dest").getAsString(), true));
            lSmartLinkDataMap.put("url", CommonUtility.nullCheck(shortCodeJson.get("url").getAsString(), true));
            lSmartLinkDataMap.put("smartlink_id", CommonUtility.nullCheck(shortCodeJson.get("smartlink_id").getAsString(), true));
            lSmartLinkDataMap.put("shortner_url", CommonUtility.nullCheck(shortCodeJson.get("shortner_url").getAsString(), true));
            lSmartLinkDataMap.put("campaign_id", CommonUtility.nullCheck(shortCodeJson.get("campaign_id").getAsString(), true));
            lSmartLinkDataMap.put("campaign_name", CommonUtility.nullCheck(shortCodeJson.get("campaign_name").getAsString(), true));
            lSmartLinkDataMap.put("recv_time", CommonUtility.nullCheck(shortCodeJson.get("recv_time").getAsString(), true));
            lSmartLinkDataMap.put("cli_msg_id", CommonUtility.nullCheck(shortCodeJson.get("cli_msg_id").getAsString(), true));
            lSmartLinkDataMap.put("expiry_date", DateTimeUtility.getFormattedDateTime(aExpiryDate, DateTimeFormat.DEFAULT_DATE_ONLY));
        }
        catch (final Exception lE)
        {
            log.error("Exception while process the ResultSet Data ", lE);
        }
        return lSmartLinkDataMap;
    }

}
