package com.itextos.beacon.commonlib.messageprocessor.process.remove;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;
import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.MiddlewareConstant;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;

public class MessageRemovePropertyReader
        implements
        ITimedProcess
{

    private static final Log    log             = LogFactory.getLog(MessageRemovePropertyReader.class);
    private static final String COMPONENT_NAMES = "component.names";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final MessageRemovePropertyReader INSTANCE = new MessageRemovePropertyReader();

    }

    public static MessageRemovePropertyReader getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private PropertiesConfiguration                  mPropConf      = null;
    private boolean                                  mCanContinue   = true;
    private final TimedProcessor                     mTimedProcessor;
    private Map<Component, List<MiddlewareConstant>> componentMcMap = new EnumMap<>(Component.class);

    private MessageRemovePropertyReader()
    {

        try
        {
            mPropConf = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.MESSAGE_KEY_REMOVE_PROPERTIES, false);
        }
        catch (final Exception e)
        {
            log.error(PropertiesPath.MESSAGE_KEY_REMOVE_PROPERTIES.getKey() + " may not be available. No keys will be removed.", e);
            mPropConf = new PropertiesConfiguration();
        }
       
        mTimedProcessor = new TimedProcessor("MessageRemovePropertyReader", this, TimerIntervalConstant.MESSAGE_REMOVE_PROPERTY_UPDATER);
 
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "MessageRemovePropertyReader");
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {
        getComponentDetails();
        return false;
    }

    private void getComponentDetails()
    {

        try
        {

            if (mPropConf != null)
            {
                final Map<Component, List<MiddlewareConstant>> tempMap = new EnumMap<>(Component.class);

                final List<Object>                             lList   = mPropConf.getList(COMPONENT_NAMES, new ArrayList<>());

                for (final Object tempObject : lList)
                {
                    final String    name      = (String) tempObject;

                    // if (log.isDebugEnabled())
                    // log.debug("Component Name " + name);

                    final Component component = Component.getComponent(name);

                    if (component == null)
                    {
                        log.error("Unable to find the component for '" + name + "'");
                        continue;
                    }
                    getMiddlewareConstants(tempMap, component, name);
                }

                if (!lList.isEmpty())
                    componentMcMap = tempMap;
            }
            else
                if (log.isDebugEnabled())
                    log.debug("Properties is null.");
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the IMessage Remove constants.", e);
        }
    }

    private void getMiddlewareConstants(
            Map<Component, List<MiddlewareConstant>> aTempMap,
            Component aComponent,
            String aComponentName)
    {
        final List<MiddlewareConstant> list             = aTempMap.computeIfAbsent(aComponent, k -> new ArrayList<>());
        final Iterator<String>         allComponentKeys = mPropConf.getKeys(aComponentName);

        while (allComponentKeys.hasNext())
        {
            final String mcIndex = allComponentKeys.next();
            final String mcName  = CommonUtility.nullCheck(mPropConf.getString(mcIndex), true);

            // if (log.isDebugEnabled())
            // log.debug("Key : '" + mcIndex + "' Value : '" + mcName + "'");

            if (!mcName.isEmpty())
            {
                final MiddlewareConstant mc = MiddlewareConstant.getMiddlewareConstantByName(mcName);

                if (mc == null)
                {
                    log.error("Unable to find a MiddlewareConstant for name '" + mcIndex + "'");
                    continue;
                }
                list.add(mc);
            }
            else
                if (log.isDebugEnabled())
                    log.debug("Not valid value specified for '" + mcIndex + "' in the properties file.");
        }

        if (list.isEmpty())
            aTempMap.remove(aComponent);
    }

    public List<MiddlewareConstant> getRemoveConstants(
            String aComponentName)
    {
        return getRemoveConstants(Component.getComponent(aComponentName));
    }

    public List<MiddlewareConstant> getRemoveConstants(
            Component aComponent)
    {
        if (aComponent != null)
            return componentMcMap.get(aComponent);
        return new ArrayList<>();
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
    }

}
