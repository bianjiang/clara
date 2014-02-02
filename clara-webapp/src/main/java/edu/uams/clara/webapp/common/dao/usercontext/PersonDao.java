package edu.uams.clara.webapp.common.dao.usercontext;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.Person;

@Repository
public class PersonDao extends AbstractDomainDao<Person> {
	private final static Logger logger = LoggerFactory
			.getLogger(PersonDao.class);

	private static final long serialVersionUID = 7916848425599894270L;

	/**
	 * get person from the database by username, if it doesn't exist, return
	 * null
	 * 
	 * @param username
	 * @return Person
	 */
	@Transactional(readOnly = true)
	public Person getPersonByUsername(String username) {

		TypedQuery<Person> query = getEntityManager()
				.createQuery(
						"SELECT p FROM Person p WHERE p.retired = :retired AND p.username LIKE :username",
						Person.class).setParameter("retired", Boolean.FALSE)
				.setParameter("username", username);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		Person person = null;
		try {
			person = query.getSingleResult();
		} catch (NoResultException ex) {
			// do nothing, return null;
		}
		return person;
	}
	
	@Transactional(readOnly = true)
	public Person getPersonByName(String firstname, String lastname, String middlename) {

		TypedQuery<Person> query = getEntityManager()
				.createQuery(
						"SELECT p FROM Person p WHERE p.retired = :retired AND p.firstname = :firstname AND p.lastname =:lastname AND p.middlename = :middlename",
						Person.class).setParameter("retired", Boolean.FALSE)
				.setParameter("firstname", firstname)
				.setParameter("lastname", lastname)
				.setParameter("middlename", middlename);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		Person person = null;
		try {
			person = query.getSingleResult();
		} catch (NoResultException ex) {
			// do nothing, return null;
		}
		return person;
	}
	
	@Transactional(readOnly = true)
	public Person getPersonByFirstNameAndLastName(String firstname, String lastname) {

		TypedQuery<Person> query = getEntityManager()
				.createQuery(
						"SELECT p FROM Person p WHERE p.retired = :retired AND p.firstname = :firstname AND p.lastname =:lastname",
						Person.class).setParameter("retired", Boolean.FALSE)
				.setParameter("firstname", firstname)
				.setParameter("lastname", lastname);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		Person person = null;
		try {
			person = query.getSingleResult();
		} catch (NoResultException ex) {
			// do nothing, return null;
		}
		return person;
	}

	@Transactional(readOnly = true)
	public List<Person> searchForPersons(String keyword) {

		TypedQuery<Person> query = getEntityManager()
				.createQuery(
						"SELECT p FROM Person p WHERE p.retired = :retired AND (p.firstname LIKE :keyword OR p.lastname LIKE :keyword OR p.email LIKE :keyword)",
						Person.class).setParameter("retired", Boolean.FALSE)
				.setParameter("keyword", keyword + "%");

		query.setHint("org.hibernate.cacheable", true);

		return query.getResultList();
	}

	/**
	 * get user from the database by sap, if it doesn't exist, return null
	 * 
	 * @param username
	 * @return User
	 */
	@Transactional(readOnly = true)
	public Person getPersonBySAP(String sap) {
		if (sap == null || sap.isEmpty())
			return null;

		TypedQuery<Person> query = getEntityManager()
				.createQuery(
						"SELECT p FROM Person p WHERE p.retired = :retired AND p.sap LIKE :sap",
						Person.class).setParameter("retired", Boolean.FALSE)
				.setParameter("sap", sap);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		Person p = null;
		try {
			p = query.getSingleResult();
		} catch (Exception e) {
			// do nothing, return null;
		}
		return p;
	}
	
	@Transactional(readOnly = true)
	public Person getPersonByEmail(String email) {
		if (email == null || email.isEmpty())
			return null;

		TypedQuery<Person> query = getEntityManager()
				.createQuery(
						"SELECT p FROM Person p WHERE p.retired = :retired AND p.email = :email",
						Person.class).setParameter("retired", Boolean.FALSE)
				.setParameter("email", email);
		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);
		Person p = null;
		try {
			p = query.getSingleResult();
		} catch (Exception e) {
			// do nothing, return null;
		}
		return p;
	}

	public Person getPersonBySSN(String ssn) {
		String sql = "SELECT p FROM Person p, SAPUser s WHERE p.retired = :retired AND s.retired = :retired AND p.sap = s.sap AND s.ssn = :ssn";

		TypedQuery<Person> query = getEntityManager().createQuery(sql,
				Person.class);
		query.setHint("org.hibernate.cacheable", true);
		query.setParameter("retired", Boolean.FALSE);
		query.setParameter("ssn", ssn);

		Person person = null;

		try {
			person = query.getSingleResult();
		} catch (Exception ex) {
			// do nothing, return null;
			logger.debug(ex.getMessage());
			// ex.printStackTrace();
		}
		return person;
	}
}
