package edu.uams.clara.webapp.xml.processor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.xml.processor.impl.BudgetXmlTransformServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/xml/processor/budgetXmlTransformServiceTest-context.xml" })
public class budgetXmlTransformServiceTest {

	private final static Logger logger = LoggerFactory
			.getLogger(budgetXmlTransformServiceTest.class);

	private BudgetXmlTransformService budgetXmlTransformService;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	@Test
	public void test(){
		String result =budgetXmlTransformService.outputCLARABudgetToPSCTemplate(protocolFormXmlDataDao.findById(10707).getXmlData(), "136961");
		logger.debug(result);
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public BudgetXmlTransformService getBudgetXmlTransformService() {
		return budgetXmlTransformService;
	}

	@Autowired(required = true)
	public void setBudgetXmlTransformService(BudgetXmlTransformService budgetXmlTransformService) {
		this.budgetXmlTransformService = budgetXmlTransformService;
	}

}
