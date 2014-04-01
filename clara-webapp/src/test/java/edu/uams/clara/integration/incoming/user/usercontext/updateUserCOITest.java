package edu.uams.clara.integration.incoming.user.usercontext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.integration.incoming.user.usercontext.service.ImportandUpdateUserCOI;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/incoming/user/usercontext/updateUserCOITest-context.xml" })
public class updateUserCOITest {
	private final static Logger logger = LoggerFactory
			.getLogger(updateUserCOITest.class);
	
	private ImportandUpdateUserCOI importandUpdateUserCOI;


	@Test
	public void test() {
		importandUpdateUserCOI.updateUserCOI();
	}


	public ImportandUpdateUserCOI getImportandUpdateUserCOI() {
		return importandUpdateUserCOI;
	}


	@Autowired(required=true)
	public void setImportandUpdateUserCOI(ImportandUpdateUserCOI importandUpdateUserCOI) {
		this.importandUpdateUserCOI = importandUpdateUserCOI;
	}

}
