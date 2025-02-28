package com.itextos.beacon.commonlib.dnddataloader.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.itextos.beacon.commonlib.dnddataloader.db.Db2RedisThreadBased;
import com.itextos.beacon.commonlib.utility.CommonUtility;

/**
 * Thread spanner will allocate the total number of threads in to group of
 * records based on the number records available DND database table.
 * The groups will be decided based on interval of 1,000,000,000 (1000
 * millions).
 * <i>The allocation will be mostly equal to the max threads specified or one
 * less than that.</i>
 * <br>
 * <br>
 * Please refer the below sample records count (<i>As on 23 June 2017</i>) and
 * allocation of 100 Threads.<br>
 * <br>
 * <table border ='1'>
 * <tr>
 * <th align ='center'>Index</th>
 * <th align ='center'>Start Number</th>
 * <th align ='center'>End Number</th>
 * <th align ='center'>&nbsp&nbspRecords Count between&nbsp&nbsp<br>
 * start and end numbers</th>
 * <th align ='center'>&nbsp&nbspAllocated Threads&nbsp&nbsp<br>
 * Count</th>
 * </tr>
 * <tr>
 * <tr>
 * <td align ='right'>&nbsp&nbsp1&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp0&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp910999999999&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp1&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp1&nbsp&nbsp</td>
 * </tr>
 * <tr>
 * <td align ='right'>&nbsp&nbsp2&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp911000000000&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp911999999999&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp974780&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp1&nbsp&nbsp</td>
 * </tr>
 * <tr>
 * <td align ='right'>&nbsp&nbsp3&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp912000000000&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp912999999999&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp716920&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp1&nbsp&nbsp</td>
 * </tr>
 * <tr>
 * <td align ='right'>&nbsp&nbsp4&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp913000000000&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp913999999999&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp89930&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp1&nbsp&nbsp</td>
 * </tr>
 * <tr>
 * <td align ='right'>&nbsp&nbsp5&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp914000000000&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp914999999999&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp470002&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp1&nbsp&nbsp</td>
 * </tr>
 * <tr>
 * <td align ='right'>&nbsp&nbsp6&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp915000000000&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp915999999999&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp66534&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp1&nbsp&nbsp</td>
 * </tr>
 * <tr>
 * <td align ='right'>&nbsp&nbsp7&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp916000000000&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp916999999999&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp14406&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp1&nbsp&nbsp</td>
 * </tr>
 * <tr>
 * <td align ='right'>&nbsp&nbsp8&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp917000000000&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp917999999999&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp39427389&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp15&nbsp&nbsp</td>
 * </tr>
 * <tr>
 * <td align ='right'>&nbsp&nbsp9&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp918000000000&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp918999999999&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp58064075&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp23&nbsp&nbsp</td>
 * </tr>
 * <tr>
 * <td align ='right'>&nbsp&nbsp10&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp919000000000&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp919999999999&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp133698560&nbsp&nbsp</td>
 * <td align ='right'>&nbsp&nbsp54&nbsp&nbsp</td>
 * </tr>
 * <tr>
 * <b>
 * <td colspan='4' align ='center'><b>Total</b></td>
 * <td align='right'>&nbsp&nbsp<b>99</b>&nbsp&nbsp</td>
 * </tr>
 * </table>
 */
public class ThreadSpanner
{

    private static final Log log = LogFactory.getLog(ThreadSpanner.class);

    private ThreadSpanner()
    {}

