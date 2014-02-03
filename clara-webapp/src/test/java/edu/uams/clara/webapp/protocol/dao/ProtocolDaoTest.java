package edu.uams.clara.webapp.protocol.dao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/protocol/dao/ProtocolDaoTest-context.xml" })
public class ProtocolDaoTest {

	private final static Logger logger = LoggerFactory
	.getLogger(ProtocolDaoTest.class);

	private ProtocolDao protocolDao;

	//@Test
	public void testListProtocolsByUser(){
		User u = new User();
		u.setId(1l);
		List<Protocol> protocols = protocolDao.listProtocolsByUser(u);

		for(Protocol p:protocols){
			logger.debug("" + p.getId());
		}
	}


	//@Test
	public void testListLastestProtocolXmlDatas(){
		List<ProtocolFormXmlData> protocolXmlDatas = protocolDao.listLastestProtocolXmlDatas();

		for(ProtocolFormXmlData pXml:protocolXmlDatas){
			logger.debug("" + pXml.getId());
		}
	}


	@Test
	public void testGetLastestProtocolXmlData(){
		 protocolDao.getLatestProtocolFormByProtocolIdAndProtocolFormType(201148,ProtocolFormType.NEW_SUBMISSION);

		//logger.debug("" + protocolXmlDatas.getId());


	}

	@Autowired(required=true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}


	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}
}
