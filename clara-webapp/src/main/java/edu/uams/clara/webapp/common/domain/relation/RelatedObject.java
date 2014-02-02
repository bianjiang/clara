package edu.uams.clara.webapp.common.domain.relation;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "related_object")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class RelatedObject extends AbstractDomainEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1613310954744578809L;
	
	@Column(name="created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@Column(name="object_id")
	private long objectId;
	
	@Column(name="object_type")
	private String objectType;

	@Column(name="related_object_id")
	private long relatedObjectId;
	
	@Column(name="related_object_type")
	private String relatedObjectType;

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public long getObjectId() {
		return objectId;
	}

	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public long getRelatedObjectId() {
		return relatedObjectId;
	}

	public void setRelatedObjectId(long relatedObjectId) {
		this.relatedObjectId = relatedObjectId;
	}

	public String getRelatedObjectType() {
		return relatedObjectType;
	}

	public void setRelatedObjectType(String relatedObjectType) {
		this.relatedObjectType = relatedObjectType;
	}

}
