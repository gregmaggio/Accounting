/**
 * 
 */
package ca.datamagic.accounting.dto;

/**
 * @author Greg
 *
 */
public class EventTrendSeriesDTO {
	private Integer year = null;
	private Integer month = null;
	private Integer day = null;
	private Long count = null;
	
	public Integer getYear() {
		return this.year;
	}
	
	public Integer getMonth() {
		return this.month;
	}
	
	public Integer getDay() {
		return this.day;
	}
	
	public Long getCount() {
		return this.count;
	}
	
	public void setYear(Integer newVal) {
		this.year = newVal;
	}
	
	public void setMonth(Integer newVal) {
		this.month = newVal;
	}
	
	public void setDay(Integer newVal) {
		this.day = newVal;
	}
	
	public void setCount(Long newVal) {
		this.count = newVal;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.year);
		builder.append(":");
		builder.append(this.month);
		builder.append(":");
		builder.append(this.day);
		builder.append(":");
		builder.append(this.count);
		return builder.toString();
	}
}
