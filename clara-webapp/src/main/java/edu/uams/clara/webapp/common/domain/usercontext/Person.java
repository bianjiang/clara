package edu.uams.clara.webapp.common.domain.usercontext;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "person")
@JsonIgnoreProperties({ "user" })
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Person extends AbstractDomainEntity implements Serializable {

	private static final long serialVersionUID = 6400891024733895164L;

	@OneToOne(mappedBy = "person")
	private User user;

	@Column(name = "username", nullable = true)
	private String username;

	@Column(name = "sap", nullable = true)
	private String sap;

	@Column(name = "firstname")
	private String firstname;

	@Column(name = "lastname")
	private String lastname;

	@Column(name = "middlename")
	private String middlename;

	@Column(name = "email")
	private String email;

	@Column(name = "workphone")
	private String workphone;

	@Column(name = "department")
	private String department;

	@Column(name = "job_title")
	private String jobTitle;

	@Column(name = "street_address")
	private String streetAddress;

	@Column(name = "state")
	private String state;

	@Column(name = "zip_code")
	private String zipCode;

	@Column(name = "annual_salary")
	private String annualSalary;

	@Transient
	@JsonProperty("userId")
	public long getUserId() {
		if (this.user == null) {
			return 0;
		}
		return this.user.getId();
	}

	@Transient
	public String getFullname() {
		return lastname + ", " + firstname;
	}

	public void setSap(String sap) {
		this.sap = sap;
	}

	public String getSap() {
		return sap;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setMiddlename(String middlename) {
		this.middlename = middlename;
	}

	public String getMiddlename() {
		return middlename;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setWorkphone(String workphone) {
		this.workphone = workphone;
	}

	public String getWorkphone() {
		return workphone;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getDepartment() {
		return department;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getAnnualSalary() {
		return annualSalary;
	}

	public void setAnnualSalary(String annualSalary) {
		this.annualSalary = annualSalary;
	}

	@Override
	public int hashCode() {
		// realistically two person will be the same if they have the same getid or if they have the same sap number or the same username
		// this is impossible to implement, so NEVER put people directly into a HashSet
		return Objects.hash(this.getId(), this.getSap(), this.getUsername());
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (getClass() != other.getClass()) {
			return false;
		}
		final Person that = (Person) other;
		return Objects.equals(this.getId(), that.getId())
				|| Objects.equals(this.getSap(), that.getSap())
				|| Objects.equals(this.getUsername(), that.getUsername());
	}
}
