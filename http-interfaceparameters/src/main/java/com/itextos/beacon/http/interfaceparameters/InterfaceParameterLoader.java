package com.itextos.beacon.http.interfaceparameters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.JndiInfo;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.constants.TimerIntervalConstant;
import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.timer.ITimedProcess;
import com.itextos.beacon.commonlib.utility.timer.TimedProcessor;
import com.itextos.beacon.commonlib.utility.tp.ExecutorSheduler;

public class InterfaceParameterLoader
        implements
        ITimedProcess
{

    private static final Log      log               = LogFactory.getLog(InterfaceParameterLoader.class);
    private static final JndiInfo CONFIGURATION_ID  = JndiInfo.CONFIGURARION_DB;
    private static final String   SQL_DEFAULT_PARAM = "select " + "a.interface_type, " + "'' as customer_id, " + "a.parameter_name, " + "default_key " + "from " + "interface_parameter_config a, "
            + "interface_parameter_master b, " + "interface_master c " + "where a.parameter_name = b.parameter_name " + "and a.interface_type = c.interface_type " + "order by a.interface_type";

    private static final String   SQL_CLIENT_PARAM  = "select " + "a.interface_type, " + "a.customer_id, " + "a.parameter_name, " + "key_name " + "from " + "interface_parameter_customer_key a, "
            + "interface_parameter_master b, " + "interface_master c " + "where a.parameter_name = b.parameter_name " + "and a.interface_type = c.interface_type " + " and b.is_customizable=1 "
            + "order by a.interface_type";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final InterfaceParameterLoader INSTANCE = new InterfaceParameterLoader();

    }

    public static InterfaceParameterLoader getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final TimedProcessor                                                     mTimedProcessor;
    private boolean                                                                  mCanContinue       = true;
    private EnumMap<InterfaceType, EnumMap<InterfaceParameter, String>>              mDefaultParamNames = new EnumMap<>(InterfaceType.class);
    private Map<String, EnumMap<InterfaceType, EnumMap<InterfaceParameter, String>>> mClientParamNames  = new HashMap<>();

    private InterfaceParameterLoader()
    {
        if (log.isDebugEnabled())
            log.debug("Loading the Interface Parameters started.");

        try
        {
            loadParameters();
        }
        catch (final Exception e)
        {
            final String s = "Exception while loading Parameter Keys the intial. Cannot continue.";
            log.error(s, e);
        //    throw new ItextosRuntimeException(s, e);
        }
       
        mTimedProcessor = new TimedProcessor("InterfaceParameterLoader", this, TimerIntervalConstant.INTERFACE_PARAMETER_LOADER);
    
        ExecutorSheduler.getInstance().addTask(mTimedProcessor, "InterfaceParameterLoader");
        if (log.isDebugEnabled())
            log.debug("Timer Thread for loading the Interface Parameters started with sleep time of 30 seconds.");
    }

    public String getParamterKey(
            InterfaceType aInterfaceType,
            InterfaceParameter aParameter)
    {
        return getParamterKey(null, aInterfaceType, aParameter);
    }

    public String getParamterKey(
            String aClientId,
            InterfaceType aInterfaceType,
            InterfaceParameter aParameter)
    {

        if (aClientId != null)
        {
            final EnumMap<InterfaceType, EnumMap<InterfaceParameter, String>> clientInterfaceMap = mClientParamNames.get(aClientId);

            if (clientInterfaceMap != null)
            {
                final EnumMap<InterfaceParameter, String> interfaceParamMap = clientInterfaceMap.get(aInterfaceType);

                if (interfaceParamMap != null)
                {
                    final String lString = interfaceParamMap.get(aParameter);

                    if (lString != null)
                        return lString;

                    if (log.isDebugEnabled())
                        log.debug("Client specific parameters not set for the Client '" + aClientId + "' and interface '" + aInterfaceType + "' and Parameter '" + aParameter
                                + "'. Giving default param key.");
                }
                else
                    if (log.isDebugEnabled())
                        log.debug("Client specific parameters not set for the Client '" + aClientId + "' and interface '" + aInterfaceType + "'");
            }
            else
                if (log.isDebugEnabled())
                    log.debug("Client specific parameters not set for the Client '" + aClientId + "'");
        }
        return getDefaultParamKey(aInterfaceType, aParameter);
    }

    private String getDefaultParamKey(
            InterfaceType aInterfaceType,
            InterfaceParameter aParameter)
    {
        final Map<InterfaceParameter, String> map             = mDefaultParamNames.get(aInterfaceType);
        String                                defaultParamKey = null;

        if (map != null)
            defaultParamKey = map.get(aParameter);

        if (defaultParamKey == null)
            log.error("For interface type '" + aInterfaceType + " 'and interface parameter '" + aParameter + "' the default parameter key is not SET.");
        return defaultParamKey;
    }

    private void loadParameters()
            throws Exception
    {

        if (log.isDebugEnabled())
        {
            log.debug("SQL to load the default Parameter Keys  : '" + SQL_DEFAULT_PARAM + "'");
            log.debug("SQL to load the Client Parameter Keys : '" + SQL_CLIENT_PARAM + "'");
        }

        Connection con = null;
        PreparedStatement pstmt1 = null;
        ResultSet rsDefault = null;
        PreparedStatement pstmt2 = null;
        ResultSet rsClient = null;
        try 
        {
        	   con = DBDataSourceFactory.getConnectionFromThin(CONFIGURATION_ID);
               pstmt1 = con.prepareStatement(SQL_DEFAULT_PARAM);
               rsDefault = pstmt1.executeQuery();
               pstmt2 = con.prepareStatement(SQL_CLIENT_PARAM);
               rsClient = pstmt2.executeQuery();
            final EnumMap<InterfaceType, EnumMap<InterfaceParameter, String>>              tempDefaultParamNames = getDefaultParams(rsDefault);
            final Map<String, EnumMap<InterfaceType, EnumMap<InterfaceParameter, String>>> tempClientParamNames  = getClientParams(rsClient);

            if (tempDefaultParamNames.size() > 0)
                mDefaultParamNames = tempDefaultParamNames;

            if (tempClientParamNames.size() > 0)
                mClientParamNames = tempClientParamNames;

            if (log.isDebugEnabled())
                log.debug("Completed loading the Default and Client Parameters.");
        }catch(Exception e) {
        	e.printStackTrace();
        }finally {
        	
        	CommonUtility.closeResultSet(rsDefault);
        	CommonUtility.closeStatement(pstmt1);
        	 CommonUtility.closeResultSet(rsClient);
             CommonUtility.closeStatement(pstmt2);
             CommonUtility.closeConnection(con);
        }
    }

    private static Map<String, EnumMap<InterfaceType, EnumMap<InterfaceParameter, String>>> getClientParams(
            ResultSet aRsDefault)
            throws SQLException
    {
        final Map<String, EnumMap<InterfaceType, EnumMap<InterfaceParameter, String>>> tempClientParamNames = new HashMap<>();

        while (aRsDefault.next())
        {
            final String interfaceType = CommonUtility.nullCheck(aRsDefault.getString(1), true);
            final String clientId      = CommonUtility.nullCheck(aRsDefault.getString(2), true);
            final String paramName     = CommonUtility.nullCheck(aRsDefault.getString(3), true);
            final String paramKey      = CommonUtility.nullCheck(aRsDefault.getString(4), true);

            if ("".equals(clientId))
                log.error("Client Id cannot be empty for interface '" + interfaceType + "' paramName '" + paramName + "' Param Key '" + paramKey + "'");

            final InterfaceType it = InterfaceType.getType(interfaceType);

            if (it == null)
            {
                log.error("Unable to get the InterfaceType type for interface '" + interfaceType + "' clientId '" + clientId + "' paramName '" + paramName + "' Param Key '" + paramKey + "'");
                continue;
            }

            final InterfaceParameter ip = InterfaceParameter.getParameter(paramName);

            if (ip == null)
            {
                log.error("Unable to get the InterfaceParameter type for interface '" + interfaceType + "' clientId '" + clientId + "' paramName '" + paramName + "' Param Key '" + paramKey + "'");
                continue;
            }

            final EnumMap<InterfaceType, EnumMap<InterfaceParameter, String>> clientInterfaceMap   = tempClientParamNames.computeIfAbsent(clientId, k -> new EnumMap<>(InterfaceType.class));
            final EnumMap<InterfaceParameter, String>                         interfaceParamKeyMap = clientInterfaceMap.computeIfAbsent(it, k -> new EnumMap<>(InterfaceParameter.class));
            interfaceParamKeyMap.put(ip, paramKey);
        }
        return tempClientParamNames;
    }

    private static EnumMap<InterfaceType, EnumMap<InterfaceParameter, String>> getDefaultParams(
            ResultSet aRsDefault)
            throws SQLException
    {
        final EnumMap<InterfaceType, EnumMap<InterfaceParameter, String>> tempDefaultParamNames = new EnumMap<>(InterfaceType.class);

        while (aRsDefault.next())
        {
            final String        interfaceType = CommonUtility.nullCheck(aRsDefault.getString(1), true);
            final String        paramName     = CommonUtility.nullCheck(aRsDefault.getString(3), true);
            final String        paramKey      = CommonUtility.nullCheck(aRsDefault.getString(4), true);

            final InterfaceType it            = InterfaceType.getType(interfaceType);

            if (it == null)
            {
                log.error("Unable to get the InterfaceType type for interface '" + interfaceType + "' paramName '" + paramName + "' Param Key '" + paramKey + "'");
                continue;
            }

            final InterfaceParameter ip = InterfaceParameter.getParameter(paramName);

            if (ip == null)
            {
                log.error("Unable to get the InterfaceParameter type for interface '" + interfaceType + "' paramName '" + paramName + "' Param Key '" + paramKey + "'");
                continue;
            }

            final EnumMap<InterfaceParameter, String> interfaceParamKeyMap = tempDefaultParamNames.computeIfAbsent(it, k -> new EnumMap<>(InterfaceParameter.class));
            interfaceParamKeyMap.put(ip, paramKey);
        }
        return tempDefaultParamNames;
    }

    @Override
    public boolean canContinue()
    {
        return mCanContinue;
    }

    @Override
    public boolean processNow()
    {

        try
        {
            loadParameters();
        }
        catch (final Exception e)
        {
            log.error("Ignorable exception.", e);
        }
        return false;
    }

    @Override
    public void stopMe()
    {
        mCanContinue = false;
    }

}