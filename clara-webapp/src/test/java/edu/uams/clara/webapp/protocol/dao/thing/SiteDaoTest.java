package edu.uams.clara.webapp.protocol.dao.thing;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.protocol.domain.thing.Site;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/protocol/dao/thing/SiteDaoTest-context.xml" })
public class SiteDaoTest {

	private final static Logger logger = LoggerFactory
	.getLogger(SiteDaoTest.class);

	private SiteDao siteDao;

	@Test
	public void testSearchforSiteByKeywordAndCommon(){
		String keyword = "ACH";
		Boolean common = Boolean.TRUE;
		List<Site> sites = siteDao.searchByKeywordAndCommon(keyword, common);

		for(Site site:sites){
			logger.debug("site: " + site.getSiteName());
		}
	}

	@Autowired(required=true)
	public void setSiteDao(SiteDao siteDao) {
		this.siteDao = siteDao;
	}

	public SiteDao getSiteDao() {
		return siteDao;
	}
}
