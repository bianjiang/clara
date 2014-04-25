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
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;


@Entity
@Table(name = "protocol_form_xml_data_document")
@JsonIgnoreProperties( { "user", "protocolFormXmlData", "parent"})
public class ProtocolFormXmlDataDocument extends AbstractDomainEntity {

	private static final long serialVersionUID = -8824146284941316017L;
	
	public enum Status{
		APPROVED("Approved"),
		DRAFT("Draft"),
		RSC_APPROVED("RSC Approved"),
		ACKNOWLEDGED("Acknowledged"),
		DECLINED("Declined"),
		DETERMINED("Determined"),
		RETIRED("Retired"),
		HC_APPROVED("HC Approved"),
		PACKET_DOCUMENT("Packet Documet"),
		EPIC_DOCUMENT("Epic Documet"),
		FINAL_LEGAL_APPROVED("Final Legal Approved");
		
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
	@JoinColumn(name="protocol_form_xml_data_id")
	private ProtocolFormXmlData protocolFormXmlData;
	
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
	private ProtocolFormXmlDataDocument parent;
	
	@Column(name="category")
	private String category;
	
	@Column(name="category_desc", nullable=true)
	private String categoryDesc;
	
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

	public void setParent(ProtocolFormXmlDataDocument parent) {
		this.parent = parent;
	}

	public ProtocolFormXmlDataDocument getParent() {
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
	@JsonProperty("protocolFormId")
	public long getProtocolFormId(){
		if(this.getProtocolFormXmlData() == null && this.getProtocolFormXmlData().getProtocolForm() == null){
			return 0;
		}
		return this.getProtocolFormXmlData().getProtocolForm().getId();
	}
	
	
	@Transient
	@JsonProperty("protocolFormType")
	public String getProtocolFormType(){
		if(this.getProtocolFormXmlData() == null && this.getProtocolFormXmlData().getProtocolForm() == null){
			return null;
		}
		return this.getProtocolFormXmlData().getProtocolForm().getProtocolFormType().toString();
	}
	

	
	@Transient
	@JsonProperty("protocolFormTypeDesc")
	public String getProtocolFormTypeDescription(){
		if(this.getProtocolFormXmlData() == null && this.getProtocolFormXmlData().getProtocolForm() == null){
			return null;
		}
		return this.getProtocolFormXmlData().getProtocolForm().getProtocolFormType().getDescription();
	}
	
	
	
	@Transient
	@JsonProperty("protocolFormXmlDataId")
	public long getProtocolFormXmlDataId(){
		if(this.getProtocolFormXmlData() == null){
			return 0;
		}
		return this.getProtocolFormXmlData().getId();
	}
	
	
	@Transient
	@JsonProperty("parentProtocolFormXmlDataDocumentId")
	public long getParentProtocolFormXmlDataDocumentId(){
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

	public void setProtocolFormXmlData(ProtocolFormXmlData protocolFormXmlData) {
		this.protocolFormXmlData = protocolFormXmlData;
	}

	
	public ProtocolFormXmlData getProtocolFormXmlData() {
		return protocolFormXmlData;
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

	public String getCategoryDesc() {
		return categoryDesc;
	}

	public void setCategoryDesc(String categoryDesc) {
		this.categoryDesc = categoryDesc;
	}
	
	
}
