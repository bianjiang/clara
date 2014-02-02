package edu.uams.clara.webapp.common.domain.usercontext;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;

/***
 * 
 * @author bianjiang
 *
 */
@Entity
@Table(name = "citi_member")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CitiMember extends AbstractDomainEntity implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3388168627508251824L;

	@Column(name="member_id_number")
	private String memberID;
	
	@Column(name="first_name")
	private String firstName;
	
	@Column(name="last_name")
	private String lastName;
	
	@Column(name="employee_number")
	private String employeeNumber;
	
	@Column(name="email_address")
	private String emailAddress;
	
	@Column(name="registration_date")
	private String registrationDate;
	
	@Column(name="name_curriculum")
	private String nameOfCurriculum;
	
	@Column(name="groups")
	private String group;
	
	@Column(name="stage_Number")
	private String stageNumber;
	
	@Column(name="stage_description")
	private String stageDescription;
	
	@Column(name="completion_report_number")
	private String completionReportNumber;
	
	@Column(name="date_completion_report_earned")
	private String dateCompletionEarned;
	
	@Column(name="learner_score")
	private String learnerScore;
	
	@Column(name="Passing_score")
	private String passingScore;
	
	@Column(name="date_completion_report_expires")
	private String dateCompletionExpires;
	
	@Column(name="groups_id_number")
	private String groupID;
	
	@Column(name="name", nullable=true)
	private String name;
	
	@Column(name="username", nullable=true)
	private String username;
	
	public String getMemberID() {
		return memberID;
	}

	public void setMemberID(String memberID) {
		this.memberID = memberID;
	}

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

	public String getEmployeeNumber() {
		return employeeNumber;
	}

	public void setEmployeeNumber(String employeeNumber) {
		this.employeeNumber = employeeNumber;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}

	public String getNameOfCurriculum() {
		return nameOfCurriculum;
	}

	public void setNameOfCurriculum(String nameOfCurriculum) {
		this.nameOfCurriculum = nameOfCurriculum;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getStageNumber() {
		return stageNumber;
	}

	public void setStageNumber(String stageNumber) {
		this.stageNumber = stageNumber;
	}

	public String getStageDescription() {
		return stageDescription;
	}

	public void setStageDescription(String stageDescription) {
		this.stageDescription = stageDescription;
	}

	public String getCompletionReportNumber() {
		return completionReportNumber;
	}

	public void setCompletionReportNumber(String completionReportNumber) {
		this.completionReportNumber = completionReportNumber;
	}

	public String getDateCompletionEarned() {
		return dateCompletionEarned;
	}

	public void setDateCompletionEarned(String dateCompletionEarned) {
		this.dateCompletionEarned = dateCompletionEarned;
	}

	public String getLearnerScore() {
		return learnerScore;
	}

	public void setLearnerScore(String learnerScore) {
		this.learnerScore = learnerScore;
	}

	public String getPassingScore() {
		return passingScore;
	}

	public void setPassingScore(String passingScore) {
		this.passingScore = passingScore;
	}

	public String getDateCompletionExpires() {
		return dateCompletionExpires;
	}

	public void setDateCompletionExpires(String dateCompletionExpires) {
		this.dateCompletionExpires = dateCompletionExpires;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
