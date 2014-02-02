package edu.uams.clara.webapp.contract.domain.businesslogicobject;

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
import javax.persistence.Lob;
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
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;

@Entity
@Table(name = "contract_form_committee_comment")
@JsonIgnoreProperties({"contractForm", "replyTo", "replies", "contractFormCommitteeStatus", "user"})
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ContractFormCommitteeComment extends AbstractDomainEntity {


	/**
	 * 
	 */
	private static final long serialVersionUID = -7183665195362681667L;

	@ManyToOne
	@JoinColumn(name="contract_form_id")
	private ContractForm contractForm;

	@Column(name="committee")
	@Enumerated(EnumType.STRING)
	private Committee committee;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;
	
	//@Lob
	@Column(name="text", length=8000)
	private String text;
		
	@ManyToOne
	@JoinColumn(name="reply_to_id")
	private ContractFormCommitteeComment replyTo;		
	
	@OneToMany(mappedBy="replyTo", fetch=FetchType.EAGER)
	private List<ContractFormCommitteeComment> replies;
	
	@Column(name="comment_type")
	@Enumerated(EnumType.STRING)
	private CommentType commentType;
	
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

	public String getText() {
		return text;
	}

	public void setContractForm(ContractForm contractForm) {
		this.contractForm = contractForm;
	}

	public ContractForm getContractForm() {
		return contractForm;
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
	public List<ContractFormCommitteeComment> getRepliesNR(){
		List<ContractFormCommitteeComment> replies = new ArrayList<ContractFormCommitteeComment>(this.replies);
				
		
		Iterator<ContractFormCommitteeComment> itr = replies.iterator();
		
		Set<Long> existIds = new HashSet<Long>();
		
		while(itr.hasNext()){
			ContractFormCommitteeComment reply = itr.next();
			
			
			if(reply.getId() == this.getId() || existIds.contains(reply.getId())){
				itr.remove();
			}else{
				reply.getReplies().clear();
			}
			
			existIds.add(reply.getId());
		}
	
		return replies;
	}
	
	public void setReplyTo(ContractFormCommitteeComment replyTo) {
		this.replyTo = replyTo;
	}

	public ContractFormCommitteeComment getReplyTo() {
		return replyTo;
	}

	public void setCommentStatus(CommentStatus commentStatus) {
		this.commentStatus = commentStatus;
	}

	public CommentStatus getCommentStatus() {
		return commentStatus;
	}
	
	public void setReplies(List<ContractFormCommitteeComment> replies) {
		this.replies = replies;
	}

	public List<ContractFormCommitteeComment> getReplies() {
		return replies;
	}

	public boolean isInLetter() {
		return inLetter;
	}

	public void setInLetter(boolean inLetter) {
		this.inLetter = inLetter;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}
	
}
