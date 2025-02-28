package com.itextos.beacon.platform.topic2table.es;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.elasticsearch.client.RestClient;

import com.itextos.beacon.commonlib.constants.Component;
import com.itextos.beacon.commonlib.constants.DateTimeFormat;
import com.itextos.beacon.commonlib.message.BaseMessage;
import com.itextos.beacon.commonlib.utility.CommonUtility;
import com.itextos.beacon.commonlib.utility.DateTimeUtility;
import com.itextos.beacon.errorlog.K2ESLog;

public class DeliveriesK2ES implements K2ES
{

    private static final K2ESLog                              log                     = K2ESLog.getInstance();

  //  private static final Log                              log                     = LogFactory.getLog(StartApplication.class);
    public  String                                  ESClientTypeConfig      = null;
    public static  AppConfiguration                        AppConfig               = null;
    public static String                                  AppMode                 = null;

    public  String                                  AppProcID               = null;
    public  String                                  HostIPAddr              = null;

    public  static String                                  ESIndexName             = null;
    public  static String                                  ESIndexUniqueColumn     = null;

    public  static String                                  ESFmsgIndexName         = null;
    public  static String                                  ESFmsgIndexUniqueColumn = null;

    public  static String                                  ESDocUpdTmColumn        = null;

    public  static ArrayList<ESIndexColMapValue>           ListESColMap            = null;

    public  String                                  KafkaTopicName          = null;
    public  int                                     KafkaConsGrpSeq         = -1;
    public  String                                  KafkaConsGrpID          = null;

    public  Kafka2ESDeliveries kafka2ESDeliveries       = null;

    public static Thread                                  mainThread              = null;

    public static RestClient                              ES_LRC_Client           = null;
    public static RestClient                              ESErr_LRC_Client        = null;

   

    public DeliveriesK2ES() {
    	
    	init();
    }
    public static synchronized void logMsg(
            String msg)
    {
        System.out.println(DateTimeUtility.getFormattedCurrentDateTime(DateTimeFormat.DEFAULT_WITH_MILLI_SECONDS)
                + ": " + Thread.currentThread().getName() + ": " + msg);
    }

    @SuppressWarnings("resource")
    void fetchESColMapFromDB()
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

   

    public void init()
    {

        try
        {

          

            mainThread      = Thread.currentThread();

            AppMode         = "deliveries";//args[0];


         

            if (AppMode.equals(Kafka2ESConstants.subMode)) {
                ESDocUpdTmColumn = Kafka2ESConstants.subUpdTmColumn;
                KafkaTopicName  = Component.T2DB_SUBMISSION.getKey();


            }else if (AppMode.equals(Kafka2ESConstants.delMode)) {
                    ESDocUpdTmColumn = Kafka2ESConstants.delUpdTmColumn;
                    KafkaTopicName  = Component.T2DB_DELIVERIES.getKey();

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

            kafka2ESDeliveries = new Kafka2ESDeliveries("dummy","dummy","dummy");

       
           
           

            
            log.info("Adding Shutdown Hook ...");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                Thread.currentThread().setName("ShutdownHook");
                log.info("Shutdown signal received");
                log.info("Waiting for Consumer Threads to join...");

                try
                {
                    DeliveriesK2ES.mainThread.join();
                }
                catch (final Exception ex)
                {
                    // TODO Auto-generated catch block
                    ex.printStackTrace(System.err);
                }
            }));

            
        }
        catch (final Exception ex)
        {
            log.error(ex.getMessage(), ex);
            ex.printStackTrace(System.err);
        }
        finally
        {

            
        }
    }

    public void pushtoElasticSearch( List<BaseMessage> mMessagesToInsert) {
    
    	kafka2ESDeliveries.pushtoElasticSearch(mMessagesToInsert);
    }
}
