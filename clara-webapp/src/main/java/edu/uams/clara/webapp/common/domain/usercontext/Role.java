package edu.uams.clara.webapp.common.domain.usercontext;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;

@Entity
@Table(name = "role")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Role extends AbstractDomainEntity implements Serializable {

	private static final long serialVersionUID = -5915045476169212071L;
	
	public enum DepartmentLevel{
		SUB_DEPARTMENT, DEPARTMENT, COLLEGE;
	};
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "display_name")
	private String displayName;
	
	@Column(name = "role_permission_identifier")
	@Enumerated(EnumType.STRING)
	private Permission rolePermissionIdentifier;
	
	@Column(name = "committee", nullable=true)
	@Enumerated(EnumType.STRING)
	private Committee committee;
	
	@Column(name = "department_level", nullable=true)
	@Enumerated(EnumType.STRING)
	private DepartmentLevel departmentLevel;
	
	@Column(name = "is_irb_roster", nullable=true)
	private boolean isIRBRoster;
	
	@Column(name = "condition", nullable=true)
	private String condition;
	
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(
			name="role_default_permission",
			joinColumns=@JoinColumn(name="role_id"))
	@Column(name="permission")
	@Enumerated(EnumType.STRING)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private Set<Permission> defaultPermissions = new HashSet<Permission>(0);

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDefaultPermissions(Set<Permission> defaultPermissions) {
		this.defaultPermissions = defaultPermissions;
	}

	public Set<Permission> getDefaultPermissions() {
		return defaultPermissions;
	}

	public void setRolePermissionIdentifier(Permission rolePermissionIdentifier) {
		this.rolePermissionIdentifier = rolePermissionIdentifier;
	}

	public Permission getRolePermissionIdentifier() {
		return rolePermissionIdentifier;
	}

	public void setCommitee(Committee committee) {
		this.committee = committee;
	}

	public Committee getCommitee() {
		return committee;
	}

	public void setDepartmentLevel(DepartmentLevel departmentLevel) {
		this.departmentLevel = departmentLevel;
	}

	public DepartmentLevel getDepartmentLevel() {
		return departmentLevel;
	}

	public void setIRBRoster(boolean isIRBRoster) {
		this.isIRBRoster = isIRBRoster;
	}

	public boolean isIRBRoster() {
		return isIRBRoster;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}
}
