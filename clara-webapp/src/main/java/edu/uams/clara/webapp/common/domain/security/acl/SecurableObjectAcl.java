package edu.uams.clara.webapp.common.domain.security.acl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.security.acl.enums.Permission;

@Entity
@Table(name = "securable_object_acl")
@org.hibernate.annotations.Table(appliesTo = "securable_object_acl", indexes = { @org.hibernate.annotations.Index(name="idx_owner_class_id", columnNames = { "owner_class", "owner_id"} ) })
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class SecurableObjectAcl extends AbstractDomainEntity {

	private static final long serialVersionUID = 7970904146045961357L;
	
	@Column(name="owner_class")
	private Class<?> ownerClass;
	
	@Column(name="owner_id")
	private long ownerId;
	
	@ManyToOne
	@JoinColumn(name="securable_object_id")
	private SecurableObject securableObject;	
	
	@Column(name="permission")
	@Enumerated(EnumType.STRING)
	private Permission permission;

	public void setPermission(Permission permission) {
		this.permission = permission;
	}

	public Permission getPermission() {
		return permission;
	}

	public void setSecurableObject(SecurableObject securableObject) {
		this.securableObject = securableObject;
	}

	public SecurableObject getSecurableObject() {
		return securableObject;
	}

	public void setOwnerClass(Class<?> ownerClass) {
		this.ownerClass = ownerClass;
	}

	public Class<?> getOwnerClass() {
		return ownerClass;
	}

	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}

	public long getOwnerId() {
		return ownerId;
	}

	

}
