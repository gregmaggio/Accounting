/**
 * 
 */
package ca.datamagic.accounting.dto;

import java.util.List;

/**
 * @author Greg
 *
 */
public class EventTrendDTO {
	private String eventName = null;
	private String eventMessage = null;
	private List<EventTrendSeriesDTO> series = null;
	
	public String getEventName() {
		return this.eventName;
	}
	
	public String getEventMessage() {
		return this.eventMessage;
	}
	
	public List<EventTrendSeriesDTO> getSeries() {
		return this.series;
	}
	
	public void setEventName(String newVal) {
		this.eventName = newVal;
	}
	
	public void setEventMessage(String newVal) {
		this.eventMessage = newVal;
	}
	
	public void setSeries(List<EventTrendSeriesDTO> newVal) {
		this.series = newVal;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.eventName);
		builder.append(":");
		builder.append(this.eventMessage);
		if (this.series != null) {
			builder.append(":");
			builder.append("[");
			for (int ii = 0; ii < this.series.size(); ii++) {
				if (ii > 0) {
					builder.append(",");
				}
				builder.append(this.series.get(ii).toString());
			}
			builder.append("]");
		}
		return builder.toString();
	}
}
