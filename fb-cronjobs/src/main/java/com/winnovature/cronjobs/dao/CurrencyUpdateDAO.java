package com.winnovature.cronjobs.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.winnovature.cronjobs.utils.Constants;
import com.winnovature.utils.singletons.ConnectionFactoryForConfigDB;

public class CurrencyUpdateDAO {

	static Log log = LogFactory.getLog(Constants.CronJobLogger);
	private static final String className = "[CurrencyUpdateDAO]";

	public List<String> getAvailableCurrencies() throws Exception {
		String logname = className + " [getAvailableCurrencies] ";

		ResultSet rs = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		List<String> currenciesList = new ArrayList<String>();
		String sqlQuery = "select distinct currency_code from billing_currency_master";
		try {
			if (log.isDebugEnabled()) {
				log.debug(logname + "sqlQuery = " + sqlQuery);
			}

			con = ConnectionFactoryForConfigDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sqlQuery);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				currenciesList.add(rs.getString("currency_code"));
			}

		} catch (Exception e) {
			log.error(logname + "Exception: ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}

		if (log.isDebugEnabled()) {
			log.debug(logname + " Currencies found are " + currenciesList);
		}

		return currenciesList.size() > 0 ? currenciesList : null;
	}
	
	public Map<String, String> getBaseCurrencies() throws Exception {
		String logname = className + " [getBaseCurrencies] ";

		ResultSet rs = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		Map<String, String> baseCurrencies = new HashMap<String, String>();
		String sqlQuery = "select param_key, key_value from configuration.app_config_values where param_key in ('base.currency','intl.base.currency')";
		try {
			if (log.isDebugEnabled()) {
				log.debug(logname + "sqlQuery = " + sqlQuery);
			}

			con = ConnectionFactoryForConfigDB.getInstance().getConnection();
			pstmt = con.prepareStatement(sqlQuery);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				if(rs.getString("param_key").equalsIgnoreCase("base.currency")) {
					baseCurrencies.put("base", rs.getString("key_value"));
				} else {
					baseCurrencies.put("intl_base", rs.getString("key_value"));
				}
			}

		} catch (Exception e) {
			log.error(logname + "Exception: ", e);
			throw e;
		} finally {
			closeConnection(rs, pstmt, con);
		}

		if (log.isDebugEnabled()) {
			log.debug(logname + " Base currencies found are " + baseCurrencies);
		}

		return baseCurrencies.size() > 0 ? baseCurrencies : null;
	}

	public void updateCurrencyRates(Map<String, Object> data, Map<String, String> baseCurrencies) throws Exception {
		String logname = className + " [updateCurrencyRates] ";
		Connection con = null;
		try {
			con = ConnectionFactoryForConfigDB.getInstance().getConnection();
			con.setAutoCommit(false);
			updateCurrencyRatesDaily(data, con, baseCurrencies);
			updateCurrencyRatesHistory(data, con, baseCurrencies);
			if(isFirstDayofMonth()) {
				updateCurrencyRatesMonthly(data, con, baseCurrencies);
			}
			con.commit();
		} catch (Exception e) {
			log.error(logname + "Exception: ", e);
			throw e;
		} finally {
			try {
				closeConnection(null, null, con);
			} catch (Exception e) {
			}
		}
	}

	/*
	public static void main(String[] args) {
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("timestamp", 1638450543);
		
		Instant instant = Instant.ofEpochSecond((int)data.get("timestamp"));
		System.out.println(instant);

		DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
				.withZone(ZoneOffset.UTC);
		System.out.println(DATE_TIME_FORMATTER.format(instant));

		OffsetDateTime utcDateTime = instant.atOffset(ZoneOffset.UTC);
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		System.out.println(formatter.format(utcDateTime));

	}
	*/

	public boolean isFirstDayofMonth() {
		Calendar calendar = Calendar.getInstance();
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		return dayOfMonth == 1;
	}
	
	public void updateCurrencyRatesHistory(Map<String, Object> data, Connection con, Map<String, String> baseCurrencies) throws Exception {
		String logname = className + " [updateCurrencyRatesHistory] ";

		PreparedStatement pstmt1 = null;
		String insertQuery = "insert into currency_rates_date_history (dt_utc,ts_utc,base_currency,quote_currency,rate,cts) values (?,?,?,?,?,now())";

		Map<String, Object> currencyWiseRate = (Map<String, Object>) data.get("rates");
		String baseCurrencyCode = data.get("base").toString();
		try {
			pstmt1 = con.prepareStatement(insertQuery);

			for (String currencyCode : currencyWiseRate.keySet()) {
				try {
					
					if (baseCurrencyCode.equalsIgnoreCase(baseCurrencies.get("intl_base"))
							&& currencyCode.equalsIgnoreCase(baseCurrencies.get("base"))) {
						// NOTE: Do not insert intl_base to base rate
						continue;
					}
					
					Instant instant = Instant.ofEpochSecond((int) data.get("timestamp"));
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
							.withZone(ZoneOffset.UTC);
					String ts_utc = formatter.format(instant);
					
					
					pstmt1.setString(1, data.get("date").toString());
					pstmt1.setString(2, ts_utc);
					pstmt1.setString(3, baseCurrencyCode);
					pstmt1.setString(4, currencyCode);
					if(currencyCode.equalsIgnoreCase(baseCurrencyCode)) {
						pstmt1.setInt(5, (int)currencyWiseRate.get(currencyCode));
					}else {
						pstmt1.setDouble(5, (double)currencyWiseRate.get(currencyCode));
					}

					pstmt1.executeUpdate();
				} catch (Exception e) {
					if (e.getMessage().toLowerCase().contains("duplicate entry")) {
						log.error(logname + " duplicate entry found for dt_utc[" + data.get("date").toString()
								+ "] + base_currency[" + baseCurrencyCode + "] + quote_currency[" + currencyCode
								+ "] combination. Data=" + data);
					} else {
						log.error(logname + " Exception in inserting to currency_rates_date_history, data: dt_utc[" + data.get("date").toString()
								+ "] + base_currency[" + baseCurrencyCode + "] + quote_currency[" + currencyCode
								+ "] combination. full data=" + data);
						log.error(logname, e);
						con.rollback();
						closeConnection(null, pstmt1, null);
						throw e;
					}
				}
			}

		} catch (Exception e) {
			log.error(logname + "Exception: ", e);
			throw e;
		} finally {
			closeConnection(null, pstmt1, null);
		}

	}
	
	
	
	public void updateCurrencyRatesDaily(Map<String, Object> data, Connection con, Map<String, String> baseCurrencies) throws Exception {
		String logname = className + " [updateCurrencyRatesDaily] ";

		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		String insertQuery = "insert into currency_rates_daily (base_currency,quote_currency,rate,mts) values (?,?,?,now())";

		Map<String, Object> currencyWiseRate = (Map<String, Object>) data.get("rates");
		String baseCurrencyCode = data.get("base").toString();
		try {
			pstmt1 = con.prepareStatement(insertQuery);

			for (String currencyCode : currencyWiseRate.keySet()) {
				try {
					
					if (baseCurrencyCode.equalsIgnoreCase(baseCurrencies.get("intl_base"))
							&& currencyCode.equalsIgnoreCase(baseCurrencies.get("base"))) {
						// NOTE: Do not insert intl_base to base rate
						continue;
					}
					
					pstmt1.setString(1, baseCurrencyCode);
					pstmt1.setString(2, currencyCode);
					if(currencyCode.equalsIgnoreCase(baseCurrencyCode)) {
						pstmt1.setInt(3, (int)currencyWiseRate.get(currencyCode));
					}else {
						pstmt1.setDouble(3, (double)currencyWiseRate.get(currencyCode));
					}
					pstmt1.executeUpdate();
				} catch (Exception e) {
					if (e.getMessage().toLowerCase().contains("duplicate entry")) {
						String updateQuery = "update currency_rates_daily set rate=?, mts=now() where base_currency=? and quote_currency=?";
						pstmt2 = con.prepareStatement(updateQuery);

						try {
							if(currencyCode.equalsIgnoreCase(baseCurrencyCode)) {
								pstmt2.setInt(1, (int)currencyWiseRate.get(currencyCode));
							}else {
								pstmt2.setDouble(1, (double)currencyWiseRate.get(currencyCode));
							}
							pstmt2.setString(2, baseCurrencyCode);
							pstmt2.setString(3, currencyCode);
							pstmt2.executeUpdate();
						} catch (Exception e1) {
							log.error(logname + " Exception in updating to currency_rates_daily,  base_currency[" + baseCurrencyCode + "]  quote_currency[" + currencyCode
									+ "] combination. full data=" + data);
							log.error(logname, e1);
							con.rollback();
							closeConnection(null, pstmt2, null);
							closeConnection(null, pstmt1, null);
							throw e1;
						}
					} else {
						log.error(logname + " Exception in inserting to currency_rates_daily,  base_currency[" + baseCurrencyCode + "]  quote_currency[" + currencyCode
								+ "] combination. full data=" + data);
						log.error(logname, e);
						con.rollback();
						closeConnection(null, pstmt2, null);
						closeConnection(null, pstmt1, null);
						throw e;
					}
				}
			}

		} catch (Exception e) {
			log.error(logname + "Exception: ", e);
			throw e;
		} finally {
			closeConnection(null, pstmt2, null);
			closeConnection(null, pstmt1, null);
		}

	}
	
	
	
	public void updateCurrencyRatesMonthly(Map<String, Object> data, Connection con, Map<String, String> baseCurrencies) throws Exception {
		String logname = className + " [updateCurrencyRatesMonthly] ";

		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		String insertQuery = "insert into currency_rates_monthly (base_currency,quote_currency,rate,mts) values (?,?,?,now())";

		Map<String, Object> currencyWiseRate = (Map<String, Object>) data.get("rates");
		String baseCurrencyCode = data.get("base").toString();
		try {
			pstmt1 = con.prepareStatement(insertQuery);

			for (String currencyCode : currencyWiseRate.keySet()) {
				try {
					if (baseCurrencyCode.equalsIgnoreCase(baseCurrencies.get("intl_base"))
							&& currencyCode.equalsIgnoreCase(baseCurrencies.get("base"))) {
						// NOTE: Do not insert intl_base to base rate
						continue;
					}
					
					pstmt1.setString(1, baseCurrencyCode);
					pstmt1.setString(2, currencyCode);
					if(currencyCode.equalsIgnoreCase(baseCurrencyCode)) {
						pstmt1.setInt(3, (int)currencyWiseRate.get(currencyCode));
					}else {
						pstmt1.setDouble(3, (double)currencyWiseRate.get(currencyCode));
					}
					pstmt1.executeUpdate();
				} catch (Exception e) {
					if (e.getMessage().toLowerCase().contains("duplicate entry")) {
						String updateQuery = "update currency_rates_monthly set rate=?, mts=now() where base_currency=? and quote_currency=?";
						pstmt2 = con.prepareStatement(updateQuery);

						try {
							if(currencyCode.equalsIgnoreCase(baseCurrencyCode)) {
								pstmt2.setInt(1, (int)currencyWiseRate.get(currencyCode));
							}else {
								pstmt2.setDouble(1, (double)currencyWiseRate.get(currencyCode));
							}
							pstmt2.setString(2, baseCurrencyCode);
							pstmt2.setString(3, currencyCode);
							pstmt2.executeUpdate();
						} catch (Exception e1) {
							log.error(logname + " Exception in updating to currency_rates_monthly,  base_currency[" + baseCurrencyCode + "]  quote_currency[" + currencyCode
									+ "] combination. full data=" + data);
							log.error(logname, e1);
							con.rollback();
							closeConnection(null, pstmt2, null);
							closeConnection(null, pstmt1, null);
							throw e1;
						}
					} else {
						log.error(logname + " Exception in inserting to currency_rates_monthly,  base_currency[" + baseCurrencyCode + "]  quote_currency[" + currencyCode
								+ "] combination. full data=" + data);
						log.error(logname, e);
						con.rollback();
						closeConnection(null, pstmt2, null);
						closeConnection(null, pstmt1, null);
						throw e;
					}
				}
			}

		} catch (Exception e) {
			log.error(logname + "Exception: ", e);
			throw e;
		} finally {
			closeConnection(null, pstmt2, null);
			closeConnection(null, pstmt1, null);
		}

	}


	private void closeConnection(ResultSet rs, PreparedStatement ps, Connection con) throws Exception {
		try {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (Exception e) {
			log.error(className + " [closeConnection] ", e);
		}
	}

}
