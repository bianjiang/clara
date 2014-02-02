package edu.uams.clara.webapp.protocol.domain.irb;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.AgendaStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.AgendaStatusEnum;
import edu.uams.clara.webapp.protocol.domain.irb.enums.IRBRoster;

@Entity
@Table(name = "agenda")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@JsonIgnoreProperties( { "agendaStatuses","meetingXmlData","xmlData"})
public class Agenda extends AbstractDomainEntity {

	private static final long serialVersionUID = 3597270332044416503L;

	@Column(name="date")
	@Temporal(TemporalType.DATE)
	private Date date;	
	
	@Column(name="irb_roster")
	@Enumerated(EnumType.STRING)
	private IRBRoster irbRoster;
	
	
	@Column(name="xml_data")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String xmlData;
	
	@Column(name="meeting_xml_data")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String meetingXmlData;
	
	@OneToMany(mappedBy="agenda", fetch=FetchType.EAGER)
	@Where(clause="retired = 0")
	@OrderBy("modified DESC")
	private List<AgendaStatus> agendaStatuses;
	
	@Transient
	@JsonProperty("agendaStatus")
	public AgendaStatusEnum getAgendaStatus(){
		if(agendaStatuses != null && agendaStatuses.size() > 0){
			return agendaStatuses.get(0).getAgendaStatus();
		}
		return AgendaStatusEnum.AGENDA_INCOMPLETE;
	}
	
	@Transient
	@JsonProperty("agendaStatusDesc")
	public String getAgendaStatusDesc(){
		if(getAgendaStatus() == null || getAgendaStatus().getDescription() == null){
			return AgendaStatusEnum.AGENDA_INCOMPLETE.getDescription();
		}
		return getAgendaStatus().getDescription();
	}	
	
		
	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public void setIrbRoster(IRBRoster irbRoster) {
		this.irbRoster = irbRoster;
	}

	public IRBRoster getIrbRoster() {
		return irbRoster;
	}

	public void setXmlData(String xmlData) {
		this.xmlData = xmlData;
	}

	public String getXmlData() {
		return xmlData;
	}

	public void setAgendaStatuses(List<AgendaStatus> agendaStatuses) {
		this.agendaStatuses = agendaStatuses;
	}

	public List<AgendaStatus> getAgendaStatuses() {
		return agendaStatuses;
	}

	public void setMeetingXmlData(String meetingXmlData) {
		this.meetingXmlData = meetingXmlData;
	}

	public String getMeetingXmlData() {
		return meetingXmlData;
	}

}
