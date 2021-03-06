/**
 * 
 */
package ca.datamagic.accounting.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import ca.datamagic.accounting.dto.AccountingDTO;
import ca.datamagic.accounting.dto.EventDTO;
import ca.datamagic.accounting.dto.EventNameDTO;
import ca.datamagic.accounting.dto.EventTrendDTO;
import ca.datamagic.accounting.dto.EventTrendSeriesDTO;
import ca.datamagic.accounting.util.DBUtils;
import ca.datamagic.accounting.util.IOUtils;

/**
 * @author Greg
 *
 */
public class AccountingDAO extends BaseDAO {
	private static Logger logger = LogManager.getLogger(AccountingDAO.class);
	private static SimpleDateFormat dateFormat = null;
	private static NumberFormat monthFormat = null;
	private static NumberFormat dayFormat = null;
	private Properties properties = null;
	
	static {
		dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		monthFormat = NumberFormat.getInstance();
		monthFormat.setMinimumIntegerDigits(2);
		dayFormat = NumberFormat.getInstance();
		dayFormat.setMinimumIntegerDigits(2);
	}
	
	public AccountingDAO() throws IOException {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(MessageFormat.format("{0}/secure.properties", getDataPath()));
			this.properties = new Properties();
			this.properties.load(inputStream);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
	
	public String getConnectionString() {
		return this.properties.getProperty("connectionString");
	}
	
	public String getUserName() {
		return this.properties.getProperty("dbUser");
	}
	
	public String getPassword() {
		return this.properties.getProperty("dbPass");
	}
	
	public String getInputDirectory() {
		return this.properties.getProperty("inputDirectory");
	}
	
	public String getOutputDirectory() {
		return this.properties.getProperty("outputDirectory");
	}
	
	public String getCookieName() {
		return this.properties.getProperty("cookieName");
	}
	
	public Connection openConnection() throws SQLException {
		return DriverManager.getConnection(getConnectionString(), getUserName(), getPassword());
	}
	
	public String[] getAccountingFiles() throws IOException {
		File inputDirectory = new File(getInputDirectory());
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (name.toLowerCase().contains("accounting.csv"));
			}
		};
		String[] files = inputDirectory.list(filter);
		Comparator<String> comparator = new Comparator<String>() {			
			@Override
			public int compare(String o1, String o2) {
				if ((o1 != null) && (o2 != null)) {
					return o1.compareToIgnoreCase(o2);
				}
				return 0;
			}
		};
		Arrays.sort(files, comparator);
		return files;
	}
	
	public String[] getLoadedFiles() throws IOException {
		File outputDirectory = new File(getOutputDirectory());
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (name.toLowerCase().contains("accounting.csv"));
			}
		};
		String[] files = outputDirectory.list(filter);
		Comparator<String> comparator = new Comparator<String>() {			
			@Override
			public int compare(String o1, String o2) {
				if ((o1 != null) && (o2 != null)) {
					return o1.compareToIgnoreCase(o2);
				}
				return 0;
			}
		};
		Arrays.sort(files, comparator);
		return files;
	}
	
	public void save(AccountingDTO accounting) throws Exception {
		Connection connection = null;
		try {
			connection = openConnection();
			save(accounting, connection);
		} finally {
			if (connection != null) {
				DBUtils.close(connection);
			}
		}
	}
	
	public void save(AccountingDTO accounting, Connection connection) throws Exception {
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement("INSERT INTO accounting (time_stamp, latitude, longitude, event_name, event_message) VALUES (?, ?, ?, ?, ?)");
			statement.setString(1, accounting.getTimeStamp());
			if (accounting.getDeviceLatitude() == null) {
				statement.setNull(2, Types.DOUBLE);
			} else {
				statement.setDouble(2, accounting.getDeviceLatitude());
			}
			if (accounting.getDeviceLongitude() == null) {
				statement.setNull(3, Types.DOUBLE);
			} else {
				statement.setDouble(3, accounting.getDeviceLongitude());
			}			
			if (accounting.getEventName() == null) {
				statement.setNull(4, Types.VARCHAR);
			} else {
				statement.setString(4, accounting.getEventName());
			}
			if (accounting.getEventMessage() == null) {
				statement.setNull(5, Types.VARCHAR);
			} else {
				statement.setString(5, accounting.getEventMessage());
			}
			statement.executeUpdate();
		} finally {
			if (statement != null) {
				DBUtils.close(statement);
			}
		}
	}
	
	public List<EventDTO> loadEvents(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) throws SQLException {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			logger.debug("loadEvents");
			logger.debug("startYear: " + Integer.toString(startYear));
			logger.debug("startMonth: " + Integer.toString(startMonth));
			logger.debug("startDay: " + Integer.toString(startDay));
			logger.debug("endYear: " + Integer.toString(endYear));
			logger.debug("endMonth: " + Integer.toString(endMonth));
			logger.debug("endDay: " + Integer.toString(endDay));
			Calendar startTime = Calendar.getInstance();
			startTime.set(Calendar.YEAR, startYear);
			startTime.set(Calendar.MONTH, startMonth - 1);
			startTime.set(Calendar.DAY_OF_MONTH, startDay);
			startTime.set(Calendar.HOUR_OF_DAY, 0);
			startTime.set(Calendar.MINUTE, 0);
			startTime.set(Calendar.SECOND, 0);
			startTime.set(Calendar.MILLISECOND, 0);
			String startTimeUTC = dateFormat.format(startTime.getTime());
			logger.debug("startTimeUTC: " + startTimeUTC);
			Calendar endTime = Calendar.getInstance();
			endTime.set(Calendar.YEAR, endYear);
			endTime.set(Calendar.MONTH, endMonth - 1);
			endTime.set(Calendar.DAY_OF_MONTH, endDay);
			endTime.set(Calendar.HOUR_OF_DAY, 0);
			endTime.set(Calendar.MINUTE, 0);
			endTime.set(Calendar.SECOND, 0);
			endTime.set(Calendar.MILLISECOND, 0);
			endTime.add(Calendar.DAY_OF_MONTH, 1);
			String endTimeUTC = dateFormat.format(endTime.getTime());
			logger.debug("endTimeUTC: " + endTimeUTC);
			connection = DriverManager.getConnection(getConnectionString(), getUserName(), getPassword());
			statement = connection.prepareStatement("select event_name, event_message, count(event_message) as event_count from accounting where time_stamp > ? and time_stamp < ? group by event_name, event_message order by event_name, event_message");
			statement.setString(1, startTimeUTC);
			statement.setString(2, endTimeUTC);
			resultSet = statement.executeQuery();
			List<EventDTO> events = new ArrayList<EventDTO>();
			while (resultSet.next()) {
				EventDTO dto = new EventDTO();
				dto.setEventName(resultSet.getString("event_name"));
				dto.setEventMessage(resultSet.getString("event_message"));
				dto.setCount(resultSet.getLong("event_count"));
				events.add(dto);
			}
			return events;
		} finally {
			if (resultSet != null) {
				DBUtils.close(resultSet);
			}
			if (statement != null) {
				DBUtils.close(statement);
			}
			if (connection != null) {
				DBUtils.close(connection);
			}
		}
	}
	
	public List<EventNameDTO> loadEventNames() throws SQLException {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			logger.debug("loadEventNames");
			connection = DriverManager.getConnection(getConnectionString(), getUserName(), getPassword());
			statement = connection.prepareStatement("select distinct event_name, event_message from accounting order by event_name, event_message");
			resultSet = statement.executeQuery();
			List<EventNameDTO> eventNames = new ArrayList<EventNameDTO>();
			while (resultSet.next()) {
				EventNameDTO dto = new EventNameDTO();
				dto.setEventName(resultSet.getString("event_name"));
				dto.setEventMessage(resultSet.getString("event_message"));
				eventNames.add(dto);
			}
			return eventNames;
		} finally {
			if (resultSet != null) {
				DBUtils.close(resultSet);
			}
			if (statement != null) {
				DBUtils.close(statement);
			}
			if (connection != null) {
				DBUtils.close(connection);
			}
		}
	}
	
	public EventTrendDTO loadEventTrend(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay, String eventName, String eventMessage) throws SQLException {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			logger.debug("loadEventTrend");
			logger.debug("startYear: " + Integer.toString(startYear));
			logger.debug("startMonth: " + Integer.toString(startMonth));
			logger.debug("startDay: " + Integer.toString(startDay));
			logger.debug("endYear: " + Integer.toString(endYear));
			logger.debug("endMonth: " + Integer.toString(endMonth));
			logger.debug("endDay: " + Integer.toString(endDay));
			logger.debug("eventName: " + eventName);
			logger.debug("eventMessage: " + eventMessage);
			Calendar startTime = Calendar.getInstance();
			startTime.set(Calendar.YEAR, startYear);
			startTime.set(Calendar.MONTH, startMonth - 1);
			startTime.set(Calendar.DAY_OF_MONTH, startDay);
			startTime.set(Calendar.HOUR_OF_DAY, 0);
			startTime.set(Calendar.MINUTE, 0);
			startTime.set(Calendar.SECOND, 0);
			startTime.set(Calendar.MILLISECOND, 0);
			String startTimeUTC = dateFormat.format(startTime.getTime());
			logger.debug("startTimeUTC: " + startTimeUTC);
			Calendar endTime = Calendar.getInstance();
			endTime.set(Calendar.YEAR, endYear);
			endTime.set(Calendar.MONTH, endMonth - 1);
			endTime.set(Calendar.DAY_OF_MONTH, endDay);
			endTime.set(Calendar.HOUR_OF_DAY, 0);
			endTime.set(Calendar.MINUTE, 0);
			endTime.set(Calendar.SECOND, 0);
			endTime.set(Calendar.MILLISECOND, 0);
			endTime.add(Calendar.DAY_OF_MONTH, 1);
			String endTimeUTC = dateFormat.format(endTime.getTime());
			logger.debug("endTimeUTC: " + endTimeUTC);
			connection = DriverManager.getConnection(getConnectionString(), getUserName(), getPassword());
			statement = connection.prepareStatement("select YEAR(time_stamp) as event_year, MONTH(time_stamp) as event_month, DAY(time_stamp) as event_day, count(event_message) as event_count from accounting where time_stamp > ? and time_stamp < ? and event_name = ? and event_message = ? group by event_year, event_month, event_day order by event_year, event_month, event_day");
			statement.setString(1, startTimeUTC);
			statement.setString(2, endTimeUTC);
			statement.setString(3, eventName);
			statement.setString(4, eventMessage);
			resultSet = statement.executeQuery();			
			List<EventTrendSeriesDTO> eventTrendSeries = new ArrayList<EventTrendSeriesDTO>();
			while (resultSet.next()) {
				EventTrendSeriesDTO dto = new EventTrendSeriesDTO();
				dto.setYear(resultSet.getInt("event_year"));
				dto.setMonth(resultSet.getInt("event_month"));
				dto.setDay(resultSet.getInt("event_day"));
				dto.setCount(resultSet.getLong("event_count"));
				eventTrendSeries.add(dto);
			}
			EventTrendDTO eventTrend = new EventTrendDTO();
			eventTrend.setEventName(eventName);
			eventTrend.setEventMessage(eventMessage);
			eventTrend.setSeries(eventTrendSeries);
			return eventTrend;
		} finally {
			if (resultSet != null) {
				DBUtils.close(resultSet);
			}
			if (statement != null) {
				DBUtils.close(statement);
			}
			if (connection != null) {
				DBUtils.close(connection);
			}
		}
	}
	
	public String minTimeStamp() throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			connection = DriverManager.getConnection(getConnectionString(), getUserName(), getPassword());
			statement = connection.createStatement();
			resultSet = statement.executeQuery("select min(time_stamp) as time_stamp from accounting");
			if (resultSet.next()) {
				return resultSet.getString("time_stamp");
			}
			return null;
		} finally {
			if (resultSet != null) {
				DBUtils.close(resultSet);
			}
			if (statement != null) {
				DBUtils.close(statement);
			}
			if (connection != null) {
				DBUtils.close(connection);
			}
		}
	}
	
	public String maxTimeStamp() throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			connection = DriverManager.getConnection(getConnectionString(), getUserName(), getPassword());
			statement = connection.createStatement();
			resultSet = statement.executeQuery("select max(time_stamp) as time_stamp from accounting");
			if (resultSet.next()) {
				return resultSet.getString("time_stamp");
			}
			return null;
		} finally {
			if (resultSet != null) {
				DBUtils.close(resultSet);
			}
			if (statement != null) {
				DBUtils.close(statement);
			}
			if (connection != null) {
				DBUtils.close(connection);
			}
		}
	}
}
