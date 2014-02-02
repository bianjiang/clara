package edu.uams.clara.webapp.contract.domain.contractform;

import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormType;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormXmlDataType;
		
@Entity
@Table(name = "contract_form")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ContractForm extends AbstractDomainEntity implements Form {

	private static final long serialVersionUID = 2630067324465063745L;
	
	@ManyToOne
	@JoinColumn(name="contract_id")
	private Contract contract;
	
	@OneToMany(mappedBy="contractForm", fetch=FetchType.EAGER)
	@MapKey(name="contractFormXmlDataType")
	private Map<ContractFormXmlDataType, ContractFormXmlData> typedContractFormXmlDatas;
	
	@Column(name="meta_data_xml")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String metaDataXml;
	/**
	 * UniDirectional 
	 */
	@ManyToOne
	@JoinColumn(name="parent_id")
	private ContractForm parent;	
	
	@Column(name="contract_form_type")
	@Enumerated(EnumType.STRING)
	private ContractFormType contractFormType;
	
	@Column(name="created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@Column(name="locked")
	private boolean locked;

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getCreated() {
		return created;
	}

	public void setParent(ContractForm parent) {
		this.parent = parent;
	}

	public ContractForm getParent() {
		return parent;
	}

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	public Contract getContract() {
		return contract;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setContractFormType(ContractFormType contractFormType) {
		this.contractFormType = contractFormType;
	}

	public ContractFormType getContractFormType() {
		return contractFormType;
	}

	public void setTypedContractFormXmlDatas(
			Map<ContractFormXmlDataType, ContractFormXmlData> typedContractFormXmlDatas) {
		this.typedContractFormXmlDatas = typedContractFormXmlDatas;
	}

	public Map<ContractFormXmlDataType, ContractFormXmlData> getTypedContractFormXmlDatas() {
		return typedContractFormXmlDatas;
	}

	public String getMetaDataXml() {
		return metaDataXml;
	}

	public void setMetaDataXml(String metaDataXml) {
		this.metaDataXml = metaDataXml;
	}

	@Override
	public String getFormType() {

		return this.getContractFormType().toString();
	}

	@Override
	public long getFormId() {
		
		return this.getId();
	}

	@Override
	public String getMetaXml() {
		// TODO Auto-generated method stub
		return this.getMetaDataXml();
	}

	@Override
	public String getIdentifier() {
		return this.getContract().getContractIdentifier();
	}

	@Override
	public long getParentFormId() {
		// TODO Auto-generated method stub
		return this.getParent().getId();
	}

	@Override
	public String getObjectMetaData() {
		// TODO Auto-generated method stub
		return this.getContract().getMetaDataXml();
	}

}
