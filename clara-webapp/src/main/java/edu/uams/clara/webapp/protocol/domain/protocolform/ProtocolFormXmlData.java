package edu.uams.clara.webapp.protocol.domain.protocolform;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;

@Entity
@Table(name = "protocol_form_xml_data")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ProtocolFormXmlData extends AbstractDomainEntity  {

	private static final long serialVersionUID = -9088959745533447271L;

	@ManyToOne
	@JoinColumn(name="protocol_form_id")
	private ProtocolForm protocolForm;

	@ManyToOne
	@JoinColumn(name="parent_id")
	private ProtocolFormXmlData parent;

	@Column(name="protocol_form_xml_data_type")
	@Enumerated(EnumType.STRING)
	private ProtocolFormXmlDataType protocolFormXmlDataType;

	@Column(name="xml_data")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String xmlData;

	@Column(name="created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	public void setXmlData(String xmlData) {
		this.xmlData = xmlData;
	}

	public String getXmlData() {
		return xmlData;
	}


	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getCreated() {
		return created;
	}

	public void setParent(ProtocolFormXmlData parent) {
		this.parent = parent;
	}

	public ProtocolFormXmlData getParent() {
		return parent;
	}

	public void setProtocolForm(ProtocolForm protocolForm) {
		this.protocolForm = protocolForm;
	}

	public ProtocolForm getProtocolForm() {
		return protocolForm;
	}

	public void setProtocolFormXmlDataType(ProtocolFormXmlDataType protocolFormXmlDataType) {
		this.protocolFormXmlDataType = protocolFormXmlDataType;
	}

	public ProtocolFormXmlDataType getProtocolFormXmlDataType() {
		return protocolFormXmlDataType;
	}
}
