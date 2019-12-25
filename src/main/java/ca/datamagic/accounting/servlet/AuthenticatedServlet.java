/**
 * 
 */
package ca.datamagic.accounting.servlet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

import ca.datamagic.accounting.dao.AccountingDAO;
import ca.datamagic.accounting.util.IOUtils;

/**
 * @author Greg
 *
 */
public abstract class AuthenticatedServlet extends HttpServlet {
	private static final Logger logger = LogManager.getLogger(AuthenticatedServlet.class);
	private static final long serialVersionUID = 1L;
	
	protected static boolean isAuthenticated(HttpServletRequest request) throws IOException {
		AccountingDAO dao = new AccountingDAO();
		Cookie sessionCookie = null;
		Cookie[] cookies = request.getCookies();
		for (int ii = 0; ii < cookies.length; ii++) {
			if (cookies[ii].getName().compareToIgnoreCase(dao.getCookieName()) == 0) {
				sessionCookie = cookies[ii];
				break;
			}
		}
		logger.debug("sessionCookie: " + sessionCookie);
		if (sessionCookie == null) {
			return false;
		}
		
		StringBuilder cookieString = new StringBuilder();
		cookieString.append(sessionCookie.getName() + "=" + sessionCookie.getValue() + ";");
		if ((sessionCookie.getDomain() != null) && (sessionCookie.getDomain().length() > 0)) {
			cookieString.append("domain=" + sessionCookie.getDomain() + ";");
		}
		if (sessionCookie.getSecure()) {
			cookieString.append("Secure;");
		}
		if (sessionCookie.isHttpOnly()) {
			cookieString.append("HttpOnly;");
		}
		logger.debug("cookieString: " + cookieString.toString());
		
		StringBuilder whoAmIURL = new StringBuilder();
		whoAmIURL.append(request.getScheme());
		whoAmIURL.append("://");
		whoAmIURL.append(request.getServerName());
		if (request.getServerPort() != 80) {
			whoAmIURL.append(":");
			whoAmIURL.append(request.getServerPort());
		}
		whoAmIURL.append("/whoAmI");
		logger.debug("whoAmIURL: " + whoAmIURL.toString());
		
		// Check whoAmI for a logged in user
		HttpURLConnection connection = null;
        try {
        	URL url = new URL(whoAmIURL.toString());
            connection = (HttpURLConnection)url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.addRequestProperty("Cookie", cookieString.toString());
			connection.connect();
            String responseText = IOUtils.readEntireStream(connection.getInputStream());
            logger.debug("responseText: " + responseText);
            String whoAmI = (new Gson()).fromJson(responseText, String.class);
            logger.debug("whoAmI: " + whoAmI);
            return ((whoAmI != null) && (whoAmI.length() > 0));
        } catch (IOException ex) {
        	logger.warn("IOException", ex);
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Throwable t) {
                	logger.warn("Throwable", t);
                }
            }
        }
        return false;
	}
}
