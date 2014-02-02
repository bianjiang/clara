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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;


@Entity
@Table(name = "contract_form_committee_checklist_xml_data")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ContractFormCommitteeChecklistXmlData extends AbstractDomainEntity {


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5008468740014220906L;

	@ManyToOne
	@JoinColumn(name="contract_form_id")
	private ContractForm contractForm;
	
	@Column(name="committee")
	@Enumerated(EnumType.STRING)
	private Committee committee;
	
	@ManyToOne
	@JoinColumn(name="parent_id")
	private ContractFormCommitteeChecklistXmlData parent;
	
	@Column(name="xml_data")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String xmlData;
	
	@Column(name="created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@Column(name="locked")
	private boolean locked;	

	public void setCommittee(Committee committee) {
		this.committee = committee;
	}

	public Committee getCommittee() {
		return committee;
	}

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

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setContractForm(ContractForm contractForm) {
		this.contractForm = contractForm;
	}

	public ContractForm getContractForm() {
		return contractForm;
	}

	public void setParent(ContractFormCommitteeChecklistXmlData parent) {
		this.parent = parent;
	}

	public ContractFormCommitteeChecklistXmlData getParent() {
		return parent;
	}

}
