package edu.uams.clara.webapp.contract.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.protocol.domain.Protocol;
		
@Entity
@Table(name = "contract")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Contract extends AbstractDomainEntity {
	
	private static final long serialVersionUID = 8573401515219761753L;
	
	@Column(name="contract_identifier")
	private String contractIdentifier;

	@Column(name="created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@Column(name="locked")
	private boolean locked;
	
	/**
	 * contract meta data (XML format), and any searchable field on the contract will be here as well...
	 */
	@Column(name="meta_data_xml")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String metaDataXml;
	
	@ManyToOne
	@JoinColumn(name="protocol_id")
	private Protocol protocol;

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
	
	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public String getContractIdentifier() {
		return contractIdentifier;
	}

	public void setContractIdentifier(String contractIdentifier) {
		this.contractIdentifier = contractIdentifier;
	}
}
