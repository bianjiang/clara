package edu.uams.clara.webapp.common.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.dao.usercontext.PersonDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserRoleDao;
import edu.uams.clara.webapp.common.domain.usercontext.Person;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.User.UserType;
import edu.uams.clara.webapp.common.service.EmailService;
import edu.uams.clara.webapp.common.service.LdapPersonLookupService;
import edu.uams.clara.webapp.common.service.UserService;

public class UserServiceImpl implements UserService {

	private final static Logger logger = LoggerFactory
			.getLogger(UserService.class);

	private LdapPersonLookupService ldapPersonLookupService;
	private PersonDao personDao;
	private UserDao userDao;
	private UserRoleDao userRoleDao;
	private Md5PasswordEncoder md5PasswordEncoder;
	private EmailService emailService;

	@Value("${application.host}")
	private String applicationHost;

	public UserServiceImpl() {

	}

	@Override
	public User getUserByUsername(String username) {
		return userDao.getUserByUsername(username);
	}

	@Override
	public User getAndUpdateUserByUsername(String username, boolean create) {
		User u = userDao.getUserByUsername(username);

		// user doesn't exist in database...haven't registered with the
		// system...
		if (u == null) {
			logger.debug("user doesn't exist in database");
			if (create) {
				Person ldapPerson = ldapPersonLookupService
						.getPersonByUsername(username);

				if (ldapPerson != null) {
					u = new User();
					u.setUsername(ldapPerson.getUsername());
					u.setPerson(ldapPerson);
					u.setUserType(UserType.LDAP_USER);
					u.setAccountNonExpired(true);
					u.setAccountNonLocked(true);
					u.setCredentialsNonExpired(true);
					u.setEnabled(true);

					u = userDao.saveOrUpdate(u);
					/*
					 * UserRole ur = new UserRole(); ur.setUser(u);
					 * ur.setRole(null); u.getUserRoles().add(ur); ur =
					 * userRoleDao.saveOrUpdate(ur);
					 */
				}
			}

			return u;
		} else {

			if (u.getUserType().equals(User.UserType.LDAP_USER)) {

				// update local user database, if the user is a ldap user...
				Person ldapPerson = ldapPersonLookupService
						.getPersonByUsername(username);

				logger.debug("user: " + u.getPerson().getLastname() + ", "
						+ u.getPerson().getFirstname());
				if (ldapPerson != null) {
					logger.debug("ldap person not null; username: "
							+ ldapPerson.getUsername() + "; "
							+ ldapPerson.getLastname() + ", "
							+ ldapPerson.getFirstname());

					u.getPerson().setDepartment(ldapPerson.getDepartment());
					u.getPerson().setEmail(ldapPerson.getEmail());
					u.getPerson().setFirstname(ldapPerson.getFirstname());
					u.getPerson().setLastname(ldapPerson.getLastname());
					u.getPerson().setJobTitle(ldapPerson.getJobTitle());
					u.getPerson().setMiddlename(ldapPerson.getMiddlename());
					u.getPerson().setSap(ldapPerson.getSap());
					u.getPerson().setWorkphone(ldapPerson.getWorkphone());
					u = userDao.saveOrUpdate(u);
				}
			}
		}

		return u;
	}

