package edu.uams.clara.webapp.protocol.dao.businesslogicobject;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;


@Repository
public class ProtocolFormStatusDao extends AbstractDomainDao<ProtocolFormStatus> {

	private static final long serialVersionUID = -2366324292063053131L;

	/**
	 * get the latest ProtocolFormStatus
	 * @param protocolFormId
	 * @return
	 */
	@Transactional(readOnly = true)
	public ProtocolFormStatus getProtocolFormStatusByFormId(long protocolFormId){
		String query = "SELECT fs FROM ProtocolFormStatus fs "
			+ " WHERE fs.protocolForm.id = :protocolFormId AND fs.retired = :retired ORDER BY fs.id DESC";

		TypedQuery<ProtocolFormStatus> q = getEntityManager().createQuery(query,
				ProtocolFormStatus.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
	
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public ProtocolFormStatus getLatestProtocolFormStatusByFormId(long protocolFormId){
		String query = "SELECT fs FROM ProtocolFormStatus fs "
			+ " WHERE fs.retired = :retired "
			+ " AND fs.protocolForm.id IN (SELECT pf.id FROM ProtocolForm pf WHERE pf.retired = :retired AND pf.parent.id = (SELECT ppf.parent.id FROM ProtocolForm ppf WHERE ppf.retired = :retired AND ppf.id = :protocolFormId))"
			+ " ORDER BY fs.id DESC";

		TypedQuery<ProtocolFormStatus> q = getEntityManager().createQuery(query,
				ProtocolFormStatus.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
	
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public ProtocolFormStatus getProtocolFormStatusByFormIdAndProtocolFormStatus(long protocolFormId, ProtocolFormStatusEnum protocolFormStatus){
		String query = "SELECT pfs FROM ProtocolFormStatus pfs WHERE pfs.retired = :retired AND pfs.id="
			+ "(SELECT MAX(fs.id) FROM ProtocolFormStatus fs "
			+ " WHERE fs.retired = :retired "
			+ " AND fs.protocolFormStatus = :protocolFormStatus "
			+ " AND fs.protocolForm.id IN (SELECT pf.id FROM ProtocolForm pf WHERE pf.retired = :retired AND pf.parent.id = (SELECT ppf.parent.id FROM ProtocolForm ppf WHERE ppf.retired = :retired AND ppf.id = :protocolFormId)))";

		TypedQuery<ProtocolFormStatus> q = getEntityManager().createQuery(query,
				ProtocolFormStatus.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("protocolFormStatus", protocolFormStatus);
	
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormStatus> getAllProtocolFormStatusByParentFormId(
			long protocolFormId) {

		String query = "SELECT fs FROM ProtocolFormStatus fs, ProtocolForm pf "
				+ " WHERE fs.protocolForm.parent.id = pf.parent.id "
				+ " AND pf.id = :protocolFormId  AND fs.retired = :retired AND pf.retired = :retired AND pf.parent.retired = :retired ORDER BY fs.modified ASC";

		TypedQuery<ProtocolFormStatus> q = getEntityManager()
				.createQuery(query, ProtocolFormStatus.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);

		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolFormStatus> getAllProtocolFormStatusByFormId(
			long protocolFormId) {

		String query = "SELECT fs FROM ProtocolFormStatus fs "
				+ " WHERE fs.protocolForm.id = :protocolFormId AND fs.retired = :retired ORDER BY fs.id DESC";

		TypedQuery<ProtocolFormStatus> q = getEntityManager()
				.createQuery(query, ProtocolFormStatus.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);

		return q.getResultList();
	}
}
