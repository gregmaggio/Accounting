/**
 * 
 */
package ca.datamagic.accounting.dto;

/**
 * @author Greg
 *
 */
public class AccountingParameters {
	private Double deviceLatitude = null;
	private Double deviceLongitude = null;
	private String eventName = null;
	private String eventMessage = null;
	
	public Double getDeviceLatitude() {
		return this.deviceLatitude;
	}
	
	public Double getDeviceLongitude() {
		return this.deviceLongitude;
	}
	
	public String getEventName() {
		return this.eventName;
	}
	
	public String getEventMessage() {
		return this.eventMessage;
	}
	
	public void setDeviceLatitude(Double newVal) {
		this.deviceLatitude = newVal;
	}
	
	public void setDeviceLongitude(Double newVal) {
		this.deviceLongitude = newVal;
	}
	
	public void setEventName(String newVal) {
		this.eventName = newVal;
	}
	
	public void setEventMessage(String newVal) {
		this.eventMessage = newVal;
	}
}
