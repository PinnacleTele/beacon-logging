package com.itextos.beacon.platform.elasticsearchutil.utility;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commonpropertyloader.PropertiesPath;
import com.itextos.beacon.commonlib.commonpropertyloader.PropertyLoader;

public class ElasticsearchProperties
{

    private static final Log    log                                   = LogFactory.getLog(ElasticsearchProperties.class);

    private static final String PROP_KEY_HOST_IP                      = "elasticsearch.host.list";
    private static final String PROP_KEY_PORT                         = "elasticsearch.port";
    private static final String PROP_KEY_HOST_SCHEME                  = "elasticsearch.scheme";
    private static final String PROP_KEY_CONN_TIMEOUT_SEC             = "elasticsearch.connection.timeout.sec";
    private static final String PROP_KEY_READ_TIMEOUT_SEC             = "elasticsearch.read.timeout.sec";
    private static final String PROP_KEY_CON_EXPIRE_TIME              = "elasticsearch.con.expire.time";
    private static final String PROP_KEY_RETRY_CONFLICT_RESOLVE_COUNT = "elasticsearch.retry.conflict.resolve.count";

    private static class SingletonHolder
    {

        @SuppressWarnings("synthetic-access")
        static final ElasticsearchProperties INSTANCE = new ElasticsearchProperties();

    }

    public static ElasticsearchProperties getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private final PropertiesConfiguration mProps = PropertyLoader.getInstance().getPropertiesConfiguration(PropertiesPath.ELASTICSEARCH_SERVER_PROPERTIES, true);
    private final String[]                mHostIps;
    private final String                  mScheme;
    private final int                     mPort;
    private final int                     mConTimeoutMillis;
    private final int                     mReadTimmeoutMillis;

    private ElasticsearchProperties()
    {
        mHostIps            = mProps.getStringArray(PROP_KEY_HOST_IP);
        mScheme             = mProps.getString(PROP_KEY_HOST_SCHEME);
        mPort               = mProps.getInt(PROP_KEY_PORT);
        mConTimeoutMillis   = 1000 * mProps.getInt(PROP_KEY_CONN_TIMEOUT_SEC, 30);
        mReadTimmeoutMillis = 1000 * mProps.getInt(PROP_KEY_READ_TIMEOUT_SEC, 30);
    }

    public PropertiesConfiguration getProps()
    {
        return mProps;
    }

    public String[] getHostIps()
    {
        return mHostIps;
    }

    public String getScheme()
    {
        return mScheme;
    }

    public int getPort()
    {
        return mPort;
    }

    public long getExpireTime()
    {
        return mProps.getInt(PROP_KEY_CON_EXPIRE_TIME, 2000);
    }

    public int getRetryConflictResolveCount()
    {
        return mProps.getInt(PROP_KEY_RETRY_CONFLICT_RESOLVE_COUNT, 10);
    }

    public int getConTimeoutMillis()
    {
        return mConTimeoutMillis;
    }

    public int getReadTimmeoutMillis()
    {
        return mReadTimmeoutMillis;
    }

}