package edu.uams.clara.webapp.protocol.domain.thing;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name="click_grant")
public class Grant extends AbstractDomainEntity{

	private static final long serialVersionUID = 7505844639417549812L;
	
	@Column(name="prn", length=255)
	private String prn;

	@Column(name="pi_id", length=255)
	private String piId;
	
	@Column(name="pi_name", length=255)
	private String piName;
	
	@Column(name="grant_title", length=255)
	private String grantTitle;
	
	@Column(name="funding_agency", length=255)
	private String fundingAgency;
	
	@Column(name="Status", length=255)
	private String status;
	
	@Column(name="created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	public String getPrn() {
		return prn;
	}

	public void setPrn(String prn) {
		this.prn = prn;
	}


	public String getPiName() {
		return piName;
	}

	public void setPiName(String piName) {
		this.piName = piName;
	}

	public String getGrantTitle() {
		return grantTitle;
	}

	public void setGrantTitle(String grantTitle) {
		this.grantTitle = grantTitle;
	}

	public String getFundingAgency() {
		return fundingAgency;
	}

	public void setFundingAgency(String fundingAgency) {
		this.fundingAgency = fundingAgency;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getPiId() {
		return piId;
	}

	public void setPiId(String piId) {
		this.piId = piId;
	}
}
