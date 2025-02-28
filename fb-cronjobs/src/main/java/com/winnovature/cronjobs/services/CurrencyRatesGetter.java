package com.winnovature.cronjobs.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.winnovature.cronjobs.dao.CurrencyUpdateDAO;
import com.winnovature.cronjobs.singletons.CronJobsPropertiesTon;
import com.winnovature.cronjobs.utils.Constants;
import com.winnovature.utils.utils.HeartBeatMonitoring;
import com.winnovature.utils.utils.JsonUtility;
import com.winnovature.utils.utils.Utility;

public class CurrencyRatesGetter implements Job {

	static Log log = LogFactory.getLog(Constants.CronJobLogger);
	private static final String className = "[CurrencyRatesGetter]";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		// String jobSays = dataMap.getString("jobSays");
		String logname = className + " [execute] ";
		PropertiesConfiguration cronJobProperties = null;

		if (log.isDebugEnabled()) {
			log.debug(logname + "Executing cronjob[" + context.getJobDetail().getKey() + "], scheduled date & time ="
					+ context.getFireTime());
		}

		CurrencyUpdateDAO currencyUpdateDAO = new CurrencyUpdateDAO();
		List<String> currencies = null;

		try {
			cronJobProperties = CronJobsPropertiesTon.getInstance().getPropertiesConfiguration();
			String instanceId = cronJobProperties.getString("instance.monitoring.id");
			
			// HeartBeat
			String timeStampAsString = Utility.getTimestampAsString();
			new HeartBeatMonitoring().pushConsumersHeartBeat("FP-CronJobs", "CurrencyRatesUpdater", instanceId,
					"CurrencyRatesUpdate", timeStampAsString);
			
			// 1. Get available base currencies
			Map<String, String> baseCurrencies = currencyUpdateDAO.getBaseCurrencies();
			
			// 2. Get available currency codes
			currencies = currencyUpdateDAO.getAvailableCurrencies();
			
			List<String> targetCurrencyList = new ArrayList(baseCurrencies.values());
			
			for (String currency_code : currencies) {
				// 2. For each currency code find the exchange rates
				String responseAsJson = fetchCurrencyExchangeRates(currency_code, targetCurrencyList);
				if (StringUtils.isNotBlank(responseAsJson)) {
					Map<String, Object> responseAsMap = new JsonUtility().jsonToMap(responseAsJson);
					if ((boolean) responseAsMap.get("success")) {
						// 3. Update into currency_rates_current and insert into currency_rates_history
						currencyUpdateDAO.updateCurrencyRates(responseAsMap, baseCurrencies);
					} else {
						// TODO - API specific error codes - need to discuss
						// ex: max limit reached(104), invalid base currency(201) etc...
					}
				}
			}
		} catch (Exception e) {
			log.error(logname + "Exception ", e);
			// TODO - Need to discuss on how to handle error cases
		}

		/*
		if (context.getNextFireTime() == null) {
			log.debug("scheduler shutdown ...");
			try {
				context.getScheduler().shutdown();
			} catch (Exception e) {
				System.err.println(e);
			}
		}
		*/

		if (log.isDebugEnabled()) {
			log.debug(logname + "Cronjob[" + context.getJobDetail().getKey() + "] next scheduled date & time ="
					+ context.getNextFireTime());
		}

	}

	private String fetchCurrencyExchangeRates(String baseCurrency, List<String> currencyCodes) throws Exception {
		String logname = className + " [fetchCurrencyExchangeRates] ";
		HttpRequest request = null;
		HttpResponse<String> response = null;
		String endPoint = null;
		String apiResponseBody = null;
		String baseURL = null;
		String accessKey = null;
		String queryParams = null;
		List<String> queryParamsList = new ArrayList<String>();
		HttpClient httpClient = null;
		PropertiesConfiguration cronJobProperties = null;
		try {
			if (log.isDebugEnabled()) {
				log.debug(
						logname + "Begin, Inputs are baseCurrency=" + baseCurrency + " target currencyCodes=" + currencyCodes);
			}

			cronJobProperties = CronJobsPropertiesTon.getInstance().getPropertiesConfiguration();

			baseURL = cronJobProperties.getString("exchange.api.endpoint");
			accessKey = cronJobProperties.getString("exchange.api.accesskey");
			
			/*
			List<String> targetCurrencyCodes = new ArrayList<String>();
			// excluding base currency from target currencies
			for (String currency_code : currencyCodes) {
				if (!currency_code.equalsIgnoreCase(baseCurrency)) {
					targetCurrencyCodes.add(currency_code);
				}
			}
			String commaSeperatedCurrencyCodes = StringUtils.join(targetCurrencyCodes, ",");
			*/

			String commaSeperatedCurrencyCodes = StringUtils.join(currencyCodes, ",");
			
			if (StringUtils.isNotBlank(accessKey)) {
				queryParamsList.add("access_key=" + accessKey);
			}

			if (StringUtils.isNotBlank(baseCurrency)) {
				queryParamsList.add("base=" + baseCurrency);
			}

			if (StringUtils.isNotBlank(commaSeperatedCurrencyCodes)) {
				queryParamsList.add("symbols=" + commaSeperatedCurrencyCodes);
			}

			if (queryParamsList.size() > 0) {
				queryParams = StringUtils.join(queryParamsList, "&");
			}

			if (StringUtils.isBlank(queryParams)) {
				endPoint = baseURL;
			} else {
				endPoint = baseURL + "?" + queryParams;
			}

			httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

			request = HttpRequest.newBuilder().GET().uri(URI.create(endPoint)).build();

			if (log.isDebugEnabled()) {
				log.debug(logname + " Hitting " + endPoint);
			}

			response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() >= 200 && response.statusCode() <= 299) {
				apiResponseBody = response.body();
				if (log.isDebugEnabled()) {
					log.debug(logname + " API Response " + apiResponseBody);
				}
			} else {
				log.error(logname + " API Response code " + response.statusCode());
				log.error(logname + " API Response " + response);
			}

		} catch (Exception e) {
			throw e;
		}

		return apiResponseBody;

	}

}
