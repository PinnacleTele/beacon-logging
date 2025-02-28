package com.itextos.beacon.platform.faillistutil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.ClusterType;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.redisconnectionprovider.RedisConnectionProvider;
import com.itextos.beacon.platform.faillistutil.util.FaillistConfig;
import com.itextos.beacon.platform.faillistutil.util.FaillistConstants;
import com.itextos.beacon.platform.faillistutil.util.FaillistPropertyLoader;
import com.itextos.beacon.platform.faillistutil.util.FaillistUtil;

import redis.clients.jedis.Jedis;

public class FaillistFinder
{

    private static final Log log = LogFactory.getLog(FaillistFinder.class);

    public static boolean isDomesticGlobalBlocklistNumber(
            String aNumber)
    {
        return isDomesticClientBlocklistNumber(FaillistConstants.CLIENT_ADDRESS_GLOBAL, aNumber);
    }

    /**
     * This method will check whether the given mobile number is available in the
     * List of Blocked Domestic mobile numbers specific to the
     * <code>Client Id</code> passed.
     *
     * @param aClientId
     *                      - A <code>String</code> representing the
     *                      <code>Client Id</code> for which the number to be
     *                      validated
     * @param aMobileNumber
     *                      - A <code>String</code> representing the mobile number
     *                      to be
     *                      validated
     *
     * @return <code>true</code> if the mobile number exists in the list of Blocked
     *         Domestic mobile numbers specific to the <code>Client Id</code>.
     *         <p>
     *         <code>false</code> if the mobile number not exists in list of Blocked
     *         Domestic mobile numbers specific to the <code>Client Id</code>.
     *         <p>
     *         <code>false</code>
     *         if the length of the mobile number passed is less than or equal to
     *         value specified in <code>domestic.num.split.len</code> property of
     *         <code>blacklist.properties</code> file.
     *
     * @see #isDomesticGlobalBlocklistNumber(String)
     * @see #isDomesticBlocklistNumber(String, String)
     * @see #findBlacklist(String, String, boolean)
     */
    public static boolean isDomesticClientBlocklistNumber(
            String aClientId,
            String aMobileNumber)
    {
        return findBlacklist(aClientId, aMobileNumber, false);
    }

    /**
     * The method to check whether the passed mobile number is available in the
     * <code>Domestic Account Specific Blocked List</code> or
     * <code>Domestic Global Blocked List</code>. This will first check
     * <code>Domestic Account Specific Blocked List</code>. If the mobile number is
     * not available in that list then it will check the
     * <code>Domestic Global Blocked List</code>. Returns
     * <code>true</code> if the mobile number available in any one of the list.
     * <code>false</code> otherwise.
     *
     * @param aClientId
     *                      - A <code>String</code> representing the
     *                      <code>Client Id</code> for which the number to be
     *                      validated
     * @param aMobileNumber
     *                      - A <code>String</code> representing the mobile number
     *                      to be
     *                      validated
     *
     * @return <code>true</code> if the mobile number exists in the list of Blocked
     *         Domestic mobile numbers specific to the <code>Client Id</code> or
     *         in <code>Domestic Global Blocked List</code>.
     *         <p>
     *         <code>false</code> if the mobile number not exists in list of Blocked
     *         Domestic mobile numbers specific to the <code>Client Id</code> or
     *         in <code>Domestic Global Blocked List</code>.
     *         <p>
     *         <code>false</code>
     *         if the length of the mobile number passed is less than or equal to
     *         value specified in <code>domestic.num.split.len</code> property of
     *         <code>blacklist.properties</code> file.
     *
     * @see #isDomesticGlobalBlocklistNumber(String)
     * @see #isDomesticClientBlocklistNumber(String, String)
     * @see #findBlacklist(String, String, boolean)
     */
    public static boolean isDomesticBlocklistNumber(
            String aClientId,
            String aMobileNumber)
    {
        boolean lBlockedNumber = isDomesticClientBlocklistNumber(aClientId, aMobileNumber);
        if (!lBlockedNumber)
            lBlockedNumber = isDomesticGlobalBlocklistNumber(aMobileNumber);

        return lBlockedNumber;
    }

