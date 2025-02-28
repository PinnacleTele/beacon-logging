package com.itextos.beacon.queryprocessor.requestreceiver;

import java.io.FileReader;
import java.util.Properties;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.itextos.beacon.errorlog.QPRLog;
import com.itextos.beacon.queryprocessor.databaseconnector.ConnectionPoolSingleton;

public class QueryEngine
{

    public static Server     server        = null;
    static final Properties  mySQL_cfg_val = new Properties();
    private static final QPRLog log           = QPRLog.getInstance();
    
    static {
    	
        final String cfg_fn = "/req_receiver.properties"+"_"+System.getenv("profile");//args[0];
        log.info("Reading values from config file: " + cfg_fn);
        FileReader file;
		try {
			file = new FileReader(cfg_fn);
			   mySQL_cfg_val.load(file);
		        file.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     
    }

    public static void main(
            String[] args)
    {

        try
        {

            final ConnectionPoolSingleton connPool    = ConnectionPoolSingleton.createInstance(mySQL_cfg_val);

            final int                     server_port = Integer.parseInt(mySQL_cfg_val.getProperty("server.port"));
            final int                     min_threads = Integer
                    .parseInt(mySQL_cfg_val.getProperty("server.min.threads"));
            final int                     max_threads = Integer
                    .parseInt(mySQL_cfg_val.getProperty("server.max.threads"));
            log.info("Query Processor Server Port: " + server_port);
            log.info("Query Processor Server Min Threads: " + min_threads);
            log.info("Query Processor Server Max Threads: " + max_threads);

            final QueuedThreadPool threadPool = new QueuedThreadPool(max_threads, min_threads);
            server = new Server(threadPool);
            final ServerConnector connector = new ServerConnector(server);
            connector.setPort(server_port);
            server.addConnector(connector);

            final ServletContextHandler handler = new ServletContextHandler(server, "/");

            log.info("Preparing api routes");
            handler.addServlet(GetData.class.getName(), "/get_data");

            handler.addServlet(CreateLogDataQueue.class.getName(), "/log_queue/initiate");

            handler.addServlet(LogDataQueueStatus.class.getName(), "/log_queue/status");

            handler.addServlet(LogDataQueueList.class.getName(), "/log_queue/list");

            server.start();

            log.info("Query Processor Server started");

            server.join();
        }
        catch (final Exception ex)
        {
            log.error("Error Occurred", ex);
            ex.printStackTrace();
        }
    }

}
