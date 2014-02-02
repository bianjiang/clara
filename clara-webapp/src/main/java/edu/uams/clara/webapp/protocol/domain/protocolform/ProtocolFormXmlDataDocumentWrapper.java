package edu.uams.clara.webapp.protocol.domain.protocolform;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FieldResult;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocument.Status;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;


@NamedNativeQuery(
	    name="listProtocolFormXmlDataDocumentsByProtocolId",
	    query="SELECT protocol_form_xml_data_document.*, protocol_form_xml_data.protocol_form_id, protocol_form.protocol_form_type, protocol_form.parent_id AS parent_protocol_form_id FROM protocol_form_xml_data_document" +
				" INNER JOIN protocol_form_xml_data ON protocol_form_xml_data.id = protocol_form_xml_data_document.protocol_form_xml_data_id" +
				" INNER JOIN protocol_form ON protocol_form.id = protocol_form_xml_data.protocol_form_id" +
				" WHERE protocol_form_xml_data_document.retired = :retired AND protocol_form_xml_data_document.id IN (" +
				" SELECT MAX(protocol_form_xml_data_document.id) FROM protocol_form_xml_data_document" +
				" INNER JOIN protocol_form_xml_data ON protocol_form_xml_data.id = protocol_form_xml_data_document.protocol_form_xml_data_id" +
				" INNER JOIN protocol_form ON protocol_form.id = protocol_form_xml_data.protocol_form_id" +
				" INNER JOIN protocol ON protocol.id = protocol_form.protocol_id" +
				" WHERE protocol_form_xml_data_document.retired = :retired AND protocol_form_xml_data.retired = :retired AND protocol_form.retired = :retired AND protocol.retired = :retired AND  protocol.id = :protocolId" +
				" GROUP BY protocol_form_xml_data_document.parent_id)", 
	    resultSetMapping = "ProtocolFormXmlDataDocumentWrapper")

@SqlResultSetMapping(name="ProtocolFormXmlDataDocumentWrapper",
entities={
    @EntityResult(
    		entityClass = ProtocolFormXmlDataDocumentWrapper.class, fields={
        @FieldResult(name="id", column="id"),
        @FieldResult(name="concurrentVersion", column="concurrent_version"),
        @FieldResult(name="retired", column="retired"),
        @FieldResult(name="protocolFormId", column="protocol_form_id"),
        @FieldResult(name="protocolFormType", column="protocol_form_type"),
        @FieldResult(name="parentProtocolFormId", column="parent_protocol_form_id"),
        @FieldResult(name="protocolFormXmlDataId", column="protocol_form_xml_data_id"),
        @FieldResult(name="userId", column="user_id"),
        @FieldResult(name="committee", column="committee"),
        @FieldResult(name="uploadedFile", column="uploaded_file_id"),
        @FieldResult(name="parentProtocolFormXmlDataDocumentId", column="parent_id"),
        @FieldResult(name="category", column="category"),
        @FieldResult(name="title", column="title"),
        @FieldResult(name="created", column="created"),
        @FieldResult(name="versionId", column="version_id"),
        @FieldResult(name="status", column="status"),
       })}
)
@Entity
@Table(name = "protocol_form_xml_data_document")
public class ProtocolFormXmlDataDocumentWrapper extends AbstractDomainEntity {
	
	private static final long serialVersionUID = 170961668521112557L;
	
	@Column(name = "protocol_form_id")
	private long protocolFormId;
	
	@Column(name = "parent_protocol_form_id")
	private long parentProtocolFormId;

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
	private long parentProtocolFormXmlDataDocumentId;
	
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
	
	@Column(name="protocol_form_type")
	@Enumerated(EnumType.STRING)
	private ProtocolFormType protocolFormType;

	@Column(name="protocol_form_xml_data_id")
	private long protocolFormXmlDataId;
	
	@Transient
	@JsonProperty("protocolFormTypeDesc")
	public String getProtocolFormTypeDescription(){
		if(this.protocolFormType == null){
			return null;
		}
		return this.protocolFormType.getDescription();
	}
	
	@Transient
	@JsonProperty("createdDate")
	public String getCreatedDate(){
		if(this.getCreated() == null){
			return "";
		}
		return DateFormatUtil.formateDate(this.getCreated());
	}
	
	public long getProtocolFormId() {
		return protocolFormId;
	}

	public void setProtocolFormId(long protocolFormId) {
		this.protocolFormId = protocolFormId;
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

	public long getParentProtocolFormXmlDataDocumentId() {
		return parentProtocolFormXmlDataDocumentId;
	}

	public void setParentProtocolFormXmlDataDocumentId(
			long parentProtocolFormXmlDataDocumentId) {
		this.parentProtocolFormXmlDataDocumentId = parentProtocolFormXmlDataDocumentId;
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



	public ProtocolFormType getProtocolFormType() {
		return protocolFormType;
	}



	public void setProtocolFormType(ProtocolFormType protocolFormType) {
		this.protocolFormType = protocolFormType;
	}


	public long getProtocolFormXmlDataId() {
		return protocolFormXmlDataId;
	}


	public void setProtocolFormXmlDataId(long protocolFormXmlDataId) {
		this.protocolFormXmlDataId = protocolFormXmlDataId;
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

	public long getParentProtocolFormId() {
		return parentProtocolFormId;
	}

	public void setParentProtocolFormId(long parentProtocolFormId) {
		this.parentProtocolFormId = parentProtocolFormId;
	}	
	
}
