package edu.uams.clara.webapp.xml.processor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.web.ajax.ProtocolDashboardAjaxControllerTest;
import edu.uams.clara.webapp.xml.processor.impl.BudgetXmlDifferServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/xml/processor/budgetXmlDifferSerivceTest-context.xml" })
public class budgetXmlDifferSerivceTest {
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolDashboardAjaxControllerTest.class);

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	private BudgetXmlDifferServiceImpl budgetXmlDifferServiceImpl;

	@Test
	public void testBudgetDiffer() {


		ProtocolFormXmlData oldBudgetXmlData = getProtocolFormXmlDataDao().findById(11866);
		ProtocolFormXmlData newBudgetXmlData = getProtocolFormXmlDataDao().findById(11868);

		String oldBudgetXml = oldBudgetXmlData.getXmlData();
		String newBudgetXml = newBudgetXmlData.getXmlData();
		oldBudgetXml=budgetXmlDifferServiceImpl.differBudgetXml(oldBudgetXml,newBudgetXml);
	    logger.debug(oldBudgetXml);
		logger.debug(newBudgetXml);

	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}
	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public BudgetXmlDifferServiceImpl getBudgetXmlDifferServiceImpl() {
		return budgetXmlDifferServiceImpl;
	}
	@Autowired(required = true)
	public void setBudgetXmlDifferServiceImpl(BudgetXmlDifferServiceImpl budgetXmlDifferServiceImpl) {
		this.budgetXmlDifferServiceImpl = budgetXmlDifferServiceImpl;
	}




}
