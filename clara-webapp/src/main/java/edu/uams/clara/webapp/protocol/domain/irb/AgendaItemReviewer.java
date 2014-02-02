package edu.uams.clara.webapp.protocol.domain.irb;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "agenda_item_reviewer")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class AgendaItemReviewer extends AbstractDomainEntity {

	private static final long serialVersionUID = -773459444941778596L;

	@ManyToOne
	@JoinColumn(name="agenda_item_id")
	private AgendaItem agendaItem;
	
	@ManyToOne
	@JoinColumn(name="irb_reviewer_id")
	private IRBReviewer irbReviewer;

	public void setAgendaItem(AgendaItem agendaItem) {
		this.agendaItem = agendaItem;
	}

	public AgendaItem getAgendaItem() {
		return agendaItem;
	}

	public void setIrbReviewer(IRBReviewer irbReviewer) {
		this.irbReviewer = irbReviewer;
	}

	public IRBReviewer getIrbReviewer() {
		return irbReviewer;
	}
	
}