    /**
     * Method to split the total threads and returned to the caller.
     * This method will also return the number of records available between each
     * range.
     *
     * @return A {@link HashMap} with two entries. The keys will be
     *         <code>DB_COUNTS, THREAD_COUNTS</code>.<br>
     *         The value for the key <code>DB_COUNTS</code> is the records count
     *         available between each range.<br>
     *         The value for the key <code>THREAD_COUNTS</code> is the thread
     *         allocation for each range of records.
     *
     * @throws Exception
     */
    public static Map<String, Map<Integer, Long>> getSpannedThreads()
            throws Exception
    {
        final Map<Integer, Long> threadCounts        = new HashMap<>();

        // Get the all the records count between the ranges
        final Map<Integer, Long> dbCounts            = getDBRecordsCount();

        int                      threadCountRequired = 0;
        long                     totalCount          = 0;

        // Calculate the total records and minimum required threads in the DND Table.
        for (final Long tempLong : dbCounts.values())
            if (tempLong.longValue() > 0)
            {
                threadCountRequired++;
                totalCount += tempLong;
            }

        if (log.isDebugEnabled())
            log.debug("Total records count : " + totalCount);

        int maxThreadCount = DndPropertyProvider.getInstance().getDbReaderThreadCount();

        // If the passed max threads count is less than the minimum required thread then
        // set the max thread count to the minimum required count.
        if (threadCountRequired > maxThreadCount)
        {
            if (log.isInfoEnabled())
                log.info("Minimum thread required is " + threadCountRequired + "; passed threads : " + maxThreadCount);
            maxThreadCount = threadCountRequired;
        }

        long                            curIndexDBCount    = 0;
        double                          percentageCurIndexDBCount;
        int                             curIndexThreadCount;
        int                             allocatedThreads   = 0;
        int                             unAllocatedThreads = 0;

        final HashMap<Integer, Integer> reworks            = new HashMap<>();

        for (final Integer index : dbCounts.keySet())
        {
            curIndexDBCount = dbCounts.get(index);

            if (log.isDebugEnabled())
                log.debug("Index : " + index + ", Count : " + curIndexDBCount);

            if (curIndexDBCount == 0)
                continue;

            // Calculate the percentage of the current index against the total count.
            // Based on that check the required threads for the percentage.
            percentageCurIndexDBCount = (100 * curIndexDBCount) / (totalCount * 1.0);
            curIndexThreadCount       = (int) ((percentageCurIndexDBCount * maxThreadCount) / 100);

            if (log.isDebugEnabled())
                log.debug("Index : " + index + ", Percentage : " + percentageCurIndexDBCount + "', Calculated Thread Count : " + curIndexThreadCount);

            // If the calculated thread count is less than or equal to 1, then
            // allocate minimum one thread to that index.
            if (curIndexThreadCount <= 1)
            {
                threadCounts.put(index, 1L);
                allocatedThreads++;
            }
            else
            {
                reworks.put(index, curIndexThreadCount);
                unAllocatedThreads += curIndexThreadCount;
            }
        }

        if (log.isDebugEnabled())
        {
            log.debug("Allocated Thread details  : " + threadCounts);
            log.debug("Index to be reworked      : " + reworks);
            log.debug("Unallocated Threads Count : " + unAllocatedThreads);
        }

        // Get the remaining threads to be allocated
        final int remainingThreads = maxThreadCount - allocatedThreads;

        // Based on the remaining threads and already allocated threads recalculate
        // threads for the index.
        // TODO Still need to validate this part of the code.
        for (final Integer curRemainderIndex : reworks.keySet())
        {
            curIndexThreadCount = (reworks.get(curRemainderIndex) * remainingThreads) / unAllocatedThreads;
            curIndexThreadCount = curIndexThreadCount == 0 ? 1 : curIndexThreadCount;
            threadCounts.put(curRemainderIndex, (long) curIndexThreadCount);
        }

        if (log.isInfoEnabled())
            log.info("Final Thread allocations : " + threadCounts);

        // Set the return values to the caller
        final Map<String, Map<Integer, Long>> returnValue = new HashMap<>();
        returnValue.put("DB_COUNTS", dbCounts);
        returnValue.put("THREAD_COUNTS", threadCounts);
        return returnValue;
    }

    /**
     * Get the total records from the
     * {@link DndPropertyProvider#getDnDDataTableName()} table and
     * return the results as a
     * {@link HashMap}.
     *
     * @return {@link HashMap}
     *         Key will be the index from 0 to 9 and the values will be the number
     *         of records between 1000 Million ranges.
     *
     * @throws Exception
     */
    private static HashMap<Integer, Long> getDBRecordsCount()
            throws Exception
    {
        final HashMap<Integer, Long> result = new HashMap<>();
        DndPropertyProvider.getInstance();
        final String sql = "select count(*) from " + DndPropertyProvider.getDnDDataTableName() + " where dest between ? and ?";

        if (log.isDebugEnabled())
            log.debug("SQL : " + sql);

        Connection        con   = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try
        {
            con = DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfo(Db2RedisThreadBased.DND_JNDI_INFO));
            String startValue, endValue;
            long   startNumber, endNumber;
            pstmt = con.prepareStatement(sql);

            // Check the counts for every 1000 millions
            for (int index = 1; index < 10; index++)
            {
                startValue  = "91" + index + "000000000";
                endValue    = "91" + index + "999999999";

                startNumber = Long.parseLong(startValue);
                endNumber   = Long.parseLong(endValue);

                if (log.isDebugEnabled())
                    log.debug("Start number : '" + startNumber + "', End Number : '" + endNumber);

                pstmt.setLong(1, startNumber);
                pstmt.setLong(2, endNumber);

                rs = pstmt.executeQuery();

                if (rs.next())
                    result.put(index, rs.getLong(1));

                // Don't use CommonUtility.closeResultset()
                if (rs != null)
                    rs.close();
            }

            // Add for the records count less than 911000000000
            pstmt.setLong(1, 0);
            pstmt.setLong(2, 911000000000L);

            rs = pstmt.executeQuery();

            if (rs.next())
                result.put(0, rs.getLong(1));
            rs.close();
        }
        catch (final Exception e)
        {
            throw e;
        }
        finally
        {
            CommonUtility.closeStatement(pstmt);
            CommonUtility.closeConnection(con);
        }
        if (log.isDebugEnabled())
            log.debug("Result : '" + result + "'");

        if (log.isInfoEnabled())
            log.info("Result : '" + result + "'");

        return result;
    }

}