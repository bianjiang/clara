package edu.uams.clara.webapp.protocol.domain.budget.code;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "hospital_charge_procedure")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class HospitalChargeProcedure extends AbstractDomainEntity {
	
	private static final long serialVersionUID = 5875283177045472495L;

	@OneToMany(mappedBy = "hospitalChargeProcedure", fetch = FetchType.EAGER)
	private List<TransactionCode> transactionCodes;
	
	@Column(name="cpt_code", length = 5)
	private String cptCode;
	
	@Column(name="description", length = 1000)
	private String description;
	
	@Column(name="cost")
	private BigDecimal cost;
	
	@Column(name = "si")
	private String si;
	
	@Column(name = "apc")
	private String apc;

	@Column(name="effective_date", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date effectiveDate;
	
	@Column(name="retired_date", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date retiredDate;
	
	@Column(name="is_hospital_only")
	private boolean isHospitalOnly;
	
	@Column(name="is_overwritten")
	private boolean isOverwritten;

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setCptCode(String cptCode) {
		this.cptCode = cptCode;
	}

	public String getCptCode() {
		return cptCode;
	}

	public void setHospitalOnly(boolean isHospitalOnly) {
		this.isHospitalOnly = isHospitalOnly;
	}

	public boolean isHospitalOnly() {
		return isHospitalOnly;
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

	public void setTransactionCodes(List<TransactionCode> transactionCodes) {
		this.transactionCodes = transactionCodes;
	}

	public List<TransactionCode> getTransactionCodes() {
		return transactionCodes;
	}

	public boolean isOverwritten() {
		return isOverwritten;
	}

	public void setOverwritten(boolean isOverwritten) {
		this.isOverwritten = isOverwritten;
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
