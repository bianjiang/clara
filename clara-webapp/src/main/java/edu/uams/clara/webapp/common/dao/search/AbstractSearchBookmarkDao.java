package edu.uams.clara.webapp.common.dao.search;

import java.util.List;

import javax.persistence.Query;

import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.search.AbstractSearchBookmark;

public class AbstractSearchBookmarkDao<T extends AbstractSearchBookmark>
		extends AbstractDomainDao<T> {

	private static final long serialVersionUID = -1076018768317096640L;

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<T> listSearchBookmarks() {

		Query query = getEntityManager().createQuery(
				"SELECT o FROM " + this.getDomainClassName()
						+ " o WHERE o.retired = :retired ORDER BY name ASC")
				.setParameter("retired", Boolean.FALSE);

		return (List<T>) query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<T> listSearchBookmarksByUserId(long userId) {

		Query query = getEntityManager().createQuery(
				"SELECT o FROM " + this.getDomainClassName()
						+ " o WHERE o.retired = :retired AND o.user.id = :userId ORDER BY name ASC")
				.setParameter("retired", Boolean.FALSE)
				.setParameter("userId", userId);

		return (List<T>) query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<T> searchByName(String name) {

		Query query = getEntityManager()
				.createQuery(
						"SELECT o FROM "
								+ this.getDomainClassName()
								+ " o WHERE o.retired = :retired AND o.name LIKE :name  ORDER BY name ASC")
				.setParameter("retired", Boolean.FALSE)
				.setParameter("name", "%" + name + "%");

		return (List<T>) query.getResultList();

	}
	
	
}
