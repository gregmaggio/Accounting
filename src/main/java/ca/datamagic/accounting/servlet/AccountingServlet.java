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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

import ca.datamagic.accounting.dao.AccountingDAO;
import ca.datamagic.accounting.dto.AccountingParameters;
import ca.datamagic.accounting.dto.EventDTO;
import ca.datamagic.accounting.dto.EventNameDTO;
import ca.datamagic.accounting.dto.EventTrendDTO;

/**
 * @author Greg
 *
 */
public class AccountingServlet extends AuthenticatedServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AccountingServlet.class);
	private static final Logger accounting = LogManager.getLogger("accountingFile");
	private static final Pattern accountingStatsPattern = Pattern.compile("/stats/(?<startYear>\\d+)/(?<startMonth>\\d+)/(?<startDay>\\d+)/(?<endYear>\\d+)/(?<endMonth>\\d+)/(?<endDay>\\d+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern minTimeStampPattern = Pattern.compile("/min", Pattern.CASE_INSENSITIVE);
	private static final Pattern maxTimeStampPattern = Pattern.compile("/max", Pattern.CASE_INSENSITIVE);
	private static final Pattern filesPattern = Pattern.compile("/files", Pattern.CASE_INSENSITIVE);
	private static final Pattern loadedPattern = Pattern.compile("/loaded", Pattern.CASE_INSENSITIVE);
	private static final Pattern eventNamesPattern = Pattern.compile("/eventNames", Pattern.CASE_INSENSITIVE);
	private static final Pattern trendPattern = Pattern.compile("/trend/(?<startYear>\\d+)/(?<startMonth>\\d+)/(?<startDay>\\d+)/(?<endYear>\\d+)/(?<endMonth>\\d+)/(?<endDay>\\d+)/(?<eventName>\\w+)/(?<eventMessage>\\w+)", Pattern.CASE_INSENSITIVE);
	
	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String origin = request.getHeader("Origin");
		logger.debug("origin: " + origin);
		addCorsHeaders(origin, response);
		response.setHeader("Content-Length", "0");
		response.setHeader("Content-Type", "text/plain");
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String origin = request.getHeader("Origin");
			logger.debug("origin: " + origin);
			String pathInfo = request.getPathInfo();
			logger.debug("pathInfo: " + pathInfo);
			Matcher accountingStatsMatcher = accountingStatsPattern.matcher(pathInfo);
			if (accountingStatsMatcher.find()) {
				logger.debug("accountingStatsMatcher");
				if (!isAuthenticated(request)) {
					response.sendError(401);
					return;
				}
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
				addCorsHeaders(origin, response);
				response.setContentType("application/json");
				response.getWriter().println(json);
				return;
			}
			Matcher minTimeStampMatcher = minTimeStampPattern.matcher(pathInfo);
			if (minTimeStampMatcher.find()) {
				logger.debug("minTimeStampMatcher");
				if (!isAuthenticated(request)) {
					response.sendError(401);
					return;
				}
				AccountingDAO dao = new AccountingDAO();
				String timeStamp = dao.minTimeStamp();
				String json = (new Gson()).toJson(timeStamp);
				addCorsHeaders(origin, response);
				response.setContentType("application/json");
				response.getWriter().println(json);
				return;
			}
			Matcher maxTimeStampMatcher = maxTimeStampPattern.matcher(pathInfo);
			if (maxTimeStampMatcher.find()) {
				logger.debug("maxTimeStampMatcher");
				if (!isAuthenticated(request)) {
					response.sendError(401);
					return;
				}
				AccountingDAO dao = new AccountingDAO();
				String timeStamp = dao.maxTimeStamp();
				String json = (new Gson()).toJson(timeStamp);
				addCorsHeaders(origin, response);
				response.setContentType("application/json");
				response.getWriter().println(json);
				return;
			}
			Matcher filesMatcher = filesPattern.matcher(pathInfo);
			if (filesMatcher.find()) {
				logger.debug("filesMatcher");
				if (!isAuthenticated(request)) {
					response.sendError(401);
					return;
				}
				AccountingDAO dao = new AccountingDAO();
				String[] files = dao.getAccountingFiles();
				String json = (new Gson()).toJson(files);
				addCorsHeaders(origin, response);
				response.setContentType("application/json");
				response.getWriter().println(json);
				return;
			}
			Matcher loadedMatcher = loadedPattern.matcher(pathInfo);
			if (loadedMatcher.find()) {
				logger.debug("loadedMatcher");
				if (!isAuthenticated(request)) {
					response.sendError(401);
					return;
				}
				AccountingDAO dao = new AccountingDAO();
				String[] files = dao.getLoadedFiles();
				String json = (new Gson()).toJson(files);
				addCorsHeaders(origin, response);
				response.setContentType("application/json");
				response.getWriter().println(json);
				return;
			}
			Matcher eventNamesMatcher = eventNamesPattern.matcher(pathInfo);
			if (eventNamesMatcher.find()) {
				logger.debug("eventNamesMatcher");
				if (!isAuthenticated(request)) {
					response.sendError(401);
					return;
				}
				AccountingDAO dao = new AccountingDAO();
				List<EventNameDTO> eventNames = dao.loadEventNames();
				String json = (new Gson()).toJson(eventNames);
				addCorsHeaders(origin, response);
				response.setContentType("application/json");
				response.getWriter().println(json);
				return;
			}			
			Matcher trendMatcher = trendPattern.matcher(pathInfo);
			if (trendMatcher.find()) {
				logger.debug("trendMatcher");
				if (!isAuthenticated(request)) {
					response.sendError(401);
					return;
				}
				String startYear = trendMatcher.group("startYear");
				String startMonth = trendMatcher.group("startMonth");
				String startDay = trendMatcher.group("startDay");
				String endYear = trendMatcher.group("endYear");
				String endMonth = trendMatcher.group("endMonth");
				String endDay = trendMatcher.group("endDay");
				String eventName = trendMatcher.group("eventName");
				String eventMessage = trendMatcher.group("eventMessage");
				logger.debug("startYear: " + startYear);
				logger.debug("startMonth: " + startMonth);
				logger.debug("startDay: " + startDay);
				logger.debug("endYear: " + endYear);
				logger.debug("endMonth: " + endMonth);
				logger.debug("endDay: " + endDay);
				logger.debug("eventName: " + eventName);
				logger.debug("eventMessage: " + eventMessage);
				Integer intStartYear = Integer.parseInt(startYear);
				Integer intStartMonth = Integer.parseInt(startMonth);
				Integer intStartDay = Integer.parseInt(startDay);
				Integer intEndYear = Integer.parseInt(endYear);
				Integer intEndMonth = Integer.parseInt(endMonth);
				Integer intEndDay = Integer.parseInt(endDay);
				AccountingDAO dao = new AccountingDAO();
				EventTrendDTO eventTrend = dao.loadEventTrend(intStartYear, intStartMonth, intStartDay, intEndYear, intEndMonth, intEndDay, eventName, eventMessage);
				String json = (new Gson()).toJson(eventTrend);
				addCorsHeaders(origin, response);
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
