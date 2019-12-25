/**
 * 
 */
package ca.datamagic.accounting.servlet;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

import ca.datamagic.accounting.dao.AccountingDAO;
import ca.datamagic.accounting.dto.AccountingParameters;
import ca.datamagic.accounting.dto.EventDTO;

/**
 * @author Greg
 *
 */
public class AccountingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AccountingServlet.class);
	private static final Logger accounting = LogManager.getLogger("accountingFile");
	private static final Pattern accountingStatsPattern = Pattern.compile("/stats/(?<startYear>\\d+)/(?<startMonth>\\d+)/(?<startDay>\\d+)/(?<endYear>\\d+)/(?<endMonth>\\d+)/(?<endDay>\\d+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern minTimeStampPattern = Pattern.compile("/min", Pattern.CASE_INSENSITIVE);
	private static final Pattern maxTimeStampPattern = Pattern.compile("/max", Pattern.CASE_INSENSITIVE);
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String pathInfo = request.getPathInfo();
			logger.debug("pathInfo: " + pathInfo);
			Matcher accountingStatsMatcher = accountingStatsPattern.matcher(pathInfo);
			if (accountingStatsMatcher.find()) {
				logger.debug("accountingStatsMatcher");
				String startYear = accountingStatsMatcher.group("startYear");
				String startMonth = accountingStatsMatcher.group("startMonth");
				String startDay = accountingStatsMatcher.group("startDay");
				String endYear = accountingStatsMatcher.group("endYear");
				String endMonth = accountingStatsMatcher.group("endMonth");
				String endDay = accountingStatsMatcher.group("endDay");
				logger.debug("startYear: " + startYear);
				logger.debug("startMonth: " + startMonth);
				logger.debug("startDay: " + startDay);
				logger.debug("endYear: " + endYear);
				logger.debug("endMonth: " + endMonth);
				logger.debug("endDay: " + endDay);
				Integer intStartYear = Integer.parseInt(startYear);
				Integer intStartMonth = Integer.parseInt(startMonth);
				Integer intStartDay = Integer.parseInt(startDay);
				Integer intEndYear = Integer.parseInt(endYear);
				Integer intEndMonth = Integer.parseInt(endMonth);
				Integer intEndDay = Integer.parseInt(endDay);
				AccountingDAO dao = new AccountingDAO();
				List<EventDTO> events = dao.loadEvents(intStartYear, intStartMonth, intStartDay, intEndYear, intEndMonth, intEndDay);
				String json = (new Gson()).toJson(events);
				response.setContentType("application/json");
				response.getWriter().println(json);
				return;
			}
			Matcher minTimeStampMatcher = minTimeStampPattern.matcher(pathInfo);
			if (minTimeStampMatcher.find()) {
				logger.debug("minTimeStampMatcher");
				AccountingDAO dao = new AccountingDAO();
				String timeStamp = dao.minTimeStamp();
				String json = (new Gson()).toJson(timeStamp);
				response.setContentType("application/json");
				response.getWriter().println(json);
				return;
			}
			Matcher maxTimeStampMatcher = maxTimeStampPattern.matcher(pathInfo);
			if (maxTimeStampMatcher.find()) {
				logger.debug("maxTimeStampMatcher");
				AccountingDAO dao = new AccountingDAO();
				String timeStamp = dao.maxTimeStamp();
				String json = (new Gson()).toJson(timeStamp);
				response.setContentType("application/json");
				response.getWriter().println(json);
				return;
			}
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} catch (Throwable t) {
			throw new IOError(t);
		}
	}
	
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
			StringBuilder message = new StringBuilder();
			if (parameters.getDeviceLatitude() != null) {
				message.append(Double.toString(parameters.getDeviceLatitude().doubleValue()));
			}
			message.append(",");
			if (parameters.getDeviceLongitude() != null) {
				message.append(Double.toString(parameters.getDeviceLongitude().doubleValue()));
			}
			message.append(",");
			if (parameters.getEventName() != null) {
				message.append(parameters.getEventName());
			}
			message.append(",");
			if (parameters.getEventMessage() != null) {
				boolean containsComma = parameters.getEventMessage().contains(",");
				if (containsComma) {
					message.append("\"");
				}
				message.append(parameters.getEventMessage());
				if (containsComma) {
					message.append("\"");
				}
			}
			accounting.debug(message.toString());
		} catch (Throwable t) {
			logger.error("Exceptoon", t);
			throw new IOError(t);
		}
	}
}
