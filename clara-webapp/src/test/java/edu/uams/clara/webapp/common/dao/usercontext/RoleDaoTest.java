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

import edu.uams.clara.webapp.common.domain.usercontext.Role;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/common/dao/usercontext/RoleDaoTest-context.xml" })
public class RoleDaoTest {

	private final static Logger logger = LoggerFactory
	.getLogger(RoleDaoTest.class);
	
	private RoleDao roleDao;

	@Test
	public void testListAllOrderByName() throws JsonGenerationException, JsonMappingException, IOException{
		List<Role> roles = roleDao.listAllOrderByName();
		
		//Hibernate.initialize(roles);
		
		for(Role r:roles){
			logger.debug(r.getName());
			//r.getDefaultPermissions().size();
		}
		
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		String json = objectMapper.writeValueAsString(roles);
		
		logger.debug(json);
		
	}

	@Autowired(required = true)
	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}

	public RoleDao getRoleDao() {
		return roleDao;
	}
	
}
