package com.itextos.beacon.commonlib.shortcodeprovider.operation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfo;
import com.itextos.beacon.commonlib.constants.ShortcodeLength;
import com.itextos.beacon.commonlib.constants.exception.ItextosException;
import com.itextos.beacon.commonlib.utility.CommonUtility;

public class DbOperation
{

    private static final Log    log        = LogFactory.getLog(DbOperation.class);

    private static final String SELECT_SQL = "select * from short_code_data_info scdi where category = ? order by redis_loaded_date, seq_no limit 1";
    private static final String UPDATE_SQL = "update short_code_data_info set redis_loaded_date = now() where seq_no = ?";

    private DbOperation()
    {}

    public static FileInfo loadFromFile(
            ShortcodeLength aShortcodeType)
            throws Exception
    {
        ResultSet rs       = null;
        FileInfo  fileInfo = null;

        Connection con = null;
        PreparedStatement pstmt = null;
        try 
        {
        	  con = DBDataSourceFactory.getConnectionFromThin(JndiInfo.CONFIGURARION_DB);
              pstmt = con.prepareStatement(SELECT_SQL);
        	if (log.isDebugEnabled())
                log.debug("Request for next file to load for the string length " + aShortcodeType);

            pstmt.setInt(1, aShortcodeType.getLength());
            rs = pstmt.executeQuery();

            if (rs.next())
            {
                final String    id              = rs.getString("seq_no");
                final String    filename        = rs.getString("file_name");
                final Timestamp redisLoadedDate = rs.getTimestamp("redis_loaded_date");

                if (log.isDebugEnabled())
                    log.debug("Id = '" + id + "', Filename = '" + filename + "' Redis loaded date = '" + redisLoadedDate + "'");

                if (redisLoadedDate != null)
                {
                    final boolean isFileExpired = isFileExpired(aShortcodeType, redisLoadedDate);
                    if (isFileExpired)
                        fileInfo = new FileInfo(id, filename);
                }
                else
                    fileInfo = new FileInfo(id, filename);
            }
            else
                log.error("No files found for the query. String Length : '" + aShortcodeType + "'");
        }
        catch (final Exception e)
        {
            final String s = "Exception while getting the next file for the length " + aShortcodeType;
            log.error(s, e);
            throw new ItextosException(s, e);
        }
        finally
        {
            CommonUtility.closeResultSet(rs);
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
   
        }

        if (log.isDebugEnabled())
            log.debug("Final return value for the file info " + fileInfo);
        return fileInfo;
    }

    private static boolean isFileExpired(
            ShortcodeLength aShortcodeType,
            Timestamp aRedisLoadedDate)
    {
        final Calendar calCur = Calendar.getInstance();
        final Calendar calOld = Calendar.getInstance();
        calOld.setLenient(false);
        calOld.setTimeInMillis(aRedisLoadedDate.getTime());
        final int days = ShortCodeProperties.getInstance().getExpiryDays(aShortcodeType);
        calOld.add(Calendar.DATE, days);
        final boolean returnValue = calOld.before(calCur);

        if (log.isDebugEnabled())
            log.debug("Dates to compare Current Date : '" + calCur.getTime() + "' Redis Loaded Date : '" + new Date(aRedisLoadedDate.getTime()) + "' days '" + days + "' Old Date + days "
                    + calOld.getTime() + "' result " + returnValue);

        return returnValue;
    }

    public static void updateStatusToDb(
            String aId)
            throws Exception
    {
        if (log.isDebugEnabled())
            log.debug("Updating short_code_data_info for the id '" + aId + "'");
        Connection con =null;
        PreparedStatement pstmt = null;
        try 
        {
        	  con = DBDataSourceFactory.getConnectionFromThin(JndiInfo.CONFIGURARION_DB);
              pstmt = con.prepareStatement(UPDATE_SQL);
            pstmt.setString(1, aId);
            final int lExecuteUpdate = pstmt.executeUpdate();
            if (log.isDebugEnabled())
                log.debug("Total number of records updated for id '" + aId + "' is '" + lExecuteUpdate + "'");
        }
        catch (final Exception e)
        {
            final String s = "Exception while updating the short_code_data_info table.";
            log.error(s, e);
            throw new ItextosException(s, e);
        }finally {
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
   
        }
    }

}