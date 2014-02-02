package edu.uams.clara.webapp.common.domain.post;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "message_post")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class Post extends AbstractDomainEntity {

	private static final long serialVersionUID = -4069806755627385001L;
	
	@Column(name="created", nullable=true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@Column(name="expire_date", nullable=true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date expireDate;
	
	@Column(name="title", length=255, nullable=true)
	private String title;
	
	@Column(name="message", length=8000, nullable=true)
	private String message;

	@Column(name="message_level", nullable=true)
	private String messageLevel;
	
	

	public String getMessageLevel() {
		return messageLevel;
	}

	public void setMessageLevel(String messageLevel) {
		this.messageLevel = messageLevel;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

}
