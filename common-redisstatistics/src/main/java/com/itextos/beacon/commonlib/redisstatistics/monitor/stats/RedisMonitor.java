package com.itextos.beacon.commonlib.redisstatistics.monitor.stats;

import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;

import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * There are lot of statistics available from Redis.<br>
 * As of Redis version <b><code>6.0.9</code></b>, lot of properties available.
 * <br>
 * <br>
 * But we are going to use few of them, which are specfied below.
 * <ol>
 * <li>Server
 * <ol>
 * <li>redis_version
 * <li>os
 * <li>tcp_port
 * <li>uptime_in_days
 * </ol>
 * <li>Clients
 * <ol>
 * <li>connected_clients
 * <li>blocked_clients
 * <li>tracking_clients
 * </ol>
 * <li>Memory
 * <ol>
 * <li>used_memory
 * <li>total_system_memory
 * <li>used_memory_dataset
 * <li>used_memory_lua
 * <li>maxmemory
 * </ol>
 * <li>Stats
 * <ol>
 * <li>total_connections_received
 * <li>total_commands_processed
 * <li>rejected_connections
 * </ol>
 * </ol>
 * <br>
 * <b>Below are the possible values from Redis statistics.</b><br>
 * <br>
 * <ol>
 * <li>CPU</li>
 * <ol>
 * <li>used_cpu_sys
 * <li>used_cpu_sys_children
 * <li>used_cpu_user
 * <li>used_cpu_user_children
 * </ol>
 * <li>Clients</li>
 * <ol>
 * <li>blocked_clients
 * <li>client_recent_max_input_buffer
 * <li>client_recent_max_output_buffer
 * <li>clients_in_timeout_table
 * <li>connected_clients
 * <li>tracking_clients
 * </ol>
 * <li>Cluster</li>
 * <ol>
 * <li>cluster_enabled
 * </ol>
 * <li>Memory</li>
 * <ol>
 * <li>active_defrag_running
 * <li>allocator_active
 * <li>allocator_allocated
 * <li>allocator_frag_bytes
 * <li>allocator_frag_ratio
 * <li>allocator_resident
 * <li>allocator_rss_bytes
 * <li>allocator_rss_ratio
 * <li>lazyfree_pending_objects
 * <li>maxmemory
 * <li>maxmemory_human
 * <li>maxmemory_policy
 * <li>mem_allocator
 * <li>mem_aof_buffer
 * <li>mem_clients_normal
 * <li>mem_clients_slaves
 * <li>mem_fragmentation_bytes
 * <li>mem_fragmentation_ratio
 * <li>mem_not_counted_for_evict
 * <li>mem_replication_backlog
 * <li>number_of_cached_scripts
 * <li>rss_overhead_bytes
 * <li>rss_overhead_ratio
 * <li>total_system_memory
 * <li>total_system_memory_human
 * <li>used_memory
 * <li>used_memory_dataset
 * <li>used_memory_dataset_perc
 * <li>used_memory_human
 * <li>used_memory_lua
 * <li>used_memory_lua_human
 * <li>used_memory_overhead
 * <li>used_memory_peak
 * <li>used_memory_peak_human
 * <li>used_memory_peak_perc
 * <li>used_memory_rss
 * <li>used_memory_rss_human
 * <li>used_memory_scripts
 * <li>used_memory_scripts_human
 * <li>used_memory_startup
 * </ol>
 * <li>Persistence</li>
 * <ol>
 * <li>aof_current_rewrite_time_sec
 * <li>aof_enabled
 * <li>aof_last_bgrewrite_status
 * <li>aof_last_cow_size
 * <li>aof_last_rewrite_time_sec
 * <li>aof_last_write_status
 * <li>aof_rewrite_in_progress
 * <li>aof_rewrite_scheduled
 * <li>loading
 * <li>module_fork_in_progress
 * <li>module_fork_last_cow_size
 * <li>rdb_bgsave_in_progress
 * <li>rdb_changes_since_last_save
 * <li>rdb_current_bgsave_time_sec
 * <li>rdb_last_bgsave_status
 * <li>rdb_last_bgsave_time_sec
 * <li>rdb_last_cow_size
 * <li>rdb_last_save_time
 * </ol>
 * <li>Replication</li>
 * <ol>
 * <li>connected_slaves
 * <li>master_repl_offset
 * <li>master_replid
 * <li>master_replid2
 * <li>repl_backlog_active
 * <li>repl_backlog_first_byte_offset
 * <li>repl_backlog_histlen
 * <li>repl_backlog_size
 * <li>role
 * <li>second_repl_offset
 * </ol>
 * <li>Server</li>
 * <ol>
 * <li>arch_bits
 * <li>atomicvar_api
 * <li>config_file
 * <li>configured_hz
 * <li>executable
 * <li>gcc_version
 * <li>hz
 * <li>io_threads_active
 * <li>lru_clock
 * <li>multiplexing_api
 * <li>os
 * <li>process_id
 * <li>redis_build_id
 * <li>redis_git_dirty
 * <li>redis_git_sha1
 * <li>redis_mode
 * <li>redis_version
 * <li>run_id
 * <li>tcp_port
 * <li>uptime_in_days
 * <li>uptime_in_seconds
 * </ol>
 * <li>Stats</li>
 * <ol>
 * <li>active_defrag_hits
 * <li>active_defrag_key_hits
 * <li>active_defrag_key_misses
 * <li>active_defrag_misses
 * <li>evicted_keys
 * <li>expire_cycle_cpu_milliseconds
 * <li>expired_keys
 * <li>expired_stale_perc
 * <li>expired_time_cap_reached_count
 * <li>instantaneous_input_kbps
 * <li>instantaneous_ops_per_sec
 * <li>instantaneous_output_kbps
 * <li>io_threaded_reads_processed
 * <li>io_threaded_writes_processed
 * <li>keyspace_hits
 * <li>keyspace_misses
 * <li>latest_fork_usec
 * <li>migrate_cached_sockets
 * <li>pubsub_channels
 * <li>pubsub_patterns
 * <li>rejected_connections
 * <li>slave_expires_tracked_keys
 * <li>sync_full
 * <li>sync_partial_err
 * <li>sync_partial_ok
 * <li>total_commands_processed
 * <li>total_connections_received
 * <li>total_net_input_bytes
 * <li>total_net_output_bytes
 * <li>total_reads_processed
 * <li>total_writes_processed
 * <li>tracking_total_items
 * <li>tracking_total_keys
 * <li>tracking_total_prefixes
 * <li>unexpected_error_replies
 * </ol>
 */
