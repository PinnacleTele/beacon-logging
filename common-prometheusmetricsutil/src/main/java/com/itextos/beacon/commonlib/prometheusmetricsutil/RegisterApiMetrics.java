package com.itextos.beacon.commonlib.prometheusmetricsutil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.constants.exception.ItextosRuntimeException;

public class RegisterApiMetrics extends
PrometheusMetricsConstants{

    private static final Log log = LogFactory.getLog(PrometheusMetrics.class);

	private static RegisterApiMetrics obj = new RegisterApiMetrics();
	
	private RegisterApiMetrics() {
		
		init();
	}
	
	private void init() {
		
		 if (PrometheusController.getInstance().isPrometheusEnabled(PrometheusMetricProvider.API))
	        {
	            registerAPIAcceptCounter();
	            registerAPIStatusCounter();
	            regsiterAPILatency();
	        }
		
	}

	
	   
    private static void regsiterAPILatency()
    {

        try
        {

            if (PrometheusController.getInstance().getApiController().isApiAllLatencyEnabled()
                    || (PrometheusController.getInstance().getApiController().isMetricsEnabledForSpecificApis() && PrometheusController.getInstance().getApiController().isApiListLatencyEnabled())
                    || (PrometheusController.getInstance().getApiController().isMetricsEnabledForSpecificUsers() && PrometheusController.getInstance().getApiController().isApiUsersLatencyEnabled()))
            {
                registerUserApiLatency();
                if (log.isDebugEnabled())
                    log.debug("Prometheus API Latency registered.");
            }
        }
        catch (final Exception e)
        {
            log.error("Exception while registering Prometheus metrics for API Latency . It should not impact the application process.", e);
        }
    }
    
    private static void registerUserApiLatency() throws ItextosRuntimeException
    {
        PrometheusDataHolder.getInstance().createMetrics(MetricType.HISTOGRAM, API_USER_PROCESS_LATENCY, PrometheusController.getInstance().getHistogramBuckgets(), "API request processing latency",
                API, MESSAGE_SOURCE, CLUSTER_NAME, IP, CLIENT_IP, USER);
    }
	 private static void registerAPIStatusCounter()
	    {

	        try
	        {

	            if (PrometheusController.getInstance().getApiController().isApiAllStatusEnabled()
	                    || (PrometheusController.getInstance().getApiController().isMetricsEnabledForSpecificApis() && PrometheusController.getInstance().getApiController().isApiListStatusEnabled())
	                    || (PrometheusController.getInstance().getApiController().isMetricsEnabledForSpecificUsers() && PrometheusController.getInstance().getApiController().isApiUsersStatusEnabled()))
	            {
	                registerUserApiStatusCounter();
	                if (log.isDebugEnabled())
	                    log.debug("Prometheus API Status counter registered.");
	            }
	        }
	        catch (final Exception e)
	        {
	            log.error("Exception while registering Prometheus metrics for API Status Counter. It should not impact the application process.", e);
	        }
	    }

	   private static void registerUserApiStatusCounter() throws ItextosRuntimeException
	    {
	        PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, API_USER_STATUS_COUNT, "Status Counter for Messages", API, MESSAGE_SOURCE, CLUSTER_NAME, IP, CLIENT_IP, STATUS_CODE, USER);
	    }
	
	   private static void registerAPIAcceptCounter()
	    {

	        try
	        {

	            if (PrometheusController.getInstance().getApiController().isApiAllAcceptEnabled()
	                    || (PrometheusController.getInstance().getApiController().isMetricsEnabledForSpecificApis() && PrometheusController.getInstance().getApiController().isApiListAcceptEnabled())
	                    || (PrometheusController.getInstance().getApiController().isMetricsEnabledForSpecificUsers() && PrometheusController.getInstance().getApiController().isApiUsersAcceptEnabled()))
	            {
	                regiterUserApiAcceptCounter();
	                if (log.isDebugEnabled())
	                    log.debug("Prometheus API Accept counter registered.");
	            }
	        }
	        catch (final Exception e)
	        {
	            log.error("Exception while registering Prometheus metrics for API Request Counter. It should not impact the application process.", e);
	        }
	    }

	   
	   private static void regiterUserApiAcceptCounter() throws ItextosRuntimeException
	    {
	        PrometheusDataHolder.getInstance().createMetrics(MetricType.COUNTER, API_USER_ACCEPT_COUNT, "Accept Counter for Messages", API, MESSAGE_SOURCE, CLUSTER_NAME, IP, CLIENT_IP, USER);
	    }
	   
	public static RegisterApiMetrics getInstance() {
		
		return obj;
	}
	
	
}
