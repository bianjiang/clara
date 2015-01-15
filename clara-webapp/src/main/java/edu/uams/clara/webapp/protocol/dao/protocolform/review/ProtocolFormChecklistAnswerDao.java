package edu.uams.clara.webapp.protocol.dao.protocolform.review;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.domain.protocolform.review.ProtocolFormChecklistAnswer;

@Repository
public class ProtocolFormChecklistAnswerDao extends AbstractDomainDao<ProtocolFormChecklistAnswer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7489753351908112743L;
	
	@Transactional(readOnly = true)
	public ProtocolFormChecklistAnswer getLatestAnswerByUserAndCommitteeAndProtocolFormId(long userId, Committee committee, long protocolFormId){
		String query = "SELECT pfca FROM ProtocolFormChecklistAnswer pfca "
				+ " WHERE pfca.retired = :retired AND pfca.committee = :committee AND pfca.user.id = :userId AND pfca.protocolForm.id = :protocolFormId "
				+ " ORDER BY pfca.id DESC";
		
		TypedQuery<ProtocolFormChecklistAnswer> q = getEntityManager()
				.createQuery(query, ProtocolFormChecklistAnswer.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setFirstResult(0);
		q.setMaxResults(1);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("committee", committee);
		q.setParameter("userId", userId);
		
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormChecklistAnswer> listChecklistAnswersByCommitteeAndProtocolFormId(Committee committee, long protocolFormId){
		String query = "SELECT pfca FROM ProtocolFormChecklistAnswer pfca "
				+ " WHERE pfca.retired = :retired AND pfca.committee = :committee AND pfca.protocolForm.parent.id IN (SELECT pf.parent.id FROM ProtocolForm pf WHERE pf.id = :protocolFormId) "
				+ " ORDER BY pfca.modified DESC";
		
		TypedQuery<ProtocolFormChecklistAnswer> q = getEntityManager()
				.createQuery(query, ProtocolFormChecklistAnswer.class);
		
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("committee", committee);
		
		return q.getResultList();
	}
}
