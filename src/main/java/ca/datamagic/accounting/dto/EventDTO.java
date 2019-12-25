/**
 * 
 */
package ca.datamagic.accounting.dto;

/**
 * @author Greg
 *
 */
public class EventDTO {
	private String eventName = null;
	private Long count = null;
	
	public String getEventName() {
		return this.eventName;
	}
	
	public void setEventName(String newVal) {
		this.eventName = newVal;
	}
	
	public Long getCount() {
		return this.count;
	}
	
	public void setCount(Long newVal) {
		this.count = newVal;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.eventName);
		builder.append(":");
		builder.append(this.count);
		return builder.toString();
	}
}
