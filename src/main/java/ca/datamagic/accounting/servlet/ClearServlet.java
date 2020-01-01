/**
 * 
 */
package ca.datamagic.accounting.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import ca.datamagic.accounting.importer.Importer;

/**
 * @author Greg
 *
 */
public class ClearServlet extends AuthenticatedServlet {
	private static final Logger logger = LogManager.getLogger(ClearServlet.class);
	private static final long serialVersionUID = 1L;	
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendError(403);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.debug("doPost");
		if (!isAuthenticated(request)) {
			response.sendError(401);
			return;
		}
		try {
			Importer.clearImporters();
		} catch (Exception ex) {
			logger.error("Exception", ex);
			throw new IOException(ex);
		}
	}
}
