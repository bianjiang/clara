package edu.uams.clara.webapp.protocol.dao.irb;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.protocol.domain.irb.IRBReviewer;
import edu.uams.clara.webapp.protocol.domain.irb.enums.IRBRoster;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/protocol/dao/irb/IRBReviewerDaoTest-context.xml" })
public class IRBReviewerDaoTest {

	private final static Logger logger = LoggerFactory
	.getLogger(IRBReviewerDaoTest.class);
	
	private IRBReviewerDao irbReviewerDao;
	
	//@Test
	public void testListIRBReviewersByIRBRoster(){
		
		List<IRBReviewer> irbReviewers = irbReviewerDao.listIRBReviewersByIRBRoster(IRBRoster.WEEK_4);
		
		for(IRBReviewer irbReviewer:irbReviewers){
			logger.debug(irbReviewer.getUser().getUsername());
		}
	}

	@Test
	public void testReviewer(){
		List<IRBReviewer> irbReviewers = irbReviewerDao.listAgendaIRBReviewersByItemId(2);
		for(IRBReviewer irbReviewer:irbReviewers){
			logger.debug(irbReviewer.getUser().getUsername());
		}
	}
	
	
	@Autowired(required=true)
	public void setIrbReviewerDao(IRBReviewerDao irbReviewerDao) {
		this.irbReviewerDao = irbReviewerDao;
	}

	public IRBReviewerDao getIrbReviewerDao() {
		return irbReviewerDao;
	}
	
	
}
