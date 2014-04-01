package edu.uams.clara.integration.incoming.thing.sponsor;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.integration.incoming.crimson.ImportCRIMSONSponsorService;
import edu.uams.clara.webapp.protocol.dao.thing.SponsorDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/incoming/thing/sponsor/SponsorUpdateTest-context.xml" })
public class SponsorUpdateTest {

	private final static Logger logger = LoggerFactory
			.getLogger(SponsorUpdateTest.class);

	private EntityManager em;

	private SponsorDao sponsorDao;

	private ImportCRIMSONSponsorService sponsorUpdateService;
	

	@Test // use ImportCRIMSONSponsorService
	public void sponsorUpdate() {
		sponsorUpdateService.updateSponsorListFromCRIMSON();
		
	}

	public SponsorDao getSponsorDao() {
		return sponsorDao;
	}

	@Autowired(required = true)
	public void setSponsorDao(SponsorDao sponsorDao) {
		this.sponsorDao = sponsorDao;
	}

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

	public ImportCRIMSONSponsorService getSponsorUpdateService() {
		return sponsorUpdateService;
	}

	@Autowired(required = true)
	public void setSponsorUpdateService(ImportCRIMSONSponsorService sponsorUpdateService) {
		this.sponsorUpdateService = sponsorUpdateService;
	}

}
