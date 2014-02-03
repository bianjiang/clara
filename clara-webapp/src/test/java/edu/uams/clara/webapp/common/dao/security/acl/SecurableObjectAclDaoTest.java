package edu.uams.clara.webapp.common.dao.security.acl;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.common.domain.security.acl.SecurableObject;
import edu.uams.clara.webapp.common.domain.security.acl.SecurableObjectAcl;
import edu.uams.clara.webapp.common.domain.security.acl.enums.Permission;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.security.acl.util.AclObjectFactory;
import edu.uams.clara.webapp.protocol.domain.Protocol;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/common/dao/security/acl/SecurableObjectAclDaoTest-context.xml" })
public class SecurableObjectAclDaoTest {

	private final static Logger logger = LoggerFactory
	.getLogger(SecurableObjectAclDaoTest.class);

	private SecurableObjectAclDao securableObjectAclDao;
	private SecurableObjectDao securableObjectDao;

	@Test
	public void testGetSecurableObjectByClassAndId(){
		/*
		SecurableObject securableObject =  securableObjectDao.getSecurableObjectByClassAndId(Protocol.class, 1l);
		if(securableObject == null){
			securableObject = securableObjectDao.saveOrUpdate( AclObjectFactory.createSecurableObject(Protocol.class, 1l));
		}

		SecurableObjectAcl securableObjectAcl =  securableObjectAclDao.getSecurableObjectAclByOwnerAndSecurableObjectAndPermission(User.class, 1l, securableObject, Permission.READ);
		if(securableObjectAcl == null){
			securableObjectAcl = securableObjectAclDao.saveOrUpdate( AclObjectFactory.createSecurableObjectAcl(securableObject, Permission.READ, User.class, 1l));
		}

		logger.debug(securableObjectAcl.getOwnerClass() + ", " + securableObjectAcl.getOwnerId());
		*/
	}

	@Autowired(required = true)
	public void setSecurableObjectAclDao(SecurableObjectAclDao securableObjectAclDao) {
		this.securableObjectAclDao = securableObjectAclDao;
	}


	public SecurableObjectAclDao getSecurableObjectAclDao() {
		return securableObjectAclDao;
	}

	@Autowired(required = true)
	public void setSecurableObjectDao(SecurableObjectDao securableObjectDao) {
		this.securableObjectDao = securableObjectDao;
	}

	public SecurableObjectDao getSecurableObjectDao() {
		return securableObjectDao;
	}




}
