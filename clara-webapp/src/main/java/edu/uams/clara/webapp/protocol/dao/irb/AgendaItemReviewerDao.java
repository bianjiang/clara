package edu.uams.clara.webapp.protocol.dao.irb;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItemReviewer;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItemReviewerWrapper;

@Repository
public class AgendaItemReviewerDao extends AbstractDomainDao<AgendaItemReviewer> {

	private static final long serialVersionUID = 8978147517521116129L;

	private final static Logger logger = LoggerFactory
	.getLogger(AgendaItemReviewerDao.class);
	
	@Transactional(readOnly=true)
	public AgendaItemReviewer findByAgendaItemIdandIRBReviewerId(long agendaItemId, long irbReviewerId){
		TypedQuery<AgendaItemReviewer> query = getEntityManager()
		.createQuery(
				"SELECT air FROM AgendaItemReviewer air " +
				"WHERE air.retired = :retired " +
				"AND air.agendaItem.id = :agendaItemId " +
				"AND air.irbReviewer.id = :irbReviewerId", AgendaItemReviewer.class)
		.setParameter("retired", Boolean.FALSE).setParameter(
				"agendaItemId", agendaItemId).setParameter("irbReviewerId", irbReviewerId);
		query.setHint("org.hibernate.cacheable", true);
		
		query.setFirstResult(0);
		
		query.setMaxResults(1);
		
		return query.getSingleResult();
	}
	
	@Transactional(readOnly=true)
	public boolean checkIfExistByAgendaItemIdandIRBReviewerId(long agendaItemId, long irbReviewerId){
		String query = "SELECT COUNT(*) FROM agenda_item_reviewer air "
				+ " WHERE air.retired = :retired "
				+ " AND air.agenda_item_id = :agendaItemId " 
				+ " AND air.irb_reviewer_id = :irbReviewerId";
		
		Query q = getEntityManager().createNativeQuery(query)
		.setParameter("retired", Boolean.FALSE).setParameter(
				"agendaItemId", agendaItemId).setParameter("irbReviewerId", irbReviewerId);
		
		try {
			long total = Long.valueOf(q.getSingleResult().toString());

			if (total > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	@Transactional(readOnly=true)
	public List<AgendaItemReviewerWrapper> listByAgendaItemId(long agendaItemId){
		Query query = (TypedQuery<AgendaItemReviewerWrapper>) getEntityManager().createNamedQuery("listByAgendaItemId");
		query.setParameter("retired", Boolean.FALSE);
		query.setParameter("agendaItemId", agendaItemId);
		query.setHint("org.hibernate.cacheable", false);

		return query.getResultList();
	}
	
	@Transactional(readOnly=true)
	public List<AgendaItemReviewer> listByAgendaItemIdNoWrapper(long agendaItemId){
		TypedQuery<AgendaItemReviewer> query = getEntityManager()
		.createQuery(
				"SELECT air FROM AgendaItemReviewer air " +
				"WHERE air.retired = :retired " +
				"AND air.agendaItem.id = :agendaItemId", AgendaItemReviewer.class)
		.setParameter("retired", Boolean.FALSE).setParameter(
				"agendaItemId", agendaItemId);
		query.setHint("org.hibernate.cacheable", true);

		return query.getResultList();
	}
	
	@Transactional(readOnly=true)
	public List listAgendaItemReviewersNameByAgendaItemId(long agendaItemId){
		HibernateEntityManager hem = getEntityManager().unwrap(HibernateEntityManager.class);
		
		Session session = hem.getSession();
		
		String query = "SELECT person.lastname + ', ' + person.firstname AS fullName FROM person"
					+ " INNER JOIN user_account ON user_account.person_id = person.id "
					+ " INNER JOIN irb_reviewer ON irb_reviewer.user_id = user_account.id "
					+ " INNER JOIN agenda_item_reviewer ON agenda_item_reviewer.irb_reviewer_id = irb_reviewer.id "
					+ " WHERE person.retired = :retired AND user_account.retired = :retired AND irb_reviewer.retired = :retired AND agenda_item_reviewer.retired = :retired "
					+ " AND agenda_item_reviewer.agenda_item_id = :agendaItemId";
		
		org.hibernate.Query q = session.createSQLQuery(query);
		q.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		q.setParameter("agendaItemId", agendaItemId);
		q.setParameter("retired", Boolean.FALSE);
		
		return q.list();
	}
	
	@Transactional(readOnly=true)
	public List<AgendaItemReviewer> listByProtocolFormId(long protocolFormId){
		TypedQuery<AgendaItemReviewer> query = getEntityManager()
		.createQuery(
				"SELECT air FROM AgendaItemReviewer air " +
				"WHERE air.retired = :retired " +
				"AND air.agendaItem.id IN (SELECT ai.id FROM AgendaItem ai WHERE ai.retired = :retired AND ai.protocolFormId = :protocolFormId)", AgendaItemReviewer.class)
		.setParameter("retired", Boolean.FALSE).setParameter(
				"protocolFormId", protocolFormId);
		query.setHint("org.hibernate.cacheable", true);

		return query.getResultList();
	}
}
