package edu.uams.clara.webapp.common.domain.security;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.usercontext.User;

@Entity
@Table(name = "mutex_lock")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class MutexLock extends AbstractDomainEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5374877855271632529L;
	
	@Column(name="object_id")
	private long objectId;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_id")
	private User user;
	
	@Column(name="locked")
	private boolean locked;
	
	@Column(name="object_class")
	private Class<?> objectClass;
	
	@Column(name="modified")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modified;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public Class<?> getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(Class<?> objectClass) {
		this.objectClass = objectClass;
	}


	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public long getObjectId() {
		return objectId;
	}

	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}

}
