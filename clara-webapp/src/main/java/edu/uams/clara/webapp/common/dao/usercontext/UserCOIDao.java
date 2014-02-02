package edu.uams.clara.webapp.common.dao.usercontext;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.UserCOI;
@Repository
public class UserCOIDao extends AbstractDomainDao<UserCOI>{

	/**
	 * 
	 */

	private final static Logger logger = LoggerFactory
			.getLogger(UserCOIDao.class);

	private EntityManager em;

	@Transactional(readOnly = true)
	public List<UserCOI> getUserCOIBySAP(String sapId) {
		TypedQuery<UserCOI> query = em.createQuery(
				"SELECT uc FROM UserCOI uc WHERE CAST(uc.sapId AS integer) = CAST(:sapId AS integer)",
				UserCOI.class).setParameter("sapId", sapId);
		query.setHint("org.hibernate.cacheable", true);
		logger.debug("getUserCOIBySAP("+sapId+"): Found "+query.getResultList().size()+" results.");

		return query.getResultList();
	}
	
	@Transactional(readOnly = true)
	public UserCOI getUserCOIBySAPAndDisclosure(String sapId,String disclosureName) {
		TypedQuery<UserCOI> query = em.createQuery(
				"SELECT uc FROM UserCOI uc WHERE CAST(uc.sapId AS integer) = CAST(:sapId AS integer) and disclosureName =:disclosureName",
				UserCOI.class).setParameter("sapId", sapId)
				.setParameter("disclosureName", disclosureName);
		query.setHint("org.hibernate.cacheable", true);
		
		return query.getSingleResult();
	}

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

}
