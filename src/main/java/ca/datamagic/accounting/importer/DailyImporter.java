/**
 * 
 */
package ca.datamagic.accounting.importer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * @author Greg
 *
 */
public class DailyImporter extends Importer {
	private static Logger logger = LogManager.getLogger(DailyImporter.class);
	
	public DailyImporter() {
		super(getYesterday());
	}
	
	private static String getYesterday() {
		Calendar yesterday = Calendar.getInstance();
		yesterday.add(Calendar.DAY_OF_MONTH, -1);
		SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String fileDate = fileDateFormat.format(yesterday.getTime());
		return fileDate;
	}
	
	public static void main(String[] args) {
		try {
			DOMConfigurator.configure("log4j.importer.cfg.xml");
			DailyImporter importer = new DailyImporter();
			importer.importFile();
		} catch (Throwable t) {
			logger.error("Exception", t);
		}
		Importer.cleanUp();
	}

}
