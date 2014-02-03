package edu.uams.clara.webapp.common.dao.usercontext;


import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.common.domain.usercontext.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/common/dao/usercontext/UserDaoTest-context.xml" })
public class UserDaoTest {

	private final static Logger logger = LoggerFactory
	.getLogger(UserDaoTest.class);

	private UserDao userDao;


	//@Test
	public void testFinById(){
		User u = userDao.findById(1l);

		logger.debug("u: " + u.getUsername());
	}

	//@Test
	public void testGetUserByUsername(){
		User u = userDao.getUserByUsername("testuser");

		logger.debug(u.getPerson().getLastname() + ", " + u.getPerson().getFirstname());
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

}
