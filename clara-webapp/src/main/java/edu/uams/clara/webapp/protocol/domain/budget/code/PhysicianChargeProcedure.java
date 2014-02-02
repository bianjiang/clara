package edu.uams.clara.webapp.protocol.domain.budget.code;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "physician_charge_procedure")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class PhysicianChargeProcedure extends AbstractDomainEntity {
	
	private static final long serialVersionUID = -6581598959661737346L;

	@Column(name="cpt_code", length=10)
	private String cptCode;
	
	@Column(name="tm_code")
	private String tmCode;
	
	@Column(name="description", length=1000)
	private String description;
	
	@Column(name="cost")
	private BigDecimal cost;
	
	@ManyToOne
	@JoinColumn(name="location_code_id")
	private PhysicianLocationCode locationCode;
	
	@Column(name="effective_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date effectiveDate;
	
	@Column(name="retired_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date retiredDate;
	
	@Column(name="is_physician_only")
	private boolean isPhysicianOnly;

	public void setCptCode(String cptCode) {
		this.cptCode = cptCode;
	}

	public String getCptCode() {
		return cptCode;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public void setPhysicianOnly(boolean isPhysicianOnly) {
		this.isPhysicianOnly = isPhysicianOnly;
	}

	public boolean isPhysicianOnly() {
		return isPhysicianOnly;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setRetiredDate(Date retiredDate) {
		this.retiredDate = retiredDate;
	}

	public Date getRetiredDate() {
		return retiredDate;
	}

	public void setLocationCode(PhysicianLocationCode locationCode) {
		this.locationCode = locationCode;
	}

	public PhysicianLocationCode getLocationCode() {
		return locationCode;
	}

	public String getTmCode() {
		return tmCode;
	}

	public void setTmCode(String tmCode) {
		this.tmCode = tmCode;
	}


}
