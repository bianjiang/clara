package edu.uams.clara.core.dao.thing;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.core.domain.thing.Thing;

@Repository
public class ThingDao extends AbstractDomainDao<Thing> {

	private static final long serialVersionUID = 1182253649760152363L;

	@Transactional(readOnly = true)
	public Thing findByIdentifierAndType(String identifier, String type){
		String qryString = "SELECT t FROM Thing t WHERE t.retired = :retired AND t.type = :type AND t.identifier LIKE :identifier";

		TypedQuery<Thing> query = getEntityManager().createQuery(qryString,
				Thing.class);
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
	
	@Transactional(readOnly = true)
	public List<Thing> searchByKeywordsAndType(String keyword, String type) {

		String qryString = "SELECT t FROM Thing t"
				+ " WHERE t.retired = :retired"
				+ " AND t.type = :type AND (t.identifier LIKE :keyword OR t.description LIKE :keyword)";

		TypedQuery<Thing> query = getEntityManager().createQuery(qryString,
				Thing.class);
		query.setParameter("retired", Boolean.FALSE);
		query.setParameter("type", type);
		query.setParameter("keyword", "%" + keyword + "%");
		query.setHint("org.hibernate.cacheable", true);

		try {
			return query.getResultList();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
			// logger.warn(ex.getMessage());
		}

	}
	
	@Transactional(readOnly = true)
	public List<Thing> searchByKeywordsAndTypes(String keyword, List<String> types) {

		String qryString = "SELECT t FROM Thing t"
				+ " WHERE t.retired = :retired"
				+ " AND t.type IN :types AND (t.identifier LIKE :keyword OR t.description LIKE :keyword)";

		TypedQuery<Thing> query = getEntityManager().createQuery(qryString,
				Thing.class);
		query.setParameter("retired", Boolean.FALSE);
		query.setParameter("types", types);
		query.setParameter("keyword", "%" + keyword + "%");
		query.setHint("org.hibernate.cacheable", true);

		try {
			return query.getResultList();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
			// logger.warn(ex.getMessage());
		}

	}
	
	@Transactional(readOnly = true)
	public List<Thing> searchByTypes (List<String> types) {
		String qryString = "SELECT t FROM Thing t"
				+ " WHERE t.retired = :retired"
				+ " AND t.type IN :types";

		TypedQuery<Thing> query = getEntityManager().createQuery(qryString,
				Thing.class);
		query.setParameter("retired", Boolean.FALSE);
		query.setParameter("types", types);
		query.setHint("org.hibernate.cacheable", true);

		try {
			return query.getResultList();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
			// logger.warn(ex.getMessage());
		}
	}
}
