package edu.uams.clara.webapp.common.domain.email;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;

@Entity
@Table(name = "email_template")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class EmailTemplate extends AbstractDomainEntity implements Serializable {

	private static final long serialVersionUID = 3478468910951793983L;
	
	public static enum Type{
		PROTOCOL, CONTRACT,REPORT;
	}
	
	@Column(name="type")
	@Enumerated(EnumType.STRING)
	private Type type;

	@Column(name="identifier", length=255)
	private String identifier;
	
	@Column(name="send_to", length=8000)
	private String to;
	
	@Column(name="cc", length=8000)
	private String cc;
	
	@Column(name="bcc", length=8000)
	private String bcc;
	
	@Column(name="subject", length=8000)
	private String subject;
	
	@Column(name="vm_template", length=8000)
	private String vmTemplate;
	
	@Transient
	private String templateContent;
	
	@Transient
	private UploadedFile uploadedFile;
	
	@Transient
	private String realSubject;
	
	@Transient
	private String realRecipient;
	
	@Transient
	private String realCCRecipient;
	
	@Column(name="attachments", length=8000)
	private String attachments;

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getVmTemplate() {
		return vmTemplate;
	}

	public void setVmTemplate(String vmTemplate) {
		this.vmTemplate = vmTemplate;
	}

	public String getAttachments() {
		return attachments;
	}

	public void setAttachments(String attachments) {
		this.attachments = attachments;
	}

	public String getBcc() {
		return bcc;
	}

	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	public String getTemplateContent() {
		return templateContent;
	}

	public void setTemplateContent(String templateContent) {
		this.templateContent = templateContent;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public String getRealSubject() {
		return realSubject;
	}

	public void setRealSubject(String realSubject) {
		this.realSubject = realSubject;
	}

	public String getRealRecipient() {
		return realRecipient;
	}

	public void setRealRecipient(String realRecipient) {
		this.realRecipient = realRecipient;
	}

	public String getRealCCRecipient() {
		return realCCRecipient;
	}

	public void setRealCCRecipient(String realCCRecipient) {
		this.realCCRecipient = realCCRecipient;
	}
	
	
}
