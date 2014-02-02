package edu.uams.clara.webapp.protocol.dao.budget.code;


import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.budget.code.PhysicianLocationCode;

@Repository
public class PhysicianLocationCodeDao  extends AbstractDomainDao<PhysicianLocationCode> {

	/**
	 * 
	 */
	
	private final static Logger logger = LoggerFactory
			.getLogger(PhysicianLocationCodeDao.class);
			
	private static final long serialVersionUID = -1495644602453596523L;
	public PhysicianLocationCode findByLocationCode(String locationCode){
		 
		String query = "SELECT lc FROM PhysicianLocationCode lc "
			+ " WHERE lc.retired = :retired AND lc.code LIKE :locationCode ";
		
		TypedQuery<PhysicianLocationCode> q = getEntityManager().createQuery(query,
				PhysicianLocationCode.class);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("locationCode", locationCode);
		
		q.setFirstResult(0);
		q.setMaxResults(1);
		
		PhysicianLocationCode physicianLocationCode = null;
		try{
			physicianLocationCode = q.getSingleResult();
		}catch(Exception ex){
			logger.warn(ex.getMessage());
		}
		
		return physicianLocationCode;
	}
}
