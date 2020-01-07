/**
 * 
 */
package ca.datamagic.accounting.importer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import ca.datamagic.accounting.dao.AccountingDAO;
import ca.datamagic.accounting.dto.AccountingDTO;
import ca.datamagic.accounting.util.DBUtils;

/**
 * @author Greg
 *
 */
public class Importer {
	private static final Logger logger = LogManager.getLogger(Importer.class);
	private static final Pattern timeStampPattern = Pattern.compile("(?<year>\\d+)-(?<month>\\d+)-(?<day>\\d+)\\s(?<hour>\\d+):(?<minute>\\d+):(?<second>\\d+)\\s(?<timeZone>\\w+)", Pattern.CASE_INSENSITIVE);
	private static SimpleDateFormat utcDateFormat = null;
	private static SimpleDateFormat utcTimeFormat = null;
	private static final List<Importer> importers = new ArrayList<Importer>();
	private static AccountingDAO dao = null;
	private static Connection connection = null;
	private String date = null;
	private boolean running = false;
	private boolean error = false;
	private long lineNumber = 0L;
	private String lastError = null;
	private String startTimeUTC = null;
	private String endTimeUTC = null;
	
	static {
		TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
		utcDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		utcDateFormat.setTimeZone(utcTimeZone);
		utcTimeFormat = new SimpleDateFormat("HH:mm:ss");
		utcTimeFormat.setTimeZone(utcTimeZone);
	}
	
	public Importer(String date) {
		this.date = date;
	}
	
	public static synchronized void cleanUp() {
		if (connection != null) {
			DBUtils.close(connection);
		}
		dao = null;
		connection = null;
	}
	
	private static synchronized AccountingDAO getDAO() throws IOException {
		if (dao == null) {
			dao = new AccountingDAO();
		}
		return dao;
	}
	
	private static synchronized Connection getConnection() throws SQLException, IOException {
		if (connection == null) {
			connection = getDAO().openConnection();
		}
		return connection;
	}
	
	public boolean isRunning() {
		return this.running;
	}
	
	public boolean hasError() {
		return this.error;
	}
	
	public String getLastError() {
		return this.lastError;
	}
	
	public String getStartTimeUTC() {
		return this.startTimeUTC;
	}
	
	public String getEndTimeUTC() {
		return this.endTimeUTC;
	}
	
	public String getDate() {
		return this.date;
	}
	
	public long getLineNumber() {
		return this.lineNumber;
	}
	
	private static String currentTimeGMT() {
		Calendar calendar = Calendar.getInstance();
		String datePart = utcDateFormat.format(calendar.getTime());
	    String timePart = utcTimeFormat.format(calendar.getTime());
	    return MessageFormat.format("{0}T{1}Z", datePart, timePart);
	}
	
