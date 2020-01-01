/**
 * 
 */
package ca.datamagic.accounting.dao;

import java.io.FileInputStream;
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
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import ca.datamagic.accounting.dto.AccountingDTO;
import ca.datamagic.accounting.dto.EventDTO;
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
			statement.setString(4, accounting.getEventName());
			statement.setString(5, accounting.getEventMessage());
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
			statement = connection.prepareStatement("select event_name, count(event_message) as event_count from accounting where time_stamp > ? and time_stamp < ? group by event_name order by event_name");
			statement.setString(1, startTimeUTC);
			statement.setString(2, endTimeUTC);
			resultSet = statement.executeQuery();
			List<EventDTO> events = new ArrayList<EventDTO>();
			while (resultSet.next()) {
				EventDTO dto = new EventDTO();
				dto.setEventName(resultSet.getString("event_name"));
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
