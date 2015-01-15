package edu.uams.clara.webapp.protocol.dao.thing;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.objectwrapper.PagedList;
import edu.uams.clara.webapp.contract.domain.Contract;
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
		String qry = "SELECT click_grant.* FROM click_grant WHERE click_grant.retired = :retired AND SUBSTRING(click_grant.prn, PATINDEX('%[1-9]%', click_grant.prn), len(:prn)) LIKE :prn AND click_grant.status = :status order by click_grant.start_date desc ";

		TypedQuery<Grant> query = (TypedQuery<Grant>)getEntityManager()
				.createNativeQuery(qry, Grant.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("prn", prn)
				.setParameter("status", "Awarded");

		//query.setHint("org.hibernate.cacheable", true);

		return query.getResultList().get(0);
		
		
	}

	@Transactional(readOnly = true)
	public Grant findGrantByFullPRN(String PRN) {
		String qry = "SELECT g FROM Grant g WHERE g.retired = :retired AND g.fullprn = :PRN";

		TypedQuery<Grant> query = getEntityManager()
				.createQuery(qry, Grant.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("PRN", PRN);

		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		
		return query.getSingleResult();
	}
	
	@Transactional(readOnly = true)
	public List<Grant> listRelatedGrantByProtocolId(long protocolId) {

		String query = " SELECT c.* FROM click_grant c "
				+ " WHERE c.retired = :retired "
				+ " AND c.id IN (SELECT ro.related_object_id FROM related_object ro WHERE ro.object_id = :protocolId AND ro.object_type = 'protocol' AND ro.related_object_type = 'project' AND ro.retired = :retired GROUP BY ro.related_object_id UNION "
				+ " SELECT rlc.object_id FROM related_object rlc WHERE rlc.related_object_id = :protocolId AND rlc.object_type = 'project' AND rlc.related_object_type = 'protocol' AND rlc.retired = :retired GROUP BY rlc.object_id) ";

		TypedQuery<Grant> q = (TypedQuery<Grant>) getEntityManager()
				.createNativeQuery(query, Grant.class);
		// q.setHint("org.hibernate.cacheable", true);

		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);

		return q.getResultList();
	}

}
