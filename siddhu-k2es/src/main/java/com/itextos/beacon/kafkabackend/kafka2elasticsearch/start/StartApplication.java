package com.itextos.beacon.kafkabackend.kafka2elasticsearch.start;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.elasticsearch.client.RestClient;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.errorlog.K2ESLog;
import com.itextos.beacon.kafkabackend.kafka2elasticsearch.kafkaconsumer.AppConfigLoader;
import com.itextos.beacon.kafkabackend.kafka2elasticsearch.kafkaconsumer.AppConfiguration;
import com.itextos.beacon.kafkabackend.kafka2elasticsearch.kafkaconsumer.ESIndexColMapValue;
import com.itextos.beacon.kafkabackend.kafka2elasticsearch.kafkaconsumer.Kafka2ESConstants;
import com.itextos.beacon.kafkabackend.kafka2elasticsearch.kafkaconsumer.Kafka2ESConsumerThread;

public class StartApplication
{

    private static final K2ESLog                              log                     = K2ESLog.getInstance();

  //  private static final Log                              log                     = LogFactory.getLog(StartApplication.class);
    public static String                                  ESClientTypeConfig      = null;
    public static AppConfiguration                        AppConfig               = null;
    public static String                                  AppMode                 = null;

    public static String                                  AppProcID               = null;
    public static String                                  HostIPAddr              = null;

    public static String                                  ESIndexName             = null;
    public static String                                  ESIndexUniqueColumn     = null;

    public static String                                  ESFmsgIndexName         = null;
    public static String                                  ESFmsgIndexUniqueColumn = null;

    public static String                                  ESDocUpdTmColumn        = null;

    public static ArrayList<ESIndexColMapValue>           ListESColMap            = null;

    public static String                                  KafkaTopicName          = null;
    public static int                                     KafkaConsGrpSeq         = -1;
    public static String                                  KafkaConsGrpID          = null;

    public static HashMap<String, Kafka2ESConsumerThread> HMConsumerThreads       = null;

    public static Thread                                  mainThread              = null;

    public static RestClient                              ES_LRC_Client           = null;
    public static RestClient                              ESErr_LRC_Client        = null;

    public static void testConsumer()
            throws Exception
    {
        TestKafkaConsumer.testConsumeSubMessage("t2db-submission", "cg-t2sub-zz-2");
    }

