package uk.co.squadlist.web.localisation;

import java.util.Date;
import java.util.List;

public interface GoverningBody {

	public String getName();
	public List<String> getPointsOptions();
	public String getRowingStatus(List<String> rowingPoints);
	public String getScullingStatus(List<String> scullingPoints);
	public int getEffectiveAge(Date dateOfBirth);
	public String getAgeGrade(int age);
	public String checkRegistrationNumber(String registrationNumber);
	public String getStatusPointsReference();
	public Integer getEffectiveAge(List<Date> datesOfBirth);
	
}
