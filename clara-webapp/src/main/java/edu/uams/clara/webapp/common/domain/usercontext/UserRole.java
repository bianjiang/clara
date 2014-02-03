package edu.uams.clara.webapp.common.domain.usercontext;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.department.College;
import edu.uams.clara.webapp.common.domain.department.Department;
import edu.uams.clara.webapp.common.domain.department.SubDepartment;


@Entity
@Table(name = "user_role")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserRole extends AbstractDomainEntity implements Serializable {

	private static final long serialVersionUID = -3339439216297046134L;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="role_id", nullable = true)
	private Role role;

	@ManyToOne
	@JoinColumn(name="sub_department", nullable = true)
	private SubDepartment subDepartment;
	
	@ManyToOne
	@JoinColumn(name="department", nullable = true)
	private Department department;
	
	@ManyToOne
	@JoinColumn(name="college", nullable = true)
	private College college;
	
	@Column(name="is_delegate", nullable=false)
    private boolean isDelegate;
	
	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Role getRole() {
		return role;
	}

	public void setSubDepartment(SubDepartment subDepartment) {
		this.subDepartment = subDepartment;
	}

	public SubDepartment getSubDepartment() {
		return subDepartment;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Department getDepartment() {
		return department;
	}

	public void setCollege(College college) {
		this.college = college;
	}

	public College getCollege() {
		return college;
	}

	public boolean isDelegate() {
		return isDelegate;
	}

	public void setDelegate(boolean isDelegate) {
		this.isDelegate = isDelegate;
	}

}
