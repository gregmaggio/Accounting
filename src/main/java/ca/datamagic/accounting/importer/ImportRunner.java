/**
 * 
 */
package ca.datamagic.accounting.importer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Greg
 *
 */
public class ImportRunner implements Runnable {
	private static final Logger logger = LogManager.getLogger(ImportRunner.class);
	private static final Pattern datePattern = Pattern.compile("(?<year>\\d+)\\x2D(?<month>\\d+)\\x2D(?<day>\\d+)", Pattern.CASE_INSENSITIVE);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private String date = null;
	private String startDate = null;
	private String endDate = null;
	
	public ImportRunner(String date) {
		this.date = date;
	}
	
	public ImportRunner(String startDate, String endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	@Override
	public void run() {
		try {
			if ((this.date != null) && (this.date.length() > 0)) {
				logger.debug("date: " + this.date);
				Importer importer = new Importer(this.date);
				importer.importFile();
			} else if ((this.startDate != null) && (this.startDate.length() > 0) &&
					   (this.endDate != null) && (this.endDate.length() > 0)) {
				logger.debug("startDate: " + this.startDate);
				logger.debug("endDate: " + this.endDate);
				Matcher startDateMatcher = datePattern.matcher(this.startDate);
				Matcher endDateMatcher = datePattern.matcher(this.endDate);
				if (startDateMatcher.matches() && endDateMatcher.matches()) {
					int startYear = Integer.parseInt(startDateMatcher.group("year"));
					int startMonth = Integer.parseInt(startDateMatcher.group("month"));
					int startDay = Integer.parseInt(startDateMatcher.group("day"));
					int endYear = Integer.parseInt(endDateMatcher.group("year"));
					int endMonth = Integer.parseInt(endDateMatcher.group("month"));
					int endDay = Integer.parseInt(endDateMatcher.group("day"));
					Calendar startCalendar = Calendar.getInstance();
					startCalendar.set(startYear, startMonth - 1, startDay);
					Calendar endCalendar = Calendar.getInstance();
					endCalendar.set(endYear, endMonth - 1, endDay);
					endCalendar.add(Calendar.DATE, 1);
					Calendar current = startCalendar;					
					while (current.before(endCalendar)) {
						String currentDate = dateFormat.format(current.getTime());
						logger.debug("currentDate: " + currentDate);
						Importer importer = new Importer(currentDate);
						importer.importFile();
						current.add(Calendar.DATE, 1);
					}
				}
			}
		} catch (Throwable t) {
			logger.error("Exception", t);
		}
		Importer.cleanUp();
	}
}
