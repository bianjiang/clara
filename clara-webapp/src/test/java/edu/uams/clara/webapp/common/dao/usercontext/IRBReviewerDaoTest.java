package edu.uams.clara.webapp.common.dao.usercontext;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uams.clara.webapp.protocol.dao.irb.IRBReviewerDao;
import edu.uams.clara.webapp.protocol.domain.irb.IRBReviewer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/common/dao/usercontext/IRBReviewerDaoTest-context.xml" })
public class IRBReviewerDaoTest {

	private final static Logger logger = LoggerFactory
	.getLogger(IRBReviewerDaoTest.class);

	private IRBReviewerDao irbReviewerDao;

	@Test
	public void testlistAllIRBReviewers() throws JsonGenerationException, JsonMappingException, IOException{
		List<IRBReviewer> irbReviewers = irbReviewerDao.listAllIRBReviewers();


		ObjectMapper objectMapper = new ObjectMapper();

		String json = objectMapper.writeValueAsString(irbReviewers);

		logger.debug(json);

	}

	public IRBReviewerDao getIrbReviewerDao() {
		return irbReviewerDao;
	}

	@Autowired(required = true)
	public void setIrbReviewerDao(IRBReviewerDao irbReviewerDao) {
		this.irbReviewerDao = irbReviewerDao;
	}

}
