package edu.uams.clara.webapp.protocol.domain.irb;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FieldResult;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem.AgendaItemCategory;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem.AgendaItemStatus;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;

@NamedNativeQuery(
	    name="listByAgendaId",
	    query="SELECT agenda_item.*, agenda.date AS agenda_date FROM agenda_item "
	    		+ " INNER JOIN agenda ON agenda_item.agenda_id = agenda.id "
				+ " WHERE agenda_item.retired = :retired AND agenda.retired = :retired AND agenda.id = :agendaId"
				+ " ORDER BY agenda_item.display_order ASC", 
	    resultSetMapping = "AgendaItemWrapper")

@SqlResultSetMapping(name="AgendaItemWrapper",
entities={
    @EntityResult(
    		entityClass = AgendaItemWrapper.class, fields={
        @FieldResult(name="id", column="id"),
        @FieldResult(name="concurrentVersion", column="concurrent_version"),
        @FieldResult(name="retired", column="retired"),
        @FieldResult(name="agendaId", column="agenda_id"),
        @FieldResult(name="agendaItemCategory", column="agenda_item_category"),
        @FieldResult(name="agendaItemStatus", column="agenda_item_status"),
        @FieldResult(name="protocolFormId", column="protocol_form_id"),
        @FieldResult(name="xmlData", column="xml_data"),
        @FieldResult(name="displayOrder", column="display_order"),
        @FieldResult(name="agendaDate", column="agenda_date"),
       })}
)
@Entity
@Configurable(dependencyCheck=true)
@Table(name = "agenda_item")
public class AgendaItemWrapper extends AbstractDomainEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5957092729705619053L;
	
	@Column(name = "agenda_id")
	private long agendaId;
	
	@Column(name = "agenda_item_category")
	@Enumerated(EnumType.STRING)
	private AgendaItemCategory agendaItemCategory;
	
	@Column(name="agenda_item_status", nullable=true)
	@Enumerated(EnumType.STRING)
	private AgendaItemStatus agendaItemStatus;
	
	@Column(name="protocol_form_id")
	private long protocolFormId;
	
	@Column(name="xml_data")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String xmlData;
	
	@Column(name="display_order")
	private int displayOrder;
	
	@Column(name="agenda_date")
	@Temporal(TemporalType.DATE)
	private Date agendaDate;
	
	@Transient
	private ProtocolFormDao protocolFormDao;
	
	@Transient
	public ProtocolForm getProtocolForm() {
		if(this.protocolFormId != 0){
			return this.getProtocolFormDao().findById(this.protocolFormId);
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

	public long getAgendaId() {
		return agendaId;
	}

	public void setAgendaId(long agendaId) {
		this.agendaId = agendaId;
	}

	public AgendaItemCategory getAgendaItemCategory() {
		return agendaItemCategory;
	}

	public void setAgendaItemCategory(AgendaItemCategory agendaItemCategory) {
		this.agendaItemCategory = agendaItemCategory;
	}

	public AgendaItemStatus getAgendaItemStatus() {
		return agendaItemStatus;
	}

	public void setAgendaItemStatus(AgendaItemStatus agendaItemStatus) {
		this.agendaItemStatus = agendaItemStatus;
	}

	public long getProtocolFormId() {
		return protocolFormId;
	}

	public void setProtocolFormId(long protocolFormId) {
		this.protocolFormId = protocolFormId;
	}

	public String getXmlData() {
		return xmlData;
	}

	public void setXmlData(String xmlData) {
		this.xmlData = xmlData;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public Date getAgendaDate() {
		return agendaDate;
	}

	public void setAgendaDate(Date agendaDate) {
		this.agendaDate = agendaDate;
	}	
	
	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}
	
	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

}
