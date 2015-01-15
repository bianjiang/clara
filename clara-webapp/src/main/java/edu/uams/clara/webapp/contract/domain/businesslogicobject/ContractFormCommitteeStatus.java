package edu.uams.clara.webapp.contract.domain.businesslogicobject;

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
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormCommitteeStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;

@Entity
@Table(name = "contract_form_committee_status")
@JsonIgnoreProperties({"contractForm"})
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ContractFormCommitteeStatus extends AbstractDomainEntity {


	private static final long serialVersionUID = 6418553368473600401L;

	@ManyToOne
	@JoinColumn(name="contract_form_id")
	private ContractForm contractForm;
	
	@Column(name="committee")
	@Enumerated(EnumType.STRING)
	private Committee committee;	
	
	@Column(name="contract_form_committee_status")
	@Enumerated(EnumType.STRING)
	private ContractFormCommitteeStatusEnum contractFormCommitteeStatus;	
	
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
	
	@Column(name="caused_by_user_id", nullable=true)
	private long causedByUserId;
	
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
	@JsonProperty("contractFormId")
	public long getContractFormId(){
		if(this.contractForm == null){
			return 0;
		}
		
		return this.contractForm.getId();
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

	public void setContractForm(ContractForm contractForm) {
		this.contractForm = contractForm;
	}

	public ContractForm getContractForm() {
		return contractForm;
	}

	public void setContractFormCommitteeStatus(
			ContractFormCommitteeStatusEnum contractFormCommitteeStatus) {
		this.contractFormCommitteeStatus = contractFormCommitteeStatus;
	}

	public ContractFormCommitteeStatusEnum getContractFormCommitteeStatus() {
		return contractFormCommitteeStatus;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getNote() {
		return note;
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

	public long getCausedByUserId() {
		return causedByUserId;
	}

	public void setCausedByUserId(long causedByUserId) {
		this.causedByUserId = causedByUserId;
	}
}