    /**
     * This method will check whether the given mobile number is available in the
     * <code>International Global Blocked List</code>
     *
     * @param aNumber
     *                - A <code>String</code> representing the mobile number to be
     *                validated.
     *
     * @return <code>true</code> if the mobile number exists in the
     *         <code>International Global Blocked List</code>.
     *         <p>
     *         <code>false</code> if the mobile number not exists in the
     *         <code>International Global Blocked List</code>.
     *         <p>
     *         <code>false</code>
     *         if the length of the mobile number passed is less than or equal to
     *         value specified in <code>intl.num.split.len</code> property of
     *         <code>blacklist.properties</code> file.
     *
     * @see #isInternationalClientBlocklistNumber(String, String)
     * @see #isInternationalBlocklistNumber(String, String)
     * @see #findBlacklist(String, String, boolean)
     */
    public static boolean isInternationalGlobalBlocklistNumber(
            String aNumber)
    {
        return isInternationalClientBlocklistNumber(FaillistConstants.CLIENT_ADDRESS_GLOBAL, aNumber);
    }

    /**
     * This method will check whether the given mobile number is available in the
     * list of Blocked International mobile numbers specific to the
     * <code>Client Id</code> passed.
     *
     * @param aClientId
     *                      - A <code>String</code> representing the
     *                      <code>Client Id</code> for which the number to be
     *                      validated
     * @param aMobileNumber
     *                      - A <code>String</code> representing the mobile number
     *                      to be
     *                      validated
     *
     * @return <code>true</code> if the mobile number exists in the list of Blocked
     *         International mobile numbers specific to the
     *         <code>Client Id</code>.
     *         <p>
     *         <code>false</code> if the mobile number not exists in list of Blocked
     *         International mobile numbers specific to the
     *         <code>Client Id</code>.
     *         <p>
     *         <code>false</code>
     *         if the length of the mobile number passed is less than or equal to
     *         value specified in <code>intl.num.split.len</code> property of
     *         <code>blacklist.properties</code> file.
     *
     * @see #isInternationalGlobalBlocklistNumber(String)
     * @see #isInternationalBlocklistNumber(String, String)
     * @see #findBlacklist(String, String, boolean)
     */
    public static boolean isInternationalClientBlocklistNumber(
            String aClientId,
            String aMobileNumber)
    {
        return findBlacklist(aClientId, aMobileNumber, true);
    }

    /**
     * The method to check whether the passed mobile number is available in the
     * <code>International Account Specific Blocked List</code> or
     * <code>International Global Blocked List</code>. This will
     * first check <code>International Account Specific Blocked List</code>. If the
     * mobile number is not available in that list then it will check the
     * <code>International Global Blocked List</code>.
     * Returns <code>true</code> if the mobile number available in any one of the
     * list. <code>false</code> otherwise.
     *
     * @param aClientId
     *                      - A <code>String</code> representing the
     *                      <code>Client Id</code> for which the number to be
     *                      validated
     * @param aMobileNumber
     *                      - A <code>String</code> representing the mobile number
     *                      to be
     *                      validated
     *
     * @return <code>true</code> if the mobile number exists in the list of Blocked
     *         International mobile numbers specific to the
     *         <code>Client Id</code> or in
     *         <code>International Global Blocked List</code>.
     *         <p>
     *         <code>false</code> if the mobile number not exists in list of Blocked
     *         International mobile numbers specific to the
     *         <code>Client Id</code> or in
     *         <code>International Global Blocked List</code>.
     *         <p>
     *         <code>false</code>
     *         if the length of the mobile number passed is less than or equal to
     *         value specified in <code>intl.num.split.len</code> property of
     *         <code>blacklist.properties</code> file.
     *
     * @see #isInternationalGlobalBlocklistNumber(String)
     * @see #isInternationalClientBlocklistNumber(String, String)
     * @see #findBlacklist(String, String, boolean)
     */
    public static boolean isInternationalBlocklistNumber(
            String aClientId,
            String aMobileNumber)
    {
        boolean blockedNumber = isInternationalClientBlocklistNumber(aClientId, aMobileNumber);

        if (!blockedNumber)
            // to make it more readable as of the word document
            blockedNumber = isInternationalGlobalBlocklistNumber(aMobileNumber);

        return blockedNumber;
    }

