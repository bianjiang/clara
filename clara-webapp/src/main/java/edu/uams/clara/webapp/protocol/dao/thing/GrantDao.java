package edu.uams.clara.webapp.protocol.dao.thing;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.protocol.domain.thing.Grant;

@Repository
public class GrantDao extends AbstractDomainDao<Grant> {

	/**
	 * 
	 */

	private static final long serialVersionUID = -1213097439919063716L;

	/**
	 * 
	 * @param PRN
	 * @return
	 */
	@Transactional(readOnly = true)
	public Grant findAwardedGrantByPRN(String prn) {

		String qry = "SELECT click_grant.* FROM click_grant WHERE click_grant.retired = :retired AND SUBSTRING(click_grant.prn, PATINDEX('%[1-9]%', click_grant.prn), len(click_grant.prn)) LIKE :prn AND click_grant.status = :status";

		TypedQuery<Grant> query = (TypedQuery<Grant>)getEntityManager()
				.createNativeQuery(qry, Grant.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("prn", prn)
				.setParameter("status", "Awarded");

		//query.setHint("org.hibernate.cacheable", true);

		return query.getSingleResult();
		
		
	}

	@Transactional(readOnly = true)
	public Grant findGrantByPRN(String PRN) {

		String qry = "SELECT g FROM Grant g WHERE g.retired = :retired AND g.prn LIKE :PRN";

		TypedQuery<Grant> query = getEntityManager()
				.createQuery(qry, Grant.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("PRN", PRN);

		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		
		return query.getSingleResult();
	}

}
