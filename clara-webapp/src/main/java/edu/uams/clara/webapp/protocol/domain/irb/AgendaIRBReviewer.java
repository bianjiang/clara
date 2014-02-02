package edu.uams.clara.webapp.protocol.domain.irb;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "agenda_roster_member")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class AgendaIRBReviewer extends AbstractDomainEntity{

	private static final long serialVersionUID = 8593939668893194053L;

	public enum AgendaIRBReviewerStatus{
		NORMAL, REPLACED, REMOVED, ADDITIONAL;
	};
	
	@ManyToOne
	@JoinColumn(name="agenda_id")
	private Agenda agenda;
	
	@ManyToOne
	@JoinColumn(name="irb_reviewer_id")
	private IRBReviewer irbReviewer;
	
	@ManyToOne
	@JoinColumn(name="alternate_irb_reviewer_id")
	private IRBReviewer alternateIRBReviewer;
	
	@Column(name="agenda_irb_reviewer_status")
	@Enumerated(EnumType.STRING)
	private AgendaIRBReviewerStatus status;
	
	@Column(name="reason", length=8000)
	private String reason;

	public void setIrbReviewer(IRBReviewer irbReviewer) {
		this.irbReviewer = irbReviewer;
	}

	public IRBReviewer getIrbReviewer() {
		return irbReviewer;
	}

	public void setAlternateIRBReviewer(IRBReviewer alternateIRBReviewer) {
		this.alternateIRBReviewer = alternateIRBReviewer;
	}

	public IRBReviewer getAlternateIRBReviewer() {
		return alternateIRBReviewer;
	}

	public void setAgenda(Agenda agenda) {
		this.agenda = agenda;
	}

	public Agenda getAgenda() {
		return agenda;
	}

	public void setStatus(AgendaIRBReviewerStatus status) {
		this.status = status;
	}

	public AgendaIRBReviewerStatus getStatus() {
		return status;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}
	
	
	
	
}
