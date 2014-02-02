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

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractStatusEnum;

@Entity
@Table(name = "contract_status")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ContractStatus extends AbstractDomainEntity {
	
	private static final long serialVersionUID = 5635979634792905922L;

	@ManyToOne
	@JoinColumn(name="contract_id")
	private Contract contract;
	
	@Column(name="contract_status")
	@Enumerated(EnumType.STRING)
	private ContractStatusEnum contractStatus;
	
	@Column(name="modified")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modified;
	
	@Column(name="note", nullable=true)
	private String note;
	
	@ManyToOne
	@JoinColumn(name="caused_by_user_id", nullable=true)
	private User causeByUser;
	
	@Column(name="caused_by_committee", nullable=true)
	@Enumerated(EnumType.STRING)
	private Committee causedByCommittee;

	public void setContract(Contract contract) {
		this.contract = contract;
	}

	public Contract getContract() {
		return contract;
	}

	public void setContractStatus(ContractStatusEnum contractStatus) {
		this.contractStatus = contractStatus;
	}

	public ContractStatusEnum getContractStatus() {
		return contractStatus;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public Date getModified() {
		return modified;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getNote() {
		return note;
	}

	public void setCauseByUser(User causeByUser) {
		this.causeByUser = causeByUser;
	}

	public User getCauseByUser() {
		return causeByUser;
	}

	public void setCausedByCommittee(Committee causedByCommittee) {
		this.causedByCommittee = causedByCommittee;
	}

	public Committee getCausedByCommittee() {
		return causedByCommittee;
	}
}
