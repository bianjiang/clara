package edu.uams.clara.webapp.protocol.domain.businesslogicobject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.CommentStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.CommentType;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;

@Entity
@Table(name = "protocol_form_committee_comment")
@JsonIgnoreProperties({"protocolForm", "replyTo", "replies", "protocolFormCommitteeStatus", "user"})
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ProtocolFormCommitteeComment extends AbstractDomainEntity {

	private static final long serialVersionUID = -6208501447184921108L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="protocol_form_id")
	private ProtocolForm protocolForm;

	@Column(name="committee")
	@Enumerated(EnumType.STRING)
	private Committee committee;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_id")
	private User user;
	
	//@Lob lob gives c3p0 trouble
	@Column(name="text", length=8000)
	private String text;
		
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="reply_to_id")
	private ProtocolFormCommitteeComment replyTo;		
	
	@OneToMany(mappedBy="replyTo", fetch=FetchType.EAGER)
	private List<ProtocolFormCommitteeComment> replies;
	
	@Column(name="comment_type")
	@Enumerated(EnumType.STRING)
	private CommentType commentType;
	
//	@Column(name="contingency_type")
//	@Enumerated(EnumType.STRING)
//	private ContingencyType contingencyType;
	
//	@Column(name="contingency_severity")
//	private Boolean contingencySeverity;
	
	@Column(name="modified")
	@Temporal(TemporalType.TIMESTAMP)
	private Date modified;
	
	@Column(name="comment_status")
	@Enumerated(EnumType.STRING)
	private CommentStatus commentStatus;
	
	@Column(name="is_private")
	private boolean isPrivate;
	
	@Column(name="in_letter")
	private boolean inLetter;
	
	@Column(name="display_order")
    private long displayOrder;
	
	public void setCommittee(Committee committee) {
		this.committee = committee;
	}

	public Committee getCommittee() {
		return committee;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public Date getModified() {
		return modified;
	}

	

	public void setCommentType(CommentType commentType) {
		this.commentType = commentType;
	}

	public CommentType getCommentType() {
		return commentType;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void setText(String text) {
		this.text = text;
	}

//	public Boolean getContingencySeverity() {
//		return contingencySeverity;
//	}
//	
//	public void setContingencySeverity(Boolean s) {
//		this.contingencySeverity = s;
//	}

	public String getText() {
		return text;
	}
	
//	public void setContingencyType(ContingencyType contingencyType) {
//		this.contingencyType = contingencyType;
//	}
//
//	public ContingencyType getContingencyType() {
//		return contingencyType;
//	}

	public void setProtocolForm(ProtocolForm protocolForm) {
		this.protocolForm = protocolForm;
	}

	public ProtocolForm getProtocolForm() {
		return protocolForm;
	}	
	
	@Transient
	@JsonProperty("replyToId")
	public long getReplyToId(){
		if(this.replyTo == null){
			return 0;
		}
		return this.replyTo.getId();
	}
	
	@Transient
	@JsonProperty("userId")
	public long getUserId(){
		if(this.user == null){
			return 0;
		}
		return this.user.getId();
	}
	
	@Transient
	@JsonProperty("userFullname")
	public String getUserFullname(){
		if(this.user == null){
			return "";
		}
		return this.user.getPerson().getFirstname() + " " + this.user.getPerson().getLastname();
	}
	
	@Transient
	@JsonProperty("modifiedDate")
	public String getModifedDate(){
		if(this.getModified() == null){
			return "";
		}
		return DateFormatUtil.formateDate(this.getModified());
	}
	
	@Transient
	@JsonProperty("committeeDescription")
	public String getCommitteeDescription(){
		if(this.committee == null){
			return "";
		}
		
		return this.committee.getDescription();
	}
	
	/**
	 * return non-recursion version to get json right
	 * @return
	 */
	@Transient
	@JsonProperty("children")
	public List<ProtocolFormCommitteeComment> getRepliesNR(){
		List<ProtocolFormCommitteeComment> replies = new ArrayList<ProtocolFormCommitteeComment>(this.replies);
				
		
		Iterator<ProtocolFormCommitteeComment> itr = replies.iterator();
		
		Set<Long> existIds = new HashSet<Long>();
		
		while(itr.hasNext()){
			ProtocolFormCommitteeComment reply = itr.next();
			
			
			if(reply.isRetired() || reply.getId() == this.getId() || existIds.contains(reply.getId())){
				itr.remove();
			}else{
				reply.getReplies().clear();
			}
			
			existIds.add(reply.getId());
		}
	
		return replies;
	}
	
	public void setReplyTo(ProtocolFormCommitteeComment replyTo) {
		this.replyTo = replyTo;
	}

	public ProtocolFormCommitteeComment getReplyTo() {
		return replyTo;
	}

	public void setCommentStatus(CommentStatus commentStatus) {
		this.commentStatus = commentStatus;
	}

	public CommentStatus getCommentStatus() {
		return commentStatus;
	}
	
	public void setReplies(List<ProtocolFormCommitteeComment> replies) {
		this.replies = replies;
	}

	public List<ProtocolFormCommitteeComment> getReplies() {
		return replies;
	}

	public boolean isInLetter() {
		return inLetter;
	}

	public void setInLetter(boolean inLetter) {
		this.inLetter = inLetter;
	}
	
	@JsonProperty("isPrivate")
	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public long getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(long displayOrder) {
		this.displayOrder = displayOrder;
	}
	
}
