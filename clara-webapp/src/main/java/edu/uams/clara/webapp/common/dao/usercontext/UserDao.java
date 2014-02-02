package edu.uams.clara.webapp.common.dao.usercontext;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.CitiMember;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;

@Repository
public class UserDao extends AbstractDomainDao<User> {

	private final static Logger logger = LoggerFactory.getLogger(UserDao.class);

	private static final long serialVersionUID = 7916848425599894270L;

	/**
	 * get user from the database by username, if it doesn't exist, return null
	 * 
	 * @param username
	 * @return User
	 */
	@Transactional(readOnly = true)
	public User getUserByUsername(String username) {

		TypedQuery<User> query = getEntityManager()
				.createQuery(
						"SELECT u FROM User u WHERE u.retired = :retired AND LOWER(u.username) LIKE :username",
						User.class).setParameter("retired", Boolean.FALSE)
				.setParameter("username", username.toLowerCase());
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		User user = null;

		try {
			user = query.getSingleResult();
		} catch (Exception ex) {
			// do nothing, return null;
			//logger.debug(ex.getMessage());
			// ex.printStackTrace();
		}
		return user;
	}

	@Transactional(readOnly = true)
	public User getUserByEmail(String email) {

		TypedQuery<User> query = getEntityManager()
				.createQuery(
						"SELECT u FROM User u, Person p WHERE u.retired = :retired AND u.person.id = p.id AND p.retired = :retired AND p.email = :email ORDER BY p.id DESC",
						User.class).setParameter("retired", Boolean.FALSE)
				.setParameter("email", email);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		User user = null;

		try {
			user = query.getSingleResult();
		} catch (Exception ex) {
			// do nothing, return null;
			//logger.debug(ex.getMessage());
			// ex.printStackTrace();
		}
		return user;
	}

	/**
	 * get user from the database by role, if it doesn't exist, return null
	 * 
	 * @param username
	 * @return User
	 */
	@Transactional(readOnly = true)
	public List<User> getUsersByUserRole(Permission rolePermissionIdentifier) {

		TypedQuery<User> query = getEntityManager()
				.createQuery(
						"SELECT ur.user FROM UserRole ur WHERE ur.role.rolePermissionIdentifier =:rolePermissionIdentifier "
								+ "AND ur.user.retired = :retired AND ur.retired = :retired",
						User.class)
				.setParameter("retired", Boolean.FALSE)
				.setParameter("rolePermissionIdentifier",
						rolePermissionIdentifier);
		query.setHint("org.hibernate.cacheable", true);

		List<User> users = null;
		try {
			users = query.getResultList();
		} catch (Exception ex) {
			// do nothing, return null;
			logger.debug(ex.getMessage());
			// ex.printStackTrace();
		}
		return users;
	}

	/**
	 * get user from the database by sap, if it doesn't exist, return null
	 * 
	 * @param username
	 * @return User
	 */
	@Transactional(readOnly = true)
	public List<User> getUserBySAP(String sap) {
		if (sap == null || sap.isEmpty())
			return null;

		TypedQuery<User> query = getEntityManager()
				.createQuery(
						"SELECT u FROM User u WHERE u.retired = :retired AND u.person.sap LIKE :sap",
						User.class).setParameter("retired", Boolean.FALSE)
				.setParameter("sap", sap);
		query.setHint("org.hibernate.cacheable", true);
		List<User> user = null;
		try {
			user = query.getResultList();
		} catch (NoResultException ex) {
			// do nothing, return null;
		}
		return user;
	}

	/**
	 * get user info from citiMember, if it doesn't exist, return null
	 * 
	 * @param UserName
	 * @return CitiMember
	 */
	@Transactional(readOnly = true)
	public CitiMember getMapedCitiMember(String username) {

		TypedQuery<CitiMember> query = getEntityManager()
				.createQuery(
						"SELECT c FROM CitiMember c, User u WHERE u.retired = :retired AND c.retired = :retired AND c.employeeNumber = u.person.sap AND LOWER(u.username) LIKE :username AND c.emailAddress = u.person.email",
						CitiMember.class);
		query.setHint("org.hibernate.cacheable", true);
		query.setParameter("retired", Boolean.FALSE);
		query.setParameter("username", username.toLowerCase());
		CitiMember citiMember = null;

		try {
			citiMember = query.getSingleResult();
		} catch (Exception ex) {
			// do nothing, return null;
			logger.debug(ex.getMessage());
			// ex.printStackTrace();
		}

		return citiMember;

	}
	

	public User findBySSN(String ssn) {
		String sql = "SELECT u FROM User u, SAPUser s WHERE u.retired = :retired AND s.retired = :retired AND u.person.sap = s.sap AND s.ssn = :ssn";

		TypedQuery<User> query = getEntityManager()
				.createQuery(sql, User.class);
		query.setHint("org.hibernate.cacheable", true);
		query.setParameter("retired", Boolean.FALSE);
		query.setParameter("ssn", ssn);

		User user = null;

		try {
			user = query.getSingleResult();
		} catch (Exception ex) {
			// do nothing, return null;
			logger.debug(ex.getMessage());
			// ex.printStackTrace();
		}
		return user;
	}


}