	@Override
	public User getAndUpdateUserBySap(String sap, boolean create) {
		User u = null;
		if (userDao.getUserBySAP(sap).size() > 0)
			u = userDao.getUserBySAP(sap).get(0);
		// user doesn't exist in database...haven't registered with the
		// system...
		if (u == null) {
			logger.debug("user doesn't exist in database");
			if (create) {
				Person ldapPerson = ldapPersonLookupService.getPersonBySAP(sap);

				if (ldapPerson != null) {
					u = new User();
					u.setUsername(ldapPerson.getUsername());
					u.setPerson(ldapPerson);
					u.setUserType(UserType.LDAP_USER);
					u.setAccountNonExpired(true);
					u.setAccountNonLocked(true);
					u.setCredentialsNonExpired(true);
					u.setEnabled(true);

					u = userDao.saveOrUpdate(u);
					/*
					 * UserRole ur = new UserRole(); ur.setUser(u);
					 * ur.setRole(null); u.getUserRoles().add(ur); ur =
					 * userRoleDao.saveOrUpdate(ur);
					 */
				}
			}

			return u;
		} else {

			if (u.getUserType().equals(User.UserType.LDAP_USER)) {

				// update local user database, if the user is a ldap user...
				Person ldapPerson = ldapPersonLookupService.getPersonBySAP(sap);

				logger.debug("user: " + u.getPerson().getLastname() + ", "
						+ u.getPerson().getFirstname());
				if (ldapPerson != null) {
					logger.debug("ldap person not null; username: "
							+ ldapPerson.getUsername() + "; "
							+ ldapPerson.getLastname() + ", "
							+ ldapPerson.getFirstname());

					u.getPerson().setDepartment(ldapPerson.getDepartment());
					u.getPerson().setEmail(ldapPerson.getEmail());
					u.getPerson().setFirstname(ldapPerson.getFirstname());
					u.getPerson().setLastname(ldapPerson.getLastname());
					u.getPerson().setJobTitle(ldapPerson.getJobTitle());
					u.getPerson().setMiddlename(ldapPerson.getMiddlename());
					u.getPerson().setSap(ldapPerson.getSap());
					u.getPerson().setWorkphone(ldapPerson.getWorkphone());
					u = userDao.saveOrUpdate(u);
				}
			}
		}

		return u;
	}

	@Override
	public User getCurrentUser() {
		/*
		 * String username = ""; try { username =
		 * SecurityContextHolder.getContext().getAuthentication() .getName(); }
		 * catch (Exception ex) { return null; } return
		 * userDao.getUserByUsername(username);
		 */
		return (User) ((SecurityContext) SecurityContextHolder.getContext())
				.getAuthentication().getPrincipal();

	}

	/**
	 * This function merges the result from daoPersons and from ldapPersons
	 * still ldap gives false result if the same user has two different email addresses... e.g., GubbinsPaulO; sap: 7643 and GubbinsPaul; sap: 
	 * One way is to filter out anyone who don't have a sap id, but a lot of students don't either...
	 * Need to figure out a different ldap query
	 */
	@Override
	public List<Person> searchForPersons(String keyword) {
		// searchForPersons might give exception
		List<Person> daoPersons = Lists.newArrayList();
		try {
			daoPersons = personDao.searchForPersons(keyword);
		} catch (Exception ex) {
			// don't care
			ex.printStackTrace();
		}
		List<Person> ldapPersons = null;
		try {
			ldapPersons = ldapPersonLookupService.searchForPersons(keyword);
		} catch (Exception ex) {
			// don't care
			ex.printStackTrace();
		}

		// dao user shows up first
		List<Person> results = daoPersons;

		if (ldapPersons != null) {
			results.addAll(ldapPersons);
		}

		Set<String> saps = Sets.newHashSet();
		Set<String> usernames = Sets.newHashSet();
		Iterator<Person> pIt = results.iterator();
		while (pIt.hasNext()) {
			Person p = pIt.next();

			if (p == null || (p.getSap() != null && !p.getSap().trim().isEmpty() && saps.contains(p.getSap()))
					|| usernames.contains(p.getUsername())) {
				pIt.remove();
			}
			if (p != null && p.getSap() != null && !p.getSap().trim().isEmpty()) {
				saps.add(p.getSap());
			}
			if (p != null && p.getUsername() != null) {
				usernames.add(p.getUsername());
			}
		}

		return results;
	}

