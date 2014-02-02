package edu.uams.clara.webapp.protocol.domain.protocolform;

import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
		
@Entity
@Table(name = "protocol_form")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ProtocolForm extends AbstractDomainEntity implements Form {

	private static final long serialVersionUID = 2630067324465063853L;
	
	@ManyToOne
	@JoinColumn(name="protocol_id")
	private Protocol protocol;
	
	@OneToMany(mappedBy="protocolForm", fetch=FetchType.EAGER)
	@MapKey(name="protocolFormXmlDataType")
	private Map<ProtocolFormXmlDataType, ProtocolFormXmlData> typedProtocolFormXmlDatas;
	
	@Column(name="meta_data_xml")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String metaDataXml;
	/**
	 * UniDirectional 
	 */
	@ManyToOne
	@JoinColumn(name="parent_id")
	private ProtocolForm parent;	
	
	@Column(name="protocol_form_type")
	@Enumerated(EnumType.STRING)
	private ProtocolFormType protocolFormType;
	
	@Column(name="created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@Column(name="locked")
	private boolean locked;

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getCreated() {
		return created;
	}

	public void setParent(ProtocolForm parent) {
		this.parent = parent;
	}

	public ProtocolForm getParent() {
		return parent;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setProtocolFormType(ProtocolFormType protocolFormType) {
		this.protocolFormType = protocolFormType;
	}

	public ProtocolFormType getProtocolFormType() {
		return protocolFormType;
	}

	public void setTypedProtocolFormXmlDatas(
			Map<ProtocolFormXmlDataType, ProtocolFormXmlData> typedProtocolFormXmlDatas) {
		this.typedProtocolFormXmlDatas = typedProtocolFormXmlDatas;
	}

	public Map<ProtocolFormXmlDataType, ProtocolFormXmlData> getTypedProtocolFormXmlDatas() {
		return typedProtocolFormXmlDatas;
	}

	public String getMetaDataXml() {
		return metaDataXml;
	}

	public void setMetaDataXml(String metaDataXml) {
		this.metaDataXml = metaDataXml;
	}

	@Override
	public String getFormType() {

		return this.getProtocolFormType().toString();
	}

	@Override
	public long getFormId() {
		
		return this.getId();
	}

	@Override
	public String getMetaXml() {
		// TODO Auto-generated method stub
		return this.getMetaDataXml();
	}

	@Override
	public String getIdentifier() {
		return this.getProtocol().getProtocolIdentifier();
	}

	@Override
	public long getParentFormId() {
		// TODO Auto-generated method stub
		return this.getParent().getId();
	}

	@Override
	public String getObjectMetaData() {
		// TODO Auto-generated method stub
		return this.getProtocol().getMetaDataXml();
	}

}
