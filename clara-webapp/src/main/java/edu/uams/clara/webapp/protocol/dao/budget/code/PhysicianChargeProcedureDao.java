package edu.uams.clara.webapp.protocol.dao.budget.code;

import java.util.List;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.budget.code.PhysicianChargeProcedure;

@Repository
public class PhysicianChargeProcedureDao extends
		AbstractDomainDao<PhysicianChargeProcedure> {
	private final static Logger logger = LoggerFactory
			.getLogger(PhysicianChargeProcedureDao.class);
	private static final long serialVersionUID = -2760758017737621403L;

	public List<PhysicianChargeProcedure> findByCptCode(String cptCode) {

		String query = "SELECT pcp FROM PhysicianChargeProcedure pcp "
				+ " WHERE pcp.retired = :retired AND pcp.cptCode LIKE :cptCode ";

		TypedQuery<PhysicianChargeProcedure> q = getEntityManager()
				.createQuery(query, PhysicianChargeProcedure.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("cptCode", cptCode);

		return q.getResultList();
	}

	public PhysicianChargeProcedure findbywithMultiple(String cptCode,
			String tmCode, String locationCode) {

		String query = "SELECT pcp FROM PhysicianChargeProcedure pcp "
				+ " WHERE pcp.retired = :retired AND pcp.cptCode LIKE :cptCode AND pcp.tmCode LIKE :tmCode AND pcp.locationCode.code LIKE :locationCode";

		TypedQuery<PhysicianChargeProcedure> q = getEntityManager()
				.createQuery(query, PhysicianChargeProcedure.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("locationCode", locationCode);
		q.setParameter("cptCode", cptCode);
		q.setParameter("tmCode", tmCode);

		q.setFirstResult(0);
		q.setMaxResults(1);

		PhysicianChargeProcedure physicianChargeProcedure = null;
		try {
			physicianChargeProcedure = q.getSingleResult();
			logger.debug("success");
		} catch (Exception ex) {
		}

		return physicianChargeProcedure;
	}


}
