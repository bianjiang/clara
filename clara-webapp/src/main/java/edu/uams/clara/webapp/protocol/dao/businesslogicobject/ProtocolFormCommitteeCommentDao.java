package edu.uams.clara.webapp.protocol.dao.businesslogicobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.UserRole;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeComment;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.CommentType;

@Repository
public class ProtocolFormCommitteeCommentDao extends AbstractDomainDao<ProtocolFormCommitteeComment> {

	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolFormCommitteeCommentDao.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -6949297347735878260L;

	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeComment> listAllParentsByProtocolFormId(long protocolFormId){
		String query = "SELECT pfcc FROM ProtocolFormCommitteeComment pfcc "
				+ " WHERE pfcc.replyTo.id = pfcc.id AND pfcc.protocolForm.id = :protocolFormId AND pfcc.retired = :retired "
				+ " ORDER BY pfcc.commentType ASC, pfcc.modified DESC";

		TypedQuery<ProtocolFormCommitteeComment> q = getEntityManager().createQuery(query,
				ProtocolFormCommitteeComment.class);

		q.setHint("org.hibernate.cacheable", true);
		
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
	
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeComment> listAllCommentsByProtocolFormId(long protocolFormId){
		String query = "SELECT pfcc FROM ProtocolFormCommitteeComment pfcc "
				+ " WHERE pfcc.protocolForm.id = :protocolFormId AND pfcc.retired = :retired ORDER BY pfcc.id DESC";
		TypedQuery<ProtocolFormCommitteeComment> q = getEntityManager().createQuery(query,
				ProtocolFormCommitteeComment.class);

		q.setHint("org.hibernate.cacheable", true);
		
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
	
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeComment> listAllCommentsByProtocolFormIdAndCommittees(long protocolFormId, List<Committee> committees){
		String query = "SELECT pfcc FROM ProtocolFormCommitteeComment pfcc "
				+ " WHERE pfcc.protocolForm.id = :protocolFormId AND pfcc.retired = :retired AND pfcc.committee IN :committees ORDER BY pfcc.id DESC";
		TypedQuery<ProtocolFormCommitteeComment> q = getEntityManager().createQuery(query,
				ProtocolFormCommitteeComment.class);

		q.setHint("org.hibernate.cacheable", true);
		
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("committees", committees);
		
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeComment> listCommentsByProtocolFormIdAndCommitteeAndInLetterOrNot(long protocolFormId, Committee committee, boolean inLetter, List<CommentType> commentTypeList){
		String query = "SELECT pfcc FROM ProtocolFormCommitteeComment pfcc "
				+ " WHERE pfcc.protocolForm.id = :protocolFormId AND pfcc.retired = :retired AND pfcc.committee = :committee AND pfcc.inLetter = :inLetter"
				+ " AND pfcc.commentType NOT IN :commentTypeList"
				+ " ORDER BY pfcc.modified DESC";

		TypedQuery<ProtocolFormCommitteeComment> q = getEntityManager().createQuery(query,
				ProtocolFormCommitteeComment.class);

		q.setHint("org.hibernate.cacheable", true);
		
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("committee", committee);
		q.setParameter("inLetter", inLetter);
		q.setParameter("commentTypeList", commentTypeList);
	
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeComment> listCommentsByProtocolFormIdAndCommitteeAndCommentTypeList(long protocolFormId, Committee committee, List<CommentType> commentTypeList){
		String query = "SELECT pfcc FROM ProtocolFormCommitteeComment pfcc "
				+ " WHERE pfcc.retired = :retired AND pfcc.committee = :committee AND pfcc.commentType IN :commentTypeList"
				+ " AND pfcc.protocolForm.id IN (SELECT pf.id FROM ProtocolForm pf WHERE pf.parent.id IN (SELECT pfm.parent.id FROM ProtocolForm pfm WHERE pfm.id = :protocolFormId)) "
				+ " AND (pfcc.commentStatus is NULL OR pfcc.commentStatus = 'NOT_MET')"
				+ " AND pfcc.inLetter = :inLetter "
				+ " ORDER BY pfcc.commentType ASC, pfcc.modified DESC";

		TypedQuery<ProtocolFormCommitteeComment> q = getEntityManager().createQuery(query,
				ProtocolFormCommitteeComment.class);

		q.setHint("org.hibernate.cacheable", true);
		
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("committee", committee);
		q.setParameter("commentTypeList", commentTypeList);
		q.setParameter("inLetter", Boolean.TRUE);
	
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeComment> listCommentsByProtocolFormIdAndCommitteeAndCommentType(long protocolFormId, Committee committee, CommentType commentType){
		String query = "SELECT pfcc FROM ProtocolFormCommitteeComment pfcc "
				+ " WHERE pfcc.retired = :retired AND pfcc.committee = :committee AND pfcc.commentType = :commentType"
				+ " AND pfcc.protocolForm.id IN (SELECT pf.id FROM ProtocolForm pf WHERE pf.parent.id IN (SELECT pfm.parent.id FROM ProtocolForm pfm WHERE pfm.id = :protocolFormId)) "
				+ " AND (pfcc.commentStatus is NULL OR pfcc.commentStatus = 'NOT_MET')"
				+ " AND pfcc.inLetter = :inLetter "
				+ " ORDER BY pfcc.commentType ASC, pfcc.modified DESC";

		TypedQuery<ProtocolFormCommitteeComment> q = getEntityManager().createQuery(query,
				ProtocolFormCommitteeComment.class);

		q.setHint("org.hibernate.cacheable", true);
		
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("committee", committee);
		q.setParameter("commentType", commentType);
		q.setParameter("inLetter", Boolean.TRUE);
	
		return q.getResultList();
	}
	
	private List<Committee> irbRelatedCommitteeLst = Lists.newArrayList();{
		irbRelatedCommitteeLst.add(Committee.IRB_CONSENT_REVIEWER);
		irbRelatedCommitteeLst.add(Committee.IRB_EXEMPT_REVIEWER);
		irbRelatedCommitteeLst.add(Committee.IRB_EXPEDITED_REVIEWER);
		irbRelatedCommitteeLst.add(Committee.IRB_OFFICE);
		irbRelatedCommitteeLst.add(Committee.IRB_PREREVIEW);
		irbRelatedCommitteeLst.add(Committee.IRB_REVIEWER);
		irbRelatedCommitteeLst.add(Committee.IRB_CHAIR);
		irbRelatedCommitteeLst.add(Committee.IRB_MEETING_OPERATOR);
	}
	
	/**
	 * since protocolForm is versioned, here we need to list all comments that link to a form, which parent is the same.
	 * @param protocolFormId
	 * @param userId
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<ProtocolFormCommitteeComment> listAllParentsByProtocolFormIdExcludingByUserId(long protocolFormId, User currentUser){
		Set<Committee> committeeLst = Sets.newHashSet();
		
		boolean showIRBComments = false;
		if (currentUser.getAuthorities().contains(Permission.VIEW_IRB_COMMENTS)){
			showIRBComments = true;
		} else {
			showIRBComments = false;
			
			if (currentUser.getUserRoles().size() > 0) {
				for (UserRole userRole : currentUser.getUserRoles()){
					if(userRole.getRole().getCommitee() == null) continue;
					committeeLst.add(userRole.getRole().getCommitee());
				}
			}
			
			/*
			if(committeeLst.contains(Committee.BUDGET_REVIEW)&&!committeeLst.contains(Committee.COVERAGE_REVIEW)){
				committeeLst.add(Committee.COVERAGE_REVIEW);
			}
			if(committeeLst.contains(Committee.COVERAGE_REVIEW)&&!committeeLst.contains(Committee.BUDGET_REVIEW)){
				committeeLst.add(Committee.BUDGET_REVIEW);
			}*/
			
			//Budget review and coverage should be able to see each's comments 
			if (committeeLst.contains(Committee.BUDGET_REVIEW) || committeeLst.contains(Committee.COVERAGE_REVIEW)) {
				committeeLst.add(Committee.COVERAGE_REVIEW);
				committeeLst.add(Committee.BUDGET_REVIEW);
			}
		}

		String query = "SELECT pfcc FROM ProtocolFormCommitteeComment pfcc, ProtocolForm pf "
				+ " WHERE pf.id = :protocolFormId AND pfcc.replyTo.id = pfcc.id AND ((pfcc.user.id = :userId) "
				+ (showIRBComments?" OR (pfcc.committee IN :irbRelatedCommitteeLst AND pfcc.isPrivate = :isPrivate) ":" OR (pfcc.committee IN :irbRelatedCommitteeLst AND pfcc.inLetter = :inLetter) "
				+ (committeeLst.size()>0?"OR (pfcc.committee IN :committeeLst AND pfcc.isPrivate = :isPrivate) ":""))
				+ " OR (pfcc.isPrivate <> :isPrivate "+ (showIRBComments?" AND pfcc.committee <> 'BUDGET_REVIEW' AND pfcc.committee <> 'COVERAGE_REVIEW'":"") +")) AND (pfcc.protocolForm.parent.id = pf.parent.id) AND pfcc.retired = :retired "
				+ " ORDER BY pfcc.commentType ASC, pfcc.modified DESC";

		TypedQuery<ProtocolFormCommitteeComment> q = getEntityManager().createQuery(query,
				ProtocolFormCommitteeComment.class);

		q.setHint("org.hibernate.cacheable", true);
		
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("isPrivate", Boolean.TRUE);
		q.setParameter("protocolFormId", protocolFormId);
		//q.setParameter("commentType", CommentType.COMMITTEE_PRIVATE_NOTE);
		q.setParameter("userId", currentUser.getId());
		q.setParameter("irbRelatedCommitteeLst", irbRelatedCommitteeLst);
		
		if (!showIRBComments){
			if (committeeLst.size()>0){
				q.setParameter("committeeLst", committeeLst);
			}
			
			q.setParameter("inLetter", Boolean.TRUE);
		}
		
		return q.getResultList();
	}
	
	
	
}
