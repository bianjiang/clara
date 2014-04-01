package edu.uams.clara.webapp.protocol.dao.thing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.integration.incoming.crimson.dao.CrimsonStudyDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/protocol/dao/thing/CRIMSONIRBStudyTypeTest-context.xml" })
public class CRIMSONIRBStudyTypeTest {
	private final static Logger logger = LoggerFactory
			.getLogger(CRIMSONIRBStudyTypeTest.class);
	
	private CrimsonStudyDao crimsonIRBStudyTypeDao;

	public CrimsonStudyDao getCrimsonIRBStudyTypeDao() {
		return crimsonIRBStudyTypeDao;
	}
	
	
	@Test
	public void testFindStudyByIRBNum() {
		int aaa =(int) crimsonIRBStudyTypeDao.findRegInfobyCtID(433)[0];
		logger.debug(crimsonIRBStudyTypeDao.findCrimosnStatusByCrimsonID(217));
	}

	@Autowired(required = true)
	public void setCrimsonIRBStudyTypeDao(CrimsonStudyDao crimsonIRBStudyTypeDao) {
		this.crimsonIRBStudyTypeDao = crimsonIRBStudyTypeDao;
	}
}
