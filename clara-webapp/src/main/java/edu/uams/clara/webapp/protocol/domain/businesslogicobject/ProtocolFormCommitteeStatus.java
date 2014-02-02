package edu.uams.clara.webapp.protocol.domain.businesslogicobject;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;



import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormCommitteeStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;

@Entity
@Table(name = "protocol_form_committee_status")
@JsonIgnoreProperties({"protocolForm"})
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ProtocolFormCommitteeStatus extends AbstractDomainEntity {

	private static final long serialVersionUID = 1423001015734658881L;

	@ManyToOne
	@JoinColumn(name="protocol_form_id")
	private ProtocolForm protocolForm;
	
	@Column(name="committee")
	@Enumerated(EnumType.STRING)
	private Committee committee;	
	
	@Column(name="protocol_form_committee_status")
	@Enumerated(EnumType.STRING)
	private ProtocolFormCommitteeStatusEnum protocolFormCommitteeStatus;	
	
	@Column(name="modified")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modified;
	
	@Column(name="xml_data",nullable=true)
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String xmlData;
	
	@Column(name="note", nullable=true)
	private String note;
	
	@Column(name="action", nullable=true)
	private String action;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="caused_by_user_id", nullable=true)
	private User causeByUser;
	
	@Column(name="cause_by_committee", nullable=true)
	@Enumerated(EnumType.STRING)
	private Committee causedByCommittee;
	
	@Transient
	@JsonProperty("committeeDescription")
	public String getCommitteeDescription(){
		if(this.committee == null){
			return "";
		}
		
		return this.committee.getDescription();
	}

	@Transient
	@JsonProperty("protocolFormId")
	public long getProtocolFormId(){
		if(this.protocolForm == null){
			return 0;
		}
		
		return this.protocolForm.getId();
	}
	
	@Transient
	@JsonProperty("modifiedDateTime")
	public String getModifiedDateTime(){
		if(this.modified == null){
			return "";
		}
		
		return DateFormatUtil.formateDate(this.modified);
	}
	
	public void setModified(Date modified) {
		this.modified = modified;
	}

	public Date getModified() {
		return modified;
	}

	public void setCommittee(Committee committee) {
		this.committee = committee;
	}

	public Committee getCommittee() {
		return committee;
	}

	public void setProtocolForm(ProtocolForm protocolForm) {
		this.protocolForm = protocolForm;
	}

	public ProtocolForm getProtocolForm() {
		return protocolForm;
	}

	public void setProtocolFormCommitteeStatus(
			ProtocolFormCommitteeStatusEnum protocolFormCommitteeStatus) {
		this.protocolFormCommitteeStatus = protocolFormCommitteeStatus;
	}

	public ProtocolFormCommitteeStatusEnum getProtocolFormCommitteeStatus() {
		return protocolFormCommitteeStatus;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getNote() {
		return note;
	}

	public void setCauseByUser(User causeByUser) {
		this.causeByUser = causeByUser;
	}

	public User getCauseByUser() {
		return causeByUser;
	}

	public void setCausedByCommittee(Committee causedByCommittee) {
		this.causedByCommittee = causedByCommittee;
	}

	public Committee getCausedByCommittee() {
		return causedByCommittee;
	}

	public String getXmlData() {
		return xmlData;
	}

	public void setXmlData(String xmlData) {
		this.xmlData = xmlData;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
