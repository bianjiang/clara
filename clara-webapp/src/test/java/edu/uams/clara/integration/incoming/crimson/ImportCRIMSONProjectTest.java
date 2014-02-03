package edu.uams.clara.integration.incoming.crimson;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.integration.incoming.crimson.dao.CRIMSONProjectDao;
import edu.uams.clara.integration.incoming.crimson.domain.CRIMSONProject;
import edu.uams.clara.webapp.protocol.dao.thing.GrantDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/incoming/crimson/ImportCRIMSONProjectTest-context.xml" })
public class ImportCRIMSONProjectTest {
	private final static Logger logger = LoggerFactory
			.getLogger(ImportCRIMSONProjectTest.class);

	private ImportCRIMSONProjectService importCRIMSONProjectService;

	private GrantDao grantDao;

	private CRIMSONProjectDao crimsonProjectDao;

	//@Test
	public void testFindCRIMSONProjectByPRN(){
		CRIMSONProject crimsonProject = crimsonProjectDao.findByPRN("04527");
		logger.debug("projectTitle: " + crimsonProject.getTitle());
	}

	@Test
	public void testUpdateGrantWithCRIMSONProject(){
		importCRIMSONProjectService.importProjectToClickGrant();
	}

	public ImportCRIMSONProjectService getImportCRIMSONProjectService() {
		return importCRIMSONProjectService;
	}

	@Autowired(required=true)
	public void setImportCRIMSONProjectService(
			ImportCRIMSONProjectService importCRIMSONProjectService) {
		this.importCRIMSONProjectService = importCRIMSONProjectService;
	}

	public GrantDao getGrantDao() {
		return grantDao;
	}

	@Autowired(required=true)
	public void setGrantDao(GrantDao grantDao) {
		this.grantDao = grantDao;
	}

	public CRIMSONProjectDao getCrimsonProjectDao() {
		return crimsonProjectDao;
	}

	@Autowired(required=true)
	public void setCrimsonProjectDao(CRIMSONProjectDao crimsonProjectDao) {
		this.crimsonProjectDao = crimsonProjectDao;
	}

}
