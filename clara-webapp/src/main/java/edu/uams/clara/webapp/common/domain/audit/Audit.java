package edu.uams.clara.webapp.common.domain.audit;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import edu.uams.clara.core.domain.AbstractDomainEntity;


@Entity
@Table(name="audit")
public class Audit extends AbstractDomainEntity {

	private static final long serialVersionUID = 4132309560170402139L;

	@Column(name="event_type")
	private String eventType;
	
	@Column(name="message", length=8000)
	private String message;
	
	@Column(name="extra_data", columnDefinition = "varchar(max)")
	private String extraData;
	
	@Column(name="datetime")
	@Temporal(TemporalType.TIMESTAMP)
	private Date datetime;
	
	@Column(name="ref_object_id", nullable=true, updatable=true)
	private long refObjectId;
	
	@Column(name="ref_object_class")
	private String refObjectClass;

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getEventType() {
		return eventType;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setExtraData(String extraData) {
		this.extraData = extraData;
	}

	public String getExtraData() {
		return extraData;
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}

	public Date getDatetime() {
		return datetime;
	}

	public void setRefObjectId(long refObjectId) {
		this.refObjectId = refObjectId;
	}

	public long getRefObjectId() {
		return refObjectId;
	}

	public void setRefObjectClass(String refObjectClass) {
		this.refObjectClass = refObjectClass;
	}

	public String getRefObjectClass() {
		return refObjectClass;
	}

}

