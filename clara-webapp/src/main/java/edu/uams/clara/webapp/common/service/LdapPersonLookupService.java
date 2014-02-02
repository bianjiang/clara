package edu.uams.clara.webapp.common.service;

import java.util.List;

import edu.uams.clara.webapp.common.domain.usercontext.Person;



public interface LdapPersonLookupService {

	
	/**
	 * search for persons by keywords in ldap server...
	 * @param keyword
	 * @return
	 */
	List<Person> searchForPersons(String keyword);

	Person getPersonBySAP(String sap);
	
	Person getLdapPersonOnlyBySAP(String sap);
	
	boolean isEnabled();

	Person getPersonByUsername(String username);
}
