package edu.uams.clara.webapp.common.domain.history;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "track")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Track extends AbstractDomainEntity {

	private static final long serialVersionUID = -291020576225953519L;
	
	@Column(name="modified")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modified;
	
	@Column(name="type", nullable=true)
	private String type;
	
	@Column(name="xml_data")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String xmlData;
	
	@Column(name="ref_object_class")
	private Class<?> refObjectClass;
	
	@Column(name="ref_object_id")
	private long refObjectId;

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getXmlData() {
		return xmlData;
	}

	public void setXmlData(String xmlData) {
		this.xmlData = xmlData;
	}

	public Class<?> getRefObjectClass() {
		return refObjectClass;
	}

	public void setRefObjectClass(Class<?> refObjectClass) {
		this.refObjectClass = refObjectClass;
	}

	public long getRefObjectId() {
		return refObjectId;
	}

	public void setRefObjectId(long refObjectId) {
		this.refObjectId = refObjectId;
	}
}
