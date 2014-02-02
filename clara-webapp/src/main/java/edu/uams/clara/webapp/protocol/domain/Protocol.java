package edu.uams.clara.webapp.protocol.domain;

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
@Table(name = "protocol")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Protocol extends AbstractDomainEntity {
	
	private static final long serialVersionUID = 8573401515219761352L;
	
	@Column(name="protocol_identifier")
	private String protocolIdentifier;
	
	@Column(name="created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@Column(name="locked")
	private boolean locked;
	
	/**
	 * protocol meta data (XML format), and any searchable field on the protcol will be here as well...
	 */
	@Column(name="meta_data_xml")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String metaDataXml;

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getCreated() {
		return created;
	}

	public void setLocked(boolean locked)  {
		this.locked = locked;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setMetaDataXml(String metaDataXml) {
		this.metaDataXml = metaDataXml;
	}

	public String getMetaDataXml() {
		return metaDataXml;
	}

	public String getProtocolIdentifier() {
		return protocolIdentifier;
	}

	public void setProtocolIdentifier(String protocolIdentifier) {
		this.protocolIdentifier = protocolIdentifier;
	}

	
}
