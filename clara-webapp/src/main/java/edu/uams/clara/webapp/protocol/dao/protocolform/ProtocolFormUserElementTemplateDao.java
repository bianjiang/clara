package edu.uams.clara.webapp.protocol.dao.protocolform;

import java.util.List;
import java.util.Set;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormUserElementTemplate;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormUserElementTemplate.TemplateType;

@Repository
public class ProtocolFormUserElementTemplateDao extends
		AbstractDomainDao<ProtocolFormUserElementTemplate> {

	private static final long serialVersionUID = 5430512720096443182L;

	@Transactional(readOnly = true)
	public List<ProtocolFormUserElementTemplate> listProtocolFormUserElementTemplateByTemplateTypeAndUserId(
			TemplateType templateType, long userId) {

		String query = "SELECT pfuet FROM ProtocolFormUserElementTemplate pfuet "
				+ " WHERE pfuet.retired = :retired AND pfuet.user.id = :userId AND pfuet.templateType = :templateType "
				+ " ORDER BY pfuet.templateName ASC";

		TypedQuery<ProtocolFormUserElementTemplate> q = getEntityManager()
				.createQuery(query, ProtocolFormUserElementTemplate.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("userId", userId);
		q.setParameter("templateType", templateType);
		return q.getResultList();

	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormUserElementTemplate> listProtocolFormUserElementTemplateByTemplateType(
			TemplateType templateType) {

		String query = "SELECT pfuet FROM ProtocolFormUserElementTemplate pfuet "
				+ " WHERE pfuet.retired = :retired AND pfuet.templateType = :templateType "
				+ " ORDER BY pfuet.templateName ASC";

		TypedQuery<ProtocolFormUserElementTemplate> q = getEntityManager()
				.createQuery(query, ProtocolFormUserElementTemplate.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("templateType", templateType);
		return q.getResultList();

	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormUserElementTemplate> listSharedTemplatesByTypeAndCommittees(
			TemplateType templateType, Set<Committee> committees) {

		String query = "SELECT p FROM ProtocolFormUserElementTemplate p WHERE p.id IN ("
				+ " SELECT DISTINCT pfuet.id FROM ProtocolFormUserElementTemplate pfuet, UserRole ur, Role r "
				+ " WHERE pfuet.retired = :retired AND pfuet.templateType = :templateType "
				+ " AND ur.user.id = pfuet.user.id AND ur.retired = :retired AND r.id = ur.role.id AND r.retired = :retired "
				+ " AND r.committee IN (:committees)) "
				+ " ORDER BY p.templateName ASC";

		TypedQuery<ProtocolFormUserElementTemplate> q = getEntityManager()
				.createQuery(query, ProtocolFormUserElementTemplate.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("templateType", templateType);
		q.setParameter("committees", committees);
		
		return q.getResultList();

	}

}
