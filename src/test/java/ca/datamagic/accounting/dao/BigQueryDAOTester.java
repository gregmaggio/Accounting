/**
 * 
 */
package ca.datamagic.accounting.dao;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.google.gson.Gson;

import ca.datamagic.accounting.dto.EventDTO;
import ca.datamagic.accounting.dto.EventNameDTO;
import ca.datamagic.accounting.dto.EventTrendDTO;
import ca.datamagic.accounting.testing.BaseTester;

/**
 * @author gregm
 *
 */
public class BigQueryDAOTester extends BaseTester {
	private static final Logger logger = LogManager.getLogger(BigQueryDAOTester.class);
	
	@Test
	public void loadEventNamesTest() throws Exception {
		logger.debug("loadEventNamesTest");
		BigQueryDAO dao = new BigQueryDAO();
		List<EventNameDTO> eventNames = dao.loadEventNames();
		logger.debug("eventNames: " + ((new Gson()).toJson(eventNames)));
	}
	
	@Test
	public void loadEventsTest() throws Exception {
		logger.debug("loadEventsTest");
		BigQueryDAO dao = new BigQueryDAO();
		List<EventDTO> events = dao.loadEvents(2022, 10, 1, 2022, 10, 31);
		logger.debug("events: " + ((new Gson()).toJson(events)));
	}
	
	@Test
	public void loadEventTrendTest() throws Exception {
		logger.debug("loadEventTrendTest");
		BigQueryDAO dao = new BigQueryDAO();
		EventTrendDTO eventTrend = dao.loadEventTrend(2022, 10, 1, 2022, 10, 31, "Application", "Exit");
		logger.debug("eventTrend: " + ((new Gson()).toJson(eventTrend)));
	}
}
