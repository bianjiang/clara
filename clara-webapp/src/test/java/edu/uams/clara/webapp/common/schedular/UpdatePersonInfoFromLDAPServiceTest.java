package edu.uams.clara.webapp.common.schedular;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.common.scheduler.UpdatePersonInfoFromLDAPService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/common/schedular/UpdatePersonInfoFromLDAPServiceTest-context.xml" })
public class UpdatePersonInfoFromLDAPServiceTest {
	private UpdatePersonInfoFromLDAPService updatePersonInfoFromLDAPService;
	
	@Test
	public void test(){
		updatePersonInfoFromLDAPService.updatePersonFromLDAP();
	}

	public UpdatePersonInfoFromLDAPService getUpdatePersonInfoFromLDAPService() {
		return updatePersonInfoFromLDAPService;
	}

	@Autowired(required=true)
	public void setUpdatePersonInfoFromLDAPService(
			UpdatePersonInfoFromLDAPService updatePersonInfoFromLDAPService) {
		this.updatePersonInfoFromLDAPService = updatePersonInfoFromLDAPService;
	}
}
