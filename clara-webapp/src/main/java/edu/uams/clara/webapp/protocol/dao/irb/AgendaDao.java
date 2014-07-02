package edu.uams.clara.webapp.protocol.dao.irb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.AgendaStatusEnum;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem.AgendaItemStatus;
import edu.uams.clara.webapp.protocol.domain.irb.enums.IRBRoster;

@Repository
public class AgendaDao extends AbstractDomainDao<Agenda> {

	private static final long serialVersionUID = -3515715245260155594L;
	private final static Logger logger = LoggerFactory
	.getLogger(AgendaDao.class);
	
	
	@Transactional(readOnly=true)
	public List<Agenda> listAllAgendas(){
		TypedQuery<Agenda> query = getEntityManager()
				.createQuery(
						"SELECT a FROM Agenda a" +
						" WHERE a.retired = :retired", Agenda.class)
				.setParameter("retired", Boolean.FALSE);
				query.setHint("org.hibernate.cacheable", true);
				
				
				return query.getResultList();
		
	}
	
	@Transactional(readOnly=true)
	public List<Agenda> findAgendaByProtocolId(long protocolId){
		String query = "SELECT a.* FROM agenda a"
					+ " INNER JOIN agenda_item ai ON a.id = ai.agenda_id"
					+ " INNER JOIN protocol_form pf ON ai.protocol_form_id = pf.id"
					+ " INNER JOIN protocol p ON pf.protocol_id = p.id"
					+ " WHERE a.retired = :retired"
					+ " AND ai.retired = :retired"
					+ " AND pf.retired = :retired"
					+ " AND p.retired = :retired"
					+ " AND p.id = :protocolId";
		
		TypedQuery<Agenda> q = (TypedQuery<Agenda>) getEntityManager()
		.createNativeQuery(query, Agenda.class);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		q.setHint("org.hibernate.cacheable", true);
		
		
		return q.getResultList();
	}
	
	@Transactional(readOnly=true)
	public Agenda findByDate(Date agendaDate){
		TypedQuery<Agenda> query = getEntityManager()
		.createQuery(
				"SELECT a FROM Agenda a" +
				" WHERE a.retired = :retired AND a.date = :agendaDate", Agenda.class)
		.setParameter("retired", Boolean.FALSE).setParameter(
				"agendaDate", agendaDate);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		
		return query.getSingleResult();
	}
	
	private List<AgendaStatusEnum> availableAgendaStatuses = new ArrayList<AgendaStatusEnum>(
			0);
	{
		availableAgendaStatuses.add(AgendaStatusEnum.AGENDA_INCOMPLETE);
		availableAgendaStatuses.add(AgendaStatusEnum.AGENDA_PENDING_CHAIR_APPROVAL);
		availableAgendaStatuses.add(AgendaStatusEnum.AGENDA_APPROVED);
	}
	
	//@TODO need to fix
	@Transactional(readOnly=true)
	public Agenda getNextAvailableAgenda(List<AgendaStatusEnum> agendaStatuses){
		TypedQuery<Agenda> query = getEntityManager()
				.createQuery(
						" SELECT a FROM Agenda a" +
						" WHERE a.retired = :retired AND a.id IN (" +
						" 	SELECT s.agenda.id FROM AgendaStatus s " +
						" 	WHERE s.retired = :retired AND s.agendaStatus IN (:agendaStatuses) AND s.id IN (" +
						"		SELECT MAX(ass.id) FROM AgendaStatus ass " +
						"		WHERE ass.retired = :retired " +
						"		GROUP BY ass.agenda.id " +
						"		)" +
						"	) AND a.date>=DATEADD(dd, DATEDIFF(dd, 0, GETDATE()), 0) ORDER BY a.date ASC", Agenda.class)

		.setParameter("retired", Boolean.FALSE)
		.setParameter("agendaStatuses", agendaStatuses);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		
		return query.getSingleResult();
	}
	
