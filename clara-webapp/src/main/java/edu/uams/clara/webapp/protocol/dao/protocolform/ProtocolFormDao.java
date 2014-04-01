package edu.uams.clara.webapp.protocol.dao.protocolform;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormUserElementTemplate;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormUserElementTemplate.TemplateType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;

@Repository
public class ProtocolFormDao extends AbstractDomainDao<ProtocolForm> {

	private static final long serialVersionUID = 7492131024720753362L;

	/*@Transactional(readOnly = true) no longer applicable, moved to protocolformstatusdao
	public ProtocolFormStatus getLatestProtocolFormStatusByProtocolFormId(long protocolFormId){
		String query = "SELECT pfs FROM ProtocolFormStatus pfs "
			+ " WHERE pfs.protocolForm.id = :protocolFormId AND pfs.retired = :retired ORDER BY pfs.id DESC";

		TypedQuery<ProtocolFormStatus> q = getEntityManager().createQuery(query,
				ProtocolFormStatus.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
	
		return q.getSingleResult();
	}*/
	
	@Transactional(readOnly = true)
	public ProtocolForm getProtocolFormByProtocolIdAndProtocolFormType(long protocolId, ProtocolFormType protocolFormType){
		String query = "SELECT pf FROM ProtocolForm pf "
			+ " WHERE pf.protocol.id = :protocolId AND pf.protocolFormType = :protocolFormType AND pf.retired = :retired";

		TypedQuery<ProtocolForm> q = getEntityManager().createQuery(query,
				ProtocolForm.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		q.setParameter("protocolFormType", protocolFormType);
	
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public ProtocolForm getProtocolFormByProtocolIdAndProtocolFormTypes(long protocolId, List<ProtocolFormType> protocolFormTypes){
		String query = "SELECT pf FROM ProtocolForm pf "
			+ " WHERE pf.protocol.id = :protocolId AND pf.protocolFormType IN :protocolFormTypes AND pf.retired = :retired ORDER BY pf.id DESC";

		TypedQuery<ProtocolForm> q = getEntityManager().createQuery(query,
				ProtocolForm.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		q.setParameter("protocolFormTypes", protocolFormTypes);
	
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public ProtocolForm getLatestProtocolFormByProtocolIdAndProtocolFormTypeAndProtocolFormStatues(long protocolId, ProtocolFormType protocolFormType, List<ProtocolFormStatusEnum> protocolFormStatues){
		String query = "SELECT pf FROM ProtocolForm pf, ProtocolFormStatus pfs "
			+ " WHERE pf.protocol.id = :protocolId AND pf.protocolFormType = :protocolFormType AND pf.retired = :retired "
			+ " AND pf.id = pfs.protocolForm.id AND pfs.protocolFormStatus IN :protocolFormStatues AND pfs.retired = :retired "
			+ " ORDER BY pf.id DESC";

		TypedQuery<ProtocolForm> q = getEntityManager().createQuery(query,
				ProtocolForm.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		q.setParameter("protocolFormType", protocolFormType);
		q.setParameter("protocolFormStatues", protocolFormStatues);
	
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public ProtocolForm getLatestProtocolFormByProtocolIdAndProtocolFormType(long protocolId, ProtocolFormType protocolFormType){
		String query = "SELECT pf FROM ProtocolForm pf "
			+ " WHERE pf.protocol.id = :protocolId AND pf.protocolFormType = :protocolFormType AND pf.retired = :retired "
			+ " ORDER BY pf.id DESC ";

		TypedQuery<ProtocolForm> q = getEntityManager().createQuery(query,
				ProtocolForm.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		q.setParameter("protocolFormType", protocolFormType);
	
		return q.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolForm> listParentProtocolFormsByProtocolFormType(ProtocolFormType protocolFormType){
		String query = "SELECT pf FROM ProtocolForm pf "
				+ " WHERE pf.parent.id = pf.id AND pf.protocolFormType = :protocolFormType AND pf.retired = :retired";
		TypedQuery<ProtocolForm> q = getEntityManager()
				.createQuery(query, ProtocolForm.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormType", protocolFormType);
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolForm> listProtocolFormsByProtocolIdAndProtocolFormType(
			long protocolId, ProtocolFormType protocolFormType) {

		String query = "SELECT pf FROM ProtocolForm pf "
			+ " WHERE pf.protocol.id = :protocolId AND pf.protocolFormType = :protocolFormType AND pf.retired = :retired";

		TypedQuery<ProtocolForm> q = getEntityManager()
				.createQuery(query, ProtocolForm.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		q.setParameter("protocolFormType", protocolFormType);
		return q.getResultList();
	}
	
	@Transactional(readOnly = true)
	public List<ProtocolForm> listProtocolFormsByProtocolId(
			long protocolId) {

		String query = "SELECT pf FROM ProtocolForm pf "
			+ " WHERE pf.protocol.id = :protocolId AND pf.retired = :retired";

		TypedQuery<ProtocolForm> q = getEntityManager()
				.createQuery(query, ProtocolForm.class);

		q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		return q.getResultList();
	}

	public List<ProtocolForm> getProtocolFormByProtocolIdOnly(long protocolId){
		String query = "SELECT pf FROM ProtocolForm pf "
			+ " WHERE pf.protocol.id = :protocolId AND pf.retired = :retired";

		TypedQuery<ProtocolForm> q = getEntityManager().createQuery(query,
				ProtocolForm.class);
		
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
	
		return q.getResultList();

	}
	
	public ProtocolForm getLatestProtocolFormByProtocolIdOnly(long protocolId){
		String query = "SELECT pf FROM ProtocolForm pf "
			+ " WHERE pf.protocol.id = :protocolId AND pf.retired = :retired ORDER BY pf.id DESC";

		TypedQuery<ProtocolForm> q = getEntityManager().createQuery(query,
				ProtocolForm.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
	
		q.setHint("org.hibernate.cacheable", true);
		
		return q.getSingleResult();

	}
	
}