	@Override
	public void createExternalUser(String username, String firstname,
			String lastname, String phone, String email, String department) {
		String password = RandomStringUtils.random(8, true, true);

		try {
			if (personDao.getPersonByUsername(username) != null) {
				logger.debug("The username already exists!  Please change to other uasername!");
			}

			if (personDao.getPersonByFirstNameAndLastName(firstname, lastname) != null) {
				logger.debug("The user already exists!");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		Person person = new Person();

		person.setDepartment(department);
		person.setEmail(email);
		person.setFirstname(firstname);
		person.setJobTitle("");
		person.setLastname(lastname);
		person.setMiddlename("");
		person.setSap("");
		person.setUsername(username);
		person.setWorkphone(phone);

		personDao.saveOrUpdate(person);

		User u = new User();
		Person p = personDao.getPersonByUsername(username);

		u.setUsername(p.getUsername());
		u.setUserType(UserType.DATABASE_USER);
		u.setAccountNonExpired(true);
		u.setAccountNonLocked(true);
		u.setCredentialsNonExpired(true);
		u.setPassword(md5PasswordEncoder.encodePassword(password, username));
		u.setEnabled(true);
		u.setTrained(false);

		u = userDao.saveOrUpdate(u);
		User u2 = new User();
		u2 = userDao.getUserByUsername(username);
		u2.setPerson(p);
		userDao.saveOrUpdate(u2);

		String emailText = "<html><head><link href=\"/clara-webapp/static/styles/letters.css\" media=\"screen\" type=\"text/css\" rel=\"stylesheet\"/></head><body>";
		emailText += "<div class=\"email-template\">";
		emailText += "<br/>Your account has been created in CLARA by System Admin.<br/><br/>Username: "
				+ username
				+ "<br/>Temporary password: "
				+ password
				+ ".<br/><br/>You can click the following link to access CLARA and reset password:<br/><a href=\""
				+ applicationHost + "\">CLARA</a>";
		emailText += "</div></body></html>";

		List<String> mailTo = new ArrayList<String>();
		mailTo.add(email);

		String subject = "New CLARA Account";

		emailService.sendEmail(emailText, mailTo, null, subject, null);

	}
	
	@Override
	public User getUserByUserId(long userId) {
		return userDao.findById(userId);
	}

	@Override
	public User getUserByEmail(String email) {
		try {
			User user = userDao.getUserByEmail(email);
			
			return user;
		} catch (Exception e) {
			return null;
		}
	}
	
	private Set<String> profileXpathSet = Sets.newHashSet();{
		//profileXpathSet.add("/metadata/citi-training-complete");
		profileXpathSet.add("/metadata/notes");
		profileXpathSet.add("/metadata/citi-training-expiredate");
	}
	
	@Override
	public Map<String, String> getUserProfileInfo(String profile) {
		Map<String, String> result = Maps.newHashMap();
		
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			
			Map<String, String> values = xmlHandler.getFirstStringValuesByXPaths(profile, profileXpathSet);
			
			result.put("citiTrainingExpiredate", values.get("/metadata/citi-training-expiredate"));
			//result.put("citiTrainingComplete", values.get("/metadata/citi-training-complete"));
			result.put("notes", values.get("/metadata/notes"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Autowired(required = false)
	public void setLdapPersonLookupService(
			LdapPersonLookupService ldapPersonLookupService) {
		this.ldapPersonLookupService = ldapPersonLookupService;
	}

	public LdapPersonLookupService getLdapPersonLookupService() {
		return ldapPersonLookupService;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setPersonDao(PersonDao personDao) {
		this.personDao = personDao;
	}

	public PersonDao getPersonDao() {
		return personDao;
	}

	@Autowired(required = true)
	public void setUserRoleDao(UserRoleDao userRoleDao) {
		this.userRoleDao = userRoleDao;
	}

	public UserRoleDao getUserRoleDao() {
		return userRoleDao;
	}

	public Md5PasswordEncoder getMd5PasswordEncoder() {
		return md5PasswordEncoder;
	}

	@Autowired(required = true)
	public void setMd5PasswordEncoder(Md5PasswordEncoder md5PasswordEncoder) {
		this.md5PasswordEncoder = md5PasswordEncoder;
	}

	public EmailService getEmailService() {
		return emailService;
	}

	@Autowired(required = true)
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

}
