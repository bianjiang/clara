package edu.uams.clara.webapp.common.web.ajax;

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

import edu.uams.clara.webapp.common.domain.usercontext.Role;
import edu.uams.clara.webapp.common.domain.usercontext.UserRole;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/common/web/ajax/UserAjaxControllerTest-context.xml" })
public class UserAjaxControllerTest {

	private final static Logger logger = LoggerFactory
	.getLogger(UserAjaxControllerTest.class);
	

	private UserAjaxController userAjaxController;
	
	@Test
	public void testListAllOrderByName() throws JsonGenerationException, JsonMappingException, IOException{
		List<Role> roles = userAjaxController.getRoles();
		
		//Hibernate.initialize(roles);
		
		for(Role r:roles){
			logger.debug(r.getName());
			//r.getDefaultPermissions().size();
		}
		
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		String json = objectMapper.writeValueAsString(roles);
		
		logger.debug(json);
		
	}
	
	@Test
	public void testCreateUserRole(){
		//UserRole userRole = userAjaxController.createUserRole(1l, 4l, 431l);
		//logger.debug("userRoleId: " + userRole.getId());
	}
	
	@Test
	public void testListUserRoles() throws JsonGenerationException, JsonMappingException, IOException{
		List<UserRole> userRoles = userAjaxController.getUserRoles(1l);
		ObjectMapper objectMapper = new ObjectMapper();
		
		String json = objectMapper.writeValueAsString(userRoles);
		
		logger.debug(json);
	}

	@Autowired(required = true)
	public void setUserAjaxController(UserAjaxController userAjaxController) {
		this.userAjaxController = userAjaxController;
	}

	public UserAjaxController getUserAjaxController() {
		return userAjaxController;
	}
}
