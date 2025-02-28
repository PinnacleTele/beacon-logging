package com.itextos.beacon;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextos.beacon.commonlib.messageidentifier.RedisDataPopulator;
import com.itextos.beacon.commonlib.prometheusmetricsutil.PrometheusMetrics;
import com.itextos.beacon.smslog.DebugLog;
import com.itextos.beacon.smslog.TimeTakenLog;

public class App {

    private static final Log                log                               = LogFactory.getLog(App.class);
	private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static boolean IS_START_PROMETHEUS=false;
    
    public static void foldercreaton(String folderPath) {
        

        // Create a File object representing the directory
        File folder = new File(folderPath);

        // Check if the directory exists
        if (!folder.exists()) {
            // Attempt to create the directory
            if (folder.mkdirs()) {
                System.out.println("Directory created successfully: " + folderPath);
				logger.debug("Directory created successfully: " + folderPath);

            } else {
                System.out.println("Failed to create directory: " + folderPath);
            }
        } else {
            System.out.println("Directory already exists: " + folderPath);
			logger.debug("Directory already exists: " + folderPath);

		}
    }
    
    public static void createfolder() {

    	System.setProperty("common.property.file.location", "/global.properties");
    	System.setProperty("log4j.configurationFile", "file:/log4j2-common.xml");
    	System.setProperty("prometheus.jetty.port", "1075");

    	foldercreaton("/opt/jboss/wildfly/logs/dnp");
    	foldercreaton("/opt/jboss/wildfly/logs/http");

    	foldercreaton("/opt/jboss/wildfly/logs/k2e");

    	foldercreaton("/opt/jboss/wildfly/logs/topic");
		foldercreaton("/opt/jboss/wildfly/logs/table2db");
		foldercreaton("/opt/jboss/wildfly/logs/consumer");
		foldercreaton("/opt/jboss/wildfly/logs/producer");
		foldercreaton("/opt/jboss/wildfly/logs/kafkasender");
		foldercreaton("/opt/jboss/wildfly/logs/executorlog1");
		foldercreaton("/opt/jboss/wildfly/logs/executorlog2");
		foldercreaton("/opt/jboss/wildfly/logs/application");
		foldercreaton("/opt/jboss/wildfly/logs/kafkareceiver");
		foldercreaton("/opt/jboss/wildfly/logs/timetaken");
		foldercreaton("/opt/jboss/wildfly/logs/aux");

		
		/*
		try {
			AppendToHosts.appendCustomHostsToSystemHosts();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
    }
	public static void main(String[] args) throws IOException {
		
		long start=System.currentTimeMillis();
	
		createfolder();

		
		String module=System.getenv("module");
		
		System.out.println("module : "+module);
		
		DebugLog.log("module : "+module);
		logger.debug("module : "+module);


		if(!isMW(module,args)) {
			
			
			if(!isBiller(module,args)) {
				
				if(!isAux(module,args)) {
					
					if(!isSMPP(module,args)) {

					

						if(!isALL(module,args)) {

							
							if(!isMWALL(module,args)) {

								
								if(!isAllBiller(module, args)) {
									
									if(!isAllDNPost(module, args)) {
										
										if(!isAllSingleTon(module, args)) {
											
										}
									}
								}

							}

						}

					}
				}
			}
		}
		
		if(IS_START_PROMETHEUS) {
			
			startPrometheusServer(true);

		}
		
		addShutdownHook();
		
		long end=System.currentTimeMillis();
		
		TimeTakenLog.log("Time Taken for Start : "+(end-start)/1000+" seconds");
	}
	
	 private static boolean isAllSingleTon(String module, String[] args) {
		 
		 if(module.trim().equals("singleton")) {
			 DebugLog.log("Start the module : singleton ");
			

				com.itextos.beacon.platform.duplicatecheckremoval.start.StartApplication.main(args);

				com.itextos.beacon.platform.dlrpayloadgen.process.StartApplication.main(args);

				com.itextos.beacon.http.interfacefallbackpoller.StartApplication.main(args);

				com.itextos.beacon.platform.kannelstatusupdater.StartApplication.main(args);

				com.itextos.beacon.platform.aysnprocessor.StartApplication.main(args);

				com.itextos.beacon.platform.rch.StartApplication.main(args);

				com.itextos.beacon.platform.sbpcore.StartApplication.main(args);

				com.itextos.beacon.smpp.concatehandover.StartApplication.main(args);


				

			IS_START_PROMETHEUS=true;
			return true;
				
			}
			
			return false;
	}

	private static boolean isAllDNPost(String module, String[] args) {
		 if(module.trim().equals("dnpost")) {
			 DebugLog.log("Start the module : dnpost ");
			 com.itextos.beacon.platform.smppdlr.StartApplication.main(args);
			 com.itextos.beacon.httpclienthandover.StartApplication.main(args);
			IS_START_PROMETHEUS=true;
			return true;
				
			}
			
			return false;
	}

	private static boolean isMWALL(String module, String[] args) {

			if(module.equals("mw")) {

					allMW(args);
					
					IS_START_PROMETHEUS=true;

					return true;

			}
			
			
		return false;
	}

	private static boolean isALL(String module, String[] args) {

			
			if(module.equals("all")) {
				
				
				allMW(args);
				allAUX(args);
				allBiller(args);

				IS_START_PROMETHEUS=true;

				return true;
				
			}
			return false;
		
	}

	private static void allBiller(String args[]) {

		
		
			
			com.itextos.beacon.platform.subbiller.StartApplication.main(args);

	
			com.itextos.beacon.platform.subt2tb.StartApplication.main(args);

			
			com.itextos.beacon.platform.dnt2tb.StartApplication.main(args);

				
			com.itextos.beacon.platform.fullmsgt2tb.StartApplication.main(args);

			
			com.itextos.beacon.platform.errorlogt2tb.StartApplication.main(args);
		
			com.itextos.beacon.platform.dnpostlogt2tb.StartApplication.main(args);

			com.itextos.beacon.platform.dnnopayloadt2tb.StartApplication.main(args);

			com.itextos.beacon.platform.sbc.StartApplication.main(args);

			com.itextos.beacon.platform.t2e.StartApplication.main(args);
				
			com.itextos.beacon.platform.clienthandovert2tb.StartApplication.main(args);
			
			com.itextos.beacon.platform.dnpcore.StartApplication.main(args);

		
	}

	private static void allAUX(String args []) {


			com.itextos.beacon.platform.wc.StartApplication.main(args);
			
			com.itextos.beacon.platform.dnpcore.StartApplication.main(args);
			
			com.itextos.beacon.platform.r3c.StartApplication.main(args);

			com.itextos.beacon.platform.prc.StartApplication.main(args);

			com.itextos.beacon.platform.dltvc.StartApplication.main(args);

			
			com.itextos.beacon.platform.smppdlr.StartApplication.main(args);

			com.itextos.beacon.httpclienthandover.StartApplication.main(args);

	
	}

	private static void allMW(String args[]) {
		

			com.itextos.beacon.platform.ic.StartApplication.main(args);
	
			com.itextos.beacon.platform.sbcv.StartApplication.main(args);
			
			
			com.itextos.beacon.platform.vc.StartApplication.main(args);
			
			
			com.itextos.beacon.platform.rc.StartApplication.main(args);
			
			
			com.itextos.beacon.platform.ch.StartApplication.main(args);
			
			
			com.itextos.beacon.platform.rch.StartApplication.main(args);
			
			
			com.itextos.beacon.platform.dch.StartApplication.main(args);
			
	
		
	}

	private static void allSMPP(String args[]) {
		
		
			com.itextos.beacon.smpp.interfaces.StartApplication.main(args);

		
	}

	

	private static void startPrometheusServer(
	            boolean aStartJettyServer)
	    {

	        try
	        {

	            if (aStartJettyServer)
	            {
	                PrometheusMetrics.registerServer();
	                PrometheusMetrics.registerPlatformMetrics();
	            }
	        }
	        catch (final Exception e)
	        {
	            // Add this exception in INFO mode.
	            if (log.isInfoEnabled())
	                log.info("IGNORE: Exception while working on prometheus counter", e);
	        }
	    }

	private static void addShutdownHook() {
	
		 log.info("Adding Shutdown Hook ...");
         Runtime.getRuntime().addShutdownHook(new Thread(() -> {
             Thread.currentThread().setName("ShutdownHook");
             log.info("Shutdown signal received");
             log.info("Waiting for Consumer Threads to join...");

             try
             {
            //      StartApplication.stopConsumerThreads();

              //   StartApplicationDN.stopConsumerThreads();
                // StartApplicationSub.stopConsumerThreads();

             }
             catch (final Exception ex)
             {
                 // TODO Auto-generated catch block
                 ex.printStackTrace(System.err);
             }
         }));

	}
	
	private static boolean isSMPP(String module, String[] args) {
		
		if(module.equals("smpp")) {
			
			com.itextos.beacon.smpp.interfaces.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("smppsimulator")) {
		
			com.itextos.beacon.smppsimulator.interfaces.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;
		}
		return false;
	}

	private static boolean isAux(String module, String[] args) {

		
		if(module.equals("wc")) {
			
			com.itextos.beacon.platform.wc.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("dnp")) {
			
			com.itextos.beacon.platform.dnpcore.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("qpr")) {
			
			com.itextos.beacon.queryprocessor.requestreceiver.QueryEngine.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("qptp")) {
			
			com.itextos.beacon.queryprocessor.threadpoolexecutor.ProcessQueueThreadPool.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("r3c")) {
			
			com.itextos.beacon.platform.r3c.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("prc")) {
			
			com.itextos.beacon.platform.prc.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("dltc")) {
			
			com.itextos.beacon.platform.dltvc.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("smppdlrhandover")) {
			
			com.itextos.beacon.platform.smppdlr.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("smppdlrpoller")) {
			
			com.itextos.beacon.platform.smppdlrpoller.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("httpclienthandover")) {
			
			com.itextos.beacon.httpclienthandover.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("digitaldnpost")) {
			
			com.itextos.beacon.platform.smppdlr.StartApplication.main(args);
			com.itextos.beacon.httpclienthandover.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("dbpoller")) {
			com.itextos.beacon.platform.kannelstatusupdater.StartApplication.main(args);

			com.itextos.beacon.platform.smppdlrpoller.StartApplication.main(args);
			com.itextos.beacon.platform.sbpcore.StartApplication.main(args);
			com.itextos.beacon.http.interfacefallbackpoller.StartApplication.main(args);
			com.itextos.beacon.platform.dnrfallbackpoller.StartApplication.main(args);
			
			com.itextos.beacon.platform.duplicatecheckremoval.start.StartApplication.main(args);
			com.itextos.beacon.platform.dlrpayloadgen.process.StartApplication.main(args);
			com.itextos.beacon.platform.rch.StartApplication.main(args);


			IS_START_PROMETHEUS=true;

			return true;			
		}
		
		
		return false;
	
	}
	
	
	private static boolean isAllBiller(String module, String[] args) {
		
		if(module.equals("biller")) {
			//
			com.itextos.beacon.platform.subt2tb.StartApplication.main(args);
			com.itextos.beacon.platform.dnt2tb.StartApplication.main(args);
			com.itextos.beacon.platform.fullmsgt2tb.StartApplication.main(args);
			com.itextos.beacon.platform.errorlogt2tb.StartApplication.main(args);
			com.itextos.beacon.platform.dnpostlogt2tb.StartApplication.main(args);
			com.itextos.beacon.platform.dnnopayloadt2tb.StartApplication.main(args);
			com.itextos.beacon.platform.t2e.StartApplication.main(args);
			com.itextos.beacon.platform.clienthandovert2tb.StartApplication.main(args);
			com.itextos.beacon.platform.dnpcore.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;
			
		}
		
		return false;
	}

	private static boolean isBiller(String module, String[] args) {
		
		if(module.equals("subbiller")) {
			
			com.itextos.beacon.platform.subbiller.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("subk2e")) {
			
			com.itextos.beacon.subk2e.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("dnk2e")) {
			
			com.itextos.beacon.dnk2e.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("subt2tb")) {
			
			com.itextos.beacon.platform.subt2tb.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("k2es")) {
			
			com.itextos.beacon.kafkabackend.kafka2elasticsearch.start.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("dnt2tb")) {
			
			com.itextos.beacon.platform.dnt2tb.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("dnt2tbbkup")) {
			
			com.itextos.beacon.platform.dnt2tbbkup.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("fullmsgt2tb")) {
			
			com.itextos.beacon.platform.fullmsgt2tb.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("errorlogt2tb")) {
			
			com.itextos.beacon.platform.errorlogt2tb.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("dnpostlogt2tb")) {
			
			com.itextos.beacon.platform.dnpostlogt2tb.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("dnnopayloadt2tb")) {
			
			com.itextos.beacon.platform.dnnopayloadt2tb.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("sbc")) {
			
			com.itextos.beacon.platform.sbc.StartApplication.main(args);

			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("t2e")) {
			
			com.itextos.beacon.platform.t2e.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;			
		}else if(module.equals("clienthandovert2tb")) {
			
			com.itextos.beacon.platform.clienthandovert2tb.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;			
		}
		
		return false;
	}

	private static boolean isMW(String module,String[] args) throws IOException {
		

		if(module.equals("ic")){
			
			com.itextos.beacon.platform.ic.StartApplication.main(args);
			
		//	com.itextos.beacon.platform.rch.StartApplication.main(args);

			
			IS_START_PROMETHEUS=true;
			return true;
			
		}else if(module.equals("ac")){
			
			com.itextos.beacon.platform.aysnprocessor.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("digitalbiller")){
			
			startDigitalBiller(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("billermain")){
			
			startBillerMain(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("billerother")){
			
			startBillerOther(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("digitalt2db")){
			
			startDigitalT2DB(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("smppinterface")){
			
			com.itextos.beacon.smpp.interfaces.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("redisdatagenerator")){
			
			
			RedisDataPopulator.main();
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("smppconcatehandover")){
			
			System.out.println("smppconcatehandover to be start");
			com.itextos.beacon.smpp.concatehandover.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("dbgwspring")){
			
		//	com.itextos.beacon.dbgw.main.DBGWSpringAPI.main(args);

			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("ifp")){
			
			com.itextos.beacon.http.interfacefallbackpoller.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("dnfp")){
			
			com.itextos.beacon.platform.dnrfallbackpoller.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("ksu")){
			
			com.itextos.beacon.platform.kannelstatusupdater.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("dpg")){
			
			com.itextos.beacon.platform.dlrpayloadgen.process.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("dcr")){
			
			com.itextos.beacon.platform.duplicatecheckremoval.start.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("sbcv")){
			
			com.itextos.beacon.platform.sbcv.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("mccmncload")){
			
			com.itextos.beacon.platform.mccmncload.Startup.main(args);
			
			IS_START_PROMETHEUS=false;

			return true;
			
		}else if(module.equals("vc")){
			
			com.itextos.beacon.platform.vc.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("rc")){
			
			com.itextos.beacon.platform.rc.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
			
		}else if(module.equals("ch")){
			
			com.itextos.beacon.platform.ch.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
		}else if(module.equals("rch")){
			
			com.itextos.beacon.platform.rch.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
		}else if(module.equals("dch")){
			
			com.itextos.beacon.platform.dch.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
		}else if(module.equals("sbp")){
			
			com.itextos.beacon.platform.sbpcore.StartApplication.main(args);
			
			IS_START_PROMETHEUS=true;

			return true;
		}
		
		return false;
	}

	private static void startDnpost(String[] args) {
		// TODO Auto-generated method stub
		
	}

	private static void startBillerOther(String[] args) {

		
		com.itextos.beacon.platform.errorlogt2tb.StartApplication.main(args);
		com.itextos.beacon.platform.dnnopayloadt2tb.StartApplication.main(args);
		com.itextos.beacon.platform.clienthandovert2tb.StartApplication.main(args);
		com.itextos.beacon.platform.dnpostlogt2tb.StartApplication.main(args);
	}

	private static void startBillerMain(String[] args) {
		
		com.itextos.beacon.platform.subt2tb.StartApplication.main(args);
		com.itextos.beacon.platform.dnt2tb.StartApplication.main(args);
	//	com.itextos.beacon.platform.fullmsgt2tb.StartApplication.main(args);
	}

	private static void startDigitalT2DB(String[] args) {
		
		
		com.itextos.beacon.platform.fullmsgt2tb.StartApplication.main(args);
		com.itextos.beacon.platform.dnt2tb.StartApplication.main(args);
		com.itextos.beacon.platform.subt2tb.StartApplication.main(args);
		com.itextos.beacon.platform.dnpcore.StartApplication.main(args);
		
	}

	private static void startDigitalDnpost(String[] args) {
		


		com.itextos.beacon.platform.kannelstatusupdater.StartApplication.main(args);
		

		com.itextos.beacon.platform.smppdlrpoller.StartApplication.main(args);
		
		com.itextos.beacon.platform.dlrpayloadgen.process.StartApplication.main(args);

		com.itextos.beacon.platform.duplicatecheckremoval.start.StartApplication.main(args);

		com.itextos.beacon.platform.rch.StartApplication.main(args);
		com.itextos.beacon.platform.aysnprocessor.StartApplication.main(args);
		com.itextos.beacon.platform.t2e.StartApplication.main(args);
		com.itextos.beacon.platform.errorlogt2tb.StartApplication.main(args);
		com.itextos.beacon.platform.dnnopayloadt2tb.StartApplication.main(args);
		com.itextos.beacon.platform.sbc.StartApplication.main(args);

	}

	private static void startDigitalBiller(String args[]) {
		
		com.itextos.beacon.platform.smppdlr.StartApplication.main(args);
	
		com.itextos.beacon.smpp.concatehandover.StartApplication.main(args);

		
		com.itextos.beacon.httpclienthandover.StartApplication.main(args);

		com.itextos.beacon.platform.clienthandovert2tb.StartApplication.main(args);
		com.itextos.beacon.platform.dnpostlogt2tb.StartApplication.main(args);

		
	}

}
