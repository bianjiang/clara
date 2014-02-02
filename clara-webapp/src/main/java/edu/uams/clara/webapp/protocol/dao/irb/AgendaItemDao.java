package edu.uams.clara.webapp.protocol.dao.irb;

import java.util.Date;
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
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem.AgendaItemCategory;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItemWrapper;

@Repository
public class AgendaItemDao extends AbstractDomainDao<AgendaItem> {

	private static final long serialVersionUID = -4185186510877376509L;
	
	private final static Logger logger = LoggerFactory
			.getLogger(AgendaItemDao.class);

	@Transactional(readOnly = true)
	public List<AgendaItem> listByAgendaDate(Date agendaDate) {
		TypedQuery<AgendaItem> query = getEntityManager()
				.createQuery(
						"SELECT ai FROM AgendaItem ai, Agenda a"
								+ " WHERE a.id = ai.agenda.id AND ai.retired = :retired AND a.retired = a.retired AND a.date = :agendaDate",
						AgendaItem.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("agendaDate", agendaDate);
		query.setHint("org.hibernate.cacheable", true);

		return query.getResultList();
	}

	@Transactional(readOnly = true)
	public List<AgendaItemWrapper> listByAgendaId(long agendaId) {
		Query q = (TypedQuery<AgendaItemWrapper>) getEntityManager().createNamedQuery("listByAgendaId");
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("agendaId", agendaId);
		
		/*
		TypedQuery<AgendaItemWrapper> query = (TypedQuery<AgendaItemWrapper>) getEntityManager()
				.createNativeQuery(
						"SELECT agenda_item.*, agenda.date AS agenda_date FROM agenda_item, agenda"
								+ " WHERE agenda_item.retired = :retired AND agenda.retired = :retired AND agenda_item.agenda_id = agenda.id AND agenda.id = :agendaId"
								+ " ORDER BY agenda_item.display_order ASC", AgendaItemWrapper.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("agendaId", agendaId);
		query.setHint("org.hibernate.cacheable", true);
		*/
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<AgendaItem> listByAgendaIdNoWrapper(long agendaId) {
		TypedQuery<AgendaItem> query = getEntityManager()
				.createQuery(
						"SELECT ai FROM AgendaItem ai "
								+ " WHERE ai.retired = :retired AND ai.agenda.id = :agendaId"
								+ " ORDER BY ai.order ASC", AgendaItem.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("agendaId", agendaId);
		query.setHint("org.hibernate.cacheable", true);

		return query.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<AgendaItem> listByAgendaIdAndCategory(long agendaId, AgendaItemCategory agendaItemCategory) {
		TypedQuery<AgendaItem> query = getEntityManager()
				.createQuery(
						"SELECT ai FROM AgendaItem ai "
								+ " WHERE ai.retired = :retired AND ai.agenda.id = :agendaId AND ai.agendaItemCategory = :agendaItemCategory "
								+ " ORDER BY ai.order ASC", AgendaItem.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("agendaId", agendaId)
				.setParameter("agendaItemCategory", agendaItemCategory);
		query.setHint("org.hibernate.cacheable", true);

		return query.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List listByAgendaIdPure(long agendaId, boolean hideReported) {
		HibernateEntityManager hem = getEntityManager().unwrap(HibernateEntityManager.class);
		
		Session session = hem.getSession();
		
		String query = "SELECT CAST(agenda_item.id AS varchar(20)) AS agendaItemId, CAST(agenda_item.protocol_form_id AS varchar(20)) AS protocolFormId, agenda_item.agenda_item_category AS agendaItemCategory, CAST(agenda_item.xml_data AS varchar(MAX)) AS agendaItemXmlData "
					+ " FROM agenda_item "
					+ " INNER JOIN agenda ON agenda_item.agenda_id = agenda.id "
					+ " WHERE agenda_item.retired = :retired AND agenda.retired = :retired "
					+ ((hideReported)?" AND agenda_item.agenda_item_category <> 'REPORTED'":"")
					+ " AND agenda.id = :agendaId"
					+ " ORDER BY agenda_item.display_order";
		
		org.hibernate.Query q = session.createSQLQuery(query);
		q.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		q.setParameter("agendaId", agendaId);
		q.setParameter("retired", Boolean.FALSE);
		
		return q.list();
	}

	@Transactional(readOnly = true)
	public List<AgendaItem> listByProtocolId(long protocolId) {
		TypedQuery<AgendaItem> query = getEntityManager()
				.createQuery(
						"SELECT ai FROM AgendaItem ai, ProtocolForm pf"
								+ " WHERE ai.retired = :retired AND pf.retired = :retired AND ai.protocolFormId = pf.id AND pf.protocol.id = :protocolId",
						AgendaItem.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("protocolId", protocolId);
		query.setHint("org.hibernate.cacheable", true);

		return query.getResultList();
	}
	
	@Transactional(readOnly = true)
	public AgendaItem getLatestByProtocolFormId(long protocolFormId) {
		TypedQuery<AgendaItem> query = getEntityManager()
				.createQuery(
						"SELECT ai FROM AgendaItem ai"
								+ " WHERE ai.retired = :retired AND ai.protocolFormId = :protocolFormId ORDER BY ai.id DESC",
						AgendaItem.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("protocolFormId", protocolFormId);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);

		return query.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public Date getAgendaDateByProtocolFormId(long protocolFormId) {
		TypedQuery<Date> query = getEntityManager()
				.createQuery(
						"SELECT a.date FROM AgendaItem ai, Agenda a"
								+ " WHERE ai.retired = :retired AND ai.protocolFormId = :protocolFormId AND ai.agenda.id = a.id ORDER BY ai.id DESC",
								Date.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("protocolFormId", protocolFormId);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		
		return query.getSingleResult();
	}
	
	@Transactional
	public void updateAgendaItemOrder(long agendaItemId, int order) {
		String query = "UPDATE agenda_item SET display_order = :order WHERE id = :agendaItemId";
		
		Query q = getEntityManager().createNativeQuery(query);
		q.setParameter("order", order);
		q.setParameter("agendaItemId", agendaItemId);
		
		q.executeUpdate();
	}

}
