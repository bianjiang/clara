package edu.uams.clara.webapp.protocol.dao.businesslogicobject;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.AgendaStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.AgendaStatusEnum;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;

@Repository
public class AgendaStatusDao extends AbstractDomainDao<AgendaStatus> {

	private static final long serialVersionUID = -7764304940397088751L;
	
	@Transactional(readOnly = true)
	public AgendaStatus getAgendaStatusByAgendaId(long agendaId){
		String query = "SELECT agendastatus FROM AgendaStatus agendastatus "
			+ " WHERE agendastatus.agenda.id = :agendaId AND agendastatus.retired = :retired ORDER BY agendastatus.id DESC";

		TypedQuery<AgendaStatus> q = getEntityManager().createQuery(query,
				AgendaStatus.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("agendaId", agendaId);
	
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public AgendaStatus getAgendaStatusByAgendaStatusAndProtocolFormId(AgendaStatusEnum agendaStatus,long protocolFormId){
		String query = "SELECT agendastatus FROM AgendaStatus agendastatus "
			+ " WHERE agendastatus.agenda.id in (SELECT a.id FROM Agenda a  WHERE a.retired = a.retired AND a.id = (SELECT ad.agenda.id FROM AgendaItem ad WHERE ad.retired = :retired AND ad.protocolFormId = :protocolFormId))"
			+ " AND agendastatus.retired = :retired "
			+ " AND agendastatus.agendaStatus=:agendaStatus"
			+ " ORDER BY agendastatus.id DESC";

		TypedQuery<AgendaStatus> q = getEntityManager().createQuery(query,
				AgendaStatus.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("agendaStatus", agendaStatus);
		q.setParameter("protocolFormId", protocolFormId);
	
		return q.getSingleResult();
	}

}
