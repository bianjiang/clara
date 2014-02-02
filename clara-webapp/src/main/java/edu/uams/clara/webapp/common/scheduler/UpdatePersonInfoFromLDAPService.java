package edu.uams.clara.webapp.common.scheduler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uams.clara.webapp.common.dao.usercontext.PersonDao;
import edu.uams.clara.webapp.common.domain.usercontext.Person;
import edu.uams.clara.webapp.common.service.LdapPersonLookupService;

@Service
public class UpdatePersonInfoFromLDAPService {
	private final static Logger logger = LoggerFactory
			.getLogger(UpdatePersonInfoFromLDAPService.class);

	private PersonDao personDao;
	private LdapPersonLookupService ldapPersonLookupService;

	public void updatePersonFromLDAP() {
		List<Person> persons = personDao.findAll();
		/*List<Person> persons = Lists.newArrayList();
		persons.add(personDao.findById(73));*/
		for (Person person : persons) {
			try {
				String sap = person.getSap();
				Person ldapPerson = null;
				ldapPerson = ldapPersonLookupService
						.getLdapPersonOnlyBySAP(sap);
				if (ldapPerson == null) {
					// person not found, skip it
					continue;
				}

				person.setDepartment(ldapPerson.getDepartment());
				person.setEmail(ldapPerson.getEmail());
				person.setFirstname(ldapPerson.getFirstname());
				person.setJobTitle(ldapPerson.getJobTitle());
				person.setLastname(ldapPerson.getLastname());
				person.setMiddlename(ldapPerson.getMiddlename());
				person.setState(ldapPerson.getState());
				person.setStreetAddress(ldapPerson.getStreetAddress());
				person.setWorkphone(ldapPerson.getWorkphone());
				person.setZipCode(ldapPerson.getZipCode());

				/*logger.debug(ldapPerson.getDepartment());
				logger.debug(ldapPerson.getEmail());
				logger.debug(ldapPerson.getFirstname());
				logger.debug(ldapPerson.getJobTitle());
				logger.debug(ldapPerson.getLastname());
				logger.debug(ldapPerson.getMiddlename());
				logger.debug(ldapPerson.getState());
				logger.debug(ldapPerson.getStreetAddress());
				logger.debug(ldapPerson.getWorkphone());
				logger.debug(ldapPerson.getZipCode());*/
				personDao.saveOrUpdate(person);
			} catch (Exception e) {

			}
		}
	}

	public PersonDao getPersonDao() {
		return personDao;
	}

	@Autowired(required = true)
	public void setPersonDao(PersonDao personDao) {
		this.personDao = personDao;
	}

	public LdapPersonLookupService getLdapPersonLookupService() {
		return ldapPersonLookupService;
	}

	@Autowired(required = true)
	public void setLdapPersonLookupService(
			LdapPersonLookupService ldapPersonLookupService) {
		this.ldapPersonLookupService = ldapPersonLookupService;
	}
}
