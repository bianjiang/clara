package edu.uams.clara.webapp.common.dao.security.acl;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.common.domain.security.acl.SecurableObject;
import edu.uams.clara.webapp.common.security.acl.util.AclObjectFactory;
import edu.uams.clara.webapp.protocol.domain.Protocol;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/common/dao/security/acl/SecurableObjectDaoTest-context.xml" })
public class SecurableObjectDaoTest {

	private final static Logger logger = LoggerFactory
	.getLogger(SecurableObjectDaoTest.class);

	private SecurableObjectDao securableObjectDao;


	@Test
	public void testGetSecurableObjectByClassAndId(){
		/*
		SecurableObject securableObject =  securableObjectDao.getSecurableObjectByClassAndId(Protocol.class, 1l);

		if(securableObject == null){
			securableObject = securableObjectDao.saveOrUpdate( AclObjectFactory.createSecurableObject(Protocol.class, 1l));
		}
		logger.debug(securableObject.getObjectClass() + ", " + securableObject.getObjectId());
		*/
	}

	@Autowired(required = true)
	public void setSecurableObjectDao(SecurableObjectDao securableObjectDao) {
		this.securableObjectDao = securableObjectDao;
	}

	public SecurableObjectDao getSecurableObjectDao() {
		return securableObjectDao;
	}

}
