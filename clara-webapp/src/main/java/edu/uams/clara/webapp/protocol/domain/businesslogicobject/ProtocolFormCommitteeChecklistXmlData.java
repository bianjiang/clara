package edu.uams.clara.webapp.protocol.domain.businesslogicobject;

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
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;


@Entity
@Table(name = "protocol_form_committee_checklist_xml_data")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ProtocolFormCommitteeChecklistXmlData extends AbstractDomainEntity {

	private static final long serialVersionUID = -1059804501039974267L;
	
	@ManyToOne
	@JoinColumn(name="protocol_form_id")
	private ProtocolForm protocolForm;
	
	@Column(name="committee")
	@Enumerated(EnumType.STRING)
	private Committee committee;
	
	@ManyToOne
	@JoinColumn(name="parent_id")
	private ProtocolFormCommitteeChecklistXmlData parent;
	
	@Column(name="xml_data")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String xmlData;
	
	@Column(name="created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@Column(name="locked")
	private boolean locked;	

	public void setCommittee(Committee committee) {
		this.committee = committee;
	}

	public Committee getCommittee() {
		return committee;
	}

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

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setProtocolForm(ProtocolForm protocolForm) {
		this.protocolForm = protocolForm;
	}

	public ProtocolForm getProtocolForm() {
		return protocolForm;
	}

	public void setParent(ProtocolFormCommitteeChecklistXmlData parent) {
		this.parent = parent;
	}

	public ProtocolFormCommitteeChecklistXmlData getParent() {
		return parent;
	}

}
