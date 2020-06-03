/**
 * 
 */
package ca.datamagic.accounting.dto;

/**
 * @author Greg
 *
 */
public class EventNameDTO {
	private String eventName = null;
	private String eventMessage = null;
	
	public String getEventName() {
		return this.eventName;
	}
	
	public String getEventMessage() {
		return this.eventMessage;
	}
	
	public void setEventName(String newVal) {
		this.eventName = newVal;
	}
	
	public void setEventMessage(String newVal) {
		this.eventMessage = newVal;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.eventName);
		builder.append(":");
		builder.append(this.eventMessage);
		return builder.toString();
	}
}
