package uk.co.squadlist.web.model.forms;

import org.joda.time.LocalDateTime;

public class OutingDetails {

	private Integer year, month, day, hour, minute;
	private String ampm;
	private String squad;
	
	public LocalDateTime toLocalTime() {
		return new LocalDateTime(year, month, day, hour, minute);	// TODO AM PM
	}
	
	public Integer getYear() {
		return year;
	}






	public void setYear(Integer year) {
		this.year = year;
	}






	public Integer getMonth() {
		return month;
	}






	public void setMonth(Integer month) {
		this.month = month;
	}






	public Integer getDay() {
		return day;
	}






	public void setDay(Integer day) {
		this.day = day;
	}






	public Integer getHour() {
		return hour;
	}






	public void setHour(Integer hour) {
		this.hour = hour;
	}






	public Integer getMinute() {
		return minute;
	}






	public void setMinute(Integer minute) {
		this.minute = minute;
	}






	public String getAmpm() {
		return ampm;
	}






	public void setAmpm(String ampm) {
		this.ampm = ampm;
	}






	public String getSquad() {
		return squad;
	}
	public void setSquad(String squad) {
		this.squad = squad;
	}
	
	@Override
	public String toString() {
		return "OutingDetails [ampm=" + ampm + ", day=" + day + ", hour="
				+ hour + ", minute=" + minute + ", month=" + month + ", squad="
				+ squad + ", year=" + year + "]";
	}
	
}
