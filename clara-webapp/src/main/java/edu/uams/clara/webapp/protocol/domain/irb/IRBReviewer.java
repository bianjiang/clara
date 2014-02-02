package edu.uams.clara.webapp.protocol.domain.irb;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.usercontext.CommitteeReviewer;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.protocol.domain.irb.enums.IRBRoster;

@Entity
@Table(name = "irb_reviewer")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
//@JsonIgnoreProperties({"user"})
public class IRBReviewer extends AbstractDomainEntity implements CommitteeReviewer {

	private static final long serialVersionUID = -2016921161024172566L;

	public enum IRBReviewerType{
		S("Scientist"), N("Non-scientist");
		
		private String description;
		
		private IRBReviewerType(String description){
			this.description = description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}
	
	//@ManyToOne
	@OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	@JoinColumn(name="user_id")
	private User user;
	
	@Column(name="irb_roster", nullable=false)
	@Enumerated(EnumType.STRING)
	private IRBRoster irbRoster;
	
	@Column(name="specialty")
	private String specialty;	
	
	@Column(name="degree")
	private String degree;
	
	@Column(name="is_alternative_member")
	private boolean alternativeMember;
	
	@Column(name="type", nullable=false)
	@Enumerated(EnumType.STRING)
	private IRBReviewerType type;
	
	@Column(name="is_affiliated", nullable=false)
	private boolean affiliated;
	
	@Column(name="is_chair")
	private boolean chair;
	
	@Column(name="is_expedited")
	private boolean expedited;
	
	@Column(name="comment", nullable=true)
	private String comment;
	
//	@Transient
//	@JsonProperty("user_id")
//	public long getUserId(){
//		return this.getId();
//	}
	
	public void setIrbRoster(IRBRoster irbRoster) {
		this.irbRoster = irbRoster;
	}

	public IRBRoster getIrbRoster() {
		return irbRoster;
	}

	public void setDegree(String degree) {
		this.degree = degree;
	}

	public String getDegree() {
		return degree;
	}

	public void setType(IRBReviewerType type) {
		this.type = type;
	}

	public IRBReviewerType getType() {
		return type;
	}

	public void setAffiliated(boolean affiliated) {
		this.affiliated = affiliated;
	}

	public boolean isAffiliated() {
		return affiliated;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void setSpecialty(String specialty) {
		this.specialty = specialty;
	}

	public String getSpecialty() {
		return specialty;
	}

	public void setAlternativeMember(boolean alternativeMember) {
		this.alternativeMember = alternativeMember;
	}

	public boolean isAlternativeMember() {
		return alternativeMember;
	}

	public void setChair(boolean chair) {
		this.chair = chair;
	}

	public boolean isChair() {
		return chair;
	}

	public void setExpedited(boolean expedited) {
		this.expedited = expedited;
	}

	public boolean isExpedited() {
		return expedited;
	}

	
}
