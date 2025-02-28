package com.itextos.beacon.commonlib.prometheusmetricsutil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;

class PrometheusController
        implements
        ITimedProcess
{

    private static final Log    log                                             = LogFactory.getLog(PrometheusController.class);
    public static final String  ALL                                             = "*";

    private static final String PROP_PROMETHEUS_ENABLED                         = "prometheus.enabled";

    private static final String PROP_KAFKA_PRODUCER_COUNTER                     = "kafka.producer.counter.enabled";
    private static final String PROP_KAFKA_CONSUMER_COUNTER                     = "kafka.consumer.counter.enabled";

    private static final String PROP_UI_USERS                                   = "ui.users";
    private static final String PROP_UI_COUNTER_ENABLED                         = "ui.counter.enabled";

    private static final String PROP_FTP_USERS                                  = "ftp.users";
    private static final String PROP_FTP_COUNTER_ENABLED                        = "ftp.counter.enabled";

    private static final String PROP_PLATFORM_COUNTER_COMPONENTS                = "platform.counter.components";
    private static final String PROP_PLATFORM_LATENCY_COMPONENTS                = "platform.latency.components";

    private static final String PROP_PLATFORM_COUNTER                           = "platform.counter.enabled";
    private static final String PROP_PLATFORM_LATENCY                           = "platform.latency.enabled";
    private static final String PROP_HISTOGRAM_LATENCY_BUCKETS                  = "histogram.latency.buckets";

    private static final String PROP_COMPONENT_METHOD_LATENCY_ENABLED           = "component.method.latency.enabled";
    private static final String PROP_COMPONENT_METHOD_LATENCY_COMPONENTS        = "component.method.latency.components";
    private static final String PROP_COMPONENT_GENERIC_ERROR_ENABLED_COMPONENTS = "generic.error.eneabled.components";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final PrometheusController INSTANCE = new PrometheusController();

    }

    static PrometheusController getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final PropertiesConfiguration mPropConf;
    private boolean                       mCanContinue   = true;
    private final TimedProcessor          mTimedProcessor;

    private Set<PrometheusMetricProvider> mPrometheusEnabled;

    private boolean                       mKafkaProducerCounterEnabled;
    private boolean                       mKafkaConsumerCounterEnabled;

    private boolean                       mIsUiCounterEnabled;
    private Set<String>                   mUiUsersList;

    private boolean                       mIsFtpCounterEnabled;
    private Set<String>                   mFtpUsersList;

    private boolean                       mIsComponentCounterEnabled;
    private boolean                       mIsComponentLatencyEnabled;
    private Set<Component>                mComponentCounterList;
    private Set<Component>                mComponentLatencyList;
    private double[]                      mBucketValues;

    private boolean                       mIsComponentMethodLatencyEnabled;
    private Set<Component>                mMethodLatencyComponentsList;
    private Set<Component>                mGenericErrorComponentList;

    private final ApiController           apiController  = new ApiController();
    private final SmppController          smppController = new SmppController();
    private boolean                       isFirstLoad    = true;

    private PrometheusController()
    {
        mPropConf = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.PROMETHEUS_CONTROL_PROPERTIES, false);

        loadProperties();

        isFirstLoad     = false;

        mTimedProcessor = new TimedProcessor("PrometheusControl", this, TimerIntervalConstant.PROMETHEUS_CONTROL_REFRESH);

        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "PrometheusControl");
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {
        loadProperties();
        return false;
    }

    ApiController getApiController()
    {
        return apiController;
    }

    SmppController getSmppController()
    {
        return smppController;
    }

    boolean isPrometheusEnabled(
            PrometheusMetricProvider aMetricProvider)
    {
        return (aMetricProvider != null) && mPrometheusEnabled.contains(aMetricProvider);
    }

    private void loadUiFtpProperties()
    {
        mIsUiCounterEnabled  = CommonUtility.isEnabled(mPropConf.getString(PROP_UI_COUNTER_ENABLED));
        mUiUsersList         = getList(mPropConf, PROP_UI_USERS, "UI Users List");

        mIsFtpCounterEnabled = CommonUtility.isEnabled(mPropConf.getString(PROP_FTP_COUNTER_ENABLED));
        mFtpUsersList        = getList(mPropConf, PROP_FTP_USERS, "FTP Users List");
    }

    boolean isKafkaProducerCounterEnabled()
    {
        return isPrometheusEnabled(PrometheusMetricProvider.KAFKA) && mKafkaProducerCounterEnabled;
    }

    boolean isKafkaConsumerCounterEnabled()
    {
        return isPrometheusEnabled(PrometheusMetricProvider.KAFKA) && mKafkaConsumerCounterEnabled;
    }

    boolean isUiCounterEnabled()
    {
        return isPrometheusEnabled(PrometheusMetricProvider.UI) && mIsUiCounterEnabled;
    }

    boolean isFtpCounterEnabled()
    {
        return isPrometheusEnabled(PrometheusMetricProvider.FTP) && mIsFtpCounterEnabled;
    }

    boolean isUiCounterEnabled(
            String aUsername)
    {
        return isUiCounterEnabled() && checkForUser(mUiUsersList, aUsername);
    }

    boolean isFtpCounterEnabled(
            String aUsername)
    {
        return isFtpCounterEnabled() && checkForUser(mFtpUsersList, aUsername);
    }

    boolean isPlatformCounterEnabled()
    {
        return isPrometheusEnabled(PrometheusMetricProvider.PLATFORM) && mIsComponentCounterEnabled;
    }

    boolean isPlatformLatencyEnabled()
    {
        return isPrometheusEnabled(PrometheusMetricProvider.PLATFORM) && mIsComponentLatencyEnabled;
    }

    boolean isPlatformCounterEnabled(
            Component aComponent)
    {
        return isPlatformCounterEnabled() && (aComponent != null) && mComponentCounterList.contains(aComponent);
    }

    boolean isPlatformLatencyEnabled(
            Component aComponent)
    {
        return isPlatformLatencyEnabled() && (aComponent != null) && mComponentLatencyList.contains(aComponent);
    }

    boolean isGenericErrorEnabled(
            Component aComponent)
    {
        return isGenericErrorEnabled() && (aComponent != null) && mGenericErrorComponentList.contains(aComponent);
    }

    private boolean isGenericErrorEnabled()
    {
        return isPrometheusEnabled(PrometheusMetricProvider.GENERIC_ERROR);
    }

    boolean isComponentMethodLatencyEnabled()
    {
        return isPlatformLatencyEnabled() && mIsComponentMethodLatencyEnabled;
    }

    boolean isComponentMethodLatencyEnabled(
            Component aComponent)
    {
        return isComponentMethodLatencyEnabled() && (aComponent != null) && mMethodLatencyComponentsList.contains(aComponent);
    }

    private void loadProperties()
    {
        mPrometheusEnabled = getPrometheusProvicerList(mPropConf, PROP_PROMETHEUS_ENABLED);

        loafKafkaCounterProperties();

        apiController.loadAPIProperties(mPropConf);

        loadUiFtpProperties();

        loadComponentsProperties();

        smppController.loadProperties(mPropConf);

        loadBucketValues();
    }

    private void loafKafkaCounterProperties()
    {
        mKafkaProducerCounterEnabled = CommonUtility.isEnabled(mPropConf.getString(PROP_KAFKA_PRODUCER_COUNTER));
        mKafkaConsumerCounterEnabled = CommonUtility.isEnabled(mPropConf.getString(PROP_KAFKA_CONSUMER_COUNTER));
    }

    private void loadBucketValues()
    {
        final List<Object> bucketValues = mPropConf.getList(PROP_HISTOGRAM_LATENCY_BUCKETS);

        if ((bucketValues != null))
        {
            int            index = 0;
            final double[] temp  = new double[bucketValues.size()];

            for (final Object obj : bucketValues)
            {
                temp[index] = Double.parseDouble((String) obj);
                index++;
            }
            mBucketValues = temp;
        }
    }

    public double[] getHistogramBuckgets()
    {
        return mBucketValues;
    }

    private void loadComponentsProperties()
    {
        mIsComponentCounterEnabled = CommonUtility.isEnabled(mPropConf.getString(PROP_PLATFORM_COUNTER));
        boolean tempLatencyEnabled = CommonUtility.isEnabled(mPropConf.getString(PROP_PLATFORM_LATENCY));
        validateComponentLatencyEnabled(tempLatencyEnabled);

        mComponentCounterList = getComponentsList(mPropConf, PROP_PLATFORM_COUNTER_COMPONENTS, "Platform Counter Components");
        Set<Component> tempComponentList = getComponentsList(mPropConf, PROP_PLATFORM_LATENCY_COMPONENTS, "Platform Latency Components");
        validateComponentLatencyList(tempComponentList);

        tempLatencyEnabled = CommonUtility.isEnabled(mPropConf.getString(PROP_COMPONENT_METHOD_LATENCY_ENABLED));
        validateComponentMethodLatencyEnabled(tempLatencyEnabled);

        tempComponentList = getComponentsList(mPropConf, PROP_COMPONENT_METHOD_LATENCY_COMPONENTS, "Method Latency Components");
        validateComponentMethodLatencyList(tempComponentList);

        tempComponentList = getComponentsList(mPropConf, PROP_COMPONENT_GENERIC_ERROR_ENABLED_COMPONENTS, "Generic Error Enabled Components");
        validateGenericErrorCode(tempComponentList);
    }

    private void validateGenericErrorCode(
            Set<Component> aTempComponentList)
    {

        if (isFirstLoad)
        {
            mGenericErrorComponentList = aTempComponentList;
            return;
        }

        if (!mGenericErrorComponentList.equals(aTempComponentList))
        {
            for (final Component c : mGenericErrorComponentList)
                if (aTempComponentList.contains(c))
                {
                    // ignore
                }
                else
                    log.error("You are disabling the Error Code counter while the applications are running." + " This will throw some exception in the log." + " So restart the applications."
                            + " <<< I am not disabling the component latency for '" + c + "'now. >>>");

            boolean updateReq = false;
            for (final Component c : aTempComponentList)
                if (mGenericErrorComponentList.contains(c))
                {
                    // ignore
                }
                else
                {
                    log.error("You are enabling the Error Code counter while the applications are running for the component '" + c + "'");
                    updateReq = true;
                }

            if (updateReq)
                mGenericErrorComponentList = aTempComponentList;
        }
    }

    private void validateComponentLatencyList(
            Set<Component> aTempComponentList)
    {

        if (isFirstLoad)
        {
            mComponentLatencyList = aTempComponentList;
            return;
        }

        if (!mComponentLatencyList.equals(aTempComponentList))
        {
            for (final Component c : mComponentLatencyList)
                if (aTempComponentList.contains(c))
                {
                    // ignore
                }
                else
                    log.error("You are disabling the Component latency while the applications are running." + " This will throw some exception in the log." + " So restart the applications."
                            + " <<< I am not disabling the component latency for '" + c + "'now. >>>");

            boolean updateReq = false;
            for (final Component c : aTempComponentList)
                if (mComponentLatencyList.contains(c))
                {
                    // ignore
                }
                else
                {
                    log.error("You are enabling the Component latency while the applications are running for the component '" + c + "'");
                    updateReq = true;
                }

            if (updateReq)
                mComponentLatencyList = aTempComponentList;
        }
    }

    private void validateComponentMethodLatencyList(
            Set<Component> aTempComponentList)
    {

        if (isFirstLoad)
        {
            mMethodLatencyComponentsList = aTempComponentList;
            return;
        }

        if (!mMethodLatencyComponentsList.equals(aTempComponentList))
        {
            for (final Component c : mMethodLatencyComponentsList)
                if (aTempComponentList.contains(c))
                {
                    // ignore
                }
                else
                    log.error("You are disabling the Component latency while the applications are running." + " This will throw some exception in the log." + " So restart the applications."
                            + " <<< I am not disabling the component latency for '" + c + "'now. >>>");

            boolean updateReq = false;
            for (final Component c : aTempComponentList)
                if (mMethodLatencyComponentsList.contains(c))
                {
                    // ignore
                }
                else
                {
                    log.error("You are enabling the Component latency while the applications are running for the component '" + c + "'");
                    updateReq = true;
                }

            if (updateReq)
                mMethodLatencyComponentsList = aTempComponentList;
        }
    }

    private void validateComponentLatencyEnabled(
            boolean aTempComponentLatencyEnabled)
    {

        if (isFirstLoad)
        {
            mIsComponentLatencyEnabled = aTempComponentLatencyEnabled;
            return;
        }

        if (mIsComponentLatencyEnabled)
        {

            if (aTempComponentLatencyEnabled)
            {
                // ignore
            }
            else
            {
                log.error("You are disabling the Component latency while the applications are running. This will stop all Latency calculations.");
                mIsComponentLatencyEnabled = false;
            }
        }
        else
            if (aTempComponentLatencyEnabled)
                log.error("You are enabling the Component latency while the applications are running." + " This will throw some exception in the log." + " So restart the applications."
                        + " <<< I am not enabling the component latency now. >>>");
            else
            {
                // ignore it
            }
    }

    private void validateComponentMethodLatencyEnabled(
            boolean aTempComponentLatencyEnabled)
    {

        if (isFirstLoad)
        {
            mIsComponentMethodLatencyEnabled = aTempComponentLatencyEnabled;
            return;
        }

        if (mIsComponentMethodLatencyEnabled)
        {

            if (aTempComponentLatencyEnabled)
            {
                // ignore
            }
            else
            {
                log.error("You are disabling the Component Method latency while the applications are running. This will stop all Latency calculations.");
                mIsComponentMethodLatencyEnabled = false;
            }
        }
        else
            if (aTempComponentLatencyEnabled)
                log.error("You are enabling the Component Method latency while the applications are running." + " This will throw some exception in the log." + " So restart the applications."
                        + " <<< I am not enabling the component latency now. >>>");
            else
            {
                // ignore it
            }
    }

    private static Set<Component> getComponentsList(
            PropertiesConfiguration aPropConf,
            String aPropKey,
            String aString)
    {
        final Set<Component> returnValue = new HashSet<>();
        final Set<String>    lList       = getList(aPropConf, aPropKey, aString);
        if (!lList.isEmpty())
            if (lList.contains(ALL))
            {
                final Component[] lValues = Component.values();
                Collections.addAll(returnValue, lValues);
            }
            else
                for (final String s : lList)
                    try
                    {
                        returnValue.add(Component.getComponent(s));
                    }
                    catch (final Exception e)
                    {
                        log.error("Unable to get the Component for '" + s + "'", e);
                    }
        return returnValue;
    }

    static boolean checkForUser(
            Set<String> aList,
            String aUsername)
    {
        if (aList.contains(ALL))
            return true;

        final String temp = CommonUtility.nullCheck(aUsername, true);
        return aList.contains(temp);
    }

    private static Set<PrometheusMetricProvider> getPrometheusProvicerList(
            PropertiesConfiguration aPropConf,
            String aPropKey)
    {
        final Set<PrometheusMetricProvider> returnValue = new HashSet<>();
        final Set<String>                   lList       = getList(aPropConf, aPropKey, "Prometheus Provider List");

        if (!lList.isEmpty())
        {
            if (log.isDebugEnabled())
                log.debug("Prometheus Provider List " + lList);

            if (lList.contains(ALL))
            {
                final PrometheusMetricProvider[] lValues = PrometheusMetricProvider.values();
                Collections.addAll(returnValue, lValues);
            }
            else
            {
                if (log.isDebugEnabled())
                    log.debug("Interface List is specific " + lList);

                getPrometheusMetricProviders(lList, returnValue);
            }
        }
        return returnValue;
    }

    private static void getPrometheusMetricProviders(
            Set<String> aList,
            Set<PrometheusMetricProvider> aReturnValue)
    {

        for (final String s : aList)
        {
            if (log.isDebugEnabled())
                log.debug("Current Interface is '" + s + "'");

            try
            {
                final PrometheusMetricProvider lType = PrometheusMetricProvider.getProvider(s);
                aReturnValue.add(lType);
            }
            catch (final Exception e)
            {
                log.error("Unable to get the interface type for '" + s + "'", e);
            }
        }
    }

    public static Set<String> getList(
            PropertiesConfiguration aPropConf,
            String aPropertyKey,
            String aDescription)
    {
        final Set<String> returnValue  = new HashSet<>();
        String            listAsString = aPropConf.getString(aPropertyKey);

        if (log.isInfoEnabled())
            log.info(aDescription + " Key '" + aPropertyKey + "' Value : '" + listAsString + "'");

        if ((listAsString != null) && (listAsString.trim().length() > 0))
        {
            listAsString = listAsString.toLowerCase();

            if (listAsString.contains(ALL))
                returnValue.add(ALL);
            else
            {
                final String[] split = listAsString.split(",");
                Collections.addAll(returnValue, split);
            }
        }

        return returnValue;
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
    }

}