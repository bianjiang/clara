package edu.uams.clara.core.dao.thing;

import java.util.List;

import javax.persistence.Query;

import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.core.domain.thing.AbstractThing;

public class AbstractThingDao<T extends AbstractThing> extends
		AbstractDomainDao<T> {

	private static final long serialVersionUID = -256074469898692740L;

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<T> listThings() {

		Query query = getEntityManager().createQuery(
				"SELECT o FROM " + this.getDomainClassName()
						+ " o WHERE o.retired = :retired ORDER BY value ASC")
				.setParameter("retired", Boolean.FALSE);

		return (List<T>) query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<T> listUnapprovedThingsByType() {

		Query query = getEntityManager().createQuery(
				"SELECT o FROM " + this.getDomainClassName()
						+ " o WHERE o.retired = :retired" 
						+ " AND o.isApproved = 0 ORDER BY value ASC")
				.setParameter("retired", Boolean.FALSE);

		return (List<T>) query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<T> searchByName(String name) {

		Query query = getEntityManager()
				.createQuery(
						"SELECT o FROM "
								+ this.getDomainClassName()
								+ " o WHERE o.retired = :retired AND o.description LIKE :name  ORDER BY value ASC")
				.setParameter("retired", Boolean.FALSE)
				.setParameter("name", "%" + name + "%");

		return (List<T>) query.getResultList();
	}
}
