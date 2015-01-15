package edu.uams.clara.integration.outgoing.ctms.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.integration.outgoing.ctms.domain.AriaUser;

@Repository
public class AriaUserDao extends  AbstractDomainDao<AriaUser> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4414947085979873726L;

	private EntityManager entityManager;

	@Transactional(readOnly=true)
	public AriaUser findARIAUserByPiSerial (long piSerial){
		
		TypedQuery<AriaUser> query = getEntityManager()
				.createQuery(
						"SELECT a FROM AriaUser a WHERE a.retired = :retired AND a.piSerial = :piSerial", AriaUser.class);
		
		// q.setHint("org.hibernate.cacheable", true);
		query.setParameter("piSerial", piSerial);
		query.setParameter("retired", Boolean.FALSE);
		
		return query.getSingleResult();
	}
	
	public EntityManager getEntityManager() {
		return entityManager;
	}


	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
}
