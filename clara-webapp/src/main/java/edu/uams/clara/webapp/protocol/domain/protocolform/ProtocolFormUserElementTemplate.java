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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.usercontext.User;

@Entity
@Table(name = "protocol_form_user_element_template")
@JsonIgnoreProperties({"xmlData"})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ProtocolFormUserElementTemplate extends AbstractDomainEntity {
	
	private static final long serialVersionUID = -2701827622189664626L;

	public enum TemplateType {
		BUDGET, STAFF, DISEASE_ONTOLOGY;
	};	
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;
	
	@Column(name="xml_data")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String xmlData;
	
	@Column(name="template_type")
	@Enumerated(EnumType.STRING)
	private TemplateType templateType;
	
	@Column(name="name")
	private String templateName;
	
	@Column(name="created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void setXmlData(String xmlData) {
		this.xmlData = xmlData;
	}

	public String getXmlData() {
		return xmlData;
	}

	public void setTemplateType(TemplateType templateType) {
		this.templateType = templateType;
	}

	public TemplateType getTemplateType() {
		return templateType;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getCreated() {
		return created;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getTemplateName() {
		return templateName;
	}
	
}
