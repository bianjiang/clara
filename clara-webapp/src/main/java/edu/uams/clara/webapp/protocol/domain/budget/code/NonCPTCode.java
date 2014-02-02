package edu.uams.clara.webapp.protocol.domain.budget.code;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "none_cpt_code")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class NonCPTCode extends AbstractDomainEntity{

	private static final long serialVersionUID = 4253008810811395240L;

	@Column(name="code")
	private String code;
	
	@Column(name="code_type")
	private String codeType;
	
	@Column(name="description")
	private String description;
	
	@Column(name="status")
	private Integer status;
	
	@Column(name="extra_data_xml")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String extraDataXml;
	
	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getExtraDataXml() {
		return extraDataXml;
	}

	public void setExtraDataXml(String extraDataXml) {
		this.extraDataXml = extraDataXml;
	}

	public String getCodeType() {
		return codeType;
	}

	public void setCodeType(String codeType) {
		this.codeType = codeType;
	}
}
