package edu.uams.clara.integration.incoming.billingcodes;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.integration.incoming.billingcodes.service.ImportCptCodeService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/incoming/billingcodes/ImportCptCodeServiceTest-context.xml" })
public class ImportCptCodeServiceTest {
	
	private ImportCptCodeService importCptCodeService;
	
	@Test
	public void test(){
		importCptCodeService.run();
	}

	public ImportCptCodeService getImportCptCodeService() {
		return importCptCodeService;
	}

	@Autowired(required = true)
	public void setImportCptCodeService(ImportCptCodeService importCptCodeService) {
		this.importCptCodeService = importCptCodeService;
	}
	
}
