package edu.uams.clara.core.dao.thing.etl;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.core.domain.thing.etl.ThingUpdate;


@Repository
public class ThingUpdateDao extends AbstractDomainDao<ThingUpdate> {


	/**
	 * 
	 */
	private static final long serialVersionUID = -2895913018290633767L;

	@Transactional(readOnly = true)
	public ThingUpdate findByIdentifierAndType(String identifier, String type){
		String qryString = "SELECT t FROM ThingUpdate t WHERE t.retired = :retired AND t.type = :type AND t.identifier LIKE :identifier";

		TypedQuery<ThingUpdate> query = getEntityManager().createQuery(qryString,
				ThingUpdate.class);
		query.setParameter("retired", Boolean.FALSE);
		query.setParameter("type", type);
		query.setParameter("identifier", identifier);
		query.setFirstResult(0);
		query.setMaxResults(1);
		query.setHint("org.hibernate.cacheable", true);

		try {
			return query.getSingleResult();
		} catch (Exception ex) {
			return null;
			// logger.warn(ex.getMessage());
		}
	}
	
}
