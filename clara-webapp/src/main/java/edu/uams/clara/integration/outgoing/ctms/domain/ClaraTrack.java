package edu.uams.clara.integration.outgoing.ctms.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import edu.uams.clara.integration.common.domain.AbstractExternalDomainEntity;

@Entity
@Table(name = "clara_track", schema="dbo", catalog="crissql.[ctms_integration]")
public class ClaraTrack extends AbstractExternalDomainEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4586856217278066026L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", insertable = true, updatable = false, nullable = false, unique = true)
	private long id;
	
	@Column(name="clara_protocol_id")
	private long claraProtocolId;
	
	@Column(name="xml_data")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String xmlData;

	@Override
	protected long getInternalId() {
		return this.id;
	}

	@Override
	protected void setInternalId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public void setXmlData(String xmlData) {
		this.xmlData = xmlData;
	}

	public String getXmlData() {
		return xmlData;
	}

	public long getClaraProtocolId() {
		return claraProtocolId;
	}

	public void setClaraProtocolId(long claraProtocolId) {
		this.claraProtocolId = claraProtocolId;
	}

}
