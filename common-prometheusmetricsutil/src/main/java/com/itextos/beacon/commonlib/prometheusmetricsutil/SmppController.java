package com.itextos.beacon.commonlib.prometheusmetricsutil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.prometheusmetricsutil.smpp.Action;
import com.itextos.beacon.commonlib.prometheusmetricsutil.smpp.RequestType;
import com.itextos.beacon.commonlib.utility.CommonUtility;

class SmppController
{

    private static final Log    log                             = LogFactory.getLog(SmppController.class);

    private static final String PROP_SMPP_BIND_REQUEST          = "smpp.bind.request.enabled";
    private static final String PROP_SMPP_BIND_LATENCY          = "smpp.bind.latency.enabled";
    private static final String PROP_SMPP_BIND_ACTIVE           = "smpp.bind.active.enabled";
    private static final String PROP_SMPP_BIND_ERROR            = "smpp.bind.error.enabled";
    private static final String PROP_SMPP_ENQUIRE_REQUEST       = "smpp.enquire.request.enabled";
    private static final String PROP_SMPP_ENQUIRE_LATENCY       = "smpp.enquire.latency.enabled";
    private static final String PROP_SMPP_SUBMIT_REQUEST        = "smpp.submitsm.request.enabled";
    private static final String PROP_SMPP_SUBMIT_LATENCY        = "smpp.submitsm.latency.enabled";
    private static final String PROP_SMPP_DELIVER_REQUEST       = "smpp.deliversm.request.enabled";
    private static final String PROP_SMPP_DELIVER_LATENCY       = "smpp.deliversm.latency.enabled";
    private static final String PROP_SMPP_DELIVER_ERROR         = "smpp.deliversm.error.enabled";
    private static final String PROP_SMPP_UNBIND_REQUEST        = "smpp.unbind.request.enabled";
    private static final String PROP_SMPP_UNBIND_LATENCY        = "smpp.unbind.latency.enabled";
    private static final String PROP_SMPP_UNBIND_COUNTER        = "smpp.unbind.counter.enabled";
    private static final String PROP_SMPP_FAILURE_COUNT         = "smpp.failure.enabled";

    private static final String PROP_SMPP_BIND_REQUEST_USERS    = "smpp.bind.request.users";
    private static final String PROP_SMPP_BIND_LATENCY_USERS    = "smpp.bind.latency.users";
    private static final String PROP_SMPP_BIND_ACTIVE_USERS     = "smpp.bind.active.users";
    private static final String PROP_SMPP_BIND_ERROR_USERS      = "smpp.bind.error.users";
    private static final String PROP_SMPP_ENQUIRE_REQUEST_USERS = "smpp.enquire.request.users";
    private static final String PROP_SMPP_ENQUIRE_LATENCY_USERS = "smpp.enquire.latency.users";
    private static final String PROP_SMPP_SUBMIT_REQUEST_USERS  = "smpp.submitsm.request.users";
    private static final String PROP_SMPP_SUBMIT_LATENCY_USERS  = "smpp.submitsm.latency.users";
    private static final String PROP_SMPP_DELIVER_REQUEST_USERS = "smpp.deliversm.request.users";
    private static final String PROP_SMPP_DELIVER_LATENCY_USERS = "smpp.deliversm.latency.users";
    private static final String PROP_SMPP_DELIVER_ERROR_USERS   = "smpp.deliversm.error.users";
    private static final String PROP_SMPP_UNBIND_REQUEST_USERS  = "smpp.unbind.request.users";
    private static final String PROP_SMPP_UNBIND_LATENCY_USERS  = "smpp.unbind.latency.users";
    private static final String PROP_SMPP_UNBIND_COUNTER_USERS  = "smpp.unbind.error.users";
    private static final String PROP_SMPP_FAILURE_COUNT_USERS   = "smpp.failure.users";

    private boolean             bindRequestEnabled;
    private boolean             bindLatencyEnabled;
    private boolean             bindActiveEnabled;
    private boolean             bindErrorEnabled;
    private boolean             enquireRequestEnabled;
    private boolean             enquireLatencyEnabled;
    private boolean             submitRequestEnabled;
    private boolean             submitLatencyEnabled;
    private boolean             deliverRequestEnabled;
    private boolean             deliverLatencyEnabled;
    private boolean             deliverErrorEnabled;
    private boolean             unbindRequestEnabled;
    private boolean             unbindLatencyEnabled;
    private boolean             unbindCounterEnabled;
    private boolean             failureEnabled;

    private Set<String>         bindRequestCounterUser;
    private Set<String>         bindLatencyUser;
    private Set<String>         bindActiveCounterUser;
    private Set<String>         bindErrorCounterUser;
    private Set<String>         enquiryRequestCounterUser;
    private Set<String>         enquiryLatencyUser;
    private Set<String>         submitRequestCounterUser;
    private Set<String>         submitLatencyUser;
    private Set<String>         deliverRequestCounterUser;
    private Set<String>         deliverLatencyUser;
    private Set<String>         deliverErrorUser;
    private Set<String>         unbindRequestCounterUser;
    private Set<String>         unbindLatencyUser;
    private Set<String>         unbindCounterUser;
    private Set<String>         failureCounterUser;

