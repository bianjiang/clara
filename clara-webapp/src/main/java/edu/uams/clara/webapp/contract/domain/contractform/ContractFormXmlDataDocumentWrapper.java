package edu.uams.clara.webapp.contract.domain.contractform;

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

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocument.Status;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormType;


@Entity
@Table(name="contract_form_xml_data_document")
public class ContractFormXmlDataDocumentWrapper extends AbstractDomainEntity {


	/**
	 * 
	 */
	private static final long serialVersionUID = -2675693309753120204L;


	@Column(name = "contract_form_id")
	private long contractFormId;
	
	@Column(name = "parent_contract_form_id")
	private long parentContractFormId;

	@Column(name="user_id")
	private long userId;
	
	@Column(name="committee", nullable=true)
	@Enumerated(EnumType.STRING)
	private Committee committee;	
	
	/**
	 * UniDirectional 
	 */
	@ManyToOne
	@JoinColumn(name="uploaded_file_id")
	private UploadedFile uploadedFile;	
	
	@Column(name="parent_id")
	private long parentContractFormXmlDataDocumentId;
	
	@Column(name="category")
	private String category;
	
	@Column(name="title")
	private String title;

	@Column(name="created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@Column(name="version_id")
	private long versionId;
	
	@Column(name="status")
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Column(name="contract_form_type")
	@Enumerated(EnumType.STRING)
	private ContractFormType contractFormType;

	@Column(name="contract_form_xml_data_id")
	private long contractFormXmlDataId;
	
	@Transient
	@JsonProperty("contractFormTypeDesc")
	public String getContractFormTypeDescription(){
		if(this.contractFormType == null){
			return null;
		}
		return this.contractFormType.getDescription();
	}
	
	@Transient
	@JsonProperty("createdDate")
	public String getCreatedDate(){
		if(this.getCreated() == null){
			return "";
		}
		return DateFormatUtil.formateDate(this.getCreated());
	}
	
	public long getContractFormId() {
		return contractFormId;
	}

	public void setContractFormId(long contractFormId) {
		this.contractFormId = contractFormId;
	}

	public Committee getCommittee() {
		return committee;
	}

	public void setCommittee(Committee committee) {
		this.committee = committee;
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public long getParentContractFormXmlDataDocumentId() {
		return parentContractFormXmlDataDocumentId;
	}

	public void setParentContractFormXmlDataDocumentId(
			long parentContractFormXmlDataDocumentId) {
		this.parentContractFormXmlDataDocumentId = parentContractFormXmlDataDocumentId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getVersionId() {
		return versionId;
	}

	public void setVersionId(long versionId) {
		this.versionId = versionId;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}



	public ContractFormType getContractFormType() {
		return contractFormType;
	}



	public void setContractFormType(ContractFormType contractFormType) {
		this.contractFormType = contractFormType;
	}


	public long getContractFormXmlDataId() {
		return contractFormXmlDataId;
	}


	public void setContractFormXmlDataId(long contractFormXmlDataId) {
		this.contractFormXmlDataId = contractFormXmlDataId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public long getParentContractFormId() {
		return parentContractFormId;
	}

	public void setParentContractFormId(long parentContractFormId) {
		this.parentContractFormId = parentContractFormId;
	}	
	
}
