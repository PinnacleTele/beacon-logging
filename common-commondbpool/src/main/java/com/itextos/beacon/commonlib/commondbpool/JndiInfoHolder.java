package com.itextos.beacon.commonlib.commondbpool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JndiInfoHolder
{

    private static final Log log = LogFactory.getLog(JndiInfoHolder.class);

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final JndiInfoHolder INSTANCE = new JndiInfoHolder();

    }

    /**
     * Use static method {@link #getJndiInfo(int)} or
     * {@link #getJndiInfoUsingName(String)}
     */
    @Deprecated
    public static JndiInfoHolder getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private JndiInfoHolder()
    {}

    public static JndiInfo getJndiInfo(
            int aId)
    {
        final JndiInfo jndiInfo = InitializeConnectionPool.getInstance().getJndiInfo(aId);

        if (jndiInfo == null)
            log.fatal("Unable to find JNDI info for the id '" + aId + "'");

        return jndiInfo;
    }

    public static JndiInfo getJndiInfoUsingName(
            String aName)
    {
        final int      lJndiProperty = JndiIdProperties.getInstance().getJndiProperty(aName);
        final JndiInfo returnValue   = getJndiInfo(lJndiProperty);

        if (returnValue == null)
            log.fatal("Unbale to get the JndiInfo detail for Name '" + aName + "' From Properties '" + lJndiProperty + "'");

        return returnValue;
    }

}