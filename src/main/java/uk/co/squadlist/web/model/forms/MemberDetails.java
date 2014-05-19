package uk.co.squadlist.web.model.forms;

import java.util.List;

import org.hibernate.validator.constraints.NotBlank;

import uk.co.squadlist.web.model.Squad;

public class MemberDetails {

	@NotBlank
	private String firstName, lastName, emailAddress;
	private String contactNumber, rowingPoints, scullingPoints, sweepOarSide, registrationNumber, emergencyContactName, emergencyContactNumber;	
	private List<Squad> squads;
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}
	
	public String getRowingPoints() {
		return rowingPoints;
	}

	public void setRowingPoints(String rowingPoints) {
		this.rowingPoints = rowingPoints;
	}

	public String getScullingPoints() {
		return scullingPoints;
	}

	public void setScullingPoints(String scullingPoints) {
		this.scullingPoints = scullingPoints;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public List<Squad> getSquads() {
		return squads;
	}

	public void setSquads(List<Squad> squads) {
		this.squads = squads;
	}

	public String getSweepOarSide() {
		return sweepOarSide;
	}

	public void setSweepOarSide(String sweepOarSide) {
		this.sweepOarSide = sweepOarSide;
	}

	public String getEmergencyContactName() {
		return emergencyContactName;
	}

	public void setEmergencyContactName(String emergencyContactName) {
		this.emergencyContactName = emergencyContactName;
	}

	public String getEmergencyContactNumber() {
		return emergencyContactNumber;
	}

	public void setEmergencyContactNumber(String emergencyContactNumber) {
		this.emergencyContactNumber = emergencyContactNumber;
	}

	@Override
	public String toString() {
		return "MemberDetails [contactNumber=" + contactNumber
				+ ", emailAddress=" + emailAddress + ", emergencyContactName="
				+ emergencyContactName + ", emergencyContactNumber="
				+ emergencyContactNumber + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", registrationNumber="
				+ registrationNumber + ", rowingPoints=" + rowingPoints
				+ ", scullingPoints=" + scullingPoints + ", squads=" + squads
				+ ", sweepOarSide=" + sweepOarSide + "]";
	}
	
}
