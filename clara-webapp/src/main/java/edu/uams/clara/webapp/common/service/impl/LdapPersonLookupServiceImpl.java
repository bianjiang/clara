package edu.uams.clara.webapp.common.service.impl;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

import edu.uams.clara.webapp.common.dao.usercontext.PersonDao;
import edu.uams.clara.webapp.common.domain.usercontext.Person;
import edu.uams.clara.webapp.common.service.LdapPersonLookupService;

public class LdapPersonLookupServiceImpl implements LdapPersonLookupService {

	private LdapTemplate ldapTemplate;
	private PersonDao personDao;

	private final static Logger logger = LoggerFactory
			.getLogger(LdapPersonLookupService.class);

	public LdapPersonLookupServiceImpl(LdapTemplate ldapTemplate, PersonDao personDao) {
		this.ldapTemplate = ldapTemplate;
		this.personDao = personDao;
	}

	private String getAttribute(String attribute, Attributes attrs)
			throws NamingException {
		return attrs.get(attribute) != null ? attrs.get(attribute).get()
				.toString() : null;
	}
	
	private class PersonAttributesMapper implements AttributesMapper {
		public Object mapFromAttributes(Attributes attrs)
				throws NamingException {

			Person p = null;
			if (getAttribute("samaccountname", attrs) != null) {
				try{
					p = personDao.getPersonByUsername(getAttribute("samaccountname",
						attrs));
				}catch(Exception ex){
					
				}
				
			}
			
			String username = getAttribute("samaccountname", attrs);
			String sap = getAttribute("employeenumber", attrs);
			if(username == null || username.trim().isEmpty()){ // || sap == null || sap.trim().isEmpty()){ // @NOTE: a lot of students don't have sap either, might have to check ldap for this... 
				return null; // no point to return this...
			}
			if (p == null) {
				
				p = new Person();

				p.setUsername(username);
				p.setSap(sap);
				p.setFirstname(getAttribute("givenname", attrs));
				p.setLastname(getAttribute("sn", attrs));
				p.setMiddlename(getAttribute("initials", attrs));
				p.setEmail(getAttribute("mail", attrs));
				p.setWorkphone(getAttribute("telephonenumber", attrs));
				p.setDepartment(getAttribute("department", attrs));
				p.setJobTitle(getAttribute("title", attrs));
				p.setStreetAddress(getAttribute("streetAddress", attrs));
				p.setState(getAttribute("st", attrs));
				p.setZipCode(getAttribute("postalCode", attrs));
			}
			

			return p;

		}
	}

	private class PersonAttributesMapperForLdapOnly implements AttributesMapper {
		public Object mapFromAttributes(Attributes attrs)
				throws NamingException {

			
			String sap = getAttribute("employeenumber", attrs);
				
			Person	p = new Person();

			p.setSap(sap);
			p.setFirstname(getAttribute("givenname", attrs));
			p.setLastname(getAttribute("sn", attrs));
			p.setMiddlename(getAttribute("initials", attrs));
			p.setEmail(getAttribute("mail", attrs));
			p.setWorkphone(getAttribute("telephonenumber", attrs));
			p.setDepartment(getAttribute("department", attrs));
			p.setJobTitle(getAttribute("title", attrs));
			p.setStreetAddress(getAttribute("streetAddress", attrs));
			p.setState(getAttribute("st", attrs));
			p.setZipCode(getAttribute("postalCode", attrs));
			

			return p;

		}
	}

	@Override
	public Person getLdapPersonOnlyBySAP(String sap) {
		Person p = null;
		String filter = "(employeenumber=" + sap + ")";
		List<?> persons = ldapTemplate
				.search("", filter, new PersonAttributesMapperForLdapOnly());
		if (!persons.isEmpty()) {
			p = (Person) persons.get(0);
		}
		return p;
	}
	
	@Override
	public Person getPersonBySAP(String sap) {
		Person p = null;

		String filter = "(employeenumber=" + sap + ")";
		List<?> persons = ldapTemplate
				.search("", filter, new PersonAttributesMapper());
		if (!persons.isEmpty()) {
			p = (Person) persons.get(0);
		}

		return p;
	}

	@Override
	public Person getPersonByUsername(String username) {

		Person p = null;

		String filter = "(samaccountname=" + username + ")";
		try {
			List<?> persons = ldapTemplate.search("", filter,
					new PersonAttributesMapper());
			if (!persons.isEmpty()) {

				p = (Person) persons.get(0);
			}
		} catch (Exception ex) {

		}

		if (p == null) {
			logger.error(username + " user not find");
		}

		return p;
	}

	private @Value("${ldap.enabled}")
	boolean ldapEnabled;

	@Override
	public boolean isEnabled() {
		return ldapEnabled;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Person> searchForPersons(String keyword) {
		String filter = "(&(samAccountType=805306368)(|(sn=" + keyword + "*)(givenname=" + keyword
				+ "*)(mail=" + keyword + "*)))";
		
		logger.debug("filter:" + filter);

		
		List<Person> persons = (List<Person>) ldapTemplate.search("", filter,
				new PersonAttributesMapper());

		persons.remove(null);
		if (logger.isTraceEnabled()) {
			for (Person p : persons) {
				if (p != null) {
					logger.trace("username: " + p.getUsername() + "; sap:" + p.getSap());
				}
			}
		}
		return persons;
	}
}
