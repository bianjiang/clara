package edu.uams.clara.integration.incoming.billingcodes.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "hospital_charge_update")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HospitalChargeUpdate extends AbstractDomainEntity {

	private static final long serialVersionUID = -3056385733555857358L;

	@Column(name = "cpt_code", length = 5)
	private String cptCode;

	@Column(name = "cost")
	private BigDecimal cost;
	
	@Column(name="description", length = 1000)
	private String description;
	
	@Column(name = "si")
	private String si;
	
	@Column(name = "apc")
	private String apc;

	public String getCptCode() {
		return cptCode;
	}

	public void setCptCode(String cptCode) {
		this.cptCode = cptCode;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSi() {
		return si;
	}

	public void setSi(String si) {
		this.si = si;
	}

	public String getApc() {
		return apc;
	}

	public void setApc(String apc) {
		this.apc = apc;
	}

}