    void loadProperties(
            PropertiesConfiguration aPropConf)
    {
        bindRequestEnabled        = isEnabled(aPropConf, PROP_SMPP_BIND_REQUEST, "Bind Request Counter");
        bindLatencyEnabled        = isEnabled(aPropConf, PROP_SMPP_BIND_LATENCY, "Bind Latency");
        bindActiveEnabled         = isEnabled(aPropConf, PROP_SMPP_BIND_ACTIVE, "Bind Active Counter");
        bindErrorEnabled          = isEnabled(aPropConf, PROP_SMPP_BIND_ERROR, "Bind Error Counter");
        enquireRequestEnabled     = isEnabled(aPropConf, PROP_SMPP_ENQUIRE_REQUEST, "Enquire Request Counter");
        enquireLatencyEnabled     = isEnabled(aPropConf, PROP_SMPP_ENQUIRE_LATENCY, "Enquire Latency");
        submitRequestEnabled      = isEnabled(aPropConf, PROP_SMPP_SUBMIT_REQUEST, "Submit Request Counter");
        submitLatencyEnabled      = isEnabled(aPropConf, PROP_SMPP_SUBMIT_LATENCY, "Submit Latency");
        deliverRequestEnabled     = isEnabled(aPropConf, PROP_SMPP_DELIVER_REQUEST, "Deliver Request Counter");
        deliverLatencyEnabled     = isEnabled(aPropConf, PROP_SMPP_DELIVER_LATENCY, "Deliver Latency");
        deliverErrorEnabled       = isEnabled(aPropConf, PROP_SMPP_DELIVER_ERROR, "Deliver Error");
        unbindRequestEnabled      = isEnabled(aPropConf, PROP_SMPP_UNBIND_REQUEST, "Unbind Request Counter");
        unbindLatencyEnabled      = isEnabled(aPropConf, PROP_SMPP_UNBIND_LATENCY, "Unbind Latency");
        unbindCounterEnabled      = isEnabled(aPropConf, PROP_SMPP_UNBIND_COUNTER, "Unbind Counter");
        failureEnabled            = isEnabled(aPropConf, PROP_SMPP_FAILURE_COUNT, "Failure Counter");

        bindRequestCounterUser    = loadCustomers(aPropConf, PROP_SMPP_BIND_REQUEST_USERS, "Bind Request User");
        bindLatencyUser           = loadCustomers(aPropConf, PROP_SMPP_BIND_LATENCY_USERS, "Bind Latency User");
        bindActiveCounterUser     = loadCustomers(aPropConf, PROP_SMPP_BIND_ACTIVE_USERS, "Bind Active User");
        bindErrorCounterUser      = loadCustomers(aPropConf, PROP_SMPP_BIND_ERROR_USERS, "Bind Error User");
        enquiryRequestCounterUser = loadCustomers(aPropConf, PROP_SMPP_ENQUIRE_REQUEST_USERS, "Enquire Request User");
        enquiryLatencyUser        = loadCustomers(aPropConf, PROP_SMPP_ENQUIRE_LATENCY_USERS, "Enquire SM Latency User");
        submitRequestCounterUser  = loadCustomers(aPropConf, PROP_SMPP_SUBMIT_REQUEST_USERS, "Submit SM Request User");
        submitLatencyUser         = loadCustomers(aPropConf, PROP_SMPP_SUBMIT_LATENCY_USERS, "Submit SM Latency User");
        deliverRequestCounterUser = loadCustomers(aPropConf, PROP_SMPP_DELIVER_REQUEST_USERS, "Delivery SM Request User");
        deliverLatencyUser        = loadCustomers(aPropConf, PROP_SMPP_DELIVER_LATENCY_USERS, "Delivery SM Latency User");
        deliverErrorUser          = loadCustomers(aPropConf, PROP_SMPP_DELIVER_ERROR_USERS, "Delivery SM Error User");
        unbindRequestCounterUser  = loadCustomers(aPropConf, PROP_SMPP_UNBIND_REQUEST_USERS, "Unbind Request User");
        unbindLatencyUser         = loadCustomers(aPropConf, PROP_SMPP_UNBIND_LATENCY_USERS, "Unbind Latency User");
        unbindCounterUser  = loadCustomers(aPropConf, PROP_SMPP_UNBIND_COUNTER_USERS, "Unbind Counter User");
        failureCounterUser        = loadCustomers(aPropConf, PROP_SMPP_FAILURE_COUNT_USERS, "Failure Counter User");
    }

    private static boolean isEnabled(
            PropertiesConfiguration aPropConf,
            String aPropertyKey,
            String aDescription)
    {
        final String isEnabledString = aPropConf.getString(aPropertyKey);

        if (log.isInfoEnabled())
            log.info(aDescription + " enabled : '" + isEnabledString + "'");
        return CommonUtility.isEnabled(isEnabledString);
    }

