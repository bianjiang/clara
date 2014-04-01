package edu.uams.clara.webapp.common.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.common.domain.usercontext.Person;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/common/service/LdapPersonTest-context.xml" })
public class LdapPersonTest {

	private final static Logger logger = LoggerFactory
			.getLogger(UserServiceTest.class);
	
	private LdapPersonLookupService ldapPersonLookupService;
	
	@Test
	public void testStudentAccount(){
		List<Person> ldapPersons = ldapPersonLookupService.searchForPersons("RAMEY");
		logger.debug("found : " + ldapPersons.size());
		for(Person p:ldapPersons){
			logger.debug("p: " + p.getEmail());
		}
	}

	public LdapPersonLookupService getLdapPersonLookupService() {
		return ldapPersonLookupService;
	}

	@Autowired(required = true)
	public void setLdapPersonLookupService(LdapPersonLookupService ldapPersonLookupService) {
		this.ldapPersonLookupService = ldapPersonLookupService;
	}
	
}
