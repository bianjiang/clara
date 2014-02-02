package edu.uams.clara.core.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Repository
public abstract class AbstractDomainDao<T extends AbstractDomainEntity>
		implements Serializable {

	private final static Logger logger = LoggerFactory
			.getLogger(AbstractDomainDao.class);

	private static final long serialVersionUID = 458010749215961765L;

	protected Class<T> domainClass;

	@SuppressWarnings("unchecked")
	public AbstractDomainDao() {

		this.domainClass = (Class<T>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public String getDomainClassName() {
		return domainClass.getName();
	}

	private EntityManager em;

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEntityManger(EntityManager em) {
		this.em = em;
	}

	public EntityManager getEntityManager() {
		return em;
	}

	public T createById(long id) throws InstantiationException,
			IllegalAccessException {
		T domainObject = null;
		if (id > 0) {
			domainObject = domainClass.newInstance();
			domainObject.setId(id);
		} else {
			domainObject = null;
		}
		return domainObject;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<T> findAll() {
		String query = "from " + getDomainClassName()
				+ " d where d.retired = :retired";
		Query q = em.createQuery(query);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		return (List<T>) q.getResultList();
	}

	@Transactional(readOnly = true)
	public T findById(long id) {
		/*
		 * String query = "from " + getDomainClassName() +
		 * " d where d.id = :id and d.retired = :retired"; Query q =
		 * em.createQuery(query); q.setHint("org.hibernate.cacheable", true);
		 * q.setParameter("id", id); q.setParameter("retired", Boolean.FALSE);
		 * return (T) q.getSingleResult();
		 */
		return em.find(domainClass, id);
	}

	/**
	 * if save or update the passed in object and return and attached object,
	 * the original object is not attached!!!! ref:
	 * http://blog.xebia.com/2009/03
	 * /23/jpa-implementation-patterns-saving-detached-entities/ Let's save if
	 * you do, user = userDao.saveOrUdate(u); here u is not attached, but the
	 * return user is !
	 * 
	 * @param domainObject
	 * @return
	 */
	@Transactional
	public T saveOrUpdate(T domainObject) {

		// em.persist(domainObject);
		// replace domainObject with an attached
		if (domainObject.getId() > 0) {
			domainObject = em.merge(domainObject);
		} else {
			em.persist(domainObject);
		}

		// em.flush();

		return domainObject;
	}

	@Transactional(readOnly = true)
	public void refresh(final T domainObject) {
		em.refresh(domainObject);
	}

	@Transactional(readOnly = true)
	public long count() {
		return (Long) em
				.createQuery(
						"select count(*) from " + domainClass.getName()
								+ " d where d.retired = :retired")
				.setParameter("retired", Boolean.FALSE).getSingleResult();
	}

	public void remove(T domainObject) {
		em.remove(domainObject);
	}

	

}