    /**
     * A method to validate the given number is available to the specific list in
     * the <code>Redis</code> and respond back based on the availability of the
     * number.
     *
     * @param aClientId
     *                        - A <code>String</code> representing the
     *                        <code>Client Id</code> for which the number to be
     *                        validated
     * @param aMobileNumber
     *                        - A <code>String</code> representing the mobile number
     *                        to be validated
     * @param isInternational
     *                        - A <code>boolean</code> representing which redis to
     *                        be checked. <code>true</code> to check the
     *                        international blocked list, <code>false</code> to
     *                        check the domestic blocked list.
     *
     * @return <code>true</code> if the mobile number exists in the list of
     *         International / Domestic Blocked mobile numbers specific to the
     *         <code>Client Id</code> or in
     *         <code>International / Domestic Blocked Global List</code>, based on
     *         the <code>isInternational</code> parameter.
     *         <p>
     *         <code>false</code> if the mobile number not exists in the list of
     *         International / Domestic Blocked mobile numbers specific to the
     *         <code>Client Id</code> or in
     *         <code>International / Domestic Blocked Global List</code>, based on
     *         the <code>isInternational</code> parameter.
     *         <p>
     *         <code>false</code>
     *         if the length of the mobile number passed is less than or equal to
     *         value specified in <code>intl.num.split.len</code> /
     *         <code>domestic.num.split.len</code> property of
     *         <code>blacklist.properties</code> file with respect to
     *         <code>isInternational</code> parameter.
     *
     * @see #isDomesticGlobalBlocklistNumber(String)
     * @see #isDomesticClientBlocklistNumber(String, String)
     * @see #isDomesticBlocklistNumber(String, String)
     * @see #isInternationalGlobalBlocklistNumber(String)
     * @see #isInternationalClientBlocklistNumber(String, String)
     * @see #isInternationalBlocklistNumber(String, String)
     */

    private static boolean findBlacklist(
            String aClientId,
            String aMobileNumber,
            boolean isInternational)
    {
        if (log.isDebugEnabled())
            log.debug("Client Id : '" + aClientId + "', Mobile Number : '" + aMobileNumber + "', Is International : '" + isInternational + "'");

        final FaillistConfig lFaillistConfig = isInternational ? FaillistPropertyLoader.getInstance().getInternationConfig() : FaillistPropertyLoader.getInstance().getDomesticConfig();

        if (log.isDebugEnabled())
            log.debug("Block List Config : '" + lFaillistConfig + "'");

        if (lFaillistConfig.getNumberSplitLength() >= aMobileNumber.length())
            return false;

        Jedis lJedisCon = null;

        try
        {
            final int lRedisPoolCount = RedisConnectionProvider.getInstance().getRedisPoolCount(ClusterType.COMMON, Component.FAILLIST);
            final int lModValue       = (int) (Long.parseLong(aMobileNumber) % lRedisPoolCount);

            lJedisCon = RedisConnectionProvider.getInstance().getConnection(ClusterType.COMMON, Component.FAILLIST, (lModValue + 1));

            final String[] redisKeywords = FaillistUtil.getRedisKeywords(aClientId, aMobileNumber, lFaillistConfig.getNumberSplitLength(), lFaillistConfig.getRedisPrefixKey());

            if (log.isDebugEnabled())
            {
                log.debug("Redis Outer Key   : '" + redisKeywords[0] + "'");
                log.debug("Redis inner value : '" + redisKeywords[1] + "'");
            }

            final Boolean sIsMember = lJedisCon.sismember(redisKeywords[0], redisKeywords[1]);

            if (log.isDebugEnabled())
                log.debug("Is '" + aMobileNumber + "' available for the Client Id : '" + aClientId + "' in Redis ? : '" + sIsMember + "'");

            return sIsMember;
        }
        catch (final Exception e)
        {
            log.error("Exception while fetching records from Redis", e);
        }
        finally
        {
            if (lJedisCon != null)
                lJedisCon.close();
        }
        return false;
    }

    public static void main(
            String[] args)
    {
        final boolean lDomesticGlobalBlocklistNumber = isDomesticGlobalBlocklistNumber("919884227203");
        System.out.println(new java.util.Date() + " - BlocklistFinder main domesticGlobalBlocklistNumber : '" + lDomesticGlobalBlocklistNumber + "'");

        final boolean lDomesticClientBlocklistNumber = isDomesticClientBlocklistNumber("80000200000000", "917654321123");

        System.out.println(new java.util.Date() + " - BlocklistFinder main isDomesticClientBlocklistNumber : '" + lDomesticClientBlocklistNumber + "'");
    }

}