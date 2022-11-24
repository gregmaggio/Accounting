/**
 * 
 */
package ca.datamagic.accounting.servlet;

import java.text.MessageFormat;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.datamagic.accounting.dao.BaseDAO;

/**
 * @author Greg
 *
 */
public class AccountingContextListener implements ServletContextListener {
	private static Logger logger = LogManager.getLogger(AccountingContextListener.class);
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		String realPath = sce.getServletContext().getRealPath("/");
		String dataPath = MessageFormat.format("{0}/WEB-INF/classes", realPath);
		BaseDAO.setDataPath(dataPath);
		logger.debug("contextInitialized");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.debug("contextDestroyed");
	}
}
