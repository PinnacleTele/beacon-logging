package com.itextos.beacon.commonlib.commondbpool;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.pwdencryption.Encryptor;

class DataSourceConfig
        extends
        DataSourceConstants
{

    private static final Log log                           = LogFactory.getLog(DataSourceConfig.class);

    private final JndiInfo   dbConID;
    private String           url;
    private String           driverClassName;
    private String           username;
    private String           password;
    private int              initialSize                   = DEFAULT_INITIAL_SIZE;
    /**
     * <code>'maxActive'</code> will be ignored by DBCP. Use
     * <code>'maxTotal'</code>. If <code>'maxTotal'</code> is not specified
     * default will be taken as '8'.
     */
    private int              maxActive                     = DEFAULT_MAX_ACTIVE;
    private int              maxTotal                      = DEFAULT_MAX_TOTAL;
    private int              maxIdle                       = DEFAULT_MAX_IDLE;
    private int              minIdle                       = DEFAULT_MIN_IDLE;
    /**
     * <code>'maxWait'</code> will be ignored by DBCP. Use
     * <code>'maxWaitMillis'</code>. If <code>'maxWaitMillis'</code> not
     * specified default will be taken as '-1'.
     */
    private int              maxWait                       = DEFAULT_MAX_WAIT;
    private int              maxWaitMillis                 = DEFAULT_MAX_WAIT_IN_MILLIS;
    private int              timeBetweenEvictionRunsMillis = DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
    private int              numTestsPerEvictionRun        = DEFAULT_NUM_TESTS_PER_EVICTION_RUN;
    private int              minEvictableIdleTimeMillis    = DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
    private String           validationQuery               = DEFAULT_VALIDATION_QUERY;
    private boolean          testOnBorrow                  = DEFAULT_TEST_ON_BORROW;
    /**
     * <code>removeAbandoned</code> will be ignored by DBCP. Use
     * <code>'removeAbandonedOnBorrow'</code> and / or
     * <code>'removeAbandonedOnMaintenance'</code>. if
     * <code>'removeAbandonedOnBorrow'</code> and / or
     * <code>'removeAbandonedOnMaintenance'</code> not specified both will have
     * default value set to false.
     */
    private boolean          removeAbandoned               = DEFAULT_REMOVE_ABANDONED;
    private boolean          removeAbandonedOnMaintenance  = DEFAULT_REMOVE_ABANDONED_ON_MAINTENANCE;
    private boolean          removeAbandonedOnBorrow       = DEFAULT_REMOVE_ABANDONED_ON_BORROW;
    private int              removeAbandonedTimeout        = DEFAULT_REMOVE_ABANDONED_TIMEOUT;
    private boolean          logAbandoned                  = DEFAULT_LOG_ABANDONED;
    private boolean          abandonedUsageTracking        = DEFAULT_ABANDONED_USAGE_TRACKING;
    private String           connectionProperties          = " ";
    private Properties       props                         = null;

    DataSourceConfig(
            JndiInfo aConID)
    {
        this(aConID, null);
    }

    DataSourceConfig(
            JndiInfo aConID,
            Properties aProps)
    {
        dbConID = aConID;

        if (aProps != null)
        {
            url                           = aProps.getProperty("url");
            driverClassName               = aProps.getProperty("driverClassName");
            username                      = aProps.getProperty("username");
            password                      = getDecryptedPassword(url, username, aProps.getProperty("password"));
            initialSize                   = Integer.parseInt(aProps.getProperty("initialSize", DEFAULT_INITIAL_SIZE + ""));
            maxActive                     = Integer.parseInt(aProps.getProperty("maxActive", DEFAULT_MAX_ACTIVE + ""));
            maxTotal                      = Integer.parseInt(aProps.getProperty("maxTotal", DEFAULT_MAX_TOTAL + ""));
            maxIdle                       = Integer.parseInt(aProps.getProperty("maxIdle", DEFAULT_MAX_IDLE + ""));
            minIdle                       = Integer.parseInt(aProps.getProperty("minIdle", DEFAULT_MIN_IDLE + ""));
            maxWait                       = Integer.parseInt(aProps.getProperty("maxWait", DEFAULT_MAX_WAIT + ""));
            maxWaitMillis                 = Integer.parseInt(aProps.getProperty("maxWaitMillis", DEFAULT_MAX_WAIT_IN_MILLIS + ""));
            timeBetweenEvictionRunsMillis = Integer.parseInt(aProps.getProperty("timeBetweenEvictionRunsMillis", DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS + ""));
            numTestsPerEvictionRun        = Integer.parseInt(aProps.getProperty("numTestsPerEvictionRun", DEFAULT_NUM_TESTS_PER_EVICTION_RUN + ""));
            minEvictableIdleTimeMillis    = Integer.parseInt(aProps.getProperty("minEvictableIdleTimeMillis", DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS + ""));
            validationQuery               = aProps.getProperty("validationQuery", DEFAULT_VALIDATION_QUERY);
            testOnBorrow                  = Boolean.parseBoolean(aProps.getProperty("testOnBorrow", DEFAULT_TEST_ON_BORROW + ""));
            removeAbandoned               = Boolean.parseBoolean(aProps.getProperty("removeAbandoned", DEFAULT_REMOVE_ABANDONED + ""));
            removeAbandonedOnMaintenance  = Boolean.parseBoolean(aProps.getProperty("removeAbandonedOnMaintenance", DEFAULT_REMOVE_ABANDONED_ON_MAINTENANCE + ""));
            removeAbandonedOnBorrow       = Boolean.parseBoolean(aProps.getProperty("removeAbandonedOnBorrow", DEFAULT_REMOVE_ABANDONED_ON_BORROW + ""));
            removeAbandonedTimeout        = Integer.parseInt(aProps.getProperty("removeAbandonedTimeout", DEFAULT_REMOVE_ABANDONED_TIMEOUT + ""));
            logAbandoned                  = Boolean.parseBoolean(aProps.getProperty("logAbandoned", DEFAULT_LOG_ABANDONED + ""));
            abandonedUsageTracking        = Boolean.parseBoolean(aProps.getProperty("abandonedUsageTracking", DEFAULT_ABANDONED_USAGE_TRACKING + ""));
            connectionProperties          = aProps.getProperty("connectionProperties", "");
        }
    }

    Properties getConfigAsProperties()
    {

        if (props == null)
        {
            props = new Properties();

            props.put("url", url);
            props.put("driverClassName", driverClassName);
            props.put("username", username);
            props.put("password", password);
            props.put("initialSize", Integer.toString(initialSize));
            props.put("maxActive", Integer.toString(maxActive));
            props.put("maxIdle", Integer.toString(maxIdle));
            props.put("minIdle", Integer.toString(minIdle));
            props.put("minEvictableIdleTimeMillis", Integer.toString(minEvictableIdleTimeMillis));
            props.put("timeBetweenEvictionRunsMillis", Integer.toString(timeBetweenEvictionRunsMillis));
            props.put("numTestsPerEvictionRun", Integer.toString(numTestsPerEvictionRun));
            props.put("maxTotal", Integer.toString(maxTotal));
            props.put("maxWait", Integer.toString(maxWait));
            props.put("maxWaitMillis", Integer.toString(maxWaitMillis));

            // ***************************************
            // Due to communication link error and hung issue the below codes
            // were commented. >>>>>>>>>>>>>>>>>>>> START
            // ***************************************

            // props.put("validationQuery", validationQuery);
            // props.put("testOnBorrow", new Boolean(testOnBorrow).toString());
            // props.put("removeAbandoned", new
            // Boolean(removeAbandoned).toString());
            // props.put("removeAbandonedOnMaintenance", new
            // Boolean(removeAbandonedOnMaintenance).toString());
            // props.put("removeAbandonedOnBorrow", new
            // Boolean(removeAbandonedOnBorrow).toString());
            // props.put("removeAbandonedTimeout", new
            // Integer(removeAbandonedTimeout).toString());
            // props.put("logAbandoned", new Boolean(logAbandoned).toString());
            // props.put("abandonedUsageTracking", new
            // Boolean(abandonedUsageTracking).toString());
            // props.put("connectionProperties", connectionProperties);

            // ***************************************
            // Due to communication link error and hung issue the above codes
            // were commented. >>>>>>>>>>>>>>>>>>>> END
            // ***************************************
        }
        return props;
    }

    JndiInfo getDbConID()
    {
        return dbConID;
    }

    void setUrl(
            String aUrl)
    {
        url = aUrl;
    }

    void setDriverClassName(
            String aDriverClassName)
    {
        driverClassName = aDriverClassName;
    }

    void setUsername(
            String aUsername)
    {
        username = aUsername;
    }

    void setPassword(
            String aPassword)
    {
        password = aPassword;
    }

    void setInitialSize(
            String aInitialSize)
    {

        try
        {
            setInitialSize(Integer.parseInt(aInitialSize));
        }
        catch (final Exception e)
        {
            setInitialSize(DEFAULT_INITIAL_SIZE);
        }
    }

    void setInitialSize(
            int aInitialSize)
    {
        initialSize = aInitialSize;
    }

    void setMaxActive(
            String aMaxActive)
    {

        try
        {
            setMaxActive(Integer.parseInt(aMaxActive));
        }
        catch (final Exception e)
        {
            setMaxActive(DEFAULT_MAX_ACTIVE);
        }
    }

    void setMaxActive(
            int aMaxActive)
    {
        maxActive = aMaxActive;
    }

    void setMaxTotal(
            String aMaxTotal)
    {

        try
        {
            setMaxTotal(Integer.parseInt(aMaxTotal));
        }
        catch (final Exception e)
        {
            setMaxTotal(DEFAULT_MAX_TOTAL);
        }
    }

    void setMaxTotal(
            int aMaxTotal)
    {
        maxTotal = aMaxTotal;
    }

    void setMaxIdle(
            String aMaxIdle)
    {

        try
        {
            setMaxIdle(Integer.parseInt(aMaxIdle));
        }
        catch (final Exception e)
        {
            setMaxIdle(DEFAULT_MAX_IDLE);
        }
    }

    void setMaxIdle(
            int aMaxIdle)
    {
        maxIdle = aMaxIdle;
    }

    void setMinIdle(
            String aMinIdle)
    {

        try
        {
            setMinIdle(Integer.parseInt(aMinIdle));
        }
        catch (final Exception e)
        {
            setMinIdle(DEFAULT_MIN_IDLE);
        }
    }

    void setMinIdle(
            int aMinIdle)
    {
        minIdle = aMinIdle;
    }

    void setMaxWait(
            String aMaxWait)
    {

        try
        {
            setMaxWait(Integer.parseInt(aMaxWait));
        }
        catch (final Exception e)
        {
            setMaxWait(DEFAULT_MAX_WAIT);
        }
    }

    void setMaxWait(
            int aMaxWait)
    {
        maxWait = aMaxWait;
    }

    void setMaxWaitMillis(
            String aMaxWaitMillis)
    {

        try
        {
            setMaxWaitMillis(Integer.parseInt(aMaxWaitMillis));
        }
        catch (final Exception e)
        {
            setMaxWaitMillis(DEFAULT_MAX_WAIT_IN_MILLIS);
        }
    }

    void setMaxWaitMillis(
            int aMaxWaitMillis)
    {
        maxWaitMillis = aMaxWaitMillis;
    }

    void setTimeBetweenEvictionRunsMillis(
            String aTimeBetweenEvictionRunsMillis)
    {

        try
        {
            setTimeBetweenEvictionRunsMillis(Integer.parseInt(aTimeBetweenEvictionRunsMillis));
        }
        catch (final Exception e)
        {
            setTimeBetweenEvictionRunsMillis(DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS);
        }
    }

    void setTimeBetweenEvictionRunsMillis(
            int aTimeBetweenEvictionRunsMillis)
    {
        timeBetweenEvictionRunsMillis = aTimeBetweenEvictionRunsMillis;
    }

    void setNumTestsPerEvictionRun(
            String aNumTestsPerEvictionRun)
    {

        try
        {
            setNumTestsPerEvictionRun(Integer.parseInt(aNumTestsPerEvictionRun));
        }
        catch (final Exception e)
        {
            setNumTestsPerEvictionRun(DEFAULT_NUM_TESTS_PER_EVICTION_RUN);
        }
    }

    void setNumTestsPerEvictionRun(
            int aNumTestsPerEvictionRun)
    {
        numTestsPerEvictionRun = aNumTestsPerEvictionRun;
    }

    void setMinEvictableIdleTimeMillis(
            String aMinEvictableIdleTimeMillis)
    {

        try
        {
            setMinEvictableIdleTimeMillis(Integer.parseInt(aMinEvictableIdleTimeMillis));
        }
        catch (final Exception e)
        {
            setMinEvictableIdleTimeMillis(DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS);
        }
    }

    void setMinEvictableIdleTimeMillis(
            int aMinEvictableIdleTimeMillis)
    {
        minEvictableIdleTimeMillis = aMinEvictableIdleTimeMillis;
    }

    void setValidationQuery(
            String aValidationQuery)
    {
        validationQuery = aValidationQuery;
    }

    void setTestOnBorrow(
            String aTestOnBorrow)
    {

        try
        {
            setTestOnBorrow(Boolean.parseBoolean(aTestOnBorrow));
        }
        catch (final Exception e)
        {
            if (BOOLEAN_DB_EQUIVALENT_TRUE.equals(aTestOnBorrow))
                setTestOnBorrow(true);
            else
                if (BOOLEAN_DB_EQUIVALENT_FALSE.equals(aTestOnBorrow))
                    setTestOnBorrow(false);
                else
                    setTestOnBorrow(DEFAULT_TEST_ON_BORROW);
        }
    }

    void setTestOnBorrow(
            boolean aTestOnBorrow)
    {
        testOnBorrow = aTestOnBorrow;
    }

    void setRemoveAbandoned(
            String aRemoveAbandoned)
    {

        try
        {
            setRemoveAbandoned(Boolean.parseBoolean(aRemoveAbandoned));
        }
        catch (final Exception e)
        {
            if (BOOLEAN_DB_EQUIVALENT_TRUE.equals(aRemoveAbandoned))
                setRemoveAbandoned(true);
            else
                if (BOOLEAN_DB_EQUIVALENT_FALSE.equals(aRemoveAbandoned))
                    setRemoveAbandoned(false);
                else
                    setRemoveAbandoned(DEFAULT_REMOVE_ABANDONED);
        }
    }

    void setRemoveAbandoned(
            boolean aRemoveAbandoned)
    {
        removeAbandoned = aRemoveAbandoned;
    }

    void setRemoveAbandonedOnMaintenance(
            String aRemoveAbandonedOnMaintenance)
    {

        try
        {
            setRemoveAbandonedOnMaintenance(Boolean.parseBoolean(aRemoveAbandonedOnMaintenance));
        }
        catch (final Exception e)
        {
            if (BOOLEAN_DB_EQUIVALENT_TRUE.equals(aRemoveAbandonedOnMaintenance))
                setRemoveAbandonedOnMaintenance(true);
            else
                if (BOOLEAN_DB_EQUIVALENT_FALSE.equals(aRemoveAbandonedOnMaintenance))
                    setRemoveAbandonedOnMaintenance(false);
                else
                    setRemoveAbandonedOnMaintenance(DEFAULT_REMOVE_ABANDONED_ON_MAINTENANCE);
        }
    }

    void setRemoveAbandonedOnMaintenance(
            boolean aRemoveAbandonedOnMaintenance)
    {
        removeAbandonedOnMaintenance = aRemoveAbandonedOnMaintenance;
    }

    void setRemoveAbandonedOnBorrow(
            String aRemoveAbandonedOnBorrow)
    {

        try
        {
            setRemoveAbandonedOnBorrow(Boolean.parseBoolean(aRemoveAbandonedOnBorrow));
        }
        catch (final Exception e)
        {
            if (BOOLEAN_DB_EQUIVALENT_TRUE.equals(aRemoveAbandonedOnBorrow))
                setRemoveAbandonedOnBorrow(true);
            else
                if (BOOLEAN_DB_EQUIVALENT_FALSE.equals(aRemoveAbandonedOnBorrow))
                    setRemoveAbandonedOnBorrow(false);
                else
                    setRemoveAbandonedOnBorrow(DEFAULT_REMOVE_ABANDONED_ON_BORROW);
        }
    }

    void setRemoveAbandonedOnBorrow(
            boolean aRemoveAbandonedOnBorrow)
    {
        removeAbandonedOnBorrow = aRemoveAbandonedOnBorrow;
    }

    void setRemoveAbandonedTimeout(
            String aRemoveAbandonedTimeout)
    {

        try
        {
            setRemoveAbandonedTimeout(Integer.parseInt(aRemoveAbandonedTimeout));
        }
        catch (final Exception e)
        {
            setRemoveAbandonedTimeout(DEFAULT_REMOVE_ABANDONED_TIMEOUT);
        }
    }

    void setRemoveAbandonedTimeout(
            int aRemoveAbandonedTimeout)
    {
        removeAbandonedTimeout = aRemoveAbandonedTimeout;
    }

    void setLogAbandoned(
            String aLogAbandoned)
    {

        try
        {
            setLogAbandoned(Boolean.parseBoolean(aLogAbandoned));
        }
        catch (final Exception e)
        {
            if (BOOLEAN_DB_EQUIVALENT_TRUE.equals(aLogAbandoned))
                setLogAbandoned(true);
            else
                if (BOOLEAN_DB_EQUIVALENT_FALSE.equals(aLogAbandoned))
                    setLogAbandoned(false);
                else
                    setLogAbandoned(DEFAULT_LOG_ABANDONED);
        }
    }

    void setLogAbandoned(
            boolean aLogAbandoned)
    {
        logAbandoned = aLogAbandoned;
    }

    void setAbandonedUsageTracking(
            String aAbandonedUsageTracking)
    {

        try
        {
            setAbandonedUsageTracking(Boolean.parseBoolean(aAbandonedUsageTracking));
        }
        catch (final Exception e)
        {
            if (BOOLEAN_DB_EQUIVALENT_TRUE.equals(aAbandonedUsageTracking))
                setAbandonedUsageTracking(true);
            else
                if (BOOLEAN_DB_EQUIVALENT_FALSE.equals(aAbandonedUsageTracking))
                    setAbandonedUsageTracking(false);
                else
                    setAbandonedUsageTracking(DEFAULT_ABANDONED_USAGE_TRACKING);
        }
    }

    void setAbandonedUsageTracking(
            boolean aAbandonedUsageTracking)
    {
        abandonedUsageTracking = aAbandonedUsageTracking;
    }

    void setConnectionProperties(
            String aConnectionProperties)
    {
        connectionProperties = aConnectionProperties;
    }

    static String getDecryptedPassword(
            String aUrl,
            String aUsername,
            String aEncryptedPassword)
    {

        try
        {
            return Encryptor.getDecryptedDbPassword(aEncryptedPassword);
        }
        catch (final Exception e)
        {
            final String s = ">>>>>>>>>>>>>>>>> Invalid password for user '" + aUsername + "' for URL '" + aUrl + "'";
             log.error(s, e);
        }

        return null;
    }

}