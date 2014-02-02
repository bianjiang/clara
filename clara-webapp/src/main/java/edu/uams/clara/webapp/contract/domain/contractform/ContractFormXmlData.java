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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormXmlDataType;

@Entity
@Table(name = "contract_form_xml_data")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ContractFormXmlData extends AbstractDomainEntity  {

	private static final long serialVersionUID = -9088959745533447754L;

	@ManyToOne
	@JoinColumn(name="contract_form_id")
	private ContractForm contractForm;

	@ManyToOne
	@JoinColumn(name="parent_id")
	private ContractFormXmlData parent;

	@Column(name="contract_form_xml_data_type")
	@Enumerated(EnumType.STRING)
	private ContractFormXmlDataType contractFormXmlDataType;

	@Column(name="xml_data")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String xmlData;

	@Column(name="created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	public void setXmlData(String xmlData) {
		this.xmlData = xmlData;
	}

	public String getXmlData() {
		return xmlData;
	}


	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getCreated() {
		return created;
	}

	public void setParent(ContractFormXmlData parent) {
		this.parent = parent;
	}

	public ContractFormXmlData getParent() {
		return parent;
	}

	public void setContractForm(ContractForm contractForm) {
		this.contractForm = contractForm;
	}

	public ContractForm getContractForm() {
		return contractForm;
	}

	public void setContractFormXmlDataType(ContractFormXmlDataType contractFormXmlDataType) {
		this.contractFormXmlDataType = contractFormXmlDataType;
	}

	public ContractFormXmlDataType getContractFormXmlDataType() {
		return contractFormXmlDataType;
	}
}
