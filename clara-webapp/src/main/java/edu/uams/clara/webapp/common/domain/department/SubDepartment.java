package edu.uams.clara.webapp.common.domain.department;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "sub_department")
@JsonIgnoreProperties({"department"})
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SubDepartment extends AbstractDomainEntity implements Serializable {

	private static final long serialVersionUID = 1932264227814142297L;

	@ManyToOne
	@JoinColumn(name="department_id")
	private Department department;
	
	@Column(name="sap_code")
	private String sapCode;
	
	@Column(name="name")
	private String name;

	public void setSapCode(String sapCode) {
		this.sapCode = sapCode;
	}

	public String getSapCode() {
		return sapCode;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Department getDepartment() {
		return department;
	}
	
	@Transient
	@JsonProperty("departmentId")
	public long getDepartmentId(){
		if(this.getDepartment() == null){
			return 0;
		}
		return this.getDepartment().getId();
	}
}
