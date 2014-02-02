package edu.uams.clara.webapp.common.domain.department;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "college")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonIgnoreProperties({"departments"})
public class College extends AbstractDomainEntity implements Serializable {

	private static final long serialVersionUID = 7595710015104468133L;
		
	@OneToMany(mappedBy="college", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@OrderBy("name ASC")
	private Set<Department> departments = new TreeSet<Department>();
	
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

	public void setDepartments(Set<Department> departments) {
		this.departments = departments;
	}

	public Set<Department> getDepartments() {
		Set<Department> depts = new HashSet<Department>();
		for(Department dep:departments){
			if (!dep.isRetired()){
				depts.add(dep);
			}
		}
		return depts;
	}

	
}