    private static HashSet<String> loadCustomers(
            PropertiesConfiguration aPropConf,
            String aPropertyKey,
            String aDescription)
    {
        final List<Object> customerList = aPropConf.getList(aPropertyKey);

        if (log.isInfoEnabled())
            log.info(aDescription + " Size = " + (customerList == null ? -1 : customerList.size()) + ", Customers List : '" + customerList + "'");

        final HashSet<String> lHashSet = new HashSet<>();
        if (customerList != null)
            for (final Object s : customerList)
            {

                if (s.equals(PrometheusController.ALL))
                {
                    lHashSet.clear();
                    lHashSet.add(PrometheusController.ALL);
                    break;
                }

                lHashSet.add((String) s);
            }
        return lHashSet;
    }

    boolean canAddPrometheusCounterForError(
            String aUsername)
    {
        final boolean returnValue = false;

        try
        {
            return failureEnabled && PrometheusController.checkForUser(failureCounterUser, aUsername);
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Prometheus property information for Error for User: '" + aUsername + "'", e);
        }

        if (log.isDebugEnabled())
            log.debug("User: '" + aUsername + "' Result : '" + returnValue + "'");

        return returnValue;
    }

    boolean canAddPrometheusCounter(
            String aUsername,
            RequestType aRequestType,
            Action aAction)
    {
        boolean returnValue = false;

        try
        {

            switch (aRequestType)
            {
                case BIND:
                    returnValue = checkForBindRequest(aAction, aUsername);
                    break;

                case DELIVERY_SM:
                    returnValue = checkForDeliverSm(aAction, aUsername);
                    break;

                case ENQUIRE:
                    returnValue = checkForEnquire(aAction, aUsername);
                    break;

                case SUBMIT_SM:
                    returnValue = checkForSubmitSm(aAction, aUsername);
                    break;

                case UNBIND:
                    returnValue = checkForUnbind(aAction, aUsername);
                    break;

                default:
                    break;
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the Prometheus property information for User: '" + aUsername + "', RequestType: '" + aRequestType + "', Action: '" + aAction + "'", e);
        }

        if (log.isDebugEnabled())
            log.debug("User: '" + aUsername + "', RequestType: '" + aRequestType + "', Action: '" + aAction + "' Result : '" + returnValue + "'");

        return returnValue;
    }

    private boolean checkForUnbind(
            Action aAction,
            String aUsername)
    {

        switch (aAction)
        {
            case REQUEST:
            case RESPONSE:
                return unbindRequestEnabled && PrometheusController.checkForUser(unbindRequestCounterUser, aUsername);

            case LATENCY:
                return unbindLatencyEnabled && PrometheusController.checkForUser(unbindLatencyUser, aUsername);

            case ERROR:
                return unbindCounterEnabled && PrometheusController.checkForUser(unbindCounterUser, aUsername);

            case ACTIVE:
            default:
                break;
        }
        return false;
    }

    private boolean checkForSubmitSm(
            Action aAction,
            String aUsername)
    {

        switch (aAction)
        {
            case REQUEST:
            case RESPONSE:
                return submitRequestEnabled && PrometheusController.checkForUser(submitRequestCounterUser, aUsername);

            case LATENCY:
                return submitLatencyEnabled && PrometheusController.checkForUser(submitLatencyUser, aUsername);

            case ERROR:
            case ACTIVE:
            default:
                break;
        }
        return false;
    }

    private boolean checkForEnquire(
            Action aAction,
            String aUsername)
    {

        switch (aAction)
        {
            case REQUEST:
            case RESPONSE:
                return enquireRequestEnabled && PrometheusController.checkForUser(enquiryRequestCounterUser, aUsername);

            case LATENCY:
                return enquireLatencyEnabled && PrometheusController.checkForUser(enquiryLatencyUser, aUsername);

            case ACTIVE:
            case ERROR:
            default:
        }
        return false;
    }

    private boolean checkForDeliverSm(
            Action aAction,
            String aUsername)
    {

        switch (aAction)
        {
            case REQUEST:
            case RESPONSE:
                return deliverRequestEnabled && PrometheusController.checkForUser(deliverRequestCounterUser, aUsername);

            case LATENCY:
                return deliverLatencyEnabled && PrometheusController.checkForUser(deliverLatencyUser, aUsername);

            case ERROR:
                return deliverErrorEnabled && PrometheusController.checkForUser(deliverErrorUser, aUsername);

            case ACTIVE:
            default:
                break;
        }
        return false;
    }

    private boolean checkForBindRequest(
            Action aAction,
            String aUsername)
    {

        switch (aAction)
        {
            case ACTIVE:
                return bindActiveEnabled && PrometheusController.checkForUser(bindActiveCounterUser, aUsername);

            case ERROR:
                return bindErrorEnabled && PrometheusController.checkForUser(bindErrorCounterUser, aUsername);

            case REQUEST:
            case RESPONSE:
                return bindRequestEnabled && PrometheusController.checkForUser(bindRequestCounterUser, aUsername);

            case LATENCY:
                return bindLatencyEnabled && PrometheusController.checkForUser(bindLatencyUser, aUsername);

            default:
        }
        return false;
    }

}
