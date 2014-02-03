package edu.uams.clara.webapp.protocol.dao.protocolform;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/protocol/dao/protocolform/ProtocolFormXmlDataDaoTest-context.xml" })
public class ProtocolFormXmlDataDaoTest {

	private final static Logger logger = LoggerFactory
	.getLogger(ProtocolFormXmlDataDocumentDaoTest.class);

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	@Test
	public void testProtocolFormXmlData(){
		ProtocolFormXmlData protocolXmlData = protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolFormIdAndType(1692, ProtocolFormXmlDataType.MODIFICATION);
		logger.debug("" + protocolXmlData.getId());
	}

	@Autowired(required=true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}
}
