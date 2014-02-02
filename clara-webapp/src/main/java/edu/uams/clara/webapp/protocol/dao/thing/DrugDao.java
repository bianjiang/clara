package edu.uams.clara.webapp.protocol.dao.thing;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.thing.AbstractThingDao;
import edu.uams.clara.webapp.protocol.domain.thing.Drug;

@Repository
public class DrugDao extends AbstractThingDao<Drug> {

	private static final long serialVersionUID = -6136838851771320731L;

	private final static Logger logger = LoggerFactory.getLogger(DrugDao.class);

	@Transactional(readOnly = true)
	public Drug findByIdentifier(String identifier) {
		TypedQuery<Drug> query = getEntityManager()
				.createQuery(
						"SELECT t FROM Drug t WHERE t.retired = :retired AND t.type LIKE :type AND t.identifier LIKE :identifier",
						Drug.class).setParameter("retired", Boolean.FALSE)
				.setParameter("identifier", identifier)
		        .setParameter("type", "DRUG");
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);

		Drug drug = null;
		try {
			drug = query.getSingleResult();
		} catch (Exception ex) {
			//logger.warn(ex.getMessage());
		}

		return drug;

	}

}
