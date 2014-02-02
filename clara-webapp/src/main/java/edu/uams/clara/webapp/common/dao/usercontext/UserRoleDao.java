package edu.uams.clara.webapp.common.dao.usercontext;

import java.util.List;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.UserRole;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;

@Repository
public class UserRoleDao extends AbstractDomainDao<UserRole>{

	private final static Logger logger = LoggerFactory
	.getLogger(UserRoleDao.class);
	
	private static final long serialVersionUID = 3022710363091104318L;

	@Transactional(readOnly=true)
	public List<UserRole> getUserRolesByUserId(long userId) {

		TypedQuery<UserRole> query = getEntityManager()
				.createQuery(
						"SELECT ur FROM UserRole ur WHERE ur.retired = :retired AND ur.user.id = :userId", UserRole.class)
				.setParameter("retired", Boolean.FALSE).setParameter(
						"userId", userId);
		query.setHint("org.hibernate.cacheable", true);
		return query.getResultList();
	}
	
	/**
	 * get user-roles from the database by roles
	 * @param rolePermissionIdentifiers
	 * @return List<UserRole>
	 */	
	@Transactional(readOnly=true)
	public List<UserRole> getUserRolesByUserRole(List<Permission> rolePermissionIdentifiers) {

		TypedQuery<UserRole> query = getEntityManager()
				.createQuery(
						"SELECT ur FROM UserRole ur WHERE ur.role.rolePermissionIdentifier IN :rolePermissionIdentifiers "
						+ "AND ur.user.retired = :retired AND ur.retired = :retired", UserRole.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("rolePermissionIdentifiers", rolePermissionIdentifiers);
		query.setHint("org.hibernate.cacheable", true);
		return query.getResultList();
	}
	
	
	@Transactional(readOnly=true)
	public UserRole getUserRolesByUserIdAndCommittee(long userId, Committee committee) {

		TypedQuery<UserRole> query = getEntityManager()
				.createQuery(
						"SELECT ur FROM UserRole ur WHERE ur.retired = :retired AND ur.user.id = :userId AND ur.role.committee = :committee", UserRole.class)
				.setParameter("retired", Boolean.FALSE).setParameter(
						"userId", userId).setParameter("committee", committee);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		return query.getSingleResult();
	}
	
	@Transactional(readOnly=true)
	public UserRole getUserRoleByUserIdAndRoleId(long userId, long roleId) {

		TypedQuery<UserRole> query = getEntityManager()
				.createQuery(
						"SELECT ur FROM UserRole ur WHERE ur.retired = :retired AND ur.user.id = :userId AND ur.role.id = :roleId", UserRole.class)
				.setParameter("retired", Boolean.FALSE).setParameter(
						"userId", userId).setParameter("roleId", roleId);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		return query.getSingleResult();
	}
}
