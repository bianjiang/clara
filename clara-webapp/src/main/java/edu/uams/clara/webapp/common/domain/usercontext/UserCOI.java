package edu.uams.clara.webapp.common.domain.usercontext;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;


@Entity
@Table(name = "user_coi")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserCOI extends AbstractDomainEntity implements Serializable{

	private static final long serialVersionUID = 1592126905737437788L;

	@Column(name="sap_id")
	private String sapId;
	
	@Column(name="first_name")
	private String firstName;
	
	@Column(name="last_name")
	private String lastName;
	
	@Column(name="disclosure_name")
	private String disclosureName;
	
	@Column(name="disclosure_status")
	private String disclosureStatus;
	
	
	@Column(name="disclosure_date_last_submitted")
	@Temporal(TemporalType.TIMESTAMP)
	private Date discDateLastSubmitted;
	
	@Column(name="expiration_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date expirationDate;

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

	public String getDisclosureName() {
		return disclosureName;
	}

	public void setDisclosureName(String disclosureName) {
		this.disclosureName = disclosureName;
	}

	public Date getDiscDateLastSubmitted() {
		return discDateLastSubmitted;
	}

	public void setDiscDateLastSubmitted(Date discDateLastSubmitted) {
		this.discDateLastSubmitted = discDateLastSubmitted;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public String getDisclosureStates() {
		return disclosureStatus;
	}

	public void setDisclosureStates(String disclosureStatus) {
		this.disclosureStatus = disclosureStatus;
	}

	

	public String getSapId() {
		return sapId;
	}

	public void setSapId(String sapId) {
		this.sapId = sapId;
	}
	
	
}
