/**
 * 
 */
package ca.datamagic.accounting.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Greg
 *
 */
public class DBUtils {
	private static final Logger logger = LogManager.getLogger(DBUtils.class);
	
	public static void close(Connection connection) {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (Throwable t) {
			logger.warn("Exception", t);
		}
	}
	
	public static void close(Statement statement) {
		try {
			if (statement != null) {
				statement.close();
			}
		} catch (Throwable t) {
			logger.warn("Exception", t);
		}
	}
	
	public static void close(ResultSet resultSet) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
		} catch (Throwable t) {
			logger.warn("Exception", t);
		}
	}
}
