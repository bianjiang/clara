package edu.uams.clara.webapp.common.dao.relation;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/common/dao/relation/RelatedObjectDaoTest-context.xml" })
public class RelatedObjectDaoTest {

	private RelatedObjectDao relatedObjectDao;
	private final static Logger logger = LoggerFactory
			.getLogger(RelatedObjectDao.class);

	@Test
	public void test() {
		/*
		logger.debug(relatedObjectDao
				.findRelatedObjectByObjIDTypeandRelatedObjIDType(201519,
						"protocol", 13060, "contract").getCreated()
				+ "");
		*/

	}

	public RelatedObjectDao getRelatedObjectDao() {
		return relatedObjectDao;
	}
	
	@Autowired(required = true)
	public void setRelatedObjectDao(RelatedObjectDao relatedObjectDao) {
		this.relatedObjectDao = relatedObjectDao;
	}

}
