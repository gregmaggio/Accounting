/**
 * 
 */
package ca.datamagic.accounting.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.datamagic.accounting.dto.AccountingParameters;

/**
 * @author Greg
 *
 */
public class AccountingDAO extends BaseDAO {
	private static final Logger accounting = LogManager.getLogger("accountingFile");
	
	public void log(AccountingParameters parameters) {
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
	}
}
