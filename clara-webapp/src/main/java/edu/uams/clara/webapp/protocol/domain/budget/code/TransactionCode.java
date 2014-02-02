package edu.uams.clara.webapp.protocol.domain.budget.code;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import edu.uams.clara.core.domain.AbstractDomainEntity;

/*** 
 * Leave it for EPIC
 * @author bianjiang
 *
 */
@Entity
@Table(name = "transaction_code")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@JsonIgnoreProperties({"hospitalChargeProcedure"})
public class TransactionCode extends AbstractDomainEntity {

	private static final long serialVersionUID = 886673525248355518L;

	@ManyToOne
	@JoinColumn(name="hospital_charge_procedure_id")
	private HospitalChargeProcedure hospitalChargeProcedure;

	@ManyToOne
	@JoinColumn(name="hospital_location_code_id")
	private HospitalLocationCode hosptialLocationCode;
	
	@Column(name = "transaction_code", length=8)
	private String transactionCode;
	
	@Column(name = "description")
	private String description;

	public void setHospitalChargeProcedure(HospitalChargeProcedure hospitalChargeProcedure) {
		this.hospitalChargeProcedure = hospitalChargeProcedure;
	}

	public HospitalChargeProcedure getHospitalChargeProcedure() {
		return hospitalChargeProcedure;
	}

	public void setHosptialLocationCode(HospitalLocationCode hosptialLocationCode) {
		this.hosptialLocationCode = hosptialLocationCode;
	}

	public HospitalLocationCode getHosptialLocationCode() {
		return hosptialLocationCode;
	}

	public void setTransactionCode(String transactionCode) {
		this.transactionCode = transactionCode;
	}

	public String getTransactionCode() {
		return transactionCode;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
	
}
