package com.winnovature.initialstate;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionFactory {

	public static Connection getConnection() {

		try {
			System.out.println(" Getting connection to database");

			Connection conn = null;
			try {
				String driverClass = "org.mariadb.jdbc.Driver";
				String jdbcUrl = "jdbc:mariadb://192.168.1.110:3306/custdb";
				String user = "devuser";
				String pwd = "devuser@123";

				Class.forName(driverClass);
				conn = DriverManager.getConnection(jdbcUrl, user, pwd);

				return conn;

			} catch (Exception e) {
				System.err.println(e);
			}

		} catch (Exception e) {
			System.err.println(e);
		}
		return null;
	}

}
