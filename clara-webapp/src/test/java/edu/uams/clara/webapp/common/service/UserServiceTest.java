package edu.uams.clara.webapp.common.service;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.uams.clara.webapp.common.dao.usercontext.CitiMemberDao;
import edu.uams.clara.webapp.common.dao.usercontext.PersonDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.CitiMember;
import edu.uams.clara.webapp.common.domain.usercontext.Person;
import edu.uams.clara.webapp.common.domain.usercontext.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/common/service/UserServiceTest-context.xml" })
public class UserServiceTest {

	private final static Logger logger = LoggerFactory
			.getLogger(UserServiceTest.class);

	private UserService userService;

	private LdapPersonLookupService ldapPersonLookupService;

	private UserDao userDao;

	private PersonDao personDao;

	private CitiMemberDao citiMemberDao;


	private List<Person> searchForPersons(String keyword) {
		// searchForPersons might give exception
		List<Person> daoPersons =  Lists.newArrayList();
		try {
			daoPersons = personDao.searchForPersons(keyword);
		}catch(Exception ex){
			//don't care
			ex.printStackTrace();
		}
		List<Person> ldapPersons = null;
		try {
			ldapPersons = ldapPersonLookupService.searchForPersons(keyword);
		}catch(Exception ex){
			//don't care
			ex.printStackTrace();
		}

		// dao user shows up first
		List<Person> results = daoPersons;

		if(ldapPersons != null){
			results.addAll(ldapPersons);
		}

		Set<String> saps = Sets.newHashSet();
		Set<String> usernames = Sets.newHashSet();
		Iterator<Person> pIt = results.iterator();
		while(pIt.hasNext()){
			Person p = pIt.next();

			if(p == null || (p.getSap() != null && saps.contains(p.getSap())) || usernames.contains(p.getUsername())){
				pIt.remove();
			}
			if(p != null && p.getSap() != null){
				saps.add(p.getSap());
			}
			if(p != null && p.getUsername() != null){
				usernames.add(p.getUsername());
			}
		}

		return results;

	}

	private List<CitiMember> getCitiTraining(User user){

		logger.debug(user.getProfile());

		return null;
	}

	@Test
	public void testSearchCitiTraining(){
		User user = userDao.findById(72l); //Birgul
		getCitiTraining(user);
	}


	//@Test
	public void testMergeDaoPersonsAndLdapPersons(){
		List<Person> persons = searchForPersons("Paul");
		for(Person p:persons){
			logger.debug("username:" + p.getUsername() + "; sap: " +  p.getSap());
		}
	}
	//@Test
	public void testGetAndUpdateUserByUsername(){
		User u = userService.getAndUpdateUserByUsername("BiasMarcia", true);

		logger.debug(u.getPerson().getLastname() + ", " + u.getPerson().getFirstname());
	}

	//@Test
	public void testGenerateExternalUser(){
		userService.createExternalUser("jwtest", "jw", "test", "1231231211", "iamyuanjiawei@gmail.com", "test");

	}


	@Autowired(required = true)
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public UserService getUserService() {
		return userService;
	}

	@Autowired(required = true)
	public void setLdapPersonLookupService(LdapPersonLookupService ldapPersonLookupService) {
		this.ldapPersonLookupService = ldapPersonLookupService;
	}

	public LdapPersonLookupService getLdapPersonLookupService() {
		return ldapPersonLookupService;
	}
	public PersonDao getPersonDao() {
		return personDao;
	}

	@Autowired(required = true)
	public void setPersonDao(PersonDao personDao) {
		this.personDao = personDao;
	}


	public CitiMemberDao getCitiMemberDao() {
		return citiMemberDao;
	}


	public void setCitiMemberDao(CitiMemberDao citiMemberDao) {
		this.citiMemberDao = citiMemberDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}


	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
}
