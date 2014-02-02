package edu.uams.clara.webapp.protocol.domain.budget.code;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "cpt_code_mapping")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CPTCodeMapping extends AbstractDomainEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6077385430851486000L;

	@Column(name = "tm_id", length = 20)
	private String tmID;

	@Column(name = "tm_description", length = 1000)
	private String tmDescription;

	@Column(name = "cpt_code", length = 5)
	private String cptCode;

	@Column(name = "other_code", length = 10)
	private String otherCode;


	public String getCptCode() {
		return cptCode;
	}

	public void setCptCode(String cptCode) {
		this.cptCode = cptCode;
	}


	public String getTmDescription() {
		return tmDescription;
	}

	public void setTmDescription(String tmDescription) {
		this.tmDescription = tmDescription;
	}

	public String getOtherCode() {
		return otherCode;
	}

	public void setOtherCode(String otherCode) {
		this.otherCode = otherCode;
	}

	public String getTmID() {
		return tmID;
	}

	public void setTmID(String tmID) {
		this.tmID = tmID;
	}

}
