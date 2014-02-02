package edu.uams.clara.webapp.common.domain.department;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "department")
@JsonIgnoreProperties({"college", "subDepartments"})
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Department extends AbstractDomainEntity implements Serializable {

	private static final long serialVersionUID = -2842006205594145542L;

	@ManyToOne
	@JoinColumn(name="college_id")
	private College college;
	
	@OneToMany(mappedBy="department", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@OrderBy("name ASC")
	private Set<SubDepartment> subDepartments = new TreeSet<SubDepartment>();
	
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

	public void setCollege(College college) {
		this.college = college;
	}

	public College getCollege() {
		return college;
	}
	
	public void setSubDepartments(Set<SubDepartment> subDepartments) {
		this.subDepartments = subDepartments;
	}

	public Set<SubDepartment> getSubDepartments() {
		return subDepartments;
	}

	@Transient
	@JsonProperty("collegeId")
	public long getCollegeId(){
		if(this.getCollege() == null){
			return 0;
		}
		return this.getCollege().getId();
	}

	
	
}
