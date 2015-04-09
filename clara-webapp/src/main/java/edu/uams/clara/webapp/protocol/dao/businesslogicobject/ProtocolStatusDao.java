package edu.uams.clara.webapp.protocol.dao.businesslogicobject;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;

@Repository
public class ProtocolStatusDao extends AbstractDomainDao<ProtocolStatus> {

	private static final long serialVersionUID = 3693534301704910546L;

	@Transactional
	public ProtocolStatus findProtocolStatusByStatusAndProtocolId(long protocolId, ProtocolStatusEnum status){
		
		String query ="SELECT ps FROM ProtocolStatus ps "
				+ " WHERE ps.protocol.id = :protocolId AND ps.protocolStatus = :status AND ps.retired = :retired";
		
		TypedQuery<ProtocolStatus> q = getEntityManager()
				.createQuery(query, ProtocolStatus.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setParameter("protocolId", protocolId);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("status", status);
		
		return q.getSingleResult();

		
	}
	
	@Transactional
	public ProtocolStatus findProtocolStatusByProtocolId(long protocolId){
		
		String query ="SELECT ps FROM ProtocolStatus ps "
				+ " WHERE ps.protocol.id = :protocolId AND ps.retired = :retired ORDER BY ps.id DESC";
		
		TypedQuery<ProtocolStatus> q = getEntityManager()
				.createQuery(query, ProtocolStatus.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setParameter("protocolId", protocolId);
		q.setParameter("retired", Boolean.FALSE);
		
		return q.getSingleResult();

		
	}
	
	@Transactional
	public List<ProtocolStatus> listProtocolStatusesByProtocolId(long protocolId){
		
		String query ="SELECT ps FROM ProtocolStatus ps "
				+ " WHERE ps.protocol.id = :protocolId AND ps.retired = :retired ORDER BY ps.id DESC";
		
		TypedQuery<ProtocolStatus> q = getEntityManager()
				.createQuery(query, ProtocolStatus.class);
		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("protocolId", protocolId);
		q.setParameter("retired", Boolean.FALSE);
		
		return q.getResultList();

		
	}

}
