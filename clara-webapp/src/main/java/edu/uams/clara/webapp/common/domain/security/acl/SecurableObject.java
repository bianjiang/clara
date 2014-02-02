package edu.uams.clara.webapp.common.domain.security.acl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "securable_object")
@org.hibernate.annotations.Table(appliesTo = "securable_object", indexes = { @org.hibernate.annotations.Index(name="idx_object_class_id", columnNames = { "object_class", "object_id" } ) })
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class SecurableObject extends AbstractDomainEntity {

	private static final long serialVersionUID = 5015557705853281605L;	
	
	@Column(name="object_class")
	private Class<?> objectClass;
	
	@Column(name="object_id")
	private long objectId;
	
	@Column(name="use_object_id_expression")
	private boolean useObjectIdExpression;
	
	@Column(name="object_identification_expression")
	private String objectIdExpression;
	
	public void setObjectClass(Class<?> objectClass) {
		this.objectClass = objectClass;
	}

	public Class<?> getObjectClass() {
		return objectClass;
	}

	public void setObjectId(long objectId) {
		this.objectId = objectId;
	}

	public long getObjectId() {
		return objectId;
	}

	public void setObjectIdExpression(String objectIdExpression) {
		this.objectIdExpression = objectIdExpression;
	}

	public String getObjectIdExpression() {
		return objectIdExpression;
	}

	public void setUseObjectIdExpression(boolean useObjectIdExpression) {
		this.useObjectIdExpression = useObjectIdExpression;
	}

	public boolean isUseObjectIdExpression() {
		return useObjectIdExpression;
	}
	
}
