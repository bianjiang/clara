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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocument.Status;


@Entity
@Table(name = "contract_form_xml_data_document")
@JsonIgnoreProperties( { "user", "contractFormXmlData", "parent"})
public class ContractFormXmlDataDocument extends AbstractDomainEntity {

	private static final long serialVersionUID = -8824146284941316854L;
	
	public enum Status{
		APPROVED("Approved"),
		DRAFT("Draft");
		
		private String description;
		
		private Status(String description){
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}
	
	/**
	 * BiDirectional 
	 */
	@ManyToOne
	@JoinColumn(name="contract_form_xml_data_id")
	private ContractFormXmlData contractFormXmlData;
	
	/**
	 * UniDirectional 
	 */
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;
	
	@Column(name="committee", nullable=true)
	@Enumerated(EnumType.STRING)
	private Committee committee;	
	
	/**
	 * UniDirectional 
	 */
	@ManyToOne
	@JoinColumn(name="uploaded_file_id")
	private UploadedFile uploadedFile;
	
	/**
	 * UniDirectional 
	 */
	@ManyToOne
	@JoinColumn(name="parent_id")
	private ContractFormXmlDataDocument parent;
	
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

	public void setParent(ContractFormXmlDataDocument parent) {
		this.parent = parent;
	}

	public ContractFormXmlDataDocument getParent() {
		return parent;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCategory() {
		return category;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}
	
	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getCreated() {
		return created;
	}
	
	@Transient
	@JsonProperty("userId")
	public long getUserId(){
		if(this.getUser() == null){
			return 0;
		}
		return this.getUser().getId();
	}
	
	@Transient
	@JsonProperty("contractFormId")
	public long getContractFormId(){
		if(this.getContractFormXmlData() == null && this.getContractFormXmlData().getContractForm() == null){
			return 0;
		}
		return this.getContractFormXmlData().getContractForm().getId();
	}
	
	@Transient
	@JsonProperty("contractFormXmlDataId")
	public long getContractFormXmlDataId(){
		if(this.getContractFormXmlData() == null){
			return 0;
		}
		return this.getContractFormXmlData().getId();
	}
	
	@Transient
	@JsonProperty("contractFormType")
	public String getContractFormType(){
		if(this.getContractFormXmlData() == null && this.getContractFormXmlData().getContractForm() == null){
			return null;
		}
		return this.getContractFormXmlData().getContractForm().getContractFormType().toString();
	}
	
	@Transient
	@JsonProperty("contractFormTypeDesc")
	public String getContractFormTypeDescription(){
		if(this.getContractFormXmlData() == null && this.getContractFormXmlData().getContractForm() == null){
			return null;
		}
		return this.getContractFormXmlData().getContractForm().getContractFormType().getDescription();
	}
	
	@Transient
	@JsonProperty("parentContractFormXmlDataDocumentId")
	public long getParentContractFormXmlDataDocumentId(){
		if(this.getParent() == null){
			return 0;
		}
		return this.getParent().getId();
	}
	
	@Transient
	@JsonProperty("createdDate")
	public String getCreatedDate(){
		if(this.getCreated() == null){
			return "";
		}
		return DateFormatUtil.formateDate(this.getCreated());
	}

	public void setContractFormXmlData(ContractFormXmlData contractFormXmlData) {
		this.contractFormXmlData = contractFormXmlData;
	}

	public ContractFormXmlData getContractFormXmlData() {
		return contractFormXmlData;
	}

	public void setCommittee(Committee committee) {
		this.committee = committee;
	}

	public Committee getCommittee() {
		return committee;
	}

	public long getVersionId() {
		return versionId;
	}

	public void setVersionId(long versionId) {
		this.versionId = versionId;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	
	
}
