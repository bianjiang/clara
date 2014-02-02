package edu.uams.clara.webapp.protocol.domain.irb;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;

@Entity
@Configurable(dependencyCheck=true)
@Table(name = "agenda_item")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@JsonIgnoreProperties({"protocolForm", "protocolFormDao"})
public class AgendaItem extends AbstractDomainEntity{

	private static final long serialVersionUID = -8605997276434111486L;

	public enum AgendaItemCategory {
		EXEMPT("Exempty"), EXPEDITED("Expedited"), REPORTED("Reported to the Committee"), FULL_BOARD("Full Board"), MINUTES("Minutes");
		
		private String description;
		
		private AgendaItemCategory(String description){
			this.description = description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}
	
	public enum AgendaItemStatus {
		REMOVED("Removed"),NEW("New");
		
		private String description;
		
		private AgendaItemStatus(String description){
			this.description = description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}


	@ManyToOne
	@JoinColumn(name="agenda_id")
	private Agenda agenda;
	
	@Column(name="agenda_item_category")
	@Enumerated(EnumType.STRING)
	private AgendaItemCategory agendaItemCategory;
	
	@Column(name="agenda_item_status", nullable=true)
	@Enumerated(EnumType.STRING)
	private AgendaItemStatus agendaItemStatus;
	
	/*@OneToOne
	@JoinColumn(name="protocol_form_id", nullable=true)
	@NotFound(action = NotFoundAction.IGNORE)
	private ProtocolForm protocolForm;*/
	
	@Column(name="protocol_form_id")
	private long protocolFormId;
	
	@Column(name="xml_data")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String xmlData;
	
	@Column(name="display_order")
	private int order;		
	
	public void setAgenda(Agenda agenda) {
		this.agenda = agenda;
	}

	public Agenda getAgenda() {
		return agenda;
	}

	public void setAgendaItemCategory(AgendaItemCategory agendaItemCategory) {
		this.agendaItemCategory = agendaItemCategory;
	}

	public AgendaItemCategory getAgendaItemCategory() {
		return agendaItemCategory;
	}
	
	@Transient
	private ProtocolFormDao protocolFormDao;
	
	@Transient
	public ProtocolForm getProtocolForm() {
		if(this.protocolFormId != 0){
			return protocolFormDao.findById(this.protocolFormId);
		}
		
		return null;
	}
	
	@Transient
	public void setProtocolForm(ProtocolForm protocolForm) {
		if(protocolForm == null){
			this.protocolFormId = 0;
		}else{
			this.protocolFormId = protocolForm.getId();
		}
	}

	public void setXmlData(String xmlData) {
		this.xmlData = xmlData;
	}

	public String getXmlData() {
		return xmlData;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}
	
	
	public long getProtocolFormId(){
		return this.protocolFormId;
	}
	
	public void setProtocolFormId(long protocolFormId) {
		this.protocolFormId = protocolFormId;
	}
	
	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}
	
	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public AgendaItemStatus getAgendaItemStatus() {
		return agendaItemStatus;
	}

	public void setAgendaItemStatus(AgendaItemStatus agendaItemStatus) {
		this.agendaItemStatus = agendaItemStatus;
	}
}
