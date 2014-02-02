package edu.uams.clara.webapp.common.dao.usercontext;

import java.util.List;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.Role;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;

@Repository
public class RoleDao extends AbstractDomainDao<Role> {

	private static final long serialVersionUID = 7001284849089850321L;

	private final static Logger logger = LoggerFactory
	.getLogger(RoleDao.class);
	
	@Transactional(readOnly=true)
	public List<Role> listAllOrderByName(){
		TypedQuery<Role> query = getEntityManager()
		.createQuery(
				"SELECT r FROM Role r WHERE r.retired = :retired ORDER BY r.name ASC", Role.class)
		.setParameter("retired", Boolean.FALSE);
		query.setHint("org.hibernate.cacheable", true);
		
		return query.getResultList();
	}
	
	@Transactional(readOnly=true)
	public List<Role> searchForRoles(String keyword){
		TypedQuery<Role> query = getEntityManager()
		.createQuery(
				"SELECT r FROM Role r WHERE r.retired = :retired AND r.displayName LIKE :keyword", Role.class)
		.setParameter("retired", Boolean.FALSE).setParameter(
				"keyword", "%" + keyword + "%");
		query.setHint("org.hibernate.cacheable", true);
		
		return query.getResultList();
	}
	
	@Transactional(readOnly=true)
	public Role getRoleByCommittee(Committee committee) {

		TypedQuery<Role> query = getEntityManager()
				.createQuery(
						"SELECT r FROM Role r WHERE r.retired = :retired AND r.committee = :committee", Role.class)
				.setParameter("retired", Boolean.FALSE).setParameter(
						"committee", committee);
		query.setHint("org.hibernate.cacheable", true);
		
		return query.getSingleResult();
	}
}
