package edu.uams.clara.webapp.protocol.dao.irb;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaIRBReviewer;
import edu.uams.clara.webapp.protocol.domain.irb.IRBReviewer;
import edu.uams.clara.webapp.protocol.domain.irb.enums.IRBRoster;

@Repository
public class IRBReviewerDao extends AbstractDomainDao<IRBReviewer> {

	private static final long serialVersionUID = -4046305216852021032L;

	private final static Logger logger = LoggerFactory
			.getLogger(IRBReviewerDao.class);

	@Transactional(readOnly = true)
	public List<IRBReviewer> findByUserId(long userId) {
		TypedQuery<IRBReviewer> query = getEntityManager()
				.createQuery(
						"SELECT i FROM IRBReviewer i WHERE i.retired = :retired AND i.user.id = :userId",
						IRBReviewer.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("userId", userId);
		query.setHint("org.hibernate.cacheable", true);
		// query.setFirstResult(0);
		// query.setMaxResults(1);

		return query.getResultList();
	}

	@Transactional(readOnly = true)
	public List<IRBReviewer> listIRBReviewersByIRBRoster(IRBRoster irbRoster) {
		TypedQuery<IRBReviewer> query = getEntityManager()
				.createQuery(
						"SELECT i FROM IRBReviewer i WHERE i.retired = :retired AND i.irbRoster LIKE :irbRoster",
						IRBReviewer.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("irbRoster", irbRoster);
		query.setHint("org.hibernate.cacheable", true);
		// query.setFirstResult(0);
		// query.setMaxResults(1);

		return query.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<IRBReviewer> listIRBChairsByIRBRoster(IRBRoster irbRoster) {
		TypedQuery<IRBReviewer> query = getEntityManager()
				.createQuery(
						"SELECT i FROM IRBReviewer i WHERE i.retired = :retired AND i.irbRoster LIKE :irbRoster AND i.chair = :isChair",
						IRBReviewer.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("isChair", Boolean.TRUE)
				.setParameter("irbRoster", irbRoster);
		query.setHint("org.hibernate.cacheable", true);
		// query.setFirstResult(0);
		// query.setMaxResults(1);

		return query.getResultList();
	}
	
	@Transactional(readOnly = true)
	public Boolean checkIfIRBChairByIRBRosterAndUserId(IRBRoster irbRoster, long userId) {
		TypedQuery<IRBReviewer> query = getEntityManager()
				.createQuery(
						"SELECT i FROM IRBReviewer i WHERE i.retired = :retired AND i.irbRoster LIKE :irbRoster AND i.chair = :isChair AND i.user.id = :userId",
						IRBReviewer.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("isChair", Boolean.TRUE)
				.setParameter("irbRoster", irbRoster)
				.setParameter("userId", userId);
		query.setHint("org.hibernate.cacheable", true);
		// query.setFirstResult(0);
		// query.setMaxResults(1);
		
		try {
			if (query.getResultList().size() > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	@Transactional(readOnly = true)
	public Boolean checkIfOnIRBRosterByIRBRosterAndUserId(IRBRoster irbRoster, long userId) {
		TypedQuery<IRBReviewer> query = getEntityManager()
				.createQuery(
						"SELECT i FROM IRBReviewer i WHERE i.retired = :retired AND i.irbRoster LIKE :irbRoster AND i.user.id = :userId",
						IRBReviewer.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("irbRoster", irbRoster)
				.setParameter("userId", userId);
		query.setHint("org.hibernate.cacheable", true);
		// query.setFirstResult(0);
		// query.setMaxResults(1);
		
		try {
			if (query.getResultList().size() > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	@Transactional(readOnly = true)
	public List<IRBReviewer> listExpeditedIRBReviewers() {

		TypedQuery<IRBReviewer> query = getEntityManager()
				.createQuery(
						"SELECT i FROM IRBReviewer i WHERE i.retired = :retired AND i.expedited = :isExpedited",
						IRBReviewer.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("isExpedited", Boolean.TRUE);
		query.setHint("org.hibernate.cacheable", true);
		// query.setFirstResult(0);
		// query.setMaxResults(1);
		//logger.warn("about to return query resultlist");
		return query.getResultList();
	}

	@Transactional(readOnly = true)
	public List<IRBReviewer> listAllIRBReviewers() {
		//logger.warn("Entered 'listAllIRBReviewers'");
		TypedQuery<IRBReviewer> query = getEntityManager().createQuery(
				"SELECT i FROM IRBReviewer i WHERE i.retired = :retired",
				IRBReviewer.class).setParameter("retired", Boolean.FALSE);
		query.setHint("org.hibernate.cacheable", true);
		// query.setFirstResult(0);
		// query.setMaxResults(1);
		//logger.warn("about to return query resultlist");
		return query.getResultList();
	}

	@Transactional(readOnly = true)
	public List<IRBReviewer> listAgendaIRBReviewersByItemId(long agendaItemId) {

		TypedQuery<IRBReviewer> query = getEntityManager()
				.createQuery(
						"SELECT i FROM IRBReviewer i, AgendaItemReviewer a WHERE i.retired = :retired AND a.retired = :retired AND a.irbReviewer.id = i.id AND a.agendaItem.id =:agendaItemId",
						IRBReviewer.class);
		query.setParameter("retired", Boolean.FALSE);
		query.setParameter("agendaItemId", agendaItemId);
		query.setHint("org.hibernate.cacheable", true);

		return query.getResultList();

	}
	
	@Transactional(readOnly = true)
	public List<IRBReviewer> listAgendaIRBReviewersByProtocolFormId(long protocolFormId) {
		/*
		TypedQuery<IRBReviewer> query = getEntityManager()
				.createQuery(
						"SELECT i FROM IRBReviewer i, AgendaItemReviewer a, AgendaItem ai WHERE i.retired = :retired AND a.retired = :retired AND ai.retired = :retired AND a.irbReviewer.id = i.id AND ai.id = a.agendaItem.id AND ai.protocolFormId = :protocolFormId",
						IRBReviewer.class);
		*/
		
		String query = "SELECT i.* FROM irb_reviewer i"
				+ " INNER JOIN agenda_item_reviewer air ON air.irb_reviewer_id = i.id"
				+ " INNER JOIN agenda_item ai ON ai.id = air.agenda_item_id"
				+ " WHERE ai.protocol_form_id = :protocolFormId "
				+ " AND i.retired = :retired AND air.retired = :retired AND ai.retired = :retired";
		
		TypedQuery<IRBReviewer> q = (TypedQuery<IRBReviewer>) getEntityManager().createNativeQuery(query, IRBReviewer.class);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setHint("org.hibernate.cacheable", true);

		return q.getResultList();

	}

}
