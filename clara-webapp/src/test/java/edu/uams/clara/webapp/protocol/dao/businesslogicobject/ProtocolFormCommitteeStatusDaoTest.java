package edu.uams.clara.webapp.protocol.dao.businesslogicobject;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormCommitteeStatusEnum;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/protocol/dao/businesslogicobject/ProtocolFormCommitteeStatusDaoTest-context.xml" })
public class ProtocolFormCommitteeStatusDaoTest {
	
	private final static Logger logger = LoggerFactory
	.getLogger(ProtocolFormCommitteeStatusDaoTest.class);

	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	
	@Test
	public void testListProtocolFormCommitteeStatus(){
		List<ProtocolFormCommitteeStatus> protocolFormsInReview = protocolFormCommitteeStatusDao.listByCommitteeAndStatus(Committee.IRB_OFFICE, ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		
		for(ProtocolFormCommitteeStatus protocolFormCommitteeStatus:protocolFormsInReview){
			logger.debug("p: " + protocolFormCommitteeStatus.getProtocolForm().getProtocolFormType());
		}
	}
		
	@Autowired(required=true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}


	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}
}
