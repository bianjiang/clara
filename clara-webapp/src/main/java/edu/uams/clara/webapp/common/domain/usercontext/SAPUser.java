package edu.uams.clara.webapp.common.domain.usercontext;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "sap_user")@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)

public class SAPUser extends AbstractDomainEntity implements Serializable {

	private static final long serialVersionUID = 7166872036676879539L;

	@Column(name="sap", nullable=false)
	private String sap;
	
	@Column(name="ssn", nullable=false)
	private String ssn;
	
	@Column(name="colledge_org_no", nullable=true)
	private String colledgeOrgNo;
	
	@Column(name="department_org_no", nullable=true)
	private String departmentOrgNo;
	
	@Column(name="position_no", nullable=true)
	private String positionNo;
	
	@Column(name="birth_date", nullable=true)
	private String birthDate;

	public String getSap() {
		return sap;
	}

	public void setSap(String sap) {
		this.sap = sap;
	}

	public String getSsn() {
		return ssn;
	}

	public void setSsn(String ssn) {
		this.ssn = ssn;
	}

	public String getColledgeOrgNo() {
		return colledgeOrgNo;
	}

	public void setColledgeOrgNo(String colledgeOrgNo) {
		this.colledgeOrgNo = colledgeOrgNo;
	}

	public String getPositionNo() {
		return positionNo;
	}

	public void setPositionNo(String positionNo) {
		this.positionNo = positionNo;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public String getDepartmentOrgNo() {
		return departmentOrgNo;
	}

	public void setDepartmentOrgNo(String departmentOrgNo) {
		this.departmentOrgNo = departmentOrgNo;
	}
	
	
}
