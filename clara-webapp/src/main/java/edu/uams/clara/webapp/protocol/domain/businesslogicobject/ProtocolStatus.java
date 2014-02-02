package edu.uams.clara.webapp.protocol.domain.businesslogicobject;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
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
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolStatusEnum;

@Entity
@Table(name = "protocol_status")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ProtocolStatus extends AbstractDomainEntity {
	
	private static final long serialVersionUID = -1170453444952157012L;
	
	@ManyToOne
	@JoinColumn(name="protocol_id")
	private Protocol protocol;
	
	@Column(name="protocol_status")
	@Enumerated(EnumType.STRING)
	private ProtocolStatusEnum protocolStatus;
	
	@Column(name="modified")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modified;
	
	@Column(name="note", nullable=true)
	private String note;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="caused_by_user_id", nullable=true)
	private User causeByUser;
	
	@Column(name="caused_by_committee", nullable=true)
	@Enumerated(EnumType.STRING)
	private Committee causedByCommittee;

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocolStatus(ProtocolStatusEnum protocolStatus) {
		this.protocolStatus = protocolStatus;
	}

	public ProtocolStatusEnum getProtocolStatus() {
		return protocolStatus;
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