    public static synchronized void logMsg(
            String msg)
    {
        System.out.println(DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS)
                + ": " + Thread.currentThread().getName() + ": " + msg);
    }

    @SuppressWarnings("resource")
    static void fetchESColMapFromDB()
            throws Exception
    {
        final String MariaDBHost     = AppConfig.getString("mariadb.host");
        final String MariaDBPort     = AppConfig.getString("mariadb.port");
        final String MariaDBDatabase = AppConfig.getString("mariadb.database");
        final String MariaDBUser     = AppConfig.getString("mariadb.user");
        final String MysqlPassword   = AppConfig.getString("mariadb.password");

        final String MariaDBJDBCURL  = "jdbc:mariadb://" + MariaDBHost + ":" + MariaDBPort + "/" + MariaDBDatabase;

        String       SQL             = "select column_name, mapped_name, column_type, default_value, ci_column_required ";
        SQL += " from configuration.es_sub_del_t2_col_map where index_type='" + AppMode;
        SQL += "' and column_name != '" + ESIndexUniqueColumn + "'";

        log.info("ES Index Column Map SQL: " + SQL);
        log.info("Connecting MariaDB: " + MariaDBJDBCURL);

        final Connection conn = DriverManager.getConnection(MariaDBJDBCURL, MariaDBUser, MysqlPassword);
        final Statement  stmt = conn.createStatement();
        stmt.setFetchSize(100);
        final ResultSet rsColMap = stmt.executeQuery(SQL);

        ListESColMap = new ArrayList<>();
        boolean ErrorFlag    = false;
        String  ErrorMessage = null;

        while (rsColMap.next())
        {
            final String column_name   = CommonUtility.nullCheck(rsColMap.getString(1), true);
            final String map_name      = CommonUtility.nullCheck(rsColMap.getString(2), true);
            final String column_type   = CommonUtility.nullCheck(rsColMap.getString(3), true);
            final String default_value = CommonUtility.nullCheck(rsColMap.getString(4), true);
            final int    ci_required   = rsColMap.getInt(5);

            if ("".equals(column_name) || "".equals(map_name) || "".equals(column_type))
            {
                ErrorFlag    = true;
                ErrorMessage = "Column Name/Map Name/Column Type cannot be Empty/Null";
                break;
            }

            boolean ci_req_flag = false;
            if (ci_required != 0)
                ci_req_flag = true;

            ListESColMap.add(new ESIndexColMapValue(column_name, map_name, column_type, default_value, ci_req_flag));
        }

        rsColMap.close();
        stmt.close();
        conn.close();

        if (ErrorFlag)
        {
            log.error(ErrorMessage);
            throw new Exception(ErrorMessage);
        }
    }

    public static void stopConsumerThreads()
            throws Exception
    {

        try
        {

        	if(HMConsumerThreads!=null) {
        		
            for (final Kafka2ESConsumerThread ct : HMConsumerThreads.values())
            {
                final String ctName = ct.getConsumerThreadName();

                if (ct.isConsumerStopped())
                    log.info("Consumer Thread: " + ctName + " has already stopped");
                else
                {
                    logMsg("Stopping Consumer Thread: " + ctName);
                    ct.stopConsumer();
                    ct.join();
                    CommonUtility.sleepForAWhile();
                }
            }
            logMsg("Consumer Threads are stopped");
        	}
        }
        catch (final Exception ex)
        {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }

    public static void main(
            String[] args)
    {

        try
        {

            if (args.length < 4)
            {
                System.err.println("Invalid Arguments");
                System.err.println("Usage: StartApplication <Mode> <TOPIC_NAME> <Consumer Greqp Seq> <Thread Count>");
            }

            mainThread      = Thread.currentThread();

            AppMode         = System.getenv("appmode");//args[0];
            KafkaConsGrpSeq = Integer.parseInt(System.getenv("topicgroupid"));//Integer.parseInt(args[2]);
            final int threadCount = Integer.parseInt(System.getenv("threadcount"));//Integer.parseInt(args[3]);


            if (threadCount <= 0)
            {
                log.error("Consumer Threads count cannot be Zero, exiting ...");
                System.err.println("Consumer Threads count cannot be Zero, exiting ...");
                return;
            }

            if (!AppMode.equals(Kafka2ESConstants.subMode) && !AppMode.equals(Kafka2ESConstants.delMode))
            {
                log.error("Invalid Consumer Mode: " + AppMode);
                log.error("Valid Modes are : " + Kafka2ESConstants.subMode + ", " + Kafka2ESConstants.delMode);
                System.err.println("Invalid Consumer Mode: " + AppMode);
                return;
            }

            if (AppMode.equals(Kafka2ESConstants.subMode)) {
                ESDocUpdTmColumn = Kafka2ESConstants.subUpdTmColumn;
                KafkaTopicName  = Component.T2DB_SUBMISSION.getKey();


            }else if (AppMode.equals(Kafka2ESConstants.delMode)) {
                    ESDocUpdTmColumn = Kafka2ESConstants.delUpdTmColumn;
                    KafkaTopicName  = Component.T2DB_DELIVERIES.getKey();

            }

            KafkaTopicName=KafkaTopicName.replaceAll("_","-");
            
            if ("".equals(KafkaTopicName))
            {
                log.error("Kafka Topic name is empty");
                System.err.println("Kafka Topic name is empty");
                return;
            }
            else
                if (!KafkaTopicName.contains(AppMode))
                {
                    log.error("Invalid Kafka Topic name for Consumer Mode");
                    System.err.println("Invalid Kafka Topic name for Consumer Mode");
                    return;
                }

            // final String vmName = ManagementFactory.getRuntimeMXBean().getName();
            // AppProcID = vmName.substring(0, vmName.indexOf("@"));

            AppProcID = CommonUtility.getJvmProcessId();

            if (AppProcID.equals("-999999"))
            {
                log.error("Unable to get JVM Proces Id, Exiting...");
                System.err.println("Unable to get JVM Proces Id, Exiting...");
                return;
            }

            HostIPAddr = CommonUtility.getApplicationServerIp();

            if (HostIPAddr.equals("unknown"))
            {
                log.error("Unable to get Host IP Address, Exiting...");
                System.err.println("Unable to get Host IP Address, Exiting...");
                return;
            }

            AppConfig   = AppConfigLoader.getInstance().getAppConfiguration();
            ESIndexName = AppConfig.getString("es.index.name");

            if ("".equals(ESIndexName))
            {
                log.error("Elastic Index name is empty");
                System.err.println("Elastic Index name is empty");
                return;
            }

            ESIndexUniqueColumn = AppConfig.getString("es.index.uidcolumn");

            if ("".equals(ESIndexUniqueColumn))
            {
                log.error("Elastic Index Unique Column name is empty");
                System.err.println("Elastic Index Unique Column name is empty");
                return;
            }

            ESFmsgIndexName = AppConfig.getString("es.fmsg.index.name");

            if ("".equals(ESFmsgIndexName))
            {
                log.error("Elastic Full Message Index name is empty");
                System.err.println("Elastic Full Message Index name is empty");
                return;
            }

            ESFmsgIndexUniqueColumn = AppConfig.getString("es.fmsg.index.uidcolumn");

            if ("".equals(ESFmsgIndexUniqueColumn))
            {
                log.error("Elastic Full Message Index Unique Column name is empty");
                System.err.println("Elastic Full Message Index Unique Column name is empty");
                return;
            }

            log.info("Kafka Consumer for ES started, Mode: " + AppMode);
            log.info("Host IP Address: " + HostIPAddr);
            log.info("App Process ID: " + AppProcID);
            log.info("Kafka2ES Consumer Application started, Mode: " + AppMode);
            log.info("Kafka Topic Name: " + KafkaTopicName);
            log.info("Kafka Consumer Group ID: " + KafkaConsGrpID);
            log.info("Elastic Index name: " + ESIndexName);
            log.info("Elastic Index Unique Column Name : " + ESIndexUniqueColumn);
            log.info("Elastic Full Message Index name: " + ESFmsgIndexName);
            log.info("Elastic Full Message Index Unique Column Name : " + ESFmsgIndexUniqueColumn);

            log.info("Fetching Column map details from DB ...");
            fetchESColMapFromDB();

            if ((ListESColMap == null) || (ListESColMap.size() == 0))
            {
                log.error("No Mapping Column details found, exiting ...");
                System.err.println("No Mapping Column details found, exiting ...");
                return;
            }

            HMConsumerThreads = new HashMap<>();

            String priority=System.getenv("priority");
            
            StringTokenizer st=new StringTokenizer(priority,",");
            
            while(st.hasMoreTokens()) {
            	
            	String p=st.nextToken();
            	
            	String topicname=KafkaTopicName;
            	
            	if(!p.equals("default")) {
            		
            		topicname=topicname+"-"+p;
            	}
            	
              String  topicgroupname = "cg-" + topicname + "-" + KafkaConsGrpSeq;

           

            for (int ti = 1; ti <= threadCount; ti++)
            {
                final String           thName = "t-" + topicgroupname + "-" + ti;

                Kafka2ESConsumerThread ct     = null;

                ct = new Kafka2ESConsumerThread(thName,topicgroupname,topicname);
                HMConsumerThreads.put(thName, ct);
                log.info("Starting Consumer Thread: " + thName);
                ct.start();
                CommonUtility.sleepForAWhile();
            }

            }
            log.info("Adding Shutdown Hook ...");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                Thread.currentThread().setName("ShutdownHook");
                log.info("Shutdown signal received");
                log.info("Waiting for Consumer Threads to join...");

                try
                {
                    StartApplication.stopConsumerThreads();
                    StartApplication.mainThread.join();
                }
                catch (final Exception ex)
                {
                    // TODO Auto-generated catch block
                    ex.printStackTrace(System.err);
                }
            }));

            for (final Kafka2ESConsumerThread ct : HMConsumerThreads.values())
                ct.join();
        }
        catch (final Exception ex)
        {
            log.error(ex.getMessage(), ex);
            ex.printStackTrace(System.err);
        }
        finally
        {

            try
            {
                if (HMConsumerThreads != null)
                    for (final Kafka2ESConsumerThread ct : HMConsumerThreads.values())
                        if (!ct.isConsumerStopped())
                        {
                            ct.stopConsumer();
                            ct.join();
                            CommonUtility.sleepForAWhile();
                        }

                CommonUtility.sleepForAWhile(1000);
                log.info("Kafka2ES Consumer Application Mode: " + AppMode + ", stopped");
                logMsg("Kafka2ES Consumer Application Mode: " + AppMode + ", stopped");
            }
            catch (final Exception ex2)
            {
                log.error(ex2.getMessage(), ex2);
                ex2.printStackTrace(System.err);
            }
        }
    }

}
