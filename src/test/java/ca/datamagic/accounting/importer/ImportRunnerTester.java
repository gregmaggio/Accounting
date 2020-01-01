/**
 * 
 */
package ca.datamagic.accounting.importer;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Greg
 *
 */
public class ImportRunnerTester {

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
		ImportRunner runner = new ImportRunner("2019-07-03", "2019-07-31");
		runner.run();
	}

}