	@Transactional(readOnly=true)
	public Agenda getNextAvailableAgendaByAgendaId(List<AgendaStatusEnum> agendaStatuses, long agendaId){
		TypedQuery<Agenda> query = getEntityManager()
				.createQuery(
						" SELECT a FROM Agenda a" +
						" WHERE a.retired = :retired AND a.id IN (" +
						" 	SELECT s.agenda.id FROM AgendaStatus s " +
						" 	WHERE s.retired = :retired AND s.agendaStatus IN (:agendaStatuses) AND s.id IN (" +
						"		SELECT MAX(ass.id) FROM AgendaStatus ass " +
						"		WHERE ass.retired = :retired " +
						"		GROUP BY ass.agenda.id " +
						"		)" +
						"	) AND a.id > :agendaId ORDER BY a.date ASC", Agenda.class)

		.setParameter("retired", Boolean.FALSE)
		.setParameter("agendaStatuses", agendaStatuses)
		.setParameter("agendaId", agendaId);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		
		return query.getSingleResult();
	}
	
	@Transactional(readOnly=true)
	public Agenda getLastAgendaForSameCommittee(IRBRoster irbRoster, long currentAgendaId, Date currentAgendaDate){
		TypedQuery<Agenda> query = getEntityManager()
				.createQuery(
						" SELECT a FROM Agenda a, AgendaStatus astatus " +
						" WHERE a.retired = :retired " +
						" AND astatus.retired = :retired " +
						" AND astatus.agenda.id = a.id " +
						" AND astatus.agendaStatus = :angedaStatus " +
						" AND a.id <> :currentAgendaId " +
						" AND a.date < :currentAgendaDate " +
						" AND a.irbRoster = :irbRoster ORDER BY a.date DESC", Agenda.class)

		.setParameter("retired", Boolean.FALSE)
		.setParameter("irbRoster", irbRoster)
		.setParameter("currentAgendaDate", currentAgendaDate)
		.setParameter("angedaStatus", AgendaStatusEnum.MEETING_CLOSED)
		.setParameter("currentAgendaId", currentAgendaId);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		
		return query.getSingleResult();
	}
	
	@Transactional(readOnly=true)
	public List<Agenda> listAgendasByStatuses(List<AgendaStatusEnum> agendaStatuses){
		TypedQuery<Agenda> query = getEntityManager()
		.createQuery(
				" SELECT a FROM Agenda a" +
				" WHERE a.retired = :retired AND a.id IN (" +
				" 	SELECT s.agenda.id FROM AgendaStatus s " +
				" 	WHERE s.retired = :retired AND s.agendaStatus IN (:agendaStatuses) AND s.id IN (" +
				"		SELECT MAX(ass.id) FROM AgendaStatus ass " +
				"		WHERE ass.retired = :retired " +
				"		GROUP BY ass.agenda.id " +
				"		)" +
				"	) ", Agenda.class)
		.setParameter("retired", Boolean.FALSE)
		.setParameter("agendaStatuses", agendaStatuses);
		
		query.setHint("org.hibernate.cacheable", true);
		
		return query.getResultList();
	}
	
	@Transactional(readOnly=true)
	public Agenda getAgendaByProtocolFormIdAndAgendaItemStatus(long protocolFormId,AgendaItemStatus agendaItemStatus){
		TypedQuery<Agenda> query = getEntityManager()
		.createQuery(
				"SELECT a FROM Agenda a" +
				" WHERE a.retired = a.retired AND a.id in (SELECT ad.agenda.id FROM AgendaItem ad WHERE ad.retired = :retired AND ad.protocolFormId = :protocolFormId AND ad.agendaItemStatus = :agendaItemStatus)", Agenda.class)
		.setParameter("retired", Boolean.FALSE).setParameter(
				"protocolFormId", protocolFormId)
				.setParameter("agendaItemStatus", agendaItemStatus);
				;
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		
		return query.getSingleResult();
	}
	
}