public class RedisMonitor
        implements
        Runnable
{

    private static final Log        log                        = LogFactory.getLog(RedisMonitor.class);

    private static final String     REDIS_VERSION              = "redis_version";
    private static final String     OPERATING_SYSTEMS          = "os";
    private static final String     TCP_PORT                   = "tcp_port";
    private static final String     UPTIME_IN_DAYS             = "uptime_in_days";
    private static final String     CONNECTED_CLIENTS          = "connected_clients";
    private static final String     BLOCKED_CLIENTS            = "blocked_clients";
    private static final String     TRACKING_CLIENTS           = "tracking_clients";
    private static final String     USED_MEMORY                = "used_memory";
    private static final String     USED_MEMORY_RSS            = "used_memory_rss";
    private static final String     TOTAL_SYSTEM_MEMORY        = "total_system_memory";
    private static final String     USED_MEMORY_DATASET        = "used_memory_dataset";
    private static final String     USED_MEMORY_LUA            = "used_memory_lua";
    private static final String     MAXMEMORY                  = "maxmemory";
    private static final String     TOTAL_CONNECTIONS_RECEIVED = "total_connections_received";
    private static final String     TOTAL_COMMANDS_PROCESSED   = "total_commands_processed";
    private static final String     REJECTED_CONNECTIONS       = "rejected_connections";

    private final Jedis             mJedis;
    private final boolean           mStatsCheck;

    private Date                    mLastUpdatedTime           = null;
    private ServerInfo              mServerInfo                = null;
    private ClientInfo              mClientInfo                = null;
    private MemoryInfo              mMemoryInfo                = null;
    private ConnectionInfo          mConnectionInfo            = null;
    private final Map<String, Long> mListQSize                 = new TreeMap<>();

    public RedisMonitor(
            Jedis aJedis,
            boolean aStatsCheck)
    {
        this(null, aJedis, aStatsCheck);
    }

    public RedisMonitor(
            RedisMonitor aFirstObject,
            Jedis aJedis,
            boolean aStatsCheck)
    {
        mJedis      = aJedis;
        mStatsCheck = aStatsCheck;

        if (!aStatsCheck && (aFirstObject != null))
        {
            mServerInfo     = aFirstObject.mServerInfo;
            mClientInfo     = aFirstObject.mClientInfo;
            mMemoryInfo     = aFirstObject.mMemoryInfo;
            mConnectionInfo = aFirstObject.mConnectionInfo;
        }
    }

    @Override
    public void run()
    {
        String host = null;
        int    port = -1;

        try (
                final Client client = mJedis.getClient();//
                final Pipeline pipe = mJedis.pipelined();//
        )
        {
            host = client.getHost();
            port = client.getPort();

            if (log.isDebugEnabled())
                log.debug("Gathering statistics information from '" + host + "' Port '" + port + "' Server Info Req '" + mStatsCheck + "'");

            final long startTime = DateTimeUtility.getCurrentTimeInNanos();

            if (mStatsCheck)
            {
                final String info = mJedis.info();

                if (log.isDebugEnabled())
                    log.debug("Info from Redis " + info);
                final Properties   props = new Properties();
                final StringReader sr    = new StringReader(info);
                props.load(sr);

                updateServerInfo(props);
                updateClientInfo(props);
                updateMemoryInfo(props);
                updateConnectionInfo(props);
            }
            // Assuming all the Qs are starting with Q and of Type List.
            final Set<String>                 keys      = mJedis.keys("Q:*");

            final Map<String, Response<Long>> listSizes = new HashMap<>();

            for (final String key : keys)
            {
                final Response<Long> llen = pipe.llen(key);
                listSizes.put(key, llen);
            }
            pipe.sync();

            for (final Entry<String, Response<Long>> entry : listSizes.entrySet())
                mListQSize.put(entry.getKey(), entry.getValue().get());

            if (log.isDebugEnabled())
                log.debug("Q Size retrived from the Redis. Total time taken " + DateTimeUtility.getTimeDifferenceInMillisFromNanoSecond(startTime) + " Millis");
            mLastUpdatedTime = new Date();
        }
        catch (final Exception e)
        {
            log.error("Exception while getting the detaills from Redis. Host '" + host + "' Port '" + port + "'", e);
            e.printStackTrace();
        }
        finally
        {
            // Need to close the Jedis
            mJedis.close();
        }
    }

    private void updateServerInfo(
            Properties aProps)
    {
        mServerInfo = new ServerInfo(CommonUtility.nullCheck(aProps.get(REDIS_VERSION), true), CommonUtility.nullCheck(aProps.get(OPERATING_SYSTEMS), true),
                CommonUtility.nullCheck(aProps.get(TCP_PORT), true), CommonUtility.nullCheck(aProps.get(UPTIME_IN_DAYS), true));
    }

    private void updateClientInfo(
            Properties aProps)
    {
        mClientInfo = new ClientInfo(CommonUtility.nullCheck(aProps.get(CONNECTED_CLIENTS), true), CommonUtility.nullCheck(aProps.get(BLOCKED_CLIENTS), true),
                CommonUtility.nullCheck(aProps.get(TRACKING_CLIENTS), true));
    }

    private void updateMemoryInfo(
            Properties aProps)
    {
        mMemoryInfo = new MemoryInfo(CommonUtility.nullCheck(aProps.get(USED_MEMORY), true), CommonUtility.nullCheck(aProps.get(USED_MEMORY_RSS), true),
                CommonUtility.nullCheck(aProps.get(TOTAL_SYSTEM_MEMORY), true), CommonUtility.nullCheck(aProps.get(USED_MEMORY_DATASET), true),
                CommonUtility.nullCheck(aProps.get(USED_MEMORY_LUA), true), CommonUtility.nullCheck(aProps.get(MAXMEMORY), true));
    }

    private void updateConnectionInfo(
            Properties aProps)
    {
        mConnectionInfo = new ConnectionInfo(CommonUtility.nullCheck(aProps.get(TOTAL_CONNECTIONS_RECEIVED), true), CommonUtility.nullCheck(aProps.get(TOTAL_COMMANDS_PROCESSED), true),
                CommonUtility.nullCheck(aProps.get(REJECTED_CONNECTIONS), true));
    }

    public ServerInfo getServerInfo()
    {
        return mServerInfo;
    }

    public ClientInfo getClientInfo()
    {
        return mClientInfo;
    }

    public MemoryInfo getMemoryInfo()
    {
        return mMemoryInfo;
    }

    public ConnectionInfo getConnectionInfo()
    {
        return mConnectionInfo;
    }

    public Map<String, Long> getListQSize()
    {
        return mListQSize;
    }

    public Date getLastUpdatedTime()
    {
        return mLastUpdatedTime;
    }

}