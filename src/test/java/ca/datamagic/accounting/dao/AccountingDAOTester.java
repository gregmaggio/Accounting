/**
 * 
 */
package ca.datamagic.accounting.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import ca.datamagic.accounting.dto.AccountingParameters;
import ca.datamagic.accounting.testing.BaseTester;

/**
 * @author Greg
 *
 */
public class AccountingDAOTester extends BaseTester {
	private static final Logger logger = LogManager.getLogger(AccountingDAOTester.class);
	
	@Test
	public void logTest() throws Exception {
		logger.debug("logTest");
		AccountingParameters parameters = new AccountingParameters();
		parameters.setDeviceLatitude(39.0045296);
		parameters.setDeviceLongitude(-76.9066717);
		parameters.setEventName("Test");
		parameters.setEventMessage("Test");
		AccountingDAO dao = new AccountingDAO();
		dao.log(parameters);
	}
}
