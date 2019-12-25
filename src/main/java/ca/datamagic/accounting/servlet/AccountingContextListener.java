/**
 * 
 */
package ca.datamagic.accounting.servlet;

import java.text.MessageFormat;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.xml.DOMConfigurator;

import ca.datamagic.accounting.dao.BaseDAO;

/**
 * @author Greg
 *
 */
public class AccountingContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		String realPath = sce.getServletContext().getRealPath("/");
		String dataPath = MessageFormat.format("{0}/WEB-INF/classes", realPath);
		String fileName = MessageFormat.format("{0}/WEB-INF/classes/log4j.cfg.xml", realPath);
		DOMConfigurator.configure(fileName);
		BaseDAO.setDataPath(dataPath);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
}
