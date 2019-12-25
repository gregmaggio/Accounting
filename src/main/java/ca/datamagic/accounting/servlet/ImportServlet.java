/**
 * 
 */
package ca.datamagic.accounting.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

import ca.datamagic.accounting.importer.ImportRunner;
import ca.datamagic.accounting.importer.Importer;

/**
 * @author Greg
 *
 */
public class ImportServlet extends AuthenticatedServlet {
	private static final Logger logger = LogManager.getLogger(ImportServlet.class);
	private static final long serialVersionUID = 1L;	
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (!isAuthenticated(request)) {
			response.sendError(401);
			return;
		}
		String json = (new Gson()).toJson(Importer.getImporters());
		response.setContentType("application/json");
		response.getWriter().print(json);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.debug("doPost");
		if (!isAuthenticated(request)) {
			response.sendError(401);
			return;
		}
		
		// Get the import date from the request
		String date = request.getParameter("date");
		logger.debug("date: " + date);
		if ((date == null) || (date.length() < 1)) {
			response.sendError(400);
			return;
		}
		
		// Kick off the importer	
		Thread thread = new Thread(new ImportRunner(date));
		thread.start();
		
		response.sendRedirect("/Accounting/importers.html");
	}
}
