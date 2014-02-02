package edu.uams.clara.webapp.protocol.domain.thing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "site")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Site extends AbstractDomainEntity {

	private static final long serialVersionUID = -3096462473286558232L;

	@Column(name="nci_identifier", length=64)
	private String nciIdentifier;
	
	@Column(name="site_name", length=512, nullable=false)
	private String siteName;
	
	@Column(name="address", length=1024)
	private String address;
	
	@Column(name="city", length=128)
	private String city;
	
	@Column(name="state", length=128)
	private String state;
	
	@Column(name="zip", length=32)
	private
	String zip;
	
	@Column(name="common")
	private boolean common = false;
	
	// admin-only fields
	
	@Column(name="fwa_obtained")
	private boolean fwaObtained;
	
	@Column(name="fwa_number")
	private String fwaNumber;
	
	@Column(name="uams_irb_of_record")
	private boolean uamsIRBOfRecord;
	
	@Column(name="has_irb_auth_agreement")
	private boolean hasIRBAuthAggreement;
	
	@Column(name="irb_auth_agreement_nature")
	private String irbAuthAgreementNature;
	
	@Column(name="approved")
	private boolean approved = false;

	public void setNciIdentifier(String nciIdentifier) {
		this.nciIdentifier = nciIdentifier;
	}

	public String getNciIdentifier() {
		return nciIdentifier;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getState() {
		return state;
	}

	public void setCommon(boolean common) {
		this.common = common;
	}

	public boolean isCommon() {
		return common;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getZip() {
		return zip;
	}

	public void setFwaObtained(boolean fwaObtained) {
		this.fwaObtained = fwaObtained;
	}

	public boolean isFwaObtained() {
		return fwaObtained;
	}

	public void setFwaNumber(String fwaNumber) {
		this.fwaNumber = fwaNumber;
	}

	public String getFwaNumber() {
		return fwaNumber;
	}

	public void setUamsIRBOfRecord(boolean uamsIRBOfRecord) {
		this.uamsIRBOfRecord = uamsIRBOfRecord;
	}

	public boolean isUamsIRBOfRecord() {
		return uamsIRBOfRecord;
	}

	public void setHasIRBAuthAggreement(boolean hasIRBAuthAggreement) {
		this.hasIRBAuthAggreement = hasIRBAuthAggreement;
	}

	public boolean isHasIRBAuthAggreement() {
		return hasIRBAuthAggreement;
	}

	public void setIrbAuthAgreementNature(String irbAuthAgreementNature) {
		this.irbAuthAgreementNature = irbAuthAgreementNature;
	}

	public String getIrbAuthAgreementNature() {
		return irbAuthAgreementNature;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCity() {
		return city;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public boolean isApproved() {
		return approved;
	}

}
