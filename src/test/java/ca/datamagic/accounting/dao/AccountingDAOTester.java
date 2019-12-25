/**
 * 
 */
package ca.datamagic.accounting.dao;

import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.datamagic.accounting.dto.EventDTO;
import ca.datamagic.accounting.importer.DailyImporter;

/**
 * @author Greg
 *
 */
public class AccountingDAOTester {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DOMConfigurator.configure("src/test/resources/log4j.cfg.xml");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1() throws Exception {
		AccountingDAO dao = new AccountingDAO();
		List<EventDTO> events = dao.loadEvents(2019, 8, 1, 2019, 9, 1);
		for (int ii = 0; ii < events.size(); ii++) {
			System.out.println(events.get(ii));
		}
	}

	@Test
	public void test2() throws Exception {
		AccountingDAO dao = new AccountingDAO();
		String minTimeStamp = dao.minTimeStamp();
		System.out.println("minTimeStamp: " + minTimeStamp);
		String maxTimeStamp = dao.maxTimeStamp();
		System.out.println("maxTimeStamp: " + maxTimeStamp);
	}
	
	@Test
	public void test3() throws Exception {
		DailyImporter importer = new DailyImporter();
		importer.importFile();
	}
}