	private static String localToGMT(int year, int month, int day, int hour, int minute, int second, TimeZone localTimeZone) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(localTimeZone);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
	    String datePart = utcDateFormat.format(calendar.getTime());
	    String timePart = utcTimeFormat.format(calendar.getTime());
	    return MessageFormat.format("{0}T{1}Z", datePart, timePart);
	}
	
	private static Double tryParseDouble(String text) {
		if (text == null) {
			return null;
		}
		text = text.trim();
		if (text.length() < 1) {
			return null;
		}
		try {
			return new Double(text);
		} catch (NumberFormatException ex) {
			return null;
		}
	}
	
	public static synchronized Importer[] getImporters() {
		if (importers.size() < 1) {
			return null;
		}
		Importer[] array = new Importer[importers.size()];
		for (int ii = 0; ii < importers.size(); ii++) {
			array[ii] = importers.get(ii);
		}
		return array;
	}
	
	private static synchronized void addImporter(Importer importer) {
		importers.add(importer);
	}
	
	public static synchronized void clearImporters() throws Exception {
		for (int ii = 0; ii < importers.size(); ii++) {
			if (importers.get(ii).isRunning()) {
				throw new Exception("Someone is still running!");
			}
		}
		importers.clear();
	}
	
	public void importFile() {
		addImporter(this);
		BufferedReader reader = null;
		try {
			this.running = true;
			this.error = false;
			this.startTimeUTC = currentTimeGMT();
					
			TimeZone timeZone = TimeZone.getTimeZone("America/Chicago");
			
			String inputFileName = MessageFormat.format("{0}/accounting.csv.{1}_", getDAO().getInputDirectory(), this.date);
			logger.debug("inputFileName: " + inputFileName);
			String outputFileName = MessageFormat.format("{0}/accounting.csv.{1}_", getDAO().getOutputDirectory(), this.date);
			logger.debug("outputFileName: " + outputFileName);
			Path inputPath = Paths.get(inputFileName);
			Path outputPath = Paths.get(outputFileName);
			if (Files.notExists(inputPath)) {
				inputFileName = MessageFormat.format("{0}/accounting.csv.{1}", getDAO().getInputDirectory(), this.date);
				logger.debug("inputFileName: " + inputFileName);
				outputFileName = MessageFormat.format("{0}/accounting.csv.{1}", getDAO().getOutputDirectory(), this.date);
				logger.debug("outputFileName: " + outputFileName);
				inputPath = Paths.get(inputFileName);
				outputPath = Paths.get(outputFileName);
			}
			if (Files.notExists(inputPath)) {
				inputFileName = MessageFormat.format("{0}/accounting.csv.{1} ", getDAO().getInputDirectory(), this.date);
				logger.debug("inputFileName: " + inputFileName);
				outputFileName = MessageFormat.format("{0}/accounting.csv.{1} ", getDAO().getOutputDirectory(), this.date);
				logger.debug("outputFileName: " + outputFileName);
				inputPath = Paths.get(inputFileName);
				outputPath = Paths.get(outputFileName);
			}
			if (Files.notExists(inputPath)) {
				throw new Exception(MessageFormat.format("No accounting file found for date {0}.", this.date));
			}
			this.lineNumber = 0L;
			reader = new BufferedReader(new FileReader(inputFileName));
			String currentLine = null;
			while ((currentLine = reader.readLine()) != null) {
				String[] items = currentLine.split(",");
				String timeStamp = null;
				String latitudeText = null;
				String longitudeText = null;
				String eventName = null;
				String eventMessage = null;
				if (items.length > 0)
					timeStamp = items[0];
				if (items.length > 1)
					latitudeText = items[1];
				if (items.length > 2)
					longitudeText = items[2];
				if (items.length > 3)
					eventName = items[3];
				if (items.length > 4)
					eventMessage = items[4];
				if ((timeStamp == null) || (latitudeText == null) || (longitudeText == null) || (eventName == null)) {
					logger.warn("Could not parse all the required arguments for line " + (this.lineNumber + 1));
					logger.warn("currentLine: " + currentLine);
					continue;
				}
				logger.debug(timeStamp + "," + latitudeText + "," + longitudeText + "," + eventName + "," + eventMessage);
				Matcher timeStampMatcher = timeStampPattern.matcher(timeStamp);
				logger.debug("timeStampMatcher: " + timeStampMatcher.matches());
				if (timeStampMatcher.matches()) {
					int year = Integer.parseInt(timeStampMatcher.group("year"));
					int month = Integer.parseInt(timeStampMatcher.group("month"));
					int day = Integer.parseInt(timeStampMatcher.group("day"));
					int hour = Integer.parseInt(timeStampMatcher.group("hour"));
					int minute = Integer.parseInt(timeStampMatcher.group("minute"));
					int second = Integer.parseInt(timeStampMatcher.group("second"));
					String timeStampUTC = localToGMT(year, month, day, hour, minute, second, timeZone);
					logger.debug("timeStampUTC: " + timeStampUTC);
					
					Double latitude = tryParseDouble(latitudeText);
					Double longitude = tryParseDouble(longitudeText);
					
					AccountingDTO accounting = new AccountingDTO();
					accounting.setTimeStamp(timeStampUTC);
					accounting.setDeviceLatitude(latitude);
					accounting.setDeviceLongitude(longitude);
					accounting.setEventName(eventName);
					accounting.setEventMessage(eventMessage);
					getDAO().save(accounting, getConnection());
				}
				this.lineNumber++;
			}
			reader.close();
			reader = null;
			Files.move(inputPath, outputPath);
		} catch (Throwable t) {
			this.error = true;
			this.lastError = t.getMessage();
			logger.error("Exception", t);			
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Throwable t) {
					logger.warn("Exception", t);
				}
			}
			this.running = false;
			this.endTimeUTC = currentTimeGMT();
		}
	}
	
	public static void main(String[] args) {
		try {
			DOMConfigurator.configure("log4j.importer.cfg.xml");
			String date = null;
			for (int ii = 0; ii < args.length; ) {
				String arg = args[ii++];
				if (arg.toLowerCase().contains("date")) {
					if (ii < args.length) {
						date = args[ii++];
					}
				}
			}
			if (date == null) {
				System.out.println("Usage:");
				System.out.println("ca.datamagic.accounting.importer.Importer --date {{date}}");
				return;
			}
			Importer importer = new Importer(date);
			importer.importFile();
		} catch (Throwable t) {
			logger.error("Exception", t);
		}
		Importer.cleanUp();
	}
}
