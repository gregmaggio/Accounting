/**
 * 
 */
package ca.datamagic.accounting.servlet;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * @author Greg
 *
 */
public class SwaggerAPIServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String requestUrl = request.getRequestURL().toString();
			URL url = new URL(requestUrl);
			StringBuffer host = new StringBuffer();
			host.append(url.getHost());
			if ((url.getPort() != -1) && (url.getPort() != 80) && (url.getPort() != 443)) {
				host.append(":");
				host.append(Integer.toString(url.getPort()));
			}
			InputStream input = getServletContext().getResourceAsStream("/WEB-INF/classes/swagger.json");
			String theString = IOUtils.toString(input, "UTF-8").replace("{{host}}", host.toString()); 
			input.close();
			response.setContentType("application/json");
			response.getWriter().println(theString);
		} catch (Throwable t) {
			throw new IOError(t);
		}
	}
}
