package com.winnovature.utils.singletons;

import java.sql.Connection;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.commonlib.commondbpool.DBDataSourceFactory;
import com.itextos.beacon.commonlib.commondbpool.DatabaseSchema;
import com.itextos.beacon.commonlib.commondbpool.JndiInfoHolder;
import com.winnovature.utils.utils.Constants;

public class ConnectionFactoryForCMDB {
	static Log log = LogFactory.getLog(Constants.UtilsLogger);
	/**
	 * ConnectionFactory singleton instance.
	 */
	private static ConnectionFactoryForCMDB conFact = new ConnectionFactoryForCMDB();

	/**
	 * class name for logging purpose.
	 */
	private static final String className = "[ConnectionFactoryForCMDB]";

	/**
	 * Private constructor.
	 */

	static DataSource ds = null;

	private ConnectionFactoryForCMDB() {

	}

	/**
	 * This method returns the instance of this class.
	 * 
	 * @return ConnectionFactory instance.
	 */
	public static ConnectionFactoryForCMDB getInstance() {
		return conFact;
	}

	/**
	 * This method returns the database Connection object.
	 * 
	 * @return Connection sql connection instance.
	 */
	/*
	public Connection getConnection() {
		String logName = className + " [getConnection] ";

		try {
			String dsJNDI = JndiPropertiesTon.getInstance().getPropertiesConfiguration()
					.getString(com.winnovature.utils.utils.Constants.MARIADB_JNDI_NAME_CM_DB);

			if (log.isDebugEnabled())
				log.debug(logName + " Getting connection to fileprocessing database " + dsJNDI);

			if (ds == null) {
				InitialContext ic = new InitialContext();

				ds = (DataSource) ic.lookup(dsJNDI);
			}

			return (ds.getConnection());

		} catch (Exception e) {
			log.error(logName + "Exception: ", e);
		}

		return null;

	}
	*/
	
	public Connection getConnection() {
		
        try {
			return DBDataSourceFactory.getConnection(JndiInfoHolder.getJndiInfoUsingName(DatabaseSchema.CM.getKey()));
		} catch (Exception e) {
			
			return null;
		}

	}
}
