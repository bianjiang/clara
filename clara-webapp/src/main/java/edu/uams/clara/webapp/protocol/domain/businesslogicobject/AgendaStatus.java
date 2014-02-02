package edu.uams.clara.webapp.protocol.domain.businesslogicobject;

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
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.AgendaStatusEnum;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;

@Entity
@Table(name = "agenda_status")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@JsonIgnoreProperties( { "agenda"})
public class AgendaStatus extends AbstractDomainEntity{

	private static final long serialVersionUID = 7143030458952364059L;

	@ManyToOne
	@JoinColumn(name="agenda_id")
	private Agenda agenda;
	
	@Column(name="agenda_status")
	@Enumerated(EnumType.STRING)
	private AgendaStatusEnum agendaStatus;
	
	@Column(name="note")
	private String note;
	
	@Column(name="modified")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modified;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;
	
	@Transient
	@JsonProperty("agendaId")
	public long getAgendaId(){
		if(this.getAgenda() != null){
			return this.getAgenda().getId();
		}
		
		return 0;
	}

	public void setAgenda(Agenda agenda) {
		this.agenda = agenda;
	}

	public Agenda getAgenda() {
		return agenda;
	}

	public void setAgendaStatus(AgendaStatusEnum agendaStatus) {
		this.agendaStatus = agendaStatus;
	}

	public AgendaStatusEnum getAgendaStatus() {
		return agendaStatus;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getNote() {
		return note;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public Date getModified() {
		return modified;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	} 
}
