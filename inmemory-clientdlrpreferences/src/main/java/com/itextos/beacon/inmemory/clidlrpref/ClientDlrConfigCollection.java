package com.itextos.beacon.inmemory.clidlrpref;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.DlrEnable;
import com.itextos.beacon.commonlib.constants.DlrHandoverMode;
import com.itextos.beacon.commonlib.constants.InterfaceType;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.inmemory.loader.process.AbstractAutoRefreshInMemoryProcessor;
import com.itextos.beacon.inmemory.loader.process.InmemoryInput;

public class ClientDlrConfigCollection
        extends
        AbstractAutoRefreshInMemoryProcessor
{

    private static final Log             log               = LogFactory.getLog(ClientDlrConfigCollection.class);

    private Map<String, ClientDlrConfig> userMediaMap      = new HashMap<>();
    private Map<String, String>          userMediaQueryMap = new HashMap<>();

    public ClientDlrConfigCollection(
            InmemoryInput aInmemoryInputDetail)
    {
        super(aInmemoryInputDetail);
    }

    public boolean getDlrQueryConfig(
            String aClientId,
            String aApp)
    {
        final String lKey = generateClientMediaQueryKey(aClientId, aApp);
        return CommonUtility.isEnabled(userMediaQueryMap.get(lKey));
    }

    public ClientDlrConfig getDlrQueryConfig(
            String aClientId,
            String aApp,
            InterfaceType aInterfaceType,
            String aExplitRequest)
    {
        final String          lKey           = generateClientMediaKey(aClientId, aApp, aInterfaceType);
        final ClientDlrConfig lClientDlrConf = userMediaMap.get(lKey);

        if (lClientDlrConf == null)
            return null;

        switch (lClientDlrConf.getDlrEnabled())
        {
            case ALWAYS_ON:
                if (lClientDlrConf.isDlrQueryEnabled())
                    return lClientDlrConf;
                break;

            case EXPLICIT_REQ:
                if ("1".equals(aExplitRequest) && (lClientDlrConf.isDlrQueryEnabled()))
                    return lClientDlrConf;
                break;

            default:
            case ALWAYS_OFF:
                return null;
        }
        return null;
    }

    public ClientDlrConfig getDlrHandoverConfig(
            String aClientId,
            String aApp,
            InterfaceType aInterfaceType,
            boolean aExplitRequest)
    {
        final String lKey = generateClientMediaKey(aClientId, aApp, aInterfaceType);

        /*
        if (log.isDebugEnabled())
        {
            log.debug("getDlrHandoverConfig() - Key : " + lKey);
            log.debug("getDlrHandoverConfig() - userMediaMap  : " + userMediaMap);
        }
		*/
        
        final ClientDlrConfig lClientDlrConf = userMediaMap.get(lKey);

        if (log.isDebugEnabled())
            log.debug("getDlrHandoverConfig() - Client Dlr Config : " + lClientDlrConf);

        if (lClientDlrConf == null)
            return null;

        switch (lClientDlrConf.getDlrHandoverMode())
        {
            case API:
            case SMPP:
                break;

            case NODLR:
            case FTP:
            default:
                return null;
        }

        switch (lClientDlrConf.getDlrEnabled())
        {
            case ALWAYS_ON:
                return lClientDlrConf;

            case EXPLICIT_REQ:
                if (aExplitRequest)
                    return lClientDlrConf;
                break;

            default:
            case ALWAYS_OFF:
                return null;
        }
        return null;
    }

    public ClientDlrConfig getClientDlrConfig(
            String aClientId,
            String aApp,
            InterfaceType aInterfaceType,
            String aDlrType)
    {
        ClientDlrConfig lClientDlrConf = null;

        try
        {
            final String lKey = generateClientMediaKey(aClientId, aApp, aInterfaceType);

            lClientDlrConf = userMediaMap.get(lKey);

            if (lClientDlrConf == null)
                return null;

            if (!lClientDlrConf.isDlrQueryEnabled())
                return null;

            switch (lClientDlrConf.getDlrEnabled())
            {
                case ALWAYS_OFF:
                    return null;

                case ALWAYS_ON:
                    break;

                case EXPLICIT_REQ:
                    if ((!"1".equals(aDlrType) && !lClientDlrConf.isDlrQueryEnabled()))
                        return null;
                    break;

                default:
                    break;
            }

            if ((DlrEnable.EXPLICIT_REQ == lClientDlrConf.getDlrEnabled()) && (!"1".equals(aDlrType) && !lClientDlrConf.isDlrQueryEnabled()))
                return null;

            if ((DlrHandoverMode.NODLR == lClientDlrConf.getDlrHandoverMode()) && !lClientDlrConf.isDlrQueryEnabled())
                return null;
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return lClientDlrConf;
    }

    @Override
    protected void processResultSet(
            ResultSet aResultSet)
            throws SQLException
    {
        if (log.isDebugEnabled())
            log.debug("Calling the resultset process of " + this.getClass());

        // Table : client_handover_config

        final Map<String, ClientDlrConfig> lTempClientDlrConfigMap = new HashMap<>();
        final Map<String, String>          lTempClientQueryMap     = new HashMap<>();

        while (aResultSet.next())
        {
            final ClientDlrConfig lClientDlrConfig = new ClientDlrConfig(aResultSet.getString("cli_id"), //
                    aResultSet.getString("app"), //
                    InterfaceType.getType(aResultSet.getString("request_interface")), //
                    DlrEnable.getDlrConfig(aResultSet.getString("dlr_enabled")), //
                    DlrHandoverMode.getInterfaceDlrType(aResultSet.getString("dlr_handover_mode")), //
                    CommonUtility.isEnabled(aResultSet.getString("dlr_query_enabled")), //
                    CommonUtility.isEnabled(aResultSet.getString("dlr_on_success")), //
                    CommonUtility.isEnabled(aResultSet.getString("dlr_on_carrier_failure")), //
                    CommonUtility.isEnabled(aResultSet.getString("dlr_on_platform_fail")), //
                    ClientDlrAdminDelivery.getAdminDelivery(aResultSet.getInt("dlr_to_su")), //
                    CommonUtility.isEnabled(aResultSet.getString("client_specific_http_topic")));

            lTempClientDlrConfigMap.put(lClientDlrConfig.getKey(), lClientDlrConfig);

            if (lClientDlrConfig.isDlrQueryEnabled())
                lTempClientQueryMap.put(lClientDlrConfig.getQueryKey(), aResultSet.getString("dlr_query_enabled"));
        }

        if (!lTempClientDlrConfigMap.isEmpty())
        {
            userMediaMap      = lTempClientDlrConfigMap;
            userMediaQueryMap = lTempClientQueryMap;
        }
    }

    public static String generateClientMediaKey(
            String aClientId,
            String aApp,
            InterfaceType aInterfaceType)
    {
        return CommonUtility.combine(aClientId, aApp, (aInterfaceType != null ? aInterfaceType.getKey() : ""));
    }

    public static String generateClientMediaQueryKey(
            String aClientId,
            String aApp)
    {
        return CommonUtility.combine(aClientId, aApp);
    }

}
