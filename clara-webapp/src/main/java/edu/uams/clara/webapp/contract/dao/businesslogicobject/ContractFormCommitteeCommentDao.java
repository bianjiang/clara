package edu.uams.clara.webapp.contract.dao.businesslogicobject;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormCommitteeComment;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.CommentType;

@Repository
public class ContractFormCommitteeCommentDao extends AbstractDomainDao<ContractFormCommitteeComment> {

	private static final long serialVersionUID = -7048757496550827848L;

	@Transactional(readOnly = true)
	public List<ContractFormCommitteeComment> listAllParentsByContractFormId(long contractFormId){
		String query = "SELECT pfcc FROM ContractFormCommitteeComment pfcc "
				+ " WHERE pfcc.replyTo.id = pfcc.id AND pfcc.contractForm.id = :contractFormId AND pfcc.retired = :retired "
				+ " ORDER BY pfcc.commentType ASC, pfcc.modified DESC";

		TypedQuery<ContractFormCommitteeComment> q = getEntityManager().createQuery(query,
				ContractFormCommitteeComment.class);

		q.setHint("org.hibernate.cacheable", true);
		
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);
	
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ContractFormCommitteeComment> listCommentsByContractFormIdAndCommitteeAndInLetterOrNot(long contractFormId, Committee committee, boolean inLetter){
		String query = "SELECT pfcc FROM ContractFormCommitteeComment pfcc "
				+ " WHERE pfcc.contractForm.id = :contractFormId AND pfcc.retired = :retired AND pfcc.committee = :committee AND pfcc.inLetter = :inLetter"
				+ " ORDER BY pfcc.commentType ASC, pfcc.modified DESC";

		TypedQuery<ContractFormCommitteeComment> q = getEntityManager().createQuery(query,
				ContractFormCommitteeComment.class);

		q.setHint("org.hibernate.cacheable", true);
		
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);
		q.setParameter("committee", committee);
		q.setParameter("inLetter", inLetter);
	
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ContractFormCommitteeComment> listCommentsByContractFormIdAndCommitteeAndCommentTypeList(long contractFormId, Committee committee, List<CommentType> commentTypeList){
		String query = "SELECT pfcc FROM ContractFormCommitteeComment pfcc "
				+ " WHERE pfcc.contractForm.id = :contractFormId AND pfcc.retired = :retired AND pfcc.committee = :committee AND pfcc.commentType IN :commentTypeList"
				+ " ORDER BY pfcc.commentType ASC, pfcc.modified DESC";

		TypedQuery<ContractFormCommitteeComment> q = getEntityManager().createQuery(query,
				ContractFormCommitteeComment.class);

		q.setHint("org.hibernate.cacheable", true);
		
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);
		q.setParameter("committee", committee);
		q.setParameter("commentTypeList", commentTypeList);
	
		return q.getResultList();
	}
	
	/**
	 * since contractForm is versioned, here we need to list all comments that link to a form, which parent is the same.
	 * @param contractFormId
	 * @param userId
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<ContractFormCommitteeComment> listAllParentsByContractFormIdExcludingByUserId(long contractFormId, long userId){
		String query = "SELECT pfcc FROM ContractFormCommitteeComment pfcc, ContractForm pf "
				+ " WHERE pf.id = :contractFormId AND ((pfcc.user.id = :userId AND pfcc.commentType = :commentType) OR (pfcc.commentType <> :commentType AND pfcc.replyTo.id = pfcc.id)) AND (pfcc.contractForm.parent.id = pf.parent.id) AND pfcc.retired = :retired "
				+ " ORDER BY pfcc.commentType ASC, pfcc.modified DESC";

		TypedQuery<ContractFormCommitteeComment> q = getEntityManager().createQuery(query,
				ContractFormCommitteeComment.class);

		q.setHint("org.hibernate.cacheable", true);
		
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("contractFormId", contractFormId);
		q.setParameter("commentType", CommentType.PERSONAL_NOTE);
		q.setParameter("userId", userId);
	
		return q.getResultList();
	}
	
	
	
}
