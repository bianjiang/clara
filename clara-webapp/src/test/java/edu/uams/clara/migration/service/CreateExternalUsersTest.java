package edu.uams.clara.migration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.common.service.UserService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/migration/service/CreateExternalUsersTest-Context.xml" })
public class CreateExternalUsersTest {

	private UserService userService;

	@Test
	public void createExternalUser() {

	}

	public UserService getUserService() {
		return userService;
	}

	@Autowired(required = true)
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

}
