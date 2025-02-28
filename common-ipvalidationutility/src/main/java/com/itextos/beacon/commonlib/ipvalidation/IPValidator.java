package com.itextos.beacon.commonlib.ipvalidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.validator.routines.InetAddressValidator;

public class IPValidator
{

    private final static Log log                       = LogFactory.getLog(IPValidator.class);
    private final static int RANGE_CHECK_SUCCESS       = 0;
    private final static int RANGE_CHECK_FAILURE       = 1;
    private final static int RANGE_CHECK_DO_NEXT_CHECK = -1;
    private final static int CIDR_CHECK_SUCCESS        = 0;
    private final static int CIDR_CHECK_FAILURE        = 1;
    private final static int CIDR_CHECK_DO_NEXT_CHECK  = -1;

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final IPValidator INSTANCE = new IPValidator();

    }

    public static IPValidator getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final Map<String, String>            m_DbIPDetails        = new HashMap<>();
    private final Map<String, Set<String>>       m_DerivedIPs         = new HashMap<>();
    private final Map<String, List<SubnetUtils>> m_DerivedSubnetUtils = new HashMap<>();

    private IPValidator()
    {}

    @Override
    public Object clone()
    {
        throw new RuntimeException("Clone not supported for this object. " + IPValidator.class.getName());
    }

    public boolean isValidIP(
            String aIsIPValidationEnable,
            String aClientId,
            String aDBIpDetails,
            String aClientIP)
    {
        if (log.isDebugEnabled())
            log.debug("aIsIPValidationEnable : '" + aIsIPValidationEnable + "' ClientId : '" + aClientId + "' DBIPDetails : '" + aDBIpDetails + "' ClientIP : '" + aClientIP + "'");

        if (!"1".equals(aIsIPValidationEnable))
        {
            if (log.isDebugEnabled())
                log.debug("IP validation disabled for client : '" + aClientId + "'");
            return true;
        }

        final String lClientId    = nullCheckAndTrim(aClientId);
        final String lDBIPDetails = nullCheckAndTrim(aDBIpDetails);
        final String lClientIP    = nullCheckAndTrim(aClientIP);

        if ("".equals(lClientId) || "".equals(lDBIPDetails) || "".equals(lClientIP))
        {
            log.error("Cannot do IP Validation for Client : '" + lClientId + "' DBIpDetails : '" + lDBIPDetails + "' ClientIP : '" + lClientIP + "'");
            return false;
        }

        if (!InetAddressValidator.getInstance().isValid(lClientIP))
        {
            log.error("Passed clientIP : is not a valid one. Passed IP :" + lClientIP + "'");
            return false;
        }

        if ("*".equals(lDBIPDetails))
        {
            if (log.isDebugEnabled())
                log.debug("Esme : '" + aClientId + "' is having '*' as valid IPs.");
            return true;
        }

        final boolean isCheckLoadedIP = checkLoadedIP(lClientId, lDBIPDetails);

        if (log.isDebugEnabled())
            log.debug("IPs loaded already ? '" + isCheckLoadedIP + "'");

        if (!isCheckLoadedIP)
        {
            removeOldEntries(lClientId);
            loadIPs(lClientId, lDBIPDetails);
            m_DbIPDetails.put(lClientId, lDBIPDetails);
        }

        if (aDBIpDetails.contains("/"))
        {
            if (log.isDebugEnabled())
                log.debug("Checking first on the SubnetUtil as the IP is having '/'. Client : '" + lClientId + "' DBIpDetails : '" + lDBIPDetails + "' ClientIP : '" + lClientIP + "'");

            final List<SubnetUtils> list = m_DerivedSubnetUtils.get(lClientId);

            if (list != null)
                for (final SubnetUtils utils : list)
                {
                    if (log.isDebugEnabled())
                        log.debug("Checking SubnetUtil for the Client : '" + lClientId + "' is : '" + utils.getInfo().getCidrSignature() + "'");

                    if (utils.getInfo().isInRange(lClientIP))
                        return true;
                }
            else
                if (log.isDebugEnabled())
                    log.debug("There is no SubnetUtils found for the Client : '" + lClientId + "'");
        } // End of if (aDBIpDetails.contains("/"))

        final Set<String> set = m_DerivedIPs.get(lClientId);

        if (log.isDebugEnabled())
            log.debug("Checking in a single or Range of IPs. Client Id : '" + lClientId + "' DBIpDetails : '" + lDBIPDetails + "' ClientIP : '" + lClientIP + "' Possible IP : '" + set + "'");

        if (set != null)
            return set.contains(lClientIP);
        return false;
    }

    private void removeOldEntries(
            Object aClientId)
    {
        m_DerivedIPs.remove(aClientId);
        m_DerivedSubnetUtils.remove(aClientId);
    }

    private void loadIPs(
            String aClientId,
            String aDBIPDetails)
    {
        if (log.isDebugEnabled())
            log.debug("Loading IP Details into Memory for Client id : '" + aClientId + "' DB IP Details : '" + aDBIPDetails + "'");

        final StringTokenizer st = new StringTokenizer(aDBIPDetails, ",");

        while (st.hasMoreTokens())
        {
            final String tempIP = st.nextToken().trim();

            if (log.isDebugEnabled())
                log.debug("Current IP : '" + tempIP + "'");

            final int checkForCIDRAndAdd = checkForCIDRAndAdd(aClientId, tempIP);

            if (checkForCIDRAndAdd == CIDR_CHECK_DO_NEXT_CHECK)
            {
                final int checkForRangeAndAdd = checkForRangeAndAdd(aClientId, tempIP);

                if ((checkForRangeAndAdd == RANGE_CHECK_DO_NEXT_CHECK))
                    checkForSingleIP(aClientId, tempIP);
            }
        }
    }

    private boolean checkForSingleIP(
            String aClientId,
            String aTempIP)
    {

        // This will look for the sigleIP only.
        try
        {
            final boolean isValid = InetAddressValidator.getInstance().isValid(aTempIP);

            if (log.isDebugEnabled())
                log.debug("Is Valid single IP for Client : '" + aClientId + "' IP : '" + aTempIP + "' is '" + isValid + "'");

            if (isValid)
            {
                Set<String> l_existingIP = m_DerivedIPs.get(aClientId);

                if (l_existingIP == null)
                {
                    l_existingIP = new HashSet<>();
                    m_DerivedIPs.put(aClientId, l_existingIP);
                }
                l_existingIP.add(aTempIP);
                return true;
            }

            log.error("Invalid ip specified for the Esme : '" + aClientId + "' IP : '" + aTempIP + "'");
        }
        catch (final Exception e)
        {
            log.error("Exception while validating single IP for the client : '" + aClientId + "' IP : '" + aTempIP + "'", e);
        }
        return false;
    }

    private int checkForRangeAndAdd(
            String aClientId,
            String aTempIP)
    {

        // If the IP is given in the Range like 180.138.1.1-180.130.1.50
        //
        // ******************************* REMEMBER *******************************
        // This will work only for the IPV4 type Ips.
        // This will work only for the range in the last segment.
        // This will not run over the other subnet. If there is a mismatch in other
        // subnets the entire range will be ignored.
        try
        {
            final int l_hyphenPosition = aTempIP.indexOf("-");

            if (l_hyphenPosition > 0)
            {
                final String lStartIP = aTempIP.substring(0, l_hyphenPosition).trim();
                final String lEndIP   = aTempIP.substring(l_hyphenPosition + 1).trim();

                if (log.isDebugEnabled())
                    log.debug("Client ID : '" + aClientId + " Start IP : '" + lStartIP + "' End IP : '" + lEndIP + "'");

                final boolean isValidStart = InetAddressValidator.getInstance().isValid(lStartIP);
                final boolean isValidEnd   = InetAddressValidator.getInstance().isValid(lEndIP);

                if (log.isDebugEnabled())
                    log.debug("Client ID : '" + aClientId + " Valid Start IP : '" + isValidStart + "' Valid End IP : '" + isValidEnd + "'");

                if (isValidStart && isValidEnd)
                {
                    final int    lLastIndexOfStartIP = lStartIP.lastIndexOf(".");
                    final int    lLastIndexOfEndIP   = lEndIP.lastIndexOf(".");
                    final String lFirst3SubnetStart  = lStartIP.substring(0, lLastIndexOfStartIP);
                    final String lFirst3SubnetEnd    = lEndIP.substring(0, lLastIndexOfEndIP);

                    if (lFirst3SubnetStart.equals(lFirst3SubnetEnd))
                    {
                        final int lStartIndex = Integer.parseInt(lStartIP.substring(lLastIndexOfStartIP + 1));
                        final int lEndIndex   = Integer.parseInt(lEndIP.substring(lLastIndexOfEndIP + 1));

                        if (lStartIndex > lEndIndex)
                        {
                            log.error(
                                    "The Start IP is greater than End IP. Client ID : '" + aClientId + " Start IP : '" + lStartIP + "' End IP : '" + lEndIP + "' Cannot Add the range for this range.");
                            return RANGE_CHECK_FAILURE;
                        }

                        final Set<String> tempIps = new HashSet<>();

                        for (int index = lStartIndex; index <= lEndIndex; index++)
                        {
                            final String temp = lFirst3SubnetStart + "." + index;
                            tempIps.add(temp);
                        }

                        if (log.isDebugEnabled())
                            log.debug("Range of IPs added for the Client Id : '" + aClientId + "' is " + tempIps);

                        Set<String> lExistingIPs = m_DerivedIPs.get(aClientId);

                        if (lExistingIPs == null)
                        {
                            lExistingIPs = new HashSet<>();
                            m_DerivedIPs.put(aClientId, lExistingIPs);
                        }
                        lExistingIPs.addAll(tempIps);

                        return RANGE_CHECK_SUCCESS;
                    }
                    log.error("The First 3 segments are different for both Start and End IPs. Client ID : '" + aClientId + "' Start IP : '" + lStartIP + "' End IP : '" + lEndIP + "'");
                    return RANGE_CHECK_FAILURE;
                }

                log.error("Unable to generate the range of IPs for the Client : '" + aClientId + "' as either the start / end ip is not valid. IP range : '" + aTempIP + "'");
                return RANGE_CHECK_FAILURE;
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while generating the IP Range for the Client : '" + aClientId + " IP Range : '" + aTempIP + "'", e);
        }
        return RANGE_CHECK_DO_NEXT_CHECK;
    }

    private int checkForCIDRAndAdd(
            String aClientId,
            String aTempIP)
    {

        // if the IP is given as 180.130.1.1/24 the it will be used.
        if (aTempIP.contains("/"))
        {
            // First check for valid IP
            final String  l_ipOnly  = aTempIP.substring(0, aTempIP.indexOf("/")).trim();
            final boolean isValidIP = InetAddressValidator.getInstance().isValid(l_ipOnly);

            if (log.isDebugEnabled())
                log.debug("Client ID : '" + aClientId + " Is Valid IP '" + l_ipOnly + "' is : '" + isValidIP + "'");

            if (isValidIP)
            {

                try
                {
                    final SubnetUtils utils = new SubnetUtils(aTempIP.trim());

                    if (log.isDebugEnabled())
                    {
                        log.debug("getCidrSignature()    : '" + utils.getInfo().getCidrSignature() + "'");
                        log.debug("getAddress()          : '" + utils.getInfo().getAddress() + "'");
                        log.debug("getLowAddress()       : '" + utils.getInfo().getLowAddress() + "'");
                        log.debug("getHighAddress()      : '" + utils.getInfo().getHighAddress() + "'");
                        log.debug("getAddressCountLong() : '" + utils.getInfo().getAddressCountLong() + "'");
                        log.debug("getBroadcastAddress() : '" + utils.getInfo().getBroadcastAddress() + "'");
                        log.debug("getNetworkAddress()   : '" + utils.getInfo().getNetworkAddress() + "'");
                    }

                    List<SubnetUtils> list = m_DerivedSubnetUtils.get(aClientId);

                    if (list == null)
                    {
                        list = new ArrayList<>();
                        m_DerivedSubnetUtils.put(aClientId, list);
                    }
                    list.add(utils);

                    if (log.isDebugEnabled())
                        log.debug("Client ID : '" + aClientId + "' Added on SubnetUtil for : '" + aTempIP + "'");
                    return CIDR_CHECK_SUCCESS;
                }
                catch (final Exception e)
                {
                    log.error("Exception while generating the SubnetUtil for the Client : '" + aClientId + " IP : '" + aTempIP + "'", e);
                }
                return CIDR_CHECK_FAILURE;
            } // End of if (isValidIP)

            log.error("Cannot get the SubnetUtil for the Client :'" + aClientId + "' as the IP '" + aTempIP + "' doesnot have a valid IP.");
            return CIDR_CHECK_FAILURE;
        } // End ofif (aTempIP.contains("/"))
        return CIDR_CHECK_DO_NEXT_CHECK;
    }

    private boolean checkLoadedIP(
            String aClientId,
            String aDBIpDetails)
    {
        final String l_dbDetail = m_DbIPDetails.get(aClientId);

        if (l_dbDetail == null)
            return false;

        if (l_dbDetail.equals(aDBIpDetails))
            return true;

        if (log.isDebugEnabled())
            log.debug("There is a mismatch between the existing DB IPs and current IPs. Will update the new IP Details.");
        return false;
    }

    private static String nullCheckAndTrim(
            Object aObject)
    {
        return aObject == null ? "" : aObject.toString();
    }

}
