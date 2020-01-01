/**
 * 
 */
package ca.datamagic.accounting.dao;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Greg
 *
 */
public abstract class BaseDAO {
	private static Logger logger = LogManager.getLogger(BaseDAO.class);
	private static String dataPath = "C:/Dev/Applications/Accounting/src/main/resources";
	
	static {
		try {
			//Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL 6+
			Class.forName("com.mysql.jdbc.Driver"); // Pre MySQL 6
		} catch (Throwable t) {
			logger.error("Exception", t);
		}
	}
	
	public static String getDataPath() {
		return dataPath;
	}
	
	public static void setDataPath(String newVal) {
		dataPath = newVal;
	}
}
