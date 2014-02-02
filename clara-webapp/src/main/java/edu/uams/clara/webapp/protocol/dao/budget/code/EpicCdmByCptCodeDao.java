package edu.uams.clara.webapp.protocol.dao.budget.code;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository
public class EpicCdmByCptCodeDao {
	private EntityManager em;

	@Transactional(readOnly = true)
	public String getEpicCdmByCptCode(String cptCode) {
		String qry = "SELECT epic_cdm_code FROM epic_cdm WHERE default_cpt_code = :cptCode";

		Query q = em.createNativeQuery(qry);

		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("cptCode", cptCode);

		q.setFirstResult(0);
		q.setMaxResults(1);

		return q.getSingleResult().toString();
	}

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

}
