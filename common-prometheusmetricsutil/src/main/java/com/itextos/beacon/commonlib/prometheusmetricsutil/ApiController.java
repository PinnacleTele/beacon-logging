package com.itextos.beacon.commonlib.prometheusmetricsutil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.InterfaceGroup;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.utility.CommonUtility;

class ApiController
{

    private static final Log    log                      = LogFactory.getLog(ApiController.class);

    private static final String PROP_API_ALL_REQ_ACCEPT  = "api.all.accept.enabled";
    private static final String PROP_API_ALL_REQ_STATUS  = "api.all.status.enabled";
    private static final String PROP_API_ALL_REQ_LATENCY = "api.all.latency.enabled";

    private static final String PROP_API_TYPES           = "api.types";
    private static final String PROP_API_TYPES_ACCEPT    = "api.types.accept.enabled";
    private static final String PROP_API_TYPES_STATUS    = "api.types.status.enabled";
    private static final String PROP_API_TYPES_LATENCY   = "api.types.latency.enabled";

    private static final String PROP_API_USERS           = "api.users";
    private static final String PROP_API_USERS_ACCEPT    = "api.users.accept.enabled";
    private static final String PROP_API_USERS_STATUS    = "api.users.status.enabled";
    private static final String PROP_API_USERS_LATENCY   = "api.users.latency.enabled";

    private boolean             mIsApiAllAcceptEnabled;
    private boolean             mIsApiAllStatusEnabled;
    private boolean             mIsApiAllLatencyEnabled;

    private Set<InterfaceType>  mApiList;
    private boolean             mIsApiListAcceptEnabled;
    private boolean             mIsApiListStatusEnabled;
    private boolean             mIsApiListLatencyEnabled;

    private Set<String>         mApiUsersList;
    private boolean             mApiUsersAcceptEnabled;
    private boolean             mApiUsersStatusEnabled;
    private boolean             mApiUsersLatencyEnabled;

    void loadAPIProperties(
            PropertiesConfiguration aPropConf)
    {
        mIsApiAllAcceptEnabled   = CommonUtility.isEnabled(aPropConf.getString(PROP_API_ALL_REQ_ACCEPT));
        mIsApiAllStatusEnabled   = CommonUtility.isEnabled(aPropConf.getString(PROP_API_ALL_REQ_STATUS));
        mIsApiAllLatencyEnabled  = CommonUtility.isEnabled(aPropConf.getString(PROP_API_ALL_REQ_LATENCY));

        mApiList                 = getApiList(aPropConf);
        mIsApiListAcceptEnabled  = CommonUtility.isEnabled(aPropConf.getString(PROP_API_TYPES_ACCEPT));
        mIsApiListStatusEnabled  = CommonUtility.isEnabled(aPropConf.getString(PROP_API_TYPES_STATUS));
        mIsApiListLatencyEnabled = CommonUtility.isEnabled(aPropConf.getString(PROP_API_TYPES_LATENCY));

        mApiUsersList            = PrometheusController.getList(aPropConf, PROP_API_USERS, "Api Users list");
        mApiUsersAcceptEnabled   = CommonUtility.isEnabled(aPropConf.getString(PROP_API_USERS_ACCEPT));
        mApiUsersStatusEnabled   = CommonUtility.isEnabled(aPropConf.getString(PROP_API_USERS_STATUS));
        mApiUsersLatencyEnabled  = CommonUtility.isEnabled(aPropConf.getString(PROP_API_USERS_LATENCY));
    }

    private static Set<InterfaceType> getApiList(
            PropertiesConfiguration aPropConf)
    {
        final Set<InterfaceType> returnValue = new HashSet<>();
        final Set<String>        lList       = PrometheusController.getList(aPropConf, PROP_API_TYPES, "Api List");

        if (lList.contains(PrometheusController.ALL))
        {
            final List<InterfaceType> allApis = InterfaceType.getTypeBasedonGroup(InterfaceGroup.API);
            returnValue.addAll(allApis);
        }
        else
            for (final String s : lList)
                try
                {
                    returnValue.add(InterfaceType.getType(s));
                }
                catch (final Exception e)
                {
                    log.error("Unable to get the InterfaceType for '" + s + "'", e);
                }
        return returnValue;
    }

    boolean isApiAllAcceptEnabled()
    {
        return PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.API) && mIsApiAllAcceptEnabled;
    }

    boolean isApiAllStatusEnabled()
    {
        return PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.API) && mIsApiAllStatusEnabled;
    }

    boolean isApiAllLatencyEnabled()
    {
        return PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.API) && mIsApiAllLatencyEnabled;
    }

    boolean isMetricsEnabledForSpecificApis()
    {
        return PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.API) && !mApiList.isEmpty()
                && (mIsApiListAcceptEnabled || mIsApiListStatusEnabled || mIsApiListLatencyEnabled);
    }

    boolean isApiListAcceptEnabled()
    {
        return PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.API) && mIsApiListAcceptEnabled;
    }

    boolean isApiListStatusEnabled()
    {
        return PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.API) && mIsApiListStatusEnabled;
    }

    boolean isApiListLatencyEnabled()
    {
        return PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.API) && mIsApiListLatencyEnabled;
    }

    boolean isApiListAcceptEnabled(
            InterfaceType aInterfaceType)
    {
        return PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.API) && isApiEnabled(aInterfaceType) && mIsApiListAcceptEnabled;
    }

    boolean isApiListStatusEnabled(
            InterfaceType aInterfaceType)
    {
        return PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.API) && isApiEnabled(aInterfaceType) && mIsApiListStatusEnabled;
    }

    boolean isApiListLatencyEnabled(
            InterfaceType aInterfaceType)
    {
        return PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.API) && isApiEnabled(aInterfaceType) && mIsApiListLatencyEnabled;
    }

    private boolean isApiEnabled(
            InterfaceType aInterfaceType)
    {
        return PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.API) && (aInterfaceType != null) && mApiList.contains(aInterfaceType);
    }

    boolean isMetricsEnabledForSpecificUsers()
    {
        return PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.API) && !mApiUsersList.isEmpty()
                && (mApiUsersAcceptEnabled || mApiUsersStatusEnabled || mApiUsersLatencyEnabled);
    }

    boolean isApiUsersAcceptEnabled()
    {
        return PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.API) && mApiUsersAcceptEnabled;
    }

    boolean isApiUsersStatusEnabled()
    {
        return PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.API) && mApiUsersStatusEnabled;
    }

    boolean isApiUsersLatencyEnabled()
    {
        return PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.API) && mApiUsersLatencyEnabled;
    }

    boolean isApiUserAcceptEnabled(
            InterfaceType aInterfaceType,
            String aUsername)
    {
        return isMetricsEnabledForSpecificUsers() && isApiEnabled(aInterfaceType) && mApiUsersAcceptEnabled && PrometheusController.checkForUser(mApiUsersList, aUsername);
    }

    boolean isApiUserStatusEnabled(
            InterfaceType aInterfaceType,
            String aUsername)
    {
        return isMetricsEnabledForSpecificUsers() && isApiEnabled(aInterfaceType) && mApiUsersStatusEnabled && PrometheusController.checkForUser(mApiUsersList, aUsername);
    }

    boolean isApiUserLatencyEnabled(
            InterfaceType aInterfaceType,
            String aUsername)
    {
        return isMetricsEnabledForSpecificUsers() && isApiEnabled(aInterfaceType) && mApiUsersLatencyEnabled && PrometheusController.checkForUser(mApiUsersList, aUsername);
    }

}
