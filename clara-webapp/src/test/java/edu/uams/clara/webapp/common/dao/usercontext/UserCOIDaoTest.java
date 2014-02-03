package edu.uams.clara.webapp.common.dao.usercontext;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.common.domain.usercontext.UserCOI;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/common/dao/usercontext/UserCOIDaoTest-context.xml" })
public class UserCOIDaoTest {

	private final static Logger logger = LoggerFactory
			.getLogger(UserCOIDaoTest.class);
	private UserCOIDao userCOIDao;
	private ResourceLoader resourceLoader;

	@Test
	public void findUserCOI() {
		List<UserCOI> userCOI = userCOIDao.getUserCOIBySAP("00022876");


	}

	public UserCOIDao getUserCOIDao() {
		return userCOIDao;
	}

	@Autowired(required = true)
	public void setUserCOIDao(UserCOIDao userCOIDao) {
		this.userCOIDao = userCOIDao;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

}
