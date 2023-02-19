/**
 * 
 */
package ca.datamagic.accounting.servlet;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import ca.datamagic.accounting.dao.AccountingDAO;
import ca.datamagic.accounting.dto.AccountingParameters;

/**
 * @author Greg
 *
 */
public class AccountingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AccountingServlet.class);
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			StringBuilder builder = new StringBuilder();
			byte[] buffer = new byte[1024];
			InputStream inputStream = request.getInputStream();
			int bytesRead = 0;
			while ((bytesRead = inputStream.read(buffer)) > 0) {
				String text = new String(buffer, 0, bytesRead);
				builder.append(text);
			}
			String requestText = builder.toString();
			AccountingParameters parameters = (new Gson()).fromJson(requestText, AccountingParameters.class);
			AccountingDAO dao = new AccountingDAO();
			dao.log(parameters);
		} catch (Throwable t) {
			logger.error("Exceptoon", t);
			throw new IOError(t);
		}
	}
}
